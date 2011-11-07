/***********************************************
This file is part of the ScoreDate project (http://www.mindmatter.it/scoredate/).

ScoreDate is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

NRTB is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ScoreDate.  If not, see <http://www.gnu.org/licenses/>.

**********************************************/

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.io.File; 
import java.io.FilenameFilter;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode; 

public class StatsPanel extends JPanel
{
	private static final long serialVersionUID = -3725519060278100632L;
	Font appFont;
	Preferences appPrefs;
	private ResourceBundle appBundle;
	
	private RoundPanel topBar;
	public RoundedButton homeBtn;
	private JPanel treePanel;
	private JTree statsList;
	private GraphPanel graphPanel;
	
	int topBarHeight = 80;

	public StatsPanel(Font f, ResourceBundle b, Preferences p, Dimension d)
	{
		appFont = f;
		appBundle = b;
		appPrefs = p;
		
		setBackground(Color.white);
		setSize(d);
		setLayout(null);
		
		topBar = new RoundPanel(Color.decode("0xE7A935"), Color.decode("0xE7E734"));
		topBar.setBounds(5, 5, d.width - 10, topBarHeight);
		topBar.setLayout(null);
		
		// Create home button
		homeBtn = new RoundedButton("", appBundle);
		//homeBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
		homeBtn.setBounds(10, 5, 70, 70);
		homeBtn.setBackground(Color.decode("0x8FC6E9"));
		homeBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/home.png")).getImage());
		
		topBar.add(homeBtn);
		
		// retrieve the list of stats file saved during exercises
		File currdir = new File(".");
		File SDdir = new File(currdir.getAbsolutePath());
		
		FilenameFilter filter = new FilenameFilter() 
		{
		    public boolean accept(File dir, String name) 
		    {
		        return name.startsWith("ScoreDateStats_");
		    }
		};
		String[] SDSfiles = SDdir.list(filter);
		
		System.out.println("Stats found in current dir: " + SDSfiles.length);
		
		if (SDSfiles.length != 0)
		{
			String allmonths = appBundle.getString("_months");
			String[] months = allmonths.split(",");
			DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(appBundle.getString("_menuStatistics"));
			// add the list as first level nodes of the JTree
			for (int i = 0; i < SDSfiles.length; i++)
			{
				String cutYYMM = SDSfiles[i].substring(SDSfiles[i].indexOf("_") + 1, SDSfiles[i].indexOf("."));
				String year = cutYYMM.substring(0, 4);
				int mInt = Integer.parseInt(cutYYMM.substring(4, 6));
				DefaultMutableTreeNode month = new DefaultMutableTreeNode("" + months[mInt - 1] + " " + year);
				//DefaultMutableTreeNode month = new DefaultMutableTreeNode(cutYYMM);
				mainNode.add(month);
			}
			statsList = new JTree(mainNode);
		}
		else
		{
			DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode("No statistics found !"); // TODO: internationalize
			statsList = new JTree(mainNode);
		}
		statsList.setBackground(Color.decode("0xFFFFD5"));
		statsList.setBounds(5, 5, 170, d.height - topBarHeight - 25);
		
		treePanel = new JPanel();
		treePanel.setLayout(null);
		treePanel.setBackground(Color.decode("0xFFFFD5"));
		Border defBorder = UIManager.getBorder(treePanel);
		treePanel.setBorder(BorderFactory.createTitledBorder(defBorder, "", TitledBorder.LEADING, TitledBorder.TOP));
		treePanel.setBounds(5, topBarHeight + 10, 200, d.height - topBarHeight - 15);
		treePanel.add(statsList);
		//statsList.setSelectionRow(0);
		
		graphPanel = new GraphPanel();
		graphPanel.setBackground(Color.white);
		graphPanel.setBounds(210, topBarHeight + 10, d.width - 220, d.height - topBarHeight - 15);
		
		add(topBar);
		add(treePanel);
		add(graphPanel);
		
		updateLanguage(appBundle);
	}
	
	public void updateLanguage(ResourceBundle bundle)
	{
		appBundle = bundle;
	}
	
	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		topBar.setBounds(5, 5, getWidth() - 10, topBarHeight);
		treePanel.setBounds(5, topBarHeight + 10, 190, getHeight() - topBarHeight - 15);
		statsList.setBounds(10, 10, 170, getHeight() - topBarHeight - 25);
		graphPanel.setBounds(200, topBarHeight + 10, getWidth() - 220, getHeight() - topBarHeight - 15);
	}
	
	
	/* 
	 * ********************************************************************************
	 * 					GRAPH PANEL - this draw the actual chart
	 * ********************************************************************************
	 */

	class GraphPanel extends JPanel
	{
		private static final long serialVersionUID = -5881943610504944809L;
		boolean gradientBack = false;
		Color startColor;
		Color endColor;

		public GraphPanel()
		{
			
		}
		

		protected void paintComponent(Graphics g) 
		{
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.decode("0x222222"));
			((Graphics2D) g).setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND  ));
			
			g.drawLine(35, 30, 35, getHeight() - 30);
			g.drawLine(35, getHeight() - 30, getWidth() - 20, getHeight() - 30);
			
		}
	}
	
}




