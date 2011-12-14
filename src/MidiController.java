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
//import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
//import javax.sound.midi.Transmitter;

public class MidiController 
{

	Preferences appPrefs; 
	
	private MidiDevice inputDevice;
	private Synthesizer midiSynth;
	private Instrument[] instruments;
    private boolean midierror = false;
    private MidiChannel[] allMC;
    public MidiChannel midiChannel; // fixed channels are: 0 = user, 1 = playback, 2 = metronome
    
    private static final int ppq=1000;

    // variables to play metronome and an exercise. Fixed index are: 0 = playback, 1 = metronome
    private Track[] tracks = { null, null };
    private Sequence[] sequences = { null, null };
    private Sequencer[] sequencers = { null, null };
    
    int errorCode = 0;
	
	public MidiController(Preferences p)
	{
		appPrefs = p;
		initialize();
	}
	
	public int checkError()
	{
		return errorCode;
	}
	
	public boolean initialize() 
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
               instruments = sb.getInstruments();

               if (instruments != null) {
                   midiSynth.loadInstrument(instruments[0]);

               } else 
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

           midiChannel = allMC[0];
       }
       return true;
	}
	 
	public Instrument[] getInstruments()
	{
		 return instruments;
	}
	 
	public MidiDevice openDevice()
	{
		 int selectedDeviceIdx = Integer.parseInt(appPrefs.getProperty("mididevice"));

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

	                //System.out.println("[TEST] device #" + i + " bAllowsInput: " + bAllowsInput + ", name: " + aInfos[i].getName() + " (tmpIdx = " + tmpIdx + ")");
	                
	                if (bAllowsInput == true && tmpIdx == selectedDeviceIdx - 1) 
	                {
	                	System.out.println("[openDevice] Found selected device. Name: " + aInfos[i].getName());
	                	deviceName = aInfos[i].getName();
	                	break;
	                }
	            }
	            catch (MidiUnavailableException e) { e.printStackTrace(); }
	            tmpIdx++;
	         }
		     
		     if (deviceName == "")
		     	 return null;

		     System.out.println("Current device name = " + deviceName);

             try 
             {
            	 inputDevice = MidiSystem.getMidiDevice(aInfos[tmpIdx]);
	             inputDevice.open();
	         }
	         catch (MidiUnavailableException e) 
	         {
	             System.out.println("Unable to open MIDI device (" + deviceName + ")");
	             return null;
	         }
              
             setNewInstrument();
	     }
		 return inputDevice;
	 }
	 
	 public void setNewInstrument()
	 {
         int midiSound = Integer.parseInt(appPrefs.getProperty("instrument"));
 		 if (midiSound == -1) midiSound = 0;
 		   
 		 midiChannel.programChange(midiSound);
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

             //ShortMessage sm=new ShortMessage();
             //sm.setMessage(ShortMessage.PROGRAM_CHANGE, 1, 115, 0);
             //metronome.add(new MidiEvent(sm, 0));
         	
         	 //System.out.println("[createMetronome] timeSignNumerator = " + timeSignNumerator);

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
        		mess.setMessage(ShortMessage.NOTE_ON, 9, pitch, 90); // can use 37 as well, but it has reverb

        		metronomeTrack.add(new MidiEvent(mess, i*ppq));
        		mess2.setMessage(ShortMessage.NOTE_OFF, 9, pitch, 0);
        		metronomeTrack.add(new MidiEvent(mess2, (i*ppq)+1));

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
		 createSequencer(0);

		 int midiSound = Integer.parseInt(appPrefs.getProperty("instrument"));
		 if (midiSound == -1) midiSound = 0;

		 ShortMessage mess = new ShortMessage();
		 try {
			 mess.setMessage(ShortMessage.PROGRAM_CHANGE, 0, midiSound, 0);
		 }
		 catch (InvalidMidiDataException e) { }

		 tracks[0].add(new MidiEvent(mess, 0));

		 if (timeOffset > 0)
			 timeOffset /= timeDivision;

		 for (int i = 0; i < notes.size(); i++)
		 {
			 Note cNote = notes.get(i);
			 tick = (int)((cNote.timestamp + timeOffset) * ppq);
			 if (playOnly == true && cNote.type != 5) // do not play silence !
				 tracks[0].add(createNoteOnEvent(cNote.pitch, 90, tick));
			 String textb = "nOn";
			 addMidiEvent(tracks[0], metaType, textb.getBytes(), tick);
			 tick+=(int)((cNote.duration)*ppq);
			 
			 if (playOnly == true && cNote.type != 5) // do not play silence !
				 tracks[0].add(createNoteOffEvent(cNote.pitch, tick));
			 textb = "nOff";
  			 addMidiEvent(tracks[0], metaType, textb.getBytes(), tick);
		 }
		 String textend = "end";
		 addMidiEvent(tracks[0], metaType, textend.getBytes(), tick);

		 sequencers[0].setTempoInBPM(BPM/timeDivision);

		 return sequencers[0];
	 }

	 public void stopPlayback()
	 {
		 sequencers[0].stop();
		 sequencers[0].close();
	 }
}
