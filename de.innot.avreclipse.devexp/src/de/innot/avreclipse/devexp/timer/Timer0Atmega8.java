package de.innot.avreclipse.devexp.timer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BoxLayout;
import de.innot.avreclipse.devexp.*;
/*
 * TCCR0  - 8 bitowy rejestr kontrolny timera (prescaler i takie tam)
 * TIMSK  - 8bit rejestr masek przeran (rowniez to TIMER1 i TIMER2)
 * TIFR   - 8bit rejestr flag przerwan - sygnalizuj¹cych wyst¹pienie jakiegos przerwania (rowniez dla TIMER1 i TIMER2)
 * TCNT0  - rejestr liczacy (tylko tryb "normal"
 */


public class Timer0Atmega8 extends Composite {
	private double mcuFreq=0; 	// input freq
	private double counterFreq;	// freq after prescaller
	private double resultFreq;	// freq of invoking IRQ 
	private double resultDelay;	// timer period between IRQs
	
	private Group grpTimer;		
	private Label lblCalc;

	private Label lblClockSrc; // selection prescaller and it's label
	public Combo comboClockSrc;
	
	private Label lblClockPreload; // wartosc wpisywana do TCNT0 (rowniez w przerwaniu - recznie) 
	private Spinner spiTCNT0;	   // od ktorej liczy licznik TCNT0
	
	private Label lblTCNT0inverse; // ile razy "cyka" timer TCNT0 (odwrotnosc TCNT0, bo TCNT0 liczy od zadanej wartosci do 255, przerwanie i znow od zera) 
	private Text txtTCNT0inverse;

	private Label lblResultFreq;   // wynikowa czestotliwosc (Hz) 
	private Text txtResultFreq; 

	private Label lblResultDelay;   // wynikowy okres (ms) 
	private Text txtResultDelay; 

	private Label lblChooseFreq;   // wybrana przez nas czestotliwosc (Hz) 
	private Text txtChooseFreq; 
	
	private Label lblChooseDelay;   // wybrany przz nas delay (ms) 
	private Text txtChooseDelay; 

	private Label lblPercentError;   // procentowy b³¹d 
	private Text  txtPercentError; 
	
	private Button btnCode;
	
    public Timer0Atmega8(Composite parent, final double mcuFreq) {
    	super(parent, SWT.NONE);

    	this.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
    	this.mcuFreq = mcuFreq;
   	
    	grpTimer = new Group(parent, SWT.SHADOW_IN);
		grpTimer.setText(" TIMER0 (8-bit) ");
		grpTimer.setLayout(new GridLayout(2,false));
		//grpTimer.setBackground(SWTResourceManager.getColor(0,152,255));
		
		
		//--------------------------------------------------------------------------------------
		lblClockSrc = new Label(grpTimer, SWT.NONE);
		lblClockSrc.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblClockSrc.setText("Clock source / Prescaler :");
		
		comboClockSrc = new Combo(grpTimer, SWT.NONE);
		comboClockSrc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		comboClockSrc.add("Timer/Counter stopped");
		comboClockSrc.add("CLK/1 - (No prescaling)");
		comboClockSrc.add("CLK/8 - (From prescaler)");
		comboClockSrc.add("CLK/64 - (From prescaler)");
		comboClockSrc.add("CLK/256 - (From prescaler)");
		comboClockSrc.add("CLK/1024 - (From prescaler)");
		comboClockSrc.add("CLK from T0 (on faling edge)");
		comboClockSrc.add("CLK from T0 (on rising edge)");
		comboClockSrc.select(0);
		comboClockSrc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkWidget();
				calcResultFreqAndDelay();
			}
		}); // addSelectionListener
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ TCNT od ktorej zaczynamy liczenie
		lblClockPreload = new Label(grpTimer, SWT.NONE);
		lblClockPreload.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblClockPreload.setText("TCNT0 value (Preloaded also in IRQ) :");
		spiTCNT0 = new Spinner(grpTimer, SWT.BORDER);
		spiTCNT0.setMaximum(255);
		spiTCNT0.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiTCNT0.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();
				calcResultFreqAndDelay();
			}
		});
		
		//---------------------------------------------------------------------------
		// ile razy cyka licznik (odwrotnosc TCNT0)
		lblTCNT0inverse = new Label(grpTimer,SWT.NONE);		
		lblTCNT0inverse.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTCNT0inverse.setText("TCNT0 tick counts (inverse of TCNT0):");
		txtTCNT0inverse = new Text(grpTimer,SWT.BORDER);
		txtTCNT0inverse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTCNT0inverse.setText("0.00");
		txtTCNT0inverse.setEditable(false);
		
		//-------------------------------------------------------------------------------
		// w rezultacie otrzymujemy czestotliwosc ... 
		lblResultFreq = new Label(grpTimer, SWT.NONE);
		lblResultFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblResultFreq.setText("Interrupt frequency (Hz) :");
		txtResultFreq = new Text(grpTimer,SWT.BORDER);
		txtResultFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtResultFreq.setText("0.00");
		txtResultFreq.setEditable(false);
		
		//----------------------------------------------------------------------
		// i okres czestotliwosc ... 
		lblResultDelay = new Label(grpTimer, SWT.NONE);
		lblResultDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblResultDelay.setText("Time between interrupts (ms) :");
		txtResultDelay = new Text(grpTimer,SWT.BORDER);
		txtResultDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtResultDelay.setText("0.00");
		txtResultDelay.setEditable(false);
		
		//------------------------------------------------------------------------
		lblCalc = new Label(grpTimer, SWT.NULL);
		lblCalc.setText(" Calculation ");
		Label lblCalc2 = new Label(grpTimer, SWT.NULL);
		lblCalc2.setText("---------------------------");
		//lblCalc.setLayout(new GridLayout(2,true));
    	//grpCalc.setBackground(SWTResourceManager.getColor(0,152,255));

		double minFreqHz = mcuFreq / 1024 / 256;
		double maxDelayMs = 1000 / (mcuFreq / 1024 / 256);  
		//---------------------------------------------------------------------------
		// wybrana czestotliwosc w Hz
		lblChooseFreq = new Label(grpTimer, SWT.NONE);
		lblChooseFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
		lblChooseFreq.setText("Selected frequency (>" + Double.toString(minFreqHz) + ") (Hz) :");
		lblChooseFreq.setBackground(SWTResourceManager.getColor(255, 255, 255));

		
		txtChooseFreq = new Text(grpTimer,SWT.BORDER);
		txtChooseFreq.setBackground(SWTResourceManager.getColor(255,255,190));
		txtChooseFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtChooseFreq.setText("0.00");
		txtChooseFreq.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					double d = 1000 / Double.parseDouble(txtChooseFreq.getText());
					txtChooseDelay.setText(Double.toString(d));
					double minimalFrequencyHz = mcuFreq / 1024 / 256;
					double maxDelayMs = 1000 / (mcuFreq / 1024 / 256);
					if (Double.parseDouble(txtChooseFreq.getText()) < minimalFrequencyHz) { 
						txtChooseFreq.setBackground(SWTResourceManager.getColor(255, 190, 190));} 
					else {  
						txtChooseFreq.setBackground(SWTResourceManager.getColor(255, 255, 190)); }

					if (Double.parseDouble(txtChooseDelay.getText()) > maxDelayMs) {
						txtChooseDelay.setBackground(SWTResourceManager.getColor(255, 190, 190)); }
					else {
						txtChooseDelay.setBackground(SWTResourceManager.getColor(255, 255, 190)); }
					
					calcValuesByFrequencyAndSetAllControls(Double.parseDouble(txtChooseFreq.getText()));
					txtChooseDelay.redraw();
					txtChooseFreq.redraw();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
			
		});
		//-------------------------------------------------------------------------------
		lblChooseDelay = new Label(grpTimer, SWT.NONE);
		lblChooseDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblChooseDelay.setText("Selected delay (< "+ Double.toString(maxDelayMs) +") (ms) :");
		lblChooseDelay.setBackground(SWTResourceManager.getColor(255, 255, 255));
		txtChooseDelay = new Text(grpTimer,SWT.BORDER);
		txtChooseDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtChooseDelay.setText("0.00");
		txtChooseDelay.setBackground(SWTResourceManager.getColor(255,255,190));
		
		txtChooseDelay.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					double d = 1000/ (Double.parseDouble(txtChooseDelay.getText()));
					txtChooseFreq.setText(Double.toString(d));
					double minimalFrequencyHz = mcuFreq / 1024 / 256;
					double maxDelayMs = 1000 / (mcuFreq / 1024 / 256);

					if (Double.parseDouble(txtChooseFreq.getText()) < minimalFrequencyHz) {
						txtChooseFreq.setBackground(SWTResourceManager.getColor(255, 190, 190));
					} else {
						txtChooseFreq.setBackground(SWTResourceManager.getColor(255, 255, 190));
					}

					if (Double.parseDouble(txtChooseDelay.getText()) > maxDelayMs) {
						txtChooseDelay.setBackground(SWTResourceManager.getColor(255, 190, 190));
					} else {
						txtChooseDelay.setBackground(SWTResourceManager.getColor(255, 255, 190));
					}
					calcValuesByFrequencyAndSetAllControls(Double.parseDouble(txtChooseFreq.getText()));
					txtChooseDelay.redraw();
					txtChooseFreq.redraw();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
			
		});

		lblPercentError = new Label(grpTimer, SWT.NONE);
		lblPercentError.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblPercentError.setText("Percentage of error (%) :");
		lblPercentError.setBackground(SWTResourceManager.getColor(255, 255, 255));
		txtPercentError = new Text(grpTimer,SWT.BORDER);
		txtPercentError.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtPercentError.setText("0.00");
		txtPercentError.setBackground(SWTResourceManager.getColor(190,190,190));
		txtPercentError.setEditable(false);

		//---------------------------------------------------------------------------------------------
	    Button btnClear = new Button(grpTimer, SWT.NONE);
	    btnClear.setText("Generuj kod");
	    btnClear.setLocation(0,0);
	    btnClear.setSize(100,100);
	    btnClear.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				getTextFromCursor();
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
	    	
	    });
		
		
    }


    public Pair<Integer,Double> getBestFittedTCNT0value(int selectedPrescaler, double freq) {
    	double f1,f2;
    	double delta;
    	Map<Integer,Double> posSuccess = new HashMap<Integer,Double>();
    	
		f1 = mcuFreq/selectedPrescaler/256; // najmniejsza czestotliwosc
		f2 = mcuFreq/selectedPrescaler/1;   // najwieksza czestotliwosc
		if (freq<f1 || freq>f2) {
			return new Pair<Integer, Double>(-1,1000000000.00); // jesli wybrany prescaler pozwala na generowanie tylko czestotliwosci poza zaresem - opusc z wynikiem 0.00 
		}
		//System.out.println("min (f1), max(f2)= " + f1 + ","+ f2);
    	
		delta=1000000000;
		// wybrany prescaler jednak pozwala, przelecmy przezen i tylko wartosci ktore obejmuj¹ zadan¹ czestotliwosc (freq)
		// wpiszmy do HashMap
    	for (int i=0;i<=255;i++) {
    		f1 = mcuFreq/selectedPrescaler/(i+1); //mniejsza czestotliwosc
    		f2 = mcuFreq/selectedPrescaler/i;  //wieksza 
    		    		
    		// jesli miedzy dwoma czestotliwosciami
    		if (freq>=f1 && freq<=f2)	 {
    			if (Math.abs(freq-f1)<delta) {  // jesli nowa roznica jest mniejsza, to 
    				delta=Math.abs(freq-f1); 	// odrzucamy stara i bierzemy j¹
    				posSuccess.put(i, delta);	// odkladamy do wysortowania potem
    			}
    		}
    	}
    	
    	//mamy wartosci danego prescalera w HashMap, teraz wybierzmy z nich t¹ najbardziej zblizona do idealu
    	delta=1000000000;
		//System.out.println("prescaler " + selectedPrescaler);
		int valTCNT0=-1;
		//System.out.println("size=" + posSuccess.size());
		for (Entry<Integer, Double> entry : posSuccess.entrySet()) {
		    Double valDifference = entry.getValue();
		    //System.out.println("Values :" + entry.getKey() + ", diff="+ entry.getValue());
		    if (valDifference<delta) {
		    	//System.out.println("JEST");
		    	//System.out.println("prescaler:"+ selectedPrescaler+ " freq:" + freq+ " key=" + entry.getKey() + ", valDiff=" + entry.getValue() );
		    	valTCNT0=entry.getKey();
		    	delta=valDifference; 
		    }
		}
		//System.out.println("result: " + valTCNT0 + "," + delta);
		return new Pair<Integer, Double>(valTCNT0,delta);
    	
    } // public double calc...
    
    
    private void calcValuesByFrequencyAndSetAllControls(double freq) {
    	Pair<Integer, Double> retPair;
    	Map<Integer,Double> pom = new HashMap<Integer,Double>();
    	double diff = 0; // duza wartosc prescalera, przy niej blad jest zawsze mniejszy
    	int valueTCNT0 = 1000000000; 
    	int x1=-1,x2=-1, x3=-1, x4=-1, x5=-1;
    	double d1=0,d2=0,d3=0,d4=0,d5=0;
    	
    	retPair = getBestFittedTCNT0value(1, freq); 
    	if (retPair.getFirst()>=0) { // cos jest
    		x1=retPair.getFirst();  
    		d1=retPair.getSecond();
    	}

    	retPair = getBestFittedTCNT0value(8, freq);
    	if (retPair.getFirst()>=0) {
        	x2=retPair.getFirst();
        	d2=retPair.getSecond();
    	}
    		
    	retPair = getBestFittedTCNT0value(64, freq);
    	if (retPair.getFirst()>=0) {    	
    		x3=retPair.getFirst();
    		d3=retPair.getSecond();
    	}
    	
    	retPair = getBestFittedTCNT0value(256, freq);
    	if (retPair.getFirst()>=0) {    	
    		x4=retPair.getFirst();
    		d4=retPair.getSecond();
    	}
    	
    	retPair = getBestFittedTCNT0value(1024, freq);
    	if (retPair.getFirst()>=0) {    	
    		x5=retPair.getFirst();
    		d5=retPair.getSecond();
    	}
    	
    	System.out.println("----------------------------------");
    	if (x1!=-1) System.out.println("x1, d1 "+ Integer.toString(x1) + ","+ d1);
    	if (x2!=-1) System.out.println("x2, d2 "+ Integer.toString(x2) + ","+ d2);
    	if (x3!=-1) System.out.println("x3, d3 "+ Integer.toString(x3) + ","+ d3);
    	if (x4!=-1) System.out.println("x4, d4 "+ Integer.toString(x4) + ","+ d4);
    	if (x5!=-1) System.out.println("x5, d5 "+ Integer.toString(x5) + ","+ d5);
    	
    	valueTCNT0=-1;
    	diff=1000000000;
    	if (x5!=-1 && diff>d5) { diff=d5; valueTCNT0=x5; comboClockSrc.select(5); }
    	if (x4!=-1 && diff>d4) { diff=d4; valueTCNT0=x4; comboClockSrc.select(4); }
    	if (x3!=-1 && diff>d3) { diff=d3; valueTCNT0=x3; comboClockSrc.select(3); }
    	if (x2!=-1 && diff>d2) { diff=d2; valueTCNT0=x2; comboClockSrc.select(2); }
    	if (x1!=-1 && diff>d1) { diff=d1; valueTCNT0=x1; comboClockSrc.select(1); }
    	
    	System.out.println("Wybrano TCNT0="+ valueTCNT0 + " / diff=" + diff);
    	//System.out.println("Diff by %:" + diff*100/freq);

    	diff=diff*100/freq;
    	txtPercentError.setText(Double.toString(diff));
    	if (diff<0.01) txtPercentError.setBackground(SWTResourceManager.getColor(190, 255, 190));
    	if (diff>0.01 && diff<1.0) txtPercentError.setBackground(SWTResourceManager.getColor(255, 255, 190));
    	if (diff>1.0 && diff<2.0) txtPercentError.setBackground(SWTResourceManager.getColor(255, 190, 190));
    	if (diff>2.0) txtPercentError.setBackground(SWTResourceManager.getColor(255, 120, 120));
    	//TODO: 
    	if (valueTCNT0<0) 
    		System.out.println("Value not found");
    	
    	spiTCNT0.setSelection(255-valueTCNT0);
    	
    	// wybierz taki prescaller, który daje najmniejsza z roznic miedzy zadana a realna czestotliwoscia
    	
    	

    	//System.out.println("Frequency          : " + Double.toString(freq));
    	//System.out.println("Selected prescaller: " + selectedPrescaler);
    	//System.out.println("Calculated difference:"+ calcDifferenc(selectedPrescaler, freq));
    }
    
    
	private void calcResultFreqAndDelay() {
		int podzial=0;
		if (comboClockSrc.getSelectionIndex()==0)  counterFreq=0;
		if (comboClockSrc.getSelectionIndex()==1)  counterFreq=mcuFreq;
		if (comboClockSrc.getSelectionIndex()==2)  counterFreq=mcuFreq/8;
		if (comboClockSrc.getSelectionIndex()==3)  counterFreq=mcuFreq/64;
		if (comboClockSrc.getSelectionIndex()==4)  counterFreq=mcuFreq/256;
		if (comboClockSrc.getSelectionIndex()==5)  counterFreq=mcuFreq/1024;
		if (comboClockSrc.getSelectionIndex()==6)  counterFreq=0;
		if (comboClockSrc.getSelectionIndex()==7)  counterFreq=0;
		podzial=(256-spiTCNT0.getSelection());
		resultFreq=counterFreq/podzial;
		
		resultDelay=1/resultFreq;
		txtTCNT0inverse.setText(Integer.toString(podzial));

		//update data in lbl and txt fields with values
		if (resultFreq>=1000000) {
			lblResultFreq.setText("Interrupt freq. (MHz) :");
			txtResultFreq.setText(Double.toString(resultFreq/1000000));
		} else if (resultFreq>=1000 && resultFreq<1000000) {
			lblResultFreq.setText("Interrupt freq. (kHz) :");
			txtResultFreq.setText(Double.toString(resultFreq/1000));
		} else if (resultFreq<1000) {
			lblResultFreq.setText("Interrupt freq. (Hz) :");
			txtResultFreq.setText(Double.toString(resultFreq));
		}
						
		if (resultDelay>=1) {
			lblResultDelay.setText("Time between interrupts (S) :");
			txtResultDelay.setText(Double.toString(resultDelay));
		} else if (resultDelay>=0.001 && resultDelay<1) {
			lblResultDelay.setText("Time between interrupts (mS) :");
			txtResultDelay.setText(Double.toString(resultDelay*1000));
		} else if (resultDelay<0.001 && resultDelay>=0.000001) {
			lblResultDelay.setText("Time between interrupts (uS) :");
			txtResultDelay.setText(Double.toString(resultDelay*1000000));
		} else if (resultDelay<=0.000001) {
			lblResultDelay.setText("Time between interrupts (nS) :");
			txtResultDelay.setText(Double.toString(resultDelay*1000000000));
		}
		redraw();
		
    }
	
	private void getTextFromCursor() {
	    IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
	            .getActivePage().getActiveEditor();
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
			offset =  locateMainRow(doc);
			
			System.out.println("offset:" + offset);
			
			doc.replace(offset, 0, getConfigTCCR0());
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

	
	
	@SuppressWarnings("deprecation")
	private int locateMainRow(IDocument doc) {
	 int pos = 0;
	try {
		pos = doc.search(0, "int main", true, true, false);
		//pos= doc.search(pos, "{", true, true, false);
	} catch (BadLocationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 return pos;
	}
	
	private String getConfigTCCR0() {
		String ret="";
		ret = "\nvoid init_Timer0() {\n\t" 
				+ "TIMSK |= (1 << TOIE0); // enable int\n\t" 
				+ "sei();\n\t" 
				+ "TCNT0=" + spiTCNT0.getText() + "; // preload value\n\t"
				+ "TCCR0 |=";
		switch (comboClockSrc.getSelectionIndex()) {
		case 1: ret += "(1 << CS00)";
			break;
		case 2: ret += "(1 << CS01)";
			break;
		case 3: ret += "(1 << CS01) | (1 << CS00)";
			break;
		case 4: ret += "(1 << CS02)";
			break;
		case 5: ret += "(1 << CS02) | (1 << CS00)";
			break;
		default:
			ret ="";
			return ret;
			
		}
		ret +="; // set prescaller and start timer\n";
		ret +="}\nISR (TIMER0_OVF_vect)  // timer0 overflow interrupt\n";
		ret +="{\n\tTCNT0=" + spiTCNT0.getText() + "; // preload value\n\t";
		ret +="// TODO: your code here\n}\n\n";
		return ret;
	}


	
}  // end of class
