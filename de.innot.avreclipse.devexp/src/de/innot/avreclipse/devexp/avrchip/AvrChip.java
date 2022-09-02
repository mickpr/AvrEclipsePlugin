// klasa reprezentuj¹ca wybrany chip (zmienna SelectedChip)
// definiuje obudowê, podstawowe dane, dostêpne obudowy, wybrane piny i zasoby oraz obiekt klasy ChipPackage - do rysowania
// danego Chipa w oknie plugina
//
package de.innot.avreclipse.devexp.avrchip;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.innot.avreclipse.devexp.PinFuncDescr;
import de.innot.avreclipse.devexp.PluginPreferences;


public class AvrChip {
	public String Name; 	//name eg. Atmega32
	public String Type;		// Type eg. ATmega
	public String Flash; 	// eg. 30278 (B)
	public String Eeprom; 	// eg. 512
	public String RAM; 	  	// eg. 2048
	public String Freq;   	// eg.16
	public String Vcc;    	// eg. 2.7-5.5
	public ArrayList<ChipPackage> Packages; 		// dostepne obudowy (tylko same nazwy)
	public ArrayList<AvrPinConfig> avrPinsConfig; 	// konfiguracja pinow ukladu (zawiera wszystkie funkcje i wskaznik, ktora jest wybrana
													// dla niektórych pinów (GPIO np) zawiera czy pin jest wejsciem (DDR=0) i czy pull'up'em (PORT=1)
	public Map<String, AvrResource> avrResources; 	// zasoby zawiera wszystkie wybrane zasoby - odswiezane podczas zmiany pinow
													// i w drug¹ stronê - zmiana w zasobach zmienia piny.
	public ChipPackage chipPackage;					// Obudowa 
	//-------------------------------------------------------------------------------------------
	//konstruktor 
	public AvrChip() {
		Name  = "";
		Type  = "";
		Flash = "";
		Eeprom= "";
		RAM   = "";
		Freq  = "";
		Vcc   = ""; 
		Packages = new ArrayList<ChipPackage>();		// mo¿liwe obudowy
		avrPinsConfig = new ArrayList<AvrPinConfig>();	// mo¿liwe konfiguracje pinow
		avrResources = new HashMap<String,AvrResource>();	// zasoby
		chipPackage = new ChipPackage();				// tworzenie obudowy chip'a
	}
	//-------------------------------------------------------------------------------------------	
	// pobiera dostêpne zasoby w postaci hashmap
	public Map<String, AvrResource> getAvrResources() {
		return avrResources;
	};
	//-------------------------------------------------------------------------------------------
	// wczytanie domyslnej konfiguracji pinow i zasobów z pliku bedacego wybranym chipem i podan¹ obudow¹ 
	public void LoadDefaultPinsAndResources(String selectedPackage)  {
		avrPinsConfig.clear();
		avrResources.clear();
		
		if (selectedPackage==null || selectedPackage=="")
		{
			System.out.println("LoadDefaultPinsAndResources wywolane z pustym argumentem");
			return;
		}
		try {
			Bundle bundle = Platform.getBundle("de.innot.avreclipse.devexp");
			URL fileURL = bundle.getEntry("resources/desc/" + Name + "-"+selectedPackage+".xml");
			File fXmlFile = new File(FileLocator.resolve(fileURL).toURI());
					
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);		
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("pin");
			
			for (int i=0;i<nList.getLength();i++)
			{
				Node nNode = nList.item(i);
				if (nNode.getNodeType()==Node.ELEMENT_NODE) {
					Element eElement = (Element)nNode;
					//System.out.println("   pin------------>:" + eElement.getAttribute("name").toString()); //numer pinu

					AvrPinConfig pinconfig = new AvrPinConfig();
					pinconfig.setPinNumber(i+1); //pin increment number from 1 to n...
					
					NodeList nListRole  = eElement.getChildNodes();
					for (int j=0;j<nListRole.getLength();j++) {
						Node nNodeRole = nListRole.item(j);
						if (nNodeRole.getNodeType()==Node.ELEMENT_NODE) {
							Element eElement1 = (Element)nNodeRole;
							pinconfig.AddNewFunction(eElement1.getAttribute("name").toString(), eElement1.getAttribute("resource").toString(), eElement1.getAttribute("desc").toString());

							if (!avrResources.containsKey(eElement1.getAttribute("resource").toString())) {
								AvrResource resconfig = new AvrResource(eElement1.getAttribute("resource").toString());
								resconfig.addPinNumber(new PinFuncDescr(i, j, eElement1.getAttribute("name"), eElement1.getAttribute("desc"),"")); 
								avrResources.put(eElement1.getAttribute("resource").toString(), resconfig);
							}
							else {
								AvrResource resconfig = avrResources.get(eElement1.getAttribute("resource").toString());
								// dodaj funkcje i numer pinu do juz istniejacego zasobu - jak - na podsawie indeksu???
								resconfig.addPinNumber(new PinFuncDescr(i, j, eElement1.getAttribute("name"), eElement1.getAttribute("desc"),""));
							}
//							System.out.println("      name      -------->:" + eElement1.getAttribute("name").toString()); //numer pinu
//							System.out.println("      resource  -------->:" + eElement1.getAttribute("resource").toString()); //numer pinu
//							System.out.println("      desc -------->:" + eElement1.getAttribute("desc").toString()); //numer pinu
						}
					}
					avrPinsConfig.add(pinconfig);
				} // if 
			} // for

		} catch (Exception e) {
			//System.out.println(e.getMessage());
			String s;
			s = "resources/desc/" + Name + "-"+selectedPackage+".xml";
			s= "File "+ s + " doesn't exist. Contact author for help.";
			JOptionPane.showMessageDialog(null, s, "Exception ", JOptionPane.INFORMATION_MESSAGE);
			//System.out.println(e.getMessage());
		}
	} // end of LoadResourcesOfMCU

	//-------------------------------------------------------------------------------------------
	//zwraca numer pinu dla podanej nazwy pinu w uk³adzie (konfiguracji)
	public int getPinNumberForPinNameInConfiguration(String pinName) {
		int i=1;
		// przelatujemy konfiguracje - piny
		for(AvrPinConfig pincfg : this.avrPinsConfig) {
			// przelatujemy wszystkie funkcje pinow
			for (String pName : pincfg.getPinNames()) {
				if (pName.equalsIgnoreCase(pinName)) return i;  
			}
			i++;
		}
		//System.out.println("Brak pinu o podanej w getSelectedPinFunction konfiguracji " + pinName );
		return -1;
	}
	
	//-------------------------------------------------------------------------------------------	
	// pinu numerowane od 1, ale index w tablicy od 0, st¹d "pinNr-1"
	public void setSelectedPinFunction(int pinNr, String pinName) {
		//System.out.println("...invoked setSelectedPinFunction("+pinNr+","+pinName+")");
		
		int nr=0;
		for (String pn : this.avrPinsConfig.get(pinNr-1).getPinNames()) {
			
			//System.out.println("pn " + pn + " - pinname " + pinName + " pinNr:" + pinNr);	
			if (pn.equalsIgnoreCase(pinName)) {
			
				AvrPinConfig pincfg = this.avrPinsConfig.get(pinNr-1);
				pincfg.setSelectedIndex(nr);
				this.avrPinsConfig.set(pinNr-1, pincfg);
				
				//System.out.println("...po aktualizacji: " + selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinName());
			} // if
			nr++;
		} //for
	} // function	
	//-------------------------------------------------------------------------------------------	
	// ustalanie koloru pinu na podstawie resource i name.
	public Color getColorDependOnResourceAndName(String resource,String name) {
	
		if (name.equalsIgnoreCase("VCC") || name.equalsIgnoreCase("AVCC")|| name.equalsIgnoreCase("AREF") ||name.contains("VDD"))
			return SWTResourceManager.getColor(255,100,100);
		if (name.equalsIgnoreCase("GND") )
			return SWTResourceManager.getColor(90,90,90);
		if (name.contains("ADC") || name.contains("AIN"))
			return SWTResourceManager.getColor(150,150,250);
		if (name.contains("TCK") || name.contains("TDI")|| name.contains("TDO") || name.contains("TMS"))
			return SWTResourceManager.getColor(150,250,250);
		
		if (resource.contains("PORT")) 
			return SWTResourceManager.getColor(190,190,190);

		if (resource.contains("ISP") || (resource.contains("SPI")))
			return SWTResourceManager.getColor(128,255,128);
		if (resource.contains("UART") || resource.contains("USART") || resource.contains("USI"))
			return SWTResourceManager.getColor(255,190,255);
		if (resource.contains("I2C"))
			return SWTResourceManager.getColor(190,255,255);
		if (name.contains("XTAL") || name.contains("TOSC"))
			return SWTResourceManager.getColor(255,150,0);
		if (name.startsWith("OC")) 
			return SWTResourceManager.getColor(0,153,255);
		// otherwise...
			return SWTResourceManager.getColor(255,255,200);
	}
	//-----------------------------------------------------------------------------------------------
	// przelatuje selectedChip.avrPinsConfig i wg ustawionej tam konfiguracji - ustawia checkboxy
	// w oknie zasobów, jesli jakis zasob ma uzyty choc jeden pin - zaznacza ten zasob
	public void setCurrentSelectedPinsInTree(Tree tree) {
		String pinName;
		for (TreeItem tiResourceItem : tree.getItems()) {
			boolean usedResource = false;
			for (TreeItem tiPinItem: tiResourceItem.getItems()) {
				
				pinName=tiPinItem.getText();
				if (isSelectedPinInCurrentConfiguration(pinName)) {
					usedResource= true;
					tiPinItem.setChecked(true);
				} else {
					tiPinItem.setChecked(false);
				}
			}
			tiResourceItem.setChecked(usedResource);
		}
	}
	//-----------------------------------------------------------------------------------------------
	// czy wybrany pin jest w obecnej konfiguracji
	public boolean isSelectedPinInCurrentConfiguration(String pinName) {
		for(AvrPinConfig pincfg : this.avrPinsConfig) {
			if (pinName.equalsIgnoreCase(pincfg.getSelectedPinName())) 
				return true;
		}
		return false;
	}
	//-------------------------------------------------------------------------------------------
	// wczytanie konfiguracji pinow
	public String LoadPinConfigFunctions() {
		String config = PluginPreferences.get("PinFunc","ERR");
		char conf[];
		// jesli próba wczytania zakonczyla sie bledem - zwroc pusty string
		if (config.equals("ERR")) return "";
		
		// jesli ok.. zamien String na tablice znakow.
		conf = config.toCharArray();
		for (int a=0;a<conf.length;a++) {
			AvrPinConfig apc = this.avrPinsConfig.get(a);
			int chr = conf[a] -'0'; // zamiana 0123456789 na odpowiednia wartosc int
			apc.setSelectedIndex(chr); 			
			// ustaw 
			this.avrPinsConfig.set(a, apc);
		}
		//System.out.println("size:" + selectedChip.avrPinsConfig.size());
		return config;
	}
	//-------------------------------------------------------------------------------------------	
	public void SavePinConfigFunctions() {
		StringBuilder config = new StringBuilder();
		for (int a=0;a<this.avrPinsConfig.size();a++) {
			config.append(this.avrPinsConfig.get(a).getSelectedIndex().toString());
		}
		//flush
		PluginPreferences.set("PinFunc", config.toString());  
	}
	//-------------------------------------------------------------------------------------------	
	public void SavePinConfigIsPullUpOrHighState() {
		StringBuilder config = new StringBuilder();
		for (int a=0;a<this.avrPinsConfig.size();a++) {
			config.append(this.avrPinsConfig.get(a).getSelectedPinIsInput()?"1":"0");
		}
		//flush
		PluginPreferences.set("PinIsInput", config.toString());  
	}
	//-------------------------------------------------------------------------------------------
	public void SavePinConfigIsInput() {
		StringBuilder config = new StringBuilder();
		for (int a=0;a<this.avrPinsConfig.size();a++) {
			config.append(this.avrPinsConfig.get(a).getSelectedPinIsPullUpOrHighState()?"1":"0");
		}
		//flush
		PluginPreferences.set("PinIsPullUpOrHighState", config.toString());  
	}
	//-------------------------------------------------------------------------------------------	
	public void updateChipPackagePinsToSelectedInAvrPinsConfig() {
		for (int a=0;a<this.avrPinsConfig.size();a++) {
    		this.chipPackage.pins.get(a).name = this.avrPinsConfig.get(a).getPinNames().get(this.avrPinsConfig.get(a).getSelectedIndex());  // ... o tak
    		this.chipPackage.pins.get(a).color = this.getColorDependOnResourceAndName(this.avrPinsConfig.get(a).getSelectedPinResouce(),this.avrPinsConfig.get(a).getSelectedPinName());
    	}
	}	
	//-------------------------------------------------------------------------------------------
	// funkcja zwraca bajt (bedacy odzwierciedleniem konfiguracji DDR - czyli konfiguracji maski bitowej gdzie
	// 1 oznacza pin jako ustawiony jako wyjscie, a 0 oznacza pin jako wejscie (domyslnie) podanego port-u)
	// jako argument podajemy cala nazwe portu.. np. PORTA, PORTF
	// jesli dany port nie jest uzywany (lub nie istnieje), lub ma wszystkie piny wejsciowe - zwracane jest zero
	// poniewaz jest to wartosc domyslna - nie trzeba (ale mozna) ustawiac jej przy inicjalizacji rejestru DDRx
	public byte getDDRportValue(String portName) {
		byte bits=0;
		for (int x=0;x< this.avrPinsConfig.size();x++) {
			AvrPinConfig apc = this.avrPinsConfig.get(x);
			if (apc.getSelectedPinResouce().equalsIgnoreCase(portName)) {
				byte bitmask = (byte) (1 << Byte.parseByte(apc.getSelectedPinName().substring(2)));
				if (apc.getSelectedPinIsInput())
					bits |= bitmask; 
			} // if
		} // for
		return bits;
	}	
	//-------------------------------------------------------------------------------------------	
	// funkcja zwraca bajt (bedacy odzwierciedleniem konfiguracji pull-up (lub high-state
	// czyli pinow w porcie (PORTA..PORTn) gdzie  1 oznacza pin w stanie HIGH, a 0 w stanie LOW
	// podczas konfiguracji pull-up-ow to wlasnie ta wartosc powinna byc wpisana (po ustawieniu DDR)
	// jako wartosc okreslonego rejestru PORTx. 
	// Argumentem jet cala nazwe portu.. np. PORTA, PORTF 
	// jesli dany port nie jest uzywany (lub nie istnieje), lub ma wszystkie bity zerowe - zwracane jest zero
	// poniewaz jest to wartosc domyslna - nie trzeba (ale mozna) ustawiac jej przy inicjalizacji rejestru PORTx
	public byte getPullUpOrHighStatePortValue(String portName) {
		byte bits=0;
		for (int x=0;x< this.avrPinsConfig.size();x++) {
			AvrPinConfig apc = this.avrPinsConfig.get(x);
			if (apc.getSelectedPinResouce().equalsIgnoreCase(portName)) {
				byte bitmask = (byte) (1 << Byte.parseByte(apc.getSelectedPinName().substring(2)));
				if (apc.getSelectedPinIsPullUpOrHighState())
					bits |= bitmask; 
			} // if
		} // for
		return bits;
	}	
	//-------------------------------------------------------------------------------------------	
	// funkcja zwraca  istniej¹ce (wykorzystane) zasoby o nazwie PORTx w wybranym MCU
	public ArrayList<String> getAssignedPorts(){
		ArrayList<String> al = new ArrayList<>();
		for (int x=0;x< this.avrPinsConfig.size();x++) {
			AvrPinConfig apc = this.avrPinsConfig.get(x);
			for(int j=0;j<apc.pinNames.size();j++) {
				if (apc.getSelectedPinResouce().startsWith("PORT") && !al.contains(apc.getSelectedPinResouce()))  {
					al.add(apc.getSelectedPinResouce());
				}
			}
		} // for
		return al;
	}
	//-------------------------------------------------------------------------------------------
	// funkcja zwraca  istniej¹ce (wykorzystane) zasoby o nazwie PORTx w wybranym MCU
	public ArrayList<String> getExistingPorts(){
		ArrayList<String> al = new ArrayList<>();
		for (int x=0;x< this.avrPinsConfig.size();x++) {
			AvrPinConfig apc = this.avrPinsConfig.get(x);
			for(int j=0;j<apc.pinResources.size();j++) {
				if (apc.pinResources.get(j).startsWith("PORT") && !al.contains(apc.pinResources.get(j)))  {
					al.add(apc.pinResources.get(j));
				}
			}
		} // for
		return al;
	}
	//-------------------------------------------------------------------------------------------	
	// print sorted existing (also not assigned) PORTs
	public void printAllPorts() {
		ArrayList<String> alpc = getExistingPorts();
		Collections.sort(alpc);
		for (int i=0;i<alpc.size();i++){
			System.out.println(alpc.get(i));
		}
	}
	//-------------------------------------------------------------------------------------------
	
} // end of class
