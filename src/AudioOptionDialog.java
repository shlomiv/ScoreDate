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
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AudioOptionDialog extends JDialog implements ActionListener, ChangeListener 
{
	private static final long serialVersionUID = 6932887852996919458L;
	private ResourceBundle appBundle;
	private Preferences appPrefs;
	AudioInputController audioControl;
	
	JPanel backPanel;
	private JComboBox audioInComboBox;
	private JSlider sensitivitySlider;
	private RoundedButton testBtn;
	private AudioMonitor audioMon;
	
	JButton okButton;
    JButton cancelButton;
    
    String currAudioDevice = "";
    boolean testInProgress = false;
	
	public AudioOptionDialog(ResourceBundle b, Preferences p, AudioInputController ac)
	{
		appBundle = b;
		appPrefs = p;
		audioControl = ac;
		
		setTitle(appBundle.getString("_menuAudio"));
        setSize(600, 380);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window on the display
        setLayout(null);
		setIconImage(new ImageIcon(getClass().getResource("/resources/microphone.png")).getImage());
		
		Font titleFont = new Font("Arial", Font.BOLD, 18);
		
		backPanel = new JPanel();
        backPanel.setLayout(null);
        backPanel.setBackground(Color.white);
        backPanel.setBounds(0, 0, 600, 380);
        
        int tmpYpos = 5;
        
        // AUDIO IN panel
        RoundPanel audioInPanel = new RoundPanel();
        audioInPanel.setLayout(null);
        audioInPanel.setBackground(Color.white);
        audioInPanel.setBounds(5, tmpYpos, 583, 50);
        
        JLabel audioDevLabel = new JLabel(appBundle.getString("_audioIn"));
        audioDevLabel.setFont(titleFont);
        audioDevLabel.setBounds(10, 5, 300, 40);
        audioInPanel.add(audioDevLabel);
        
        audioInComboBox = new JComboBox();
        audioInComboBox.setBounds(320, 12, 250, 25);
        currAudioDevice = appPrefs.getProperty("audiodevice");
        Vector<String> tmpList = audioControl.getDevicesList("");
        
        if (tmpList.size() == 0)
        	audioInComboBox.addItem(appBundle.getString("_noAudioIn"));
        else
        {
        	for (int i = 0; i < tmpList.size(); i++)
        	{
        		audioInComboBox.addItem(tmpList.get(i));
        		if (currAudioDevice.equals(tmpList.get(i)))
        			audioInComboBox.setSelectedIndex(i);
        	}
        }
        audioInPanel.add(audioInComboBox);
        tmpYpos+=55;
        
     // ******************************* audio sensitivity panel *****************************
        RoundPanel audioSensitivityPanel = new RoundPanel();
        audioSensitivityPanel.setLayout(null);
        audioSensitivityPanel.setBackground(Color.white);
        audioSensitivityPanel.setBounds(5, tmpYpos, 583, 50);
        
        JLabel audioSensLabel = new JLabel(appBundle.getString("_audioSensitivity"));
        audioSensLabel.setFont(titleFont);
        audioSensLabel.setBounds(10, 5, 300, 40);
        audioSensitivityPanel.add(audioSensLabel);
        
        sensitivitySlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 10);
        sensitivitySlider.setBounds(365, -6, 200, 60);
        sensitivitySlider.setMajorTickSpacing(10);
        sensitivitySlider.setSnapToTicks(true);
        //sensitivitySlider.setMinorTickSpacing(10);
        sensitivitySlider.setPaintTicks(true);
        //sensitivitySlider.setPaintLabels(true);
        sensitivitySlider.addChangeListener(this);
        
        audioSensitivityPanel.add(sensitivitySlider);
        tmpYpos+=55;
        
     // ******************************* audio test panel *****************************
        RoundPanel audioTestPanel = new RoundPanel();
        audioTestPanel.setLayout(null);
        audioTestPanel.setBackground(Color.white);
        audioTestPanel.setBounds(5, tmpYpos, 583, 150);
        
        testBtn = new RoundedButton("Test", appBundle, Color.decode("0xA2DDFF"));
        testBtn.setBounds(15, 40, 70, 70);
        testBtn.setBackground(Color.decode("0xA1C5FF"));
        testBtn.setFont(new Font("Arial", Font.BOLD, 16));
        testBtn.setTextOffsets(0, 15);
        testBtn.addActionListener(this);
        
        audioMon = new AudioMonitor(appBundle);
        audioMon.setBackground(Color.white);
        audioMon.setBounds(150, 10, 430, 130);
        
        audioTestPanel.add(testBtn);
        audioTestPanel.add(audioMon);
        tmpYpos+=155;
        
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
        
        backPanel.add(audioInPanel);
        backPanel.add(audioSensitivityPanel);
        backPanel.add(audioTestPanel);
        backPanel.add(buttonPanel);
        
        add(backPanel);
	}
	
	public void actionPerformed(ActionEvent ae)
    {
		if (ae.getSource() == okButton)
		{
			if (appPrefs.getProperty("audiodevice") != audioInComboBox.getSelectedItem().toString())
			{
				appPrefs.setProperty("audiodevice", audioInComboBox.getSelectedItem().toString());
				appPrefs.storeProperties();
			}
			this.dispose();
		}
		else if (ae.getSource() == cancelButton)
		{
			this.dispose();
		}
		else if (ae.getSource() == testBtn)
		{
			if (testInProgress == false)
			{
				testBtn.setLabel("Stop");
				audioControl.enableInfo(audioMon);
				audioControl.getDevicesList(audioInComboBox.getSelectedItem().toString());
				audioControl.startCapture();
				testInProgress = true;
			}
			else
			{
				testBtn.setLabel("Test");
				audioControl.stopCapture();
				testInProgress = false;
			}
		}
    }
	
	public void stateChanged(ChangeEvent e) 
	{
		if (e.getSource() == sensitivitySlider)
		{
			audioControl.setSensitivity(sensitivitySlider.getValue());
		}
	}
}
	
