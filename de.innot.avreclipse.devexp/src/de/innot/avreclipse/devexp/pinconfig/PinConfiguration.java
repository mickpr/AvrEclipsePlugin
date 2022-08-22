package de.innot.avreclipse.devexp.pinconfig;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.core.internal.utils.Convert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PinConfiguration extends Composite {
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPathString) {
		this.projectPath = projectPathString;
		//System.out.println("dupa=------------------------");
		//System.out.println(this.projectName);
	}

	private String projectName;
	private String projectPath;
	final TableViewer tableViewer;
	
	public PinConfigData[] configData;	// zmienna zapamietujaca aktualna konfiguracje pinow
	public PinConfiguration(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		Button chkPUD = new Button(this, SWT.CHECK);
		GridData gd_chkPUD = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_chkPUD.widthHint = 287;
		chkPUD.setLayoutData(gd_chkPUD);
		chkPUD.setText("Pull up disable (PUD bit in MCUSR)");
		
		Button chkUseLabels = new Button(this, SWT.CHECK);
		GridData gd_chkUseLabels = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_chkUseLabels.widthHint = 289;
		chkUseLabels.setLayoutData(gd_chkUseLabels);
		chkUseLabels.setText("Use label in generated code");

		
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(2));
		tableLayout.addColumnData(new ColumnWeightData(2));
		tableLayout.addColumnData(new ColumnWeightData(2));
		tableLayout.addColumnData(new ColumnWeightData(2));
		tableLayout.addColumnData(new ColumnWeightData(4));
		
		
		Table pinTable = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		GridData gd_pinTable = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_pinTable.widthHint = 456;
		gd_pinTable.heightHint = 370;
		pinTable.setLayoutData(gd_pinTable);
		pinTable.setLinesVisible(true);
		pinTable.setHeaderVisible(true);
		pinTable.setLayout(tableLayout);
		
		
		tableViewer=new TableViewer(pinTable);
		tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableViewerColumn portColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		portColumn.getColumn().setText("PORT");
		portColumn.getColumn().setWidth(85);
		
		TableViewerColumn pinNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		pinNameColumn.getColumn().setText("Pin");
		pinNameColumn.getColumn().setWidth(85);

		
		TableViewerColumn isInputColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		EditingSupport inoutEditingSupport = new InOutEditing(isInputColumn.getViewer());
		isInputColumn.getColumn().setText("In/Out");
		isInputColumn.setEditingSupport(inoutEditingSupport);
		isInputColumn.getColumn().setWidth(100);
		
		tableViewer.getColumnViewerEditor().addEditorActivationListener(new ColumnViewerEditorActivationListener() {

			@Override
			public void beforeEditorActivated( ColumnViewerEditorActivationEvent event) { }
			@Override
			public void afterEditorActivated( ColumnViewerEditorActivationEvent event) { }
			@Override
			public void beforeEditorDeactivated( ColumnViewerEditorDeactivationEvent event) { }
			@Override
			public void afterEditorDeactivated(
					ColumnViewerEditorDeactivationEvent event) {
				tableViewer.setInput(configData);;
			}
			
		});
		
		TableViewerColumn isPullUpColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		EditingSupport pullupEditingSupport = new PullUpEditing(isPullUpColumn.getViewer());
		isPullUpColumn.getColumn().setText("PullUp");
		isPullUpColumn.setEditingSupport(pullupEditingSupport);
		isPullUpColumn.getColumn().setWidth(85);
		
		TableViewerColumn labelColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		EditingSupport labelEditingSupport = new LabelEditing(labelColumn.getViewer());
		labelColumn.getColumn().setText("Label");
		labelColumn.setEditingSupport(labelEditingSupport);
		labelColumn.getColumn().setWidth(200);
		
		Button btnNewButton = new Button(this, SWT.NONE);
		btnNewButton.setText("< Generate Config Code");

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new PinTableLabelProvider());
		//tableViewer.setColumnProperties(columnProperties);

		
		

		//configData = new PinConfigData[]{ 
		//		new PinConfigData("1","PORTA", "PA0", false, false, "LED"), 
		//		new PinConfigData("4","PORTB", "PB6", true,false, "SD_CS"),
        //        new PinConfigData("5","PORTB", "PB7", false, true ,"SD_MOSI")};
		tableViewer.setInput(configData);
//		System.out.println("------------------------------------------------");
//		try {
//			saveConfigData();
//		} catch (TransformerException | ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("------------------------------------------------");
	}
	
	public boolean loadConfigData(PinConfigData configData) {
		//iterate trought pins, and select... GPIO only
		try {
			//JOptionPane.showMessageDialog(null, "Load start", "xxx", JOptionPane.INFORMATION_MESSAGE);	
			//chips = new ArrayList<>();
			
			Bundle bundle = Platform.getBundle("de.innot.avreclipse.devexp");
			URL fileURL = bundle.getEntry("D:\\pins.xml");
			//File fXmlFile = new File(FileLocator.resolve(fileURL).toURI());
			File fXmlFile = new File("D:/pins.xml");
					
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);		
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
 			//doc.getDocumentElement().normalize();
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			
			//NodeList nList = doc.getElementsByTagName("dataroot");
			NodeList nList= doc.getDocumentElement().getChildNodes();
			for (int i=0;i<nList.getLength();i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType()==Node.ELEMENT_NODE) {
					Element eElement = (Element)nNode;
					System.out.println(eElement.getElementsByTagName("label").item(0).getTextContent());
				} // if 
			} // for
			
			//JOptionPane.showMessageDialog(null, chips.size(), "Size of chips: ", JOptionPane.INFORMATION_MESSAGE);
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			JOptionPane.showMessageDialog(null, "Error while loading pins.xml file.", "Exception!", JOptionPane.INFORMATION_MESSAGE);
			return false;
		} // try.catch
	} // private boolean loadConfigData(PinConfigData configData) {
	
	
	public boolean saveConfigData(String filepath) throws TransformerException, ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("pins");
        doc.appendChild(rootElement);

        for (int x=0;x<configData.length;x++) {
            // numer pinu
        	Element item = doc.createElement("pin");
            item.setAttribute("nr", configData[x].getPinNr());
            
            // nazwa pinu
            Element pinName = doc.createElement("name");
            pinName.setTextContent(configData[x].getPinName());

            //port
            Element pinPort = doc.createElement("port");
            pinPort.setTextContent(configData[x].getPort());
            
            Element isInput = doc.createElement("in");
            if (configData[x].getIsInput())  isInput.setTextContent("true"); else isInput.setTextContent("false");
            
            Element isPullUp= doc.createElement("IsPullUp"); 
            if (configData[x].getIsPullUp()) isPullUp.setTextContent("true"); else isPullUp.setTextContent("false");
            
            Element pinLabel= doc.createElement("label");
            pinLabel.setTextContent(configData[x].getLabel());

            item.appendChild(pinName);
            item.appendChild(pinPort);
            item.appendChild(isInput);
            item.appendChild(isPullUp);
            item.appendChild(pinLabel);
            rootElement.appendChild(item);
        }
        
        doc.getDocumentElement().normalize();

        // write dom document to a file
        try (FileOutputStream output = new FileOutputStream(filepath)) {
            writeXml(doc, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
		return false;
	} //end of : saveconfigData
	
    // write doc to output stream
    private static void writeXml(Document doc, OutputStream output) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer tr= transformerFactory.newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        tr.transform(source, result);
    } // writeXml
    
    //iterate trough pin config and set properly IN/OUT, PULLUP and PORT values
    private void setIoPullAndPort() {
    	// set IO and PullUp
        for (int x=0;x<configData.length;x++) {
        	if (configData[x].getPinName().equals("MOSI") || 
        		configData[x].getPinName().equals("SCK")  ||
        		configData[x].getPinName().contains("SS") ||
        		configData[x].getPinName().contains("TX") ||
        		configData[x].getPinName().startsWith("CLKO") ||
        		configData[x].getPinName().startsWith("ALE") ||
        		configData[x].getPinName().startsWith("OC")) {
        		configData[x].setIsInput(false);
        	}
        	
        	
        	if (configData[x].getPinName().equals("MISO") ||
        		configData[x].getPinName().contains("RX") ||
        		configData[x].getPinName().contains("XCK") ||
        		configData[x].getPinName().contains("INT") ||
        		configData[x].getPinName().startsWith("CLKI") ||
        		configData[x].getPinName().startsWith("ICP") ||
        		configData[x].getPinName().startsWith("ADC") ||
        		configData[x].getPinName().startsWith("AIN") ||
        		configData[x].getPinName().equals("T0") || 
        		configData[x].getPinName().equals("T1") ||
        		configData[x].getPinName().equals("T2") || 
        		configData[x].getPinName().equals("T3") ||
            	configData[x].getPinName().equals("")) {	
        		configData[x].setIsInput(true);
        	}
        			
        }
    	// change port for non GPIO ports
    }
}

