package org.jpab;

import java.util.HashMap;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StreamConfiguration implements PortAudio.Component {

	public enum Mode {
		INPUT_ONLY, OUTPUT_ONLY, BIDIRECTIONAL;
	}
	
	public static class SampleFormat {

		private final static HashMap <Integer, SampleFormat> map = new HashMap <Integer, SampleFormat> ();
		private final static HashSet <SampleFormat> set = new HashSet <SampleFormat> ();
		
		public static final SampleFormat 
			FLOAT_32 = new SampleFormat("32-Bit Float", 0x00000001),
			SIGNED_INTEGER_8 = new SampleFormat("8-Bit Signed Integer", 0x00000010),
			UNSIGNED_INTEGER_8 = new SampleFormat("8-Bit Unsigned Integer", 0x00000020),
			SIGNED_INTEGER_16 = new SampleFormat("16-Bit Signed Integer", 0x00000008),
			SIGNED_INTEGER_24 = new SampleFormat("24-Bit Signed Integer", 0x00000004),
			SIGNED_INTEGER_32 = new SampleFormat("32-Bit Signed Integer", 0x00000002);

		public static Set <SampleFormat> values() {
			return Collections.unmodifiableSet(set);
		}
		
		public static SampleFormat resolve(int code) {
			return map.get(code);
		}
		
		private final int code;
		private final String name;
		
		public SampleFormat(String name, int code) {
			if (map.containsValue(code)) throw new IllegalArgumentException();
			this.name = name;
			this.code = code;
			map.put(code, this);
			set.add(this);
		}

		public int getCode() {
			return code;
		}

		public String getName() {
			return name;
		}
		
		public String toString() {
			return name;
		}
		
	}
	
	private int flags;
	private int inputChannels;
	private Device inputDevice;
	private SampleFormat inputFormat;
	private double inputLatency;
	private Mode mode;
	private int outputChannels;
	private Device outputDevice;
	private SampleFormat outputFormat;
	private double outputLatency;
	private double sampleRate;
	
	public StreamConfiguration() {}

	public int getFlags() {
		return flags;
	}

	public int getInputChannels() {
		return inputChannels;
	}

	public Device getInputDevice() {
		return inputDevice;
	}

	public SampleFormat getInputFormat() {
		return inputFormat;
	}

	public double getInputLatency() {
		return inputLatency;
	}

	public Mode getMode() {
		return mode;
	}

	public int getOutputChannels() {
		return outputChannels;
	}

	public Device getOutputDevice() {
		return outputDevice;
	}

	public SampleFormat getOutputFormat() {
		return outputFormat;
	}

	public double getOutputLatency() {
		return outputLatency;
	}
	
	public double getSampleRate() {
		return sampleRate;
	}
	
	public void isSupported() throws PortAudioException {
		PortAudio.isFormatSupported(serialize());
	}
	
	public void setFlags(int flags) {
		this.flags = flags;
	}

	public void setInputChannels(int inputChannels) {
		this.inputChannels = inputChannels;
	}

	public void setInputDevice(Device inputDevice) {
		this.inputDevice = inputDevice;
	}

	public void setInputFormat(SampleFormat inputFormat) {
		this.inputFormat = inputFormat;
	}

	public void setInputLatency(double inputLatency) {
		this.inputLatency = inputLatency;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public void setOutputChannels(int outputChannels) {
		this.outputChannels = outputChannels;
	}

	public void setOutputDevice(Device outputDevice) {
		this.outputDevice = outputDevice;
	}

	public void setOutputFormat(SampleFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	public void setOutputLatency(double outputLatency) {
		this.outputLatency = outputLatency;
	}

	public void setSampleRate(double sampleRate) {
		this.sampleRate = sampleRate;
	}

	public String toString() {
		return "Port Audio Stream Configuration { \n\tFlags: " + flags + 
		"\n\tInput Channels: " + inputChannels +
		"\n\tInput Device: " + (inputDevice == null ? "NULL" : inputDevice.getID()) +
		"\n\tInput Format: " + inputFormat +
		"\n\tInput Latency: " + inputLatency +
		"\n\tMode: " + mode +
		"\n\tOutput Channels: " + outputChannels +
		"\n\tOutput Device: " + (outputDevice == null ? "NULL" : outputDevice.getID()) +
		"\n\tOutput Format: " + outputFormat +
		"\n\tOutput Latency: " + outputLatency +
		"\n\tSample Rate: " + sampleRate + "\n}";
	}

	protected ByteBuffer serialize() throws PortAudioException {
		if (mode == null || (mode != Mode.INPUT_ONLY && (outputFormat == null || outputDevice == null)) || (mode != Mode.OUTPUT_ONLY && (inputFormat == null || inputDevice == null)))
			throw new PortAudioException("Illegal Stream Configuration!");
		final ByteBuffer buffer = ByteBuffer.allocateDirect(45);
		buffer.order(ByteOrder.nativeOrder());
		buffer.putDouble(inputLatency);
		buffer.putDouble(outputLatency);
		buffer.putDouble(sampleRate);
		buffer.putInt(inputFormat == null ? -1 : inputFormat.getCode());
		buffer.putInt(outputFormat == null ? -1 : outputFormat.getCode());
		buffer.putInt(flags);
		buffer.put((byte) (mode == null ? -1 : mode.ordinal() + 1));
		buffer.put((byte) inputChannels);
		buffer.put((byte) (inputDevice == null ? -1 : inputDevice.getID()));
		buffer.put((byte) outputChannels);
		buffer.put((byte) (outputDevice == null ? -1 : outputDevice.getID()));
		return buffer;
	}
	
}
