package de.innot.avreclipse.devexp;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AvailableMCUsList {

	
	public ArrayList<AvailableMCUsListEntity> chips;
	
	public String toString() { 
	    return "AvrChips object";
	} 	
	
	public boolean LoadAvrChipsFromDevicesXML() {
		
		try {
			//JOptionPane.showMessageDialog(null, "Load start", "xxx", JOptionPane.INFORMATION_MESSAGE);	
			chips = new ArrayList<>();
			
			Bundle bundle = Platform.getBundle("de.innot.avreclipse.devexp");
			URL fileURL = bundle.getEntry("resources/devices.xml");
			File fXmlFile = new File(FileLocator.resolve(fileURL).toURI());
					
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);		
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
				//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("device");
			for (int i=0;i<nList.getLength();i++)
			{
				Node nNode = nList.item(i);
				if (nNode.getNodeType()==Node.ELEMENT_NODE) {
					Element eElement = (Element)nNode;
					
					AvailableMCUsListEntity achip = new AvailableMCUsListEntity();
					achip.Name = eElement.getElementsByTagName("Name").item(0).getTextContent();
					//System.out.println("Chip :" + achip.Name);
					achip.Type= eElement.getElementsByTagName("Type").item(0).getTextContent();
					achip.FLASH= eElement.getElementsByTagName("Flash").item(0).getTextContent();
					achip.EEPROM= eElement.getElementsByTagName("EEPROM").item(0).getTextContent();
					achip.RAM= eElement.getElementsByTagName("RAM").item(0).getTextContent();
					achip.Freq = eElement.getElementsByTagName("Freq").item(0).getTextContent();
					achip.Vcc = eElement.getElementsByTagName("Vcc").item(0).getTextContent();

					NodeList collected_objects = eElement.getElementsByTagName("Packages").item(0).getChildNodes();
					if (collected_objects != null) {
						int len = collected_objects.getLength();
						for (int a=0;a<len;a++) {
							if (collected_objects.item(a).getNodeType() == Node.ELEMENT_NODE) {
				                Element el = (Element) collected_objects.item(a);
				                if (el.getNodeName().endsWith("Package")) {
				                	//System.out.println("   Obudowa:" + el.getAttribute("name").toString());
				                	//System.out.println("   Plik:" + el.getAttribute("file").toString());
				                	achip.Packages.add(el.getAttribute("name").toString());
				                }
							}
							
						} // for 
					} // if
					chips.add(achip);
				} // if 
			} // for
			//JOptionPane.showMessageDialog(null, chips.size(), "Size of chips: ", JOptionPane.INFORMATION_MESSAGE);
			return true;
		} catch (Exception e) {
			//System.out.println(e.getMessage());
			JOptionPane.showMessageDialog(null, "Error while loading devices.xml file. Contact with mickpr@poczta.onet.pl", "Exception!", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
	}
	
	
}
