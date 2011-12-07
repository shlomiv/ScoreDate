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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class AudioInputController 
{
	Preferences appPrefs;
	
	double freqFactor = 0;
	
	public AudioInputController(Preferences p)
	{
		appPrefs = p;
		initialize();
	}
	
	public boolean initialize() 
	{
		float sampleRate = 8000;
	    int sampleSizeInBits = 8;
	    int channels = 1;
	    boolean signed = true;
	    boolean bigEndian = true;
	    AudioFormat format =  new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	    
	    freqFactor = Math.pow(2, 1.0/12.0); // calculate the factor between frequencies
	    
	    Mixer.Info[] mixers = AudioSystem.getMixerInfo();
	    for (int i = 0; i < mixers.length; i++)
	    {
	    	System.out.println("Mixer #" + i + ": " + mixers[i].getName());
	    }
	    
		return true;
	}
}
