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
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent; 
import javax.swing.event.TreeSelectionListener;

public class StatsPanel extends JPanel implements TreeSelectionListener
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
	private boolean[] showGame = { true, true, true };
	private int[][] statInfo = { {  -1 , -1, -1 , -1, 0 },   // line game: start day, end day, min, max, count
			 				   {  -1 , -1, -1 , -1, 0 },   // rhythm game: start day, end day, min, max, count
			 				   {  -1 , -1, -1 , -1, 0 } }; // score game: start day, end day, min, max, count
	private int[] globalInfo = {  -1 , -1, -1 , -1, 0 };   // global info: start day, end day, min, max, count
	
	
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
			statsList.setSelectionRow(statsList.getRowCount() - 1);
			statsList.addTreeSelectionListener(this);
			readStatsFile((DefaultMutableTreeNode)statsList.getLastSelectedPathComponent(), 
					currDir, SDSfiles[SDSfiles.length - 1]);
		}
		else
		{
			DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(appBundle.getString("_noStatistics"));
			statsList = new JTree(mainNode);
		}
		statsList.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		statsList.setBackground(Color.decode("0xFFFFD5"));
		statsList.setBounds(5, 5, 170, getHeight() - topBarHeight - 30);
		
		treePanel.add(statsList);
		
	}
	
	/*
	 * Function that creates the statistics months nodes 
	 */
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
	
	/*
	 * Function that creates the statistics days nodes. Read a whole .sds file
	 */
	private void readStatsFile(DefaultMutableTreeNode node, File dir, String file)
	{
		node.removeAllChildren();
		statInfo[0][0] = statInfo[0][1] = statInfo[0][3] = -1;
		statInfo[0][2] = 99999;
		statInfo[0][4] = 0;
		statInfo[1][0] = statInfo[1][1] = statInfo[1][3] = -1;
		statInfo[1][2] = 99999;
		statInfo[1][4] = 0;
		statInfo[2][0] = statInfo[2][1] = statInfo[2][3] = -1;
		statInfo[2][2] = 99999;
		statInfo[2][4] = 0;
		globalInfo[0] = globalInfo[1] = globalInfo[3] = -1;
		globalInfo[2] = 99999;
		globalInfo[4] = 0;

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
			  
			  if (statInfo[record.gameType][0] == -1) // set start day
				  statInfo[record.gameType][0] = record.day;
			  if (globalInfo[0] == -1)
				  globalInfo[0] = record.day;
			  if (record.day != statInfo[record.gameType][1]) // set end day
				  statInfo[record.gameType][1] = record.day;

			  if (record.day != globalInfo[1])
			  {
				  globalInfo[1] = record.day;
				  node.add(day);
			  }
			  
			  if (record.totalScore > statInfo[record.gameType][3]) // set max score
				  statInfo[record.gameType][3] = record.totalScore;
			  if (record.totalScore > globalInfo[3]) // set global max score
				  globalInfo[3] = record.totalScore;
			  
			  if (record.totalScore < statInfo[record.gameType][2]) // set min score
				  statInfo[record.gameType][2] = record.totalScore;
			  if (record.totalScore < globalInfo[2]) // set global min score
				  globalInfo[2] = record.totalScore;
			  
			  statInfo[record.gameType][4]++;
			  globalInfo[4]++;
		  }
		  br.close();
		  in.close();
		  fstream.close();
		  
		  System.out.println("--INLINE-- done. startDay: " + statInfo[0][0] + ", endDay: " + statInfo[0][1] + ", minScore: " + statInfo[0][2] + ", maxScore: " + statInfo[0][3]);
		  System.out.println("--RHTYHM-- done. startDay: " + statInfo[1][0] + ", endDay: " + statInfo[1][1] + ", minScore: " + statInfo[1][2] + ", maxScore: " + statInfo[1][3]);
		  System.out.println("--SCORE-- done. startDay: " + statInfo[2][0] + ", endDay: " + statInfo[2][1] + ", minScore: " + statInfo[2][2] + ", maxScore: " + statInfo[2][3]);
		}
		catch (Exception e)
		{
		  System.out.println("An exception occured while reading the file !!");	
		}
		
	}
	
	public void valueChanged(TreeSelectionEvent e) 
	{
		//Returns the last path element of the selection.
		//This method is useful only when the selection model allows a single selection.
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		    		statsList.getLastSelectedPathComponent();

		    if (node == null) //Nothing is selected.     
		    	return;

		    if (node.getLevel() == 1)
		    {
		    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		        System.out.println("Node slected idx: " + parent.getIndex(node));
		    }
		    /*
		    Object nodeInfo = node.getUserObject();
		    if (node.isLeaf()) {
		        BookInfo book = (BookInfo)nodeInfo;
		        displayURL(book.bookURL);
		    } else {
		        displayURL(helpURL); 
		    }
		    */
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
		
		private Color getGameColor(int type)
		{
			if (type == 0)
				return Color.decode("0x389C14");
			else if (type == 1)
				return Color.decode("0x2330A3");
			if (type == 2)
				return Color.decode("0x9C1313");
			
			return Color.black;
		}

		protected void paintComponent(Graphics g) 
		{
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.decode("0x222222"));
			((Graphics2D) g).setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND  ));
			
			int xPos = 35;
			int yPos = 30;
			int graphH = getHeight() - 60;
			int graphW = getWidth() - 55;
			
			// draw axis
			g.drawLine(xPos, 30, xPos, graphH + 30);
			g.drawLine(xPos, 30 + graphH, xPos + graphW, 30 + graphH);
			
			int scoreDiff = globalInfo[3] - globalInfo[2];
			int scoreStep = scoreDiff / 10;
			
			// draw Y axis labels
			g.setFont(new Font("Arial", Font.BOLD, 12));
			for (int y = yPos, c = 0; y < yPos + graphH - 20; y+=((graphH - 20) / 10), c++)
			{
				g.drawString(Integer.toString(globalInfo[3] - (c * scoreStep)), 0, y + 10);
			}
			
			for (int i = 0; i < 3; i++)
			{
				if ( showGame[i] == false || statInfo[i][2] == -1 || statInfo[i][3] == -1) // if game disabled or min or max score not available 
					continue;
				
				xPos = 37;

				g.setColor(getGameColor(i));
				if (statInfo[i][0] == statInfo[i][1]) // if same day
				{
				  if (statInfo[i][4] == 1)
				  {
					int relYPos = ((statInfo[i][3] - globalInfo[2]) * (graphH - 20)) / scoreDiff;
					System.out.println("relYPos = " + relYPos);
					g.drawLine(xPos, yPos + relYPos, xPos + graphW, yPos + relYPos);
				  }
				  else
				  {
					// draw a serious chart here :)
				  }
				}
			}
		}
	}
}




