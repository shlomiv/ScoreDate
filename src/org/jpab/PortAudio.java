package org.jpab;

import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public final class PortAudio 
{
	public interface Component {}	
	
	private static HashMap <Integer, Stream> streams = new HashMap <Integer, Stream> ();
	
    public static void load(File file) throws UnsatisfiedLinkError 
    {
        try {
                System.load(file.getCanonicalPath());
        } catch (IOException ex) {
                UnsatisfiedLinkError error = new UnsatisfiedLinkError();
                error.initCause(ex);
                throw error;
        }
    }

	static {
		String LIBS_PATH = "libs";
		String WIN32_ARCH_PATH = "win32";
		String WIN64_ARCH_PATH = "win64";
		String LINUX_ARCH_PATH = "linux";
		File directory = null;
		String arch = System.getProperty("sun.arch.data.model");
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win") == true)
		{
			if (arch.equals("64"))
			{
				directory = new File(LIBS_PATH + File.separator + WIN64_ARCH_PATH + File.separator);
				load(new File(directory, System.mapLibraryName("portaudio_x64")));
			}
			else
			{
				directory = new File(LIBS_PATH + File.separator + WIN32_ARCH_PATH + File.separator);
				load(new File(directory, System.mapLibraryName("portaudio_x86")));
			}
		}
		if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0)
			directory = new File(LIBS_PATH + File.separator + LINUX_ARCH_PATH + File.separator);
		
		load(new File(directory, System.mapLibraryName("PortAudioJNI")));
	}
	
	protected static int callback(int id, ByteBuffer input, ByteBuffer output) {
		if (input != null) {
			input.limit(input.capacity());
			input = input.asReadOnlyBuffer();
		}
		return streams.get(id).callback.callback(input, output).ordinal();
	}
	
	protected static void hook(int id) {
		streams.get(id).hook.run();
	}
	
	private PortAudio() {}

	public static Stream createStream(StreamConfiguration configuration, Callback callback, Runnable hook) throws PortAudioException {
		assert(configuration != null && callback != null && hook != null);
		final int id = openStream(configuration.serialize());
		final Stream stream = new Stream(callback, configuration, hook, id);
		streams.put(id, stream);
		return stream;
	}

	public static HostAPI getDefaultHostAPI() throws PortAudioException {
		final ByteBuffer buffer = getDefaultHostAPIAsBuffer();
		buffer.order(ByteOrder.nativeOrder());
		final HostAPI api = new HostAPI(buffer);
		free(buffer);
		return api;
	}
	
	public static StreamConfiguration getDefaultStreamConfiguration(StreamConfiguration.Mode mode) throws PortAudioException {
		final StreamConfiguration configuration = new StreamConfiguration();
		configuration.setMode(mode);
		final HostAPI hostApi = getDefaultHostAPI();
		double sampleRate = Double.MAX_VALUE;
		if (mode != StreamConfiguration.Mode.OUTPUT_ONLY) {
			final Device inputDevice = hostApi.getDefaultInputDevice();
			configuration.setInputDevice(inputDevice);
			configuration.setInputChannels(1);
			configuration.setInputLatency(inputDevice.getDefaultHighInputLatency());
			configuration.setInputFormat(StreamConfiguration.SampleFormat.SIGNED_INTEGER_16);
			sampleRate = inputDevice.getDefaultSampleRate();
		}
		if (mode != StreamConfiguration.Mode.INPUT_ONLY) {
			final Device outputDevice = hostApi.getDefaultOutputDevice();
			configuration.setOutputDevice(outputDevice);
			configuration.setOutputChannels(2);
			configuration.setOutputLatency(outputDevice.getDefaultHighOutputLatency());
			configuration.setOutputFormat(StreamConfiguration.SampleFormat.SIGNED_INTEGER_16);
			sampleRate = Math.min(sampleRate, outputDevice.getDefaultSampleRate());
		}
		configuration.setSampleRate(sampleRate);
		return configuration;
	}
	
	public static List <Device> getDevices() throws PortAudioException {
		ArrayList <Device> devices = new ArrayList <Device> ();
		ByteBuffer data = getDevicesAsBuffer();
		data.order(ByteOrder.nativeOrder());
		try {
			while (data.remaining() > 0)
				devices.add(new Device(data));
		} finally {
			free(data);
		}
		return devices;
	}
	
	public static List <HostAPI> getHostAPIs() throws PortAudioException {
		ArrayList <HostAPI> hostAPIs = new ArrayList <HostAPI> ();
		ByteBuffer data = getHostAPIsAsBuffer();
		data.order(ByteOrder.nativeOrder());
		try {
			while (data.remaining() > 0)
				hostAPIs.add(new HostAPI(data));
		} finally {
			free(data);
		}
		return hostAPIs;
	}
	
	public static native int getVersion();
	public static native String getVersionText();
	public static native void initialize() throws PortAudioException;
	public static native void terminate() throws PortAudioException;
	protected static native void abortStream(int id) throws PortAudioException;
	protected static native void closeStream(int id) throws PortAudioException;
	protected static native void free(ByteBuffer buffer);
	protected static native ByteBuffer getDefaultHostAPIAsBuffer() throws PortAudioException;
	
	protected static Device getDevice(int index) throws PortAudioException {
		final ByteBuffer buffer = getDeviceAsBuffer(index);
		buffer.order(ByteOrder.nativeOrder());
		final Device device = new Device(buffer);
		free(buffer);
		return device;
	}
	
	protected static native ByteBuffer getDeviceAsBuffer(int index) throws PortAudioException;
	protected static native ByteBuffer getDevicesAsBuffer() throws PortAudioException;
	
	protected static HostAPI getHostAPI(int index) throws PortAudioException {
		final ByteBuffer buffer = getHostAPIAsBuffer(index);
		buffer.order(ByteOrder.nativeOrder());
		final HostAPI api = new HostAPI(buffer);
		free(buffer);
		return api;
	}
	
	protected static native ByteBuffer getHostAPIAsBuffer(int index) throws PortAudioException;
	protected static native ByteBuffer getHostAPIsAsBuffer() throws PortAudioException;
	protected static native ByteBuffer getHostAPIsDevicesAsBuffer(int hostIndex) throws PortAudioException;
	protected static native double getStreamCpuLoad(int id) throws PortAudioException;
	protected static native double getStreamTime(int id) throws PortAudioException;
	protected static native void isFormatSupported(ByteBuffer configuration) throws PortAudioException;
	protected static native boolean isStreamActive(int id) throws PortAudioException;
	protected static native boolean isStreamStopped(int id) throws PortAudioException;
	protected static native int openStream(ByteBuffer configuration) throws PortAudioException;
	protected static native void startStream(int id) throws PortAudioException;
	protected static native void stopStream(int id) throws PortAudioException;
	
}