package org.jpab;

import java.nio.ByteBuffer;

/**
 * The Device class represents an audio device usable by the system for playback
 * or recording.
 * 
 * Note that there is no mechanism in place to update the details of a device
 * object in the event of a change in the underlying library. Device objects
 * should therefore be thought of as accurate only at the moment they were
 * created, though in practice the details are unlikely to change.
 * 
 * @author Ryan Holdren
 */
public class Device implements PortAudio.Component {

	private final double defaultHighInputLatency;
	private final double defaultHighOutputLatency;
	private final double defaultLowInputLatency;
	private final double defaultLowOutputLatency;
	private final double defaultSampleRate;
	private final int hostAPI;
	private final int id;
	private final int maxInputChannels;
	private final int maxOutputChannels;
	private final String name;
	
	protected Device(ByteBuffer data) {
		this.defaultHighInputLatency = data.getDouble();
		this.defaultHighOutputLatency = data.getDouble();
		this.defaultLowInputLatency = data.getDouble();
		this.defaultLowOutputLatency = data.getDouble();
		this.defaultSampleRate = data.getDouble();
		this.id = data.get();
		this.hostAPI = data.get();
		this.maxInputChannels = data.get();
		this.maxOutputChannels = data.get();
		final byte[] bytes = new byte[data.get()];
		data.get(bytes);
		this.name = new String(bytes);
	}
	
	public double getDefaultHighInputLatency() {
		return defaultHighInputLatency;
	}

	public double getDefaultHighOutputLatency() {
		return defaultHighOutputLatency;
	}

	public double getDefaultLowInputLatency() {
		return defaultLowInputLatency;
	}

	public double getDefaultLowOutputLatency() {
		return defaultLowOutputLatency;
	}

	public double getDefaultSampleRate() {
		return defaultSampleRate;
	}

	public HostAPI getHostAPI() throws PortAudioException {
		return PortAudio.getHostAPI(hostAPI);
	}

	public int getMaxInputChannels() {
		return maxInputChannels;
	}

	public int getMaxOutputChannels() {
		return maxOutputChannels;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return "Port Audio Device {\n\tDefault High Input Latency: " + defaultHighInputLatency + 
		"\n\tDefault High Output Latency: " + defaultHighOutputLatency + 
		"\n\tDefault Low Input Latency: " + defaultLowInputLatency + 
		"\n\tDefault Low Output Latency: " + defaultLowOutputLatency + 
		"\n\tDefault Sample Rate: " + defaultSampleRate + 
		"\n\tHost API ID: " + hostAPI + 
		"\n\tID: " + id + 
		"\n\tMax Input Channels: " + maxInputChannels +
		"\n\tMax Output Channels: " + maxOutputChannels +
		"\n\tName: " + name + "\n}\n";
	}

	protected int getID() {
		return id;
	}

}
