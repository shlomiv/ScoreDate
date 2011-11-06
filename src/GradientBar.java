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
