import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import java.awt.RenderingHints;

import javax.swing.JProgressBar;

public class GradientBar extends JProgressBar
{
	private static final long serialVersionUID = 5027733950216091052L;

	public GradientBar(int min, int max)
	{
		super(min, max);
		
	}

	protected void paintComponent(Graphics g)
    {
		super.paintComponent(g);
	    Color color1 = new Color(255, 0, 0, 85);
	    Color color2 = new Color(0, 255, 0, 85);
	    int w = ((getWidth() * this.getValue()) / 100);
	    int h = getHeight();
	    Graphics2D g2d = (Graphics2D) g;
	    GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
	    g2d.setPaint(gp);
	    g2d.fillRect(0, 0, w, h);
	  }

}
