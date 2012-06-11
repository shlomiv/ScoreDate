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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

import java.io.File;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.io.FileOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jpab.*;
import org.jpab.StreamConfiguration.SampleFormat;

public class AudioInputController
{
	Preferences appPrefs;
	Vector<Double> freqList = new Vector<Double>(); // vector holding the generated frequencies lookup table
	Vector<String> audioDevList = new Vector<String>(); // list of available device (Java + ASIO)
	
	// PortAudio variables
	Device paInputDev = null;
	Stream paStream = null;
	//private float[] PortAudioFFTBuffer; // buffer on which FFT is performed. Try to reach 4k
	//private int PortAudioBufferSize = 0; // buffer received from ASIO. Can have any user-defined size
	//private int PortAudioBufferMax = 0;  // number of ASIO buffer to accumulate into asioSoundBuffer
	//private int PortAudioBufferCount = 0; // counter of cumulative ASIO buffers

	float sampleRate = 44100;
	int sampleSizeInBits = 16;
	int bufferSize = 4096;
	AudioFormat inputFormat;
	TargetDataLine inputLine;
	int sensitivity = 40;
	long latency = 0;
	int previousVolume = 0;

	boolean infoEnabled = false;
	AudioMonitor audioMon;
	int currentVolume = 0;

	//private AudioCaptureThread captureThread = null;
	boolean captureStarted = false;

	public AudioInputController(Preferences p)
	{
		appPrefs = p;
		initialize();
	}

	public boolean initialize() 
	{
	    initFrequenciesList();

	    String userAudioDev = appPrefs.getProperty("inputDevice");
		if (userAudioDev == "-1" || userAudioDev.split(",")[0].equals("MIDI"))
			return false;
		
		int audioDevIndex = Integer.parseInt(userAudioDev.split(",")[1]);
			
	    audioDevList = getDevicesList(audioDevIndex);

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

	public Vector<String> getDevicesList(int devIdx)
	{
		int tmpIdx = 0;
		Vector<String> devList = new Vector<String>();

		try {
			PortAudio.initialize();
			for (Device device : PortAudio.getDevices()) 
			{
				if (device.getMaxInputChannels() > 0)
				{
					//System.out.println(device);
					devList.add(device.getName());
					if (tmpIdx == devIdx)
						paInputDev = device;
					tmpIdx++;
				}
			}
		} catch (PortAudioException e) { 
			e.printStackTrace();
		};
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
		sensitivity = 100 - s;
	}

	public void startCapture()
	{
		if (captureStarted == true)
		{
			try {
				paStream.stop();
			} catch (PortAudioException ex) {  }
			paStream = null;
		}
		StreamConfiguration InputStream = new StreamConfiguration();
		InputStream.setMode(StreamConfiguration.Mode.INPUT_ONLY);
		InputStream.setInputDevice(paInputDev);
		if (sampleSizeInBits == 16)
			InputStream.setInputFormat(SampleFormat.SIGNED_INTEGER_16);
		else
			InputStream.setInputFormat(SampleFormat.SIGNED_INTEGER_8);
		//InputStream.setSampleRate(paInputDev.getDefaultSampleRate());
		InputStream.setSampleRate(sampleRate);
		InputStream.setInputLatency(paInputDev.getDefaultLowInputLatency());
		InputStream.setInputChannels(1);
		try {
			paStream = PortAudio.createStream(InputStream, new Callback() {
			public State callback(ByteBuffer input, ByteBuffer output) {
				System.out.println("Input buffer received ! Size: " + input.capacity() );
				performPeakDetection(input); // <------- perform magic here :)
				return State.RUNNING;
			  }
			}, new Runnable() {
				public void run() {
					try {
						PortAudio.terminate();
					} catch (PortAudioException ignore) { ignore.printStackTrace(); }
				}
			});
			paStream.start();
			//Thread.sleep(24000);
		} catch (PortAudioException ex) {  }
		captureStarted = true;
	}

	public void stopCapture()
	{
		try {
			if (paStream != null)
				paStream.stop();
		} catch (PortAudioException ex) {  }
		paStream = null;
		captureStarted = false;
	}
	
	public void saveToFile(ByteBuffer buf)
	{
		File file = new File("audioCap.wav");

		// Set to true if the bytes should be appended to the file;
		// set to false if the bytes should replace current bytes
		// (if the file exists)
		boolean append = true;

		try {
		    // Create a writable file channel
		    FileChannel wChannel = new FileOutputStream(file, append).getChannel();

		    // Write the ByteBuffer contents; the bytes between the ByteBuffer's
		    // position and the limit is written to the file
		    wChannel.write(buf);

		    // Close the file
		    wChannel.close();
		} catch (IOException e) { }
	}
	
	private void performPeakDetection(ByteBuffer tmpBuf)
	{
		int bufLength = tmpBuf.capacity();
		//saveToFile(tmpBuf); // Just for debug: this call prevents the FFT to work
		System.out.println("Performing FFT on " + bufLength + " bytes");
		DoubleFFT_1D fft = new DoubleFFT_1D(bufLength);
		double[] audioDataDoubles = new double[bufLength*2];

	    currentVolume = 0;
	    
        if (sampleSizeInBits == 8)
	    {
    	    for (int i = 0, j = 0; i < bufLength; i++, j+=2)
		    {
    	    	byte tmpByte = tmpBuf.get();
    	    	if (infoEnabled == true && tmpByte > currentVolume)
    	    		currentVolume = (int)tmpByte;
    	    	if (tmpByte < -5 || tmpByte > 5)
    	    		audioDataDoubles[j] = (double)tmpByte; // real part
    	    	else
    	    		audioDataDoubles[j] = 0;
    	    	audioDataDoubles[j + 1] = 0; // imaginary part
		    }
	    }
	    else if (sampleSizeInBits == 16)
	    {
	        for (int j = 0; j < bufLength; j+=2) // convert audio data to double[] real, imaginary
	        {
	        	byte tmpByteMSB = tmpBuf.get();
	        	byte tmpByteLSB = tmpBuf.get();
	        	int sampleInt = tmpByteMSB << 8 + tmpByteLSB;
	        	if (infoEnabled == true && sampleInt > currentVolume)
	        		currentVolume = sampleInt;
	    		
	        	audioDataDoubles[j] = (double)sampleInt; // real part
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
/*
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
				String str = new String(buf);
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
*/
}
