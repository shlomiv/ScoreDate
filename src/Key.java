/***********************************************
This file is part of the ScoreDate project

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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;

import javax.swing.JButton;

public class Key extends JButton
{
	private static final long serialVersionUID = 8886689336934022704L;

    boolean is_black = false;
    boolean is_highlighted = false;
    int pitch;
    int octave;
    int noteIndex; // holds the index of the seven note scale (C, D, E, F, G, A, B)
    int offx; // just for black keys since they have an irregular pattern

    public Key(int x, int y, int w, int h, int kpitch, int koctave, int idx, int offset, boolean black) 
    {
      pitch = kpitch;
      octave = koctave;
      is_black = black;
      is_highlighted = false;
      noteIndex = idx;
      offx = offset;
      setBounds(x + offx, y, w, h);
      setPreferredSize(new Dimension(w, h));
      if (black == true)
    	  setBackground(Color.black);
      else
    	  setBackground(Color.white); 
    }
    
    public void highlight(boolean on, boolean learning)
    {
    	is_highlighted = on;
    	if (on == true)
    	{
    		if (learning == false)
    			setBackground(Color.decode("0x98FB98"));
    		else
    		{
    			setBackground(Color.decode("0xE7A935"));
    			is_highlighted = false;
    		}
    	}
		else if (is_black == true)
			setBackground(Color.black);
		else
			setBackground(Color.white); 
    }
    
    public void setPressed(boolean on)
    {
    	if (on == true)
    		setBackground(Color.decode("0xB8D8FF"));
    	else
    	{
    		if (is_highlighted == true)
    			setBackground(Color.decode("0x98FB98"));
    		else if (is_black == true)
    			setBackground(Color.black);
    		else
    			setBackground(Color.white); 
    	}
    }
    
    public void setXpos(int newx)
    {
    	Rectangle b = getBounds();
    	b.x = newx;
    	setBounds(b);
    }

    public int getXoffset(){
      return this.offx;
    }


    protected void paintComponent(Graphics g) 
    {
    	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   		//System.out.println("Draw Key: size = " + getSize().width);
    	
    	g.setColor(Color.black);
    	if (is_black == false)
    		g.drawRoundRect(0, -7, getSize().width-1, getSize().height, 7, 7);
    	else
    		g.drawRect(0, -1, getSize().width-1, getSize().height-6);
    		

    	if (getModel().isArmed()) // is button being clicked ?
  	      g.setColor(Color.decode("0x869EBA"));
  	    else if (getModel().isRollover()) // rollover effect
  	      g.setColor(Color.decode("0xB8D8FF"));
  	    else
  	      g.setColor(getBackground()); // normal state

    	if (is_black == false)
    		g.fillRoundRect(1, -6, getSize().width-2, getSize().height-1, 6, 6);
    	else
    		g.fillRect(1, 0, getSize().width-2, getSize().height-7);
    	g.setColor(Color.black);
    	g.drawLine(0,0,getSize().width,0);
	}
}
