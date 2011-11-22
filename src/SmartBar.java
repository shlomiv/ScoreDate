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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField; 
import javax.swing.JSlider;
import javax.swing.UIManager;

public class SmartBar extends JPanel implements ActionListener, ChangeListener 
{
	private static final long serialVersionUID = 4914147249638690529L;
	ResourceBundle appBundle;
	Preferences appPrefs;
	Font appFont;
	
	public RoundedButton homeBtn;

	RoundPanel tempoContainer;
	public JPanel tempoPanel;
	public JSlider tempoSlider;
	private JTextField tempoLabel;
	public JCheckBox metronomeCheckBox;
	
	private RoundedButton clefNoteBtn;
	private RoundPanel gameContainer;
	public JComboBox gameSelector;
	public JComboBox gameType;
	public RoundedButton refreshBtn;
	public RoundedButton playBtn;
	public RoundedButton listenBtn;

	private int buttonSize = 70;
	private int totalObjWidth = 725;
	private int upperMargin = 20;
	private boolean isInline = false;

	private ClefNotesOptionDialog clefNotesDialog;

	private Color compColor = Color.decode("0x749CC5");

	public SmartBar (Dimension d, ResourceBundle b, Font f, Preferences p, boolean inline)
	{
		appBundle = b;
		appFont = f;
		appPrefs = p;
		isInline = inline;
		setSize(d);
		setLayout(null);
		
		if (inline == false)
		{
			totalObjWidth = 700;
			upperMargin = 15;
		}

		int posX = (d.width - totalObjWidth) / 2;

		// Create home button
		homeBtn= new RoundedButton("", appBundle);
		homeBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
		homeBtn.setBounds(posX, upperMargin, buttonSize, buttonSize);
		homeBtn.setBackground(Color.decode("0x8FC6E9"));
		//homeBtn.setBackground(compColor);
		homeBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/home.png")).getImage());
		
		posX += buttonSize + 5;

		// create button to configure clefs and notes levels
		clefNoteBtn = new RoundedButton("RBL_NOTES", appBundle);
		clefNoteBtn.setPreferredSize(new Dimension(160, buttonSize));
		clefNoteBtn.setBounds(posX, upperMargin, 160, buttonSize);
		clefNoteBtn.setBackground(Color.decode("0x8FC6E9"));
		clefNoteBtn.setFont(f);
		clefNoteBtn.setIcon(new ImageIcon(getClass().getResource("/resources/settings.png")));
		clefNoteBtn.addActionListener(this);
		
		posX += 165;
		
		// create tempo container with tempo scroller and metronome check box
		tempoContainer = new RoundPanel();
		tempoContainer.setBackground(compColor);
		((FlowLayout)tempoContainer.getLayout()).setHgap(7);

		tempoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, -2));
		Border defBorder = UIManager.getBorder(tempoPanel);
		tempoPanel.setBorder(BorderFactory.createTitledBorder(defBorder, appBundle.getString("_speed"), TitledBorder.LEADING, TitledBorder.TOP));
		tempoPanel.setBackground(compColor);
		tempoPanel.setPreferredSize(new Dimension(230, 80));

		if (inline == true)
		{
			tempoSlider = new JSlider(JSlider.HORIZONTAL, 30, 200, 60);
			tempoLabel = new JTextField("60");
		}
		else
		{
			tempoSlider = new JSlider(JSlider.HORIZONTAL, 40, 200, 80);
			tempoLabel = new JTextField("80 BPM");
		}
		tempoSlider.addChangeListener(this);
		tempoLabel.setBackground(Color.white);
		tempoLabel.setPreferredSize(new Dimension(65, 25));
		tempoLabel.setAlignmentX(LEFT_ALIGNMENT);
		if (inline == false)
		{
			metronomeCheckBox = new JCheckBox(appBundle.getString("_menuMetronom"));
			metronomeCheckBox.setForeground(Color.white);
			metronomeCheckBox.setAlignmentX(RIGHT_ALIGNMENT);
			int mOn = Integer.parseInt(appPrefs.getProperty("metronome"));
			if (mOn == 1)
				metronomeCheckBox.setSelected(true);
			//metronomeCheckBox.addChangeListener(this);
			metronomeCheckBox.addActionListener(this);
		}
		
		tempoPanel.add(tempoLabel);
		if (inline == false)
			tempoPanel.add(metronomeCheckBox);
		tempoPanel.add(tempoSlider);
		
		tempoContainer.add(tempoPanel);
		tempoContainer.setBounds(posX, upperMargin - 10, 240, 90);
		
		posX += 245;
		
		if (inline == true)
		{
			gameContainer = new RoundPanel();
			gameContainer.setBackground(compColor);
			//((FlowLayout)gameContainer.getLayout()).setHgap(0);
			gameContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 3));
			
			gameContainer.setPreferredSize(new Dimension(166, buttonSize));
			
			Font sbf = new Font("Arial", Font.BOLD, 13);
			gameSelector = new JComboBox();
			gameSelector.setPreferredSize(new Dimension(155, 30));
			gameSelector.setFont(sbf);
			//gameSelector.setBackground(compColor);

			gameType = new JComboBox();
			gameType.setPreferredSize(new Dimension(155, 30));
			gameType.setFont(sbf);
			
			gameContainer.add(gameSelector);
			gameContainer.add(gameType);
			
			gameContainer.setBounds(posX, upperMargin, 166, buttonSize);
			posX += 171;
		}

		// Create refresh button
		if (inline == false)
		{
			refreshBtn= new RoundedButton("", appBundle);
			refreshBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
			refreshBtn.setBounds(posX, upperMargin, buttonSize, buttonSize);
			refreshBtn.setBackground(Color.decode("0x8FC6E9"));
			refreshBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/refresh.png")).getImage());
			
			posX += buttonSize + 5;
		}
		// Create playback button
		playBtn= new RoundedButton("", appBundle);
		playBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
		playBtn.setBounds(posX, upperMargin, buttonSize, buttonSize);
		playBtn.setBackground(Color.decode("0x8FC6E9"));
		playBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/playback.png")).getImage());
		
		posX += buttonSize + 5;

		if (inline == false)
		{
			// Create playback button
			listenBtn= new RoundedButton("", appBundle);
			listenBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
			listenBtn.setBounds(posX, upperMargin, buttonSize, buttonSize);
			listenBtn.setBackground(Color.decode("0x8FC6E9"));
			listenBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/listen.png")).getImage());
		}
		
		updateLanguage(appBundle);
		
		this.add(homeBtn);
		this.add(clefNoteBtn);
		this.add(tempoContainer);
		if (inline == true)
			this.add(gameContainer);
		else
			this.add(refreshBtn);
		this.add(playBtn);
		if (inline == false)
			this.add(listenBtn);		
	}
	
	public void updateLanguage(ResourceBundle bundle)
	{
		appBundle = bundle;
		
		clefNoteBtn.setResBundle(appBundle);

		if (isInline == true)
		{
			gameType.removeAllItems();
			gameSelector.removeAllItems();
			
			gameSelector.addItem(appBundle.getString("_normalgame"));
			gameSelector.addItem(appBundle.getString("_linegame"));
			gameSelector.addItem(appBundle.getString("_learninggame"));
			
			// TODO: 
			// WARNING !!!! If you change these entries you must change indexes of 
			// InlinePanel.setLearningInfo and InlinePanel.setGameType !!!!
			gameType.addItem(appBundle.getString("_notes"));
			gameType.addItem(appBundle.getString("_alterednotes"));
			//gameType.addItem(appBundle.getString("_customnotes"));
			gameType.addItem(appBundle.getString("_intervals") + " - " + appBundle.getString("_second"));
			gameType.addItem(appBundle.getString("_intervals") + " - " + appBundle.getString("_third"));
			gameType.addItem(appBundle.getString("_intervals") + " - " + appBundle.getString("_fourth"));
			gameType.addItem(appBundle.getString("_intervals") + " - " + appBundle.getString("_fifth"));
			gameType.addItem(appBundle.getString("_intervals") + " - " + appBundle.getString("_sixth"));
			gameType.addItem(appBundle.getString("_intervals") + " - " + appBundle.getString("_seventh"));
			gameType.addItem(appBundle.getString("_intervals") + " - " + appBundle.getString("_octave"));
			gameType.addItem(appBundle.getString("_intervals") + " - " + appBundle.getString("_minor"));
			gameType.addItem(appBundle.getString("_intervals") + " - " + appBundle.getString("_major"));
			gameType.addItem(appBundle.getString("_chords"));
			//gameType.addItem(appBundle.getString("_inversion"));
		}
		else
			metronomeCheckBox.setText(appBundle.getString("_menuMetronom"));
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == clefNoteBtn)
		{
			System.out.println("SmartBar Event received !! (" + ae.getActionCommand() + ")");
			clefNotesDialog = new ClefNotesOptionDialog(appFont, appBundle, appPrefs);
			clefNotesDialog.setVisible(true);
			clefNotesDialog.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt)
				{
					if (evt.getPropertyName() == "updateParameters")
					{
						System.out.println("ClefNotesOptionDialog update parameters.");
						firePropertyChange("updateParameters", false, true);
					}
				}
			});
		}
		else if (ae.getSource() == metronomeCheckBox)
		{
			if (metronomeCheckBox.isSelected() == true)
				appPrefs.setProperty("metronome", "1");
			else
				appPrefs.setProperty("metronome", "0");
			appPrefs.storeProperties();
		}
	}
	
	public void stateChanged(ChangeEvent e) 
	{
		if (e.getSource() == tempoSlider)
		{
			if (isInline == false)
				tempoLabel.setText(Integer.toString(tempoSlider.getValue()) + " BPM");
			else
				tempoLabel.setText(Integer.toString(tempoSlider.getValue()));
		}
    }
	
	protected void paintComponent(Graphics g) 
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//System.out.println("SmartBar paintComponent. w: " + getWidth() + ", h: " + getHeight());
		//g.setColor(Color.decode("0xAFC6E9"));
		GradientPaint vertGrad = new GradientPaint(0, 0, Color.decode("0xAFC6E9"), 0, getHeight() - 50, Color.decode("0x4D5D8F"));
		((Graphics2D) g).setPaint(vertGrad);
		g.fillRoundRect(20, -20, getWidth() - 40, getHeight(), 25, 25);
		
		int posX = (getWidth() - totalObjWidth) / 2;
		homeBtn.setBounds(posX, upperMargin, buttonSize, buttonSize);
		posX += buttonSize + 5;
		clefNoteBtn.setBounds(posX, upperMargin, 160, buttonSize);
		posX += 165;
		tempoContainer.setBounds(posX, upperMargin - 10, 240, 90);
		posX += 245;
		if (isInline == true)
		{
			gameContainer.setBounds(posX, upperMargin, 166, buttonSize);
			posX += 171;			
		}
		else
		{
			refreshBtn.setBounds(posX, upperMargin, buttonSize, buttonSize);
			posX += buttonSize + 5;
		}
		
		playBtn.setBounds(posX, upperMargin, buttonSize, buttonSize);
		posX += buttonSize + 5;

		if (isInline == false)
			listenBtn.setBounds(posX, upperMargin, buttonSize, buttonSize);
	}
}