import java.util.Vector;

public class NoteGenerator 
{
	Preferences appPrefs;
	Accidentals accidentals;

	int baseNote = 24; // pitch of C0
	int TREBLE_CLEF_BASEPITCH = 53;
	int BASS_CLEF_BASEPITCH = 33;
	int ALTO_CLEF_BASEPITCH = 43;
	int TENOR_CLEF_BASEPITCH = 40;
	
	// intervals from base note to build chords or intervals 
	char[][] chordsIntervals = { { 4 , 7 },   // major
								 { 3 , 7 },   // minor
								 { 3 , 6 },   // diminished
								 { 4 , 8 } }; // augmented
	
	char[] intervals = { 0, 1, 2, 4, 5, 7, 9, 11, 12 };
	
	int clefMask = -1;
	int baseRangeClef;
	int addRangeIndex; // index of randomPitchList of a second range (if added)
	int addRangeClef; // clef mask of a second range (if added)
	
	private int timeSignNumerator = 0;
	private int timeSignDenominator = 0;

	Vector<Integer> baseList = new Vector<Integer>();	// list holding the whole list of notes, from C0 to C6
	Vector<Integer> alteredList = new Vector<Integer>();	// list holding the whole list of laterated notes, from C0 to C6
	Vector<Note> randomPitchList = new Vector<Note>(); // list of notes holding the user selected notes
	Vector<Integer> notesTypeList = new Vector<Integer>(); // list of note types to facilitate random generation
	
    /*                           C   D   E   F   G   A   B  */
    /*                         ---------------------------- */
    char[][] sharpsMatrix = { {  0,  2,  4,  5,  7,  9, 11 }, // offsets from the octave first note
	   					      {  0,  0,  0,  1,  0,  0,  0 }, // 1 alteration
						      {  1,  0,  0,  1,  0,  0,  0 }, // 2 alterations
						      {  1,  0,  0,  1,  1,  0,  0 }, // 3 alterations
						      {  1,  1,  0,  1,  1,  0,  0 }, // 4 alterations
						      {  1,  1,  0,  1,  1,  1,  0 }, // 5 alterations
						      {  1,  1,  1,  1,  1,  1,  0 }, // 6 alterations
						      {  1,  1,  1,  1,  1,  1,  1 }  // 7 alterations
						    };

    /*                          C   D   E   F   G   A   B  */
    /*                        ---------------------------- */
    char[][] flatsMatrix = { {  0,  2,  4,  5,  7,  9, 11 }, // offsets from the octave first note
	     				     {  0,  0,  0,  0,  0,  0,  1 }, // 1 alteration
		     			     {  0,  0,  1,  0,  0,  0,  1 }, // 2 alterations
		     			     {  0,  0,  1,  0,  0,  1,  1 }, // 3 alterations
		     			     {  0,  1,  1,  0,  0,  1,  1 }, // 4 alterations
		     			     {  0,  1,  1,  0,  1,  1,  1 }, // 5 alterations
		     			     {  1,  1,  1,  0,  1,  1,  1 }, // 6 alterations
		     			     {  1,  1,  1,  1,  1,  1,  1 }  // 7 alterations
		   				   };
    
    public NoteGenerator(Preferences p, Accidentals acc) 
    {
    	this.appPrefs = p;
        this.accidentals = acc;
        
        baseRangeClef = -1;
    	addRangeIndex = -1;
    	addRangeClef = -1; 

        randomPitchList.clear();
        initLists();
    }
    
    public void initLists()
    {
    	int pitch = baseNote; // starts from C0
    	for (int i = 0; i < 6; i++) // 6 octaves, from C0 to C6
    	{
    		for (int j = 0; j < 7; j++) // 7 notes per octave
    		{
    			baseList.add(pitch + sharpsMatrix[0][j]);
    			if (this.accidentals == null || this.accidentals.getType() == "")
    				alteredList.add(pitch + sharpsMatrix[0][j]);
    			else if (this.accidentals.getType() == "#")
    				alteredList.add(pitch + sharpsMatrix[0][j] + sharpsMatrix[this.accidentals.getNumber()][j]);
    			else if (this.accidentals.getType() == "b")
    				alteredList.add(pitch + flatsMatrix[0][j] - flatsMatrix[this.accidentals.getNumber()][j]);
    			
    		}
    		pitch+=12;
    	}
    	//System.out.print(baseList);
    }

    /*
     * Functions used to convert ClefSelector levels to real pitches (and vice versa),
     * to be saved on preferences.
     * Also used to calculate Y position when creating a new note 
     */
    public int getPitchFromLevel(int clefLowestPitch, int level)
    {
    	int i = baseList.indexOf(clefLowestPitch);
    	return baseList.get(i + level);
    }

    public int getIndexFromPitch(int clefLowestPitch, int pitch, boolean alterated)
    {
    	int i;
   		i = baseList.indexOf(clefLowestPitch);
    	int j = 0;
    	for (j = i; j < baseList.size(); j++)
    	{
    		int p;
    		if (alterated == true)
    			p = alteredList.get(j);
    		else
    			p = baseList.get(j);
    		if (p == pitch)
    			return j - i;
    	}
    	return 0;
    }
    
    public int getRowsDistance()
    {
    	int height = 40;
    	int tmpH = randomPitchList.size() * 5;
    	if (addRangeIndex != -1) // means there are 2 clefs
    	{
    		if (tmpH < 100) tmpH = 100;
    		height += tmpH + 40;
    	}
    	else
    	{
    		if (tmpH < 50) tmpH = 50;
    		height += tmpH;
    	}
    		
    	return height;
    }
    /* ************************************************************** */

    public void reset()
    {
    	//wholeList.clear();
    	randomPitchList.clear();
    	baseRangeClef = -1;
    	addRangeIndex = -1;
    	addRangeClef = -1; 
    }
    
    public void update()
    {
    	int accIdx = Integer.parseInt(appPrefs.getProperty("accidentals"));
		if (accIdx <= 0) accidentals.setTypeAndCount("", 0);
		else if (accIdx < 8) accidentals.setTypeAndCount("#", accIdx);
		else accidentals.setTypeAndCount("b", accIdx - 7);
		reset();
		
		// reinit whole list with new accidentals
		baseList.clear();
		alteredList.clear();
    	initLists();

		clefMask = Integer.parseInt(appPrefs.getProperty("clefsMask"));
		if (clefMask == -1) clefMask = appPrefs.TREBLE_CLEF;
		if ((clefMask & appPrefs.TREBLE_CLEF) > 0) 
		{
			int lowerPitch = Integer.parseInt(appPrefs.getProperty("trebleClefLower"));
			int higherPitch = Integer.parseInt(appPrefs.getProperty("trebleClefUpper"));
			if (lowerPitch == -1) lowerPitch = 64; // default, set to E3
			if (higherPitch == -1) higherPitch = 77; // default, set to F4
			addRange(appPrefs.TREBLE_CLEF, alteredList.get(baseList.indexOf(lowerPitch)), alteredList.get(baseList.indexOf(higherPitch)));
		}
		if ((clefMask & appPrefs.BASS_CLEF) > 0) 
		{
			int lowerPitch = Integer.parseInt(appPrefs.getProperty("bassClefLower"));
			int higherPitch = Integer.parseInt(appPrefs.getProperty("bassClefUpper"));
			if (lowerPitch == -1) lowerPitch = 43; // default, set to G1
			if (higherPitch == -1) higherPitch = 57; // default, set to A2
			addRange(appPrefs.BASS_CLEF, alteredList.get(baseList.indexOf(lowerPitch)), alteredList.get(baseList.indexOf(higherPitch)));
		}
		if ((clefMask & appPrefs.ALTO_CLEF) > 0) 
		{
			int lowerPitch = Integer.parseInt(appPrefs.getProperty("altoClefLower"));
			int higherPitch = Integer.parseInt(appPrefs.getProperty("altoClefUpper"));
			if (lowerPitch == -1) lowerPitch = 53; // default, set to F2
			if (higherPitch == -1) higherPitch = 67; // default, set to G3
			addRange(appPrefs.ALTO_CLEF, alteredList.get(baseList.indexOf(lowerPitch)), alteredList.get(baseList.indexOf(higherPitch)));
		}
		if ((clefMask & appPrefs.TENOR_CLEF) > 0) 
		{
			int lowerPitch = Integer.parseInt(appPrefs.getProperty("tenorClefLower"));
			int higherPitch = Integer.parseInt(appPrefs.getProperty("tenorClefUpper"));
			if (lowerPitch == -1) lowerPitch = 50; // default, set to D2
			if (higherPitch == -1) higherPitch = 64; // default, set to E3
			addRange(appPrefs.TENOR_CLEF, alteredList.get(baseList.indexOf(lowerPitch)), alteredList.get(baseList.indexOf(higherPitch)));
		}
		
		notesTypeList.clear();
		int nType = Integer.parseInt(appPrefs.getProperty("wholeNote"));
		if (nType == -1 || nType == 1)
			notesTypeList.add(0);
		nType = Integer.parseInt(appPrefs.getProperty("halfNote")); 
		if (nType == -1 || nType == 1)
			notesTypeList.add(1);
		if (Integer.parseInt(appPrefs.getProperty("quarterNote")) == 1)
			notesTypeList.add(2);
		if (Integer.parseInt(appPrefs.getProperty("eighthNote")) == 1)
			notesTypeList.add(3);
		if (Integer.parseInt(appPrefs.getProperty("tripletNote")) == 1)
			notesTypeList.add(4);
		nType = Integer.parseInt(appPrefs.getProperty("silenceNote"));
		if(nType == -1 || nType == 1)
			notesTypeList.add(5);

		int tsIdx = Integer.parseInt(appPrefs.getProperty("timeSignature"));
		if (tsIdx <= 0)	{ timeSignNumerator = 4; timeSignDenominator = 4; }
		else if (tsIdx == 1) { timeSignNumerator = 2; timeSignDenominator = 4; }
		else if (tsIdx == 2) { timeSignNumerator = 3; timeSignDenominator = 4; }
		else if (tsIdx == 3) { timeSignNumerator = 6; timeSignDenominator = 8; }
    }
    
    public int getClefMask()
    {
    	return clefMask;
    }
    
    private int getLevelFromClefAndPitch(int clef, int pitch)
    {
    	if (clef == appPrefs.TREBLE_CLEF)
    		return 20 - getIndexFromPitch(TREBLE_CLEF_BASEPITCH, pitch, true);
    	else if (clef == appPrefs.BASS_CLEF)
    		return 20 - getIndexFromPitch(BASS_CLEF_BASEPITCH, pitch, true);
    	else if (clef == appPrefs.ALTO_CLEF)
    		return 20 - getIndexFromPitch(ALTO_CLEF_BASEPITCH, pitch, true);
    	else if (clef == appPrefs.TENOR_CLEF)
    		return 20 - getIndexFromPitch(TENOR_CLEF_BASEPITCH, pitch, true);

    	return 0;
    }
    
    private int getPitchFromClefAndLevel(int clef, int level)
    {
    	if (clef == appPrefs.TREBLE_CLEF)
    		return getPitchFromLevel(TREBLE_CLEF_BASEPITCH, 20 - level);
    	else if (clef == appPrefs.BASS_CLEF)
    		return getPitchFromLevel(BASS_CLEF_BASEPITCH, 20 - level);
    	else if (clef == appPrefs.ALTO_CLEF)
    		return getPitchFromLevel(ALTO_CLEF_BASEPITCH, 20 - level);
    	else if (clef == appPrefs.TENOR_CLEF)
    		return getPitchFromLevel(TENOR_CLEF_BASEPITCH, 20 - level);

    	return 0;
    }

    // add notes to randomPitchList. lower and upper pitches must be NOT alterated
    public void addRange(int clef, int lower, int upper)
    {
    	//int altIndex = this.accidentals.getNumber();
    	//String accType = this.accidentals.getType(); 
    	int lowIdx = alteredList.indexOf(lower);
    	int highIdx = alteredList.indexOf(upper);
    	boolean secondRow = false;
    	
    	// find the index on sharpsMatrix/flatsMatrix to add correctly alterated notes
    	int matrixIdx = lowIdx % 7;
    	
    	System.out.println("[NG addRange] clef: " + clef + ", lower: " + lower + ", upper: " + upper);

    	if (randomPitchList.size() == 0)
    		baseRangeClef = clef;
    	else
    	{
    		addRangeClef = clef;
    		addRangeIndex = randomPitchList.size();
    		secondRow = true;
    	}	
    	
    	for (int i = lowIdx; i < highIdx + 1; i++)
    	{
    		int pitch = alteredList.get(i);
    		//int altType = pitch - baseList.get(i); 

    		int level = getLevelFromClefAndPitch(clef, pitch);
        	
    		Note tmpNote = new Note(0, clef, level, pitch, 0, secondRow, 0 /*altType*/);
    		randomPitchList.add(tmpNote);
    			
			if (matrixIdx == 6)	matrixIdx = 0;
	  		else matrixIdx++;
    	}

    	for (int n = 0; n < randomPitchList.size(); n++)
    		System.out.print(randomPitchList.get(n).pitch + ", ");
    	System.out.println("");
    }
    
    public int getFirstLowPitch()
    {
    	if (randomPitchList.size() == 0)
    		return -1;
    	return randomPitchList.get(0).pitch;
    }
    
    public int getFirstHighPitch()
    {
    	if (randomPitchList.size() == 0)
    		return -1;
    	if (addRangeIndex != -1)
    		return randomPitchList.get(addRangeIndex - 1).pitch;
    	else
    		return randomPitchList.get(randomPitchList.size() - 1).pitch;
    }
    
    public int getSecondLowPitch()
    {
    	if (randomPitchList.size() == 0 || addRangeIndex == -1)
    		return -1;
    	return randomPitchList.get(addRangeIndex).pitch;
    }
    
    public int getSecondHighPitch()
    {
    	if (randomPitchList.size() == 0 || addRangeIndex == -1)
    		return -1;
    	return randomPitchList.get(randomPitchList.size() - 1).pitch;
    }
    
    public boolean isAlterated(int noteIdx)
    {
    	if (accidentals.getType() == "#" && sharpsMatrix[accidentals.getNumber()][noteIdx] == 1)
    		return true;
    	else if (accidentals.getType() == "b" && flatsMatrix[accidentals.getNumber()][noteIdx] == 1)
    		return true;
    	return false;
    }
    
    private int getRhythmPitch(int clef)
    {
		if (clef == appPrefs.TREBLE_CLEF)
			return baseList.get(baseList.indexOf(TREBLE_CLEF_BASEPITCH) + 10);
		else if (clef == appPrefs.BASS_CLEF)
			return baseList.get(baseList.indexOf(BASS_CLEF_BASEPITCH) + 10);
		else if (clef == appPrefs.ALTO_CLEF)
			return baseList.get(baseList.indexOf(ALTO_CLEF_BASEPITCH) + 10);
		else if (clef == appPrefs.TENOR_CLEF)
			return baseList.get(baseList.indexOf(TENOR_CLEF_BASEPITCH) + 10);
		
		return 71;
    }
    
    public Note getRandomNote(int forcedType)
    {
    	int randIdx = (int) (randomPitchList.size() * Math.random());
    	Note tmpNote  = randomPitchList.get(randIdx);
    	int type = forcedType;
    	if (forcedType == -1)
    		type = notesTypeList.get((int) (notesTypeList.size() * Math.random()));
    	Note randNote = new Note(0, tmpNote.clef, tmpNote.level, tmpNote.pitch, type, tmpNote.secondRow, tmpNote.altType);
    	randNote.type = type;
    	randNote.duration = randNote.getDuration(type);

    	//System.out.println("[NG getRandomNote] clef: " + baseRangeClef + ", addRangeIndex: " + addRangeIndex);
    	//System.out.println("selClef: " + selClef + ", level: " + level);
    	//System.out.println("New random pitch = " + pitch);

    	return randNote;
    }
    
    public Note getTripletRandomNote(int basePitch)
    {
    	int baseIndex = 0;
    	int lowerIndex = 0;
    	int upperIndex = randomPitchList.size();
    	int delta = 4; // within +2 and -2 tones from basePitch
    	int shift = -2; // index shift from base note

    	for (int i = 0; i < upperIndex; i++)
    	{
    		if (randomPitchList.get(i).pitch == basePitch)
    		{
    			baseIndex = i;
    			break;
    		}
    	}
    	
    	if (addRangeIndex != -1)
    	{
    		if (baseIndex >= addRangeIndex) // triplet base note is on the second range
    			lowerIndex = addRangeIndex;
    		else
    			upperIndex = addRangeIndex - 1; // triplet base note is on the first range 
    	}
    	if (baseIndex - lowerIndex < 2)
    		shift += (2 - (baseIndex - lowerIndex));
    	if (upperIndex - baseIndex < 2)
    		delta -= (upperIndex - baseIndex);
    	int randIndex = baseIndex + shift + (int)(Math.random() * delta);
    	Note baseNote = randomPitchList.get(baseIndex);
    	Note newNote = randomPitchList.get(randIndex);
    	Note randNote = new Note(0, baseNote.clef, newNote.level, newNote.pitch, 4, baseNote.secondRow, newNote.altType);

    	//System.out.println("Triplet base: " + basePitch + ", baseIndex: " + baseIndex + ", randIdx: " + randIndex);
    	//System.out.println("Triplet new note: " +  randNote.pitch + ", level: " + randNote.level + ", dur: " + randNote.duration);
    	
    	return randNote;
    }
    
    public void getRandomSequence(Vector<Note> seq, int measuresNumber, boolean isRhythm)
    {
    	double measureCounter = 0;
    	boolean eighthPresent = false;
    	seq.clear(); // clear sequence in case it was already set
    	for (int i = 0; i < measuresNumber; i++)
    	{
    		measureCounter = timeSignNumerator;
    		eighthPresent = false;
    		while (measureCounter != 0)
    		{
    			Note tmpNote = getRandomNote(-1);
    			if (tmpNote.type == 4) // triplet
    			{
    				
    				if (measureCounter < 1 || eighthPresent == true)
    					continue;
    				Note secondNote = getTripletRandomNote(tmpNote.pitch);
    				Note thirdNote = getTripletRandomNote(tmpNote.pitch);
    				if (isRhythm == true)
    				{
    					tmpNote.level = secondNote.level = thirdNote.level = 10;
    					tmpNote.pitch = secondNote.pitch = thirdNote.pitch = getRhythmPitch(tmpNote.clef);
    				}
    				
    				int tripletLevel = tmpNote.level;
    				int mult = 1;
    				if (isRhythm == false)
    				{
    				  if (tmpNote.level >= 10) // oriented upward. Find the lowest level
    				  {
    					if (secondNote.level < tripletLevel) tripletLevel = secondNote.level;
    					if (thirdNote.level < tripletLevel) tripletLevel = thirdNote.level;
    				  }
    				  else // oriented downward. Find the highest level
    			 	  {
    					if (secondNote.level > tripletLevel) tripletLevel = secondNote.level;
    					if (thirdNote.level > tripletLevel) tripletLevel = thirdNote.level;
    					mult = -1; // negative values will tell the renderer to stretch downward
    				  }
    				  tripletLevel *= mult;
    				}
    				
    				tmpNote.setTripletValue(tripletLevel);
    				secondNote.setTripletValue(tripletLevel + (mult * 1000));
    				thirdNote.setTripletValue(tripletLevel + (mult * 1000));
    				
					measureCounter--; // one quarter for the triplet group
    				seq.add(tmpNote);
    				System.out.println("Random Note: #" + seq.size() + ": Pitch: " + tmpNote.pitch + ", level: " + tmpNote.level);
    				seq.add(secondNote);
    				System.out.println("Random Note: #" + seq.size() + ": Pitch: " + secondNote.pitch + ", level: " + secondNote.level);
    				seq.add(thirdNote);
    				System.out.println("Random Note: #" + seq.size() + ": Pitch: " + thirdNote.pitch + ", level: " + thirdNote.level);
    				continue;
    				
    			}
    			else if (tmpNote.type == 5) // if this is silence, then set a random duration
    			{
    				tmpNote.duration = tmpNote.getDuration((int)((notesTypeList.size() - 1) * Math.random()));
    				if (tmpNote.duration == (1.0 / 3.0))
    					continue;
    			}

    			if (isRhythm == true)
    			{
    				tmpNote.level = 10;
    				//tmpNote.ypos = 62;
    				tmpNote.pitch = getRhythmPitch(tmpNote.clef);
    			}

    			//System.out.println("Generated note pitch: " + tmpNote.pitch + ", duration: " + tmpNote.duration);
    			if (tmpNote.duration <= measureCounter)
    			{
    				measureCounter -= tmpNote.duration;
    				seq.add(tmpNote);
    				if (tmpNote.type == 3)
    					eighthPresent = true;
    				System.out.println("Random Note: #" + seq.size() + ": Pitch: " + tmpNote.pitch + ", level: " + tmpNote.level); 
    			}
    			//System.out.println("tempMesCnt: " + measureCounter);
    		}
    	}
    }
    
    public int getRandomChordorInterval(Vector<Note> seq, int xpos, boolean chord, int intervalDegree)
    {
    	char notesWanted = 1;
    	//int randType = (int)(Math.random() * 4); // 0 major, 1 minor, 2 diminished, 3 augmented
    	int randType = 0;
    	if (chord == true) 
    		randType = (int)(Math.random() * 2); // 0 major, 1 minor
    	else
    		randType = (int)(Math.random() * 2) - 1; // -1 diminished, 0 exact, 1 augmented
    	
    	int[] addNotes = { 0, 0 };
    	Note baseNote = getRandomNote(0);
    	
    	System.out.println("[getRandomChordorInterval] randType: " + randType);
    	
    	baseNote.xpos = xpos;
    	seq.add(baseNote);
    	
    	if (chord == true) 
    		notesWanted = 2;

    	for (int i = 0; i < notesWanted; i++)
    	{
    		if (chord == true)
    			addNotes[i] = baseNote.pitch + chordsIntervals[randType][i];
    		else
    		{
    			addNotes[i] = baseNote.pitch + intervals[intervalDegree] + randType;
    		}
    		/*
    		int newPitch = addNotes[i];
    		
    		int altType = 0;
        	int addIndex = baseList.indexOf(addNotes[i]);
        	if (addIndex == -1)
        	{
        		addIndex = alteredList.indexOf(addNotes[i]);
        		// worst case. Chord note is not present in any list. Find the closest match
        		if (addIndex == -1)
        		{
        	    	int baseNoteIdx = alteredList.indexOf(baseNote.pitch);
        			for (int j = baseNoteIdx; j < baseList.size(); j++)
        			{
        				if (baseList.get(j) > addNotes[i])
        				{
        					addIndex = j - 1;
        					newPitch = baseList.get(addIndex);
        					break;
        				}
        			}
        			altType = 1;
        		}
        		else
        			altType = alteredList.get(addIndex) - baseList.get(addIndex);
        	}
        	*/

        	//int level = getLevelFromClefAndPitch(baseNote.clef, newPitch);
    		int level = 0;
    		if (chord == true)
    			level = baseNote.level - (2 * (i + 1)); // calculate 3rd and 5th position
    		else
    			level = baseNote.level - intervalDegree + 1;
        	int addNoteBasePitch = getPitchFromClefAndLevel(baseNote.clef, level);
        	int addNoteIdx = baseList.indexOf(addNoteBasePitch);
        	int altType = addNotes[i] - alteredList.get(addNoteIdx);
        	if (altType != 0 && alteredList.get(addNoteIdx) != baseList.get(addNoteIdx))
        		altType = 2; // there is an accidental on key. Must show a natural 

    		//System.out.println("addNotes: " + addNotes[i] + ", idx: " + addIndex + ", lev: " + level);

        	Note newNote = new Note(xpos, baseNote.clef, level, addNotes[i], 0, baseNote.secondRow, altType);
        	seq.add(newNote);
    	}
    	
    	return randType;
    }
}
