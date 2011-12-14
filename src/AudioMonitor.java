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

import java.util.ResourceBundle;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

public class AudioMonitor extends JPanel
{
	ResourceBundle appBundle;

	double[] inputFFT;
	int volume = 0;
	int[] spectrumBars = null;
	String currentNote = "";
	Piano piano;
	
	private static final long serialVersionUID = -3780914781525599762L;

	public AudioMonitor(ResourceBundle b)
	{
		appBundle = b;

		spectrumBars = new int[16];	
		piano = new Piano(73);
		this.setFont(new Font("Arial", Font.BOLD, 50));
	}

	public void showVolume(int vol)
	{
		volume = vol;
	}

	public void showSpectrum(double[] spec)
	{
		long[][] spectrumSums = new long[2][16];
		inputFFT = spec;
		for (int i = 0; i < 16; i++)
			spectrumBars[i] = 0;
		for (int i = 0; i < inputFFT.length; i++)
		{
	    	double value = Math.log10(inputFFT[i]);
	    	double frequency = (8000 * i) / inputFFT.length;
	    	if (value <= 1)
	    		continue;
	    	if (frequency > 2000)
	    		break;
	    	int specIdx = (int)(frequency / 125) - 1;
	    	if (specIdx < 0) specIdx = 0;
	    	spectrumSums[0][specIdx] += (long)value;
	    	spectrumSums[1][specIdx]++;
	    	spectrumBars[specIdx] = (int)(spectrumSums[0][specIdx] / spectrumSums[1][specIdx]) * 30;
		}
		repaint();
	}

	private String getLabelFromIndex(int idx)
	{
		String noteInfo = "";
		switch (idx)
		{
			case 0: noteInfo = appBundle.getString("_do"); break;
			case 1: noteInfo = appBundle.getString("_re"); break;
			case 2: noteInfo = appBundle.getString("_mi"); break;
			case 3: noteInfo = appBundle.getString("_fa"); break;
			case 4: noteInfo = appBundle.getString("_sol"); break;
			case 5: noteInfo = appBundle.getString("_la"); break;
			case 6: noteInfo = appBundle.getString("_si"); break;
		}

		return noteInfo;
	}

	public void showPitch(int p)
	{
		//System.out.println("[audioMon] got pitch " + p);
		if (p == 23)
		{
			currentNote = "";
			return;
		}
		int noteIdx = piano.getNoteIndexFromPitch(p);
		int octave = piano.getOctaveFromPitch(p);
		int alt = 0;
		
		if (noteIdx >= 100)
		{
			alt = 1;
			noteIdx-=100;
		}
		currentNote = getLabelFromIndex(noteIdx);
		currentNote += Integer.toString(octave);
		if (alt == 1)
			currentNote += "#";
	}

	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		int volHeight = volume * (getHeight() / 127);
		g.setColor(Color.blue);
		g.fillRect(0, getHeight() - volHeight, 10, volHeight);
		
		g.setColor(Color.gray);
		if (spectrumBars != null)
		{
			int xPos = 20;
			for (int i = 0; i < spectrumBars.length; i++)
			{
				g.drawRect(xPos, getHeight() - spectrumBars[i], 15, spectrumBars[i] - 1);
				xPos += 15;
			}
		}
		g.setColor(Color.black);
		g.drawString(currentNote, 280, 70);
	}
}

