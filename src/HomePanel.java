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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class HomePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	Font appFont;
	private ResourceBundle appBundle;
	private BufferedImage MainPic = null;
	public RoundedButton inlineBtn, rhythmBtn, scoreBtn, statsBtn, lessonsBtn, earTrainBtn;
	public JPanel homeButtons;
	private int btnWidth = 220;
	private int logoHeight = 160;

	public HomePanel(Font f, ResourceBundle b, Dimension d) 
	{
		appFont = f;
		appBundle = b;
        try 
        {
    		MainPic = ImageIO.read(getClass().getResourceAsStream("resources/logo.png"));
        }
        catch(Exception e)
        {
            System.out.println("Cannot load logo image");
        }
        btnWidth = (d.width / 3) - 30;
        System.out.println("Buttons width = "+ btnWidth);

	    //setLayout(new BoxLayout( this, BoxLayout.Y_AXIS ) );
        //setLayout(null);
		inlineBtn = new RoundedButton("RBL_INLINE", appBundle);
		inlineBtn.setFont(appFont);
		inlineBtn.setBackground(Color.decode("0xAFC6E9"));
		inlineBtn.setPreferredSize(new Dimension(btnWidth, 300));
	    rhythmBtn = new RoundedButton("RBL_RHYTHM", appBundle);
	    rhythmBtn.setFont(appFont);
	    rhythmBtn.setBackground(Color.decode("0xAFC6E9"));
	    rhythmBtn.setPreferredSize(new Dimension(btnWidth, 300));
	    scoreBtn = new RoundedButton("RBL_SCORE", appBundle);
	    scoreBtn.setFont(appFont);
	    scoreBtn.setBackground(Color.decode("0xAFC6E9"));
	    scoreBtn.setPreferredSize(new Dimension(btnWidth, 300));
	    statsBtn = new RoundedButton("RBL_STATS", appBundle);
	    statsBtn.setFont(appFont);
	    statsBtn.setBackground(Color.decode("0xAFC6E9"));
	    statsBtn.setPreferredSize(new Dimension(btnWidth, 300));
	    lessonsBtn = new RoundedButton("RBL_LESSONS", appBundle);
	    lessonsBtn.setFont(appFont);
	    lessonsBtn.setBackground(Color.decode("0xAFC6E9"));
	    lessonsBtn.setPreferredSize(new Dimension(btnWidth, 300));
	    earTrainBtn = new RoundedButton("RBL_EARTRAIN", appBundle);
	    earTrainBtn.setFont(appFont);
	    earTrainBtn.setBackground(Color.decode("0xAFC6E9"));
	    earTrainBtn.setPreferredSize(new Dimension(btnWidth, 300));

	    homeButtons = new JPanel();
	    homeButtons.setLayout(null);
	    homeButtons.setBackground(Color.white);
	    homeButtons.setPreferredSize(new Dimension(d.width, d.height - logoHeight));
	    homeButtons.setBounds(0, logoHeight, d.width, d.height - logoHeight);
	    homeButtons.add(inlineBtn);
	    homeButtons.add(rhythmBtn);
	    homeButtons.add(scoreBtn);
	    homeButtons.add(statsBtn);
	    homeButtons.add(lessonsBtn);
	    homeButtons.add(earTrainBtn);
	    
	    add(homeButtons);
	}

	protected void paintComponent(Graphics g) 
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//System.out.println("[paintComponent] width = "+ this.getWidth());
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		btnWidth = (this.getWidth() / 3) - 26;
		//System.out.println("Buttons width = "+ btnWidth);
		g.drawImage(MainPic, (this.getWidth() / 2) - 300, 10, null);
		homeButtons.setBounds(0, logoHeight, this.getWidth(), this.getHeight() - logoHeight);
		int btnHeight = (this.getHeight() - logoHeight) / 2 - 16;
		inlineBtn.setBounds(30, 10, btnWidth, btnHeight);
		rhythmBtn.setBounds(40 + btnWidth, 10, btnWidth, btnHeight);
		scoreBtn.setBounds(50 + (btnWidth * 2), 10, btnWidth, btnHeight);
		//statsBtn.setBounds(30 + (btnWidth/2), 20 + btnHeight, btnWidth, btnHeight);
		//lessonsBtn.setBounds(40 + (int)(btnWidth*1.5), 20 + btnHeight, btnWidth, btnHeight);
		statsBtn.setBounds(30, 20 + btnHeight, btnWidth, btnHeight);
		lessonsBtn.setBounds(40 + btnWidth, 20 + btnHeight, btnWidth, btnHeight);
		earTrainBtn.setBounds(50 + (btnWidth * 2), 20 + btnHeight, btnWidth, btnHeight);
	}
}
