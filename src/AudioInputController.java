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
	private AsioDriver asioDriver;
	boolean ASIOsupported = false;
	boolean isCurrentASIO = false;
	
	Vector<Double> freqList = new Vector<Double>();
	
	float sampleRate = 8000;
	AudioFormat inputFormat;
	TargetDataLine inputLine;
	int sensitivity = 10;
	long latency = 0;
	
	boolean infoEnabled = false;
	AudioMonitor audioMon;
	double[] spctrumInfo = new double[16];
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
	    int sampleSizeInBits = 8;
	    int channels = 1;
	    boolean signed = true;
	    boolean bigEndian = true;
	    inputFormat =  new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	    
	    initFrequenciesList();
	    
	    /*
	    try 
	    {
	      System.out.println("OS: "+System.getProperty("os.name")+" " + System.getProperty("os.version")+"/" + System.getProperty("os.arch")+"\nJava: "+
	    	      System.getProperty("java.version")+" ("+ System.getProperty("java.vendor")+")\n");
	      for (Mixer.Info thisMixerInfo : AudioSystem.getMixerInfo()) 
	      {
	    	System.out.println("Mixer: "+ thisMixerInfo.getDescription() + " ["+thisMixerInfo.getName()+"]");
	    	Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);
	    	for (Line.Info thisLineInfo:thisMixer.getSourceLineInfo()) 
	    	{
	    		if (thisLineInfo.getLineClass().getName().equals("javax.sound.sampled.Port")) 
	    		{
	    			Line thisLine = thisMixer.getLine(thisLineInfo);
	    	        thisLine.open();
	    	        System.out.println("  Source Port: " + thisLineInfo.toString());
	    	        for (Control thisControl : thisLine.getControls()) 
	    	        {
	    	        	System.out.println(AnalyzeControl(thisControl));
	    	        }
	    	        thisLine.close();
	    	    }
	    	}
	    	for (Line.Info thisLineInfo:thisMixer.getTargetLineInfo()) 
	    	{
    	    	if (thisLineInfo.getLineClass().getName().equals("javax.sound.sampled.Port")) 
    	    	{
    	            Line thisLine = thisMixer.getLine(thisLineInfo);
    	            thisLine.open();
    	            System.out.println("  Target Port: " + thisLineInfo.toString());
    	            for (Control thisControl : thisLine.getControls()) 
    	            {
    	            	System.out.println(AnalyzeControl(thisControl));
    	            }
    	            thisLine.close();
    	        }
	    	}
    	  }
    	} catch (Exception e) { e.printStackTrace();} 
    	*/
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
		    				inputLine = (TargetDataLine)thisMixer.getLine(thisLineInfo); // either way...
			    			//inputLine = (TargetDataLine)AudioSystem.getLine(thisLineInfo);
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
	    				System.out.println("Found the ASIO user selected device. Open it !");
	    				if (asioDriver != null)
	    				{
	    					asioDriver.shutdownAndUnloadDriver();
	    					asioDriver = null;
	    				}
	    				try
	    				{
	    					final AsioDriverListener host = this;
	    					asioDriver = AsioDriver.getDriver(userAudioDev);
	    					asioDriver.addAsioDriverListener(host);
	    					isCurrentASIO = true;
	    				}
	    				catch (AsioException e)
	    				{
	    					
	    				}
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
		if (inputLine == null)
			return;
		if (inputLine.isOpen() == true)
			inputLine.close();
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

	}

	public void stopCapture()
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
	
	// *********************** ASIO related events ***************************
	public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannel> channels) 
	{

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
	
	
	// ************************** capture thread ******************************
	private class AudioCaptureThread extends Thread 
	{
		int bufferSize = 1024; //(int) inputFormat.getSampleRate() * inputFormat.getFrameSize();
		byte buffer[] = new byte[bufferSize];
		int readBytes = 0;
		FileWriter out = null;
		char cbuf[] = new char[bufferSize];
		boolean checkLatency = true;
		
		public AudioCaptureThread()
		{
			System.out.println("[AudioCaptureThread] created");
			try
			{
				out = new FileWriter("audioCap.wav", true);
			}
			catch (IOException e) { }
		}
		
		public void saveToFile(byte buffer[])
		{
			try
			{
				String str = new String(buffer); //using the platform's default charset
				cbuf = str.toCharArray();
				out.write(cbuf);
			} catch (IOException e) { }
		}
		
		public void run() 
		{
			System.out.println("[AudioCaptureThread] started");
			DoubleFFT_1D fft = new DoubleFFT_1D(buffer.length);
			while(captureStarted)
			{
				if (checkLatency == true)
					latency = System.currentTimeMillis();
				readBytes = inputLine.read(buffer, 0, buffer.length);
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

				    double[] audioDataDoubles = new double[buffer.length*2];

				    currentVolume = 0;
				    for (int j = 0; j < buffer.length; j+=2) // convert audio data in double[] real, imaginary
				    {
				    	if (buffer[j] < -sensitivity || buffer[j] > sensitivity) // noise reduction ?
				    	{
				    		if (infoEnabled == true && buffer[j] > currentVolume)
				    			currentVolume = buffer[j];
				    		
				    		audioDataDoubles[j] = (double)buffer[j]; // real part
				    	}
				    	else
				    		audioDataDoubles[j] = 0;
				    	audioDataDoubles[j + 1] = 0; // imaginary part
				    }
				    if (infoEnabled == true)
				    	audioMon.showVolume(currentVolume);

				    fft.complexForward(audioDataDoubles);
				    
				    // calculate vector magnitude and extract highest peak
				    double[] magnitude = new double[buffer.length];
				    double peak = 0;
				    int peakIdx = 0;
				    for (int j = 0, i = 0; j < buffer.length*2; j+=2, i++)
				    {				    	
				    	magnitude[i] = Math.sqrt(audioDataDoubles[j]*audioDataDoubles[j] + audioDataDoubles[j+1]*audioDataDoubles[j+1]);
				    	if ( magnitude[i] > peak)
				    	{
				    		peak = magnitude[i];
				    		peakIdx = i;
				    	}
				    }
				    
				    double frequency = (sampleRate * peakIdx) / (buffer.length * 2);
				    if (frequency > 2000)
				    	continue;
				    if (infoEnabled == true)
				    	audioMon.showSpectrum(magnitude);
				    //System.out.println("[AudioCaptureThread] FFT took " + (System.currentTimeMillis() - time) + "ms");
				    System.out.println("[AudioCaptureThread] Peak at: " + frequency + "Hz (value: " + peak + ")");
				    
				    int pitch = frequencyLookup(frequency);
				    if (infoEnabled == true)
				    	audioMon.showPitch(pitch);
				}
			}
		}
	}

/*
	public static String AnalyzeControl(Control thisControl) 
	{
	    String type = thisControl.getType().toString();
	    if (thisControl instanceof BooleanControl) 
	    {
	      return "    Control: "+type+" (boolean)"; 
	    }
	    if (thisControl instanceof CompoundControl) 
	    {
	      System.out.println("    Control: "+type+ " (compound - values below)");
	      String toReturn = "";
	      for (Control children: ((CompoundControl)thisControl).getMemberControls()) 
	      {
	        toReturn+="  "+AnalyzeControl(children)+"\n";
	      }
	      return toReturn.substring(0, toReturn.length()-1);
	    }
	    if (thisControl instanceof EnumControl) 
	    {
	      return "    Control:"+type+" (enum: "+thisControl.toString()+")";
	    }
	    if (thisControl instanceof FloatControl) 
	    {
	      return "    Control: "+type+" (float: from "+ ((FloatControl) thisControl).getMinimum()+" to "+ ((FloatControl) thisControl).getMaximum()+")";
	    }
	    return "    Control: unknown type";
	}
*/
}
