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
import java.io.File;
import java.util.ResourceBundle;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequencer;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class ExercisesPanel extends JPanel implements TreeSelectionListener, ActionListener, PropertyChangeListener
{
	private static final long serialVersionUID = -1142716145008143198L;
	Font appFont;
	Preferences appPrefs;
	private ResourceBundle appBundle;
	private MidiController appMidi;
	
	private JPanel leftPanel;
	public RoundPanel topBar;
	public RoundedButton homeBtn;
	public RoundedButton newExerciseBtn;
	public RoundedButton listenBtn;
	private JScrollPane treeScrollPanel = null;
	private JTree exercisesList;
	
	private JLabel exerciseTitle;
	
	private JLayeredPane layers;
	private Staff scoreStaff;
	private NotesPanel notesLayer;
	
	public RoundedButton exLineBtn, exRhythmBtn, exScoreBtn;
	
	private Exercise newExercise;
	private Exercise selectedExercise;
	private ExerciseWizard exerciseTypeDialog;
	private ExerciseScoreWizard exerciseScoreSetupDialog;
	private ExerciseScoreEditor exerciseScoreEditorDialog;
	
	File exercisesDir; // the ScoreDate directory path
	
	int playbackSpeed = 80;
	int timeNumerator = 4;
    int timeDenominator = 4;

	private boolean isPlaying = false;
	private Sequencer playback;
	
	public ExercisesPanel(Font f, ResourceBundle b, Preferences p, MidiController mc, Dimension d)
	{
		appFont = f;
		appBundle = b;
		appPrefs = p;
		appMidi = mc;

		setBackground(Color.white);
		setSize(d);
		setLayout(null);

		leftPanel = new JPanel();
		leftPanel.setLayout(null);
		leftPanel.setBackground(Color.decode("0xCCF5FF"));
		Border defBorder = UIManager.getBorder(leftPanel);
		leftPanel.setBorder(BorderFactory.createTitledBorder(defBorder, "", TitledBorder.LEADING, TitledBorder.TOP));
		leftPanel.setBounds(5, 10, 330, getHeight() - 20);

		topBar = new RoundPanel(Color.decode("0xA3C7FF"), Color.decode("0xA2DDFF"));
		topBar.setBorderColor(Color.decode("0xA4D6FF"));
		topBar.setBounds(10, 7, 310, 67);
		topBar.setLayout(null);

		homeBtn = new RoundedButton("", appBundle);
		homeBtn.setBounds(13, 5, 57, 57);
		homeBtn.setBackground(Color.decode("0x8FC6E9"));
		homeBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/home.png")).getImage());
		//homeBtn.setImagSize(42, 42);

		newExerciseBtn = new RoundedButton("", appBundle);
		newExerciseBtn.setBounds(77, 5, 57, 57);
		newExerciseBtn.setBackground(Color.decode("0x8FC6E9"));
		newExerciseBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/new_exercise.png")).getImage());
		newExerciseBtn.addActionListener(this);

		listenBtn = new RoundedButton("", appBundle);
		listenBtn.setBounds(244, 5, 57, 57);
		listenBtn.setBackground(Color.decode("0x8FC6E9"));
		listenBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
		listenBtn.setEnabled(false);
		listenBtn.addActionListener(this);

		topBar.add(homeBtn);
		topBar.add(newExerciseBtn);
		topBar.add(listenBtn);

		leftPanel.add(topBar);

		exerciseTitle = new JLabel("", null, JLabel.LEFT);
		exerciseTitle.setFont(new Font("Arial", Font.BOLD, 20));
		exerciseTitle.setBounds(350, 10, getWidth() - 360, 30);

		int panelsWidth = getWidth() - 360;
		int panelsHeight = getHeight() - 140;
		int scaledWidth = (int)(panelsWidth * 1.66);
		int scaledHeight = (int)(panelsHeight * 1.66);

		layers = new JLayeredPane();
		layers.setPreferredSize(new Dimension(panelsWidth, panelsHeight));
		layers.setBounds(350, 40, panelsWidth, panelsHeight);

		scoreStaff = new Staff(appFont, appBundle, appPrefs, null, false, true);
		scoreStaff.setPreferredSize( new Dimension(scaledWidth, scaledHeight));
		scoreStaff.setBounds(0, 0, scaledWidth, scaledHeight);
		scoreStaff.setScale(0.6);
		scoreStaff.setMeasuresNumber(10);
		scoreStaff.setOpaque(true);

		notesLayer = new NotesPanel(appFont, appPrefs, null, false);
		notesLayer.setPreferredSize( new Dimension(scaledWidth, scaledHeight));
		notesLayer.setBounds(0, 0, scaledWidth, scaledHeight);
		notesLayer.setScale(0.6);
		notesLayer.setOpaque(false);

		layers.add(scoreStaff, new Integer(1));
		layers.add(notesLayer, new Integer(2));
		
		exLineBtn = new RoundedButton(appBundle.getString("_menuNotereading"), appBundle, Color.decode("0xA2DDFF"));
		exLineBtn.setBounds(230 + ((getWidth() - 330) / 2), getHeight() - 110, 200, 40);
		exLineBtn.setBackground(Color.decode("0xA1C5FF"));
		exLineBtn.setFont(new Font("Arial", Font.BOLD, 16));
		exLineBtn.setVisible(false);
		
		exRhythmBtn = new RoundedButton(appBundle.getString("_menuRythmreading"), appBundle, Color.decode("0xA2DDFF"));
		exRhythmBtn.setBounds(230 + ((getWidth() - 330) / 2), getHeight() - 110, 200, 40);
		exRhythmBtn.setBackground(Color.decode("0xA1C5FF"));
		exRhythmBtn.setFont(new Font("Arial", Font.BOLD, 16));
		exRhythmBtn.setVisible(false);
		
		exScoreBtn = new RoundedButton(appBundle.getString("_menuScorereading"), appBundle, Color.decode("0xA2DDFF"));
		exScoreBtn.setBounds(350 + ((getWidth() - 330) / 2), getHeight() - 110, 200, 40);
		exScoreBtn.setBackground(Color.decode("0xA1C5FF"));
		exScoreBtn.setFont(new Font("Arial", Font.BOLD, 16));
		exScoreBtn.setVisible(false);

		add(leftPanel);
		add(exerciseTitle);
		add(layers);
		
		add(exLineBtn);
		add(exRhythmBtn);
		add(exScoreBtn);
		
		updateTreeList();
	}
	
    private class ExNodeInfo 
    {
        public String exLabel;
        public String filePath;

        public ExNodeInfo(String lbl, String path) 
        {
        	exLabel = lbl;
        	filePath = path;
        }

        public String toString() {
            return exLabel;
        }
    }
	
	public void walk( String path, DefaultMutableTreeNode parentNode ) 
	{
        File root = new File( path );
        File[] list = root.listFiles();

        for ( File f : list ) 
        {
            if ( f.isDirectory() ) 
            {
            	DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(f.getName());
            	parentNode.add(dirNode);
                walk( f.getAbsolutePath(), dirNode );
                System.out.println( "Dir: " + f.getAbsolutePath() );
            }
            else 
            {
            	if (f.getAbsolutePath().endsWith(".xml"))
            	{
            		ExNodeInfo nInfo = new ExNodeInfo(f.getName().substring(0, f.getName().length() - 4), f.getAbsolutePath());
            		DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(nInfo);
            		dirNode.setUserObject(nInfo);
            		parentNode.add(dirNode);
            		System.out.println( "File: " + f.getAbsolutePath() );
            	}
            }
        }
    }

	private void updateTreeList()
	{
		exercisesDir = new File("Exercises");
		File EXdir = new File(exercisesDir.getAbsolutePath());
		File[] list = EXdir.listFiles();
		
		if (treeScrollPanel != null)
			leftPanel.remove(treeScrollPanel);
		
		if (list == null || list.length == 0)
		{
			DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(appBundle.getString("_exNotFound"));
			exercisesList = new JTree(mainNode);
		}
		else
		{
			DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode("Exercises");
			walk(EXdir.getAbsolutePath(), mainNode);
			exercisesList = new JTree(mainNode);
			exercisesList.setRootVisible(false);
			exercisesList.setShowsRootHandles(true);
			exercisesList.addTreeSelectionListener(this);
		}
		
		exercisesList.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		exercisesList.setBackground(Color.decode("0xCCF5FF"));
		exercisesList.setBounds(0, 0, 280, getHeight() - 180);
		
		treeScrollPanel = new JScrollPane(exercisesList);
		//Border border = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		//treeScrollPanel.setBorder(border);
		treeScrollPanel.setBounds(10, 80, 300, getHeight() - 170);
		leftPanel.add(treeScrollPanel);
	}
	
	private void showExerciseSetup(int type)
	{
		exerciseTypeDialog.dispose();
		newExercise.setType(type);
		exerciseScoreSetupDialog = new ExerciseScoreWizard(appBundle, appPrefs, appFont, newExercise);
		exerciseScoreSetupDialog.setVisible(true);
		exerciseScoreSetupDialog.addPropertyChangeListener(this);
	}
	
	public Exercise getSelectedExercise()
	{
		return selectedExercise;
	}

// **************************************************************************************************
// *                                             EVENTS                                             *
// **************************************************************************************************
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == newExerciseBtn)
		{
			newExercise = new Exercise(appPrefs);
			exerciseTypeDialog = new ExerciseWizard(appBundle, appPrefs, appFont);
			exerciseTypeDialog.setVisible(true);
			exerciseTypeDialog.inlineExBtn.addActionListener(this);
			exerciseTypeDialog.rhythmExBtn.addActionListener(this);
			exerciseTypeDialog.scoreExBtn.addActionListener(this);
		}
		if (exerciseTypeDialog != null)
		{
			if(ae.getSource() == exerciseTypeDialog.inlineExBtn)
			{
				showExerciseSetup(0);
			}
			else if(ae.getSource() == exerciseTypeDialog.rhythmExBtn)
			{
				showExerciseSetup(1);
			}
			else if(ae.getSource() == exerciseTypeDialog.scoreExBtn)
			{
				showExerciseSetup(2);
			}
		}
		if (ae.getSource() == listenBtn && selectedExercise != null)
		{
			if (isPlaying == false)
			{
				playback = appMidi.createPlayback(appPrefs, selectedExercise.speed, selectedExercise.notes, timeDenominator / 4, true, 0);
				playback.addMetaEventListener(new MetaEventListener() {
			          public void meta(MetaMessage meta) 
			          {
			        	  byte[] metaData = meta.getData();
			              String strData = new String(metaData);
			        	  if ("end".equals(strData))
			              {
			        		appMidi.stopPlayback();
			  				listenBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
			  				listenBtn.repaint();
			  				isPlaying = false;
			              }
			          }
					});
				listenBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/stop.png")).getImage());
				playback.start();
				isPlaying = true;
			}
			else
			{
				appMidi.stopPlayback();
				listenBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
				listenBtn.repaint();
				isPlaying = false;
			}
		}
	}
	
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName() == "gotoScoreEditor")
		{
			exerciseScoreEditorDialog = new ExerciseScoreEditor(appBundle, appPrefs, appFont, appMidi, newExercise);
			exerciseScoreEditorDialog.setVisible(true);
			exerciseScoreEditorDialog.addPropertyChangeListener(this);
		}
		else if (evt.getPropertyName() == "exerciseSaved")
		{
			updateTreeList();
		}
	}
	
	public void valueChanged(TreeSelectionEvent e) 
	{
		//Returns the last path element of the selection.
	    DefaultMutableTreeNode selNode = (DefaultMutableTreeNode)exercisesList.getLastSelectedPathComponent();
	    
	    if (selNode == null || selNode.isLeaf() == false)
	    {
	    	listenBtn.setEnabled(false);
	    	return;
	    }

	    ExNodeInfo nInfo = (ExNodeInfo)selNode.getUserObject();
        System.out.println("Tree node clicked. Absolute path: " + nInfo.filePath);
        selectedExercise = new Exercise(appPrefs);
        selectedExercise.loadFromFile(nInfo.filePath);
        
        exerciseTitle.setText(selectedExercise.title);
        scoreStaff.setClef(selectedExercise.clefMask);
        scoreStaff.setAccidentals(selectedExercise.acc);
        timeNumerator = 4;
        timeDenominator = 4;
        if (selectedExercise.timeSign == 1)
        	timeNumerator = 2;
        else if (selectedExercise.timeSign == 2)
        	timeNumerator = 3;
        else if (selectedExercise.timeSign == 3)
        	{ timeNumerator = 6; timeDenominator = 8; }
        scoreStaff.setTimeSignature(timeNumerator, timeDenominator);
        double totalDuration = selectedExercise.notes.get(selectedExercise.notes.size() - 1).timestamp + selectedExercise.notes.get(selectedExercise.notes.size() - 1).duration;
        scoreStaff.setMeasuresNumber((int)Math.ceil(totalDuration / timeNumerator));
        playbackSpeed = selectedExercise.speed;
        
        notesLayer.setClef(selectedExercise.clefMask);
        notesLayer.setStaffWidth(scoreStaff.getStaffWidth());
        notesLayer.setFirstNoteXPosition(scoreStaff.getFirstNoteXPosition());
        notesLayer.setNotesSequence(selectedExercise.notes);
        notesLayer.setNotesPositions();
        listenBtn.setEnabled(true);

        exLineBtn.setVisible(false);
        exRhythmBtn.setVisible(false);
        exScoreBtn.setVisible(false);

        if (selectedExercise.type == 0)
        {
        	exLineBtn.setBounds(exRhythmBtn.getX(), exRhythmBtn.getY(), 200, 40);
        	exLineBtn.setVisible(true);
        }
        else if (selectedExercise.type == 1)
        	exRhythmBtn.setVisible(true);
        else if (selectedExercise.type == 2)
        {
        	exScoreBtn.setVisible(true);
        	exLineBtn.setBounds(exRhythmBtn.getX() - 120, exRhythmBtn.getY(), 200, 40);
        	exLineBtn.setVisible(true);
        }
	}
	
	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		leftPanel.setBounds(5, 10, 330, getHeight() - 20);
		exercisesList.setBounds(0, 0, 290, getHeight() - 120);
		treeScrollPanel.setBounds(10, 80, 310, getHeight() - 110);
		
		exerciseTitle.setBounds(350, 10, getWidth() - 360, 30);
		int panelsWidth = getWidth() - 360;
		int panelsHeight = getHeight() - 120;
		int scaledWidth = (int)(panelsWidth * 1.66);
		int scaledHeight = (int)(panelsHeight * 1.66);

		layers.setBounds(350, 40, panelsWidth, panelsHeight);
		scoreStaff.setBounds(0, 0, scaledWidth, scaledHeight);
		notesLayer.setBounds(0, 0, scaledWidth, scaledHeight);
		notesLayer.setStaffWidth(scoreStaff.getStaffWidth());
		notesLayer.setNotesPositions();
		
		if (selectedExercise != null && selectedExercise.type == 0)
			exLineBtn.setBounds(230 + ((getWidth() - 330) / 2), getHeight() - 55, 200, 40);
		else
			exLineBtn.setBounds(130 + ((getWidth() - 330) / 2), getHeight() - 55, 200, 40);
		exRhythmBtn.setBounds(230 + ((getWidth() - 330) / 2), getHeight() - 55, 200, 40);
		exScoreBtn.setBounds(350 + ((getWidth() - 330) / 2), getHeight() - 55, 200, 40);
	}
}
