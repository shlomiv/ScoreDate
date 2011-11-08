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
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import java.util.Vector;

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
	
	public RoundPanel topBar;
	public RoundedButton homeBtn;
	private JPanel treePanel;
	private JTree statsList;
	private GraphPanel graphPanel;
	File currDir; // variable that holds ScoreDate directory
	String[] SDSfiles; // Array of filenames (.sds) of the saved stats
	Vector<statRecord> currentStats = new Vector<statRecord>(); // Vector of the currently selected statistics
	
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
		currDir = new File(".");
		File SDdir = new File(currDir.getAbsolutePath());
		
		FilenameFilter filter = new FilenameFilter() 
		{
		    public boolean accept(File dir, String name) 
		    {
		        return name.startsWith("ScoreDateStats_");
		    }
		};
		
		SDSfiles = SDdir.list(filter);
		
		System.out.println("Stats found in current dir: " + SDSfiles.length);
		
		treePanel = new JPanel();
		treePanel.setLayout(null);
		treePanel.setBackground(Color.decode("0xFFFFD5"));
		Border defBorder = UIManager.getBorder(treePanel);
		treePanel.setBorder(BorderFactory.createTitledBorder(defBorder, "", TitledBorder.LEADING, TitledBorder.TOP));
		treePanel.setBounds(5, topBarHeight + 10, 200, d.height - topBarHeight - 15);
		//treePanel.add(statsList);
		
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
		
		if (statsList != null)
			treePanel.remove(statsList);

		if (SDSfiles.length != 0)
		{
			DefaultMutableTreeNode mainNode = loadTree();
			statsList = new JTree(mainNode);
			readStatsFile(mainNode.getLastLeaf(), currDir, SDSfiles[SDSfiles.length - 1]);			
		}
		else
		{
			DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(appBundle.getString("_noStatistics"));
			statsList = new JTree(mainNode);
		}
		statsList.setBackground(Color.decode("0xFFFFD5"));
		statsList.setBounds(5, 5, 170, getHeight() - topBarHeight - 30);
		statsList.setSelectionRow(statsList.getRowCount() - 1);
		treePanel.add(statsList);
		
	}
	
	private DefaultMutableTreeNode loadTree()
	{
		if (statsList != null)
			statsList.removeAll();
		String allmonths = appBundle.getString("_months");
		String[] months = allmonths.split(",");
		DefaultMutableTreeNode firstNode = new DefaultMutableTreeNode(appBundle.getString("_menuStatistics"));
		// add the list as first level nodes of the JTree
		for (int i = 0; i < SDSfiles.length; i++)
		{
			String cutYYMM = SDSfiles[i].substring(SDSfiles[i].indexOf("_") + 1, SDSfiles[i].indexOf("."));
			String year = cutYYMM.substring(0, 4);
			int mInt = Integer.parseInt(cutYYMM.substring(4, 6));
			DefaultMutableTreeNode month = new DefaultMutableTreeNode("" + months[mInt - 1] + " " + year);
			DefaultMutableTreeNode loading = new DefaultMutableTreeNode(appBundle.getString("_statsLoading"));
			//DefaultMutableTreeNode month = new DefaultMutableTreeNode(cutYYMM);
			month.add(loading);
			firstNode.add(month);
		}
		
		return firstNode;
	}
	
	private void readStatsFile(DefaultMutableTreeNode node, File dir, String file)
	{
		node.removeAllChildren();

		try
		{
		  FileInputStream fstream = new FileInputStream(dir.getAbsolutePath() + "/" + file);
		  // Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  
		  String strLine;
		  //Read File Line By Line
		  while ((strLine = br.readLine()) != null)   
		  {
			  // Print the content on the console
			  System.out.println (strLine);
			  String[] recFields = strLine.split(","); 
			  statRecord record = new statRecord();

			  record.day = Integer.parseInt(recFields[0]);
			  record.hours = Integer.parseInt(recFields[1]);
			  record.minutes = Integer.parseInt(recFields[2]);
			  record.seconds = Integer.parseInt(recFields[3]);
			  record.gameType = Integer.parseInt(recFields[4]);
			  record.notesPlayed = Integer.parseInt(recFields[5]);
			  record.correctAnswers = Integer.parseInt(recFields[6]);
			  record.wrongAnswers = Integer.parseInt(recFields[7]);
			  record.wrongRhythms = Integer.parseInt(recFields[8]);
			  record.totalScore = Integer.parseInt(recFields[9]);
			  record.avgPrecision = Integer.parseInt(recFields[10]);
			  record.gameSpeed = Integer.parseInt(recFields[11]);
			  record.timeSpent = Integer.parseInt(recFields[12]);
			  
			  currentStats.add(record);
			  DefaultMutableTreeNode day = new DefaultMutableTreeNode(Integer.toString(record.notesPlayed));
			  node.add(day);
		  }
		}
		catch (Exception e)
		{
		  System.out.println("An exception occured while reading the file !!");	
		}
		
	}
	
	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		topBar.setBounds(5, 5, getWidth() - 10, topBarHeight);
		treePanel.setBounds(5, topBarHeight + 10, 210, getHeight() - topBarHeight - 15);
		statsList.setBounds(10, 10, 190, getHeight() - topBarHeight - 30);
		graphPanel.setBounds(230, topBarHeight + 10, getWidth() - 250, getHeight() - topBarHeight - 15);
	}
	
	/* 
	 * ********************************************************************************
	 * 			RECORD CLASS - defines a structure used to store each statistic
	 * ********************************************************************************
	 */	
	class statRecord
	{
		public int day;
		public int hours;
		public int minutes;
		public int seconds;
		public int gameType;
		public int notesPlayed;
		public int correctAnswers;
		public int wrongAnswers;
		public int wrongRhythms;
		public int totalScore;
		public int avgPrecision;
		public int gameSpeed;
		public int timeSpent;
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




