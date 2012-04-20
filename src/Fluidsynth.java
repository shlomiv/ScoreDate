import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.sound.midi.ShortMessage;

public class Fluidsynth {

	private static final int NAME_MAX_LENGTH = 32;
	
	private ByteBuffer context;

	public Fluidsynth() throws IllegalStateException, IOException {
		this("", 16, null);
	}

	public Fluidsynth(String name, int channels, String audioDriver)
			throws IllegalStateException, IOException {
		this(name, 1, channels, 256, 44100.0f, audioDriver, null, 8, 512, 0.5f,
				0.5f, 0.5f, 0.5f, 0.5f);
	}

	public Fluidsynth(String name, int cores, int channels, int polyphony,
			float sampleRate, String audioDriver, String audioDevice,
			int buffers, int bufferSize, float overflowAge,
			float overflowPercussion, float overflowReleased,
			float overflowSustained, float overflowVolume) throws IOException {

		name = name.substring(0, Math.min(name.length(), NAME_MAX_LENGTH));

		context = init(name, cores, channels, polyphony, sampleRate,
				audioDriver, audioDevice, buffers, bufferSize, overflowAge,
				overflowPercussion, overflowReleased, overflowSustained,
				overflowVolume);
	}

	public void soundFontLoad(File soundfont) throws IOException {
		soundFontLoad(context, soundfont.getAbsolutePath());
	}
	
	public List<String> getSoundfontPrograms()
	{
		return getProgramsList(context);
	}

	public void setGain(float gain) {
		setGain(context, gain);
	}

	public void setInterpolate(int number) {
		setInterpolate(context, number);
	}

	public void setReverbOn(boolean b) {
		setReverbOn(context, b);
	}

	public void setReverb(double roomsize, double damping, double width,
			double level) {
		setReverb(context, roomsize, damping, width, level);
	}

	public void setChorusOn(boolean b) {
		setChorusOn(context, b);
	}

	public void setChorus(int nr, double level, double speed, double depth_ms,
			int type) {
		setChorus(context, nr, level, speed, depth_ms, type);
	}

	public void setTuning(int tuningBank, int tuningProgram, String name,
			double[] derivations) {
		if (derivations == null || derivations.length != 12) {
			throw new IllegalArgumentException();
		}
		setTuning(context, tuningBank, tuningProgram, name, derivations);
	}

	public void send(int channel, int command, int data1, int data2) {
		switch (command) {
		case ShortMessage.NOTE_ON:
			noteOn(context, channel, data1, data2);
			break;
		case ShortMessage.NOTE_OFF:
			noteOff(context, channel, data1);
			break;
		case ShortMessage.PROGRAM_CHANGE:
			programChange(context, channel, data1);
			break;
		case ShortMessage.CONTROL_CHANGE:
			controlChange(context, channel, data1, data2);
			break;
		case ShortMessage.PITCH_BEND:
			pitchBend(context, channel, (data2 * 128) + data1);
			break;
		}
	}

	public void destroy() {
		destroy(context);
		context = null;
	}

	private static native ByteBuffer init(String name, int cores, int channels,
			int polyphony, float sampleRate, String audioDriver,
			String audioDevice, int buffers, int bufferSize, float overflowAge,
			float overflowPercussion, float overflowReleased,
			float overflowSustained, float overflowVolume) throws IOException;

	private static native void destroy(ByteBuffer context);

	private native void soundFontLoad(ByteBuffer context, String filename)
			throws IOException;
	
	private static native List<String> getProgramsList(ByteBuffer context);

	private static native void noteOn(ByteBuffer context, int channel, int key,
			int velocity);

	private static native void noteOff(ByteBuffer context, int channel, int key);

	private static native void controlChange(ByteBuffer context, int channel,
			int controller, int value);

	private static native void pitchBend(ByteBuffer context, int channel,
			int bend);

	private static native void programChange(ByteBuffer context, int channel,
			int program);

	private static native void setGain(ByteBuffer context, float gain);

	private static native void setInterpolate(ByteBuffer context, int number);

	private static native void setReverbOn(ByteBuffer context, boolean b);

	private static native void setReverb(ByteBuffer context, double roomsize,
			double damping, double width, double level);

	private static native void setChorusOn(ByteBuffer context, boolean b);

	private static native void setChorus(ByteBuffer context, int nr,
			double level, double speed, double depth_ms, int type);

	private static native void setTuning(ByteBuffer context, int tuningBank,
			int tuningProgram, String name, double[] derivations);

	/**
	 * Get the available {@link #getAudioDriver()}s.
	 * 
	 * @return possible options for audio drivers
	 */
	public native static List<String> getAudioDrivers();

	/**
	 * Get the available {@link #getAudioDevice()}s.
	 * 
	 * @param audioDriver
	 *            the audio driver to get possible devices for
	 * @return possible options for audio devices
	 */
	public native static List<String> getAudioDevices(String audioDriver);

	/**
	 * Load the native library "fluidsynth" from the local path 
	 * 
	 */

	public static void loadLibraries(String drv)
	{
		String LIBS_PATH = "libs";
		String WIN32_ARCH_PATH = "win32";
		String WIN64_ARCH_PATH = "win64";
		String LINUX_ARCH_PATH = "linux";
		File directory = null;
		File driverDirectory = null;
		String arch = System.getProperty("sun.arch.data.model");
		System.out.println("Running on " + arch + "bit system");

		if (NativeUtils.isWindows()) {
			if (arch.equals("64"))
			{
				directory = new File(LIBS_PATH + File.separator + WIN64_ARCH_PATH + File.separator);
				driverDirectory = new File(LIBS_PATH + File.separator + WIN64_ARCH_PATH + File.separator + drv + File.separator);
			}
			else
			{
				directory = new File(LIBS_PATH + File.separator + WIN32_ARCH_PATH + File.separator);
				driverDirectory = new File(LIBS_PATH + File.separator + WIN32_ARCH_PATH + File.separator + drv + File.separator);
			}
			
			try {
				NativeUtils.load(new File(directory, "libintl-8.dll"));
				NativeUtils.load(new File(directory, "libglib-2.0-0.dll"));
				NativeUtils.load(new File(directory, "libgthread-2.0-0.dll"));
				if (arch.equals("64"))
					NativeUtils.load(new File(driverDirectory, "portaudio_x64.dll"));
				else
					NativeUtils.load(new File(driverDirectory, "portaudio_x86.dll"));
				NativeUtils.load(new File(directory, "libfluidsynth.dll"));
			} catch (UnsatisfiedLinkError error) {
				System.out.println("Dependencies not provided" + error);
			}
		}

		if (NativeUtils.isMac()) {
			// libraries on mac include their install name, thus we cannot load
			// the dependecies explicitly. Instead we depend on tweaked loader
			// locations, see ./lib/mac/install_name_tool.sh
		}
		
		if (NativeUtils.isLinux()) {
			directory = new File(LIBS_PATH + File.separator + LINUX_ARCH_PATH + File.separator);
		}

		try {
			NativeUtils.load(directory, "fluidsynthJNI");
			//NativeUtils.load(new File(directory, "fluidsynthJNI.dll"));
		} catch (UnsatisfiedLinkError error) {
			System.out.println("Fluidsynth error: " + error);
			throw new NoClassDefFoundError();
		}
	}	

}
