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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Vector;
//import java.awt.Polygon;

import javax.swing.JPanel;

public class Piano extends JPanel 
{
  private static final long serialVersionUID = -5581159862523677986L;
  Vector<Key> whiteKeys = new Vector<Key>();
  Vector<Key> blackKeys = new Vector<Key>();
  Vector<Key> keys = new Vector<Key>(); // Array of whiteKeys + blackKeys
  
  // painting parameters
  int currentWidth = 0;
  
  int keysNumber = 73;
  int firstLowPitch = 40;
  int firstHighPitch = 42;  
  int secondLowPitch = 70;
  int secondHighPitch = 75;
  int selectedKeyIndex = -1;

  // Keys dimensions and offsets
  final int kw = 16, kh = 80;
  final int ypos = 5;
  int leftMargin = 43;

  public Piano(int l) 
  {
    int octavesNumber;
    int transpose;
    int offset = 0;

    this.keysNumber = l;

    if (keysNumber == 61) 
    {
        octavesNumber = 5;
        transpose = 36;
    }
    else 
    {
        octavesNumber = 6;
        transpose = 24;
    }

    keys.removeAll(blackKeys);
    keys.removeAll(whiteKeys);
    
    setLayout(null);

    int whiteIDs[] = { 0, 2, 4, 5, 7, 9, 11 };
    int blackIDs[] = { 1, 3, 6, 8, 10 };

    for (int i = 0, x = 0; i < octavesNumber; i++) 
    {
      for (int j = 0; j < 7; j++, x += kw) 
      {
        int keyPitch = i * 12 + whiteIDs[j] + transpose;
        whiteKeys.add(new Key(x + leftMargin, ypos, kw, kh, keyPitch, i, j, 0, false));
      }
    }

    whiteKeys.add(new Key(7 * octavesNumber * kw + leftMargin + 3, ypos, kw, kh, 
			octavesNumber * 12 + transpose, octavesNumber, 0, 0, false));

    for (int i = 0, x = -1; i < octavesNumber; i++) 
    {
      for (int j = 0, noteIdx = 0; j < 5; j++, x += kw, noteIdx++) 
      {
    	  int keyPitch = i * 12 + blackIDs[j] + transpose;
    	  blackKeys.add(new Key( x += kw + leftMargin, ypos, kw / 2, kh / 2 + 11, keyPitch, i, noteIdx, offset, true));
    	  if (j == 1) x += kw;
    	  if (j == 1 || j == 4) 
    	  {
            offset +=kw;
            noteIdx++;
    	  }
      }
    }

    keys.addAll(blackKeys);
    keys.addAll(whiteKeys);
    for (int i = 0; i < keys.size(); i++)
    {
    	if (keys.get(i).pitch == 60)
    		keys.get(i).setBackground(Color.decode("0xFFAFAF"));
    	this.add(keys.get(i));
    }
  }
  
  public void reset(boolean highlightStart)
  {
	  for (int i = 0; i < whiteKeys.size(); i++)
	  {
		  if (highlightStart == true && whiteKeys.get(i).pitch == 60)
			  whiteKeys.get(i).setBackground(Color.decode("0xFFAFAF"));
		  else
			  whiteKeys.get(i).setBackground(Color.white);
		  whiteKeys.get(i).is_highlighted = false;
	  }
	  for (int i = 0; i < blackKeys.size(); i++)
	  {
		  blackKeys.get(i).setBackground(Color.black);
		  blackKeys.get(i).is_highlighted = false;
	  }
  }

  // highlight a key and returns the note index from 0 to 7
  public int highlightKey(int pitch, boolean enable)
  {
	  for (int i = 0; i < keys.size(); i++)
	  {
		  if (keys.get(i).pitch == pitch)
		  {
			  keys.get(i).highlight(enable, true);
			  if (enable == true)
			  	  selectedKeyIndex = i;
			  else
				  selectedKeyIndex = -1;
			  
		  	  return keys.get(i).noteIndex;
		  }
	  }
	  return 0;
  }
  
  public void keyPressed(int pitch, boolean on)
  {
	  for (int i = 0; i < keys.size(); i++)
	  {
		  if (keys.get(i).pitch == pitch)
		  {
			  keys.get(i).setPressed(on);
			  return;
		  }
	  }
  }
  
  public int getNoteIndexFromPitch(int pitch)
  {
	  for (int i = 0; i < keys.size(); i++)
	  {
		  Key tmpKey = keys.get(i);
		  if (tmpKey.pitch == pitch)
		  {
			  if (tmpKey.is_black == true)
				  return tmpKey.noteIndex + 100;
			  else
				  return tmpKey.noteIndex;
		  }
	  }
	  return 0;
  }
  
  public int getOctaveFromPitch(int pitch)
  {
	  for (int i = 0; i < keys.size(); i++)
	  {
		  Key tmpKey = keys.get(i);
		  if (tmpKey.pitch == pitch)
			  return tmpKey.octave;
	  }
	  return 0;
  }
  
  public boolean isSelectedBlack()
  {
	  if (selectedKeyIndex == -1)
		  return false;

	  return keys.get(selectedKeyIndex).is_black;
  }

  public boolean is73keys() 
  {
    return this.keysNumber == 73;
  }

  public boolean is61keys() 
  {
    return this.keysNumber == 61;
  }

  public void setNewBound(int low, int high)
  {
	  if (low == -1 || high == -1)
		  return;
	  // reset previously set keys
	  /*
	  for (int i = 0; i < whiteKeys.size(); i++)
	  {
		  if (whiteKeys.get(i).pitch >= leftHandLowPitch && whiteKeys.get(i).pitch <= leftHandHighPitch)
		  	whiteKeys.get(i).setBackground(Color.white);
		  if (whiteKeys.get(i).pitch > leftHandHighPitch)
			  break;
	  }
	  leftHandLowPitch = low;
	  leftHandHighPitch = high;
	  */
	  // highlight new range
	  for (int i = 0; i < whiteKeys.size(); i++)
	  {
		  if (whiteKeys.get(i).pitch >= low && whiteKeys.get(i).pitch <= high)
		  	whiteKeys.get(i).highlight(true, false); //setBackground(new Color(152, 251, 152));
		  if (whiteKeys.get(i).pitch > high)
			  break;
      }
	  repaint();
  }

  protected void paintComponent(Graphics g) 
  {
    Graphics2D g2 = (Graphics2D) g;
    
    int offx = 0;
    if (keysNumber == 73)
    	offx = (getWidth()/2) - 390;
    else
    	offx = (getWidth()/2) - 325;

    // repaint keys only if piano panel has been resized
    if (currentWidth != getWidth())
    {
    	currentWidth = getWidth();
    	g2.setColor(Color.black);
    	for (int i = 0; i < whiteKeys.size(); i++) 
    	{
    		Key key = (Key) whiteKeys.get(i);
    		key.setXpos(2 + leftMargin + offx + (kw * i));
    		key.repaint();
    	}

    	for (int i = 0; i < blackKeys.size(); i++) 
    	{
    		Key key = (Key) blackKeys.get(i);
    		key.setXpos(leftMargin + offx + key.getXoffset() - 2 + ((i+1) * kw)); 
    		key.repaint();
    	}
    }
  }
} // End class Piano

