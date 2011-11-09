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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class MidiOptionsDialog extends JDialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ResourceBundle appBundle;
	private Preferences appPrefs;
	
    //private JCheckBox soundOnCheckBox;
    private JComboBox instrumentsComboBox;
    private JComboBox keyboardLengthComboBox; // for length-number of touchs of keyboard
    private JComboBox transpositionComboBox; // for transposition MIDI keyboard
    private JSlider latencySlider;

    private JCheckBox keyboardsoundCheckBox;
    private JCheckBox showBeatsCheckBox;

    private JComboBox midiInComboBox;
    
    JButton okButton;
    JButton cancelButton;

	public MidiOptionsDialog(ResourceBundle b, Preferences p, Instrument[] iList)
	{
		appBundle = b;
		appPrefs = p;
		
		this.setIconImage(new ImageIcon(getClass().getResource("/resources/midi.png")).getImage());
		
		JPanel soundPanel = new JPanel(); // panel midi keyboard
		soundPanel.setBorder(BorderFactory.createTitledBorder(appBundle.getString("_sound")));

		/*soundOnCheckBox = new JCheckBox(appBundle.getString("_notessound"), false);
		if (Integer.parseInt(appPrefs.getProperty("sound")) != 0)
			soundOnCheckBox.setSelected(true);
		 */
        keyboardsoundCheckBox = new JCheckBox(appBundle.getString("_keyboardsound"), false);
        int kSound = Integer.parseInt(appPrefs.getProperty("keyboardsound")); 
        if (kSound == -1 || kSound == 1)
        	keyboardsoundCheckBox.setSelected(true);

		instrumentsComboBox = new JComboBox();
		if (iList != null) 
		{
            for (int i=0; i<20; i++) 
                instrumentsComboBox.addItem(iList[i].getName());
        } 
		else 
		{
            instrumentsComboBox.addItem("No instrument available");
            System.out.println("No soundbank file : http://java.sun.com/products/java-media/sound/soundbanks.html");
        }
		if (instrumentsComboBox.getItemCount() > 0)
		{
			int instIdx = Integer.parseInt(appPrefs.getProperty("instrument"));
			if (instIdx == -1) instIdx = 0;
			instrumentsComboBox.setSelectedIndex(instIdx);
		}
		instrumentsComboBox.addActionListener(this);

        JPanel keyboardSoundPanel = new JPanel();
        //keyboardSoundPanel.add(soundOnCheckBox);
        keyboardSoundPanel.add(keyboardsoundCheckBox);
        keyboardSoundPanel.add(instrumentsComboBox);

        soundPanel.setLayout(new BorderLayout());
        soundPanel.add(keyboardSoundPanel, BorderLayout.CENTER);

        // show metronome beats panel
        JPanel metronomePanel = new JPanel();
        metronomePanel.setBorder(BorderFactory.createTitledBorder(appBundle.getString("_menuMetronom")));
        showBeatsCheckBox = new JCheckBox(appBundle.getString("_menuShowMetronom"));
        if (Integer.parseInt(appPrefs.getProperty("showBeats")) == 1)
        	showBeatsCheckBox.setSelected(true);
        metronomePanel.add(showBeatsCheckBox);
        
        /* Latency - Cursor Speed panel */
		latencySlider = new JSlider(JSlider.HORIZONTAL, 0, 250, 0);
        latencySlider.setMajorTickSpacing(50);
        latencySlider.setMinorTickSpacing(10);
        latencySlider.setPaintTicks(true);
        latencySlider.setPaintLabels(true);

		JPanel latencyPanel = new JPanel();
		latencyPanel.setBorder(BorderFactory.createTitledBorder(appBundle.getString("_latency")));
        latencyPanel.add(latencySlider);
       
    	try
    	{
            latencySlider.setValue(Integer.parseInt(appPrefs.getProperty("latency")));	   
    	}
  	    catch (Exception e) 
  	    {
  	      System.out.println(e);
  	    }

        // MIDI IN panel
    	midiInComboBox = new JComboBox();
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

        keyboardLengthComboBox = new JComboBox();
        keyboardLengthComboBox.addItem("73 " + appBundle.getString("_keys"));
        keyboardLengthComboBox.addItem("61 " + appBundle.getString("_keys"));
        if (Integer.parseInt(appPrefs.getProperty("keyboardlength")) == 61)
        	keyboardLengthComboBox.setSelectedIndex(1);
        keyboardLengthComboBox.addActionListener(this);

        transpositionComboBox = new JComboBox();
        transpositionComboBox.addItem("-2 " + appBundle.getString("_octave"));
        transpositionComboBox.addItem("-1 " + appBundle.getString("_octave"));
        transpositionComboBox.addItem(appBundle.getString("_notransposition"));
        transpositionComboBox.addItem("1 " + appBundle.getString("_octave"));
        transpositionComboBox.addItem("2 " + appBundle.getString("_octave"));
        int trIdx = Integer.parseInt(appPrefs.getProperty("transposition"));
        if (trIdx == -1) trIdx = 2;
        transpositionComboBox.setSelectedIndex(trIdx);
        transpositionComboBox.addActionListener(this);

        JPanel keyboardPanel = new JPanel();
        keyboardPanel.add(keyboardLengthComboBox);
        keyboardPanel.add(transpositionComboBox);

        JPanel midiInPanel = new JPanel();
        midiInPanel.setBorder(BorderFactory.createTitledBorder(appBundle.getString("_midiclavier")));
        midiInPanel.setLayout(new BorderLayout());
        midiInPanel.add(midiInComboBox, BorderLayout.NORTH);
        midiInPanel.add(keyboardPanel, BorderLayout.CENTER);

        // ----

        okButton = new JButton(appBundle.getString("_buttonok"));
        okButton.setIcon(new ImageIcon(getClass().getResource("/resources/correct.png")));
        okButton.addActionListener(this);

        cancelButton = new JButton(appBundle.getString("_buttoncancel"));
        cancelButton.setIcon(new ImageIcon(getClass().getResource("/resources/wrong.png")));
        cancelButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // ----

        JPanel contentPanel = new JPanel();
        contentPanel.add(soundPanel);
        contentPanel.add(metronomePanel);
        contentPanel.add(midiInPanel);
        contentPanel.add(latencyPanel);
        contentPanel.add(buttonPanel);

        setTitle(appBundle.getString("_menuMidi"));
        setContentPane(contentPanel);
        setSize(520, 320);
        setLocationRelativeTo(null); // Center the window on the display
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
	    	
	    	if (showBeatsCheckBox.isSelected() == true)
	    		appPrefs.setProperty("showBeats", "1");
	    	else
	    		appPrefs.setProperty("showBeats", "0");
	    	
	    	if (Integer.parseInt(appPrefs.getProperty("transposition")) != transpositionComboBox.getSelectedIndex())
	    		newTranpose = true;
			appPrefs.setProperty("transposition",String.valueOf(transpositionComboBox.getSelectedIndex()));
			
			
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
	    		this.firePropertyChange("newTranpose", false, true);
	    	
			this.dispose();
		}
		else if (ae.getSource() == cancelButton)
		{
			this.dispose();
		}
	}
}
