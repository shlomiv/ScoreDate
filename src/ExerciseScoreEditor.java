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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequencer;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class ExerciseScoreEditor extends JDialog implements ActionListener, PropertyChangeListener
{
	private static final long serialVersionUID = -4561500030719709787L;
	ResourceBundle appBundle;
	Preferences appPrefs;
	Font appFont;
	MidiController appMidi;
	
	Exercise currExercise;

	private RoundedButton wholeBtn, halfBtn, quartBtn, eightBtn;
	private RoundedButton wholePauseBtn, halfPauseBtn, quartPauseBtn, eightPauseBtn;
	
	private RoundedButton playBtn;
	
	private JScrollPane scoreScrollPanel;
	private JLayeredPane layers;
	private Staff scoreStaff;
	private NotesPanel notesLayer;
	private NoteGenerator exerciseNG;
	private Sequencer playback;
	
	RoundedButton removeNoteButton;
	RoundedButton finishButton;
	
	//private int rowsDistance = 90; // distance in pixel between staff rows
	private int timeNumerator = 4;
	private int timeDenominator = 4;
	
	private int measuresNumber = 1;
	private double measureCounter = 0;
	private double timeCounter = 0;
	
	private boolean isPlaying = false;
	
	public ExerciseScoreEditor(ResourceBundle b, Preferences p, Font f, MidiController mc, Exercise e)
	{
		appBundle = b;
		appPrefs = p;
		appFont = f;
		appMidi = mc;
		currExercise = e;

		String title = appBundle.getString("_exWizard") + " 3/3";
		setTitle(title);
        setLayout(null);
		setSize(700, 380);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window on the display
        
        JPanel backPanel = new JPanel();
        backPanel.setLayout(null);
        backPanel.setBackground(Color.white);
        backPanel.setBounds(0, 0, 700, 380);
        
        RoundPanel notesPanel = new RoundPanel(Color.decode("0xFFFFFF"), Color.decode("0xA2DDFF"));
        notesPanel.setLayout(null);
        notesPanel.setBackground(Color.white);
        notesPanel.setBounds(5, 5, 685, 112);

        int xPos = 10;
        int btnW = 60, btnH = 45;
        
        Font notesFont = appFont.deriveFont(52f);
        wholeBtn = new RoundedButton("w", appBundle, Color.decode("0xD6FFAA"));
        wholeBtn.setBackground(Color.decode("0xFFFFFF"));
        wholeBtn.setFont(notesFont);
        wholeBtn.setTextOffsets(5, 2);
        wholeBtn.setBounds(xPos, 10, btnW, btnH);
        wholeBtn.addActionListener(this);
        xPos+= btnW + 5;
        
        halfBtn = new RoundedButton("h", appBundle, Color.decode("0xD6FFAA"));
        halfBtn.setBackground(Color.decode("0xFFFFFF"));
        halfBtn.setFont(notesFont);
        halfBtn.setTextOffsets(8, 15);
        halfBtn.setBounds(xPos, 10, btnW, btnH);
        halfBtn.addActionListener(this);
        xPos+= btnW + 5;

        quartBtn = new RoundedButton("q", appBundle, Color.decode("0xD6FFAA"));
        quartBtn.setBackground(Color.decode("0xFFFFFF"));
        quartBtn.setFont(notesFont);
        quartBtn.setTextOffsets(8, 15);
        quartBtn.setBounds(xPos, 10, btnW, btnH);
        quartBtn.addActionListener(this);
        xPos+= btnW + 5;

        eightBtn = new RoundedButton("" + (char)0xC8, appBundle, Color.decode("0xD6FFAA"));
        eightBtn.setBackground(Color.decode("0xFFFFFF"));
        eightBtn.setFont(notesFont);
        eightBtn.setTextOffsets(8, 15);
        eightBtn.setBounds(xPos, 10, btnW, btnH);
        eightBtn.addActionListener(this);
        
        xPos = 10;
        
        wholePauseBtn = new RoundedButton("W", appBundle, Color.decode("0xFFC67F"));
        wholePauseBtn.setBackground(Color.decode("0xFFFFFF"));
        wholePauseBtn.setFont(notesFont);
        wholePauseBtn.setTextOffsets(2, 12);
        wholePauseBtn.setBounds(xPos, 58, btnW, btnH);
        wholePauseBtn.addActionListener(this);
        xPos+= btnW + 5;
        
        halfPauseBtn = new RoundedButton("H", appBundle, Color.decode("0xFFC67F"));
        halfPauseBtn.setBackground(Color.decode("0xFFFFFF"));
        halfPauseBtn.setFont(notesFont);
        halfPauseBtn.setTextOffsets(3, 17);
        halfPauseBtn.setBounds(xPos, 58, btnW, btnH);
        halfPauseBtn.addActionListener(this);
        xPos+= btnW + 5;

        quartPauseBtn = new RoundedButton("Q", appBundle, Color.decode("0xFFC67F"));
        quartPauseBtn.setBackground(Color.decode("0xFFFFFF"));
        quartPauseBtn.setFont(notesFont);
        quartPauseBtn.setTextOffsets(3, 17);
        quartPauseBtn.setBounds(xPos, 58, btnW, btnH);
        quartPauseBtn.addActionListener(this);
        xPos+= btnW + 5;

        eightPauseBtn = new RoundedButton("E", appBundle, Color.decode("0xFFC67F"));
        eightPauseBtn.setBackground(Color.decode("0xFFFFFF"));
        eightPauseBtn.setFont(notesFont);
        eightPauseBtn.setTextOffsets(3, 17);
        eightPauseBtn.setBounds(xPos, 58, btnW, btnH);
        eightPauseBtn.addActionListener(this);
        xPos+= btnW + 5;

        notesPanel.add(wholeBtn);
        notesPanel.add(halfBtn);
        notesPanel.add(quartBtn);
        notesPanel.add(eightBtn);
        notesPanel.add(wholePauseBtn);
        notesPanel.add(halfPauseBtn);
        notesPanel.add(quartPauseBtn);
        notesPanel.add(eightPauseBtn);
        
        if (currExercise.type == 0)
        {
        	wholeBtn.setEnabled(false);
        	halfBtn.setEnabled(false);
        	eightBtn.setEnabled(false);
        	wholePauseBtn.setEnabled(false);
        	halfPauseBtn.setEnabled(false);
        	quartPauseBtn.setEnabled(false);
        	eightPauseBtn.setEnabled(false);
        }

        if (currExercise.timeSign <= 0) timeNumerator = 4;
		else if (currExercise.timeSign == 1) timeNumerator = 2;
		else if (currExercise.timeSign == 2) timeNumerator = 3;
		else if (currExercise.timeSign == 3) { timeNumerator = 6; timeDenominator = 8; }
        
        measureCounter = timeNumerator;
        
        removeNoteButton = new RoundedButton("", appBundle, Color.decode("0xFF7F7F"));
        removeNoteButton.setBounds(xPos, 10, btnW, 92);
        removeNoteButton.setBackground(Color.decode("0xFFFFFF"));
        removeNoteButton.setButtonImage(new ImageIcon(getClass().getResource("/resources/wrong.png")).getImage());
        removeNoteButton.addActionListener(this);
        removeNoteButton.setEnabled(false);
        
        // Create playback button
 		playBtn = new RoundedButton("", appBundle);
 		playBtn.setBounds(580, 10, 90, 90);
 		playBtn.setBackground(Color.decode("0x8FC6E9"));
 		playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
 		playBtn.addActionListener(this);
 		
 		notesPanel.add(removeNoteButton);
 		notesPanel.add(playBtn);
        
        layers = new JLayeredPane();
		//layers.setPreferredSize( new Dimension(panelsWidth, staffHeight));
		layers.setPreferredSize(new Dimension(670, 145));
		layers.setBackground(Color.white);
		
		if (currExercise.type == 0)
			scoreStaff = new Staff(appFont, appBundle, appPrefs, currExercise.acc, true, true);
		else
			scoreStaff = new Staff(appFont, appBundle, appPrefs, currExercise.acc, false, true);
        scoreStaff.setBounds(0, 0, 670, 145);
        scoreStaff.setOpaque(true);
        scoreStaff.setClef(currExercise.clefMask);
        scoreStaff.setTimeSignature(timeNumerator, timeDenominator);
        scoreStaff.setMeasuresNumber(1);
        
        notesLayer = new NotesPanel(appFont, appPrefs, currExercise.notes, false);
		//notesLayer.setPreferredSize( new Dimension(670, 125));
		notesLayer.setBounds(0, 0, 670, 145);
		notesLayer.setOpaque(false);
		notesLayer.setClef(currExercise.clefMask);
		notesLayer.setStaffWidth(scoreStaff.getStaffWidth());
		notesLayer.setFirstNoteXPosition(scoreStaff.getFirstNoteXPosition());
		if (e.type != 1)
			notesLayer.setEditMode(true, false);
		else
			notesLayer.setEditMode(true, true);
		notesLayer.addPropertyChangeListener(this);
		
        layers.add(scoreStaff, new Integer(1));
        layers.add(notesLayer, new Integer(2));
        
        scoreScrollPanel = new JScrollPane(layers);
        scoreScrollPanel.setBounds(10, 120, 676, 166);
        scoreScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        finishButton = new RoundedButton(appBundle.getString("_exFinished"), appBundle, Color.decode("0x0E9B20"));
        finishButton.setBackground(Color.decode("0x13DC2E"));
        finishButton.setFont(new Font("Arial", Font.BOLD, 20));
        finishButton.setBounds(490, 295, 190, 40);
        finishButton.addActionListener(this);

        backPanel.add(notesPanel);
        backPanel.add(scoreScrollPanel);
        backPanel.add(finishButton);
        
        exerciseNG = new NoteGenerator(appPrefs, currExercise.acc, true);
        notesLayer.setEditNoteGenerator(exerciseNG);

        add(backPanel);
        setButtonsState();
	}
	
	private void setButtonsState()
	{
		if (currExercise.type == 0)
			return;
		System.out.println("Measure counter = " + measureCounter);
		if (measureCounter == 0)
		{
			if (timeNumerator >= 4)
			{
				wholeBtn.setEnabled(true);
				wholePauseBtn.setEnabled(true);
			}
			halfBtn.setEnabled(true);
			halfPauseBtn.setEnabled(true);
			quartBtn.setEnabled(true);
			quartPauseBtn.setEnabled(true);
		}
		else
		{
			boolean whole = true, half = true, quart = true; 
			if (measureCounter < 4)
				whole = false;
			if (measureCounter < 2)
				half = false;
			if (measureCounter < 1)
				quart = false;

			wholeBtn.setEnabled(whole);
			wholePauseBtn.setEnabled(whole);
			halfBtn.setEnabled(half);
			halfPauseBtn.setEnabled(half);
			quartBtn.setEnabled(quart);
			quartPauseBtn.setEnabled(quart);
		}
		if (currExercise.notes.size() > 0)
			finishButton.setEnabled(true);
		else
			finishButton.setEnabled(false);
	}
	
	private void addEditNote(double type, boolean isSilence)
	{
		Note tmpNote;
		if (measureCounter == 0)
		{
			measureCounter = timeNumerator;
			measuresNumber++;
			int staffW = scoreStaff.getWidth();
			staffW += (scoreStaff.getNotesDistance() * timeNumerator);
			scoreStaff.setBounds(0, 0, staffW, scoreStaff.getHeight());
			scoreStaff.setMeasuresNumber(measuresNumber);
			notesLayer.setBounds(0, 0, staffW, notesLayer.getHeight());
			notesLayer.setStaffWidth(scoreStaff.getStaffWidth());
			layers.setPreferredSize(new Dimension(staffW, layers.getHeight()));
			layers.setBounds(0, 0, staffW, layers.getHeight());
			scoreScrollPanel.getHorizontalScrollBar().setValue(scoreScrollPanel.getHorizontalScrollBar().getMaximum());
			scoreScrollPanel.repaint();
		}

		int pitch = exerciseNG.getPitchFromClefAndLevel(currExercise.clefMask, 12);
		pitch = exerciseNG.getAlteredFromBase(pitch);
		if (isSilence == true)
		{
			tmpNote = new Note(0, currExercise.clefMask, 12, pitch, 5, false, 0);
			tmpNote.duration = type;
		}
		else
			tmpNote = new Note(0, currExercise.clefMask, 12, pitch, (int)type, false, 0);
		
		measureCounter -= tmpNote.duration;
		tmpNote.setTimeStamp(timeCounter);
		timeCounter += tmpNote.duration;

		currExercise.notes.add(tmpNote);
		notesLayer.setEditNoteIndex(currExercise.notes.size() - 1);
		notesLayer.setNotesPositions();
		layers.repaint();
		removeNoteButton.setEnabled(true);
		setButtonsState();
	}

	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == wholeBtn)
			addEditNote(0, false);
		else if (ae.getSource() == halfBtn)
			addEditNote(1, false);
		else if (ae.getSource() == quartBtn)
			addEditNote(2, false);
		else if (ae.getSource() == eightBtn)
			addEditNote(3, false);
		else if (ae.getSource() == wholePauseBtn)
			addEditNote(4, true);
		else if (ae.getSource() == halfPauseBtn)
			addEditNote(2, true);
		else if (ae.getSource() == quartPauseBtn)
			addEditNote(1, true);
		else if (ae.getSource() == eightPauseBtn)
			addEditNote(0.5, true);
		else if (ae.getSource() == playBtn)
		{
			if (isPlaying == false)
			{
				playback = appMidi.createPlayback(appPrefs, currExercise.speed, currExercise.notes, timeDenominator / 4, true, 0);
				playback.addMetaEventListener(new MetaEventListener() {
			          public void meta(MetaMessage meta) 
			          {
			        	  byte[] metaData = meta.getData();
			              String strData = new String(metaData);
			        	  if ("end".equals(strData))
			              {
			        		appMidi.stopPlayback();
			  				playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
			  				playBtn.repaint();
			  				isPlaying = false;
			              }
			          }
					});
				playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/stop.png")).getImage());
				playback.start();
				isPlaying = true;
			}
			else
			{
				appMidi.stopPlayback();
				playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
				playBtn.repaint();
				isPlaying = false;
			}
		}
		else if (ae.getSource() == removeNoteButton)
		{
			int lastIdx =  currExercise.notes.size() - 1;
			measureCounter += currExercise.notes.get(lastIdx).duration;
			timeCounter -= currExercise.notes.get(lastIdx).duration;
			if (measureCounter == timeNumerator)
			{
				measureCounter = 0;
				if (measuresNumber > 1)
				{
					measuresNumber--;
					int staffW = scoreStaff.getWidth();
					staffW -= (scoreStaff.getNotesDistance() * timeNumerator);
					scoreStaff.setBounds(0, 0, staffW, scoreStaff.getHeight());
					scoreStaff.setMeasuresNumber(measuresNumber);
					notesLayer.setBounds(0, 0, staffW, notesLayer.getHeight());
					notesLayer.setStaffWidth(scoreStaff.getStaffWidth());
					layers.setPreferredSize(new Dimension(staffW, layers.getHeight()));
					layers.setBounds(0, 0, staffW, layers.getHeight());
					scoreScrollPanel.getHorizontalScrollBar().setValue(scoreScrollPanel.getHorizontalScrollBar().getMaximum());
					scoreScrollPanel.repaint();
				}
			}
			currExercise.notes.removeElementAt(lastIdx);

			if (currExercise.notes.size() > 0)
			{
				lastIdx--;
				notesLayer.setEditNoteIndex(lastIdx);
			}
			else
				removeNoteButton.setEnabled(false);

			notesLayer.setNotesPositions();
			layers.repaint();
			setButtonsState();
		}
		else if (ae.getSource() == finishButton)
		{
			currExercise.saveToXML();
			this.firePropertyChange("exerciseSaved", false, true);
			this.dispose();
		}
	}
	
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName() == "selectionChanged")
		{
			if (evt.getNewValue().equals(currExercise.notes.size() - 1))
				removeNoteButton.setEnabled(true);
			else
				removeNoteButton.setEnabled(false);
		}
		else if (evt.getPropertyName() == "levelChanged")
		{
			
		}
	}
}
