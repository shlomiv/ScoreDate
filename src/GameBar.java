import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField; 

public class GameBar extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 6433532306226295481L;
	ResourceBundle appBundle;
	Preferences appPrefs;
	Font appFont;

	public GradientBar progress;
	private JLabel scoreLabel;
	public JTextField scoreCnt;
	private JLabel precisionLabel;
	public JTextField precisionCnt;
	
	int totalObjWidth = 670;
	int upperMargin = 7;

	public GameBar (Dimension d, ResourceBundle b, Font f, Preferences p)
	{
		appBundle = b;
		appFont = f;
		appPrefs = p;
		setSize(d);
		setLayout(null);

		int posX = (d.width - totalObjWidth) / 2;
		
		progress = new GradientBar(0, 100);
		progress.setPreferredSize(new Dimension(200, 27));
		progress.setValue(0);
		progress.setBounds(posX, upperMargin, 200, 27);
		posX += 210;
		
		Font scf = new Font("Arial", Font.BOLD, 20);
		scoreLabel = new JLabel(appBundle.getString("_gameScore"), null, JLabel.RIGHT);
		scoreLabel.setFont(scf);
		scoreLabel.setBounds(posX, upperMargin, 150, 30);
		posX += 160;

		scoreCnt = new JTextField("0");
		scoreCnt.setPreferredSize(new Dimension(80, 30));
		scoreCnt.setEditable(false);
		scoreCnt.setFont(scf);
		scoreCnt.setBounds(posX, upperMargin, 80, 30);
		posX += 90;
		
		precisionLabel = new JLabel(appBundle.getString("_gamePrecision"), null, JLabel.RIGHT);
		precisionLabel.setFont(scf);
		precisionLabel.setPreferredSize(new Dimension(120, 30));
		precisionLabel.setBounds(posX, upperMargin, 120, 30);
		posX += 130;

		precisionCnt = new JTextField("0%");
		precisionCnt.setPreferredSize(new Dimension(80, 30));
		precisionCnt.setEditable(false);
		precisionCnt.setFont(scf);
		precisionCnt.setBounds(posX, upperMargin, 80, 30);

		add(progress);
		add(scoreLabel);
		add(scoreCnt);
		add(precisionLabel);
		add(precisionCnt);
	}
	
	public void updateLanguage(ResourceBundle bundle)
	{
		appBundle = bundle;
		scoreLabel.setText(appBundle.getString("_gameScore"));
		precisionLabel.setText(appBundle.getString("_gamePrecision"));
	}

	public void actionPerformed(ActionEvent ae)
	{
		
	}
	
	protected void paintComponent(Graphics g) 
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		GradientPaint vertGrad = new GradientPaint(0, 0, Color.decode("0xE7E734"), 0, getHeight() - 30, Color.decode("0xE7A935"));
		((Graphics2D) g).setPaint(vertGrad);
		g.fillRoundRect(20, 0, getWidth() - 40, getHeight()+ 15, 15, 15);
		
		int posX = (getWidth() - totalObjWidth) / 2;
		progress.setBounds(posX, upperMargin, 200, 27);
		posX += 210;
		scoreLabel.setBounds(posX, upperMargin, 150, 30);
		posX += 160;
		scoreCnt.setBounds(posX, upperMargin, 80, 30);
		posX += 90;
		precisionLabel.setBounds(posX, upperMargin, 120, 30);
		posX += 130;
		precisionCnt.setBounds(posX, upperMargin, 80, 30);
	}
}

