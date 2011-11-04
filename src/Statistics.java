
public class Statistics 
{
	private int notesPlayed;
	private int correctAnswers;
	private int wrongAnswers;
	private int wrongRhythms;
	
	private int totalScore;
	private int precisionAmount;
	private int avgPrecision;
	
	public Statistics()
	{
		reset();
	}
	
	public void reset()
	{
		notesPlayed = 0;
		correctAnswers = 0;
		wrongAnswers = 0;
		wrongRhythms = 0;

		totalScore = 0;
		avgPrecision = 0;		
	}
	
	public void notePlayed(int answerType, int score)
	{
		notesPlayed++;
		if (answerType == 1)
		{
			correctAnswers++;
			precisionAmount+=100;
		}
		else if (answerType == 2)
		{
			wrongRhythms++;
			precisionAmount+=50;
		}
		else
			wrongAnswers++;
		totalScore+=score;

		avgPrecision = precisionAmount / notesPlayed;
	}
	
	public int getNotesPlayed()
	{
		return notesPlayed;
	}
	
	public int getCorrectNumber()
	{
		return correctAnswers;
	}

	public int getWrongNumber()
	{
		return wrongAnswers;
	}
	
	public int getWrongRhythms()
	{
		return wrongRhythms;
	}
	
	public int getTotalScore()
	{
		return totalScore;
	}

	public int getAveragePrecision()
	{
		return avgPrecision;
	}
}
