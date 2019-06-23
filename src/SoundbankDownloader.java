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

import java.net.URL;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

public class SoundbankDownloader extends JDialog 
{
	private static final long serialVersionUID = 4062706733206786112L;
	
	public GradientBar progress;

	public SoundbankDownloader()
	{
		setTitle("Downloading...please wait");
		setAlwaysOnTop(true);
        setSize(400, 100);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on the display
        setLayout(null);
        
        progress = new GradientBar(0, 100);
        progress.setBounds(25, 15, 340, 30);
        add(progress);
        setVisible(true);
	}
	
	public void startDownload()
	{
		try
		{
			BufferedInputStream in = new java.io.BufferedInputStream(
					new URL("http://scoredate.sourceforge.net//extras/soundbank-mid.gm").openStream());
			String jreDir = System.getProperty("java.home");
			FileOutputStream fos = new java.io.FileOutputStream(jreDir + File.separator + "lib" + File.separator + "audio" + File.separator + "soundbank-mid.gm");
			BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
			byte[] data = new byte[1024];
			int count = 0;
			int x=0;
			while((x = in.read(data,0,1024))>=0)
			{
				bout.write(data,0,x);
				count += x;
				progress.setValue((count * 100) / 1154250);
				progress.repaint();
			}
			bout.close();
			in.close();
		}
		catch (MalformedURLException e)
	    { System.err.println(e.toString()); }
	    catch (IOException e)
	    { System.err.println(e.toString()); }
	}
	
	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		progress.setBounds(25, 15, 340, 30);
		
	}
}
