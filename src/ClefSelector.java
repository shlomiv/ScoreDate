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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JLabel;


public class ClefSelector extends JPanel implements MouseListener
{
	private static final long serialVersionUID = -3872352788125977616L;
	
	ResourceBundle appBundle;
	String clefSymbol;
	JLabel clefText;
	JLabel disabledText;
	boolean enabled = false;
	int lowerLevel = 0;
	int higherLevel = 0;
	
	public ClefSelector(ResourceBundle b, String s)
	{
		appBundle = b;
		clefSymbol = s;
		setLayout(null);
		Font arial = new Font("Arial", Font.BOLD, 15);
		
		if (clefSymbol == "G")
			clefText = new JLabel(appBundle.getString("_trebleclef"), null, JLabel.CENTER);
		else if (clefSymbol == "?")
			clefText = new JLabel(appBundle.getString("_bassclef"), null, JLabel.CENTER);
		else if (clefSymbol == "ALTO")
			clefText = new JLabel(appBundle.getString("_altoclef"), null, JLabel.CENTER);
		else if (clefSymbol == "TENOR")
			clefText = new JLabel(appBundle.getString("_tenorclef"), null, JLabel.CENTER);
		 
		clefText.setFont(arial);
		clefText.setForeground(Color.lightGray);
		clefText.setPreferredSize(new Dimension(140, 40));
		clefText.setBounds(15, 10, 140, 40);
		
		disabledText = new JLabel(appBundle.getString("_clefDisabled"));
		disabledText.setFont(arial);
		disabledText.setForeground(Color.lightGray);
		disabledText.setPreferredSize(new Dimension(140, 140));
		disabledText.setBounds(65, 25, 140, 140);

		add(clefText);
		add(disabledText);

		addMouseListener(this);
	}
	
	public void setEnabled(boolean set)
	{
		enabled = set;
		clefText.setVisible(!enabled);
		disabledText.setVisible(!enabled);
		repaint();
	}
	
	public void setLevels(int low, int high)
	{
		lowerLevel = low;
		higherLevel = high;
		repaint();
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}

	public int getLowerLevel()
	{
		return lowerLevel;
	}
	
	public int getHigherLevel()
	{
		return higherLevel;
	}
	
	public void mouseClicked(MouseEvent e) 
	{
		//System.out.println("Mouse clicked (# of clicks: " + e.getClickCount() + ")");
		System.out.println("X pos: " + e.getX() + ", Y pos: " + e.getY());
		if (e.getX() < 50)
		{
			enabled = !enabled;
			clefText.setVisible(!enabled);
			disabledText.setVisible(!enabled);
			repaint();
			return;
		}
		else
		{
			if (enabled == false)
				return;
		}

		if (e.getY() > 9 && e.getY() < 189)
		{
			int relYpos = e.getY() - 14;
			int level = (relYpos / 7);
			System.out.println("[ClefSelector] New level = " + level);
			
			if (e.getX() < 90)
			{
				if (level < higherLevel)
					higherLevel = level;
				else
					lowerLevel = level;
			}
			else
			{
				if (level > lowerLevel)
					lowerLevel = level;
				else
					higherLevel = level;
			}
		}
			
		repaint();
	}

	public void mousePressed(MouseEvent e) 
	{
		//System.out.println("Mouse pressed; # of clicks: " + e.getClickCount());
	}

    public void mouseReleased(MouseEvent e) 
    {
    	//System.out.println("Mouse released; # of clicks: " + e.getClickCount());
    }

    public void mouseEntered(MouseEvent e) 
    {
    	//System.out.println("Mouse entered");
    }

    public void mouseExited(MouseEvent e) 
    {
    	//System.out.println("Mouse exited");
    }

	protected void paintComponent(Graphics g) 
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Color fc;
		if (enabled == false)
			fc = Color.lightGray;
		else
			fc = Color.black;
		g.setColor(fc);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
		g.setColor(Color.white);
		g.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 20, 20);
		g.setColor(fc);
		
		if (clefSymbol == "ALTO")
		{
			g.setFont(getFont().deriveFont(73f));
			g.drawString("" + (char)0xBF, 15, 132);
		}
		else if (clefSymbol == "TENOR")
		{
			g.setFont(getFont().deriveFont(73f));
			g.drawString("" + (char)0xBF, 15, 118);
		}
		else
		{
			g.setFont(getFont().deriveFont(80f));
			g.drawString(clefSymbol, 15, 128);
		}
		if (enabled == true)
		{
			g.setFont(getFont().deriveFont(68f));
			String ss = "" + (char)0xA9 + (char)0xA9 + (char)0xA9 + (char)0xA9; // staff symbol
			g.drawString(ss, 15, 128);
			
			int ypos = 143;
			for (int i = 0; i < 4; i++, ypos+=14) // draw 3 additional lines below
				g.fillRect(70, ypos, 32, 2);
			ypos = 59;
			for (int i = 0; i < 4; i++, ypos-=14) // draw 3 additional lines above
				g.fillRect(100, ypos, 32, 2);
			
			g.drawString("w", 75, 25 + (lowerLevel * 7));
			g.drawString("w", 105, 25 + (higherLevel * 7));
		}
			
	}
}
