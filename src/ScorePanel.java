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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequencer;

import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class ScorePanel extends JPanel implements ActionListener 
{
	private static final long serialVersionUID = 1L;
	Font appFont;
	Preferences appPrefs;
	private ResourceBundle appBundle;
	private MidiController appMidi;
	private boolean isRhythm;

	public SmartBar sBar;
	private int sBarHeight = 125;
	private JLayeredPane layers;
	private Staff scoreStaff;
	private NotesPanel notesLayer;
	private GameBar gameBar;
	private Accidentals scoreAccidentals;
	private NoteGenerator scoreNG;
	private Statistics stats;
	Vector<Note> gameNotes = new Vector<Note>(); // array of random notes composing the game 
	Vector<Integer> userNotes = new Vector<Integer>(); // array of notes hit by the user

	// Graphics metrics 
	private int staffHMargin = 50;
	private int staffVMargin = 120;
	private int staffHeight = 400;
	private int gBarHeight = 40;
	private int rowsDistance = 90; // distance in pixel between staff rows
		
	// Time variables
	private int currentSpeed = 120;
	private int latency = 0;
	private int timeNumerator = 4;
	private int timeDenominator = 4;
	private int timeDivision = 1;

	// MIDI controllers
	private Sequencer metronome;
	private Sequencer playback;
	
	// Game variables
	private ScoreGameThread gameThread = null;
	private boolean gameStarted = false; // variable to control thread job
	private int gameType = -1; // type of game. See prefernces for values
	private long startTime; // timestamp of cursor at the beginning of a row
	private int currentNoteIndex = -1; // index of the currently playing note
	private int cursorStartX; // X start position of cursor for each row
	private int cursorX; // X position of cursor during game
	private int cursorY; // Y position of cursor during game
	
	// Variables to check notes validity
	private int accuracy = 24; // a note is valid within 24 pixels around the note X position 
	private int releaseXpos; // on key press, save the release X position for key release check
	
	public ScorePanel(Font f, ResourceBundle b, Preferences p, MidiController mc, Dimension d, boolean rhythm)
	{
		appFont = f;
		appBundle = b;
		appPrefs = p;
		appMidi = mc;
		isRhythm = rhythm;
		
		setBackground(Color.white);
		setSize(d);
		setLayout(null);
		scoreAccidentals = new Accidentals("", 0, appPrefs);
		scoreNG = new NoteGenerator(appPrefs, scoreAccidentals);
		stats = new Statistics();
		
		sBar = new SmartBar(new Dimension(d.width-8, sBarHeight), b, f, p, false);
		sBar.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getPropertyName() == "updateParameters")
				{
					System.out.println("SCORE panel update parameters !");
					refreshPanel();
				}
			}
		});
		
		sBar.refreshBtn.addActionListener(this);
		sBar.playBtn.addActionListener(this);
		sBar.listenBtn.addActionListener(this);
		
		int panelsWidth = d.width - (staffHMargin * 2);
		staffHeight = getHeight() - sBarHeight - gBarHeight;
		
		layers = new JLayeredPane();
		layers.setPreferredSize( new Dimension(panelsWidth, staffHeight));
		layers.setBounds(staffHMargin, staffVMargin, panelsWidth, staffHeight);
		
		scoreStaff = new Staff(appFont, appBundle, appPrefs, scoreAccidentals, false, true);
		scoreStaff.setPreferredSize( new Dimension(panelsWidth, staffHeight));
		scoreStaff.setBounds(0, 0, panelsWidth, staffHeight);
		scoreStaff.setOpaque(true);
		
		notesLayer = new NotesPanel(appFont, appPrefs, gameNotes, false);
		notesLayer.setPreferredSize( new Dimension(panelsWidth, staffHeight));
		notesLayer.setBounds(0, 0, panelsWidth, staffHeight);
		notesLayer.setOpaque(false);
		
		layers.add(scoreStaff, new Integer(1));
		layers.add(notesLayer, new Integer(2));
		
		gameBar = new GameBar(new Dimension(d.width, gBarHeight), b, f, p, false);
		gameBar.setBounds(0, getHeight() - gBarHeight, getWidth(), gBarHeight);
		
		add(sBar);
		add(layers);
		add(gameBar);
		refreshPanel();
	}
	
	public void refreshPanel()
	{
		scoreNG.update();
		
		rowsDistance = scoreNG.getRowsDistance();
		scoreStaff.setRowsDistance(rowsDistance);
		notesLayer.setRowsDistance(rowsDistance);
		scoreStaff.setClef(scoreNG.getClefMask());
		notesLayer.setClef(scoreNG.getClefMask());
		System.out.println("Staff width = " + scoreStaff.getStaffWidth());
		System.out.println("rowsDistance = " + rowsDistance);
		notesLayer.setStaffWidth(scoreStaff.getStaffWidth());
		
		int tsIdx = Integer.parseInt(appPrefs.getProperty("timeSignature"));
		timeDenominator = 4;
		if (tsIdx <= 0) timeNumerator = 4;
		else if (tsIdx == 1) timeNumerator = 2;
		else if (tsIdx == 2) timeNumerator = 3;
		else if (tsIdx == 3) { timeNumerator = 6; timeDenominator = 8; }
			
		scoreStaff.setTimeSignature(timeNumerator, timeDenominator);
		timeDivision = timeDenominator / 4;

		notesLayer.setFirstNoteXPosition(scoreStaff.getFirstNoteXPosition());
		
		if (Integer.parseInt(appPrefs.getProperty("showBeats")) == 1)
			notesLayer.enableCursor(true);
		else
			notesLayer.enableCursor(false);
		
		latency = Integer.parseInt(appPrefs.getProperty("latency"));
		if (latency < 0) latency = 0;
		
		createNewSequence();
	}
	
	public void updateLanguage(ResourceBundle bundle)
	{
		appBundle = bundle;
		sBar.updateLanguage(appBundle);
		gameBar.updateLanguage(appBundle);
	}

	public void createNewSequence()
	{
		scoreNG.getRandomSequence(gameNotes, scoreStaff.getMeasuresNumber(), isRhythm);
		notesLayer.setNotesPositions();
	}

	private void updateGameStats(int answType)
	{
		int score = -50;
		if (answType == 1)
		{
			score = 100;
			score *= (currentSpeed / 40); // multiply by speed factor
		}

		stats.notePlayed(answType, score);
		gameBar.precisionCnt.setText(Integer.toString(stats.getAveragePrecision()) + "%");
		gameBar.scoreCnt.setText(Integer.toString(stats.getTotalScore()));
	}
	
	private void gameFinished()
	{
		String title;
		int type = 0;
		int correct = stats.getCorrectNumber();
		int wrong = stats.getWrongNumber();
		int rhythms = stats.getWrongRhythms();
		
		if (wrong == 0)
		{
			title = appBundle.getString("_congratulations");
			type = JOptionPane.INFORMATION_MESSAGE;
		}
		else if (correct > wrong)
		{
			title = appBundle.getString("_sorry");
			type = JOptionPane.WARNING_MESSAGE;
		}
		else
		{
             title = appBundle.getString("_sorry");
			 type = JOptionPane.ERROR_MESSAGE;
		}
		
		JOptionPane.showMessageDialog(this.getParent(), "<html><b>" + stats.getNotesPlayed() + " " + appBundle.getString("_menuNotes") +
				" : "+ stats.getCorrectNumber() + " "+ appBundle.getString("_correct")+
                " / " + stats.getWrongNumber() + " " + appBundle.getString("_wrong") + 
                " ( " + rhythms + " "+ appBundle.getString("_wrongrhythm") + " )</b></html>",
                title, type);
		
		if (Integer.parseInt(appPrefs.getProperty("saveStats")) == 1)
		{
			if (gameType == appPrefs.RHTYHM_GAME_USER)
				stats.storeData(1);
			else if (gameType == appPrefs.SCORE_GAME_USER)
				stats.storeData(2);
		}
	}

	private void checkNote(int currPos, int pitch, boolean press)
	{
		if (currentNoteIndex < 0 || currentNoteIndex >= gameNotes.size())
			return;

		// adjust X position if latency is on
		if (latency > 0)
			currPos -= ((scoreStaff.getNotesDistance() * latency) / (60000 / currentSpeed));

		if (press == true) // check key press
		{
			if (gameNotes.size() == 0) return; // security check
			int lookupIndex = currentNoteIndex;
			int noteMargin = gameNotes.get(lookupIndex).xpos - (accuracy / 2);
			boolean noteFound = false;

			// check against current note X position + margins
			if (currPos > noteMargin && currPos < noteMargin + accuracy)
				noteFound = true;

			// maybe the user pressed the key too early. Check on the next note
			if (noteFound == false && lookupIndex < gameNotes.size())
			{
				lookupIndex++;
				if (gameNotes.size() == 0) return; // security check
				noteMargin = gameNotes.get(lookupIndex).xpos - (accuracy / 2);
					
				if (currPos > noteMargin && currPos < noteMargin + accuracy)
					noteFound = true;
			}
			
			// still not found ? Display a warning
			if (noteFound == false)
			{
				notesLayer.drawAnswer(2, currPos, cursorY);
				lookupIndex = currentNoteIndex;
			}
			else
			{
				if (gameNotes.size() == 0) return; // security check

				if ((pitch == gameNotes.get(lookupIndex).pitch || 
					gameType == appPrefs.RHTYHM_GAME_USER) && // any pitch is OK for rhythm game 
					gameNotes.get(lookupIndex).type != 5)  
					{
						notesLayer.drawAnswer(1, currPos, cursorY); // pitch and rhythm correct
						updateGameStats(1);
					}
				else
				{
					notesLayer.drawAnswer(0, currPos, cursorY); // wrong pitch
					updateGameStats(0);
				}
			}

			if (gameNotes.size() == 0)return; // security check
			// is last note of array or last note of the row ?
			if (lookupIndex == gameNotes.size() - 1 || gameNotes.get(lookupIndex + 1).xpos < noteMargin)
				releaseXpos = scoreStaff.getStaffWidth() - (accuracy / 2);
			else
				releaseXpos = gameNotes.get(lookupIndex + 1).xpos - (accuracy / 2);
			
			if (gameNotes.size() == 0) return; // security check
		    System.out.println("[checkNote] noteIdx: " + currentNoteIndex + 
					   ", xpos: " + gameNotes.get(currentNoteIndex).xpos +
					   ", cursor: " + currPos +
			           ", noteMargin: " + noteMargin + 
					   ", releaseXpos: " + releaseXpos);
		}
		else // check release
		{
			System.out.println("[checkNote *release*] currPos: " +  currPos + ", noteXpos: " + gameNotes.get(currentNoteIndex).xpos);
			 
			if (((currPos < releaseXpos && currPos > cursorStartX + accuracy) || 
				 currPos > releaseXpos + accuracy) && currPos > gameNotes.get(currentNoteIndex).xpos + accuracy)
			{
					notesLayer.drawAnswer(2, currPos, cursorY);
					updateGameStats(2);
			}
		}
	}

	public void noteEvent(int pitch, int velocity)
	{
		if (gameType != appPrefs.SCORE_GAME_LISTEN)
		{
			if (velocity != 0)
			{
				appMidi.midiChannel.noteOn(pitch, 90);
				checkNote(cursorX, pitch, true);
			}
			else
			{
				appMidi.midiChannel.noteOff(pitch, 0);
				checkNote(cursorX, pitch, false);
			}			
		}
	}

	private void handleAsyncMIDIevent(MetaMessage msg)
	{
		byte[] metaData = msg.getData();
        String strData = new String(metaData);
       
        //System.out.println("META message: text= " + strData);

        if ("beat".equals(strData)) 
        {
        	if (gameType != appPrefs.SCORE_GAME_LISTEN && sBar.metronomeCheckBox.isSelected() == true)
        		notesLayer.drawMetronome(cursorX, cursorY);
        }
        else if ("gameOn".equals(strData))
        {
        	// this is a workaround
        	notesLayer.drawCursor(cursorX, cursorY, true);
        }
        else if ("cursorOn".equals(strData))
        {
        	cursorStartX = scoreStaff.getFirstNoteXPosition() - scoreStaff.getNotesDistance();
        	cursorX = cursorStartX;
        	cursorY = 10;
        	startTime = System.currentTimeMillis();
        }
        else if ("nOn".equals(strData))
        {
        	if (gameType == appPrefs.SCORE_GAME_LISTEN)
        		notesLayer.highlightNote(currentNoteIndex, true);
        }
        else if ("nOff".equals(strData))
        {
        	if (gameType == appPrefs.SCORE_GAME_LISTEN)
        		notesLayer.highlightNote(currentNoteIndex, false);
        	currentNoteIndex++;
        	gameBar.progress.setValue((currentNoteIndex * 100) / gameNotes.size());
        }
        else if ("end".equals(strData))
        {
        	gameStarted = false;
        	if (gameType == appPrefs.SCORE_GAME_LISTEN)
        		appMidi.stopPlayback();
        	appMidi.stopMetronome();
			sBar.playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
			sBar.playBtn.repaint();
			startTime = 0;
			notesLayer.drawCursor(cursorX, cursorY, true);
			if (gameType != appPrefs.SCORE_GAME_LISTEN)
				gameFinished();
			gameType = appPrefs.GAME_STOPPED;
        }
        
	}

	private void createPlayback(boolean playOnly)
	{
		sBar.playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/stop.png")).getImage());
		sBar.playBtn.repaint();
		currentSpeed = sBar.tempoSlider.getValue();
		metronome = appMidi.createMetronome(appPrefs, currentSpeed, scoreStaff.getMeasuresNumber(), timeNumerator, timeDivision);
		metronome.addMetaEventListener(new MetaEventListener() {
            public void meta(MetaMessage meta) 
            {
            	handleAsyncMIDIevent(meta);
            }
		});
	    playback = appMidi.createPlayback(appPrefs, currentSpeed, gameNotes, timeDivision, playOnly);
		playback.addMetaEventListener(new MetaEventListener() {
          public void meta(MetaMessage meta) 
          {
          	handleAsyncMIDIevent(meta);
          }
		});
		
		currentNoteIndex = 0;
		metronome.start();
		playback.start();
	}
	
	public void stopGame()
	{
		if(gameType == appPrefs.GAME_STOPPED)
			return;
		appMidi.stopPlayback();
		appMidi.stopMetronome();
		if (gameType == appPrefs.SCORE_GAME_LISTEN)
		{
			notesLayer.highlightNote(currentNoteIndex, false);
			currentNoteIndex = -1;
		}
		gameBar.scoreCnt.setText("");
		gameBar.progress.setValue(0);
		sBar.playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
		sBar.playBtn.repaint();
		gameStarted = false;
		gameType = appPrefs.GAME_STOPPED;
	}
		
	public void actionPerformed(ActionEvent ae)

	{
		if (ae.getSource() == sBar.playBtn)
		{
			if (gameType == appPrefs.SCORE_GAME_LISTEN)
			{
				stopGame();
				return;
			}
			
			if (gameThread != null && gameThread.isAlive() == true)
			{
				/* ************** STOP CURRENT GAME ***************** */
				stopGame();
			}
			else
			{
				/* ************** START NEW GAME ***************** */
				if (isRhythm == true)
					gameType = appPrefs.RHTYHM_GAME_USER;
				else 
					gameType = appPrefs.SCORE_GAME_USER;
				//notesLayer.repaint();
				gameBar.precisionCnt.setText("");
				gameBar.scoreCnt.setText("");
				gameBar.progress.setValue(0);
				stats.reset();
				gameThread = new ScoreGameThread();
				gameStarted = true;
				cursorX = cursorY = 0; 
				startTime = 0;
				createPlayback(false);
				stats.setGameSpeed(currentSpeed);
				gameThread.start();
				
			}
		}
		else if (ae.getSource() == sBar.refreshBtn)
		{
			refreshPanel();
			notesLayer.repaint();
		}
		else if (ae.getSource() == sBar.listenBtn)
		{
			gameType = appPrefs.SCORE_GAME_LISTEN;
			createPlayback(true);
		}
	}
	
	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		sBar.setSize(getWidth(), sBarHeight);
		staffHeight = getHeight() - sBarHeight - gBarHeight;
		layers.setBounds(staffHMargin, staffVMargin, getWidth() - (staffHMargin * 2), staffHeight);
		scoreStaff.setBounds(0, 0, getWidth() - (staffHMargin * 2), staffHeight);
		notesLayer.setBounds(0, 0, getWidth() - (staffHMargin * 2), staffHeight);
		gameBar.setBounds(0, getHeight() - gBarHeight, getWidth(), gBarHeight);
		
		//System.out.println("--------- REFRESH PANEL **********");
		/*
		rowsDistance = scoreNG.getRowsDistance();
		scoreStaff.setRowsDistance(rowsDistance);
		notesLayer.setRowsDistance(rowsDistance);
		notesLayer.setStaffWidth(scoreStaff.getStaffWidth());
		createNewSequence();
		*/
	}
	
	private class ScoreGameThread extends Thread 
	{
		int noteDistance = scoreStaff.getNotesDistance();
		//int beatsPerRow = (scoreStaff.getWidth() - scoreStaff.getFirstNoteXPosition()) / noteDistance;
		int cursorXlimit = scoreStaff.getStaffWidth();

		private ScoreGameThread()
		{
			//System.out.println("beatsPerRow: " + beatsPerRow);
		}

		public void run() 
		{
			while (gameStarted) 
			{
				try
				{
					
					if (startTime != 0)
					{
						cursorX = cursorStartX + ((int)(System.currentTimeMillis() - startTime) * (noteDistance*timeDivision))/(60000 / currentSpeed);
						notesLayer.drawCursor(cursorX, cursorY, false);
						if (cursorX >= cursorXlimit)
						{
							notesLayer.drawCursor(cursorX, cursorY, true);
							cursorY+=rowsDistance;
							cursorStartX = scoreStaff.getFirstNoteXPosition() - 10;
							cursorX = cursorStartX;
							startTime = System.currentTimeMillis();
							continue;
						}
					}
					sleep(10);
					
				}
				catch (Exception e) {  }
			}
		}
	}

	
}
