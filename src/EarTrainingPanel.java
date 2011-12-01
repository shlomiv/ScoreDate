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
	private JPanel buttonsContainer;
	private Vector<RoundedButton> notesButtons = new Vector<RoundedButton>();
	private Piano piano;
	private GameBar gameBar;
	
	Vector<Note> gameNotes = new Vector<Note>();
	Vector<Integer> userNotes = new Vector<Integer>();

	private EarTrainingGameThread gameThread = null;
	private boolean gameStarted = false; 
	
	private int gameType = -1;
	
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
		int gameIdx = sBar.gameSelector.getSelectedIndex();
		stopGame();
		
		for (int i = 0; i < 7; i++)
		{
			notesButtons.get(i).setLabel(appBundle.getString(notesStr[i]));
		}

		switch(gameIdx)
		{
			case 0:
				earNG.reset();
				earNG.addRange(appPrefs.TREBLE_CLEF, 64, 76); // from E3 to E4
				gameTitle.setText(appBundle.getString("_earOctaves") + ": 1, " + appBundle.getString("_alterednotes") + ": " + 
								  appBundle.getString("_no") + ", " + appBundle.getString("_notes") + ": " + earNG.getNotesNumber());
				buttonsContainer.setVisible(true);
				piano.setVisible(false);
				sBar.clefNoteBtn.setEnabled(false);
			break;
			case 1:
				earNG.reset();
				earNG.addRange(appPrefs.TREBLE_CLEF, 57, 81); // from A2 to A4
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
		gameType = appPrefs.EAR_TRAINING;
		gameNotes.clear();
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
				appMidi.midiChannel.noteOff(gameNotes.get(i).pitch, 0);
			gameNotes.clear();
			gameType = appPrefs.GAME_STOPPED;
		}
	}
	
	private void pianoKeyPressed(Key k, boolean pressed)
	{
		System.out.println("[pianoKeyPressed] pitch = " + k.pitch);
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
	}
	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		sBar.setSize(getWidth(), sBarHeight);
		gameTitle.setBounds(0, sBarHeight, getWidth(), 30);
		noteContainer.setBounds((getWidth() / 2) - 200, (getHeight() / 2 - 90), 400, 160);
		buttonsContainer.setBounds((getWidth() / 2) - 300, getHeight() - gBarHeight - 120, 600, 80);
		gameBar.setBounds(0, getHeight() - gBarHeight, getWidth(), gBarHeight);
	}
	
	private class EarTrainingGameThread extends Thread 
	{
		public void run() 
		{
			while (gameStarted) 
			{
				try
				{
					
				}
				catch (Exception e) {  }
			}
		}
	}
}
