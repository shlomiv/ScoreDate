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

import java.util.Vector;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Exercise 
{
	Preferences appPrefs;
	
	int type; // 0 - notes in line, 1 - rhythm, 2 - score
	String title; // user defined exercise title
	int clefMask;
	Accidentals acc;
	int timeSign;
	int speed;
	Vector<Note> notes;
	
	public Exercise(Preferences p)
	{
		appPrefs = p;
		notes = new Vector<Note>();
		reset();
	}
	
	public void reset()
	{
		type = -1;
		title = "";
		clefMask = -1;
		acc = new Accidentals("", 0, appPrefs);
		timeSign = -1;
		speed = 60;
		notes.clear();
	}
	
	public void setType(int t)
	{
		type = t;
	}
	
	public void setTitle(String t)
	{
		System.out.println("Set exercise title to - " + t);
		title = t;
	}
	
	public void setClefMask(int mask)
	{
		System.out.println("Set exercise clefs mask to - " + mask);
		clefMask = mask;
	}
	
	public void setMeasure(int mes)
	{
		System.out.println("Set exercise measure to - " + mes);
		timeSign = mes;
	}
	
	public void setSpeed(int s)
	{
		System.out.println("Set exercise speed to - " + s);
		speed = s;
	}
	
	public void saveToXML()
	{
		try {
			 
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("exercise");
			doc.appendChild(rootElement);

			// ************************* HEADER FIELDS ***********************************
			Element staff = doc.createElement("header");
			rootElement.appendChild(staff);

			// set attribute to staff element
			//Attr attr = doc.createAttribute("id");
			//attr.setValue("1");
			//staff.setAttributeNode(attr);

			// shorten way
			// staff.setAttribute("id", "1");

			Element exTitle = doc.createElement("title");
			exTitle.appendChild(doc.createTextNode(title));
			staff.appendChild(exTitle);

			Element clef = doc.createElement("clef");
			clef.appendChild(doc.createTextNode(Integer.toString(clefMask)));
			staff.appendChild(clef);
	 
			Element accType = doc.createElement("accType");
			accType.appendChild(doc.createTextNode(acc.getType()));
			staff.appendChild(accType);
			
			Element accCount = doc.createElement("accCount");
			accCount.appendChild(doc.createTextNode(Integer.toString(acc.getNumber())));
			staff.appendChild(accCount);
	 
			Element measure = doc.createElement("measure");
			measure.appendChild(doc.createTextNode(Integer.toString(timeSign)));
			staff.appendChild(measure);
			
			Element exSpeed = doc.createElement("speed");
			exSpeed.appendChild(doc.createTextNode(Integer.toString(speed)));
			staff.appendChild(exSpeed);
			
			// ************************ SEQUENCE ****************************
			Element exNotes = doc.createElement("sequence");
			rootElement.appendChild(exNotes);
			
			for (int i = 0; i < notes.size(); i++)
			{
				Note tmpNote = notes.get(i);
				Element nType = doc.createElement("t");
				nType.appendChild(doc.createTextNode(Integer.toString(tmpNote.type)));
				exNotes.appendChild(nType);		

				Element nPitch = doc.createElement("p");
				nPitch.appendChild(doc.createTextNode(Integer.toString(tmpNote.pitch)));
				exNotes.appendChild(nPitch);
				
				Element nLevel = doc.createElement("l");
				nLevel.appendChild(doc.createTextNode(Integer.toString(tmpNote.level)));
				exNotes.appendChild(nLevel);
				
				Element nDur = doc.createElement("d");
				nDur.appendChild(doc.createTextNode(Double.toString(tmpNote.duration)));
				exNotes.appendChild(nDur);
				
				Element nClef = doc.createElement("c");
				nClef.appendChild(doc.createTextNode(Integer.toString(tmpNote.clef)));
				exNotes.appendChild(nClef);				
			}
	 
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(title + ".xml"));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			
			transformer.transform(source, result);
	 
			System.out.println("XML File successfully saved !");
	 
		  } catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		  } catch (TransformerException tfe) {
			tfe.printStackTrace();
		  }
	}
}
