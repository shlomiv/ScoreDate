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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;


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
	private JScrollPane treeScrollPanel;
	private JTree exercisesList;
	
	private Exercise newExercise;
	private ExerciseWizard exerciseTypeDialog;
	private ExerciseScoreWizard exerciseScoreSetupDialog;
	private ExerciseScoreEditor exerciseScoreEditorDialog;
	
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
		leftPanel.setBounds(5, 10, 330, d.height - 80);
		
		topBar = new RoundPanel(Color.decode("0xA3C7FF"), Color.decode("0xA2DDFF"));
		topBar.setBorderColor(Color.decode("0xA4D6FF"));
		topBar.setBounds(10, 7, 310, 67);
		topBar.setLayout(null);
		
		homeBtn = new RoundedButton("", appBundle);
		//homeBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
		homeBtn.setBounds(15, 5, 57, 57);
		homeBtn.setBackground(Color.decode("0x8FC6E9"));
		homeBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/home.png")).getImage());
		//homeBtn.setImagSize(32, 32);
		
		newExerciseBtn = new RoundedButton("", appBundle);
		//homeBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
		newExerciseBtn.setBounds(244, 5, 57, 57);
		newExerciseBtn.setBackground(Color.decode("0x8FC6E9"));
		newExerciseBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/new_exercise.png")).getImage());
		newExerciseBtn.addActionListener(this);
		
		topBar.add(homeBtn);
		topBar.add(newExerciseBtn);
		
		leftPanel.add(topBar);
		
		add(leftPanel);
	}
	
	private void showExerciseSetup(int type)
	{
		exerciseTypeDialog.dispose();
		newExercise.setType(type);
		exerciseScoreSetupDialog = new ExerciseScoreWizard(appBundle, appPrefs, appFont, newExercise);
		exerciseScoreSetupDialog.setVisible(true);
		exerciseScoreSetupDialog.addPropertyChangeListener(this);
	}
	
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
			System.out.println("----> refresh this panel <-----");
		}
	}
	
	public void valueChanged(TreeSelectionEvent e) 
	{
		//Returns the last path element of the selection.
		//This method is useful only when the selection model allows a single selection.
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		    		exercisesList.getLastSelectedPathComponent();

		    if (node == null) //Nothing is selected.     
		    	return;

		    if (node.getLevel() == 1)
		    {

		    }
	}
	
	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}
}
