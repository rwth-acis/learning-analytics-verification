package i5.las2peer.services.privacyControl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import i5.las2peer.serialization.MalformedXMLException;
import i5.las2peer.serialization.XmlTools;

/**
 * ConsentLevel
 * 
 * Class that defines object for a certain level of consent.
 * Includes information about the functions (e.g. from a LMS) that are included.
 * 
 */
public class ConsentLevel {

	private String name;
	private int level;
	private List<String> functions;
	private LocalDate timestamp;
	
	public ConsentLevel(String name, int level, List<String> functions, LocalDate timestamp) {
		this.name = name;
		this.level = level;
		this.functions = functions;
		this.timestamp = timestamp;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public List<String> getFunctions() {
		return functions;
	}
	
	public void setFunctions(List<String> functions) {
		this.functions = functions;
	}
	
	/**
	 * Sets the state of the object based on a read xml representation.
	 *
	 * @param root parsed XML document
	 * @return Returns a new ConsentLevel instance
	 * @throws MalformedXMLException
	 */
	public static ConsentLevel createFromXml(Element root) throws MalformedXMLException {
		// read name field from XML
		Element elName = XmlTools.getSingularElement(root, "name");
		String name = elName.getTextContent();
		// read level field from XML
		Element elLevel = XmlTools.getSingularElement(root, "level");
		int level = Integer.valueOf(elLevel.getTextContent());
		// read function fields from XML
		Element elFunctions = XmlTools.getSingularElement(root, "functions");
		List<Element> elements = XmlTools.getElementList(elFunctions, "function");
		List<String> functions = new ArrayList<>();
		for (Element el : elements) {
			functions.add(el.getTextContent());
		}
		
		ConsentLevel result = new ConsentLevel(name, level, functions, LocalDate.now());
		return result;
	}
}
