package org.jpab;

public final class Stream implements PortAudio.Component {
	
	protected final Callback callback;
	private final StreamConfiguration configuration;
	protected final Runnable hook;
	private final int id;
	
	protected Stream(Callback callback, StreamConfiguration configuration, Runnable hook, int id) throws PortAudioException {
		this.callback = callback;
		this.configuration = configuration;
		this.hook = hook;
		this.id = id;
	}

	public void abort() throws PortAudioException {
		PortAudio.abortStream(id);
	}

	public void close() throws PortAudioException {
		PortAudio.closeStream(id);
	}
	
	public StreamConfiguration getConfiguration() {
		return configuration;
	}
	
	public double getCpuLoad() throws PortAudioException {
		return PortAudio.getStreamCpuLoad(id);
	}
	
	public double getTime() throws PortAudioException {
		return PortAudio.getStreamTime(id);
	}

	public boolean isActive() throws PortAudioException {
		return PortAudio.isStreamActive(id);
	}

	public boolean isStopped() throws PortAudioException {
		return PortAudio.isStreamStopped(id);
	}

	public void start() throws PortAudioException {
		PortAudio.startStream(id);
	}

	public void stop() throws PortAudioException {
		PortAudio.stopStream(id);
	}

	public String toString() {
		return "Port Audio Stream #" + id;
	}

	protected int getId() {
		return id;
	}
	
}
