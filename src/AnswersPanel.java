import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class AnswersPanel extends JPanel  
{
	private static final long serialVersionUID = 448119751116801373L;

	private boolean showCursorAndBeats = false;

	int currWidth = 0, currHeight = 0;
	BufferedImage bImage = null;
	
	Image rightImg = null;
	Image wrongImg = null;
	Image warnImg = null;

	int cursorXpos = -1;
	int cursorYpos = -1;
	
	public AnswersPanel()
	{
		
		rightImg = new ImageIcon(getClass().getResource("/resources/correct.png")).getImage();
		wrongImg = new ImageIcon(getClass().getResource("/resources/wrong.png")).getImage();
		warnImg = new ImageIcon(getClass().getResource("/resources/warning.png")).getImage();
		checkSurface();
	}
	
	private void checkSurface()
	{
		int tmpWidth = getWidth();
		int tmpHeight = getHeight();
		if (tmpWidth != currWidth || tmpHeight != currHeight)
		{
			System.out.println("answersLayer size changed !!");
			currWidth = tmpWidth;
			currHeight = tmpHeight;
			bImage = new BufferedImage(currWidth, currHeight, BufferedImage.TYPE_INT_ARGB);
		}
	}
	
	public void clearSurface()
	{
		if (bImage == null)
			return;
		Graphics2D g2d = bImage.createGraphics();
		g2d.setBackground(new Color(255, 255, 255, 0));
		g2d.clearRect(0, 0, currWidth, currHeight);
		g2d.dispose();
		cursorXpos = -1;
		cursorYpos = -1;
	}

    public void enableCursor(boolean on)
    {
    	showCursorAndBeats = on;
    }

    public void drawCursor(int x, int y, boolean clean)
    {
    	if (showCursorAndBeats == false || x < 0 || y < 0)
    		return;
    	Graphics2D g2d = bImage.createGraphics();

    	if (clean == false)
    		g2d.setColor(Color.orange);
    	else
    		g2d.setColor(Color.white);
    	g2d.fillRect(0, y, x, 3);
    	g2d.dispose();
    	this.repaint();
    }

    public void drawMetronome(int x, int y)
    {
    	if (showCursorAndBeats == false)
    		return;
    	Graphics2D g2d = bImage.createGraphics();
		g2d.setColor(Color.black);
		g2d.fillRect(x + 2, y - 8, 5, 8);
		g2d.dispose();
    	this.repaint();
    }
    
    public void drawAnswer(int type, int x, int y)
    {
    	Graphics2D g2d = bImage.createGraphics();
    	if (type == 0)
    		g2d.drawImage(wrongImg, x, y, null);
    	else if (type == 1)
    		g2d.drawImage(rightImg, x, y, null);
    	else if (type == 2)
    		g2d.drawImage(warnImg, x, y, null);
    	g2d.dispose();
    	this.repaint();
    }

	protected void paintComponent(Graphics g) 
 	{
		super.paintComponent(g);
		checkSurface();
		g.drawImage(bImage, 0, 0, null);		
 	}
}
