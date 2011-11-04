import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class NotesPanel extends JPanel 
{
	private static final long serialVersionUID = -1735923156425027329L;
	Font appFont;
	Preferences appPrefs;
	private Vector<Note> notes;
	
    private int clefMask = 1;
    private Vector<Integer> clefs = new Vector<Integer>();    
	private int rowsDistance = 90; // distance in pixel between staff rows
	private int noteDistance = 72; // distance in pixel between 1/4 notes
	private int firstNoteXPos = 50;

	private int staffWidth;
	
	private boolean inlineMode = false;
	private int singleNoteIndex = -1;
	private boolean showCursorAndBeats = false;
	
	private JLabel learningText;
	Image rightImg = null;
	Image wrongImg = null;
	Image warnImg = null;
	
	public NotesPanel(Font f, Preferences p, Vector<Note> n, boolean inline)
	{
		appFont = f;
		appPrefs = p;
		notes = n;
		inlineMode = inline;
		
		rightImg = new ImageIcon(getClass().getResource("/resources/correct.png")).getImage();
		wrongImg = new ImageIcon(getClass().getResource("/resources/wrong.png")).getImage();
		warnImg = new ImageIcon(getClass().getResource("/resources/warning.png")).getImage();
		
		learningText = new JLabel("", null, JLabel.CENTER);
		learningText.setForeground(Color.decode("0x869EBA"));
		learningText.setVisible(false);
		
		setLayout(null);
		
		add(learningText);
		//setDoubleBuffered(true);
    	//setBackground(Color.blue);
	}

    public void setRowsDistance(int dist)
    {
    	rowsDistance = dist;
    }

    public void setClef(int type)
    {
    	clefMask = type;
    	clefs.clear();

    	if ((clefMask & appPrefs.TREBLE_CLEF) > 0) clefs.add(appPrefs.TREBLE_CLEF);
    	if ((clefMask & appPrefs.BASS_CLEF) > 0) clefs.add(appPrefs.BASS_CLEF);
    	if ((clefMask & appPrefs.ALTO_CLEF) > 0) clefs.add(appPrefs.ALTO_CLEF);
    	if ((clefMask & appPrefs.TENOR_CLEF) > 0) clefs.add(appPrefs.TENOR_CLEF);

		Font ltf = new Font("Arial", Font.BOLD, 30);
		learningText.setPreferredSize( new Dimension(200, 50));
		learningText.setBounds((getWidth() / 2) - 150, getHeight() - 60, 300, 50);
		learningText.setText("");
		learningText.setFont(ltf);
		
    	repaint();
    }
    
    public void setFirstNoteXPosition(int xpos)
    {
    	firstNoteXPos = xpos;
    }
    
    public void setStaffWidth(int w)
    {
    	staffWidth = w;
    }
    
    public void setNotesPositions()
    {
    	int tmpX = firstNoteXPos;
    	int tmpY = 0;
    	
    	for (int i = 0; i < notes.size(); i++)
    	{
    		Note note = notes.get(i);
    		int type = note.type;
       		int ypos = (note.level * 5) + 11;
       		int yOffset = 0;
    		
    		if (tmpX >= staffWidth)
    		{
    			tmpX = firstNoteXPos;
    			tmpY += rowsDistance;
    		}
    		
    		if (note.secondRow == true)
    			yOffset += (rowsDistance / 2);
    		
    		if (note.level < 5)
    		{
    			note.addLinesNumber = 3 - (note.level / 2);
    			note.addLinesYpos = ypos + tmpY - 6 + ((note.level%2) * 5); 
    		}
    		else if  (note.level > 15)
    		{
    			note.addLinesNumber = (note.level / 2) - 7;
    			note.addLinesYpos = ypos + tmpY - 6 - ((note.level - 16) * 5); 
    		}
    		
    		if (type == 0) // whole note
    			ypos++;
    		else if (type == 2) // quarter note
    		{
    			if (note.level <= 10)
    				ypos += 41;
    		}
    		else if (type == 3) // eigth note
    		{
    			if (note.level <= 10) 
    				ypos += 30;
    		}
    		else if (type == 4) // quarter note
    		{
    			if (note.level <= 10)
    				ypos += 41;
    		}    		
    		else if (type == 5) // silence
    		{
    			if (note.duration == 4)
    				ypos -= 16;
    			else if (note.duration == 2)
    				ypos -= 12;
    			else if (note.duration == 1)
    				ypos += 13;
    			else if (note.duration == 0.5)
    				ypos += 13;
    		}
    		
    		notes.get(i).ypos = ypos + tmpY + yOffset;
    		if (inlineMode == false) // the inline game controls X position itself
    		{
    			notes.get(i).xpos += tmpX;
    			tmpX += (note.duration * noteDistance);
    		}
    		System.out.println("[Note: #" + i + "] type: " + notes.get(i).type + ", xpos: " + notes.get(i).xpos + ", ypos: " + notes.get(i).ypos);
    	}
    }
    
    public void setLearningTips(String tip, boolean enable)
    {
    	if (enable == true)
    		learningText.setText(tip);
    	
   		learningText.setVisible(enable); 	
    }

    public void highlightNote(int index, boolean enable)
    {
    	singleNoteIndex = index;
    	notes.get(index).highlight = enable;
    	repaint();
    	singleNoteIndex = -1;
    }
    
    public void enableCursor(boolean on)
    {
    	showCursorAndBeats = on;
    }
    
    public void drawCursor(int x, int y, boolean clean)
    {
    	if (showCursorAndBeats == false)
    		return;
    	Graphics g = this.getGraphics();
    	if (clean == false)
    		g.setColor(Color.orange);
    	else
    		g.setColor(Color.white);
    	g.fillRect(0, y, x, 3);
    }
    
    public void drawMetronome(int x, int y)
    {
    	if (showCursorAndBeats == false)
    		return;
    	Graphics g = this.getGraphics();
    	g.setColor(Color.black);
    	g.fillRect(x + 2, y - 8, 5, 8);
    }
    
    public void drawAnswer(int type, int x, int y)
    {
    	Graphics g = this.getGraphics();
    	if (type == 0)
    		g.drawImage(wrongImg, x, y, null);
    	else if (type == 1)
    		g.drawImage(rightImg, x, y, null);
    	else if (type == 2)
    		g.drawImage(warnImg, x, y, null);
    }
    
    private void drawNote(Graphics g, int index) 
    {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	String symbol = "";
		Note note = notes.get(index);
		int type = note.type;

		if (note.highlight == true)
	    	g.setColor(Color.blue);
		else 
			g.setColor(Color.black);

		// draw additional lines if needed
		if (note.addLinesNumber > 0)
		{
			int addLineWidth = (note.type == 0)?23:16;
			for (int j = 0; j < note.addLinesNumber; j++)
				g.drawLine(note.xpos - 5, note.addLinesYpos + (j * 10), note.xpos + addLineWidth, note.addLinesYpos + (j * 10));
		}
		
    	g.setFont(appFont.deriveFont(57f));
		if (type == 0) symbol = "w"; // whole note
		else if (type == 1) symbol = "h"; // half note 
		else if (type == 2)
		{
			if (note.level > 10) symbol = "q"; // quarter note upward
			else symbol = "" + (char)0xF6; // quarter note downward
		}
		else if (type == 3)
		{
			if (note.level > 10) symbol = "" + (char)0xC8; // eighth note upward 
			else symbol = "" + (char)0xCA; // eighth note downward
		}
		else if (type == 4)
		{
			if (note.tripletValue > 0)
				symbol = "q"; // quarter note upward
			else symbol = "" + (char)0xF6; // quarter note downward
		}
		else if (type == 5) // silence
		{
			if (note.duration == 4)
				g.fillRect(note.xpos + (int)(noteDistance * 1.55), note.ypos, 14, 6);
			else if (note.duration == 2)
				g.fillRect(note.xpos, note.ypos, 14, 6);
			else if (note.duration == 1)
				symbol = "Q";
			else if (note.duration == 0.5)
			{
				g.setFont(appFont.deriveFont(50f));
				symbol = "E";
			}
		}

		g.drawString(symbol, note.xpos, note.ypos);
		if (note.altType != 0)
		{
			g.setFont(appFont.deriveFont(50f));
			if (note.altType == -1)
				g.drawString("b", note.xpos - 14, note.ypos);
			else
				g.drawString("B", note.xpos - 14, note.ypos);
		}

    	if (note.tripletValue != 0) // draw triplets special graphics 
    	{
    		int tsub = 0; 
    		if (note.tripletValue < 0) // notes downward
    		{
    			if (note.tripletValue <= -1000) tsub = 1000;
    			int tripletBarYPos = note.ypos + (((Math.abs(note.tripletValue) - tsub) - note.level) * 5);
    			g.drawLine(note.xpos, note.ypos - 15, note.xpos, tripletBarYPos - 15);
    			if (note.tripletValue > -1000)
    			{
    				g.setFont(new Font("Arial", Font.BOLD, 15));
            		g.drawString("3", note.xpos + 20, tripletBarYPos + 5);
    				g.fillRect(note.xpos, tripletBarYPos - 15, 49, 5);
    			}
    		}
    		else // notes upward
    		{
    			if (note.tripletValue >= 1000) tsub = 1000;
    			int tripletBarYPos = note.ypos - ((note.level - (note.tripletValue - tsub)) * 5);
    			g.drawLine(note.xpos + 11, note.ypos - 40, note.xpos + 11, tripletBarYPos - 40);
    			if (note.tripletValue < 1000)
    			{
    				g.setFont(new Font("Arial", Font.BOLD, 15));
            		g.drawString("3", note.xpos + 30, tripletBarYPos - 45);
    				g.fillRect(note.xpos + 11, tripletBarYPos - 40, 49, 5);
    			}
    		}
    	}
    }

	protected void paintComponent(Graphics g) 
 	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//super.paintComponent(g);
    	
    	if (singleNoteIndex == -1)
    	{
        	g.setColor(Color.black);
    		for (int i = 0; i < notes.size(); i++)
    		{
    			drawNote(g, i);
    		}
    	}
    	else
    		drawNote(g, singleNoteIndex);
 	}
}
