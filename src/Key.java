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
	final int ON = 0, OFF = 1;
    int noteState = OFF;
    boolean is_black = false;
    int pitch;
    int noteIndex; // holds the idex of the seven note scale (A, B, C, D, E, F, G)
    int offx; // just for black keys since they have an irregular pattern

    public Key(int x, int y, int w, int h, int kpitch, int idx, int offset, boolean black) 
    {
      pitch = kpitch;
      is_black = black;
      noteIndex = idx;
      offx = offset;
      setBounds(x + offx, y, w, h);
      setPreferredSize(new Dimension(w, h));
      if (black == true)
    	  setBackground(Color.black);
      else
    	  setBackground(Color.white); 
    }

    public int GetPitch()
    {
      return this.pitch;
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

    public boolean isNoteOn() {
      return noteState == ON;
    }

    public void setNoteState(int state) {
      noteState = state;
    }

    protected void paintComponent(Graphics g) 
    {
    	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   		//System.out.println("Draw Key: size = " + getSize().width);
    	
    	if (is_black == false)
    	{
    		g.setColor(Color.black);
    		g.drawRoundRect(0, -7, getSize().width-1, getSize().height, 7, 7);
    	}

    	if (getModel().isArmed()) // is button being clicked ?
  	      g.setColor(Color.decode("0x869EBA"));
  	    else if (getModel().isRollover()) // rollover effect
  	      g.setColor(Color.decode("0xB8D8FF"));
  	    else
  	      g.setColor(getBackground()); // normal state

    	if (is_black == false)
    		g.fillRoundRect(1, -6, getSize().width-2, getSize().height-1, 6, 6);
    	else
    		g.fillRect(0, 0, getSize().width, getSize().height);
    	g.setColor(Color.black);
    	g.drawLine(0,0,getSize().width,0);
	}
}
