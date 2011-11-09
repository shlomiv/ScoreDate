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

import java.awt.BasicStroke; 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.ResourceBundle;

import javax.swing.JButton;

public class RoundedButton extends JButton
{
	  private static final long serialVersionUID = 8458705986423151858L;
	  private String bLabel;
	  private ResourceBundle appBundle;
	  
	  Image img = null;
	  
	  public RoundedButton(String label, ResourceBundle b) 
	  {
	    super(label);
	    bLabel = label;
	    appBundle = b;

	    // These statements enlarge the button so that it becomes a square rather than a rectangle.
	    Dimension size = getPreferredSize();
	    size.width = size.height = Math.max(size.width, size.height);
	    setPreferredSize(size);
	  }
	  
	  public void setResBundle(ResourceBundle b)
	  {
		  appBundle = b;
		  repaint();
	  }
	  
	  public void setButtonImage(Image i)
	  {
		  img = i;
	  }

	  // Draw the button
	  protected void paintComponent(Graphics g) 
	  {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Color bgColor;
		Color endColor = Color.decode("0x4D5D8F");
		
		g.setColor(Color.decode("0x5F8DD3"));
		g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
	    if (getModel().isArmed()) // is button being clicked ?
	    	bgColor = Color.decode("0x869EBA");
	    else if (getModel().isRollover()) // rollover effect
	    {
	    	bgColor = Color.decode("0xB8D8FF");
	    	endColor = Color.decode("0x667BBD");
	    }
	    else
	    	bgColor = getBackground(); // normal state

	    // gradient fill: http://www.java2s.com/Code/Java/2D-Graphics-GUI/GradientPaintdemo.htm
	    GradientPaint vertGrad = new GradientPaint(0, 0, bgColor, 0, getHeight(), endColor);
		((Graphics2D) g).setPaint(vertGrad);
		g.fillRoundRect(3, 3, getSize().width-6, getSize().height-6, 27, 27);

	    int textWidth = 0;
	    g.setColor(Color.black);
	    int vOffset = getHeight() / 2;
	    int hOffset = getWidth() / 2;
	    if (bLabel == "RBL_INLINE")
	    {
	    	String title = appBundle.getString("_menuNotereading");
	    	String ss = "" + (char)0xA9 + (char)0xA9 + (char)0xA9 + (char)0xA9; // staff symbol
	    	g.setFont(new Font("Arial", Font.BOLD, 22));
	    	FontMetrics fM1 = g.getFontMetrics();
	    	textWidth = fM1.stringWidth(title);
	    	g.drawString(title, (getSize().width - textWidth) / 2, 50);
	    	g.setFont(getFont().deriveFont(75f));
	    	FontMetrics fM2 = g.getFontMetrics();
	    	textWidth = fM2.stringWidth(ss);
	    	g.drawString(ss, (getSize().width - textWidth) / 2, vOffset + 60);
	    	g.drawString("w", hOffset, vOffset + 24);
	    }
	    else if (bLabel == "RBL_RHYTHM")
	    {
	    	String title = appBundle.getString("_menuRythmreading");
	    	String ss = "" + (char)0xA9 + (char)0xA9 + (char)0xA9 + (char)0xA9; // staff symbol
	    	g.setFont(new Font("Arial", Font.BOLD, 22));
	    	FontMetrics fM1 = g.getFontMetrics();
	    	textWidth = fM1.stringWidth(title);
	    	g.drawString(title, (getSize().width - textWidth) / 2, 50);
	    	g.setFont(getFont().deriveFont(75f));
	    	FontMetrics fM2 = g.getFontMetrics();
	    	textWidth = fM2.stringWidth(ss);
	    	g.drawString(ss, (getSize().width - textWidth) / 2, vOffset + 60);
	    	String rs = "" + (char)0xDA;
	    	g.drawString(rs, hOffset, vOffset + 39);
	    }
	    else if (bLabel == "RBL_SCORE")
	    {
	    	String title = appBundle.getString("_menuScorereading");
	    	String ss = "" + (char)0xA9 + (char)0xA9 + (char)0xA9 + (char)0xA9; // staff symbol
	    	g.setFont(new Font("Arial", Font.BOLD, 20));
	    	FontMetrics fM1 = g.getFontMetrics();
	    	textWidth = fM1.stringWidth(title);
	    	g.drawString(title, (getSize().width - textWidth) / 2, 50);
	    	g.setFont(getFont().deriveFont(75f));
	    	FontMetrics fM2 = g.getFontMetrics();
	    	textWidth = fM2.stringWidth(ss);
	    	g.drawString(ss, (getSize().width - textWidth) / 2, vOffset + 60);
	    	String sm = "" + (char)0xF4;
	    	g.drawString(sm, hOffset - 30, vOffset + 68);
	    	g.drawString(sm, hOffset + 15, vOffset + 38);
	    }
	    else if (bLabel == "RBL_NOTES")
	    {
	    	String titlep1 = appBundle.getString("_menuClef");
	    	String titlep2 = appBundle.getString("_menuNotes");
	    	g.setColor(Color.white);
	    	g.setFont(new Font("Arial", Font.BOLD, 12));
	    	FontMetrics fM = g.getFontMetrics();
	    	textWidth = fM.stringWidth(titlep1) / 2;
	    	g.drawString(titlep1, 15 + 25 - textWidth, vOffset - 10);
	    	fM = g.getFontMetrics();
	    	textWidth = fM.stringWidth("&") / 2;
	    	g.drawString("&", 15 + 25 - textWidth, vOffset + 5);
	    	fM = g.getFontMetrics();
	    	textWidth = fM.stringWidth(titlep2) / 2;
	    	g.drawString(titlep2, 15 + 25 - textWidth, vOffset + 20);
	    	g.fillRoundRect(80, vOffset - 25, 70, 50, 15, 15); // 22
	    	g.setFont(getFont().deriveFont(27f));
	    	String ss = "" + (char)0xA9 + (char)0xA9 + (char)0xA9 + (char)0xA9; // staff symbol
	    	String sm = "" + (char)0xF4;
	    	g.setColor(Color.black);
	    	g.drawString(ss, 87, vOffset + 11);
	    	g.setFont(getFont().deriveFont(35f));
	    	g.drawString("G", 90, vOffset + 12);
	    	g.drawString(sm, 110, vOffset + 18);
	    	g.drawString(sm, 130, vOffset + 11);
	    }
	    else if (bLabel == "RBL_STATS")
	    {
	    	String title = appBundle.getString("_menuStatistics");
	    	g.setFont(new Font("Arial", Font.BOLD, 20));
	    	FontMetrics fM1 = g.getFontMetrics();
	    	textWidth = fM1.stringWidth(title);
	    	g.drawString(title, (getSize().width - textWidth) / 2, 50);
	    	
	    	((Graphics2D) g).setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND  ));
	    	g.drawLine(hOffset - 60, vOffset - 10, hOffset - 60, vOffset + 60);
	    	g.drawLine(hOffset - 65, vOffset + 53, hOffset + 70, vOffset + 53);
	    	
	    	g.drawLine(hOffset - 39, vOffset + 40, hOffset - 10, vOffset + 20);
	    	g.drawLine(hOffset - 10, vOffset + 20, hOffset + 10, vOffset + 30);
	    	g.drawLine(hOffset + 10, vOffset + 30, hOffset + 40, vOffset - 5);
	    	
	    }
	    else if (bLabel == "RBL_LESSONS")
	    {
	    	String title = appBundle.getString("_menuLessons");
	    	g.setFont(new Font("Arial", Font.BOLD, 20));
	    	FontMetrics fM1 = g.getFontMetrics();
	    	textWidth = fM1.stringWidth(title);
	    	g.drawString(title, (getSize().width - textWidth) / 2, 50);
	    	
	    	((Graphics2D) g).setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND  ));
	    	g.drawRoundRect(hOffset - 50, vOffset - 10, 100, 65, 10, 10);
	    	g.setFont(new Font("Arial", Font.BOLD, 25));
	    	g.drawString("A", hOffset - 30, vOffset + 30);
	    	g.drawString("B", hOffset - 5, vOffset + 20);
	    	g.drawString("C", hOffset + 20, vOffset + 40);
	    }
	    
	    if (img != null)
	    {
	    	g.drawImage(img, (getWidth() - img.getWidth(null)) / 2, (getHeight() - img.getHeight(null)) / 2 , null);
	    }
	  }
}
