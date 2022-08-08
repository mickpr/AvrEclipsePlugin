package de.innot.avreclipse.devexp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.innot.avreclipse.devexp.avrchip.AvrChip;
import de.innot.avreclipse.devexp.avrchip.AvrResource;
import de.innot.avreclipse.devexp.avrchip.ChipPackage;

public class DeviceExplorerCore {
	public AvailableMCUsList availableChips;	// for store all main device configuration from devices.xml
	public AvrChip selectedChip;
	public String projectName;
	
	public DeviceExplorerCore() {
		availableChips = new AvailableMCUsList(); 
		availableChips.LoadAvrChipsFromDevicesXML();
		
		selectedChip = new AvrChip();
		// load all chip list into allChip object 
//		availableChips = new AvailableMCUsList();
//		availableChips.LoadAvrChipsFromDevicesXML();
	}
	
	//-----------------------------------------------------------------------------------------------
	public void SetupSelectedChip(String chipName, String packageName) {

		selectedChip.Name = chipName;
		selectedChip.LoadDefaultPinsAndResources(packageName);
		
		selectedChip.avrResources = new TreeMap<String, AvrResource>(selectedChip.avrResources);
		selectedChip.chipPackage.LoadPackage(packageName);
	
		// load configuration tylko w przypadku zmiany chipa
		selectedChip.LoadPinConfigFunctions();		
		selectedChip.updateChipPackagePinsToSelectedInAvrPinsConfig();
	}

	//-----------------------------------------------------------------------------------------------	
	public void FillComboWithChipsNames(Combo combo) {
		String s;
		
		try {
			Bundle bundle = Platform.getBundle("de.innot.avreclipse.devexp");
			URL fileURL = bundle.getEntry("resources/devices.xml");
			File fXmlFile = new File(FileLocator.resolve(fileURL).toURI());
					
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);		
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			Map<String, ChipPackage> chipPackages= new HashMap<String, ChipPackage>();
			
			NodeList nList = doc.getElementsByTagName("device");
			for (int i=0;i<nList.getLength();i++)
			{
				Node nNode = nList.item(i);
				if (nNode.getNodeType()==Node.ELEMENT_NODE) {
					Element eElement = (Element)nNode;
					if (eElement.getElementsByTagName("Name").item(0).getTextContent()!=null) {
						s=eElement.getElementsByTagName("Name").item(0).getTextContent();
						combo.add(s);
						// pobierz nazwe chipa
						ChipPackage cp = new ChipPackage();
						
						chipPackages.put(s,cp);
					} // if
				} // if 
			} // for
		} catch (Exception e) {
			//System.out.println(e.getMessage());
		}			    
		
	}
	//-------------------------------------------------------------------------------------
	
	//-------------------- TUTAJ JEST SYF-------------------------------------------
	// -----------------------------------------------------
	public void FillTreeWithResources(Tree tree) {
		//JOptionPane.showMessageDialog(null, "jestem", "jestem", JOptionPane.INFORMATION_MESSAGE);

		for (String key : selectedChip.avrResources.keySet()) {
    		AvrResource value = selectedChip.getAvrResources().get(key);
    		//JOptionPane.showMessageDialog(null, "jestem", "jestem", JOptionPane.INFORMATION_MESSAGE);		
		
    		TreeItem trtmNewTreeitem = new TreeItem(tree, SWT.NONE);
    		trtmNewTreeitem.setText(value.ResourceName);
//    		trtmNewTreeitem.addListener(SWT.MouseEnter, new Listener() {
//				@Override
//				public void handleEvent(Event event) {
//					// TODO Auto-generated method stub
//					System.out.println("Dupa");
//				}
//			});
//    		
			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/blackplate.png"));    		
    		if (value.ResourceName.equalsIgnoreCase("ACOMP")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/acomp.png"));
    		if (value.ResourceName.equalsIgnoreCase("ADC")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/ac.png"));
    		if (value.ResourceName.equalsIgnoreCase("SPI")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/spi.png"));
    		if (value.ResourceName.startsWith("UART") || value.ResourceName.startsWith("USART")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/rs232.png"));
    		if (value.ResourceName.equalsIgnoreCase("I2C")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/i2c.png"));
    		if (value.ResourceName.equalsIgnoreCase("CORE")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/core.gif"));
    		if (value.ResourceName.startsWith("PORT")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/port.gif"));
    		if (value.ResourceName.equalsIgnoreCase("EXTINT")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pulse.png"));
    		if (value.ResourceName.startsWith("TIMER")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/clock.png"));
    		if (value.ResourceName.equalsIgnoreCase("JTAG")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/port.gif"));
    		if (value.ResourceName.equalsIgnoreCase("PWM")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pwm2.png"));
    		if (value.ResourceName.endsWith("RAM")) 
    			trtmNewTreeitem.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/chip.png"));

			// sort child nodes by comparator 
			ArrayList<PinFuncDescr> sorted = selectedChip.getAvrResources().get(key).Pins;
			Collections.sort(sorted,PinFuncDescr.ChipPinComparator);
			
			for (PinFuncDescr pfd : sorted) {
				//dodaj dzieci - piny
				TreeItem tt= new TreeItem(trtmNewTreeitem,SWT.NONE);
				tt.setText(pfd.name);
				tt.setData("func_nr", pfd.func_nr);
				tt.setData("descr", pfd.descr);
				
				if (pfd.name.substring(0, 2).equalsIgnoreCase("ADC")) tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/AC.png")); 
				if (pfd.name.startsWith("AIN")) tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/adc.gif"));
				if (pfd.name.startsWith("XTAL") || pfd.name.startsWith("TOSC")) tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/xtal.png"));
				if (pfd.name.startsWith("VCC") || pfd.name.startsWith("AVCC") || pfd.name.startsWith("AREF")) tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/power.png"));
				if (pfd.name.startsWith("PCINT")) tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/int.png"));
				
				// odpowiednie odwzorowanie wejœcia. wyjœcia i pull up dla ikon w Tree.
				if (value.ResourceName.startsWith("PORT")) { 
					if (this.selectedChip.isSelectedPinInCurrentConfiguration(pfd.name)) {
						int pinNr =this.selectedChip.getPinNumberForPinNameInConfiguration(pfd.name);
						if (this.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsInput()) {
							if (this.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsPullUpOrHighState()) {
								tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_in_pup.gif"));
							} else {
								tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_in.gif"));
							}
						} else {
							tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_out.gif"));
						}
					}
				}
				
				if (pfd.name.startsWith("GND")) tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/gnd.gif"));
				if (pfd.name.startsWith("OC")) 	tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pwm2.png"));
				if (pfd.name.endsWith("XD")) 	tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/rs232.png"));
				if (pfd.name.contains("INT") || pfd.name.contains("CLK") ) tt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pulse.png"));
			} // for
			
			int ic = selectedChip.getAvrResources().get(key).Pins.size();
			
			trtmNewTreeitem.setText(value.ResourceName + " (" + ic + ")" );
			
    	} // for
	} // public void FillTreeWithResources(Tree tree) {
	
	// ---------------------------------------------------------------------------------------

	public double getMcuFrequency() {
		String s_freq= PluginPreferences.get("ClockFrequency","");
		// frequency
		if (s_freq.length()==0) {
			return 0;
		}
		return Double.parseDouble(s_freq);
	}
	
	public void setMcuFrequency(double freq) {
		//String s_freq= Double.toString(freq);
		DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
		PluginPreferences.set("ClockFrequency", df.format(freq));
	}

	//----------------------------------------------------------------------------------------------------	
	public void ParseProgram() throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder("avr-size.exe","--mlist-devices");
	    processBuilder.redirectErrorStream(true);
	    final Process process = processBuilder.start();

	    InputStream stderr = process.getInputStream();
	    InputStreamReader isr = new InputStreamReader(stderr);
	    BufferedReader br = new BufferedReader(isr);
	    String line = null;

	    while ((line = br.readLine()) != null) {
	        //System.out.println("x:"+line);
	    	if (line.indexOf("attiny5") >=0) { 
	    		System.out.println("jest attiny5");
	    	}
	    }
	    process.waitFor();
//	    System.out.println("Waiting ...");
//	    System.out.println("Returned Value :" + process.exitValue());		
	}
	//----------------------------------------------------------------------------------------------------	
//	private Shell getShell() {
//		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//	}
	//----------------------------------------------------------------------------------------------------

	//--------------------------------------------------------------------------------------------
	
}
