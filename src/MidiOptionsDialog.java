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
import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.Collections;
import java.util.ResourceBundle;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.jpab.Device;
import org.jpab.PortAudio;
import org.jpab.PortAudioException;

public class MidiOptionsDialog extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private ResourceBundle appBundle;
	private Preferences appPrefs;
	
	JPanel backPanel;

	private JRadioButton midiInputRadio;
	private JRadioButton audioInputRadio;
	private JComboBox inputDeviceComboBox;
	private JButton audioTestButton;
	
    //private JCheckBox soundOnCheckBox;
    private JComboBox instrumentsComboBox;
    private JComboBox keyboardLengthComboBox; // virtual keyboard keys number
    private JSpinner transpositionSpinner; // MIDI IN transposition
    private JSlider latencySlider;

    private JCheckBox keyboardsoundCheckBox;
    private JCheckBox accentsCheckBox;
    private JCheckBox showBeatsCheckBox;
    
    private JRadioButton javaSynthRadio;
    private JRadioButton fluidsynthRadio;
    private JComboBox fluidDevComboBox;
    private JTextField sbankPath;
    private JButton sfSelectButton;
    
    JButton okButton;
    JButton cancelButton;
    
    Vector <String>outDevList = new Vector<String>();
    AudioOptionDialog audioOptions;
    AudioInputController appAudioController;

	public MidiOptionsDialog(ResourceBundle b, Preferences p, MidiController midiCtrl, AudioInputController audioCtrl)
	{
		appBundle = b;
		appPrefs = p;
		appAudioController = audioCtrl;
		
		Font titleFont = new Font("Arial", Font.BOLD, 18);
		
		setTitle(appBundle.getString("_menuMidi"));
        setSize(517, 485);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window on the display
        setLayout(null);
		setIconImage(new ImageIcon(getClass().getResource("/resources/midi.png")).getImage());
		
		backPanel = new JPanel();
        backPanel.setLayout(null);
        backPanel.setBackground(Color.white);
        backPanel.setBounds(0, 0, 517, 460);
        
        int tmpYpos = 5;
        
        // ******************************* MIDI in panel *****************************
        
        RoundPanel midiInPanel = new RoundPanel();
        midiInPanel.setLayout(null);
        midiInPanel.setBackground(Color.white);
        midiInPanel.setBounds(5, tmpYpos, 500, 80);

        JLabel midiLabel = new JLabel(appBundle.getString("_midiInput"));
        midiLabel.setFont(titleFont);
        midiLabel.setBounds(10, 5, 200, 40);
        midiInPanel.add(midiLabel);
        
        // MIDI IN panel
        ButtonGroup inputGroup = new ButtonGroup();
        midiInputRadio = new JRadioButton("MIDI");
        midiInputRadio.setBounds(180, 12, 70, 25);
        audioInputRadio = new JRadioButton("Audio");
        audioInputRadio.setBounds(270, 12, 70, 25);
        
        inputGroup.add(midiInputRadio);
        inputGroup.add(audioInputRadio);
        
    	inputDeviceComboBox = new JComboBox();
    	inputDeviceComboBox.setBounds(180, 42, 260, 25);
    	String midiInput = appPrefs.getProperty("inputDevice");
		if (midiInput == "-1" || midiInput.split(",")[0].equals("MIDI"))
			midiInputRadio.setSelected(true);
		else if (midiInput.split(",")[0].equals("Audio"))
			audioInputRadio.setSelected(true);
		midiInputRadio.addActionListener(this);
		audioInputRadio.addActionListener(this);

		audioTestButton = new JButton("...");
		audioTestButton.setIcon(new ImageIcon(getClass().getResource("/resources/microphone.png")));
		audioTestButton.setFont(new Font("Arial", Font.BOLD, 13));
		audioTestButton.setBounds(445, 42, 45, 25);
		audioTestButton.addActionListener(this);
		if (audioInputRadio.isSelected() == false)
			audioTestButton.setVisible(false);

        midiInPanel.add(midiInputRadio);
        midiInPanel.add(audioInputRadio);
        midiInPanel.add(inputDeviceComboBox);
        midiInPanel.add(audioTestButton);
        tmpYpos+=85;
       
        // ******************************* MIDI playback panel *****************************
        RoundPanel soundPanel = new RoundPanel();
        soundPanel.setLayout(null);
        soundPanel.setBackground(Color.white);
        soundPanel.setBounds(5, tmpYpos, 500, 135);
        
        JLabel midiOut = new JLabel(appBundle.getString("_midiOutput"));
        midiOut.setFont(titleFont);
        midiOut.setBounds(10, 5, 250, 40);
        soundPanel.add(midiOut);
        
        ButtonGroup rbGroup = new ButtonGroup();
        javaSynthRadio = new JRadioButton("Java");
        javaSynthRadio.setBounds(20, 37, 70, 30);
        javaSynthRadio.addActionListener(this);
        fluidsynthRadio = new JRadioButton("Fluidsynth");
        fluidsynthRadio.setBounds(90, 37, 90, 30);
        fluidsynthRadio.addActionListener(this);
        rbGroup.add(javaSynthRadio);
		rbGroup.add(fluidsynthRadio);
		
		fluidDevComboBox = new JComboBox(outDevList);
		fluidDevComboBox.setBounds(180, 38, 300, 25);

		soundPanel.add(javaSynthRadio);
		soundPanel.add(fluidsynthRadio);
		soundPanel.add(fluidDevComboBox);
		
		String midiSynth = appPrefs.getProperty("outputDevice");
		if (midiSynth == "-1" || midiSynth.equals("Java"))
			javaSynthRadio.setSelected(true);
		else if (midiSynth.split(",")[0].equals("Fluidsynth"))
		{
			fluidsynthRadio.setSelected(true);
		}
		
		JLabel soundBank = new JLabel(appBundle.getString("_midiLibrary"));
		soundBank.setFont(new Font("Arial", Font.BOLD, 13));
		soundBank.setBounds(20, 61, 200, 40);
        soundPanel.add(soundBank);
        
        String bankPath = "Default Java soundbank";
        if (fluidsynthRadio.isSelected() == true)
        	bankPath = appPrefs.getProperty("soundfontPath");
        if (bankPath == "-1") bankPath = "No soundfont selected";
        sbankPath = new JTextField(bankPath);
        sbankPath.setBounds(190, 70, 240, 25);
        soundPanel.add(sbankPath);
        
        sfSelectButton = new JButton("...");
        sfSelectButton.setBounds(440, 70, 40, 25);
        sfSelectButton.setFont(new Font("Arial", Font.BOLD, 13));
        sfSelectButton.addActionListener(this);
        if (javaSynthRadio.isSelected() == true)
        {
        	fluidDevComboBox.setVisible(false);
        	sfSelectButton.setVisible(false);
        }
        soundPanel.add(sfSelectButton);

        keyboardsoundCheckBox = new JCheckBox(appBundle.getString("_keyboardsound"), false);
        keyboardsoundCheckBox.setBounds(20, 93, 140, 40);
        int kSound = Integer.parseInt(appPrefs.getProperty("keyboardsound")); 
        if (kSound == -1 || kSound == 1)
        	keyboardsoundCheckBox.setSelected(true);

		instrumentsComboBox = new JComboBox();
		instrumentsComboBox.setBounds(160, 100, 190, 25);
		List<String> iList = midiCtrl.getInstruments();
		if (iList != null && iList.size() > 0)
		{
            for (int i=0; i < iList.size(); i++) 
                instrumentsComboBox.addItem(iList.get(i));
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
        keyboardLengthComboBox.setBounds(360, 100, 120, 25);
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
        tmpYpos+=140;
        
        // ******************************* transposition *****************************
        
        RoundPanel keyboardPanel = new RoundPanel();
        keyboardPanel.setLayout(null);
        keyboardPanel.setBackground(Color.white);
        keyboardPanel.setBounds(5, tmpYpos, 500, 50);
        
        JLabel keyLabel = new JLabel(appBundle.getString("_transposition"));
        keyLabel.setFont(titleFont);
        keyLabel.setBounds(10, 5, 350, 40);

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
        buttonPanel.setBounds(5, tmpYpos, 517, 40);

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
        
        reloadInputList();

        if (fluidsynthRadio.isSelected() == true)
        {
        	List<String> devList = null;
        	if (NativeUtils.isWindows() == false) 
        		devList = midiCtrl.getFluidDevices();
        	reloadDevicesList(devList);
        	fluidDevComboBox.addActionListener(this);
        }

	}
	
	public void reloadInputList()
	{
		String inputDev = appPrefs.getProperty("inputDevice");
		int devIndex = -1;
		if (inputDev != "-1")
			devIndex = Integer.parseInt(inputDev.split(",")[1]);
		
		inputDeviceComboBox.removeAllItems();
		if (midiInputRadio.isSelected() == true)
		{
			inputDeviceComboBox.addItem(appBundle.getString("_nomidiin"));
			MidiDevice.Info[] aInfos = MidiSystem.getMidiDeviceInfo();
			for (int i = 0; i < aInfos.length; i++) 
			{
				try 
				{
					MidiDevice device=MidiSystem.getMidiDevice(aInfos[i]);
					boolean bAllowsInput = (device.getMaxTransmitters() != 0);
					if (bAllowsInput) 
						inputDeviceComboBox.addItem(aInfos[i].getName());
				} catch (MidiUnavailableException e) {  }
			}
        
			if (devIndex < 0 || devIndex >= inputDeviceComboBox.getItemCount())
				inputDeviceComboBox.setSelectedIndex(0);
			else
				inputDeviceComboBox.setSelectedIndex(devIndex);
		}
	}
	
	public void reloadInstruments(List<String> iList)
	{
		instrumentsComboBox.removeAllItems();
		if (iList != null)
			System.out.println("Number of instruments: " +  iList.size());
		if (iList != null && iList.size() > 0)
		{
            for (int i=0; i<iList.size(); i++) 
                instrumentsComboBox.addItem(iList.get(i));
        }
		else 
		{
            instrumentsComboBox.addItem("No instrument available");
        }
	}
	
	public void reloadDevicesList(List<String> devList)
	{
		String outputDevice = appPrefs.getProperty("outputDevice");
		String inputDevice = appPrefs.getProperty("inputDevice");
		int inputDevIndex = -1, outputDevIndex = -1;
		if (outputDevice != "-1" && outputDevice.equals("Java") == false)
			outputDevIndex = Integer.parseInt(outputDevice.split(",")[1]);
		if (inputDevice != "-1")
			inputDevIndex = Integer.parseInt(inputDevice.split(",")[1]);
		
		outDevList.clear();
		fluidDevComboBox.removeAllItems();
		if (audioInputRadio.isSelected() == true)
			inputDeviceComboBox.removeAllItems();
		
		if (NativeUtils.isWindows()) 
		{
			// since we use only PortAudio on Windows, retrieve the list directly from PortAudio
			try {
				PortAudio.initialize();
				for (Device device : PortAudio.getDevices()) 
				{
					String devName = device.getName() + " [" + device.getHostAPI().getType() + "]";
					if (device.getMaxOutputChannels() >= 2 && fluidsynthRadio.isSelected() == true)
						outDevList.add(devName);
					else if (device.getMaxInputChannels() > 0 &&  audioInputRadio.isSelected() == true)
						inputDeviceComboBox.addItem(devName);
					//System.out.println(device);
					//System.out.println(device.getName());
					//System.out.println("Host API ID: " + device.getHostAPI().toString());
				}
				PortAudio.terminate();
			} catch (PortAudioException ex) {  }

			outDevList.add("PortAudio Default"); // this is a dummy device added by Fluidsynth
			Collections.sort(outDevList);
			if (outputDevIndex >= fluidDevComboBox.getItemCount())
				outputDevIndex = 0;
			fluidDevComboBox.setSelectedIndex(outputDevIndex);

			if (inputDevIndex >= inputDeviceComboBox.getItemCount())
				inputDevIndex = 0;
			inputDeviceComboBox.setSelectedIndex(inputDevIndex);
		}
		else
		{
			//String selDevice = appPrefs.getProperty("fluidDevice");

			if (devList != null && devList.size() > 0)
				for (int d = 0; d < devList.size(); d++)
				{
					fluidDevComboBox.addItem(devList.get(d));
					if (outputDevIndex == d)
						fluidDevComboBox.setSelectedIndex(d);
				}
		}
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
			String inDev = appPrefs.getProperty("inputDevice");
			String outDev = appPrefs.getProperty("outputDevice");
			if (inDev == "-1" || Integer.parseInt(inDev.split(",")[1]) != inputDeviceComboBox.getSelectedIndex())
	    		newMidiDev = true;
			else if (inDev != "-1")
			{
				String inSys =  inDev.split(",")[0];
				if (inSys.equals("MIDI") && audioInputRadio.isSelected() == true || 
					inSys.equals("Audio") && midiInputRadio.isSelected() == true)
						newMidiDev = true;
			}
			
			if (outDev == "-1" || Integer.parseInt(outDev.split(",")[1]) != fluidDevComboBox.getSelectedIndex())
				newMidiDev = true;
			else if (outDev != "-1")
			{
				String outSys =  outDev.split(",")[0];
				if (outSys.equals("Java") && fluidsynthRadio.isSelected() == true || 
					outSys.equals("Fluidsynth") && javaSynthRadio.isSelected() == true)
						newMidiDev = true;
			}

			if (javaSynthRadio.isSelected() == true)
				appPrefs.setProperty("outputDevice", "Java");
			else
			{
				int idx = fluidDevComboBox.getSelectedIndex();
				appPrefs.setProperty("outputDevice", "Fluidsynth," + idx);
			}
			
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

	    	SpinnerNumberModel model = (SpinnerNumberModel)transpositionSpinner.getModel();
	    	if (Integer.parseInt(appPrefs.getProperty("transposition")) != model.getNumber().intValue())
	    		newTranpose = true;
			appPrefs.setProperty("transposition",String.valueOf(model.getNumber().intValue()));
			
	    	if (keyboardLengthComboBox.getSelectedIndex() == 1) 
	    		appPrefs.setProperty("keyboardlength","61");
	    	else 
	    		appPrefs.setProperty("keyboardlength","73");

	    	if (midiInputRadio.isSelected() == true)
	    		appPrefs.setProperty("inputDevice", "MIDI," + String.valueOf(inputDeviceComboBox.getSelectedIndex()));
	    	else
	    		appPrefs.setProperty("inputDevice", "Audio," + String.valueOf(inputDeviceComboBox.getSelectedIndex()));
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
		else if (ae.getSource() == midiInputRadio)
		{
			reloadInputList();
			audioTestButton.setVisible(false);
		}
		else if (ae.getSource() == audioInputRadio)
		{
			reloadDevicesList(null);
			audioTestButton.setVisible(true);
		}
		else if (ae.getSource() == audioTestButton)
		{
			audioOptions = new AudioOptionDialog(appBundle, appPrefs, appAudioController);
			audioOptions.setVisible(true);
		}
		else if (ae.getSource() == javaSynthRadio)
		{
			appPrefs.setProperty("outputDevice", "Java");
			appPrefs.storeProperties();
			sbankPath.setText("Default Java soundbank");
			fluidDevComboBox.setVisible(false);
			sfSelectButton.setVisible(false);
			this.firePropertyChange("newMidiDevice", false, true);
		}
		else if (ae.getSource() == fluidsynthRadio)
		{
			appPrefs.setProperty("outputDevice", "Fluidsynth,0");
			appPrefs.storeProperties();
			fluidDevComboBox.setVisible(true);
			sfSelectButton.setVisible(true);
			
			String bankPath = appPrefs.getProperty("soundfontPath");
	        if (bankPath == "-1") bankPath = "No soundfont selected";
	        sbankPath.setText(bankPath);

			this.firePropertyChange("newMidiDevice", false, true);
		}
		else if (ae.getSource() == sfSelectButton)
		{
			final JFileChooser fc = new JFileChooser();
			fc.setFileFilter( new BankFilter() );
			int returnVal = fc.showOpenDialog(this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) 
	        {
	            File file = fc.getSelectedFile();
	            //This is where a real application would open the file.
	            System.out.println("Opening: " + file.getAbsolutePath() + "(" + file.getName() + ")");
	            sbankPath.setText(file.getAbsolutePath());
	            appPrefs.setProperty("soundfontPath", file.getAbsolutePath());
	            appPrefs.storeProperties();
	            this.firePropertyChange("newMidiDevice", false, true);
	        } 
	        else 
	        {
	        	System.out.println("Open command cancelled by user.");
	        }
		}
	}
}

class BankFilter extends FileFilter 
{
   public String getDescription() 
   { 
      return "Soundfont File (*.sf2)"; 
   } 

   public boolean accept(File f) 
   {
	  if(f.isDirectory()) return true;

      return f.getName().toUpperCase().endsWith(".SF2");
   }
}
