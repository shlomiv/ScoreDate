import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JDialog;

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

public class AudioOptionDialog extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 6932887852996919458L;
	private ResourceBundle appBundle;
	private Preferences appPrefs;
	
	public AudioOptionDialog(ResourceBundle b, Preferences p)
	{
		appBundle = b;
		appPrefs = p;
		
	}
	
	public void actionPerformed(ActionEvent ae)
    {
		/*
		if (ae.getSource() == okButton)
		{
			
		}
		*/
    }
}
	
