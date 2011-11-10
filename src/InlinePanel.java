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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

public class InlinePanel extends JPanel implements ActionListener 
{
	private static final long serialVersionUID = 1L;
	private Font appFont;
	private ResourceBundle appBundle;
	private Preferences appPrefs;
	private MidiController appMidi;
	private Accidentals inlineAccidentals;
	private NoteGenerator inlineNG;
	private Statistics stats;
	
	private InlineGameThread gameThread = null;
	private boolean gameStarted = false; 
	
	// GUI elements
	public SmartBar sBar;
	private int sBarHeight = 130;
	private JLayeredPane layers;
	private Staff inlineStaff;
	private NotesPanel notesLayer;
	private Piano piano;
	private GameBar gameBar;
	private int gBarHeight = 40;
	private int pianoHeight = 80;
	private int staffHMargin = 180;
	private int staffVMargin = 150;
	private int staffHeight = 260;
	private int rowsDistance = 90; // distance in pixel between staff rows
	
	Vector<Note> gameNotes = new Vector<Note>();
	Vector<Integer> userNotes = new Vector<Integer>();
	private int clefMask = 1;
	private int gameType = -1;
	private int gameSubType = -1;
	private int gameInterval = -1;
	private int progressStep = 1;
	private int currentSpeed = 120;
	private int noteXStartPos = 0; // X position of the notes appearing on the staff
	
	public InlinePanel(Font f, ResourceBundle b, Preferences p, MidiController mc, Dimension d)
	{
		appFont = f;
		appBundle = b;
		appPrefs = p;
		appMidi = mc;
		
		setBackground(Color.white);
		setSize(d);
		setLayout(null);
		inlineAccidentals = new Accidentals("", 0, appPrefs);
		inlineNG = new NoteGenerator(appPrefs, inlineAccidentals, false);
		stats = new Statistics();

		gameType = appPrefs.GAME_STOPPED;
		
		sBar = new SmartBar(new Dimension(d.width, sBarHeight), b, f, p, true);
		sBar.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getPropertyName() == "updateParameters")
				{
					System.out.println("INLINE panel update parameters !");
					refreshPanel();
				}
			}
		});
		
		sBar.playBtn.addActionListener(this);
		
		int panelsWidth = d.width - (staffHMargin * 2);
		
		layers = new JLayeredPane();
		layers.setPreferredSize( new Dimension(panelsWidth, staffHeight));
		layers.setBounds(staffHMargin, staffVMargin, panelsWidth, staffHeight);
		
		inlineStaff = new Staff(appFont, appBundle, appPrefs, inlineAccidentals, true, true);
		inlineStaff.setPreferredSize( new Dimension(panelsWidth, staffHeight));
		inlineStaff.setBounds(0, 0, panelsWidth, staffHeight);
		inlineStaff.setClef(clefMask);
		inlineStaff.setOpaque(true);
		
		notesLayer = new NotesPanel(appFont, appPrefs, gameNotes, true);
		notesLayer.setPreferredSize( new Dimension(panelsWidth, staffHeight));
		notesLayer.setBounds(0, 0, panelsWidth, staffHeight);
		notesLayer.setOpaque(false);
		
		layers.add(inlineStaff, new Integer(1));
		layers.add(notesLayer, new Integer(2));

		int pianoKeysNum = Integer.parseInt(appPrefs.getProperty("keyboardlength"));
		if (pianoKeysNum == -1) pianoKeysNum = 73;
		piano = new Piano(pianoKeysNum);
		piano.setPreferredSize( new Dimension(d.width, pianoHeight));
		piano.setBounds(0, staffVMargin + 240, d.width, pianoHeight);
		
		for (int i = 0; i < pianoKeysNum; i++)
		{
			piano.keys.get(i).addMouseListener(new MouseAdapter() {

	            public void mousePressed(MouseEvent e) 
	            {
	            	pianoKeyPressed((Key)e.getSource(), true);
	            }
	            
	            public void mouseReleased(MouseEvent e) 
	            {
	            	pianoKeyPressed((Key)e.getSource(), false);
	            }
			});
		}
		
		gameBar = new GameBar(new Dimension(d.width, gBarHeight), b, f, p, true);
		gameBar.setBounds(0, getHeight() - gBarHeight, getWidth(), gBarHeight);
		gameBar.progress.setValue(20);
		
		gameType = appPrefs.GAME_STOPPED;

		add(sBar);
		add(layers);
		add(piano);
		add(gameBar);
		refreshPanel();
	}
	
	public void refreshPanel()
	{
		piano.reset(true);
		inlineNG.update();

		int lowerPitch = inlineNG.getFirstLowPitch();
		int higherPitch = inlineNG.getFirstHighPitch();
		piano.setNewBound(lowerPitch, higherPitch);
		lowerPitch = inlineNG.getSecondLowPitch();
		higherPitch = inlineNG.getSecondHighPitch();
		piano.setNewBound(lowerPitch, higherPitch);

		rowsDistance = inlineNG.getRowsDistance();
		inlineStaff.setRowsDistance(rowsDistance);
		notesLayer.setRowsDistance(rowsDistance);
		inlineStaff.setClef(inlineNG.getClefMask());
		notesLayer.setClef(inlineNG.getClefMask());
		notesLayer.setFirstNoteXPosition(inlineStaff.getFirstNoteXPosition());
		setLearningInfo(false, -1);
	}
	
	public void updateLanguage(ResourceBundle bundle)
	{
		System.out.println("INLINE - update language");
		appBundle = bundle;
		sBar.updateLanguage(appBundle);
		gameBar.updateLanguage(appBundle);
	}
	
	public void setGameType()
	{
		gameInterval = -1;
		int gameIdx = sBar.gameSelector.getSelectedIndex();
		switch(gameIdx)
		{
			case 0:	gameType = appPrefs.INLINE_SINGLE_NOTES; break;
			case 1:	gameType = appPrefs.INLINE_MORE_NOTES; break;
			case 2:	gameType = appPrefs.INLINE_LEARN_NOTES;	break;			
		}
		
		int subGameIdx = sBar.gameType.getSelectedIndex();
		switch (subGameIdx)
		{
			case 0: 
				gameSubType = appPrefs.NOTE_NORMAL; 
			break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				gameSubType = appPrefs.NOTE_INTERVALS;
				gameInterval = subGameIdx + 1;
			break;
			case 10: 
				gameSubType = appPrefs.NOTE_CHORDS; 
			break;
		}
		int notesNum = gameBar.notesNumber.getSelectedIndex();
		switch(notesNum)
		{
			case 0: progressStep = 8; break; // 10 notes
			case 1: progressStep = 4; break; // 20 notes
			case 2: progressStep = 2; break; // 40 notes
			case 3: progressStep = 1; break; // 80 notes
		}
	
	}
	
	public void setLearningInfo(boolean enable, int chordType)
	{
		if (gameNotes.size() == 0)
			return;
		int noteIdx = piano.highlightKey(gameNotes.get(0).pitch, enable);
		String noteInfo = "";
		String altInfo = "";
		String chord = "";
		
		if (gameNotes.size() > 1)
			for (int i = 1; i < gameNotes.size(); i++)
				piano.highlightKey(gameNotes.get(i).pitch, enable);

		if (enable == true)
		{
			if (noteIdx != 2 && noteIdx != 6) // E and B are already OK
			{
				if (inlineNG.isAlterated(noteIdx) == true)
					altInfo = inlineAccidentals.getType();
			}
			if (gameSubType != appPrefs.NOTE_INTERVALS)
			{
			  switch (noteIdx)
			  {
				case 0: noteInfo = appBundle.getString("_do"); break;
				case 1: noteInfo = appBundle.getString("_re"); break;
				case 2: noteInfo = appBundle.getString("_mi"); break;
				case 3: noteInfo = appBundle.getString("_fa"); break;
				case 4: noteInfo = appBundle.getString("_sol"); break;
				case 5: noteInfo = appBundle.getString("_la"); break;
				case 6: noteInfo = appBundle.getString("_si"); break;
			  }
			  if (gameSubType == appPrefs.NOTE_CHORDS && chordType != -1)
			  {
				chord = " ";
				switch (chordType)
				{
					case 0: chord += appBundle.getString("_major"); break;
					case 1: chord += appBundle.getString("_minor"); break;
					case 2: chord += appBundle.getString("_diminished"); break;
					case 3: chord += appBundle.getString("_augmented"); break;
				}
			  }
			}

			if (gameSubType == appPrefs.NOTE_INTERVALS)
			{
				chord = " ";
				int intervalType = sBar.gameType.getSelectedIndex() + 1;
				String keyStr = "";
				switch (intervalType)
				{
					case 2: keyStr = "_second"; break;
					case 3: keyStr = "_third"; break;
					case 4: keyStr = "_fourth"; break;
					case 5: keyStr = "_fifth"; break;
					case 6: keyStr = "_sixth"; break;
					case 7: keyStr = "_seventh"; break;
					case 8: keyStr = "_octave"; break;
				}
				switch (chordType)
				{
					case -2: keyStr += "dim"; break; 
					case -1: keyStr += "min"; break; 
					case 0:
						if (intervalType == 4 || intervalType == 5 || intervalType == 8)
							keyStr += "per";
						else
							keyStr += "maj";
					break;
					case 1: keyStr += "aug"; break; 
				}
				chord += appBundle.getString(keyStr);
			}
		}
		
		notesLayer.setLearningTips(noteInfo + altInfo + chord, enable);
	}
	
	public void stopGame()
	{
		if (gameType == appPrefs.GAME_STOPPED)
			return;

		if (gameThread != null && gameThread.isAlive() == true)
		{
			/* ************** STOP CURRENT GAME ***************** */
			gameStarted = false;
			sBar.playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
			for (int i = 0; i < gameNotes.size(); i++)
				appMidi.midiChannel.noteOff(gameNotes.get(i).pitch, 0);
			gameNotes.clear();
			gameType = appPrefs.GAME_STOPPED;
		}
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == sBar.playBtn)
		{
			if (gameThread != null && gameThread.isAlive() == true)
			{
				/* ************** STOP CURRENT GAME ***************** */
				stopGame();
				refreshPanel();
			}
			else
			{
				/* ************** START NEW GAME ***************** */
				currentSpeed = sBar.tempoSlider.getValue();
				piano.reset(false);
				gameNotes.clear();
				gameBar.precisionCnt.setText("");
				gameBar.scoreCnt.setText("");
				gameBar.progress.setValue(20);
				setGameType();
				noteXStartPos = inlineStaff.getFirstNoteXPosition();
				notesLayer.setFirstNoteXPosition(noteXStartPos);
				notesLayer.setStaffWidth(inlineStaff.getStaffWidth());
				stats.reset();
				stats.setGameSpeed(currentSpeed);
				gameThread = new InlineGameThread();
				gameStarted = true;
				gameThread.start();
				sBar.playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/stop.png")).getImage());
			}
		}
	}
	
	private void pianoKeyPressed(Key k, boolean pressed)
	{
		System.out.println("[pianoKeyPressed] pitch = " + k.pitch);
		if (pressed)
		{
			if (gameType == appPrefs.GAME_STOPPED)
				appMidi.midiChannel.noteOn(k.pitch, 90);
			noteEvent(k.pitch, 90);
		}
		else
		{
			appMidi.midiChannel.noteOff(k.pitch, 0);
			noteEvent(k.pitch, 0);
		}
	}
	
	public void noteEvent(int pitch, int velocity)
	{
		if (velocity == 0)
		{
			appMidi.midiChannel.noteOff(pitch, 0);
			if (userNotes.size() != 0)
				userNotes.removeElementAt(userNotes.indexOf(pitch));
		}
		else
		{
			if (gameType != appPrefs.GAME_STOPPED)
			{
			  userNotes.add(pitch);
			  boolean match = checkGameStatus(gameNotes, userNotes);
			  if (match == true)
			  {
				updateGameStats(1);
				if (gameType != appPrefs.INLINE_MORE_NOTES)
				{
				  for (int i = 0; i < gameNotes.size(); i++)
				  {
					appMidi.midiChannel.noteOff(gameNotes.get(i).pitch, 0);
					if (gameType == appPrefs.INLINE_LEARN_NOTES)
						setLearningInfo(false, -1);
				  }
				  gameNotes.clear();
				}
				else
				  gameNotes.remove(0);

				gameThread.needNewNote = true;			
			  }
			  else
			  {
				appMidi.midiChannel.noteOn(pitch, 90);
				if ((gameSubType == appPrefs.NOTE_CHORDS && userNotes.size() == 3) ||
					gameSubType != appPrefs.NOTE_CHORDS)
					updateGameStats(0);
				
			  }
			}
			else
				appMidi.midiChannel.noteOn(pitch, 90);
		}
	}

	// check game notes against user notes
	public boolean checkGameStatus(Vector<Note> game, Vector<Integer> user)
	{
		int matchCount = 0;
		int checkSize = 1;
		
		/*
		// enable to debug
		System.out.print("[checkGameStatus] ");
		for (int i = 0; i < game.size(); i++)
			System.out.print(game.get(i).pitch + " ");
		System.out.print("  ");
		System.out.print(user);
    	System.out.println("");
		*/
		if (gameType != appPrefs.INLINE_MORE_NOTES)
		{
			if (game.size() != user.size())
				return false;
			checkSize = game.size();
		}

		for (int i = 0; i < checkSize; i++)
		{
			for (int j = 0; j < user.size(); j++)
			{
				if (game.get(i).pitch == user.get(j))
				{
					matchCount++;
					break;
				}
			}
		}
		if (matchCount == checkSize)
			return true;

		return false;
	}
	
	private void updateGameStats(int answType)
	{
		if (gameNotes.size() == 0)
			return;
		int score = -50;
		if (gameType == appPrefs.INLINE_LEARN_NOTES)
			score = 0;
		else if (answType == 1)
		{
			int xdelta = gameNotes.get(0).xpos - noteXStartPos;
			score = (xdelta * 100) / (getWidth() -  (staffHMargin * 2)); // find linear score based on note position
			score = 100 - score; // invert it to scale from 0 to 100
			score *= (currentSpeed / 40); // multiply by speed factor
		}

		stats.notePlayed(answType, score);
		gameBar.precisionCnt.setText(Integer.toString(stats.getAveragePrecision()) + "%");
		gameBar.scoreCnt.setText(Integer.toString(stats.getTotalScore()));
		if (gameType != appPrefs.INLINE_LEARN_NOTES)
		{
			if (answType == 1)
				gameBar.progress.setValue(gameBar.progress.getValue() + progressStep);
			else
				gameBar.progress.setValue(gameBar.progress.getValue() - 4);
			if (gameBar.progress.getValue() == 100)
				gameFinished(true);
			else if (gameBar.progress.getValue() == 0)
				gameFinished(false);
		}
	}
	
	private void gameFinished(boolean win)
	{
		gameStarted = false;
		sBar.playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
		refreshPanel();
		for (int i = 0; i < gameNotes.size(); i++)
		{
			appMidi.midiChannel.noteOff(gameNotes.get(i).pitch, 0);
			//if (gameType == appPrefs.INLINE_LEARN_NOTES)
			//	setLearningInfo(gameNotes.get(i).pitch, false);
		}
		gameNotes.clear();
		gameType = appPrefs.GAME_STOPPED;

		String title = "";
		int type = 0;

		if (win == true)
		{
			title = appBundle.getString("_congratulations");
			type = JOptionPane.INFORMATION_MESSAGE;
		}
		else
		{
             title = appBundle.getString("_sorry");
			 type = JOptionPane.ERROR_MESSAGE;
		}

		JOptionPane.showMessageDialog(this.getParent(),
					"  "+ stats.getCorrectNumber()+" "+ appBundle.getString("_correct")+
	                " / " + stats.getWrongNumber() + " " + appBundle.getString("_wrong")+ "  ",
	                title, type);
		
		if (Integer.parseInt(appPrefs.getProperty("saveStats")) == 1)
			stats.storeData(0);
	}

	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		sBar.setSize(getWidth(), sBarHeight);
		layers.setBounds(staffHMargin, staffVMargin, getWidth() - (staffHMargin * 2), staffHeight);
		inlineStaff.setBounds(0, 0, getWidth() - (staffHMargin * 2), staffHeight);
		notesLayer.setBounds(0, 0, getWidth() - (staffHMargin * 2), staffHeight);
		piano.setBounds(0, staffVMargin + staffHeight, getWidth(), pianoHeight);
		gameBar.setBounds(0, getHeight() - gBarHeight, getWidth(), gBarHeight);
	}
	

	private class InlineGameThread extends Thread 
	{
		boolean needNewNote = true;
		int noteXincrement = (currentSpeed < 121)?1:2; // above 120 the increment is of 2 pixels
		int sleepVal = 0;
		int marginX = inlineStaff.getStaffWidth();

		private InlineGameThread()
		{
			if (currentSpeed <= 120)
				sleepVal = ((120 - currentSpeed) * 10 / 80);
			else
				sleepVal = ((200 - currentSpeed) * 10 / 80);
			if (gameType == appPrefs.INLINE_MORE_NOTES)
			{
				noteXincrement = noteXincrement - (noteXincrement * 2); // change sign here
				noteXStartPos = marginX;
				marginX = inlineStaff.getFirstNoteXPosition();
				sleepVal *= 2; // slow down baby !
			}
			System.out.println("Thread increment: " + noteXincrement + ", sleep: " + sleepVal);
		}

		public void run() 
		{
			while (gameStarted) 
			{
				try
				{
					//System.out.println("Thread is running");
					if (needNewNote == true)
					{
						if (gameType != appPrefs.INLINE_MORE_NOTES)
							sleep(100);
						needNewNote = false;
						if (gameSubType == appPrefs.NOTE_CHORDS)
						{
							int chordType = inlineNG.getRandomChordorInterval(gameNotes, noteXStartPos, true, -1);
							if (gameType == appPrefs.INLINE_LEARN_NOTES)
								setLearningInfo(true, chordType);
							if (gameType != appPrefs.INLINE_MORE_NOTES)
								for (int j = 0; j < gameNotes.size(); j++)
									appMidi.midiChannel.noteOn(gameNotes.get(j).pitch, 90);
						}
						else if (gameSubType == appPrefs.NOTE_INTERVALS)
						{
							int intervalType = inlineNG.getRandomChordorInterval(gameNotes, noteXStartPos, false, gameInterval);
							if (gameType == appPrefs.INLINE_LEARN_NOTES)
								setLearningInfo(true, intervalType);
							if (gameType != appPrefs.INLINE_MORE_NOTES)
								for (int j = 0; j < gameNotes.size(); j++)
									appMidi.midiChannel.noteOn(gameNotes.get(j).pitch, 90);
						}
						else
						{
							Note newNote = inlineNG.getRandomNote(0);
							newNote.duration = 0; // set duration to 0 not to mess up X position
							newNote.xpos = noteXStartPos;
							gameNotes.add(newNote);
							if (gameType == appPrefs.INLINE_LEARN_NOTES)
								setLearningInfo(true, -1);
							System.out.println("Got note with pitch: " + newNote.pitch + " (level:" + newNote.level + ")");
							if (gameType != appPrefs.INLINE_MORE_NOTES)
								appMidi.midiChannel.noteOn(newNote.pitch, 90);
						}

						notesLayer.setNotesPositions();
						
					}
					else
					{
						for (int i = 0; i < gameNotes.size(); i++)
						{
							gameNotes.get(i).xpos+=noteXincrement;
							if ((gameType != appPrefs.INLINE_MORE_NOTES && gameNotes.get(i).xpos > marginX) ||
								(gameType == appPrefs.INLINE_MORE_NOTES && i == 0 && gameNotes.get(i).xpos < marginX))
							{
								if (gameType != appPrefs.INLINE_MORE_NOTES)
									appMidi.midiChannel.noteOff(gameNotes.get(i).pitch, 0);
								updateGameStats(0);
								if (gameType == appPrefs.INLINE_LEARN_NOTES)
									setLearningInfo(false, -1);
								gameNotes.removeElementAt(i);
							}
						}
						if (gameNotes.size() == 0)
							needNewNote = true;
						
						if (gameType == appPrefs.INLINE_MORE_NOTES && gameNotes.lastElement().xpos < noteXStartPos - 50)
							needNewNote = true;
					}
					//sleep(260 - currentSpeed);
					notesLayer.repaint();
					sleep(10 + sleepVal);
					
				}
				catch (Exception e) {  }
			}
		}
	}
}
