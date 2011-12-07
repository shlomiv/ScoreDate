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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ResourceBundle;


/**
 * @author Massimo Callegari
 *
 */
public class Accidentals {
	
	Preferences appPrefs;
	private String type;
	private int amount;
	
	public Accidentals(String t, int count, Preferences p) 
	{
		appPrefs = p;
		type = t;
		amount = count;
	}

	public void setTypeAndCount(String t, int count)
	{
		type = t;
		amount = count;
		//System.out.println("[Accidentals - setTypeAndCount] type: " + type + ", count: " + count);
	}

	public int getNumber()
	{
		return amount;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getTonality(ResourceBundle bundle)
	{
		String tStr = "";

	    String DO = bundle.getString("_do");
	    String RE = bundle.getString("_re");
	    String MI = bundle.getString("_mi");
	    String FA = bundle.getString("_fa");
	    String SOL = bundle.getString("_sol");
	    String LA = bundle.getString("_la");
	    String SI = bundle.getString("_si");

	    if (amount == 0)
	    	tStr = DO + " Maj | " + LA + " min";
	    else if (amount == 1)
	    {
	      if (type == "#")
	    	tStr = SOL + " Maj | " + MI + " min";
	      else
	    	tStr = FA + " Maj | " + RE + " min";
	    }
	    if (amount == 2)
	    {
	      if (type == "#")
	    	tStr = RE + " Maj | " + SI + " min";
	      else
	    	tStr = SI + "b Maj | " + SOL + " min";
	    }
	    if (amount == 3)
	    {
	      if (type == "#")
	    	tStr = LA + " Maj | " + FA + "# min";
	      else
	    	tStr = MI + "b Maj | " + DO + " min";
	    }
	    if (amount == 4)
	    {
	      if (type == "#")
	    	tStr = MI + " Maj | " + DO + "# min";
	      else
	    	tStr = LA + "b Maj | " + FA + " min";
	    }
	    if (amount == 5)
	    {
	      if (type == "#")
	    	tStr = SI + " Maj | " + SOL + "# min";
	      else
	    	tStr = RE + "b Maj | " + SI + "b min";
	    }
	    if (amount == 6)
	    {
	      if (type == "#") 
	    	tStr = FA + "# Maj | " + RE + "# min";
	      else
	        tStr = SOL + "b Maj | " + MI + "b min";
	    }
	    if (amount == 7)
	    {
	      if (type == "#")
	    	tStr = DO + "# Maj | " + LA + "# min";
	      else
	    	tStr = DO + "b Maj | " + LA + "b min";
	    }

	    return tStr;
	}

    private void drawAlteration(Graphics g, Font f, int x, int y, String altType) 
    {
    	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.black);
		g.setFont(f.deriveFont(54f));
		if (altType == "B")
			g.drawString(altType, x+2, y + 21);
		else
			g.drawString(altType, x+2, y + 22);
	}

	public void paint(Graphics g, Font f, int xPos, int yPos, int clefMask) 
	{
	    String sharp = "B"; // # alteration
	    String flat = "b"; // b alteration
	    int clefOffset = 0;
	    
	    if (clefMask == appPrefs.BASS_CLEF)
	    	clefOffset = 10;
	    else if (clefMask == appPrefs.ALTO_CLEF)
	    	clefOffset = 5;
	    else if (clefMask == appPrefs.TENOR_CLEF)
	    	clefOffset = -5;

	    if (type.equals("#")) 
	    {
	      if (amount >= 1) // FA#
	        drawAlteration(g, f, xPos, yPos - 15 + clefOffset, sharp);
	      if (amount >= 2) // DO#
   	  		drawAlteration(g, f, xPos + 10, yPos + clefOffset, sharp);
	      if (amount >= 3) // SOL#
	      {
	    	if (clefMask == appPrefs.TENOR_CLEF)
	    		drawAlteration(g, f, xPos + 20, yPos + clefOffset + 15, sharp); // shift an octave down
	    	else	    	  
	        	drawAlteration(g, f, xPos + 20, yPos - 20 + clefOffset, sharp);
	      }
	      if (amount >= 4) // RE#
	        drawAlteration(g, f, xPos + 30, yPos - 5 + clefOffset, sharp);
	      if (amount >= 5) // LA#
	        drawAlteration(g, f, xPos + 40, yPos + 10 + clefOffset, sharp);
	      if (amount >= 6) // MI#
	      {
	    	if (clefMask == appPrefs.TENOR_CLEF)
	    		drawAlteration(g, f, xPos + 50, yPos + 25 + clefOffset, sharp);
	    	else
	    		drawAlteration(g, f, xPos + 50, yPos - 10 + clefOffset, sharp);
	      }
	      if (amount >= 7) // SI#
	        drawAlteration(g, f, xPos + 60, yPos + 5 + clefOffset, sharp);
	    }

	    if (type.equals("b"))
	    {
	      if (amount >= 1) // SIb
	        drawAlteration(g, f, xPos, yPos + 5, flat);
	      if (amount >= 2) // MIb
	        drawAlteration(g, f, xPos + 9, yPos - 10, flat);
	      if (amount >= 3) // LAb
	        drawAlteration(g, f, xPos + 18, yPos + 10, flat);
	      if (amount >= 4) // REb
	        drawAlteration(g, f, xPos + 27, yPos - 5, flat);
	      if (amount >= 5) // SOLb
	        drawAlteration(g, f, xPos + 36, yPos + 15, flat);
	      if (amount >= 6) // DOb
	        drawAlteration(g, f, xPos + 45, yPos, flat);
	      if (amount >= 7) // FAb
	        drawAlteration(g, f, xPos + 54, yPos + 20, flat);
	    }
	  }
}
