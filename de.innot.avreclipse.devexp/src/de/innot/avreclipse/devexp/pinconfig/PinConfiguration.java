package de.innot.avreclipse.devexp.pinconfig;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;

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
    	
		btnNewButton.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/copy.gif"));    	

		btnNewButton.setText("Copy config code");

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new PinTableLabelProvider());
		//tableViewer.setColumnProperties(columnProperties);


		//configData = new PinConfigData[]{ 
		//		new PinConfigData("1","PORTA", "PA0", false, false, "LED"), 
		//		new PinConfigData("4","PORTB", "PB6", true,false, "SD_CS"),
        //        new PinConfigData("5","PORTB", "PB7", false, true ,"SD_MOSI")};
		tableViewer.setInput(configData);
	}
	
	//JOptionPane.showMessageDialog(null, "Load start", "xxx", JOptionPane.INFORMATION_MESSAGE);	
}

