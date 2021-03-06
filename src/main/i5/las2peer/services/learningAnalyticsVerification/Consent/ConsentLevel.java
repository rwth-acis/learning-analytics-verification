package i5.las2peer.services.learningAnalyticsVerification.Consent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import i5.las2peer.serialization.MalformedXMLException;
import i5.las2peer.serialization.XmlTools;

/**
 * ConsentLevel
 * 
 * Class that defines object for a certain level of consent.
 * Includes information about the services and actions that are included.
 * 
 */
public class ConsentLevel {

	private String name;
	private int level;
	private List<String> functions;
	private List<String> services;
	private LocalDate timestamp;
	private final String formattedOutput;

	public ConsentLevel(String name, int level, List<String> functions, List<String> services, LocalDate timestamp) {
		this.name = name;
		this.level = level;
		this.functions = functions;
		this.services = services;
		this.timestamp = timestamp;
		formattedOutput = formatStringOutput();
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

	public List<String> getServices() {
		return services;
	}

	public void setServices(List<String> services) {
		this.services = services;
	}
	
	private String formatStringOutput() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[");
		stringBuilder.append(getLevel());
		stringBuilder.append("] ");
		stringBuilder.append(getName());
		stringBuilder.append("\n");
		stringBuilder.append("Services: ");
		
		Iterator<String> iterator = getServices().iterator();
	    while (iterator.hasNext()) {
	        String service = iterator.next();
	        stringBuilder.append(service);
	        if (iterator.hasNext()) {
	        	stringBuilder.append(", ");
	        }
	     }
		
	    stringBuilder.append("\n");
	    stringBuilder.append("Aktionen: ");
		
		iterator = getFunctions().iterator();
	    while (iterator.hasNext()) {
	        String function = iterator.next();
	        stringBuilder.append(function);
	        if (iterator.hasNext()) {
	        	stringBuilder.append(", ");
	        }
	     }
	    stringBuilder.append("\n");
		return stringBuilder.toString();
	}
	
	@Override
	public String toString() {
		return formattedOutput;
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
		Element rootElem = XmlTools.getSingularElement(root, "functions");
		List<Element> elements = XmlTools.getElementList(rootElem, "function");
		List<String> functions = new ArrayList<>();
		for (Element el : elements) {
			functions.add(el.getTextContent());
		}

		// read service fields from XML
		rootElem = XmlTools.getSingularElement(root, "services");
		elements = XmlTools.getElementList(rootElem, "service");
		List<String> services = new ArrayList<>();
		for (Element el : elements) {
			services.add(el.getTextContent());
		}

		ConsentLevel result = new ConsentLevel(name, level, functions, services, LocalDate.now());
		return result;
	}
}
