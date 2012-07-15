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

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

public class MidiController 
{

	Preferences appPrefs; 
	
	private MidiDevice inputDevice = null;
	private Fluidsynth fluidSynth = null;
	private Synthesizer midiSynth;
	private Instrument[] jInstruments; // this is used only by the Java MIDI system
	private List<String> instrumentsList;
	private List<String> fluidDevicesList;
    private boolean midierror = false;
    private MidiChannel[] allMC; // fixed channels are: 0 = user, 1 = playback, 2 = metronome
    public MidiChannel midiOutChannel = null;
    
    private static final int ppq=1000;

    // variables to play metronome and an exercise. Fixed index are: 0 = playback, 1 = metronome
    private Track[] tracks = { null, null };
    private Sequence[] sequences = { null, null };
    private Sequencer[] sequencers = { null, null };
    
    int errorCode = 0;
    private boolean useFluidsynth = false;
    int lastNote = -1;
	
	public MidiController(Preferences p)
	{
		errorCode = 0;
		appPrefs = p;
		instrumentsList = new ArrayList<String>();
		String outDevice = appPrefs.getProperty("outputDevice");
		System.out.println("----------> Selected driver = " + outDevice);
		if (outDevice == "-1" || outDevice.equals("Java"))
			initJavaSynth();
		else if (outDevice.split(",")[0].equals("Fluidsynth"))
			initFluidsynth(Integer.parseInt(outDevice.split(",")[1]));
	}
	
	public void close()
	{
		if (useFluidsynth == false)
		{
			if (inputDevice != null && inputDevice.isOpen())
			   inputDevice.close();
		}
		else
		{
			if (fluidSynth != null)
				fluidSynth.destroy();
			fluidSynth = null;
/*
			try{
			  Thread.sleep(1000);//sleep for 1000 ms
			}
			catch(InterruptedException ie){	}
*/
		}
	}
	
	public int checkError()
	{
		return errorCode;
	}
	
	public boolean initJavaSynth() 
	{
	   midierror = false;

	   if (inputDevice != null && inputDevice.isOpen())
		   inputDevice.close();

       try 
       {
           if (midiSynth == null) 
           {
               if ((midiSynth = MidiSystem.getSynthesizer()) == null) 
               {
                   System.out.println("getSynthesizer() failed!");
                   errorCode = 1;
                   return false;
               }
           }
           midiSynth.open();
       }
       catch (MidiUnavailableException e) 
       {
           System.out.println("Midi System not available: MIDI output busy - please stop all the other applications using the MIDI system. "+e);
           midierror = true;
       }

       if (!midierror) 
       {
           Soundbank sb = midiSynth.getDefaultSoundbank();
           if (sb != null) 
           {
               jInstruments = sb.getInstruments();

               if (jInstruments != null) {
                   midiSynth.loadInstrument(jInstruments[0]);

               } 
               else
               {
                   midierror = true;
                   System.out.println("No instruments found !!");
               }
           }
           else
           {
        	   System.out.println("NO SOUNDBANK FOUND !! Download one...");
        	   errorCode = 2;
           }

           allMC = midiSynth.getChannels();

           midiOutChannel = allMC[0];
           useFluidsynth = false;
       }
       return true;
	}
	 
	public List<String> getInstruments()
	{
		System.out.println("Number of instruments: " +  instrumentsList.size());
		if (useFluidsynth == false)
		{
			instrumentsList.clear();
			if (jInstruments != null && jInstruments.length > 0)
				for (int i = 0; i < 20; i++)
					instrumentsList.add(jInstruments[i].getName());
		}
		return instrumentsList;
	}
	 
	public MidiDevice openInputDevice()
	{
		String inDev = appPrefs.getProperty("inputDevice");
		if(inDev.split(",")[0].equals("MIDI") == false)
			return null;
		int selectedDeviceIdx = Integer.parseInt(inDev.split(",")[1]);

		if (inputDevice != null && inputDevice.isOpen())
			inputDevice.close();
		 
		//System.out.println("[TEST] selectedDeviceIdx: " + selectedDeviceIdx);

		if (selectedDeviceIdx > 0) // 0 means there are no available MIDI devices 
		{
			MidiDevice.Info[] aInfos = MidiSystem.getMidiDeviceInfo();
			MidiDevice tmpDevice = null;
			String deviceName = "";
			int tmpIdx = 0;
		    for (int i = 0; i < aInfos.length; i++) 
		    {
	            try 
	            {
	                tmpDevice = MidiSystem.getMidiDevice(aInfos[i]);
	                boolean bAllowsInput = (tmpDevice.getMaxTransmitters() != 0);
	                //boolean bAllowsOutput = (tmpDevice.getMaxReceivers() != 0);

	                if (bAllowsInput == false) // non-input devices are excluded from the list !!
	                	continue;

	                System.out.println("[TEST] device #" + i + " bAllowsInput: " + bAllowsInput + ", name: " + aInfos[i].getName() + " (tmpIdx = " + tmpIdx + ")");

	                if (bAllowsInput == true && tmpIdx == selectedDeviceIdx - 1) 
	                {
	                	System.out.println("[openDevice] Found selected device. Name: " + aInfos[i].getName());
	                	deviceName = aInfos[i].getName();
	                	try 
	                    {
	                		inputDevice = MidiSystem.getMidiDevice(aInfos[i]);
		       	            inputDevice.open();
		       	        }
		       	        catch (MidiUnavailableException e) 
		       	        {
		       	            System.out.println("Unable to open MIDI device (" + deviceName + ")");
		       	            return null;
	       	            }
		                    
	                    setNewInstrument();
	                    return inputDevice;
	                }
	            }
	            catch (MidiUnavailableException e) { e.printStackTrace(); }
	            tmpIdx++;
	         }
	     }
		 return null;
	 }
	
	 public void setNewInstrument()
	 {
		 if (useFluidsynth == false)
        	 setJavaInstrument();
         else
        	 setFluidsynthInstrument();
	 }
	 
	 public void setJavaInstrument()
	 {
         int midiSound = Integer.parseInt(appPrefs.getProperty("instrument"));
 		 if (midiSound == -1) midiSound = 0;

 		 if (midiOutChannel != null)
 			 midiOutChannel.programChange(midiSound);
	 }
	 
	 public boolean initFluidsynth(int devIndex)
	 {
		 midierror = false;
		 fluidDevicesList = new ArrayList<String>();
		 int matchIdx = 0;

		 if (fluidSynth != null)
		 {
			fluidSynth.destroy();
		    fluidSynth = null;
		 }

		 Fluidsynth.loadLibraries();
		 if (NativeUtils.isWindows())
		 {
			try {
				fluidSynth = new Fluidsynth("fluidDriver", 1, 16, 256, 44100.0f, "portaudio", "", devIndex, 8, 1024, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f);
			} catch (IOException expected) {
				System.out.println("Cannot open Fluidsynth audio output driver!!");
				errorCode = 1;
				return false;
			}
		 }
		 else // linux case
		 { 
		   List<String> drivers = Fluidsynth.getAudioDrivers();
		   if (devIndex == -1) devIndex = 0;
		   for (String driver : drivers)
		   {
			System.out.println(driver);
			if (driver.equals("file"))
				continue;

			fluidDevicesList.add(driver);
			if (matchIdx == devIndex)
			{
				System.out.println("Fluidsynth is going to output on: " + driver);
				try {
					fluidSynth = new Fluidsynth("fluidDriver", 1, 16, 256, 44100.0f, driver, null, devIndex, 8, 1024, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f);
				} catch (IOException expected) {
					System.out.println("Cannot open Fluidsynth audio output driver!!");
					errorCode = 1;
					return false;
				}
			}
			matchIdx++;
		   }
		 }
		String bankPath = appPrefs.getProperty("soundfontPath");
		try {
			fluidSynth.soundFontLoad(new File("metronome.sf2"));
			if (bankPath != "-1")
				fluidSynth.soundFontLoad(new File(bankPath));
		} catch (IOException expected) {
			System.out.println("Cannot load Fluidsynth soundfont !!");
			fluidSynth.destroy();
			errorCode = 2;
			return false;
		}
		useFluidsynth = true;
		instrumentsList.clear();
		if (bankPath != "-1")
		{
			List<String> programs = fluidSynth.getSoundfontPrograms();
			//int i = 0;
			for (String program : programs)
			{
				//System.out.println("Program #" + i + ": " + program);
				instrumentsList.add(program);
				//i++;
			}
			setNewInstrument();
		}
		 return true;
	 }
	 
	 public void setFluidsynthInstrument()
	 {
		 int midiSound = Integer.parseInt(appPrefs.getProperty("instrument"));
 		 if (midiSound == -1) midiSound = 0;

		 fluidSynth.send(0, ShortMessage.PROGRAM_CHANGE, midiSound, 0);
	 }
	 
	 public List<String> getFluidDevices()
	 {
		 return fluidDevicesList;
	 }

	 public void playNote(int pitch, int volume)
	 {
		 if (useFluidsynth == false)
			 midiOutChannel.noteOn(pitch, volume);
		 else
		 {
			 //fluidSynth.send(0, ShortMessage.CONTROL_CHANGE , 0, 0);
			 fluidSynth.send(0, ShortMessage.NOTE_ON, pitch, volume);
		 }
	 }
	 
	 public void stopNote(int pitch, int volume)
	 {
		 if (useFluidsynth == false)
			 midiOutChannel.noteOff(pitch, volume);
		 else
			 fluidSynth.send(0, ShortMessage.NOTE_OFF, pitch, volume);
	 }
	 
	 private void fluidsynthAsyncMIDIevent(MetaMessage msg)
	 {
		byte[] metaData = msg.getData();
        String strData = new String(metaData);
       
        //System.out.println("*FS* META message: text= " + strData);

        if ("fsbOnLow".equals(strData))
        	fluidSynth.send(9, ShortMessage.NOTE_ON, 77, 100);
        else if ("fsbOnHi".equals(strData))
        	fluidSynth.send(9, ShortMessage.NOTE_ON, 76, 100);
        else if ("fsbOff".equals(strData))
        	fluidSynth.send(9, ShortMessage.NOTE_OFF, 77, 0);
        else if ("fsnOn".equals(strData.substring(0, 5)))
        {
        	lastNote = Integer.parseInt(strData.substring(5));
        	fluidSynth.send(0, ShortMessage.NOTE_ON, lastNote, 100);
        }
        else if ("fsnOff".equals(strData.substring(0, 6)))
        {
        	fluidSynth.send(0, ShortMessage.NOTE_OFF, Integer.parseInt(strData.substring(6)), 0);
        	lastNote = -1;
        }
	 }
	 
	 private void addMidiEvent(Track track, int type, byte[] data, long tick) 
	 {
        MetaMessage message=new MetaMessage();
        try {
            message.setMessage(type, data, data.length);
            MidiEvent event=new MidiEvent(message, tick);
            track.add(event);
        }
        catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
	 }

     private static MidiEvent createNoteOnEvent(int nKey, int velocity, long lTick) 
     {
    	 return createNoteEvent(ShortMessage.NOTE_ON, nKey, velocity, lTick);
	 }

	 private static MidiEvent createNoteOffEvent(int nKey, long lTick) 
	 {
		 return createNoteEvent(ShortMessage.NOTE_OFF, nKey, 0, lTick);
	 }

	 private static MidiEvent createNoteEvent(int nCommand, int nKey, int nVelocity, long lTick) 
	 {
		 ShortMessage message=new ShortMessage();
	     try {
	            message.setMessage(nCommand,
	                0, // always on channel 1
	                nKey,
	                nVelocity);
	     }
	     catch (InvalidMidiDataException e) 
	     {
	            e.printStackTrace();
	            System.exit(1);
	     }
	     return new MidiEvent(message, lTick);
	 }
	 
	 private void createSequencer(int index)
	 {
		 if (sequencers[index] != null)
			 sequencers[index].close();

         try 
         {
        	 sequences[index] = new Sequence(Sequence.PPQ, ppq);
         } 
         catch (InvalidMidiDataException e) {
            e.printStackTrace();
            System.exit(1);
         }

		 tracks[index] = sequences[index].createTrack();
		 
         try 
         {
        	 sequencers[index] = MidiSystem.getSequencer();
         }
         catch (MidiUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
         }

         try {
        	 sequencers[index].open();
         }
         catch (MidiUnavailableException e) {
             e.printStackTrace();
             System.exit(1);
         }

         try {
        	 sequencers[index].setSequence(sequences[index]);
         }
         catch (InvalidMidiDataException e) {
             e.printStackTrace();
             System.exit(1);
         }

         if (!(sequencers[index] instanceof Synthesizer)) 
         {
             try 
             {
                 Synthesizer synthesizer = MidiSystem.getSynthesizer();
                 synthesizer.open();
                 Receiver synthReceiver = synthesizer.getReceiver();
                 Transmitter seqTransmitter = sequencers[index].getTransmitter();
                 seqTransmitter.setReceiver(synthReceiver);
                 //long latency = sm_synthesizer.getLatency()/1000;  
                 //System.out.println("MIDI latency " + latency);
             }
             catch (MidiUnavailableException e) {
                 e.printStackTrace();
             }
         }
	 }

	 public Sequencer createMetronome(Preferences p, int BPM, int measures, int timeSignNumerator, int timeDivision)
	 {
		 // create metronome sequence, sequencer and track
         createSequencer(1);
         
         Track metronomeTrack = tracks[1];
         boolean accents = false;
         if (p.getProperty("clickAccents").equals("1"))
        	 accents = true;

         try {
             final int metaType = 0x01;
             int beatsNumber;

         	 //System.out.println("[createMetronome] timeSignNumerator = " + timeSignNumerator);
             if (useFluidsynth == true)
             	 fluidSynth.send(9, ShortMessage.PROGRAM_CHANGE, 0, 0);

             String textd="gameOn"; // first note beat
             addMidiEvent(metronomeTrack, metaType, textd.getBytes(), 0);
             
             String textdt="cursorOn"; //one beat before first note
             addMidiEvent(metronomeTrack, metaType, textdt.getBytes(), (int)((timeSignNumerator/timeDivision)-1)*ppq);
             
             
             if (Integer.parseInt(p.getProperty("metronome")) == 1)
            	 beatsNumber = (timeSignNumerator * measures) + timeSignNumerator;
             else 
            	 beatsNumber = timeSignNumerator; //only few first to indicate pulse

             for (int i = 0; i < beatsNumber; i++) 
             {
           		ShortMessage mess = new ShortMessage();
        		ShortMessage mess2 = new ShortMessage();
      		    int pitch = 77;
      		    if (accents == true && i%(timeSignNumerator/timeDivision) == 0)
      		    	pitch = 76;

        		if (useFluidsynth == false)
        		{
        			mess.setMessage(ShortMessage.NOTE_ON, 9, pitch, 90);
        			metronomeTrack.add(new MidiEvent(mess, i*ppq));
        			mess2.setMessage(ShortMessage.NOTE_OFF, 9, pitch, 0);
            		metronomeTrack.add(new MidiEvent(mess2, (i*ppq)+1));
        		}
        		else
        		{
        			String textb="fsbOnLow";
        			if (pitch == 76)
        				textb="fsbOnHi";
     				addMidiEvent(metronomeTrack, metaType, textb.getBytes(), (int)i*ppq);
        			textb="fsbOff";
     				addMidiEvent(metronomeTrack, metaType, textb.getBytes(), (int)(i*ppq)+1);     				
        		}

        		if (i > ((timeSignNumerator / timeDivision) - 1)) 
        		{
         			//System.out.println("adding metronome beat : "+i);
         			String textb="beat";
         			addMidiEvent(metronomeTrack, metaType, textb.getBytes(), (int)i*ppq);
        		}
             }
         }
         catch (InvalidMidiDataException e) {
             e.printStackTrace();
             System.exit(1);
         }

         sequencers[1].setTempoInBPM(BPM );
         
         if (useFluidsynth == true)
         {
           sequencers[1].addMetaEventListener(new MetaEventListener() {
             public void meta(MetaMessage meta) 
             {
             	fluidsynthAsyncMIDIevent(meta);
             }
 		   });
         }
         
         return sequencers[1];
	 }
	 
	 public void stopMetronome()
	 {
		 sequencers[1].stop();
		 sequencers[1].close();
	 }
	 
	 public Sequencer createPlayback(Preferences p, int BPM, Vector<Note> notes, int timeDivision, boolean playOnly, int timeOffset)
	 {
		 final int metaType = 0x01;
		 int tick = 0;
		 int endtick = 0;
		 createSequencer(0);

		 int midiSound = Integer.parseInt(appPrefs.getProperty("instrument"));
		 if (midiSound == -1) midiSound = 0;

		 if (useFluidsynth == false)
		 {
			 ShortMessage mess = new ShortMessage();
			 try {
				 mess.setMessage(ShortMessage.PROGRAM_CHANGE, 0, midiSound, 0);
			 }
			 catch (InvalidMidiDataException e) { }
			 tracks[0].add(new MidiEvent(mess, 0));
		 }
		 else
			 fluidSynth.send(0, ShortMessage.PROGRAM_CHANGE, midiSound, 0);

		 if (timeOffset > 0)
			 timeOffset /= timeDivision;

		 for (int i = 0; i < notes.size(); i++)
		 {
			 Note cNote = notes.get(i);
			 tick = (int)((cNote.timestamp + timeOffset) * ppq);
			 
			 if (playOnly == true && cNote.type != 5) // do not play silence !
			 {
				 if (useFluidsynth == false)
					 tracks[0].add(createNoteOnEvent(cNote.pitch, 90, tick));
				 else
				 {
					 String textb = "fsnOn" + cNote.pitch;
					 addMidiEvent(tracks[0], metaType, textb.getBytes(), tick);
				 }
			 }

			 String textb = "nOn";
			 if (cNote.secondRow == true)
				 textb = "n2On";
			 addMidiEvent(tracks[0], metaType, textb.getBytes(), tick);
			 tick+=(int)((cNote.duration)*ppq);
			 
			 if (playOnly == true && cNote.type != 5) // do not play silence !
			 {
				 if (useFluidsynth == false)
					 tracks[0].add(createNoteOffEvent(cNote.pitch, tick));
				 else
				 {
					 textb = "fsnOff" + cNote.pitch;
		  			 addMidiEvent(tracks[0], metaType, textb.getBytes(), tick);
				 }
			 }

			 textb = "nOff";
			 if (cNote.secondRow == true)
				 textb = "n2Off";
  			 addMidiEvent(tracks[0], metaType, textb.getBytes(), tick);
  			 if (tick > endtick)
  				endtick = tick;
		 }
		 String textend = "end";
		 addMidiEvent(tracks[0], metaType, textend.getBytes(), endtick);

		 sequencers[0].setTempoInBPM(BPM/timeDivision);

		 if (useFluidsynth == true)
         {
           sequencers[0].addMetaEventListener(new MetaEventListener() {
             public void meta(MetaMessage meta) 
             {
            	 fluidsynthAsyncMIDIevent(meta);
             }
 		   });
         }

		 return sequencers[0];
	 }

	 public void stopPlayback()
	 {
		 sequencers[0].stop();
		 sequencers[0].close();
		 // stop last note in case there's one suspended...
		 if (lastNote != -1 && useFluidsynth == true)
			 fluidSynth.send(0, ShortMessage.NOTE_OFF, lastNote, 0);
	 }
}
