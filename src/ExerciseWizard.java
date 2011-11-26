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
import javax.swing.event.ChangeListener;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;

public class ExerciseWizard extends JDialog
{
	private static final long serialVersionUID = 2656910435412692590L;
	ResourceBundle appBundle;
	Preferences appPrefs;
	Font appFont;
	
	JPanel backPanel;
	JLabel gameTitle;
	public RoundedButton inlineExBtn, rhythmExBtn, scoreExBtn;

	public ExerciseWizard(ResourceBundle b, Preferences p, Font f)
	{
		appBundle = b;
		appPrefs = p;
		appFont = f;

        setSize(600, 270);
		String title = appBundle.getString("_exWizard") + " 1/3";
		setTitle(title);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window on the display
        setLayout(null);
        
        backPanel = new JPanel();
        backPanel.setLayout(null);
        backPanel.setBackground(Color.white);
        backPanel.setBounds(0, 0, 600, 270);
        
        gameTitle = new JLabel(appBundle.getString("_exType"), null, JLabel.CENTER);
        gameTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gameTitle.setBounds(10, 5, 580, 30);
        
        int btnWidth = 184;
        int btnHeight = 180;
        int tmpXpos = 10;

        inlineExBtn = new RoundedButton("RBL_INLINE", appBundle, Color.decode("0xA2DDFF"));
        inlineExBtn.setFont(appFont);
        inlineExBtn.setFontSize(16);
        inlineExBtn.setBackground(Color.decode("0xA1C5FF"));
        inlineExBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        inlineExBtn.setBounds(tmpXpos, 45, btnWidth, 180);
        tmpXpos+=btnWidth+10;
        rhythmExBtn = new RoundedButton("RBL_RHYTHM", appBundle, Color.decode("0xA2DDFF"));
        rhythmExBtn.setFont(appFont);
        rhythmExBtn.setFontSize(16);
        rhythmExBtn.setBackground(Color.decode("0xA1C5FF"));
        rhythmExBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
        rhythmExBtn.setBounds(tmpXpos, 45, btnWidth, 180);
        tmpXpos+=btnWidth+10;
        scoreExBtn = new RoundedButton("RBL_SCORE", appBundle, Color.decode("0xA2DDFF"));
	    scoreExBtn.setFont(appFont);
	    scoreExBtn.setFontSize(16);
	    scoreExBtn.setBackground(Color.decode("0xA1C5FF"));
	    scoreExBtn.setPreferredSize(new Dimension(btnWidth, btnHeight));
	    scoreExBtn.setBounds(tmpXpos, 45, btnWidth, 180);
	    
	    backPanel.add(gameTitle);
	    backPanel.add(inlineExBtn);
	    backPanel.add(rhythmExBtn);
	    backPanel.add(scoreExBtn);
	    
	    add(backPanel);
	}
}

class ExerciseScoreWizard extends JDialog implements ActionListener, ChangeListener
{
	private static final long serialVersionUID = 4264234519336822654L;
	ResourceBundle appBundle;
	Preferences appPrefs;
	Font appFont;
	
	Exercise currExercise;
	
	JTextField titleTextField;
	JComboBox accCB;
	JRadioButton trebleClefCB, bassClefCB, altoClefCB, tenorClefCB;

	JRadioButton fourfourButton, twofourButton, threefourButton, sixeightButton;
	
	JLabel tempoLabel;
	JSlider tempoSlider;
	
	JRadioButton randYes, randNo;
	
	RoundedButton nextButton;
	
	boolean clefsActive = false;
	boolean accidentalsActive = false;
	boolean meausersActive = false;

	public ExerciseScoreWizard(ResourceBundle b, Preferences p, Font f, Exercise e)
	{
		appBundle = b;
		appPrefs = p;
		appFont = f;
		currExercise = e;

		String title = appBundle.getString("_exWizard") + " 2/3";
		setTitle(title);
        setLayout(null);
        
        JPanel backPanel = new JPanel();
        backPanel.setLayout(null);
        backPanel.setBackground(Color.white);
        
        int tmpYpos = 5; 
        // ************************ TITLE PANEL ******************************
        RoundPanel titlePanel = new RoundPanel(Color.decode("0xFFFFFF"), Color.decode("0xA2DDFF"));
        titlePanel.setLayout(null);
        titlePanel.setBackground(Color.white);
        titlePanel.setBounds(5, tmpYpos, 585, 90);
        
        JLabel titleLabel = new JLabel(appBundle.getString("_exTitle") + "  ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(10, 10, 570, 25);
        
        titleTextField = new JTextField("");
        titleTextField.setFont(new Font("Arial", Font.BOLD, 20));
        titleTextField.setBounds(10, 40, 565, 40);
        
        titlePanel.add(titleLabel);
        titlePanel.add(titleTextField);

        backPanel.add(titlePanel);
        
        tmpYpos += 95;
        // ************************ CLEFS PANEL ******************************
        if (currExercise.type != 1)
        {
        	//RoundPanel clefsPanel = new RoundPanel(Color.decode("0xA3C7FF"), Color.decode("0xA2DDFF"));
        	RoundPanel clefsPanel = new RoundPanel(Color.decode("0xFFFFFF"), Color.decode("0xA2DDFF"));
        	clefsPanel.setLayout(null);
        	clefsPanel.setBackground(Color.white);
        	clefsPanel.setBounds(5, tmpYpos, 585, 90);
        
        	JLabel clefLabel = new JLabel(appBundle.getString("_menuClef") + "  ");
        	clefLabel.setFont(new Font("Arial", Font.BOLD, 20));
        	clefLabel.setBounds(10, 30, 570, 25);
        
        	ButtonGroup clefsGroup = new ButtonGroup();
        	trebleClefCB = new JRadioButton("G");
        	trebleClefCB.setFont(appFont.deriveFont(50f));
        	trebleClefCB.setBounds(270, 0, 70, 80);
        	trebleClefCB.setSelected(true);
        	bassClefCB = new JRadioButton("?");
	        bassClefCB.setFont(appFont.deriveFont(50f));
	        bassClefCB.setBounds(340, 0, 70, 80);
	        altoClefCB = new JRadioButton("" + (char)0xBF);
	        altoClefCB.setFont(appFont.deriveFont(50f));
	        altoClefCB.setBounds(410, 0, 70, 80);
	        tenorClefCB = new JRadioButton("" + (char)0xBF);
	        tenorClefCB.setFont(appFont.deriveFont(50f));
	        tenorClefCB.setBounds(480, -5, 70, 80);
			
			clefsGroup.add(trebleClefCB);
			clefsGroup.add(bassClefCB);
			clefsGroup.add(altoClefCB);
			clefsGroup.add(tenorClefCB);
			
			clefsPanel.add(clefLabel);
			clefsPanel.add(trebleClefCB);
			clefsPanel.add(bassClefCB);
			clefsPanel.add(altoClefCB);
			clefsPanel.add(tenorClefCB);
	
	        tmpYpos += 95;
	        clefsActive = true;
        
	        // ******************** ACCIDENTALS PANEL ****************************
	        RoundPanel accidentalsPanel = new RoundPanel(Color.decode("0xFFFFFF"), Color.decode("0xA2DDFF"));
			accidentalsPanel.setLayout(null);
			accidentalsPanel.setBackground(Color.white);
			accidentalsPanel.setBounds(5, tmpYpos, 585, 40);
	
			JLabel accLabel = new JLabel(appBundle.getString("_accidentals") + "  ");
			accLabel.setFont(new Font("Arial", Font.BOLD, 20));
			accLabel.setBounds(10, 7, 300, 25);
			
			accCB = new JComboBox();
			accCB.setBounds(270, 5, 260, 27);
			accCB.addItem(appBundle.getString("_nosharpflat"));
			accCB.addItem("1 "+ appBundle.getString("_sharp"));
			accCB.addItem("2 "+ appBundle.getString("_sharp"));
			accCB.addItem("3 "+ appBundle.getString("_sharp"));
			accCB.addItem("4 "+ appBundle.getString("_sharp"));
			accCB.addItem("5 "+ appBundle.getString("_sharp"));
			accCB.addItem("6 "+ appBundle.getString("_sharp"));
			accCB.addItem("7 "+ appBundle.getString("_sharp"));
			accCB.addItem("1 "+ appBundle.getString("_flat"));
			accCB.addItem("2 "+ appBundle.getString("_flat"));
			accCB.addItem("3 "+ appBundle.getString("_flat"));
			accCB.addItem("4 "+ appBundle.getString("_flat"));
			accCB.addItem("5 "+ appBundle.getString("_flat"));
			accCB.addItem("6 "+ appBundle.getString("_flat"));
			accCB.addItem("7 "+ appBundle.getString("_flat"));
			
			accidentalsPanel.add(accLabel);
			accidentalsPanel.add(accCB);
			
			tmpYpos+=45;
			accidentalsActive = true;
			backPanel.add(clefsPanel);
			backPanel.add(accidentalsPanel);
        }
		// ******************** MEASURE PANEL ****************************
        if (currExercise.type != 0)
        {
			RoundPanel tsPanel = new RoundPanel(Color.decode("0xFFFFFF"), Color.decode("0xA2DDFF"));
			tsPanel.setLayout(null);
			tsPanel.setBackground(Color.white);
			tsPanel.setBounds(5, tmpYpos, 585, 65);
			
			JLabel timeSignLabel = new JLabel(appBundle.getString("_timeSignature") + "  ");
			timeSignLabel.setFont(new Font("Arial", Font.BOLD, 22));
			timeSignLabel.setBounds(10, 15, 300, 30);
			ButtonGroup rbGroup = new ButtonGroup();
			
			fourfourButton = new JRadioButton("$"); // 4/4 symbol
			fourfourButton.setFont(appFont.deriveFont(50f));
			fourfourButton.setBounds(270, 0, 70, 60);
			fourfourButton.setSelected(true);
			twofourButton = new JRadioButton("@"); // 2/4 symbol
			twofourButton.setFont(appFont.deriveFont(50f));
			twofourButton.setBounds(340, 0, 70, 60);
			threefourButton = new JRadioButton("#"); // 3/4 symbol
			threefourButton.setFont(appFont.deriveFont(50f));
			threefourButton.setBounds(410, 0, 70, 60);
			sixeightButton = new JRadioButton("P"); // 6/8 symbol
			sixeightButton.setFont(appFont.deriveFont(50f));
			sixeightButton.setBounds(480, 0, 70, 60);
			
			rbGroup.add(fourfourButton);
			rbGroup.add(twofourButton);
			rbGroup.add(threefourButton);
			rbGroup.add(sixeightButton);
			
			tsPanel.add(timeSignLabel);
			tsPanel.add(fourfourButton);
			tsPanel.add(twofourButton);
			tsPanel.add(threefourButton);
			tsPanel.add(sixeightButton);
			meausersActive = true;
        
			backPanel.add(tsPanel);
			tmpYpos+=75;
        }
        
        RoundPanel speedPanel = new RoundPanel(Color.decode("0xFFFFFF"), Color.decode("0xA2DDFF"));
        speedPanel.setLayout(null);
        speedPanel.setBackground(Color.white);
        speedPanel.setBounds(5, tmpYpos, 585, 50);
        
        if (currExercise.type == 0)
		{
			tempoSlider = new JSlider(JSlider.HORIZONTAL, 30, 200, 60);
			tempoLabel = new JLabel(appBundle.getString("_speed") + ": 60");
		}
		else
		{
			tempoSlider = new JSlider(JSlider.HORIZONTAL, 40, 200, 80);
			tempoLabel = new JLabel(appBundle.getString("_speed") + ": 80 BPM");
		}

     	tempoLabel.setFont(new Font("Arial", Font.BOLD, 22));
     	tempoLabel.setBounds(10, 10, 300, 30);
        
     	tempoSlider.setBounds(270, 10, 260, 30);
     	tempoSlider.addChangeListener(this);
     	
        speedPanel.add(tempoLabel);
        speedPanel.add(tempoSlider);
        backPanel.add(speedPanel);
        tmpYpos+=60;

        if (currExercise.type == 0)
        {
			RoundPanel randomPanel = new RoundPanel(Color.decode("0xFFFFFF"), Color.decode("0xA2DDFF"));
			randomPanel.setLayout(null);
			randomPanel.setBackground(Color.white);
			randomPanel.setBounds(5, tmpYpos, 585, 50);
			
			JLabel randomLabel = new JLabel(appBundle.getString("_exRandomize") + "  ");
			randomLabel.setFont(new Font("Arial", Font.BOLD, 22));
			randomLabel.setBounds(10, 10, 300, 30);
			ButtonGroup rbRandGroup = new ButtonGroup();
			
			randYes = new JRadioButton(appBundle.getString("_yes"));
			randYes.setFont(new Font("Arial", Font.BOLD, 20));
			randYes.setBounds(270, 0, 70, 50);
			randYes.setSelected(true);
			randNo = new JRadioButton(appBundle.getString("_no"));
			randNo.setFont(new Font("Arial", Font.BOLD, 20));
			randNo.setBounds(340, 0, 70, 50);
			randNo.setSelected(false);
			
			rbRandGroup.add(randYes);
			rbRandGroup.add(randNo);
			
			randomPanel.add(randomLabel);
			randomPanel.add(randYes);
			randomPanel.add(randNo);
			backPanel.add(randomPanel);
	        tmpYpos+=60;
        }

        nextButton = new RoundedButton(appBundle.getString("_exNext"), appBundle, Color.decode("0x0E9B20"));
        nextButton.setBackground(Color.decode("0x13DC2E"));
        nextButton.setFont(new Font("Arial", Font.BOLD, 20));
        nextButton.setBounds(400, tmpYpos, 190, 40);
        nextButton.addActionListener(this);
        
        backPanel.add(nextButton);
        tmpYpos+=40;
        
        backPanel.setBounds(0, 0, 600, tmpYpos + 45);

        setSize(600, tmpYpos + 45);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window on the display
        
        add(backPanel);
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == nextButton)
		{
			String exTitle = titleTextField.getText();
			if (exTitle.equals(""))
				exTitle = "Exercise" + System.currentTimeMillis();
			
			currExercise.setTitle(exTitle);
			if (currExercise.type != 1)
			{
				if (trebleClefCB.isSelected() == true)
					currExercise.setClefMask(appPrefs.TREBLE_CLEF);
				else if (bassClefCB.isSelected() == true)
					currExercise.setClefMask(appPrefs.BASS_CLEF);
				else if (altoClefCB.isSelected() == true)
					currExercise.setClefMask(appPrefs.ALTO_CLEF);
				else if (tenorClefCB.isSelected() == true)
					currExercise.setClefMask(appPrefs.TENOR_CLEF);

				int accIdx = accCB.getSelectedIndex();
				if (accIdx <= 0) currExercise.acc.setTypeAndCount("", 0);
				else if (accIdx < 8) currExercise.acc.setTypeAndCount("#", accIdx);
				else currExercise.acc.setTypeAndCount("b", accIdx - 7);
			}
			else
			{
				currExercise.setClefMask(appPrefs.TREBLE_CLEF);
			}
			
			if (currExercise.type != 0)
	        {
				if (fourfourButton.isSelected() == true)
					currExercise.setMeasure(0);
				else if (twofourButton.isSelected() == true)
					currExercise.setMeasure(1);
				else if (threefourButton.isSelected() == true)
					currExercise.setMeasure(2);
				else if (sixeightButton.isSelected() == true)
					currExercise.setMeasure(3);
	        }
			else
			{
				currExercise.setMeasure(0);
				if (randYes.isSelected() == true)
					currExercise.randomize = 1;
				else
					currExercise.randomize = 0;
			}
			
			currExercise.setSpeed(tempoSlider.getValue());
			this.firePropertyChange("gotoScoreEditor", false, true);
			this.dispose();
		}
	}
	
	public void stateChanged(ChangeEvent e) 
	{
		if (e.getSource() == tempoSlider)
		{
			if (currExercise.type != 0)
				tempoLabel.setText(appBundle.getString("_speed") + ": " + Integer.toString(tempoSlider.getValue()) + " BPM");
			else
				tempoLabel.setText(appBundle.getString("_speed") + ": " + Integer.toString(tempoSlider.getValue()));
		}
    }
}