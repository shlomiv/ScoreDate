/***********************************************
This file is part of the ScoreDate project (http://www.mindmatter.it/scoredate/).

ScoreDate is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ScoreDate is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ScoreDate.  If not, see <http://www.gnu.org/licenses/>.

**********************************************/

import java.util.Vector;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Line;
import javax.sound.sampled.TargetDataLine;

import javax.sound.sampled.LineUnavailableException;
/*
import javax.sound.sampled.Control;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.EnumControl;
import javax.sound.sampled.FloatControl;
*/
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.synthbot.jasiohost.*;

public class AudioInputController implements AsioDriverListener 
{
	Preferences appPrefs;
	Vector<String> audioDevList = new Vector<String>();
	private byte[] javaSoundBuffer;
	private AsioDriver asioDriver;
	private Set<AsioChannel> asioChannels;
	private float[] AsioBuffer;
	boolean ASIOsupported = false;
	boolean ASIOmode = false;

	Vector<Double> freqList = new Vector<Double>();

	float sampleRate = 44100;
	int sampleSizeInBits = 8;
	int bufferSize = 4096;
	AudioFormat inputFormat;
	TargetDataLine inputLine;
	int sensitivity = 40;
	long latency = 0;
	int previousVolume = 0;

	boolean infoEnabled = false;
	AudioMonitor audioMon;
	int currentVolume = 0;

	private AudioCaptureThread captureThread = null;
	boolean captureStarted = false;

	public AudioInputController(Preferences p)
	{
		appPrefs = p;
		initialize();
	}

	public boolean initialize() 
	{
	    int channels = 1;
	    boolean signed = true;
	    boolean bigEndian = true;
	    inputFormat =  new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	    
	    initFrequenciesList();

	    String userAudioDev = appPrefs.getProperty("audiodevice");
	    audioDevList = getDevicesList(userAudioDev);

		return true;
	}

	public void initFrequenciesList()
	{
		double freqFactor = Math.pow(2, 1.0/12.0); // calculate the factor between frequencies
		double aFreq = 27.50;
		double currFreq = 16.35; // frequency of C0
		
		freqList.clear();
		
		for (int oct = 0; oct < 7; oct++)
		{
			for (int i = 0; i < 12; i++) // 12 notes. Includes semitones
			{
				if (i == 9) // back on track when encounter an A
				{
					currFreq = aFreq;
					aFreq*=2;
				}
				freqList.add(currFreq);
				//System.out.print(" | " + currFreq);
				currFreq *= freqFactor;
			}
			//System.out.print("\n");
		}
	}

	public Vector<String> getDevicesList(String userAudioDev)
	{
		Vector<String> devList = new Vector<String>();
		
		try 
	    {
			for (Mixer.Info thisMixerInfo : AudioSystem.getMixerInfo()) 
		    {
		    	Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);
		    	for (Line.Info thisLineInfo:thisMixer.getTargetLineInfo()) 
		    	{
		    		//System.out.println("Class name: " + thisLineInfo.getLineClass().getName());
		    		if (thisLineInfo.getLineClass().getName().equals("javax.sound.sampled.TargetDataLine")) 
		    		{
				    	System.out.println("Mixer: "+ thisMixerInfo.getDescription() + " ["+thisMixerInfo.getName()+"]");
		    			devList.add(thisMixerInfo.getName());
		    			if (userAudioDev != "" && userAudioDev.equals(thisMixerInfo.getName()))
			    		{
		    				System.out.println("Found the user selected device. Open it !");
		    				if (inputLine!= null && inputLine.isOpen())
		    					inputLine.close();
		    				inputLine = (TargetDataLine)thisMixer.getLine(thisLineInfo);
			    		}
		    			break;
		    		}
		    	}
		    }
	    } catch (Exception e) { e.printStackTrace();}

		// check if ASIO dll is present and if we're running on Windows
		File f = new File("jasiohost.dll");
		if (f.exists() && System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) 
		{
			ASIOsupported = true;
			
			List<String> ASIOlist = AsioDriver.getDriverNames();
			if (ASIOlist.size() > 0)
			{
				for (int i = 0; i < ASIOlist.size(); i++)
				{
					devList.add(ASIOlist.get(i));
					if (userAudioDev != "" && userAudioDev.equals(ASIOlist.get(i)))
		    		{
	    				System.out.println("Found the ASIO user selected device. Open it ! (" + userAudioDev + ")");
	    				if (asioDriver != null)
	    				{
	    					asioDriver.shutdownAndUnloadDriver();
	    					asioDriver = null;
	    				}
	    				try
	    				{
	    					final AsioDriverListener host = this;
	    					System.out.println("----- 1 -----");
	    					asioDriver = AsioDriver.getDriver(userAudioDev);
	    					System.out.println("----- 2 -----");
	    					asioDriver.addAsioDriverListener(host);
	    					System.out.println("----- 3 -----");
	    					asioChannels = new HashSet<AsioChannel>();
	    					System.out.println("----- 4 -----");
	    					ASIOmode = true;
	    				}
	    				catch (AsioException e)
	    				{
	    					System.err.println("[getDevicesList] ASIO exception: " + e);
	    				}
	    				break;
		    		}
				}
			}
		}
		return devList;
	}
	
	public void enableInfo(AudioMonitor am)
	{
		infoEnabled = true;
		audioMon = am;
	}
	
	public int frequencyLookup(double freq)
	{
		int startIdx = 0;
		if (freq > freqList.get(freqList.size() / 2))
			startIdx = freqList.size() / 2;
		for (int i = startIdx; i < freqList.size(); i++)
		{
			if (freq < freqList.get(i))
				return i + 23;
		}
		return 0;
	}
	
	public void setSensitivity(int s)
	{
		System.out.println("Set new sensitivity: " + s);
		sensitivity = s;
	}

	public void startCapture()
	{
		if (ASIOmode == false)
		{
		  if (inputLine == null)
			return;
		  if (inputLine.isOpen() == true)
			inputLine.close();
		  bufferSize = 4096;
	      javaSoundBuffer = new byte[bufferSize];
		  try 
		  {
			inputLine.open(inputFormat);
			inputLine.start();
			captureThread = new AudioCaptureThread();
			captureStarted = true;
			captureThread.start();
		  }
		  catch (LineUnavailableException e) 
		  {
			System.err.println("Line unavailable: " + e);
		  }
		  catch (IllegalArgumentException e)
		  {
			  int channels = 1;
			  boolean signed = true;
			  boolean bigEndian = true;
			  sampleSizeInBits = 16; // 8 doesn't work ? try 16
			  inputFormat =  new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
			  try 
			  {
				inputLine.open(inputFormat);
				inputLine.start();
				captureThread = new AudioCaptureThread();
				captureStarted = true;
				captureThread.start();
			  }
			  catch (LineUnavailableException ex) 
			  {
				System.err.println("Line unavailable: " + ex);
			  }
		  }
		}
		else
		{
			if (asioDriver == null)
			  return;
			asioChannels.clear();
			asioChannels.add(asioDriver.getChannelInput(0));
            bufferSize = asioDriver.getBufferPreferredSize();
            AsioBuffer = new float[bufferSize];
	        sampleRate = (float)asioDriver.getSampleRate();
	        asioDriver.createBuffers(asioChannels);
	        System.out.println("[startCapture] ASIO samplerate: " + sampleRate + ", buffer size: " + bufferSize + " bytes");
	        latency = (int)(asioDriver.getLatencyInput() / sampleRate) * 1000;
	        System.out.println("[startCapture] ASIO latency: " + latency + "ms");
	        asioDriver.start();
		}
	}

	public void stopCapture()
	{
		if (ASIOmode == false)
		{
		  if (inputLine == null || inputLine.isOpen() == false)
			return;
		  try 
		  {
			captureStarted = false;
			inputLine.stop();
			inputLine.close();
		  }
		  catch (Exception e) 
		  {
			System.err.println("[stopCapture] exception: " + e);
		  }
		}
		else
		{
		  if (asioDriver == null)
			return;
		  asioDriver.stop();
		  asioDriver.disposeBuffers();
		  asioChannels.clear();
		}
	}
	
	// *********************** ASIO related events ***************************
	public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannel> channels) 
	{
		for (AsioChannel channelInfo : channels) 
		{
		      channelInfo.read(AsioBuffer);
		}
		performPeakDetection();
	}
	
	public void resetRequest() 
	{
	    System.out.println("resetRequest() callback received.");
	}

	public void bufferSizeChanged(int bufferSize) 
	{
	    System.out.println("bufferSizeChanged() callback received.");
	}

	public void latenciesChanged(int inputLatency, int outputLatency) 
	{
	    System.out.println("latenciesChanged() callback received.");
	}

	public void resyncRequest() 
	{
		System.out.println("resyncRequest() callback received.");
	}

	public void sampleRateDidChange(double sampleRate) 
	{
		System.out.println("sampleRateDidChange() callback received.");
	}
	// *********************************************************************
	
	public void performPeakDetection()
	{
		int bufLength = 0;
		if (ASIOmode == false)
			bufLength = javaSoundBuffer.length;
		else
			bufLength = AsioBuffer.length;
		
		DoubleFFT_1D fft = new DoubleFFT_1D(bufLength);
		double[] audioDataDoubles = new double[bufLength*2];

	    currentVolume = 0;
	    if (ASIOmode == false)
	    {
	      if (sampleSizeInBits == 8)
	      {
    	    for (int i = 0, j = 0; i < bufLength; i++, j+=2)
		    {
    	    	if (infoEnabled == true && javaSoundBuffer[i] > currentVolume)
    	    		currentVolume = (int)javaSoundBuffer[i];
    	    	if (javaSoundBuffer[i] < -5 || javaSoundBuffer[i] > 5)
    	    		audioDataDoubles[j] = (double)javaSoundBuffer[i]; // real part
    	    	else
    	    		audioDataDoubles[j] = 0;
    	    	audioDataDoubles[j + 1] = 0; // imaginary part
		    }
	      }
	      else if (sampleSizeInBits == 16)
	      {
	        for (int j = 0; j < bufLength; j+=2) // convert audio data to double[] real, imaginary
	        {
	        	int sampleInt = javaSoundBuffer[j] << 8 + javaSoundBuffer[j + 1];
	        	if (infoEnabled == true && sampleInt > currentVolume)
	        		currentVolume = sampleInt;
	    		
	        	audioDataDoubles[j] = (double)sampleInt; // real part
	        	audioDataDoubles[j + 1] = 0; // imaginary part
	        }
	      }
	    	   
	    }
	    else // ASIO: convert from float[] to double[] real, imaginary
	    {
	      for (int i = 0, j = 0; i < bufLength; i++, j+=2)
		  {
	    	if (infoEnabled == true && AsioBuffer[i] > currentVolume)
	    		currentVolume = (int)AsioBuffer[i];
	    	audioDataDoubles[j] = (double)AsioBuffer[i]; // real part
	    	audioDataDoubles[j + 1] = 0; // imaginary part
		  }
	    }
	    if (infoEnabled == true)
	    	audioMon.showVolume(currentVolume);

	    fft.complexForward(audioDataDoubles);
	    
	    // calculate vector magnitude and extract highest peak
	    double[] magnitude = new double[bufLength];
	    double peak = 0;
	    int peakIdx = 0;
	    for (int j = 0, i = 0; j < bufLength*2; j+=2, i++)
	    {				    	
	    	magnitude[i] = Math.sqrt(audioDataDoubles[j]*audioDataDoubles[j] + audioDataDoubles[j+1]*audioDataDoubles[j+1]);
	    	if ( magnitude[i] > peak)
	    	{
	    		peak = magnitude[i];
	    		peakIdx = i;
	    	}
	    }
	    
	    double frequency = (sampleRate * peakIdx) / (bufLength * 2);
	    if (frequency > 2000)
	    	return;
	    if (infoEnabled == true)
	    	audioMon.showSpectrum(magnitude);
	    //System.out.println("[AudioCaptureThread] FFT took " + (System.currentTimeMillis() - time) + "ms");
	    System.out.println("[AudioCaptureThread] Peak at: " + frequency + "Hz (value: " + peak + ")");
	    
	    if ( currentVolume - previousVolume > sensitivity)
		{
			int pitch = frequencyLookup(frequency);
			if (infoEnabled == true)
				audioMon.showPitch(pitch);
		}
		previousVolume = currentVolume;
	}
	
	
	// ************************** capture thread ******************************
	private class AudioCaptureThread extends Thread 
	{
		int readBytes = 0;
		boolean checkLatency = true;
		
		public AudioCaptureThread()
		{
			System.out.println("[AudioCaptureThread] created");
		}
		
		public void saveToFile(byte buf[])
		{
			FileWriter out = null;
			try
			{
				out = new FileWriter("audioCap.wav", true);
				String str = new String(buf); //using the platform's default charset
				char cbuf[] = new char[bufferSize];
				cbuf = str.toCharArray();
				out.write(cbuf);
			} catch (IOException e) { }
		}
		
		public void run() 
		{
			System.out.println("[AudioCaptureThread] started");
			while(captureStarted)
			{
				if (checkLatency == true)
					latency = System.currentTimeMillis();
				readBytes = inputLine.read(javaSoundBuffer, 0, javaSoundBuffer.length);
				if (checkLatency == true)
				{
					latency = System.currentTimeMillis() - latency;
					System.out.println("[AudioCaptureThread] latency = " + latency);
					checkLatency = false;
				}
					
				if (readBytes > 0)
				{
					//long time = System.currentTimeMillis();
					//System.out.println("[AudioCaptureThread] got " + readBytes + " bytes);
					//saveToFile(buffer);
					performPeakDetection();
				}
			}
		}
	}
}
