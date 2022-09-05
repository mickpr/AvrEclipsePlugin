package de.innot.avreclipse.devexp;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import de.innot.avreclipse.devexp.avrchip.AvrPinConfig;
import de.innot.avreclipse.devexp.avrchip.AvrResource;
import de.innot.avreclipse.devexp.avrchip.ChipPin;
import de.innot.avreclipse.devexp.pinconfig.*;
import de.innot.avreclipse.devexp.utils.PinLocation;

//enum PinLocation { LEFT,RIGHT, TOP, BOTTOM }; 
public class DeviceExplorerView extends ViewPart {

	private SashForm sashFormTop;
	private SashForm sashForm; 
	private Composite compositeTop; 
	
	private Canvas canvas;
	private int canvas_offset_x, canvas_offset_y;
	private Composite compositeTree;
	private Composite composResource; 
	private Composite myViewParent;
/*	
//	private TIMER t0;
//	private Composite compositeTimer;
//	private Composite compositeUsart;
//	private Composite compositeExtInt;
//	private Composite compositeAcomp;
//	private Composite compositeAdc;
//	private Composite compositeCode;
//	private ScrolledComposite scroller; 
//	private TabItem tiTimer;	
//	private EXTINT tiExtint;
*/
	private Combo combo_chipname; 
	private Combo combo_package;
	public Tree tree;
	public int treeTopItemIndex;
	private Button btnSave;
	Label lblChipSelect;
	//Label lblFreq;
	private Combo combo_freq;
	private MyProgressBar progBar; 
	private TabFolder tabFolder;
	
	PinConfiguration pinconf; 	
	
	public Text descrTxt;	
	public DeviceExplorerCore core = new DeviceExplorerCore();
	
	public double mcuFreq;

	public String projectPath;
	public String projectName;
	
	public IProject currentProject;
	
	//=============================================================================
	// obsluga wskazywania projektu w oknie projektu
	// the listener we register with the selection service 
	private ISelectionListener listenerProject = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// we ignore our own selections
			if (sourcepart != DeviceExplorerView.this) {
				selectProjectFile(sourcepart, selection);
				
				if (projectPath!=null) {
					// load configuration from selected project
					//System.out.println("Trying to load pin config file from project .settings directory...");
					//String fileToLoad = projectPath + "/.settings/pins.xml";
					//pinconf.loadConfigData(fileToLoad);
				}
			}
		}
	};	
	//-----------------------------------------------------------------------------
	public void selectProjectFile(IWorkbenchPart sourcepart, ISelection selection) {
		//IProject project = null;		
		IStructuredSelection ss = null;
		Object element = null;

		// sprawdz co wskazalismy w oknie Projekt/Package Explorer  
		if (selection instanceof IStructuredSelection) {
			ss = (IStructuredSelection) selection;
			element=ss.getFirstElement();
		}
		
		// jesli wskazalismy projekt
		if (element instanceof IProject) {
			// set pointer to current project
			currentProject = (IProject)element;
			
			// pobierz jego nazwa 
			core.projectName = ((IProject) element).getName();
			// setup for preference access
			tree.setVisible(true);
			// tylko jesli wskazemy projekt otwarty...
			if (PluginPreferences.SwitchProject(core.projectName)==true)
			{
				setEnableDeviceView(true);
				canvas.setVisible(true);
				// sprawdz jaki jest typ projektu
				String avrName = PluginPreferences.get("MCUType");
				
				// jesli nie mozna odczytac zmiennej MCUType z podkatalogu ./settings projektu
				// znaczy to, ze najprawdopodobniej nie ma takiego pliku
				if (avrName.length()==0) {
					combo_chipname.setBackground(SWTResourceManager.getColor(255,200,200));
					combo_chipname.deselectAll();
					combo_freq.deselectAll();
					combo_package.deselectAll();
					tree.setVisible(false);
					canvas.setVisible(false);
					return;
				}
				
//				// Get the build configurations for a project
//				try {
//					//IBuildConfiguration[] buildConfigs = ((IProject) element).getBuildConfigs();
//					//System.out.print("Project name:");
//					//System.out.println(((IProject) element).getName());  // Project name
//					//projectName = ((IProject) element).getName();
//					//projectPath = ((IProject) element).getLocation().toString();
//					// lokalizacja projektu
//					//System.out.println("Lokalizacja projektu: " +((IProject) element).getLocation().toString());
//					//System.out.println(buildConfigs.length);
//					//for (IBuildConfiguration ibc : buildConfigs) {}
//					//System.out.println();  // Project name
//				
//				} catch (CoreException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

//				try { IProjectDescription projDesc = ((IProject) element).getDescription();
//				} catch (CoreException e) { e.printStackTrace(); }
			
				//	IProjectDescription projDesc = project.getDescription();
				//	... 
				// Creating a build configuration
				//	ResourcesPlugin().getWorkspace().newBuildConfig(project.getName(), "myNewBuildConfig");
				//...
				//	Set new build configurations on a project
				//projDesc.setBuildConfigs(buildConfigs);
				//
				//	Set the description
				//project.setDescription(projDesc, null);			
				//			projDesc.setActiveBuildConfig(String buildConfigName);			

				// System.out.println("Chip z tego " + PluginPreferences.get("MCUType","ATmega8"));
				//convert to name in special form (2 Capital letrers at the begining)
				avrName = avrName.substring(0, 2).toUpperCase() + avrName.substring(2, avrName.length());
				// enter chipname into field
				combo_chipname.setText(avrName);
				//set selectedChip.Name
				core.selectedChip.Name = avrName;
				// reload available packages for current selected chip
				reloadPackageComboAndSetFirstPackageAsDefault(avrName); // combo_chipname.getItem(combo_chipname.getSelectionIndex())

				// if exist Package =then select it  
				if (PluginPreferences.get("Package").length()>0) {
					combo_package.setText(PluginPreferences.get("Package"));
				}
				// setSelected chip and repaint it
				core.SetupSelectedChip(combo_chipname.getText(),combo_package.getText());
				// freq
				this.mcuFreq=core.getMcuFrequency();
				//lblFreq.setText( "@ " + this.mcuFreq/1000000 + " MHz");
				combo_freq.setText(this.mcuFreq/1000000+"");
			
				// show loaded resources into tree :)
				loadDeviceResourcesIntoTree(composResource);
				//set checked pins 
				core.selectedChip.setCurrentSelectedPinsInTree(tree);

				// TODO: musimy przelaczyc na package, inaczej timery sie nie odswieza - nie wiem dlaczego - poprawic
				tabFolder.setSelection(0);

				
				
				
				
				
				
				/* 
				//-------------------------------------------------------------------------
							TabItem tiExtInt = new TabItem(tabFolder,SWT.FILL);
						    compositeExtInt = new Composite(tabFolder,SWT.FILL);
				 compositeExtInt.setLayout(new FillLayout());
				if ((tiExtInt !=null) && (!tiExtInt.isDisposed())) {
					tiExtInt.dispose(); }
						    
				tiExtint = new EXTINT(compositeExtInt,tree,SWT.FILL);
				tiExtInt.setText("EXTINT");
				tiExtInt.setControl(compositeExtInt);
				tiExtInt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/wykrzyknik.png"));			
				 timers - redraw after selection
					if ((compositeTimer != null) && (!compositeTimer.isDisposed())) {
					compositeTimer.dispose();
						    }			
							if ((tiTimer !=null)&&(!tiTimer.isDisposed())) {
								tabFolder.getTabList()[1].dispose();
					}
				
				//TabItem tiTimer = new TabItem(tabFolder,SWT.FILL);
				//compositeTimer = new Composite(tabFolder, SWT.FILL);
				//compositeTimer.setLayout(new FillLayout()); 	    

						    if ((t0 != null) && (!t0.isDisposed())) {
				    		t0.dispose();
						    }
					    	t0 = new TIMER(compositeTimer,SWT.FILL,core.selectedChip.Name, core.getMcuFrequency());

					    	tiTimer.setText("TIMERS/COUNTERS");
							tiTimer.setControl(compositeTimer);
							tiTimer.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/clock.png"));		    
						    tiTimer.setControl(compositeTimer);
			
				int sel=tabFolder.getSelectionIndex();

							compositeTimer.redraw();
							tabFolder.redraw();
							tabFolder.setSelection(sel);
			
				 end timers - redraw after selection
				// nazwa pliku wynikowego (ELF) projektu
				//System.out.println(org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation().append(core.projectName).append("/Release/").append(core.projectName.concat(".elf")));
		
				// sama nazwa pliku projektu
				//System.out.println(core.projectName.concat(".elf"));

				*/
				canvas.redraw();
			}
			else {
				System.out.println("projekt jest zamkniety");
				setEnableDeviceView(false); // pozamykaj kontrolki, zeby nie bylo bledu			
			}
		} else {
			if (element!=null) {
			// show type of selected item in project explorer 
			System.out.println("Klasa: " + element.getClass().getName());
			}
		}
	}	
	//--------------------------------------------------------------------------------------------
	
	//constructor of formularz must be empty 
	public DeviceExplorerView() {
	}

	//--------------------------------------------------------------------------------------------
	// glowna funkcja formularza
	@Override
	public void createPartControl(Composite parent) {

		try {
			// run avr-size and check if attiny5 exist.... TODO:remove
			core.ParseProgram();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		
		//---------------------------------------------------
		// pierwsza czesc - 3x combo (chip, obudowa, czêstotliwoœæ oraz klawisz Save) 
		sashFormTop = new SashForm(parent, SWT.SMOOTH | SWT.VERTICAL);
	    sashFormTop.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
	    sashFormTop.setDragDetect(false);
	    sashFormTop.setSashWidth(1);
	    
	    compositeTop = new Composite(sashFormTop, SWT.NONE);
	    compositeTop.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
	    compositeTop.setLayout(new GridLayout(7, false));
	    

	    lblChipSelect = new Label(compositeTop, SWT.NONE);
	    lblChipSelect.setLocation(3, 3);
	    lblChipSelect.setSize(90, 16);
	    lblChipSelect.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
	    lblChipSelect.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
	    lblChipSelect.setText("Select chip:");
	    
	    combo_chipname = new Combo(compositeTop, SWT.FLAT);
	    combo_chipname.setBounds(3, 20, 142, 21);
	    combo_chipname.setToolTipText("Selected MCU");
	    combo_chipname.select(0);
	    core.FillComboWithChipsNames(combo_chipname);
	    
	    combo_package = new Combo(compositeTop, SWT.FLAT);
	    //combo_package.setLocation(150, 20);
	    combo_package.setToolTipText("Selected MCU package");
	    combo_package.setSize(50, 21);
	    combo_package.select(0);
	    
	    combo_chipname.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent e) {
	    		
		    	if (combo_chipname.getSelectionIndex() <0) {
		    	}
		    	else {
	    			//odczytaj jakie obudowy s¹ w wybranym mikrokontrolerze i je wpisz do combo_package
		    		reloadPackageComboAndSetFirstPackageAsDefault(combo_chipname.getItem(combo_chipname.getSelectionIndex()));
		    	}
		    	// clear potentially red warting colour
		    	combo_chipname.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		    	// jesli czestotliwosc nie jest wybrana - wybierz jakas
		    	// (obudowa jest juz automatycznie ustawiana podczas wyboruu MCU) 
		    	if (combo_freq.getSelectionIndex()==-1)
		    		combo_freq.select(0);
		    	// odblokuj mozliwosc zapisu
		    	btnSave.setEnabled(true);
		    	compositeTop.redraw();
		    }
	    });	    

	    combo_freq = new Combo(compositeTop, SWT.FLAT);
	    combo_freq.setBounds(3, 20, 180, 19);	    	    
	    combo_freq.add("1");
	    combo_freq.add("1.843");
	    combo_freq.add("2");
	    combo_freq.add("2.4576");
	    combo_freq.add("3.276");
	    combo_freq.add("3.57");
	    combo_freq.add("3.6864");
	    combo_freq.add("4");
	    combo_freq.add("4.0963");
	    combo_freq.add("4.194");
	    combo_freq.add("4.433618");
	    combo_freq.add("4.9152");
	    combo_freq.add("5");
	    combo_freq.add("6");
	    combo_freq.add("6.144");
	    combo_freq.add("6.5536");
	    combo_freq.add("7");
	    combo_freq.add("7.3728");
	    combo_freq.add("7.68");
	    combo_freq.add("8");
	    combo_freq.add("8.86");
	    combo_freq.add("10");
	    combo_freq.add("11.0592");
	    combo_freq.add("12");
	    combo_freq.add("14.7456");
	    combo_freq.add("16");
	    combo_freq.add("18.432");
	    combo_freq.add("20");
//	    combo_freq.add("24 MHz");
//	    combo_freq.add("24,576 MHz");
	    combo_freq.select(0);
	    combo_freq.setToolTipText("Base frequency of MCU");
	    combo_freq.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
		    	btnSave.setEnabled(true);
		    	compositeTop.redraw();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
	    });
	    
	    btnSave = new Button(compositeTop,SWT.NONE);
	    btnSave.setText("Save config");
	    btnSave.setSize(130,25);
    	
	    btnSave.setEnabled(false);
	    btnSave.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {}
			@Override
			public void mouseDown(MouseEvent e) {
				saveClick();
				btnSave.setEnabled(false);
			}
			@Override
			public void mouseUp(MouseEvent e) {}
	    }); // btnSave listener
	    
	    
	    progBar = new MyProgressBar(compositeTop, SWT.LEFT);
	    progBar.setSize(190, 20);
	    progBar.setPercent(75);
	    
	    Button btnClear = new Button(compositeTop, SWT.NONE);
	    btnClear.setText("Reset config");
	    btnClear.setLocation(670, 19);
	    btnClear.setSize(100,25);
	    btnClear.setEnabled(true);
	    
	    btnClear.addMouseListener(new MouseListener() {
	    	@Override
			public void mouseDown(MouseEvent e) { 	
	    		
	    		//PreferenceDialog pd = new PreferenceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new PreferenceManager());
	    		//pd.open();
	    		/*
	    		String id = "de.innot.avreclipse.core.preferences.AVRDudePreferences";
	    		String id2 = "de.innot.avreclipse.core.preferences.DatasheetPreferences";
	    	    //String id2 = "org.eclipse.wst.server.ui.runtime.preferencePage";
	    	    //final PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, id2, new String[] { id, id2 }, null);
	    	    final PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, id2, new String[] { id, id2 }, null);
	    		dialog.open();
	    		*/
	    		
	    		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	    		//MessageDialog.openInformation(shell, "Project Proeperties", "Properties window will open next");

	    		String propertyPageId = "de.innot.avreclipse.core.properties.AVRProjectProperties";
	    		String id = "Target";
	    		//PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(shell, currentProject, propertyPageId, new String[] {id}, null);
	    		PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(shell, currentProject, propertyPageId, null, null);
	    		dialog.open();
	    	    //return (dialog.open() == Window.OK);	    		
	    	}

			@Override
			public void mouseDoubleClick(MouseEvent e) { }
			
			@Override
			public void mouseUp(MouseEvent e) {
				progBar.setPercent((progBar.getPercent()+3)%100);
			}
	    });

	    // wypelnij combobox nazwami chipow.
	    //---------------------------------------------------
	    // druga czesc = SashForm - podzielona poziomo
	    sashForm = new SashForm(sashFormTop, SWT.HORIZONTAL);
	    sashForm.setSashWidth(1);
	    
	    // composite drugiej czesci  
	    compositeTree = new Composite(sashForm, SWT.NONE);
	    compositeTree.setFont(SWTResourceManager.getFont("Microsoft Sans Serif", 8, SWT.NORMAL));
	    compositeTree.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
	    compositeTree.setLayout(new FillLayout(SWT.FILL));

	    loadDeviceResourcesIntoTree(compositeTree);
	    
	    final Menu menu = new Menu(tree);
	    tree.setMenu(menu);

	    // Do not show menu, when no item is selected
	    menu.addListener(SWT.MenuDetect, new Listener() {
	      @Override
	      public void handleEvent(Event event) {
//	        if (table.getSelectionCount() <= 0) {
//	          event.doit = false;
//	        }
	      }
	    });
	    
	    tree.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Point point = new Point(event.x,event.y);
				try {
					tree.select(tree.getItem(point));
					// clear current menuitem 
					MenuItem[] items = menu.getItems();
		            for (int i = 0; i < items.length; i++) {
		                items[i].dispose();
		            }
		            // make new one menu item
					
		            
		            int PinNr=core.selectedChip.getPinNumberForPinNameInConfiguration(tree.getSelection()[0].getText());
		            //System.out.println("Selected pin" + tree.getSelection()[0].getText() );
		            
	            
		            if (core.selectedChip.avrPinsConfig.get(PinNr-1).getSelectedPinResouce().startsWith("PORT") 
		                && core.selectedChip.avrPinsConfig.get(PinNr-1).getSelectedPinName().endsWith(tree.getSelection()[0].getText())) {
		            	
		            	//System.out.println("Selected res " + core.selectedChip.avrPinsConfig.get(PinNr-1).getSelectedPinResouce());
		            	//System.out.println("Selected pin " + core.selectedChip.avrPinsConfig.get(PinNr-1).getSelectedPinName());
		            	
		            	MenuItem newItemINP = new MenuItem(menu,SWT.NONE);
		            	newItemINP.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								// TODO Auto-generated method stub
								int pinX=core.selectedChip.getPinNumberForPinNameInConfiguration(tree.getSelection()[0].getText());
								core.selectedChip.avrPinsConfig.get(pinX-1).setSelectedPinIsInput(true);
								core.selectedChip.avrPinsConfig.get(pinX-1).setSelectedPinIsPullUpOrHighState(false);
								tree.getSelection()[0].setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_in.gif"));
								saveClick();
							}
		            	});
		            	MenuItem newItemINPU = new MenuItem(menu,SWT.NONE);
		            	newItemINPU.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								// TODO Auto-generated method stub
								int pinX=core.selectedChip.getPinNumberForPinNameInConfiguration(tree.getSelection()[0].getText());
								core.selectedChip.avrPinsConfig.get(pinX-1).setSelectedPinIsInput(true);
								core.selectedChip.avrPinsConfig.get(pinX-1).setSelectedPinIsPullUpOrHighState(true);
								tree.getSelection()[0].setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_in_pup.gif"));
								saveClick();
							}
		            	});
		            	MenuItem newItemSEP = new MenuItem(menu,SWT.SEPARATOR);
		            	MenuItem newItemOUT = new MenuItem(menu,SWT.NONE);
		            	newItemOUT.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								// TODO Auto-generated method stub
								int pinX=core.selectedChip.getPinNumberForPinNameInConfiguration(tree.getSelection()[0].getText());
								core.selectedChip.avrPinsConfig.get(pinX-1).setSelectedPinIsInput(false);
								core.selectedChip.avrPinsConfig.get(pinX-1).setSelectedPinIsPullUpOrHighState(false);
								tree.getSelection()[0].setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_out.gif"));
								saveClick();
							}
		            	});
	            	
		            	newItemINP.setText("Input "); // + tree.getSelection()[0].getText());
		            	newItemINP.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_in.gif"));
		            	newItemINPU.setText("Input with Internal Pull Up"); // + tree.getSelection()[0].getText());
		            	newItemINPU.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_in_pup.gif"));
		            	newItemOUT.setText("Output"); 
		            	newItemOUT.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_out.gif"));
		            }

		            
				} catch (Exception e) { 
					//System.out.println("You not hit any TreeItem, ignore click!");
					//System.out.println(e.getMessage());
				}
				
	            //tree.setTopItem(tree.getItem(2));
	            //tree.redraw();
			} // public void handleEvent(Event event) {
	    }); // tree.addListener(SWT.MouseDown, new Listener() {

	    //--------------------------------------------------------------------------------
	    // ustawia zmienn¹ treeTopItemIndex na aktualna pozycje scrollbara 
	    // Pozwala to na zachowanie pozycji podczas klikania i odswiezania okna tree 
	    // (zasobow i funkcji pinow). Wartosc z treeTopItemIndex jest przypisywana 
	    // do aktualnej pozycji TOP drzewka tree po odswiezeniu (przeladowaniu) drzewka. 
	    tree.getVerticalBar().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScrollBar scb = tree.getVerticalBar();
				treeTopItemIndex=scb.getSelection();      
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
	    //------------------------------------------------------------------------------
	    // ... oraz druga czesc = composite a w srodku canvas
	    myViewParent = new Composite(sashForm, SWT.NONE);
	    myViewParent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
	    myViewParent.setLayout(new FillLayout(SWT.HORIZONTAL));
	    tabFolder = new TabFolder(myViewParent, SWT.FILL);
	    TabItem tiPackage = new TabItem(tabFolder,SWT.FILL);
	    tiPackage.setText("Package");
	    tiPackage.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/chip.gif"));

	    canvas = new Canvas(tabFolder, SWT.FILL);
	    canvas_offset_x = canvas_offset_y = 250;  //granice rysowania body

	    canvas_offset_x=canvas.getBounds().width/2 + 300;
	    canvas_offset_y=canvas.getBounds().height/2 + 300;

	    tiPackage.setControl(canvas);

	    TabItem tiGPIO = new TabItem(tabFolder,SWT.FILL);
	    tiGPIO.setText("GPIO Pins");
	    tiGPIO.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/inout.png"));
	    pinconf = new PinConfiguration(tabFolder, SWT.NONE);
	    pinconf.setProjectName(this.projectName);
	    pinconf.setProjectPath(this.projectPath);
	    
	    
	    tiGPIO.setControl(pinconf);
	    
//	    ScrolledComposite scroller = new ScrolledComposite(tabFolder, SWT.BORDER | SWT.V_SCROLL);
//	    canvas = new Canvas(scroller, SWT.NONE);
//	    canvas_offset_x = canvas_offset_y = 250;
//
//	    scroller.setContent(canvas);
//	    tiPackage.setControl(scroller);
	    
	    
//	    tiTimer = new TabItem(tabFolder,SWT.FILL);
//	    tiTimer.setText("TIMERS/COUNTERS");
//	    tiTimer.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/clock.png"));
	    //-------------------------------------------------------------------------	    
//	    TabItem tiUsart = new TabItem(tabFolder,SWT.FILL);
//	    
//	    compositeUsart = new Composite(tabFolder, SWT.FILL);
//	    compositeUsart.setLayout(new FillLayout()); 	    
//
//	    USART u0= new USART(compositeUsart,SWT.FILL);
//	    
//		tiUsart.setText("USART/UART");
//	    tiUsart.setControl(compositeUsart);
//	    tiUsart.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/rs232.png"));
	    
	    //-------------------------------------------------------------------------
	    //TabItem tiExtInt = new TabItem(tabFolder,SWT.FILL);
	    
	    //compositeExtInt = new Composite(tabFolder,SWT.FILL);
	    //compositeExtInt.setLayout(new FillLayout());

	    //EXTINT tiExtint = new EXTINT(compositeExtInt,tree,SWT.FILL);
	    
	    //tiExtInt.setText("EXTINT");
	    //tiExtInt.setControl(compositeExtInt);
	    //tiExtInt.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/wykrzyknik.png"));
	    //-------------------------------------------------------------------------
	    
//		TabItem tiAcomp= new TabItem(tabFolder,SWT.FILL);
//	    compositeAcomp= new Composite(tabFolder,SWT.FILL);
//	    tiAcomp.setText("ACOMP");
//	    tiAcomp.setControl(compositeAcomp);
//	    tiAcomp.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/acomp.png"));
//
//	    TabItem tiAdc= new TabItem(tabFolder,SWT.FILL);
//	    compositeAdc= new Composite(tabFolder,SWT.FILL);
//	    tiAdc.setText("ADC");
//	    tiAdc.setControl(compositeAdc);
//	    tiAdc.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/AC.png"));

//	    TabItem tiCode= new TabItem(tabFolder,SWT.FILL);
//	    compositeCode= new CodeTemplate(tabFolder,SWT.FILL);
//	    tiCode.setText("Code Templates");
//	    tiCode.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/OK.gif"));
//	    tiCode.setControl(compositeCode);
	    
	    canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
	    canvas.setLayout(new FillLayout(SWT.HORIZONTAL));
	    canvas.addPaintListener(new PaintListener() {
	        public void paintControl(PaintEvent e) {
	        	//redraw whole Package
	        	//mickpr:todo: zrobic rysowanie tylko gdy istnieje plik AtmegaXX-OBUDOWA.xml, inaczej blad
	        	PaintPackage(e.gc,canvas_offset_x,canvas_offset_y,combo_package.getText());
	        }
	    });	  
	    
	    canvas.addMouseMoveListener(new MouseMoveListener() {
	    	//int lastPinNr=0;
			@Override
			public void mouseMove(MouseEvent e) {

//				if (core.selectedChip.chipPackage.pins != null) {
//					// sprawdz czy wskazywany punkt jest na jakimœ pinie.
//					for (ChipPin cp : core.selectedChip.chipPackage.pins) {
//						
//						int x1 = cp.dx+canvas_offset_x; // lewy gorny rog obrysu pinu
//						int y1 = cp.dy+canvas_offset_y;
//						int x2 = cp.dx+canvas_offset_x+15; // prawy dolny rog obrysu pinu
//						int y2 = cp.dy+canvas_offset_y+15;
//						if (cp.orient ==PinLocation.LEFT || cp.orient==PinLocation.RIGHT) x2=x2+5;
//						if (cp.orient ==PinLocation.TOP || cp.orient==PinLocation.BOTTOM) y2=y2+5;
//
//						if (e.x >= x1 && e.x<=x2 && e.y>=y1 && e.y<=y2) {
//
//							if (lastPinNr!=cp.number) {
//								lastPinNr=cp.number;
//								pinMenuShape=null;
//								
//								// get pin functions and show them
//								String ss="PIN " + cp.number + "\n";
//								int ii=0;
//								for(AvrPinConfig apc : core.selectedChip.avrPinsConfig) {
//									ii++;
//									if (apc.getPinNumber()==cp.number)
//										ss = apc.getSelectedPinDescription();
//										//ss = ss + " " + apc.getPinNames().toString() + "\n";
//									//System.out.println("PinNr:" + apc.getPinNumber() + "," + apc.getPinNames().toString());
//									//System.out.println("Selected: " + apc.getSelectedPinName());
//									//}
//								}
//								 
//								
//								pinMenuShape=new PinMenuShape(e.x+10,e.y,canvas_offset_x,canvas_offset_y,ss );
//
//								//System.out.println(" pin =" + core.selectedChip.avrResources);
//								
//								canvas.redraw();
//								
//							} // if (lastPinNr!-.... 
//						} // if (e.x >=x1...
//					} // for (ChipPin cp...
//
//				} // if (core.selectedChip...
			} // public mouseMove..
		});  
	    // ...........................................canvas.addMouseMoveListener...

	    
	    
	    
	    
	    
	    
	    
	    
	    
			
	    
	    
	    
	    
	    canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				
				// podwojne klikniecie prawym klawiszem myszy? - nie robimy nic
				if (e.button == 3) return;				
				// odczytaj na ktorym pinie klknieto
				Integer pinNr = isMouseClickedAtPinPosition(e);
				// .. i jesli wogole kliknieto na jakims pinie - pinNr zwraca jego numer (>0)
				if (pinNr>0) {
					for(AvrPinConfig apc : core.selectedChip.avrPinsConfig)
						if (apc.getPinNumber()==pinNr) {
							if (apc.getSelectedPinIndex()==apc.getPinNames().size()-1)
								apc.setSelectedPinIndex(0); 
							else
								apc.setSelectedPinIndex(apc.getSelectedPinIndex()+1);
							
							Integer pn1 = apc.getPinNumber();
							String pnam1= apc.getSelectedPinName();
							System.out.println("kliknieto pin numer" + pn1);
							System.out.println("Wybrana aktualnie pozycja to:" + pnam1);
							// prawdopodobnie nie istnieje, lub raczej jest pusta 
							if (pinconf.configData!=null)
								System.out.println(pinconf.configData[1].getPinName());
							//pinconf.configData[pn1-1].setPinName(pnam1);
							// poszukaj, czy pin ma okreslenie portu
						}
				}
				saveClick();
			} // end of: public void mouseDouble
			
				
			public void mouseDown(MouseEvent e) {
				// jesli kliknieto prawym klawiszem (e.button=3)
				if (e.button == 3 && core.selectedChip.chipPackage.pins != null) {
					Integer pinNr=isMouseClickedAtPinPosition(e);
					
					
					// dla pinow oznaczonych jako PORT przelaczamy INPUT/INPUT-PULLUP/OUTPUT...
					if (pinNr>0) {
						if (core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinResouce().startsWith("PORT")) {
							// dla pinu INPUT - przelaczamy na input-pull-up
							if (core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsInput() && 
								!core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsPullUpOrHighState()) {
								core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsPullUpOrHighState(true);
							} else
							// dla pinu INPUT + PULL-UP - przelaczamy na OUTPUT
							if (core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsInput() && 
								core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsPullUpOrHighState()) {
								core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsPullUpOrHighState(false);
								core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsInput(false);
							} else
							// dla pinu OUTPUT - przelaczamy na INPUT
							if (!core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsInput()) {
								core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsInput(true);
							}
						}
					}
					saveClick();
					tree.redraw();					
					canvas.redraw();
				} else {
					//pinMenuShape = null;
					canvas.redraw();
				}
			}
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
			}
	    });
	    
	    sashForm.setWeights(new int[] {120, 421});
	    sashFormTop.setWeights(new int[] {40, 500});
	    
	    combo_package.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent e) {
	    	btnSave.setEnabled(true);
	    	compositeTop.redraw();
	    	
	    	core.selectedChip.Name = combo_chipname.getText();
    	
	   		core.selectedChip.LoadDefaultPinsAndResources(combo_package.getText());
	    	core.selectedChip.avrResources = new TreeMap<String, AvrResource>(core.selectedChip.avrResources);
	    	// wczytaj piny do zmiennej ChipPackage/ChipPackagePins naszego wybranego chipa
	    	core.selectedChip.chipPackage.LoadPackage(combo_package.getText());
	    	// przepisz piny
	    	for (int a=0;a<core.selectedChip.avrPinsConfig.size();a++) {
	    		core.selectedChip.chipPackage.pins.get(a).name = core.selectedChip.avrPinsConfig.get(a).getPinNames().get(0); 
	    	}
	    	core.selectedChip.LoadDefaultPinsAndResources(combo_package.getText());
	    	loadDeviceResourcesIntoTree(composResource);
	    	
			// uaktualnij konfiguracje wg wybranego pinu
	    	core.selectedChip.updateChipPackagePinsToSelectedInAvrPinsConfig();
	        // show loaded resources into tree :)
	        loadDeviceResourcesIntoTree(composResource);
	        //set checked pins 
	        core.selectedChip.setCurrentSelectedPinsInTree(tree);	
	    	
	    	btnSave.setEnabled(true);
	    	canvas.redraw();
	    }
	    }); // combo_package.addSelectionListener	    
	    	    
		// listener for selecting project in ProjectExplorer Window 
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listenerProject);
	}
	
    
    //-------------------------------------------------------------------------
    // sprawdzenie, czy kliknieto na pin w okreslonej pozycji, zwraca numer wskazanego pinu, lub 0 (brak wskazanego pinu)
    private Integer isMouseClickedAtPinPosition(MouseEvent e) {
    	if (core.selectedChip.chipPackage.pins != null) {
    		for (ChipPin cp : core.selectedChip.chipPackage.pins) {
			
    			int x1 = cp.dx+canvas_offset_x; // lewy gorny rog obrysu pinu
    			int y1 = cp.dy+canvas_offset_y;
    			int x2 = cp.dx+canvas_offset_x+15; // prawy dolny rog obrysu pinu
    			int y2 = cp.dy+canvas_offset_y+15;
			
    			if (cp.orient ==PinLocation.LEFT || cp.orient==PinLocation.RIGHT) x2=x2+5;
    			if (cp.orient ==PinLocation.TOP || cp.orient==PinLocation.BOTTOM) y2=y2+5;

    			if (e.x >= x1 && e.x<=x2 && e.y>=y1 && e.y<=y2) {
    				for(AvrPinConfig apc : core.selectedChip.avrPinsConfig) {
    					if (apc.getPinNumber()==cp.number) {
							return apc.getPinNumber();
    					} // if
    				} // for
    			} // if
    		} //for
    	} // if	
    	return 0;
    }
	//-------------------------------------------------------------------------		
	
	
	//--------------------------------------------------------------------------------------------
	// draw package body and pins
	private void PaintPackage(GC gc, int x, int y, String package_name) {
		int xx=0;
		int yy=0;
		// zoom - scale - skalowanie------------------------------------
		//		float scale = (float) 0.75;
		//		Transform tr =new Transform(gc.getDevice());
		//		tr.scale(scale,scale);
		//		gc.setTransform(tr);
		
		//todo: error check if chip not selected properly
		if (core.selectedChip == null) return;
		if (core.selectedChip.chipPackage == null) return;
		if (core.selectedChip.chipPackage.body == null) return;
		
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		gc.setLineWidth(1);
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		gc.fillRectangle(x +core.selectedChip.chipPackage.body.dx, x +core.selectedChip.chipPackage.body.dy, core.selectedChip.chipPackage.body.width, core.selectedChip.chipPackage.body.height);
		gc.drawRectangle(x +core.selectedChip.chipPackage.body.dx, x +core.selectedChip.chipPackage.body.dy, core.selectedChip.chipPackage.body.width, core.selectedChip.chipPackage.body.height);

		gc.setLineWidth(1);
		int xy=1;
		for (ChipPin chipPin  : core.selectedChip.chipPackage.pins) {

			// todo: can be error ... if null
			chipPin.CreatePinWithWideDescription(core, gc,chipPin.number, chipPin.orient, x+chipPin.dx,y+chipPin.dy, chipPin.color);

			if (core.selectedChip.avrPinsConfig.get(xy-1).getSelectedPinResouce().startsWith("PORT"))
				if (core.selectedChip.avrPinsConfig.get(xy-1).getSelectedPinIsInput())
					chipPin.CreatePinWithWideDescription(core, gc,chipPin.number, chipPin.orient, x+chipPin.dx,y+chipPin.dy, SWTResourceManager.getColor(0xA0,0xFF,0xA0));
				else
					chipPin.CreatePinWithWideDescription(core, gc,chipPin.number, chipPin.orient, x+chipPin.dx,y+chipPin.dy, SWTResourceManager.getColor(0xFF,0xA0,0xA0));
			xy++;
		}
		
		// obliczanie polozenia srodkowego - do wydrukowania nazwy MCU
		String text = core.selectedChip.Name; // nazwa mikrokontrolera
		AffineTransform affinetransform = new AffineTransform();      // obliczenie dlugosci tekstu
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
		Font font = new Font("Courier New", Font.PLAIN, 10);
		
		int textwidth = (int)(font.getStringBounds(text, frc).getWidth());
		int textheight = (int)(font.getStringBounds(text, frc).getHeight());

		// rotuj napisy dla wybranych obudow...
		if(core.selectedChip.chipPackage.Name.contains("DIP") || 
		   core.selectedChip.chipPackage.Name.contains("SOP") || 
		   core.selectedChip.chipPackage.Name.contains("SOIC")) {

	        // oblicz xx i yy do wlasciwego rysowania przy rotacji napisu
			xx=canvas_offset_x+core.selectedChip.chipPackage.body.dx;
			xx=xx-core.selectedChip.chipPackage.body.height/2;
			yy=canvas_offset_y+core.selectedChip.chipPackage.body.dy;
			yy=yy+core.selectedChip.chipPackage.body.width/2;
			xx=xx-textwidth; 
			yy=yy-textwidth/2 + 12;
				Transform oldTransform = new Transform(gc.getDevice());  
		        gc.getTransform(oldTransform);
				Transform tr = new Transform(gc.getDevice());
				tr.translate(x, y);
		        tr.rotate(-90); 
		        tr.translate(-x,-y);	
		        gc.setTransform(tr);			
			        // here we draw (rotated)
			   	    gc.setFont(SWTResourceManager.getFont("Courier New", 12, SWT.NORMAL));
					gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
					gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					gc.drawText(core.selectedChip.Name ,xx,yy);
		        gc.setTransform(oldTransform); // back to normal rotation
				tr.dispose();		
		} else {
			// dla pozostalych obudow rysuj nazwe procesora na œrodku, bez rotacji :)
			xx=canvas_offset_x+core.selectedChip.chipPackage.body.dx;
			xx=xx+core.selectedChip.chipPackage.body.width/2;
			yy=canvas_offset_y+core.selectedChip.chipPackage.body.dy;
			yy=yy+core.selectedChip.chipPackage.body.height/2;
			xx=xx-textwidth/2-12; 
			yy=yy-textheight/2;
			   	    gc.setFont(SWTResourceManager.getFont("Courier New", 12, SWT.NORMAL));
					gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
					gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
					gc.drawText(core.selectedChip.Name ,xx,yy);
		}
		

		
					//xx = gc.getClipping().width/2;
					//if (yy<0) yy=0;
		
//      gc.setTransform(oldTransform);
//		tr.dispose();		
		
		
//		// draw menu if visible.
//		if (pinMenuShape != null) {
//			pinMenuShape.PaintPinMenuShape(gc);
//		}
		
	}
	

	//--------------------------------------------------------------------------------------------
	public void loadDeviceResourcesIntoTree(Composite composite) {
		
		if (tree==null) {
			tree = new Tree(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL |SWT.CHECK); //
			//--------------------------------------------------------------------------------------------
			tree.addListener(SWT.MouseDown, new Listener() {
				@Override
				public void handleEvent(Event event) {
					 
			        Point point = new Point(event.x, event.y);
			        TreeItem item = tree.getItem(point);
			        if (item != null) {
			        	//System.out.print("Mouse down :" + item.getText() + " state: " + (item.getChecked()?"checked":"unchecked"));

/*			        	
			        	//automatyczne odznaczanie dzieci
			        	if (item.getItemCount()>0 && !item.getChecked()) {
			        		for (TreeItem it : item.getItems()) {
			        			it.setChecked(false);
			        			// jesli dziecko przeslanialo domyslna funckcje pinu (czyli '0')- trzeba ja przywrocic! :)
			        		}
			        	}

			        	//automatyczne zaznaczanie dzieci
			        	if (item.getItemCount()>0 && item.getChecked()) {
			        		for (TreeItem it : item.getItems()) {
			        			it.setChecked(true);
			        			// dla kazdego dziecka trzeba sprawdziæ czy nie ma kolizji i poinformowac z mozliwoscia wyboru
			        		}
			        	}
*/
			        	
			        	// przelaczenie funkcji
			        	if (item.getItemCount()==0) {
			        		
			        		// wyswietlanie opisu
				        	if (item.getData("descr") !=null) {
			        			//descrTxt.setText(item.getData("descr").toString());
			        			canvas.redraw();
				        	} 
			        		
			        		// mamy nazwe pinu... item.getText() i wiemy,  ze jest to pin, a nie zasob
			        		int pinNr = core.selectedChip.getPinNumberForPinNameInConfiguration(item.getText()); 

			        		// mamy numer pinu.		
			        		//System.out.println("Item name: " + item.getText() + " Item pinNr: " + pinNr);
			        		
			        		// teraz wystarczy sprawdzic, ktora funckcja jest wybrana aktualnie dla tego pinu
			        		// pinu numerowane s¹ od 1, ale index w ArrayList od 0 - stad "pinNr-1" 
//			        		int funcIndex = core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedIndex();
//			        		System.out.println("Current selected index =" + funcIndex);
//			        		System.out.println("pinNr=" + pinNr +", name=" + item.getText());	

			        		if (item.getChecked()) {
				        		//ustaw pin 
				        		core.selectedChip.setSelectedPinFunction(pinNr, item.getText());
			        		} else {
			        			// odznaczamy pin, "wyplywa" niejako jego domyslna (0) funkcja
			        			core.selectedChip.avrPinsConfig.get(pinNr -1).setSelectedIndex(0);
			        		}
			        		btnSave.setEnabled(true);
			        		
			    			// uaktualnij konfiguracje wg wybranego pinu
			        		core.selectedChip.updateChipPackagePinsToSelectedInAvrPinsConfig();

// -------------------------------------------- TUTAJ JEST SYF ------------------------------------------
			    	        /// sprawdz... 
//							ScrollBar scb = tree.getVerticalBar();
//							scb.getSelection()      
//			    	        scb.getThumb()
			    	        
			    	        // zapamietaj ktore drzewka sa rozwiniete
			    	        ArrayList<Boolean>  expandedResourceItems = new ArrayList<Boolean>(); 
			    	        for (TreeItem ti : tree.getItems()) {
			    	        	expandedResourceItems.add(ti.getExpanded());
			    	        }

			    	        // show loaded resources into tree :)
			    	        loadDeviceResourcesIntoTree(composResource);
			    	        
			    	        int ex=0;
			    	        for (TreeItem ti : tree.getItems()) {
			    	        	ti.setExpanded(expandedResourceItems.get(ex));
			    	        	ex++;
			    	        }
			    	        //set checked pins 
			    	        core.selectedChip.setCurrentSelectedPinsInTree(tree);	
			    			//mickpr tutaj
			    	        canvas.redraw();
			        	} 

			        	
			        	// clear current config
			        	pinconf.configData= new PinConfigData[core.selectedChip.avrPinsConfig.size()];
			    		for (int a=0;a<core.selectedChip.avrPinsConfig.size();a++) {
			    			pinconf.configData[a] = new PinConfigData(Integer.toString(core.selectedChip.avrPinsConfig.get(a).getPinNumber()), 
			    					core.selectedChip.avrPinsConfig.get(a).getSelectedPinResouce(), 
			    					core.selectedChip.avrPinsConfig.get(a).getSelectedPinName(), 
			    					core.selectedChip.avrPinsConfig.get(a).getSelectedPinIsInput(), 
			    					core.selectedChip.avrPinsConfig.get(a).getSelectedPinIsPullUpOrHighState(), 
			    					"");
			    			//core.selectedChip.avrPinsConfig..pins.get(a).color = this.getColorDependOnResourceAndName(core.selectedChip.avrPinsConfig.get(a).getSelectedPinResouce(),this.avrPinsConfig.get(a).getSelectedPinName());

			        		//pinBitNumber = Integer.parseInt(pinName.substring(2));
			        		
			        		//this.selectedChip.GpioPorts.put(portLetter, port);
			        		// 
			        		// get 
			        		//
			        	}			        	
//			        	try {
//							pinconf.saveConfigData(projectPath + "/.settings/pins.xml");
//						} catch (TransformerException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} catch (ParserConfigurationException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
			        	
			        	// pokaz opis
						ScrollBar scb = tree.getVerticalBar();
						scb.setSelection(treeTopItemIndex);      
			        	tree.redraw();
			        }				
				}
	        });	// addListener
	        
	    	//--------------------------------------------------------------------------------------------
	        tree.addListener(SWT.MouseDoubleClick, new Listener() {
				@Override
				public void handleEvent(Event event) {
			        Point point = new Point(event.x, event.y);
			        TreeItem item = tree.getItem(point);
				        if (item != null) {
				        	String itemText=item.getText();

	//			        	if (item.getData("descr") !=null) {
	//			        		System.out.println( " - " + item.getData("descr").toString());
	//			        		//item.getData(key)
	//			        	} else System.out.println("");
				        	
				            // 			        	System.out.println("Mouse fclick :" + itemText + " ");
				        	//System.out.println(itemText.substring(0,2));
				        
				        // tylko piny nale¿¹ce do PORTx 
				        int pinNr = core.selectedChip.getPinNumberForPinNameInConfiguration(itemText);
				        if (core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinResouce().startsWith("PORT")) {
				        	
				        	// zmiana  
				        	if (core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsInput() && 
				        		!core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsPullUpOrHighState()) {
				        			// set as input with pullup
				        			core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsInput(true);
				        			core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsPullUpOrHighState(true);
				        			item.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_in_pup.gif"));
				        	} else if (core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsInput() &&
				        		core.selectedChip.avrPinsConfig.get(pinNr-1).getSelectedPinIsPullUpOrHighState()) {
				        			//  set as output without pulllup
				        			core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsInput(false);
				        			core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsPullUpOrHighState(false);
				        			item.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_out.gif"));
				        	} else {
				        		// set as input without pullup
			        			core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsInput(true);
			        			core.selectedChip.avrPinsConfig.get(pinNr-1).setSelectedPinIsPullUpOrHighState(false);
			        			item.setImage(ResourceManager.getPluginImage("de.innot.avreclipse.devexp", "icons/pin_in.gif"));
				        	}
				        	 
				        	//item.getParent().redraw();
				        	tree.redraw();
				        } // if
			        } // if (item != null) 
				} // handleEvent
	        }); // addListener
		} 
		else {
			tree.removeAll();
		}
		core.FillTreeWithResources(tree);
	}
	
	public void reloadPackageComboAndSetFirstPackageAsDefault(String chipName) {
		for (AvailableMCUsListEntity chip : core.availableChips.chips) {
			//JOptionPane.showMessageDialog(null, chip.Name, "InfoBox: " + chip.Type, JOptionPane.INFORMATION_MESSAGE);
			//znaleziono chip
			if (chip.Name.compareToIgnoreCase(chipName)==0) {
				combo_package.removeAll();
				for (String cp : chip.Packages) {
					combo_package.add(cp);
				}
				//po zmianie uk³adu zawsze zmieniamy na pierwszy typ obudowy
				combo_package.select(0);
			}
		}
		loadDeviceResourcesIntoTree(composResource);
		canvas.redraw();		
	}
	//--------------------------------------------------------------------------------------------
	
	//--------------------------------------------------------------------------------------------	
	private int locateString(IDocument doc,int startOffset, String string) {
		int pos = 0;
		try {
			pos = doc.search(startOffset, string, true, true, false);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	 return pos;
	}

	//--------------------------------------------------------------------------------------------	
	private void generateCode() {
	    IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	    TextEditor editor = null;

	    if (part instanceof TextEditor) {
	        editor = (TextEditor) part;
	    }

	    if (editor == null) {
	        return;
	    }

	    IDocumentProvider dp = editor.getDocumentProvider();
	    IDocument doc = dp.getDocument(editor.getEditorInput());

	    //System.out.println("Wierszy: " + doc.getNumberOfLines());
	    
	    int offset;
		try {
			doc.replace(0, 0,"#include <avr/interrupt.h>\n");

			//offset = doc.getLineOffset(doc.getNumberOfLines()-4);
			offset =  locateString(doc,0,"int main(");
			
			System.out.println("offset:" + offset);
			
			doc.replace(offset, 0, "dupa");
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
//	    StyledText text = (StyledText) editor.getAdapter(Control.class);
//
//	    int caretOffset = text.getCaretOffset();
//
//	    IDocumentProvider dp = editor.getDocumentProvider();
//	    IDocument doc = dp.getDocument(editor.getEditorInput());
//
//	    IRegion findWord = (IRegion) CWordFinder.findWord(doc, caretOffset);
	    String text2 = "";
//	    if (((IDocument) findWord).getLength() != 0)
//	        text2 = text.getText(((org.eclipse.jface.text.IRegion) findWord).getOffset(), ((org.eclipse.jface.text.IRegion) findWord).getOffset()
//	                + ((IDocument) findWord).getLength() - 1);
	    
	    System.out.println("znalezione: " + text2);
	    //return text2;
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
	
	public void dispose() {
		// important: We need do unregister our listener when the view is disposed
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listenerProject);
		super.dispose();
	} // of void dispose
	

	private void setEnableDeviceView(boolean enable) {
		if (!enable) {
			combo_chipname.deselectAll();
			combo_freq.deselectAll();
			combo_package.deselectAll();
			combo_chipname.setBackground(SWTResourceManager.getColor(255,200,200));
		} else {
			combo_chipname.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		}
		tree.setVisible(enable);
		canvas.setVisible(enable);
		lblChipSelect.setEnabled(enable);
		combo_chipname.setEnabled(enable);
		combo_freq.setEnabled(enable);
		combo_package.setEnabled(enable);
		tabFolder.setEnabled(enable);
		btnSave.setEnabled(enable);
	}

	/**
	 * Save current configuration to the properties
	 */
	private void saveClick() {
		PluginPreferences.set("Package", combo_package.getText());
		Double f = Double.parseDouble(combo_freq.getText())*1000000;
		core.setMcuFrequency(f);
		//PluginPreferences.set("ClockFrequency", Double.toString(f));
				
		storeUnfoldedTreeElements(tree);
		
		// zapisz konfiguracje pinow
		core.selectedChip.SavePinConfigFunctions();
		core.selectedChip.SavePinConfigIsInput();
		core.selectedChip.SavePinConfigIsPullUpOrHighState();
		
		// uaktualnij konfiguracje wg wybranego pinu
        core.selectedChip.updateChipPackagePinsToSelectedInAvrPinsConfig();
        //System.out.println("xxxxxxxxxxxxxxxxxxx");
        
        // show loaded resources into tree :)
        loadDeviceResourcesIntoTree(composResource);
        //set checked pins 
        core.selectedChip.setCurrentSelectedPinsInTree(tree);

        // odtworz ustawienia drzewka
        restoreUnfoldedTreeElements(tree);
        
        // save pin configuration
        //savePinConfigToXML(projectPath + "/.settings/pins.xml");
		System.out.println(core.selectedChip.getDDRportValue("PORTA"));
		System.out.println(core.selectedChip.getDDRportValue("PORTB"));
		System.out.println(core.selectedChip.getDDRportValue("PORTC"));
		System.out.println(core.selectedChip.getDDRportValue("PORTD"));
		System.out.println(core.selectedChip.getDDRportValue("PORTE"));
		System.out.println(core.selectedChip.getDDRportValue("PORTF"));
		
		core.selectedChip.printAllPorts();
		canvas.redraw();
	}
	
	
	
	ArrayList<Boolean> unfolded;
	/**
	 * Store unfolded tree elements
	 */
	public void storeUnfoldedTreeElements(Tree tree) {
		unfolded = new ArrayList<Boolean>();
		int itemCount = tree.getItemCount(); 
		
		for (int i =0;i<itemCount;i++) {
			unfolded.add(tree.getItem(i).getExpanded());
		}
	} // end of : public void storeUnfoldedTreeElements.....
	
	public void restoreUnfoldedTreeElements(Tree tree) {
		for (int i =0;i<unfolded.size();i++) {
			tree.getItem(i).setExpanded(unfolded.get(i));
		}
	}
	
} //of class
