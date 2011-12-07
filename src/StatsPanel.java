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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionEvent; 
import javax.swing.event.TreeSelectionListener;

public class StatsPanel extends JPanel implements TreeSelectionListener, ActionListener
{
	private static final long serialVersionUID = -3725519060278100632L;
	Font appFont;
	Preferences appPrefs;
	private ResourceBundle appBundle;
	
	public RoundPanel topBar;
	public RoundedButton homeBtn;
	public RoundPanel linePanel, rtmPanel, scrPanel;
	public JCheckBox inlineCheckBox, rhythmCheckBox, scoreCheckBox;
	private JLabel lineAvgScoreLabel, rtmAvgScoreLabel, scrAvgScoreLabel;
	private JLabel lineAvgScoreResult, rtmAvgScoreResult, scrAvgScoreResult;
	private JLabel lineAvgGamesLabel, rtmAvgGamesLabel, scrAvgGamesLabel;
	private JLabel lineAvgGamesResult, rtmAvgGamesResult, scrAvgGamesResult;
	private JLabel lineAvgPrecLabel, rtmAvgPrecLabel, scrAvgPrecLabel;
	private JPanel treePanel;
	private JScrollPane treeScrollPanel;
	private JTree statsList;
	private DefaultMutableTreeNode selNode; // currently selected node
	private GraphPanel graphPanel;
	File currDir; // the ScoreDate directory path
	String[] SDSfiles; // Array of filenames (.sds) of the saved stats
	Vector<statRecord> currentStats = new Vector<statRecord>(); // Vector of the currently selected statistics
	private boolean[] showGame = { true, true, true };
	private int[][] statInfo = { {  -1 , -1, -1 , -1, 0 },   // line game: start day, end day, min, max, count
			 				   {  -1 , -1, -1 , -1, 0 },   // rhythm game: start day, end day, min, max, count
			 				   {  -1 , -1, -1 , -1, 0 } }; // score game: start day, end day, min, max, count
	private int[] globalInfo = {  -1 , -1, -1 , -1, 0 };   // global info: start day, end day, min, max, count
	private int singleDay = -1;
	
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
		topBar.setBorderColor(Color.decode("0xE7A935"));
		topBar.setBounds(5, 5, d.width - 10, topBarHeight);
		topBar.setLayout(null);
		
		// Create home button
		homeBtn = new RoundedButton("", appBundle);
		//homeBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
		homeBtn.setBounds(10, 5, 70, 70);
		homeBtn.setBackground(Color.decode("0x8FC6E9"));
		homeBtn.setButtonImage(new ImageIcon(getClass().getResource("/resources/home.png")).getImage());

		int panelsWidth = ((d.width - 100) / 3) - 5;
		int panelXPos = 90;
		
		linePanel = new RoundPanel(Color.decode("0xA1D5A6"), Color.decode("0xC0FFC6"));
		linePanel.setLayout(null);
		//linePanel.setBackground(new Color(0xC0, 0xFF, 0xC6));
		linePanel.setBorderColor(new Color(0x38, 0xC1, 0x14));
		linePanel.setBounds(panelXPos, 5, panelsWidth, 70);
		inlineCheckBox = new JCheckBox(appBundle.getString("_menuNotereading"), true);
		inlineCheckBox.setFont(new Font("Arial", Font.BOLD, 12));
		inlineCheckBox.setBounds(7, 7, panelsWidth - 14, 14);
		inlineCheckBox.addActionListener(this);
		lineAvgScoreLabel = new JLabel(appBundle.getString("_gameScore") + ":");
		lineAvgScoreLabel.setBounds(7, 26, 100, 14);
		lineAvgScoreResult = new JLabel("0");
		lineAvgScoreResult.setFont(new Font("Arial", Font.BOLD, 12));
		lineAvgScoreResult.setBounds(107, 26, 50, 14);
		lineAvgGamesLabel = new JLabel(appBundle.getString("_menuExercises") + ":");
		lineAvgGamesLabel.setBounds(7, 45, 100, 14);
		lineAvgGamesResult = new JLabel("0");
		lineAvgGamesResult.setFont(new Font("Arial", Font.BOLD, 12));
		lineAvgGamesResult.setBounds(107, 45, 50, 14);
		
		lineAvgPrecLabel = new JLabel("");
		lineAvgPrecLabel.setFont(new Font("Arial", Font.BOLD, 25));
		lineAvgPrecLabel.setBounds(panelsWidth - 70, 26, 50, 35);

		linePanel.add(inlineCheckBox);
		linePanel.add(lineAvgScoreLabel);
		linePanel.add(lineAvgScoreResult);
		linePanel.add(lineAvgGamesLabel);
		linePanel.add(lineAvgGamesResult);
		linePanel.add(lineAvgPrecLabel);
		
		panelXPos += panelsWidth + 5;
		rtmPanel = new RoundPanel(Color.decode("0x91AED9"), Color.decode("0xAACCFF"));
		rtmPanel.setLayout(null);
		//rtmPanel.setBackground(new Color(0xAA, 0xCC, 0xFF));
		rtmPanel.setBorderColor(new Color(0x23, 0x30, 0xA3));
		rtmPanel.setBounds(panelXPos, 5, panelsWidth, 70);
		rhythmCheckBox = new JCheckBox(appBundle.getString("_menuRythmreading"), true);
		rhythmCheckBox.setFont(new Font("Arial", Font.BOLD, 12));
		rhythmCheckBox.setBounds(7, 7, panelsWidth - 14, 14);
		rhythmCheckBox.addActionListener(this);
		rtmAvgScoreLabel = new JLabel(appBundle.getString("_gameScore") + ":");
		rtmAvgScoreLabel.setBounds(7, 25, 100, 14);
		rtmAvgScoreResult = new JLabel("0");
		rtmAvgScoreResult.setFont(new Font("Arial", Font.BOLD, 12));
		rtmAvgScoreResult.setBounds(107, 26, 50, 14);
		rtmAvgGamesLabel = new JLabel(appBundle.getString("_menuExercises") + ":");
		rtmAvgGamesLabel.setBounds(7, 42, 100, 14);
		rtmAvgGamesResult = new JLabel("0");
		rtmAvgGamesResult.setFont(new Font("Arial", Font.BOLD, 12));
		rtmAvgGamesResult.setBounds(107, 45, 50, 14);
		
		rtmAvgPrecLabel = new JLabel("");
		rtmAvgPrecLabel.setFont(new Font("Arial", Font.BOLD, 25));
		rtmAvgPrecLabel.setBounds(panelsWidth - 70, 26, 50, 35);

		rtmPanel.add(rhythmCheckBox);
		rtmPanel.add(rtmAvgScoreLabel);
		rtmPanel.add(rtmAvgScoreResult);
		rtmPanel.add(rtmAvgGamesLabel);
		rtmPanel.add(rtmAvgGamesResult);
		rtmPanel.add(rtmAvgPrecLabel);
		
		panelXPos += panelsWidth + 5;
		scrPanel = new RoundPanel(Color.decode("0xDE7373"), Color.decode("0xFF8484"));
		scrPanel.setLayout(null);
		//scrPanel.setBackground(new Color(0xFF, 0x84, 0x84));
		scrPanel.setBorderColor(new Color(0x9C, 0x13, 0x13));
		scrPanel.setBounds(panelXPos, 5, panelsWidth, 70);
		scoreCheckBox = new JCheckBox(appBundle.getString("_menuScorereading"), true);
		scoreCheckBox.setFont(new Font("Arial", Font.BOLD, 12));
		scoreCheckBox.setBounds(7, 7, panelsWidth - 14, 14);
		scoreCheckBox.addActionListener(this);
		scrAvgScoreLabel = new JLabel(appBundle.getString("_gameScore") + ":");
		scrAvgScoreLabel.setBounds(7, 24, 100, 14);
		scrAvgScoreResult = new JLabel("0");
		scrAvgScoreResult.setFont(new Font("Arial", Font.BOLD, 12));
		scrAvgScoreResult.setBounds(107, 26, 50, 14);
		scrAvgGamesLabel = new JLabel(appBundle.getString("_menuExercises") + ":");
		scrAvgGamesLabel.setBounds(7, 42, 100, 14);
		scrAvgGamesResult = new JLabel("0");
		scrAvgGamesResult.setFont(new Font("Arial", Font.BOLD, 12));
		scrAvgGamesResult.setBounds(107, 45, 50, 14);
		
		scrAvgPrecLabel = new JLabel("");
		scrAvgPrecLabel.setFont(new Font("Arial", Font.BOLD, 25));
		scrAvgPrecLabel.setBounds(panelsWidth - 70, 26, 50, 35);
		
		scrPanel.add(scoreCheckBox);
		scrPanel.add(scrAvgScoreLabel);
		scrPanel.add(scrAvgScoreResult);
		scrPanel.add(scrAvgGamesLabel);
		scrPanel.add(scrAvgGamesResult);
		scrPanel.add(scrAvgPrecLabel);
		
		topBar.add(homeBtn);
		topBar.add(linePanel);
		topBar.add(rtmPanel);
		topBar.add(scrPanel);
		
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
		
		//treeScrollPanel = new JScrollPane();
		//treeScrollPanel.setLayout(null);
		//treeScrollPanel.setBackground(Color.decode("0xFFFFD5"));
		//treeScrollPanel.setBounds(5, 5, 190, d.height - topBarHeight - 25);
		//treePanel.add(treeScrollPanel);
		
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
		
		inlineCheckBox.setText(appBundle.getString("_menuNotereading"));
		rhythmCheckBox.setText(appBundle.getString("_menuRythmreading"));
		scoreCheckBox.setText(appBundle.getString("_menuScorereading"));
		
		lineAvgScoreLabel.setText(appBundle.getString("_gameScore") + ":");
		rtmAvgScoreLabel.setText(appBundle.getString("_gameScore") + ":");
		scrAvgScoreLabel.setText(appBundle.getString("_gameScore") + ":");
		
		lineAvgGamesLabel.setText(appBundle.getString("_menuExercises") + ":");
		rtmAvgGamesLabel.setText(appBundle.getString("_menuExercises") + ":");
		scrAvgGamesLabel.setText(appBundle.getString("_menuExercises") + ":");
		
		
		if (statsList != null)
			treePanel.remove(treeScrollPanel);

		if (SDSfiles.length != 0)
		{
			DefaultMutableTreeNode mainNode = loadTree();
			statsList = new JTree(mainNode);
			statsList.setSelectionRow(statsList.getRowCount() - 1);
			statsList.addTreeSelectionListener(this);
			readStatsFile((DefaultMutableTreeNode)statsList.getLastSelectedPathComponent(), 
					currDir, SDSfiles[SDSfiles.length - 1]);
			selNode = (DefaultMutableTreeNode)statsList.getLastSelectedPathComponent();
			statsList.expandPath(new TreePath(selNode.getPath()));
		}
		else
		{
			DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(appBundle.getString("_noStatistics"));
			statsList = new JTree(mainNode);
		}
		statsList.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		statsList.setBackground(Color.decode("0xFFFFD5"));
		statsList.setBounds(5, 5, 170, getHeight() - topBarHeight - 30);
		
		treeScrollPanel = new JScrollPane(statsList);
		Border border = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		treeScrollPanel.setBorder(border);
		treePanel.add(treeScrollPanel);
		updateResults();
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == inlineCheckBox)
		{
			showGame[0] = inlineCheckBox.isSelected();
		}
		else if (ae.getSource() == rhythmCheckBox)
		{
			showGame[1] = rhythmCheckBox.isSelected();
		}
		else if (ae.getSource() == scoreCheckBox)
		{
			showGame[2] = scoreCheckBox.isSelected();
		}
		graphPanel.repaint();
		updateResults();
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
		DefaultTreeModel treeModel = (DefaultTreeModel)statsList.getModel();

        int leaves = node.getLeafCount();
        for (int l = 0; l < leaves; l++)
        	treeModel.removeNodeFromParent(node.getLastLeaf());
        currentStats.clear();

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
		  int daysFound = 0;
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
			  
			  if (statInfo[record.gameType][0] == -1) // set start day
				  statInfo[record.gameType][0] = record.day;
			  if (globalInfo[0] == -1)
				  globalInfo[0] = record.day;
			  if (record.day != statInfo[record.gameType][1]) // set end day
				  statInfo[record.gameType][1] = record.day;

			  if (record.day != globalInfo[1])
			  {
				  globalInfo[1] = record.day;
				  DefaultMutableTreeNode day = new DefaultMutableTreeNode(appBundle.getString("_day") + " " + Integer.toString(record.day));
				  treeModel.insertNodeInto(day, node, daysFound);
				  daysFound++;
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
		  System.out.println("--GLOBAL-- done. startDay: " + globalInfo[0] + ", endDay: " + globalInfo[1] + ", minScore: " + globalInfo[2] + ", maxScore: " + globalInfo[3]);
		}
		catch (Exception e)
		{
		  System.out.println("An exception occured while reading the file !!");	
		}
	}
	
	public void updateResults()
	{
		// TODO
		lineAvgScoreResult.setText("");
		rtmAvgScoreResult.setText("");
		scrAvgScoreResult.setText("");
		lineAvgGamesResult.setText("");
		rtmAvgGamesResult.setText("");
		scrAvgGamesResult.setText("");
		lineAvgPrecLabel.setText("");
		rtmAvgPrecLabel.setText("");
		scrAvgPrecLabel.setText("");
		
		long[][] AVGvalues = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
		
		
		for (int s = 0; s < currentStats.size(); s++)
		{
			statRecord tmpRec = currentStats.get(s);
			if ( showGame[tmpRec.gameType] == false) // if game disabled skip it
				continue;
			if (singleDay != -1 && tmpRec.day != singleDay)
				continue;
			AVGvalues[tmpRec.gameType][0] += tmpRec.totalScore;
			AVGvalues[tmpRec.gameType][1] += tmpRec.avgPrecision;
			AVGvalues[tmpRec.gameType][2]++;
		}
		if (AVGvalues[0][2] > 0)
		{
			lineAvgScoreResult.setText(Integer.toString((int)(AVGvalues[0][0] / AVGvalues[0][2])));
			lineAvgPrecLabel.setText(Integer.toString((int)(AVGvalues[0][1] / AVGvalues[0][2])) + "%");
			lineAvgGamesResult.setText(Integer.toString((int)(AVGvalues[0][2])));
		}
		if (AVGvalues[1][2] > 0)
		{
			rtmAvgScoreResult.setText(Integer.toString((int)(AVGvalues[1][0] / AVGvalues[1][2])));
			rtmAvgPrecLabel.setText(Integer.toString((int)(AVGvalues[1][1] / AVGvalues[1][2])) + "%");
			rtmAvgGamesResult.setText(Integer.toString((int)(AVGvalues[1][2])));
		}
		if (AVGvalues[2][2] > 0)
		{
			scrAvgScoreResult.setText(Integer.toString((int)(AVGvalues[2][0] / AVGvalues[2][2])));
			scrAvgPrecLabel.setText(Integer.toString((int)(AVGvalues[2][1] / AVGvalues[2][2])) + "%");
			scrAvgGamesResult.setText(Integer.toString((int)(AVGvalues[2][2])));
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
		        System.out.println("Node selected idx: " + parent.getIndex(node));

		        DefaultTreeModel treeModel = (DefaultTreeModel)statsList.getModel();

		        int leaves = selNode.getLeafCount();
		        for (int l = 0; l < leaves; l++)
		        	treeModel.removeNodeFromParent(selNode.getLastLeaf());
		        DefaultMutableTreeNode loading = new DefaultMutableTreeNode(appBundle.getString("_statsLoading"));
		        
		        treeModel.insertNodeInto(loading, selNode, 0);
		        //selNode.add(loading);
		        
		        singleDay = -1;
		        readStatsFile(node,	currDir, SDSfiles[parent.getIndex(node)]);
		        selNode = node;
		        statsList.expandPath(new TreePath(selNode.getPath()));
		        graphPanel.repaint();
		        updateResults();
		        //statsList.collapsePath(new TreePath(selNode.getPath()));
		    }
		    else if (node.getLevel() == 2)
		    {
		    	String[] lblFields = node.toString().split(" ");
		    	System.out.println("Day: " + lblFields[lblFields.length - 1] + " selected");
		    	singleDay = Integer.parseInt(lblFields[lblFields.length - 1]);
		    	graphPanel.repaint();
		    	updateResults();
		    }
	}

	protected void paintComponent(Graphics g) 
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		int panelsWidth = ((getWidth() - 100) / 3) - 5;
		int panelXPos = 90;
		
		topBar.setBounds(5, 5, getWidth() - 10, topBarHeight);
		linePanel.setBounds(panelXPos, 5, panelsWidth, 70);
		lineAvgPrecLabel.setBounds(panelsWidth - 70, 22, 60, 35);
		panelXPos += panelsWidth + 5;
		rtmPanel.setBounds(panelXPos, 5, panelsWidth, 70);
		rtmAvgPrecLabel.setBounds(panelsWidth - 70, 22, 60, 35);
		panelXPos += panelsWidth + 5;
		scrPanel.setBounds(panelXPos, 5, panelsWidth, 70);
		scrAvgPrecLabel.setBounds(panelsWidth - 70, 22, 60, 35);
		treePanel.setBounds(5, topBarHeight + 10, 210, getHeight() - topBarHeight - 15);
		treeScrollPanel.setBounds(7, 7, 196, getHeight() - topBarHeight - 30);
		//statsList.setBounds(10, 10, 180, getHeight() - topBarHeight - 50);
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
			g.setColor(this.getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.decode("0x222222"));
			((Graphics2D) g).setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND  ));
			
			int maxCount = 0; // total number of exercises of the selected games
			int maxScore = 0, minScore = 99999; // maximum and minimum score of the selected games
			int[] sCount = { 0, 0, 0 }; // keeps the number of played exercises for each game type 
			int[] lastEntryIdx = { 0, 0, 0 }; // keeps the last index of each game type - used only in daysView mode

			int xPos = 35;
			int yPos = 30;
			int graphH = getHeight() - 60;
			int graphW = getWidth() - 55;
			int xAxisStep = 0;
			boolean daysView = false;
			
			// draw axis
			g.drawLine(xPos, 30, xPos, graphH + 30);
			g.drawLine(xPos, 30 + graphH, xPos + graphW, 30 + graphH);
			
			int tmpDay = 0;
			int tmpExCount = 0;
			int exerciseMaxCount = 0;
			int daysPlayed = 0;
			
			if (currentStats.size() == 0)
				return;

			// pre-parse to identify score bounds and maximum number of exercises in a day
			for (int d = 0; d < currentStats.size(); d++)
			{
				statRecord tmpRec = currentStats.get(d);
				if (showGame[tmpRec.gameType] == false)
					continue;
				if (singleDay != -1 && tmpRec.day != singleDay)
					continue;

				maxCount++;
				sCount[tmpRec.gameType]++;
				
				if (singleDay == -1) // count the exercises only in the month view
				{
				  if (tmpRec.day != tmpDay)
				  {
					tmpDay = tmpRec.day;
					tmpExCount = 0;
					daysPlayed++;
				  }
				  tmpExCount++;
				  if (tmpExCount > exerciseMaxCount)
					  exerciseMaxCount = tmpExCount;
				}

				if (tmpRec.totalScore > maxScore)
					maxScore = tmpRec.totalScore;
				if (tmpRec.totalScore < minScore)
					minScore = tmpRec.totalScore;
				if (d != lastEntryIdx[tmpRec.gameType])
					lastEntryIdx[tmpRec.gameType] = d;
			}

			System.out.println("[Pre-parse done] maxCount = " + maxCount + ", daysPlayed: "+ daysPlayed + ", exerciseMaxCount: " + exerciseMaxCount);
			if (maxCount > 1)
				xAxisStep =  graphW / (maxCount - 1);
			System.out.println("---> xAxisStep = " + xAxisStep);

			if (xAxisStep < 10 && xAxisStep != 0)
			{
				maxCount = daysPlayed;
				xAxisStep = graphW / (maxCount - 1);
				daysView = true;
			}
			
			// draw X axis scale lines
			for (int d = 0, tmpX = xPos + xAxisStep; d < maxCount; d++, tmpX += xAxisStep)
			{
				((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND  ));
				g.setColor(Color.lightGray);
				g.drawLine(tmpX + 2, 30, tmpX + 2, 30 + graphH);
				((Graphics2D) g).setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND  ));
				g.setColor(Color.black);
				g.drawLine(tmpX + 2, 30 + graphH, tmpX + 2, 35 + graphH);
			}
			
			int scoreDiff = maxScore - minScore;
			int scoreStep = scoreDiff / 10;
			
			if (scoreDiff == 0)
				scoreDiff = 1;
			
			// draw Y axis labels
			g.setFont(new Font("Arial", Font.PLAIN, 12));
			if (maxCount == 1)
			{
				g.drawString(Integer.toString(maxScore), 0, yPos + (graphH / 2));
				g.drawLine(32, yPos + (graphH / 2), 35, yPos + (graphH / 2));
			}
			else
			{
			  for (int y = yPos, c = 0; y < yPos + graphH - 5; y+=((graphH - 5) / 10), c++)
			  {
				g.drawString(Integer.toString(maxScore - (c * scoreStep)), 0, y + 5);
				g.drawLine(32, y, 35, y);
			  }
			}
			
			xPos = 37;
			int[][] lastPos = { { 0, 0 }, { 0, 0 }, { 0, 0 } }; // keeps last x,y position to trace a line
			long[][] prevInfo = { { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 } }; // in daysView mode use this to elapse average score (total score, count, xpos, prevDay) 
			int currentDay = 0; // used only to draw days labels
			
			// scan the whole stats list (skipping disabled data) and display the chart
			for (int s = 0; s < currentStats.size(); s++)
			{
				statRecord tmpRec = currentStats.get(s);
				if ( showGame[tmpRec.gameType] == false || sCount[tmpRec.gameType] == 0) // if game disabled or no entries 
					continue;
				if (singleDay != -1 && tmpRec.day != singleDay)
					continue;

				int relYPos = 0;

				if (daysView == false)
				{
				  relYPos = ((tmpRec.totalScore - minScore) * (graphH - 5)) / scoreDiff;
				  relYPos = graphH - 5 - relYPos;
				  //System.out.println("count = " + sCount[tmpRec.gameType]);
				  if (sCount[tmpRec.gameType] == 1)
				  {
				    g.setColor(getGameColor(tmpRec.gameType));
				    g.drawLine(37, yPos + (graphH / 2), 37 + graphW, yPos + (graphH / 2));
				    continue;
				  }
				  g.setColor(getGameColor(tmpRec.gameType));
				  if (lastPos[tmpRec.gameType][0] != 0 && lastPos[tmpRec.gameType][1] != 0)
					  g.drawLine(lastPos[tmpRec.gameType][0], lastPos[tmpRec.gameType][1], xPos, yPos + relYPos);
				  else
					  g.drawLine(37, yPos + relYPos, xPos, yPos + relYPos);
				  
				  if (tmpRec.day != prevInfo[tmpRec.gameType][3])
				  {
					  g.setColor(Color.black);
					  g.drawString(Integer.toString(tmpRec.day), xPos - 4, graphH + 50);
					  prevInfo[tmpRec.gameType][3] = tmpRec.day;
				  }
				  lastPos[tmpRec.gameType][0] = xPos;
				  lastPos[tmpRec.gameType][1] = yPos + relYPos;
				  xPos += xAxisStep;
				}
				else // ********************* daysView - draw average values for each day *****************************************************
				{
				  if (prevInfo[tmpRec.gameType][3] == 0) // last day not set yet
				  {
					  prevInfo[tmpRec.gameType][3] = tmpRec.day;
					  lastPos[tmpRec.gameType][0] = xPos;
					  if (tmpRec.day != currentDay)
					  {
						  if (xPos > 37)
							  xPos += xAxisStep;
						  // draw day label on X axis
						  g.setColor(Color.black);
						  g.drawString(Integer.toString(tmpRec.day), xPos - 4, graphH + 50);
						  currentDay = tmpRec.day;
					  }
				  }
				  else if (tmpRec.day != prevInfo[tmpRec.gameType][3] || s == lastEntryIdx[tmpRec.gameType])
				  {
					//System.out.println("avgScore: " + prevInfo[tmpRec.gameType][0]/prevInfo[tmpRec.gameType][1]);
					if (prevInfo[tmpRec.gameType][0] == 0)
						relYPos = 0;
					else
						relYPos = (((int)(prevInfo[tmpRec.gameType][0]/prevInfo[tmpRec.gameType][1]) - minScore) * (graphH - 5)) / scoreDiff;
					relYPos = graphH - 5 - relYPos;
					if (sCount[tmpRec.gameType] == 1)
					  {
						relYPos = graphH - 5 - (((tmpRec.totalScore - minScore) * (graphH - 5)) / scoreDiff);
					    g.setColor(getGameColor(tmpRec.gameType));
					    g.drawLine(37, yPos + relYPos, 37 + graphW, yPos + relYPos);
					    continue;
					  }
					
			    	if (prevInfo[tmpRec.gameType][2] != 0)
			    	{
			    		g.setColor(getGameColor(tmpRec.gameType));
			    		g.drawLine(lastPos[tmpRec.gameType][0], lastPos[tmpRec.gameType][1], (int)prevInfo[tmpRec.gameType][2], yPos + relYPos);
			    		lastPos[tmpRec.gameType][0] = (int)prevInfo[tmpRec.gameType][2];
				    	lastPos[tmpRec.gameType][1] = yPos + relYPos;
			    	}
			    	else
				    	lastPos[tmpRec.gameType][1] = yPos + relYPos;

					if (s == lastEntryIdx[tmpRec.gameType])
					{
						//System.out.println("[Last index] gameType: " + tmpRec.gameType + ", day: " + tmpRec.day + ", last x pos:" + lastPos[tmpRec.gameType][0]);
						//System.out.println("[Last index] lastDay: " + prevInfo[tmpRec.gameType][3] + ", last score: " + prevInfo[tmpRec.gameType][0]);
						if (tmpRec.day != prevInfo[tmpRec.gameType][3])
						{
							prevInfo[tmpRec.gameType][0] = tmpRec.totalScore;
							prevInfo[tmpRec.gameType][1] = 1;
						}
						else
						{
							prevInfo[tmpRec.gameType][0] += tmpRec.totalScore;
							prevInfo[tmpRec.gameType][1]++;
						}
						relYPos = (((int)(prevInfo[tmpRec.gameType][0]/prevInfo[tmpRec.gameType][1]) - minScore) * (graphH - 5)) / scoreDiff;
						relYPos = graphH - 5 - relYPos;
						g.setColor(getGameColor(tmpRec.gameType));
			    		g.drawLine(lastPos[tmpRec.gameType][0], lastPos[tmpRec.gameType][1], xPos + xAxisStep, yPos + relYPos);
					}

					if (tmpRec.day != currentDay)
					{
				      currentDay = tmpRec.day;
				      xPos += xAxisStep;
					  // draw day label on X axis
					  g.setColor(Color.black);
					  g.drawString(Integer.toString(currentDay), xPos - 4, graphH + 50);
					}

			    	// reset counters
			    	prevInfo[tmpRec.gameType][0] = tmpRec.totalScore;
					prevInfo[tmpRec.gameType][1] = 1;
					prevInfo[tmpRec.gameType][2] = xPos;
					prevInfo[tmpRec.gameType][3] = tmpRec.day;
					continue;
				  }
				  
				  prevInfo[tmpRec.gameType][0] += tmpRec.totalScore;
				  prevInfo[tmpRec.gameType][1]++;
				}
			}

			// draw next day TODO: improve this
			//g.setColor(Color.black);
			//g.drawString(Integer.toString(prevDay + 1), xPos - xAxisStep - 4, graphH + 50);
		}
	}

	
}




