/***********************************************
This file is part of the ScoreDate project

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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/*
 *  Preferences map:
 *  
 *  language         | the UI global language
 *  
 *  clefsMask        | the four clefs masked with logical OR ( TREBLE_CLEF | BASS_CLEF | ALTO_CLEF | TENOR_CLEF);
 *  trebleClefLower  | pitch of the lowest note used in exercises
 *  trebleClefUpper  | pitch of the highest note used in exercises
 *  bassClefLower    | pitch of the lowest note used in exercises
 *  bassClefUpper    | pitch of the highest note used in exercises
 *  altoClefLower    | pitch of the lowest note used in exercises
 *  altoClefUpper    | pitch of the highest note used in exercises
 *  tenorClefLower   | pitch of the lowest note used in exercises
 *  tenorClefUpper   | pitch of the highest note used in exercises
 *  
 *  accidentals      | index of accidentals related to ClefNotesOptionDialog combo box list
 *  timeSignature    | index of time signature related to ClefNotesOptionDialog radio button group
 *  wholeNote        | 0: whole notes disabled, 1: whole notes enabled
 *  halfNote         | 0: half notes disabled, 1: half notes enabled
 *  quarterNote      | 0: quarter notes disabled, 1: quarter notes enabled
 *  eighthNote       | 0: eighth notes disabled, 1: eighth notes enabled
 *  tripletNote      | 0: triplets disabled, 1: triplets enabled
 *  silenceNote      | 0: silence disabled, 1: silence enabled
 *  3_4_Note		 | 0: 3/4 notes disabled, 1: 3/4 notes enabled
 *  3_8_Note		 | 0: 3/8 notes disabled, 1: 3/8 notes enabled
 *  
 *  metromome	     | metronome - 0: disabled, 1: enabled 
 *  showBeats		 | show metronome beats - 0: disabled, 1: enabled
 *  clickAccents     | metronome accents - 0: disabled, 1: enabled
 *  
 *  keyboardlenght   | number of piano keys. Can be 63 or 73
 *  mididevice       | index of the MIDI device to use. 0 is always "no device"
 *  sound            | playback sound enabled
 *  keyboardsound    | exercises sound enabled 
 *  instrument       | index of instrument taken from instrument list
 *  latency          | latency between MIDI in and synthesizer playback
 *  transposition    | number of octaves used to transpose sound
 *  
 *  audiodevice      | index of the audio input device to use for capture
 *  defaultInput     | default input to capture notes. 0: MIDI, 1: Microphone
 *  audiosensitivity | audio sensitivity threshold
 *  
 *  synthDriver      | synthesizer system to be used (Java or Fluidsynth)
 *  fluidDevice		 | audio device that Fluidsynth will use to output sounds     // TODO: not used yet
 *  soundfontPath    | path of the Soundfont to use when Fluidsynth is active
 *  
 *  inputDevice      | input device to use to acquire notes [MIDI | Audio],index
 *  outputDevice     | outputDevice to reproduce notes [Java | Fluidsynth],index
 */
public class Preferences 
{
	public int TREBLE_CLEF = 0x0001;
	public int   BASS_CLEF = 0x0002;
	public int   ALTO_CLEF = 0x0004;
	public int  TENOR_CLEF = 0x0008;
	
	public int GAME_STOPPED        = 0;
	public int INLINE_SINGLE_NOTES = 1;
	public int INLINE_MORE_NOTES   = 2;
	public int INLINE_LEARN_NOTES  = 3;
	public int SCORE_GAME_LISTEN   = 4;
	public int RHTYHM_GAME_USER    = 5;
	public int SCORE_GAME_USER     = 6;
	public int EAR_TRAINING		   = 7;
	
	public int NOTE_NORMAL		   = 0;
	public int NOTE_ACCIDENTALS	   = 1;
	public int NOTE_INTERVALS      = 2;
	public int NOTE_CHORDS   	   = 3;

	Properties prefs = new Properties();
	
	boolean globalExerciseMode = false;
	Exercise currentExercise;

	public Preferences()
	{
	  try
	  {
		prefs.load(new FileInputStream("scoredate.properties"));
 	    //  System.out.println("language = " + prefs.getProperty("language"));
		prefs.list(System.out);
  	  }
  	  catch (Exception e) 
  	  {
 	      System.out.println(e);
  	  }
	}

	public String getProperty(String prop)
	{
		if (prop == "language")
			return prefs.getProperty(prop, "");
		else
			return prefs.getProperty(prop, "-1");
	}

	public void setProperty(String prop, String value)
	{
		prefs.setProperty(prop, value);
	}

	public void storeProperties()
	{
		try 
		{ 
			prefs.store(new FileOutputStream("scoredate.properties"), null); 
			prefs.list(System.out);
        } 
        catch (IOException e) { }
	}
	
	public void setExerciseMode(boolean enable, Exercise e)
	{
		globalExerciseMode = enable;
		currentExercise = e;
	}
}
