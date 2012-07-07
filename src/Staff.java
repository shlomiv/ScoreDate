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
import java.util.Vector;

import javax.swing.JPanel;

/**
 * @author Massimo Callegari
 *
 */
public class Staff extends JPanel 
{
	private static final long serialVersionUID = 7759085255881441116L;

	Font appFont;
	private ResourceBundle appBundle; // reference to languages bundle just to display tonality
	Preferences appPrefs;

	/*
	 *  ************************************* SCORE LAYOUT ***************************************
	 * 
	    | window |clefWidth|alteration|timeSign|  noteDistance  
	    | Margin |         |  Width   | Width  |    /------\    
	    |        |         |          |        |    |      |    
	    |         ----GG------#---------------------|------|--------------------------------------
	    |         -----G----------#-------4---------|-----O---------------------------------------
	    |         ---GG---------#---------4--------O----------------------------------------------
	    |         ---G-G--------------------------------------------------------------------------
	    |         ----G---------------------------------------------------------------------------
	 *
	 *  ******************************************************************************************
	 *	
	*/

    private int clefWidth = 32; // width of score clefs
    private int alterationWidth = 0; // width of alterations symbols. None by default
    private int timeSignWidth = 30; // width of current score time signature symbol. This includes also the first note margin
    private int timeSignNumerator = 4;
    private int timeSignDenominator = 4;
    private int timeDivision = 1; // ratio between time signature denominator and quarters 
    private int scoreYpos = 45; // Y coordinate of the first row of the score
    private int rowsDistance = 90; // distance in pixel between staff rows
    private int numberOfMeasuresPerRow = 2; // number of measures in a single row
    private int numberOfRows = 4; // number of score rows
    private int notesShift = 10; // space in pixel to align notes to the score layout
    private int noteDistance = 72; // distance in pixel between 1/4 notes
    private int firstNoteXPos = clefWidth + alterationWidth + alterationWidth + timeSignWidth + notesShift;
    private int scoreLineWidth;
    
    private boolean inlineMode = false;
    private int forcedNumberOfMeasures = -1;
    private int clefMask = 1;
    private Vector<Integer> clefs = new Vector<Integer>();
    private Accidentals acc; // accidentals reference used for drawing
    
    private double globalScale = 1.0;
    
    public Staff(Font f, ResourceBundle b, Preferences p, Accidentals a, boolean inline, boolean singlePage)
    {
    	appFont = f;
    	appBundle = b;
    	appPrefs = p;
    	acc = a;
    	globalScale = 1.0;

    	if (inline == true)
    	{
    		inlineMode = true;
    		numberOfRows = 1;
    		numberOfMeasuresPerRow = 0;
    	}
    	
    	setBackground(Color.white);
    }
    
    public void setRowsDistance(int dist)
    {
    	rowsDistance = dist;
    }
    
    public void setClefs(int type)
    {
    	clefMask = type;
    	clefs.clear();

    	if ((clefMask & appPrefs.TREBLE_CLEF) > 0) clefs.add(appPrefs.TREBLE_CLEF);
    	if ((clefMask & appPrefs.BASS_CLEF) > 0) clefs.add(appPrefs.BASS_CLEF);
    	if ((clefMask & appPrefs.ALTO_CLEF) > 0) clefs.add(appPrefs.ALTO_CLEF);
    	if ((clefMask & appPrefs.TENOR_CLEF) > 0) clefs.add(appPrefs.TENOR_CLEF);

    	repaint();
    }
 
    public void setAccidentals(Accidentals a)
    {
    	acc = new Accidentals(a.getType(), a.getNumber(), appPrefs);
    }
    
    public void setTimeSignature(int num, int denom)
    {
    	timeSignNumerator = num;
    	timeSignDenominator = denom;
    	timeDivision = timeSignDenominator / 4;
    	repaint();
    }
    
    public int getMeasuresNumber()
    {
    	if (forcedNumberOfMeasures == -1)
    	{
    		if (acc != null)
    			alterationWidth = acc.getNumber() * 12;
    		int scoreLineWidth = clefWidth + alterationWidth + timeSignWidth;
    		int tmpMeas =  (getWidth() - scoreLineWidth) / ((timeSignNumerator/timeDivision) * noteDistance);
    		int tmpRows = getHeight() / rowsDistance;
       	
    		return tmpMeas * tmpRows;
    	}
    	else
    		return forcedNumberOfMeasures;
    }
    
    public void setMeasuresNumber(int num)
    {
    	forcedNumberOfMeasures = num;
    }
    
    public int getNotesDistance()
    {
    	return noteDistance;
    }
    
    public int getRowsNumber()
    {
    	return numberOfRows;
    }
    
    public int getFirstNoteXPosition()
    {
    	if (acc != null)
    		alterationWidth = acc.getNumber() * 12;
    	firstNoteXPos = clefWidth + alterationWidth + timeSignWidth + notesShift;
    	return firstNoteXPos;
    }
    
    public int getStaffWidth()
    {
    	if (inlineMode == false)
    	{
    		if (acc != null)
    			alterationWidth = acc.getNumber() * 12;
    		scoreLineWidth = clefWidth + alterationWidth + timeSignWidth;
    		numberOfMeasuresPerRow = (getWidth() - scoreLineWidth) / ((timeSignNumerator/timeDivision) * noteDistance);
    		scoreLineWidth += (numberOfMeasuresPerRow * ((timeSignNumerator/timeDivision) * noteDistance));
    	}
    	else 
    		scoreLineWidth = getWidth();

    	//System.out.println("[getStaffWidth] staff width: " + scoreLineWidth);
    	return scoreLineWidth;
    }
    
    public int getStaffHeight()
    {
    	calculateSize();
    	return (numberOfRows * rowsDistance) + scoreYpos;
    }

    public void setScale(double factor)
    {
    	globalScale = factor;
    }
    
    private int calculateSize()
    {
		if ( acc != null)
			alterationWidth = acc.getNumber() * 12;
		else
			alterationWidth = 0;
        firstNoteXPos = clefWidth + alterationWidth + alterationWidth + timeSignWidth + notesShift;

        scoreLineWidth = clefWidth + alterationWidth + timeSignWidth;
        
        int vxPos = scoreLineWidth + ((timeSignNumerator/timeDivision) * noteDistance);

        if (inlineMode == false)
        {
        	if (forcedNumberOfMeasures == -1)
        	{
        		numberOfMeasuresPerRow = (getWidth() - scoreLineWidth) / ((timeSignNumerator/timeDivision) * noteDistance);
        		numberOfRows = getHeight() / rowsDistance;
        	}
        	else
        	{
        		numberOfMeasuresPerRow = (getWidth() - scoreLineWidth) / ((timeSignNumerator/timeDivision) * noteDistance);
        		numberOfRows = (int)Math.ceil((double)forcedNumberOfMeasures / (double)numberOfMeasuresPerRow);
        		//System.out.println("[Staff] numberOfMeasuresPerRow: " + numberOfMeasuresPerRow + ", numberOfRows: " + numberOfRows);
        	}
        	
        	scoreLineWidth += (numberOfMeasuresPerRow * ((timeSignNumerator/timeDivision) * noteDistance));
        }
        else
        {
        	scoreLineWidth = getWidth();
        }
        return vxPos;
    }
    
    
    // Draw staff. Includes clefs, alterations, time signature
 	protected void paintComponent(Graphics g) 
 	{
 		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		super.paintComponent(g);
 		if (globalScale != 1.0)
			((Graphics2D) g).scale(globalScale, globalScale);
 		
 		g.setColor(Color.white);
 		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.black);
		
		//System.out.println("[Staff - paintComponent] w = " + getWidth());

        int yPos = scoreYpos;
        int vXPos = calculateSize();
        
        for (int r = 0; r < numberOfRows; r++) 
        {
        	// draw vertical separators first
        	for (int v = 0; v < numberOfMeasuresPerRow; v++)
        		g.drawLine(vXPos + v * ((timeSignNumerator/timeDivision) * noteDistance), yPos, vXPos + v * ((timeSignNumerator/timeDivision) * noteDistance), yPos+40);
        	// draw the staff 5 lines 
        	for (int l = 0; l < 5; l++)
        		g.drawLine(0, yPos + (l * 10), scoreLineWidth, yPos + (l * 10));
        	
        	// 1 - Draw clef
        	if ((clefMask & appPrefs.TREBLE_CLEF) > 0)
        	{
        		g.setFont(appFont.deriveFont(70f));
        		g.drawString("G", 0, yPos + 42);
        	}
        	else if ((clefMask & appPrefs.BASS_CLEF) > 0)
        	{
        		g.setFont(appFont.deriveFont(60f));
        		g.drawString("?", 0, yPos + 40);
        	}
        	else if ((clefMask & appPrefs.ALTO_CLEF) > 0)
        	{
        		g.setFont(appFont.deriveFont(55f));
        		g.drawString("" + (char)0xBF, 0, yPos + 43);
        	}
        	else if ((clefMask & appPrefs.TENOR_CLEF) > 0)
        	{
        		g.setFont(appFont.deriveFont(55f));
        		g.drawString("" + (char)0xBF, 0, yPos + 33);
        	}
    		
    		// 2 - Draw accidentals
        	if (acc != null && clefs.size() > 0)
        		acc.paint(g, appFont, clefWidth, yPos, clefs.get(0));
        	
        	// 3 - Draw tonality (only on the first row)
        	if (r == 0 && acc != null)
        	{
        		g.setColor(Color.gray);
        		g.setFont(new Font("LucidaSans", Font.PLAIN, 11));
        		g.drawString(acc.getTonality(appBundle), 0, yPos - 20);
        		g.setColor(Color.black);
        	}

        	// 4 - Draw time signature
    		String t = "";
        	if (inlineMode == false)
        	{
        		if (timeSignNumerator == 4 && timeSignDenominator == 4)
        			t = "$";
        		if (timeSignNumerator == 3 && timeSignDenominator == 4)
        			t = "#";
        		if (timeSignNumerator == 2 && timeSignDenominator == 4)
        			t = "@";
        		if (timeSignNumerator == 6 && timeSignDenominator == 8)
        			t = "P";
	    		g.setFont(appFont.deriveFont(58f));
	    		g.drawString(t, clefWidth + alterationWidth, yPos+41);
        	}

    		// 5 - Draw double clef elements
        	if (clefs.size() > 1)
        	{
            	for (int v = 0; v < numberOfMeasuresPerRow; v++)
            		g.drawLine(vXPos + v * ((timeSignNumerator/timeDivision) * noteDistance), yPos + (rowsDistance / 2), vXPos + v * ((timeSignNumerator/timeDivision) * noteDistance), yPos + (rowsDistance / 2) + 40);
        		for (int l = 0; l < 5; l++)
            		g.drawLine(0, yPos + (rowsDistance / 2) + (l * 10), scoreLineWidth, yPos + (rowsDistance / 2) + (l * 10));

        		// draw second clef
            	if (clefs.get(1) == appPrefs.TREBLE_CLEF)
            	{
            		g.setFont(appFont.deriveFont(70f));
            		g.drawString("G", 0, yPos + (rowsDistance / 2) + 42);
            	}
            	else if (clefs.get(1) == appPrefs.BASS_CLEF)
            	{
            		g.setFont(appFont.deriveFont(60f));
            		g.drawString("?", 0, yPos + (rowsDistance / 2) + 40);
            	}
            	else if (clefs.get(1) == appPrefs.ALTO_CLEF)
            	{
            		g.setFont(appFont.deriveFont(55f));
            		g.drawString("" + (char)0xBF, 0, yPos + (rowsDistance / 2) + 43);
            	}
            	else if (clefs.get(1) == appPrefs.TENOR_CLEF)
            	{
            		g.setFont(appFont.deriveFont(55f));
            		g.drawString("" + (char)0xBF, 0, yPos + (rowsDistance / 2) + 33);
            	}        		
        		
        		// draw accidentals
        		acc.paint(g, appFont, clefWidth, yPos + (rowsDistance / 2), clefs.get(1));

        		// draw tonality
            	g.setColor(Color.gray);
           	    g.setFont(new Font("LucidaSans", Font.PLAIN, 11));
            	g.drawString(acc.getTonality(appBundle), 0, yPos + (rowsDistance / 2) - 20);
            	g.setColor(Color.black);
        		// draw time signature
        		if (inlineMode == false)
            	{
        			g.setFont(appFont.deriveFont(58f));
        			g.drawString(t, clefWidth + alterationWidth, yPos+ + (rowsDistance / 2) + 41);
            	}
        	}
        	
        	yPos += rowsDistance;
        }
 	}

}
