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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class MidiOptionsDialog extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private ResourceBundle appBundle;
	private Preferences appPrefs;
	
	JPanel backPanel;
	
    //private JCheckBox soundOnCheckBox;
    private JComboBox instrumentsComboBox;
    private JComboBox keyboardLengthComboBox; // virtual keyboard keys number
    private JSpinner transpositionSpinner; // MIDI IN transposition
    private JSlider latencySlider;

    private JCheckBox keyboardsoundCheckBox;
    private JCheckBox accentsCheckBox;
    private JCheckBox showBeatsCheckBox;

    private JComboBox midiInComboBox;
    
    JButton okButton;
    JButton cancelButton;

	public MidiOptionsDialog(ResourceBundle b, Preferences p, Instrument[] iList)
	{
		appBundle = b;
		appPrefs = p;
		
		Font titleFont = new Font("Arial", Font.BOLD, 18);
		
		setTitle(appBundle.getString("_menuMidi"));
        setSize(517, 380);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window on the display
        setLayout(null);
		setIconImage(new ImageIcon(getClass().getResource("/resources/midi.png")).getImage());
		
		backPanel = new JPanel();
        backPanel.setLayout(null);
        backPanel.setBackground(Color.white);
        backPanel.setBounds(0, 0, 517, 380);
        
        int tmpYpos = 5;
        
        // ******************************* MIDI in panel *****************************
        
        RoundPanel midiInPanel = new RoundPanel();
        midiInPanel.setLayout(null);
        midiInPanel.setBackground(Color.white);
        midiInPanel.setBounds(5, tmpYpos, 500, 50);

        JLabel midiLabel = new JLabel(appBundle.getString("_midiclavier"));
        midiLabel.setFont(titleFont);
        midiLabel.setBounds(10, 5, 200, 40);
        midiInPanel.add(midiLabel);
        
        // MIDI IN panel
    	midiInComboBox = new JComboBox();
    	midiInComboBox.setBounds(250, 12, 230, 25);
    	midiInComboBox.addItem(appBundle.getString("_nomidiin"));
        MidiDevice.Info[] aInfos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < aInfos.length; i++) 
        {
            try 
            {
                MidiDevice device=MidiSystem.getMidiDevice(aInfos[i]);
                boolean bAllowsInput = (device.getMaxTransmitters() != 0);

                if (bAllowsInput) 
                {
                	midiInComboBox.addItem(aInfos[i].getName());
                }

            }
            catch (MidiUnavailableException e) {  }
        }
        
        int midiDevIdx = Integer.parseInt(appPrefs.getProperty("mididevice"));
        if (midiDevIdx < 0 || midiDevIdx >= midiInComboBox.getItemCount())
        	midiInComboBox.setSelectedIndex(0);
        else
        	midiInComboBox.setSelectedIndex(midiDevIdx);

        midiInPanel.add(midiInComboBox);
        tmpYpos+=55;
       
        // ******************************* keyboard sound & lenght *****************************
        RoundPanel soundPanel = new RoundPanel();
        soundPanel.setLayout(null);
        soundPanel.setBackground(Color.white);
        soundPanel.setBounds(5, tmpYpos, 500, 50);

        keyboardsoundCheckBox = new JCheckBox(appBundle.getString("_keyboardsound"), false);
        keyboardsoundCheckBox.setBounds(20, 5, 140, 40);
        int kSound = Integer.parseInt(appPrefs.getProperty("keyboardsound")); 
        if (kSound == -1 || kSound == 1)
        	keyboardsoundCheckBox.setSelected(true);

		instrumentsComboBox = new JComboBox();
		instrumentsComboBox.setBounds(160, 12, 200, 25);
		if (iList != null) 
		{
            for (int i=0; i<20; i++) 
                instrumentsComboBox.addItem(iList[i].getName());
        } 
		else 
		{
            instrumentsComboBox.addItem("No instrument available");
        }
		if (instrumentsComboBox.getItemCount() > 0)
		{
			int instIdx = Integer.parseInt(appPrefs.getProperty("instrument"));
			if (instIdx == -1) instIdx = 0;
			instrumentsComboBox.setSelectedIndex(instIdx);
		}
		instrumentsComboBox.addActionListener(this);

        keyboardLengthComboBox = new JComboBox();
        keyboardLengthComboBox.setBounds(370, 12, 110, 25);
        keyboardLengthComboBox.addItem("73 " + appBundle.getString("_keys"));
        keyboardLengthComboBox.addItem("61 " + appBundle.getString("_keys"));
        if (Integer.parseInt(appPrefs.getProperty("keyboardlength")) == 61)
        	keyboardLengthComboBox.setSelectedIndex(1);
        keyboardLengthComboBox.addActionListener(this);
		
		/*soundOnCheckBox = new JCheckBox(appBundle.getString("_notessound"), false);
		if (Integer.parseInt(appPrefs.getProperty("sound")) != 0)
			soundOnCheckBox.setSelected(true);
		 */

        //soundPanel.add(soundOnCheckBox);
        soundPanel.add(keyboardsoundCheckBox);
        soundPanel.add(instrumentsComboBox);
        soundPanel.add(keyboardLengthComboBox);
        tmpYpos+=55;
        
        // ******************************* transposition *****************************
        
        RoundPanel keyboardPanel = new RoundPanel();
        keyboardPanel.setLayout(null);
        keyboardPanel.setBackground(Color.white);
        keyboardPanel.setBounds(5, tmpYpos, 500, 50);
        
        JLabel keyLabel = new JLabel(appBundle.getString("_transposition"));
        keyLabel.setFont(titleFont);
        keyLabel.setBounds(10, 5, 260, 40);

        int trVal = Integer.parseInt(appPrefs.getProperty("transposition"));
        if (trVal == -1) trVal = 0;

        SpinnerModel model = new SpinnerNumberModel(trVal, -24, 24, 1);
        transpositionSpinner = new JSpinner(model);
        transpositionSpinner.setBounds(380, 12, 100, 25);
        

        keyboardPanel.add(keyLabel);
        keyboardPanel.add(transpositionSpinner);
        tmpYpos+=55;

        // ******************************* metronome panel *****************************
        // show metronome beats panel
        RoundPanel metronomePanel = new RoundPanel();
        metronomePanel.setLayout(null);
        metronomePanel.setBackground(Color.white);
        metronomePanel.setBounds(5, tmpYpos, 500, 50);
        
        JLabel clickLabel = new JLabel(appBundle.getString("_menuMetronom"));
        clickLabel.setFont(titleFont);
        clickLabel.setBounds(10, 5, 260, 40);
        
        accentsCheckBox = new JCheckBox(appBundle.getString("_beatAccents"));
        accentsCheckBox.setBounds(220, 12, 100, 25);
        if (Integer.parseInt(appPrefs.getProperty("clickAccents")) == 1)
        	accentsCheckBox.setSelected(true);
        
        showBeatsCheckBox = new JCheckBox(appBundle.getString("_menuShowMetronom"));
        showBeatsCheckBox.setBounds(330, 12, 150, 25);
        if (Integer.parseInt(appPrefs.getProperty("showBeats")) == 1)
        	showBeatsCheckBox.setSelected(true);
        
        metronomePanel.add(clickLabel);
        metronomePanel.add(accentsCheckBox);
        metronomePanel.add(showBeatsCheckBox);
        tmpYpos+=55;
        
        // ******************************* latency panel *****************************
        RoundPanel latencyPanel = new RoundPanel();
        latencyPanel.setLayout(null);
        latencyPanel.setBackground(Color.white);
        latencyPanel.setBounds(5, tmpYpos, 500, 70);
        
        JLabel latLabel = new JLabel(appBundle.getString("_latency"));
        latLabel.setFont(titleFont);
        latLabel.setBounds(10, 5, 200, 60);

        latencySlider = new JSlider(JSlider.HORIZONTAL, 0, 500, 0);
        latencySlider.setBounds(130, 3, 350, 60);
        latencySlider.setMajorTickSpacing(50);
        latencySlider.setMinorTickSpacing(10);
        latencySlider.setPaintTicks(true);
        latencySlider.setPaintLabels(true);

        latencyPanel.add(latLabel);
        latencyPanel.add(latencySlider);
        tmpYpos+=75;
       
    	try
    	{
            latencySlider.setValue(Integer.parseInt(appPrefs.getProperty("latency")));	   
    	}
  	    catch (Exception e) 
  	    {
  	      System.out.println(e);
  	    }

    	// ******************************* buttons panel *****************************
    	JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.white);
        buttonPanel.setBounds(5, tmpYpos, 500, 40);

    	okButton = new JButton(appBundle.getString("_buttonok"));
        okButton.setIcon(new ImageIcon(getClass().getResource("/resources/correct.png")));
        okButton.addActionListener(this);

        cancelButton = new JButton(appBundle.getString("_buttoncancel"));
        cancelButton.setIcon(new ImageIcon(getClass().getResource("/resources/wrong.png")));
        cancelButton.addActionListener(this);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        backPanel.add(midiInPanel);
        backPanel.add(keyboardPanel);
        backPanel.add(soundPanel);
        backPanel.add(metronomePanel);
        backPanel.add(latencyPanel);
        backPanel.add(buttonPanel);
        
        add(backPanel);

	}
	
	public void actionPerformed(ActionEvent ae)
    {
		if (ae.getSource() == okButton)
		{
			boolean newMidiDev = false;
			boolean newInstrument = false;
			boolean newTranpose = false;

	    	/*
	    	if (soundOnCheckBox.isSelected())
	    		appPrefs.setProperty("sound", "1");
		    else 
		    	appPrefs.setProperty("sound", "0");
		    */ 
	    	if (keyboardsoundCheckBox.isSelected())
	    		appPrefs.setProperty("keyboardsound", "1");
		    else 
		    	appPrefs.setProperty("keyboardsound", "0");
	    	
	    	if (accentsCheckBox.isSelected() == true)
	    		appPrefs.setProperty("clickAccents", "1");
	    	else
	    		appPrefs.setProperty("clickAccents", "0");
	    	
	    	if (showBeatsCheckBox.isSelected() == true)
	    		appPrefs.setProperty("showBeats", "1");
	    	else
	    		appPrefs.setProperty("showBeats", "0");

	    	SpinnerNumberModel model =  (SpinnerNumberModel)transpositionSpinner.getModel();
	    	if (Integer.parseInt(appPrefs.getProperty("transposition")) != model.getNumber().intValue())
	    		newTranpose = true;
			appPrefs.setProperty("transposition",String.valueOf(model.getNumber().intValue()));
			
	    	if (keyboardLengthComboBox.getSelectedIndex() == 1) 
	    		appPrefs.setProperty("keyboardlength","61");
	    	else 
	    		appPrefs.setProperty("keyboardlength","73");

	    	if (Integer.parseInt(appPrefs.getProperty("mididevice")) != midiInComboBox.getSelectedIndex())
	    		newMidiDev = true;
	    	appPrefs.setProperty("mididevice",String.valueOf(midiInComboBox.getSelectedIndex()));
	    	if (Integer.parseInt(appPrefs.getProperty("instrument")) != instrumentsComboBox.getSelectedIndex())
	    		newInstrument = true;
	    	appPrefs.setProperty("instrument",String.valueOf(instrumentsComboBox.getSelectedIndex())); 
	    	appPrefs.setProperty("latency",String.valueOf(latencySlider.getValue()));
	    	appPrefs.storeProperties();
	    	
	    	if (newMidiDev == true)
	    		this.firePropertyChange("newMidiDevice", false, true);
	    	if (newInstrument == true)
	    		this.firePropertyChange("newMidiInstrument", false, true);
	    	if (newTranpose == true)
	    		this.firePropertyChange("newTranspose", false, true);
	    	
			this.dispose();
		}
		else if (ae.getSource() == cancelButton)
		{
			this.dispose();
		}
	}
}
