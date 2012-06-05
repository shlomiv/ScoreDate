package org.jpab;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

/**
 * The HostAPI class represents a possible choice as to which underlying audio
 * API your streams will utilize.
 * 
 * 
 * @author Ryan Holdren
 */
public class HostAPI implements PortAudio.Component {

	/**
	 * The Type enumeration is used to designate the actual API used by a
	 * HostAPI. To my understanding, there will only ever be one HostAPI of each
	 * Type. This class does not extend Enum intentionally to facilitate the
	 * dynamic addition of new APIs should JPAB fail to be updated.
	 * 
	 * @author Ryan Holdren
	 */
	public static class Type {

		private static final HashMap <Integer, Type> map = new HashMap <Integer, Type> ();
		private static final HashSet <Type> set = new HashSet <Type> ();
		private static final Set <Type> view = Collections.unmodifiableSet(set);
		
		public static final Type 
			IN_DEVELOPEMENT = new Type("Under Development", 0),
			DIRECT_SOUND = new Type("DirectSound", 1),
			MME = new Type("MultiMedia Extensions", 2),
			ASIO = new Type("ASIO", 3),
			SOUND_MANAGER = new Type("Sound Manager", 4),
			CORE_AUDIO = new Type("Core Audio", 5),
			OSS = new Type("Open Sound System", 7),
			ALSA = new Type("Advanced Linux Sound Architecture", 8),
			AL = new Type("?", 9),
			BEOS = new Type("BeOS Media Kit", 10),
			WDMKS = new Type("WDM-KS", 11),
			JACK = new Type("JACK Audio Connection Kit", 12),
			WASAPI = new Type("Windows Audio Session API", 13),
			AUDIO_SCIENCE_HPI = new Type("Audio Science HPI", 14);

		public static Set <Type> values() {
			return view;
		}
		
		public static Type resolve(int code) {
			return map.get(code);
		}
		
		private final int code;
		private final String name;
		
		/**
		 * The constructor automatically adds the new Type to the set so that
		 * it can be used just as the constant Types are.
		 * 
		 * @param name The name of the API.
		 * @param code The code that will be passed to PortAudio.
		 */
		public Type(String name, int code) {
			if (map.containsValue(code)) throw new IllegalArgumentException();
			this.name = name;
			this.code = code;
			map.put(code, this);
			set.add(this);
		}

		public long getCode() {
			return code;
		}

		public String getName() {
			return name;
		}
		
		public String toString() {
			return name;
		}
	}

	private final int defaultInputDevice;
	private final int defaultOutputDevice;
	private final int deviceCount;
	private final int id;
	private final String name;
	private final Type type;

	protected HostAPI(ByteBuffer data) {
		defaultInputDevice = data.get();
		defaultOutputDevice = data.get();
		deviceCount = data.get();
		id = data.get();
		type = Type.resolve(data.get());
		final byte[] bytes = new byte[data.get()];
		data.get(bytes);
		this.name = new String(bytes);
	}

	public Device getDefaultInputDevice() throws PortAudioException {
		return PortAudio.getDevice(defaultInputDevice);
	}

	public Device getDefaultOutputDevice() throws PortAudioException {
		return PortAudio.getDevice(defaultOutputDevice);
	}
	
	public int getDeviceCount() {
		return deviceCount;
	}
	
	public List <Device> getDevices() throws PortAudioException {
		ArrayList <Device> devices = new ArrayList <Device> ();
		ByteBuffer data = PortAudio.getHostAPIsDevicesAsBuffer(id);
		data.order(ByteOrder.nativeOrder());
		try {
			while (data.remaining() > 0)
				devices.add(new Device(data));
		} finally {
			PortAudio.free(data);
		}
		return devices;
	}

	public String getName() {
		return name;
	}
	
	public Type getType() {
		return type;
	}

	public String toString() {
		return "Port Audio Host API {\n\tDefault Input Device ID: " + defaultInputDevice +
		"\n\tDefault Output Device ID: " + defaultOutputDevice +
		"\n\tDevice Count: " + deviceCount +
		"\n\tID: " + id +
		"\n\tName: " + name +
		"\n\tType: " + type + "\n}";
	}
	
	protected int getID() {
		return id;
	}
	
}
