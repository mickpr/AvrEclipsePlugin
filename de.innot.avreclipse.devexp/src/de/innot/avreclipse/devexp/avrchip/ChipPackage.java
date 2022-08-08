package de.innot.avreclipse.devexp.avrchip;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.innot.avreclipse.devexp.utils.*;

public class ChipPackage {
	public String Name;
	public ArrayList<ChipPin> pins;
	public ChipBody body;
	private int pincount =0;
	
	public int getPinCount(){
		return pincount;
	}
	
	public ChipPackage() {
		
	}
	
	
	// load package pins into array
	public void LoadPackage(String packageName) {
		this.Name=packageName;
		pins = new ArrayList<ChipPin>();
		
		// try to read pins for given package name
		try {
			Bundle bundle = Platform.getBundle("de.innot.avreclipse.devexp");
			URL fileURL = bundle.getEntry("resources/packages.xml");
			File fXmlFile = new File(FileLocator.resolve(fileURL).toURI());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);		
			doc.getDocumentElement().normalize();
				//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
				//NodeList nList = doc.getElementsByTagName("staff");
			NodeList nList = doc.getElementsByTagName("package");
			if (nList.getLength()>0) {
				for (int i=0;i<nList.getLength();i++)
				{
					Node nNode = nList.item(i); //iteracja po "package"-sach
					if (nNode.getNodeType()==Node.ELEMENT_NODE) {
						Element eElement = (Element)nNode;
						
						if(eElement.getElementsByTagName("name").item(0).getTextContent().equals(packageName)) {
							
							//System.out.println(eElement.getElementsByTagName("name").item(0).getTextContent());  // DIP8, TQFP32 itd...
							body = new ChipBody();
							body.dx = Integer.parseInt(eElement.getElementsByTagName("body").item(0).getAttributes().getNamedItem("x").getNodeValue().toString());
							body.dy = Integer.parseInt(eElement.getElementsByTagName("body").item(0).getAttributes().getNamedItem("y").getNodeValue().toString());
							body.width = Integer.parseInt(eElement.getElementsByTagName("body").item(0).getAttributes().getNamedItem("width").getNodeValue().toString());
							body.height = Integer.parseInt(eElement.getElementsByTagName("body").item(0).getAttributes().getNamedItem("height").getNodeValue().toString());
							
							//teraz po pakiecie
							// liczba pinów w poszcególnych scalakach
							//System.out.println(eElement.getElementsByTagName("pins").item(0).getAttributes().getNamedItem("count").getNodeValue().toString());
							// odzyskaj liczbe pinow
							pincount = Integer.parseInt(eElement.getElementsByTagName("pins").item(0).getAttributes().getNamedItem("count").getNodeValue().toString());
							NodeList nPinList = eElement.getElementsByTagName("pin");
							for (int j=0;j<nPinList.getLength();j++) {
								// teraz iteracja po pinach
								Node nNodePin = nPinList.item(j);
								if (nNodePin.getNodeType() == Node.ELEMENT_NODE)
								{
									Element eElementPin = (Element)nNodePin; // konwersja na element

									try {
										//String PinName =  eElementPin.getElementsByTagName("pin").item(0).getTextContent();
										//if (PinName==null) PinName="";
										String sNr = eElementPin.getAttribute("nr");
										String sPosX=eElementPin.getAttribute("x");
										String sPosY=eElementPin.getAttribute("y");
										String sOrient = eElementPin.getAttribute("orient");
										int nr = Integer.parseInt(sNr);
										int PosX=Integer.parseInt(sPosX);
										int PosY=Integer.parseInt(sPosY);
										 
										// orientation
										PinLocation Orient=PinLocation.LEFT;
										if (sOrient.equals("L")) 
											Orient = PinLocation.LEFT;
										if (sOrient.equals("R")) 
											Orient = PinLocation.RIGHT;
										if (sOrient.equals("T")) 
											Orient = PinLocation.TOP;
										if (sOrient.equals("B")) 
											Orient = PinLocation.BOTTOM;
										
										ChipPin cp = new ChipPin(nr,"---",PosX, PosY, Orient );
										pins.add(cp);
										//System.out.println(pins.get(j).toString());
									} catch (Exception e) {
										System.out.println("Error in packages.xml !!!");
										System.out.println(e.getMessage());
									} // try...catch 
								} // if (nNodePin.getNodeType() == Node.ELEMENT_NODE)
							} //for (int j=0;j<nPinList.getLength();j++) {
							
							
							
						} // if(eElement.getElementsByTagName("name").item(0).getTextContent().equals(packageName)) {
					} // if (nNode.getNodeType()==Node.ELEMENT_NODE) {
				} // for .. po obudowach (packages.. DIP8,.... etc)
			} // if (nList.getLength()>0) { 

		} catch (Exception e) {
			System.out.println("Error w ChipPackage");
			System.out.println(e.getMessage());
		} // try ... catch

		//MessageDialog.openError(getShell(), "dupa", "dupa content");
		//MessageDialog.openConfirm((getShell(), "dupa", "dupa content");
	}
	
//	private Shell getShell() {
//		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//	}
//	
	
	public String toString() {
		return this.Name;
	}

}
