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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;

public class EarTrainingPanel extends JPanel implements ActionListener 
{
	private static final long serialVersionUID = 1L;
	Font appFont;
	Preferences appPrefs;
	private ResourceBundle appBundle;
	private MidiController appMidi;
	private Accidentals earAccidentals;
	private NoteGenerator earNG;
	
	// GUI elements
	public SmartBar sBar;
	private int sBarHeight = 130;
	private int pianoHeight = 80;
	private int gBarHeight = 40;
	private JLabel gameTitle;
	private RoundPanel noteContainer;
	private Icon listenIcon;
	private JLabel questionLabel, answerLabel, commentLabel;
	private JPanel buttonsContainer;
	private Vector<RoundedButton> notesButtons = new Vector<RoundedButton>();
	private Piano piano;
	private GameBar gameBar;
	
	Vector<Note> gameNotes = new Vector<Note>();
	private Statistics stats;
	
	private int currentSpeed = 6;
	private EarTrainingGameThread gameThread = null;
	private boolean gameStarted = false; 
	
	private int gameType = -1;
	private int gameSubType = -1;
	private int progressStep = 1;
	
	public EarTrainingPanel(Font f, ResourceBundle b, Preferences p, MidiController mc, Dimension d)
	{
		appFont = f;
		appBundle = b;
		appPrefs = p;
		appMidi = mc;
		
		setBackground(Color.white);
		setSize(d);
		setLayout(null);
		earAccidentals = new Accidentals("", 0, appPrefs);
		earNG = new NoteGenerator(appPrefs, earAccidentals, false);
		stats = new Statistics();
		
		gameType = appPrefs.GAME_STOPPED;
		
		sBar = new SmartBar(new Dimension(d.width, sBarHeight), b, f, p, true, true);
		sBar.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getPropertyName() == "updateParameters")
				{
					System.out.println("EAR TRAINING panel update parameters !");
					refreshPanel();
				}
			}
		});

		sBar.playBtn.addActionListener(this);
		
		gameTitle = new JLabel("", null, JLabel.CENTER);
		gameTitle.setFont(new Font("Arial", Font.BOLD, 25));
		gameTitle.setForeground(Color.decode("0x7666A7"));
		gameTitle.setBounds(0, sBarHeight, getWidth(), 30);
		
		noteContainer = new RoundPanel(Color.decode("0xFFFFFF"), Color.decode("0xB59CFF"));
		noteContainer.setLayout(null);
		noteContainer.setBackground(Color.white);
		noteContainer.setBorderColor(Color.decode("0x7666A7"));
		noteContainer.setBounds((getWidth() / 2) - 200, (getHeight() / 2 - 90), 400, 160);
		
		listenIcon = new ImageIcon(getClass().getResource("/resources/listen.png"));
		
		questionLabel = new JLabel("", listenIcon, JLabel.CENTER);
		questionLabel.setFont(new Font("Arial", Font.BOLD, 60));
		questionLabel.setForeground(Color.decode("0x28B228"));
		questionLabel.setBounds(0, 0, 200, 160);
		questionLabel.setVisible(false);
		
		answerLabel = new JLabel("", null, JLabel.CENTER);
		answerLabel.setFont(new Font("Arial", Font.BOLD, 60));
		answerLabel.setForeground(Color.decode("0xB22525"));
		answerLabel.setBounds(200, 0, 200, 160);
		answerLabel.setVisible(false);
		
		commentLabel = new JLabel("", null, JLabel.CENTER);
		commentLabel.setFont(new Font("Arial", Font.BOLD, 30));
		commentLabel.setForeground(Color.decode("0x28B228"));
		commentLabel.setBounds(0, 110, 400, 35);
		commentLabel.setVisible(true);

		noteContainer.add(questionLabel);
		noteContainer.add(answerLabel);
		noteContainer.add(commentLabel);

		piano = new Piano(73);
		piano.setPreferredSize( new Dimension(d.width, pianoHeight));
		piano.setBounds(0, getHeight() - gBarHeight - 180, d.width, pianoHeight);
		
		for (int i = 0; i < 73; i++)
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
		piano.setVisible(false);
		
		buttonsContainer = new JPanel();
		buttonsContainer.setLayout(null);
		buttonsContainer.setBackground(Color.white);
		buttonsContainer.setBounds((getWidth() / 2) - 300, getHeight() - gBarHeight - 180, 595, 80);
		
		Font tmpF = new Font("Arial", Font.BOLD, 25);
		for (int i = 0; i < 7; i ++)
		{
			RoundedButton tmpBtn = new RoundedButton("", appBundle, Color.decode("0x7666A7"));
			tmpBtn.setBounds(i*85, 0, 80, 80);
			tmpBtn.setFont(tmpF);
			tmpBtn.setTextOffsets(0, 23);
			tmpBtn.addActionListener(this);
			notesButtons.add(tmpBtn);
			buttonsContainer.add(tmpBtn);
		}
		
		gameBar = new GameBar(new Dimension(d.width, gBarHeight), b, f, p, true);
		gameBar.setBounds(0, getHeight() - gBarHeight, getWidth(), gBarHeight);
		gameBar.progress.setValue(20);
		
		sBar.gameSelector.addActionListener(this);
		
		add(sBar);
		add(gameTitle);
		add(noteContainer);
		add(piano);
		add(buttonsContainer);
		add(gameBar);
		refreshPanel();
	}

	public void refreshPanel()
	{
		String[] notesStr = { "_do", "_re", "_mi", "_fa", "_sol", "_la", "_si" };
		gameSubType = sBar.gameSelector.getSelectedIndex();
		stopGame();
		
		for (int i = 0; i < 7; i++)
		{
			notesButtons.get(i).setLabel(appBundle.getString(notesStr[i]));
		}

		switch(gameSubType)
		{
			case 0:
				earNG.reset();
				earNG.addRange(appPrefs.TREBLE_CLEF, 60, 71); // from C3 to B3
				gameTitle.setText(appBundle.getString("_earOctaves") + ": 1, " + appBundle.getString("_alterednotes") + ": " + 
								  appBundle.getString("_no") + ", " + appBundle.getString("_notes") + ": " + earNG.getNotesNumber());
				buttonsContainer.setVisible(true);
				piano.setVisible(false);
				sBar.clefNoteBtn.setEnabled(false);
			break;
			case 1:
				earNG.reset();
				earNG.addRange(appPrefs.TREBLE_CLEF, 48, 71); // from C2 to B3
				gameTitle.setText(appBundle.getString("_earOctaves") + ": 2, " + appBundle.getString("_alterednotes") + ": " + 
						  appBundle.getString("_no") + ", " + appBundle.getString("_notes") + ": " + earNG.getNotesNumber());
				buttonsContainer.setVisible(true);
				piano.setVisible(false);
				sBar.clefNoteBtn.setEnabled(false);
			break;
			case 2:
				earNG.reset();
				earNG.addRange(appPrefs.TREBLE_CLEF, 36, 84); // from C1 to C5
				gameTitle.setText(appBundle.getString("_earOctaves") + ": 4, " + appBundle.getString("_alterednotes") + ": " + 
						  appBundle.getString("_yes") + ", " + appBundle.getString("_notes") + ": " + earNG.getNotesNumber());
				buttonsContainer.setVisible(false);
				piano.setVisible(true);
				sBar.clefNoteBtn.setEnabled(false);
			break;
			case 3:
				earNG.update();
				gameTitle.setText(appBundle.getString("_earOctaves") + ": 1, " + appBundle.getString("_alterednotes") + ": " + 
						  appBundle.getString("_no") + ", " + appBundle.getString("_notes") + ": " + earNG.getNotesNumber());
				buttonsContainer.setVisible(false);
				piano.setVisible(true);
				sBar.clefNoteBtn.setEnabled(true);
			break;
		}		
			
		//piano.reset(true);
		
	}
	
	public void updateLanguage(ResourceBundle bundle)
	{
		System.out.println("EAR TRAINING - update language");
		appBundle = bundle;
		sBar.updateLanguage(appBundle);
		gameBar.updateLanguage(appBundle);
		refreshPanel();
	}

	public void startGame()
	{
		currentSpeed = sBar.tempoSlider.getValue();
		int notesNum = gameBar.notesNumber.getSelectedIndex();
		switch(notesNum)
		{
			case 0: progressStep = 8; break; // 10 notes
			case 1: progressStep = 4; break; // 20 notes
			case 2: progressStep = 2; break; // 40 notes
			case 3: progressStep = 1; break; // 80 notes
		}
		gameType = appPrefs.EAR_TRAINING;
		gameNotes.clear();
		stats.reset();
		gameBar.precisionCnt.setText("");
		gameBar.scoreCnt.setText("");
		gameBar.progress.setValue(20);
		gameThread = new EarTrainingGameThread();
		gameStarted = true;
		gameThread.start();
		sBar.playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/stop.png")).getImage());
		sBar.playBtn.repaint();
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
				appMidi.stopNote(gameNotes.get(i).pitch, 0);
			gameNotes.clear();
			gameType = appPrefs.GAME_STOPPED;
		}
	}
	
	private void checkNote(int value, boolean fromMidi)
	{
		int noteIdx = 0;
		int alt = 0;
		int octave = -1;
		boolean rightAnswer = false;
		
		showQuestion();

		if (gameSubType == 2 || gameSubType == 3 || fromMidi == true)
		{
			if (gameNotes.size() > 0 && value == gameNotes.get(0).pitch)
				rightAnswer = true;
			noteIdx = piano.getNoteIndexFromPitch(value);
			if (gameSubType == 2 || gameSubType == 3)
				octave = piano.getOctaveFromPitch(value);
			if (noteIdx >= 100)
			{
				alt = 1;
				noteIdx-=100;
			}
		}
		else
		{
			noteIdx = value;
			if (gameNotes.size() > 0)
			{
				int qIdx = piano.getNoteIndexFromPitch(gameNotes.get(0).pitch);
				if (noteIdx == qIdx)
					rightAnswer = true;
			}
		}

		if (gameType != appPrefs.GAME_STOPPED)
		{
			if (rightAnswer == true)
			{
				answerLabel.setForeground(Color.decode("0x28B228"));
				commentLabel.setForeground(Color.decode("0x28B228"));
				commentLabel.setText(appBundle.getString("_congratulations"));
				gameBar.progress.setValue(gameBar.progress.getValue() + progressStep);
				stats.notePlayed(1, 100);
			}
			else
			{
				answerLabel.setForeground(Color.decode("0xB22525"));
				commentLabel.setForeground(Color.decode("0xB22525"));
				commentLabel.setText(appBundle.getString("_sorry"));
				gameBar.progress.setValue(gameBar.progress.getValue() - 4);
				stats.notePlayed(0, 0);
			}

		gameBar.precisionCnt.setText(Integer.toString(stats.getAveragePrecision()) + "%");
		gameBar.scoreCnt.setText(Integer.toString(stats.getTotalScore()));
		}

		setLabelInfo(answerLabel, noteIdx, octave, alt);
		
		gameNotes.clear();
		if (gameBar.progress.getValue() == 100)
			gameFinished(true);
		else if (gameBar.progress.getValue() == 0)
			gameFinished(false);
		else if (gameThread != null)
			gameThread.resetTimer();
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
	
	private void setLabelInfo(JLabel l, int noteIdx, int oct, int alt)
	{
		String noteInfo = "";
		noteInfo = getLabelFromIndex(noteIdx);
		if (oct != -1)
			noteInfo += Integer.toString(oct);
		if (alt == 1)
			noteInfo+="#";
	
		l.setText(noteInfo);
		l.setIcon(null);
		l.setVisible(true);
	}
	
	private void showQuestion()
	{
		if (gameNotes.size() > 0)
		{
			int alt = 0;
			int noteIdx = piano.getNoteIndexFromPitch(gameNotes.get(0).pitch);
			int octave = -1;
			if (gameSubType == 2 || gameSubType == 3)
				octave = piano.getOctaveFromPitch(gameNotes.get(0).pitch);
			if (noteIdx >= 100)
			{
				alt = 1;
				noteIdx-=100;
			}
			setLabelInfo(questionLabel, noteIdx, octave, alt);
		}
		questionLabel.setIcon(null);
		for (int i = 0; i < gameNotes.size(); i++)
			appMidi.stopNote(gameNotes.get(i).pitch, 0);
	}
	
	private void gameFinished(boolean win)
	{
		gameStarted = false;
		sBar.playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
		refreshPanel();
		for (int i = 0; i < gameNotes.size(); i++)
		{
			appMidi.stopNote(gameNotes.get(i).pitch, 0);
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
	
	private void pianoKeyPressed(Key k, boolean pressed)
	{
		System.out.println("[pianoKeyPressed] pitch = " + k.pitch);
		checkNote(k.pitch, false);
	}

	public void noteEvent(int pitch, int velocity, boolean fromPiano)
	{
		if (velocity == 0)
			appMidi.stopNote(pitch, 0);
		else
		{
			appMidi.playNote(pitch, 90);
			checkNote(pitch, true);
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
				startGame();
			}
		}
		else if (ae.getSource() == sBar.gameSelector)
		{
			refreshPanel();
		}
		else
		{
			for (int i = 0; i < 7; i++)
			{
				if (ae.getSource() == notesButtons.get(i))
				{
					checkNote(i, false);
					return;
				}
			}
		}
	}
	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		sBar.setSize(getWidth(), sBarHeight);
		gameTitle.setBounds(0, sBarHeight, getWidth(), 30);
		noteContainer.setBounds((getWidth() / 2) - 200, (getHeight() / 2 - 90), 400, 160);
		buttonsContainer.setBounds((getWidth() / 2) - 300, getHeight() - gBarHeight - 120, 600, 80);
		piano.setBounds(0, getHeight() - gBarHeight - 130, getWidth(), pianoHeight);
		gameBar.setBounds(0, getHeight() - gBarHeight, getWidth(), gBarHeight);
	}
	
	private class EarTrainingGameThread extends Thread 
	{
		boolean needNewNote = true;
		int pauseBetweenNotes = 20;
		int noteTimeout = (currentSpeed * 10) + pauseBetweenNotes;
		int timeoutCounter = pauseBetweenNotes;

		public void resetTimer()
		{
			timeoutCounter = 0;
			needNewNote = true;
		}

		public void run() 
		{
			while (gameStarted) 
			{
				try
				{
					if (needNewNote == true && timeoutCounter > pauseBetweenNotes)
					{
						Note newNote = null;
						if (gameSubType == 2) newNote = earNG.getRandomNote(0, true); // alterations only on advanced game
						else newNote = earNG.getRandomNote(0, false);
						
						gameNotes.add(newNote);
						appMidi.playNote(newNote.pitch, 90);
						needNewNote = false;
						questionLabel.setText("");
						answerLabel.setText("");
						commentLabel.setText("");
						questionLabel.setIcon(listenIcon);
						questionLabel.setVisible(true);
					}
					
					if (timeoutCounter == noteTimeout) // timed out without an answer
					{
						for (int i = 0; i < gameNotes.size(); i++)
							appMidi.stopNote(gameNotes.get(i).pitch, 0);
						needNewNote = true;
						timeoutCounter = 0;
						showQuestion();
						gameNotes.clear();
					}
					timeoutCounter++;
					sleep(100);
				}
				catch (Exception e) {  }
			}
		}
	}
}
