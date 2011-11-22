/***********************************************
This file is part of the ScoreDate project (http://www.mindmatter.it/scoredate/).

ScoreDate is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

NRTB is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ScoreDate.  If not, see <http://www.gnu.org/licenses/>.

**********************************************/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ClefNotesOptionDialog extends JDialog implements ActionListener
{
	private static final long serialVersionUID = -2654540587350157146L;
	ResourceBundle appBundle;
	Font appFont;
	Preferences appPrefs;
	
	ClefSelector trebleClef;
	ClefSelector bassClef;
	ClefSelector altoClef;
	ClefSelector tenorClef;
	
	JComboBox accCB;
	
	JCheckBox wholeCB;
	JCheckBox halfCB;
	JCheckBox quarterCB;
	JCheckBox eighthCB;
	JCheckBox tripletCB;
	JCheckBox silenceCB;
	
	JRadioButton fourfourButton;
	JRadioButton twofourButton;
	JRadioButton threefourButton;
	JRadioButton sixeightButton;

	JButton okButton;
    JButton cancelButton;
	
	public ClefNotesOptionDialog(Font f, ResourceBundle b, Preferences p)
	{
		appFont = f;
		appBundle = b;
		appPrefs = p;

		int clefSelHeight = 205;
		int clefSelWidth = 170;

		setLayout(null);
        setSize(700, 510);
		String title = appBundle.getString("_menuClef") + " & " + appBundle.getString("_menuNotes");
		setTitle(title);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window on the display

        /*
         * ***** First panel: Contains the ClefSelector objects to select clefs ******
         */
		JPanel clefsPanel = new JPanel();
		clefsPanel.setLayout(null);
		clefsPanel.setBackground(Color.white);
		clefsPanel.setPreferredSize(new Dimension((clefSelWidth * 2) + 15, (clefSelHeight * 2) + 20));
		clefsPanel.setBounds(0, 0, (clefSelWidth * 2) + 15, (clefSelHeight * 2) + 20);
		
		int clefsMask = Integer.parseInt(appPrefs.getProperty("clefsMask")); 
		if (clefsMask == -1) clefsMask = appPrefs.TREBLE_CLEF;

		trebleClef = new ClefSelector(appBundle, "G");
		trebleClef.setPreferredSize(new Dimension(clefSelWidth, clefSelHeight));
		trebleClef.setBounds(5, 10, clefSelWidth, clefSelHeight);
		trebleClef.setFont(appFont);
		if ((clefsMask & appPrefs.TREBLE_CLEF) > 0)
			trebleClef.setEnabled(true);
		else
			trebleClef.setEnabled(false);
		
		NoteGenerator tmpNG = new NoteGenerator(appPrefs, null, false);
		// retrieve previously saved pitches and convert them into levels 
		int lowerPitch = Integer.parseInt(appPrefs.getProperty("trebleClefLower"));
		if (lowerPitch == -1) lowerPitch = 64; // default, set to E3
		int higherPitch = Integer.parseInt(appPrefs.getProperty("trebleClefUpper"));
		if (higherPitch == -1) higherPitch = 77; // default, set to F4
		System.out.println("Treble Clef pitches: " + lowerPitch + " to " + higherPitch);
		trebleClef.setLevels(20 - tmpNG.getIndexFromPitch(tmpNG.TREBLE_CLEF_BASEPITCH, lowerPitch, false), 
							 20 - tmpNG.getIndexFromPitch(tmpNG.TREBLE_CLEF_BASEPITCH, higherPitch, false));
		
		bassClef = new ClefSelector(appBundle, "?");
		bassClef.setPreferredSize(new Dimension(clefSelWidth, clefSelHeight));
		bassClef.setBounds(clefSelWidth + 10, 10, clefSelWidth, clefSelHeight);
		bassClef.setFont(appFont);
		if ((clefsMask & appPrefs.BASS_CLEF) > 0)
			bassClef.setEnabled(true);
		else
			bassClef.setEnabled(false);
		// retrieve previously saved pitches and convert them into levels 
		lowerPitch = Integer.parseInt(appPrefs.getProperty("bassClefLower"));
		if (lowerPitch == -1) lowerPitch = 43; // default, set to G1
		higherPitch = Integer.parseInt(appPrefs.getProperty("bassClefUpper"));
		if (higherPitch == -1) higherPitch = 57; // default, set to A2
		System.out.println("Bass Clef pitches: " + lowerPitch + " to " + higherPitch);
		bassClef.setLevels(20 - tmpNG.getIndexFromPitch(tmpNG.BASS_CLEF_BASEPITCH, lowerPitch, false), 
						   20 - tmpNG.getIndexFromPitch(tmpNG.BASS_CLEF_BASEPITCH, higherPitch, false));

		altoClef = new ClefSelector(appBundle, "ALTO");
		altoClef.setPreferredSize(new Dimension(clefSelWidth, clefSelHeight));
		altoClef.setBounds(5, clefSelHeight + 15, clefSelWidth, clefSelHeight);
		altoClef.setFont(appFont);
		if ((clefsMask & appPrefs.ALTO_CLEF) > 0)
			altoClef.setEnabled(true);
		else
			altoClef.setEnabled(false);
		// retrieve previously saved pitches and convert them into levels 
		lowerPitch = Integer.parseInt(appPrefs.getProperty("altoClefLower"));
		if (lowerPitch == -1) lowerPitch = 53; // default, set to F2
		higherPitch = Integer.parseInt(appPrefs.getProperty("altoClefUpper"));
		if (higherPitch == -1) higherPitch = 67; // default, set to G3
		System.out.println("Alto Clef pitches: " + lowerPitch + " to " + higherPitch);
		altoClef.setLevels(20 - tmpNG.getIndexFromPitch(tmpNG.ALTO_CLEF_BASEPITCH, lowerPitch, false), 
						   20 - tmpNG.getIndexFromPitch(tmpNG.ALTO_CLEF_BASEPITCH, higherPitch, false));
		
		tenorClef = new ClefSelector(appBundle, "TENOR");
		tenorClef.setPreferredSize(new Dimension(clefSelWidth, clefSelHeight));
		tenorClef.setBounds(clefSelWidth + 10, clefSelHeight + 15, clefSelWidth, clefSelHeight);
		tenorClef.setFont(appFont);
		if ((clefsMask & appPrefs.TENOR_CLEF) > 0)
			tenorClef.setEnabled(true);
		else
			tenorClef.setEnabled(false);
		// retrieve previously saved pitches and convert them into levels 
		lowerPitch = Integer.parseInt(appPrefs.getProperty("tenorClefLower"));
		if (lowerPitch == -1) lowerPitch = 50; // default, set to D2
		higherPitch = Integer.parseInt(appPrefs.getProperty("tenorClefUpper"));
		if (higherPitch == -1) higherPitch = 64; // default, set to E3
		System.out.println("Tenor Clef pitches: " + lowerPitch + " to " + higherPitch);
		tenorClef.setLevels(20 - tmpNG.getIndexFromPitch(tmpNG.TENOR_CLEF_BASEPITCH, lowerPitch, false), 
							20 - tmpNG.getIndexFromPitch(tmpNG.TENOR_CLEF_BASEPITCH, higherPitch, false));
		
		clefsPanel.add(trebleClef);
		clefsPanel.add(bassClef);
		clefsPanel.add(altoClef);
		clefsPanel.add(tenorClef);		

		 /*
		  * ***** Second panel: contains accidentals, notes type and time signature selections  ******
		  */
		JPanel notesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		notesPanel.setBackground(Color.white);
		notesPanel.setBounds((clefSelWidth * 2) + 15, 0, getWidth() - (clefSelWidth * 2) - 20, getHeight() - 75);
		notesPanel.setPreferredSize(new Dimension(getWidth() - (clefSelWidth * 2) - 20, getHeight() - 95));

		// ****** Sub panel of notesPanel. Contains accidental selection (label + combobox)
		RoundPanel accidentalsPanel = new RoundPanel();
		accidentalsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));
		accidentalsPanel.setBackground(Color.white);
		accidentalsPanel.setPreferredSize(new Dimension(getWidth() - (clefSelWidth * 2) - 40, 50));

		JLabel accLabel = new JLabel(appBundle.getString("_accidentals") + "  ");
		accLabel.setFont(new Font("Arial", Font.BOLD, 20));
		accidentalsPanel.add(accLabel);

		accCB = new JComboBox();
		accCB.setPreferredSize(new Dimension(150, 27));
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
		//accCB.addItem(appBundle.getString("_random")); // TODO: implement random accidentals
		accidentalsPanel.add(accCB);
		int accIdx = Integer.parseInt(appPrefs.getProperty("accidentals"));
		if (accIdx == -1)
			accCB.setSelectedIndex(0);
		else
			accCB.setSelectedIndex(accIdx);

		/*
		 * ***** Sub panel of notesPanel. Contains notes type selection (label + checkboxes)
		 */
		RoundPanel notesTypePanel = new RoundPanel();
		notesTypePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
		notesTypePanel.setBackground(Color.white);
		notesTypePanel.setPreferredSize(new Dimension(getWidth() - (clefSelWidth * 2) - 40, 220));

		JLabel nTypesLabel = new JLabel(appBundle.getString("_menuNotes") + "  ");
		nTypesLabel.setFont(new Font("Arial", Font.BOLD, 22));
		nTypesLabel.setPreferredSize(new Dimension(getWidth() - (clefSelWidth * 2) - 60, 40));

		wholeCB = new JCheckBox("w");
		wholeCB.setFont(appFont.deriveFont(50f));
		int noteOn = Integer.parseInt(appPrefs.getProperty("wholeNote")); 
		if (noteOn == 1 || noteOn == -1)
			wholeCB.setSelected(true);
		halfCB = new JCheckBox("h");
		halfCB.setFont(appFont.deriveFont(50f));
		if (Integer.parseInt(appPrefs.getProperty("halfNote")) == 1)
			halfCB.setSelected(true);
		quarterCB = new JCheckBox("q");
		quarterCB.setFont(appFont.deriveFont(50f));
		if (Integer.parseInt(appPrefs.getProperty("quarterNote")) == 1)
			quarterCB.setSelected(true);
		eighthCB = new JCheckBox("" + (char)0xC8);
		eighthCB.setFont(appFont.deriveFont(50f));
		if (Integer.parseInt(appPrefs.getProperty("eighthNote")) == 1)
			eighthCB.setSelected(true);
		tripletCB = new JCheckBox("T");
		tripletCB.setFont(appFont.deriveFont(50f));
		if (Integer.parseInt(appPrefs.getProperty("tripletNote")) == 1)
			tripletCB.setSelected(true);
		silenceCB = new JCheckBox("H");
		silenceCB.setFont(appFont.deriveFont(50f));
		noteOn = Integer.parseInt(appPrefs.getProperty("silenceNote")); 
		if (noteOn == 1 || noteOn == -1)
			silenceCB.setSelected(true);

		notesTypePanel.add(nTypesLabel);
		notesTypePanel.add(wholeCB);
		notesTypePanel.add(halfCB);
		notesTypePanel.add(quarterCB);
		notesTypePanel.add(eighthCB);
		notesTypePanel.add(tripletCB);
		notesTypePanel.add(silenceCB);

		/*
		 * ***** Sub panel of notesPanel. Contains time signature selection (label + group of radio buttons)
		 */
		RoundPanel tsPanel = new RoundPanel();
		tsPanel.setBackground(Color.white);
		tsPanel.setPreferredSize(new Dimension(getWidth() - (clefSelWidth * 2) - 40, 125));
		
		JLabel timeSignLabel = new JLabel(appBundle.getString("_timeSignature") + "  ");
		timeSignLabel.setFont(new Font("Arial", Font.BOLD, 22));
		timeSignLabel.setPreferredSize(new Dimension(getWidth() - (clefSelWidth * 2) - 60, 40));
		ButtonGroup rbGroup = new ButtonGroup();
		
		fourfourButton = new JRadioButton("$"); // 4/4 symbol
		fourfourButton.setFont(appFont.deriveFont(45f));
		twofourButton = new JRadioButton("@"); // 2/4 symbol
		twofourButton.setFont(appFont.deriveFont(45f));
		threefourButton = new JRadioButton("#"); // 3/4 symbol
		threefourButton.setFont(appFont.deriveFont(45f));
		sixeightButton = new JRadioButton("P"); // 6/8 symbol
		sixeightButton.setFont(appFont.deriveFont(45f));
		int tsIdx = Integer.parseInt(appPrefs.getProperty("timeSignature"));
		if (tsIdx == 0 || tsIdx == -1)
			fourfourButton.setSelected(true);
		else if (tsIdx == 1)
			twofourButton.setSelected(true);
		else if (tsIdx == 2)
			threefourButton.setSelected(true);
		else if (tsIdx == 3)
			sixeightButton.setSelected(true);
		rbGroup.add(fourfourButton);
		rbGroup.add(twofourButton);
		rbGroup.add(threefourButton);
		rbGroup.add(sixeightButton);
		
		tsPanel.add(timeSignLabel);
		tsPanel.add(fourfourButton);
		tsPanel.add(twofourButton);
		tsPanel.add(threefourButton);
		tsPanel.add(sixeightButton);

		notesPanel.add(accidentalsPanel);
		notesPanel.add(notesTypePanel);
		notesPanel.add(tsPanel);
		
		 /*
		  * ***** Third panel: contains OK and Cancel buttons  ******
		  */
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setPreferredSize(new Dimension(getWidth(), 50));
        buttonsPanel.setBounds(0, (clefSelHeight * 2) + 20, getWidth(), 50);

		okButton = new JButton(appBundle.getString("_buttonok"));
        okButton.setIcon(new ImageIcon(getClass().getResource("/resources/correct.png")));
        okButton.addActionListener(this);

        cancelButton = new JButton(appBundle.getString("_buttoncancel"));
        cancelButton.setIcon(new ImageIcon(getClass().getResource("/resources/wrong.png")));
        cancelButton.addActionListener(this);
        
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setBackground(Color.white);
        
		add(clefsPanel);
		add(notesPanel);
		add(buttonsPanel);
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == okButton)
		{
			int clefsMask = 0;
			NoteGenerator tmpNG = new NoteGenerator(appPrefs, null, false);
			if (trebleClef.isEnabled() == true)
			{
				int lowerPitch = tmpNG.getPitchFromLevel(tmpNG.TREBLE_CLEF_BASEPITCH, 20 - trebleClef.getLowerLevel());
				int higherPitch = tmpNG.getPitchFromLevel(tmpNG.TREBLE_CLEF_BASEPITCH, 20 - trebleClef.getHigherLevel());
				System.out.println("Treble Clef pitches: " + lowerPitch + " to " + higherPitch);
				clefsMask = clefsMask | appPrefs.TREBLE_CLEF;
				appPrefs.setProperty("trebleClefUpper", Integer.toString(higherPitch));
				appPrefs.setProperty("trebleClefLower", Integer.toString(lowerPitch));
			}
			if (bassClef.isEnabled() == true)
			{
				int lowerPitch = tmpNG.getPitchFromLevel(tmpNG.BASS_CLEF_BASEPITCH, 20 - bassClef.getLowerLevel());
				int higherPitch = tmpNG.getPitchFromLevel(tmpNG.BASS_CLEF_BASEPITCH, 20 - bassClef.getHigherLevel());
				System.out.println("Bass Clef pitches: " + lowerPitch + " to " + higherPitch);
				clefsMask = clefsMask | appPrefs.BASS_CLEF;
				appPrefs.setProperty("bassClefUpper", Integer.toString(higherPitch));
				appPrefs.setProperty("bassClefLower", Integer.toString(lowerPitch));
			}
			if (altoClef.isEnabled() == true)
			{
				int lowerPitch = tmpNG.getPitchFromLevel(tmpNG.ALTO_CLEF_BASEPITCH, 20 - altoClef.getLowerLevel());
				int higherPitch = tmpNG.getPitchFromLevel(tmpNG.ALTO_CLEF_BASEPITCH, 20 - altoClef.getHigherLevel());
				System.out.println("Alto Clef pitches: " + lowerPitch + " to " + higherPitch);
				clefsMask = clefsMask | appPrefs.ALTO_CLEF;
				appPrefs.setProperty("altoClefUpper", Integer.toString(higherPitch));
				appPrefs.setProperty("altoClefLower", Integer.toString(lowerPitch));
			}
			if (tenorClef.isEnabled() == true)
			{
				int lowerPitch = tmpNG.getPitchFromLevel(tmpNG.TENOR_CLEF_BASEPITCH, 20 - tenorClef.getLowerLevel());
				int higherPitch = tmpNG.getPitchFromLevel(tmpNG.TENOR_CLEF_BASEPITCH, 20 - tenorClef.getHigherLevel());
				System.out.println("Tenor Clef pitches: " + lowerPitch + " to " + higherPitch);
				clefsMask = clefsMask | appPrefs.TENOR_CLEF;
				appPrefs.setProperty("tenorClefUpper", Integer.toString(higherPitch));
				appPrefs.setProperty("tenorClefLower", Integer.toString(lowerPitch));
			}
			
			if (clefsMask == 0) // if all clefs are disabled then set TREBLE clef by default
				appPrefs.setProperty("clefsMask", "1");
			else
				appPrefs.setProperty("clefsMask", Integer.toString(clefsMask));
			
			appPrefs.setProperty("accidentals", Integer.toString(accCB.getSelectedIndex()));
			
			if (wholeCB.isSelected() == true) appPrefs.setProperty("wholeNote", "1");
			else appPrefs.setProperty("wholeNote", "0");
			if (halfCB.isSelected() == true) appPrefs.setProperty("halfNote", "1");
			else appPrefs.setProperty("halfNote", "0");
			if (quarterCB.isSelected() == true) appPrefs.setProperty("quarterNote", "1");
			else appPrefs.setProperty("quarterNote", "0");
			if (eighthCB.isSelected() == true) appPrefs.setProperty("eighthNote", "1");
			else appPrefs.setProperty("eighthNote", "0");
			if (tripletCB.isSelected() == true) appPrefs.setProperty("tripletNote", "1");
			else appPrefs.setProperty("tripletNote", "0");
			if (silenceCB.isSelected() == true) appPrefs.setProperty("silenceNote", "1");
			else appPrefs.setProperty("silenceNote", "0");

			if (fourfourButton.isSelected() == true)
				appPrefs.setProperty("timeSignature", "0");
			else if (twofourButton.isSelected() == true)
				appPrefs.setProperty("timeSignature", "1");
			else if (threefourButton.isSelected() == true)
				appPrefs.setProperty("timeSignature", "2");
			else if (sixeightButton.isSelected() == true)
				appPrefs.setProperty("timeSignature", "3");

			appPrefs.storeProperties();
			
			this.firePropertyChange("updateParameters", false, true);
			this.dispose();
		}
		else if (ae.getSource() == cancelButton)
		{
			this.dispose();
		}
	}
}
