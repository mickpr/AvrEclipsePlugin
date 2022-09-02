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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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

/* TIMER1 jest dwukierunkowy
 * 
 * TCNT1  - 16bitowy rejestr (TCNT1H i TC1NT1L) wartosc licznika
 * OCR1A  - 16bit Output Compare A Register
 * OCR1B  - 16bit Output Compare B Register
 * ICR1   - 16 bit Input Compare Register z wejœcia INT1 albo z wyjœcia komparatora!!! 
 * TCCR1B - 8 bitowy rejestr kontrolny timera (prescaler i WGM12 i WGM13)
 * TCCR1A - 8 bitowy rejestr kontrolny timera (WGM11 i WGM10)
 * TIMSK  - 8bit rejestr masek przeran (rowniez to TIMER0 i TIMER2)
 * TIFR   - 8bit rejestr flag przerwan - sygnalizuj¹cych wyst¹pienie jakiegos przerwania
 * 
 zrodla sygnalu - wewnêtrznie, przez prescaler lub z wejscia T1
 zegar mo¿e zwiêkszaæ lub zmniejszaæ podany 
 TCNT1 jest porownywany z podwójnie buforowanymi rejestrami OCR1A i OCR1B. 
 Jeœli porównywanie by³o pozytywne ustawia flagê w TIFR.
  
 Wartoœc maksymalna timera mo¿e byæ ustalona na OCR1A, ICR1, lub szereg sta³ych wartoœci.
 Jeœli wkorzystujemy OCR1A do okreœlenia wartosci TOP w trybie PWM, to nie mo¿emy wykorzystaæ 
 OCR1A do generowania trybu PWM. Jednak¿e wartoœc TOP w tym przypadku mo¿e byæ podwójnie buforowana
 pozwalaj¹c na zmianê tej wartoœci TOP w czasie dzia³ania. Jesli potrzebna jest wartoœæ sta³a, 
 to mo¿na u¿yæ rejestru ICR1 zwalniaj¹c do u¿ycia w PWM rejestr OCR1A. 
 
 TCNT1 (16bit) liczy w górê, w dó³ - w zale¿noœci od ustawieñ WGM13:0. Wartoœæ odczytywana jest buforowane 
 (pozwala to odczytac w jednym takcie dwa bajty). 
 
 ICP1 - wejœcie zliczaj¹ce impulsy zewnêtrzne lub impusy z komparatora.  
    Mo¿na mierzyæ czestotliwosc, czas, cykl zajetosci i inne. 
 	Gdy CPU wykryje zmiane na ICP1 - przepisuje aktualna wartoœc z TCNT1 do ICR1 
 	(ICR1 nie zwiêksza siê tak jak TCNT1, tylko jest wype³niane kopia TCNT1 co okrelsony czas)
 	Po wykryciu zmiany ustawiana jest flaga ICF1.  	Jesli dodatkowo ustawiono flage przerwania TICIE1="1" 
 	to wystêpuj¹ca flaga ICF1 powoduje uruchomienie procedury obslugi przerwania (flaga ICF1 jest kasowana)
 	
 	Rejestt ICR1 (16 bit) moze byc  zapisany jedynie podczas u¿ycia "Waveform generation mode", który u¿ywa ICR1 
 	do zdefiniowania wartoœci TOP W tym przypadku WGM13:0 misi byc ustawiony przed wpisaniem wartoœci TOP do ICR1.
 	Wpisyanie zaczynamy od ICR1H a potem ICR1L. Kompilator zadbaæ powinien o to automatycznie. 

    ICP1 moze byc rowniez podlaczone do komparatora analogowego. Wtedy ustawiamy bit ACIC w rejestrze ACSR 
    (Analog Comparator Control and Status Register). Uwaga, bo sama zmiana zrodla wyzwalania moze wyzwoliæ
    Input Capture. Dlatego ICF (Input Capture Flag) musi byæ wyczyszczona po tej zmianie.
    
    Obydwa ICP1 i ACO (Analog Comparator Output) s¹ przepuszczane przez filtr i uklad wykrywania zbocza - 
    tak jak dla wejscia T1.  Uk³ady te wprowadzaja opoznienie 4 cykli zegara. Sa one aktywne zawsze, 
    chyba, ¿e  Timer/Counter jest w trybie Waveform Generator które u¿ywa ICR1 do definiowania wartosci TOP.
     
    Input Capture moze byc wyzwolone programowo poprzez zmiane wartoœci danego bitu portu zawieraj¹cego wejscie ICP1.
     
    Uk³ad "noise canceler" opozniajacy cykl o 4 takty jest wlaczany przez ustawienie bitu ICNC1 w rejestrze TCCR1B. 
    4 cykle zegara (bez prescalera)! 
     
    Kluczowe przy Input Capture jest dbanie o to by odczytywaæ wartoœæ od razu po jej zarejestrowaniu. Jesli program nie zdazy, 
    moze sie zdarzyc, ze kolejne zdarzenie siê zarejestruje i stracimy kontrolê nad tym, jaka faktycznie zmiana zasz³a, a np. 
    mierzony czas bêdzie b³êdny.   Wartoœæ ICR1 powinna w przerwaniu zostaæ odczytana najszybciej jak to mo¿liwe. 
     
	Output Compare Unit. 
	16-bitowy komparator porownuje stale wartosc TCNT1 z OCR1x (OCR1A, OCR1B). 
	Gdy zgodzi sie z któr¹œ z wartosci ustawiana jest flaga OCF1x i w razie zezwolenia na przerwanie wywo³ywane jest przrwanie.
	W przerwaniu flaga jest automatycznie kasowana. Mo¿na j¹ skasowaæ programowo - wpisuj¹c "1" do niej.
	 
	Uk³ad Waveform generator u¿ywa sygna³u zgodnoœci do generowania odpowiedniego do ustawien (WGM13:0 i COM1x1:0) sygnalu.  
	Wartosci TOP i BOTTOM oraz tryb okresla - co wychodzi z wyjsc. Specjalna funkcja OCR1A pozwala zdefiniowaæ wartosc TOP
	(która jest rozdzielczosci¹ licznika).  Dodatkowo okreœla okres czasu generowany przez timer.
	 
	Rejestry OCR1x sa podwójnie buforowane podczas uzywanie dowolnego z 12-tu trybów PWM. Dla normalnego trybu CTC buforowania brak :)
	Buforowanie to synchronizuje uaktualnianie rejestru OCR1x do TOP czy BOTTOM podczas sekwencj zliczania.  Synchronizacja ta 
	zapobiega wystapieniu nieparzystych, niesymetrycznych impusów PWM, tym samym tworzenia PWM bez zak³óceñ. 
	 
	W trybach nie-PWM Wyjœcie komparatora moze byæ zapisane przez zapis bitu w Force Output Compare (FOC1x). 
	Jednak ten zapis nie ustawi flagi OCF1x i nie "przeladuje" timera. ale pin OC1x zostanie zmieniony (jak - o tym decyduj¹ bityCOM1x1:0)
	 
	Wszystkie zapisy do TCNT1 blokuj¹ dowolne Compare Match w nastêpnym cyklu zegara, nawet jesli timer jest zatrzymany. To pozwala OCR1x
	byc zainicjowany na ta sam¹ wartosc co TCNT1 bez wyzwalania przerwania jesli zegar jest wlaczony.
	 
	Poniewaz zapis TCNT1 blokuje Compare Match w nastepnym cyklu, jest ryzyko ¿e kiedy zmienimy TCNT1 na wartosc OCR1x, albo TOP 
	(Bottom jesli odlicza do do³u), to licznik przeskoczy nad t¹ wartoœci¹ (np. przeskoczy wartosc TOP=AAAA i dojdzie do FFFF, 
	przekreci sie i zliczy od nowa  - i bêdziemy mieli zle generowany przebieg.
	
	bity COM1x1:0 nie s¹ buforowane. Zmiana ich nastepuje od razu.
	 
	Compare Output(COM1x1:0) bity maj¹2 funkcje. Okerslaj¹ stan pinow OC1x w nastepnym porownaniu, a dwa - kontroluj a wyjscia OC1x.   
	 
	MODE 	WGM13 	WGM12 	WGM11 	WGM10 	DESCRIPTION 	 			TOP
	0 		0 	 	0  		0 		0 	 	Normal  	 				0xFFFF
	1 		0 		0 		0 		1 	 	PWM, Phase Corrected, 8bit 	0x00FF
	2 		0 		0 		1 		0 	 	PWM, Phase Corrected, 9bit 	0x01FF
	3 		0 		0 		1 		1 	 	PWM, Phase Corrected, 10bit 0x03FF 
	4 		0 		1 		0 		0 	 	CTC 	 					OCR1A
	5 		0 		1 		0 		1 	 	Fast PWM, 8bit  			0x00FF 
	6 		0 		1 		1 		0 	 	Fast PWM, 9bit  			0x01FF 
	7 		0 		1 		1 		1 	 	Fast PWM, 10bit  			0x03FF 
	8 		1 		0 		0 		0 	 	PWM, Phase and Frequency Corrected  	ICR1 
	9 		1 		0 		0 		1 	 	PWM, Phase and Frequency Corrected  	OCR1A 
	10 		1 		0 		1 		0 		PWM, Phase Correct  		ICR1 
	11 		1 		0 		1 		1 	 	PWM, Phase Correct  	 	OCR1A
	12 		1 		1		0		0		CTC 	 					ICR1
	13 		1 		1 		0 		1 	 	RESERVED 	 
	14 		1 		1 		1 		0 	 	Fast PWM  					ICR1 
	15 		1 		1 		1 		1 	 	Fast PWM 					OCR1A 
 
 W zale¿noœci od wybranego trybu licznik jest czyszczony, zwiêkszany b¹dŸ zmniejszany z ka¿dym taktem zegara
 */

public class Timer1Atmega8 extends Composite {
	private double mcuFreq=0; 	// input freq
	private double counterFreq;	// freq after prescaller
	//private double resultFreq;	// freq of invoking IRQ 	private double resultDelay;	// timer period between IRQs
	private double minFreqHz;
	private double maxDelayMs;  
	
	private Group grpTimer;		
	
	// clock source
	private Label lblClockSrc; 		// selection prescaller and it's label
	public Combo comboClockSrc;

	// clock mode
	private Label lblClockMode; 	// clock mode
	private Combo comboClockMode;
	
	// normal mode - preloaded TCNT1 value
	private Label lblClockPreload; 	// wartosc wpisywana do TCNT1 (rowniez w przerwaniu - recznie) 
	private Spinner spiTCNT1;	   	// od ktorej liczy licznik TCNT1

	// top value 
	private Label lblTopValue; 		// wartosc top dla trybu PWM 
	private Text  txtTopValue;	   	// do ktorej liczy licznik TCNT1
	
	// Timer Overflow 
	private Label lblTIMEROVFFreq;   	// wynikowa czestotliwosc (Hz) przerwan TIMER1_OVF
	private Text txtTIMEROVFFreq; 

	private Label lblTIMEROVFPeriod; // wynikowy okres (ms) przerwan TIMER1_OVF
	private Text txtTIMEROVFPeriod; 

	// OCR1A
	private Label lblOCR1A; 		// wartosc OCR1A rejestru porownajacego A
	private Spinner spiOCR1A;	   	// 
	
	private Label lblTIMERCOMPAFreq;   	// wynikowa czestotliwosc (Hz) przerwan TIMER1_COMPA
	private Text  txtTIMERCOMPAFreq; 
	
	private Label lblTIMERCOMPAPeriod;   // wynikowy okres (ms) przerwan TIMER1_COMPA
	private Text  txtTIMERCOMPAPeriod; 
	
	private Label lblOCR1ADelay;    // opoznienie (ms) przerwan TIMER1_COMPA
	private Text  txtOCR1ADelay; 

	// OCR1B
	private Label lblOCR1B; 		// wartosc OCR1B rejestru porownajacego B 
	private Spinner spiOCR1B;	   	// 
	
//	private Label lblTIMERCOMPBFreq;   	// wynikowa czestotliwosc (Hz) przerwan TIMER1_COMPB
//	private Text txtTIMERCOMPBFreq; 

	private Label lblTIMERCOMPBPeriod;   // wynikowy okres (ms) przerwan TIMER1_COMPB
	private Text txtTIMERCOMPBPeriod;
	
	private Label lblOCR1BDelay;    // opoznienie (ms) przerwan TIMER1_COMPB
	private Text  txtOCR1BDelay; 
	
	// ICR1
	private Label lblICR1; 			// wartosc rejestru porownajacego ICR1  
	private Spinner spiICR1;	   	// 

	private Label lblTIMERCAPTFreq;   	// wynikowa czestotliwosc (Hz) przerwan TIMER1_CAPT (ICR)
	private Text  txtTIMERCAPTFreq; 
	
	private Label lblTIMERCAPTPeriod;   // wynikowy okres (ms) przerwan TIMER1_CAPT
	private Text  txtTIMERCAPTPeriod; 
	
	// calculator fields
	private Label lblSelectedFreq;   	// wybrana przez nas czestotliwosc (Hz) 
	private Text txtSelectedFreq; 
	
	private Label lblSelectedDelay;   // wybrany przz nas delay (ms) 
	private Text txtSelectedDelay; 

	private Label lblPercentError;  // procentowy b³¹d 
	private Text  txtPercentError; 
	
	// button Code
	private Button btnCode;
	
	// outputs
	private Label lblOutputA;
	private Label lblOutputB;
	private Combo cmbOutA;
	private Combo cmbOutB;
	
	// other
	private Composite compos;

	//wave
	Group grpWave;
	
	public Timer1Atmega8(final Composite parent, final double mcuFreq) {
    	super(parent, SWT.NONE);

    	this.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
    	this.mcuFreq = mcuFreq;
   	
    	grpTimer = new Group(parent, SWT.SHADOW_IN);
		grpTimer.setText(" TIMER1 (16-bit) ");
		grpTimer.setLayout(new GridLayout(2,false));
		grpTimer.setBackground(SWTResourceManager.getColor(240,240,240));

		// default controls
		addCommonControls(grpTimer,0);
		
		comboClockSrc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkWidget();
				//calcAll();
			}
		}); // addSelectionListener
		
		comboClockSrc.select(1);
		comboClockMode.layout(true);
		comboClockMode.select(0);

		
//		grpWave = new Group(parent, SWT.SHADOW_IN);
//		grpWave.setText(" WAVEFORM ");
//		grpWave.setLayout(new GridLayout(1,false));
//		grpWave.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		grpWave.setBackground(SWTResourceManager.getColor(240,240,240));
//		grpWave.setBounds(0, 0, grpTimer.getSize().x, grpTimer.getSize().y);
//
//		//wave
//		TimerGraph tg = new TimerGraph(grpWave,SWT.FILL,500); // getParent() grpTimer.getSize().x
	}  //end of constructor
	
	//------------------------------------------------------------------------------------


	// dla podanego prescalera i czestotliwosci
	// zwraca parê wybranego prescaler'a oraz Delta (roznice miedzy podana czestotliwoscia, a faktyczna)
    public Pair<Integer,Double> getBestFittedTCNT1valueForNormalMode(int selectedPrescaler, double freq) {
    	double f1,f2;
    	double delta;
    	Map<Integer,Double> posSuccess = new HashMap<Integer,Double>();
    	
		f1 = mcuFreq/selectedPrescaler/65536; // najmniejsza czestotliwosc
		f2 = mcuFreq/selectedPrescaler/1;   // najwieksza czestotliwosc
		if (freq<f1 || freq>f2) {
			return new Pair<Integer, Double>(-1,1000000000.00); // jesli wybrany prescaler pozwala na generowanie tylko czestotliwosci poza zaresem - opusc z wynikiem 0.00 
		}
		//System.out.println("min (f1), max(f2)= " + f1 + ","+ f2);
    	
		delta=1000000000;
		// wybrany prescaler jednak pozwala, przelecmy przezen i tylko wartosci ktore obejmuj¹ zadan¹ czestotliwosc (freq)
		// wpiszmy do HashMap
    	for (int i=0;i<=65525;i++) {
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
		int valTCNT1=-1;
		System.out.println("size=" + posSuccess.size());
		for (Entry<Integer, Double> entry : posSuccess.entrySet()) {
		    Double valDifference = entry.getValue();
		    //System.out.println("Values :" + entry.getKey() + ", diff="+ entry.getValue());
		    if (valDifference<delta) {
		    	//System.out.println("JEST");
		    	//System.out.println("prescaler:"+ selectedPrescaler+ " freq:" + freq+ " key=" + entry.getKey() + ", valDiff=" + entry.getValue() );
		    	valTCNT1=entry.getKey();
		    	delta=valDifference; 
		    }
		}
		//System.out.println("result: " + valTCNT1 + "," + delta);
		return new Pair<Integer, Double>(valTCNT1,delta);
    	
    } // public double calc...
    
    
    
    // poszukuje podana wartosc czestotliwosci i ustawia kontrolki wg niej
    private void calcValuesByFrequencyAndSetAllControls(double freq) {
    	Pair<Integer, Double> retPair;
    	double diff = 0; // duza wartosc prescalera, przy niej blad jest zawsze mniejszy
    	int valueTCNT1 = 1000000000; 
    	int x1=-1,x2=-1, x3=-1, x4=-1, x5=-1;
    	double d1=0,d2=0,d3=0,d4=0,d5=0;
    	
    	retPair = getBestFittedTCNT1valueForNormalMode(1, freq); 
    	if (retPair.getFirst()>=0) { // cos jest
    		x1=retPair.getFirst();  
    		d1=retPair.getSecond();
    	}

    	retPair = getBestFittedTCNT1valueForNormalMode(8, freq);
    	if (retPair.getFirst()>=0) {
        	x2=retPair.getFirst();
        	d2=retPair.getSecond();
    	}
    		
    	retPair = getBestFittedTCNT1valueForNormalMode(64, freq);
    	if (retPair.getFirst()>=0) {    	
    		x3=retPair.getFirst();
    		d3=retPair.getSecond();
    	}
    	
    	retPair = getBestFittedTCNT1valueForNormalMode(256, freq);
    	if (retPair.getFirst()>=0) {    	
    		x4=retPair.getFirst();
    		d4=retPair.getSecond();
    	}
    	
    	retPair = getBestFittedTCNT1valueForNormalMode(1024, freq);
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
    	
    	valueTCNT1=-1;
    	diff=1000000000;
    	if (x5!=-1 && diff>d5) { diff=d5; valueTCNT1=x5; comboClockSrc.select(5); }
    	if (x4!=-1 && diff>d4) { diff=d4; valueTCNT1=x4; comboClockSrc.select(4); }
    	if (x3!=-1 && diff>d3) { diff=d3; valueTCNT1=x3; comboClockSrc.select(3); }
    	if (x2!=-1 && diff>d2) { diff=d2; valueTCNT1=x2; comboClockSrc.select(2); }
    	if (x1!=-1 && diff>d1) { diff=d1; valueTCNT1=x1; comboClockSrc.select(1); }
    	
    	System.out.println("Wybrano TCNT1="+ valueTCNT1 + " / diff=" + diff);
    	//System.out.println("Diff by %:" + diff*100/freq);

    	diff=diff*100/freq; // odchylenie w procentach
    	txtPercentError.setText(Double.toString(diff));
    	if (diff<0.01) txtPercentError.setBackground(SWTResourceManager.getColor(190, 255, 190));
    	if (diff>0.01 && diff<1.0) txtPercentError.setBackground(SWTResourceManager.getColor(255, 255, 190));
    	if (diff>1.0 && diff<2.0) txtPercentError.setBackground(SWTResourceManager.getColor(255, 190, 190));
    	if (diff>2.0) txtPercentError.setBackground(SWTResourceManager.getColor(255, 120, 120));
 
    	if (valueTCNT1<0) 
    		System.out.println("Value not found");
    	
    	spiTCNT1.setSelection(65535-valueTCNT1);
    	
    	// wybierz taki prescaller, który daje najmniejsza z roznic miedzy zadana a realna czestotliwoscia

//    	System.out.println("Frequency          : " + Double.toString(freq));
//    	System.out.println("Selected prescaller: " + selectedPrescaler);
//    	System.out.println("Calculated difference:"+ calcDifferenc(selectedPrescaler, freq));
    }
    
  
    // oblicza wartosci poszczegolnych pol wg ich ustawien. Wywolywane w wielu miejscach (przy zmienie wartosci pol itd). 
    private void calcAll() {
    	System.out.println("Calc all");
		if (comboClockSrc.getSelectionIndex()==0)  counterFreq=0;
		if (comboClockSrc.getSelectionIndex()==1)  counterFreq=mcuFreq;
		if (comboClockSrc.getSelectionIndex()==2)  counterFreq=mcuFreq/8;
		if (comboClockSrc.getSelectionIndex()==3)  counterFreq=mcuFreq/64;
		if (comboClockSrc.getSelectionIndex()==4)  counterFreq=mcuFreq/256;
		if (comboClockSrc.getSelectionIndex()==5)  counterFreq=mcuFreq/1024;
		if (comboClockSrc.getSelectionIndex()==6)  counterFreq=0;
		if (comboClockSrc.getSelectionIndex()==7)  counterFreq=0;
			System.out.println("Timer counter freq=" + counterFreq);

		switch (comboClockMode.getSelectionIndex()) {
		case  0 : //normal mode
			System.out.println("Calc Normal mode");
			if (counterFreq==0) {
//				txtTIMEROVF.setText("n/a");
//				txtTIMEROVFPeriod.setText("n/a");
				break;
			}
			
			if (spiTCNT1.isDisposed()) break;
			if (spiTCNT1 == null) break;
			
			double d = (65535-spiTCNT1.getSelection()+1)/counterFreq;
			txtTIMEROVFPeriod.setText(Double.toString(d*1000)); // in mS
			txtTIMEROVFFreq.setText(Double.toString(1/d)); // in Hz
			
			int s0 = (Integer)spiTCNT1.getSelection();
			int s1 = (Integer)spiOCR1A.getSelection();
			
			String jm = " mS";
			if (s1<s0) {
				txtOCR1ADelay.setText("n/a");
				cmbOutA.setEnabled(false);
			} else {
				cmbOutA.setEnabled(true);
				d=(s1-s0)*1000/counterFreq; // ms
				if (d<1) { d=d*1000; jm = " uS"; }
				if (d<1) { d=d*1000; jm = " nS"; }
				txtOCR1ADelay.setText(((Double)d).toString() + jm);
			}
			s1 = (Integer)spiOCR1B.getSelection();
			if (s1<s0) {
				txtOCR1BDelay.setText("n/a");
				cmbOutB.setEnabled(false);
			} else {
				cmbOutB.setEnabled(true);
				d=(s1-s0)*1000/counterFreq; // ms 
				if (d<1) { d=d*1000; jm = " uS"; }
				if (d<1) { d=d*1000; jm = " nS"; }
				txtOCR1BDelay.setText(((Double)d).toString() + jm);
			}
			this.redraw();
			
			break;
		case  1 : // pwm phase corr. 8bit 0xFF
			System.out.println("Calc pwm phase corr. 8bit 0xFF");
			break;
		case  2 : // pwm phase corr. 9bit 0x1FF
			System.out.println("Calc pwm phase corr. 9bit 0x1FF");
			break;
		case  3 : // pwm phase corr. 10bit 0x3FF
			System.out.println("Calc pwm phase corr. 10bit 0x3FF");
			break;
		case  4 : // CTC ORCR1A
			System.out.println("Calc CTC. OCR1A");
			txtTIMERCOMPAFreq.setText(Double.toString(counterFreq/(spiOCR1A.getSelection()+1)));
			txtTIMERCOMPAPeriod.setText(Double.toString(1000*(spiOCR1A.getSelection()+1)/counterFreq));
			break;
		case  5 : // fast PWM 8bit 0xFF
			System.out.println("Calc pwm fast 0xFF");
			break;
		case  6 : // fast PWM 9bit 0x1FF
			System.out.println("Calc pwm fast 0x1FF");
			break;
		case  7 : // fast PWM 10bit 0x3FF
			System.out.println("Calc pwm fast 0x3FF");
			break;
		case  8 : // pwm phase, freq corr ICR1 
			System.out.println("Calc pwm phase, freq corr ICR1");
			break;
		case  9 : // pwm phase, freq corr OCR1A 
			System.out.println("Calc pwm phase, freq corr OCR1A");
			break;
		case  10 : // pwm phase corr ICR1
			System.out.println("Calc pwm phase corr ICR1");
			break;
		case  11 : // pwm phase corr OCR1A 
			System.out.println("Calc pwm phase corr OCR1A");
			break;
		case  12 : // CTC ICR1
			System.out.println("Calc CTC ICR1");
			break;
		case  13 : // Fast PWM ICR1
			System.out.println("Calc fast PWM ICR1");
			break;
		case  14 : // Fast PWM OCR1A
			System.out.println("Calc fast PWM OCR1A");
			break;
		}
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
			
			//offset = doc.getLineOffset(doc.getNumberOfLines()-4);
			offset =  locateMainRow(doc);
			
			System.out.println("oFfset:" + offset);
			
			doc.replace(offset, 0, "\nvoid init_Timer1() {\n\tTCNT1=0;\n}\n");
		} catch (BadLocationException e) {
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
		e.printStackTrace();
	}
	 return pos;
	}
	
	
//	private void setOutputAsPWM() {
//		cmbOutA.removeAll();
//		cmbOutA.add("Disconnected");
//		cmbOutA.add("Non-inverted PWM");
//		cmbOutA.add("Inverted PWM");
//		cmbOutA.select(0);
//		cmbOutB.removeAll();
//		cmbOutB.add("Disconnected");
//		cmbOutB.add("Non-inverted PWM");
//		cmbOutB.add("Inverted PWM");
//		cmbOutB.select(0);
//	}
//	
//	private void setOutputAsNormal() {
//		cmbOutA.removeAll();
//		cmbOutA.add("Disconnected");
//		cmbOutA.add("Toggle on compare match");
//		cmbOutA.add("Clear on compare match");
//		cmbOutA.add("Set on compare match");
//		cmbOutA.select(0);
//		cmbOutB.removeAll();
//		cmbOutB.add("Disconnected");
//		cmbOutB.add("Toggle on compare match");
//		cmbOutB.add("Clear on compare match");
//		cmbOutB.add("Set on compare match");
//		cmbOutB.select(0);
//	}

	//----------------------------------------------------------------------------------------------------------------
	private void addCommonControls(final Group grpTimer,int selClockMode) {

	    for (Control control : grpTimer.getChildren()) {
	        control.dispose();
	    }		
		
		lblClockSrc = new Label(grpTimer, SWT.NONE);
		lblClockSrc.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblClockSrc.setText("Clock source / Prescaler :");
		
		comboClockSrc = new Combo(grpTimer, SWT.NONE);
		comboClockSrc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		comboClockSrc.setBackground(SWTResourceManager.getColor(200,255,200));
		comboClockSrc.add("Timer/Counter stopped");
		comboClockSrc.add("CLK/1 - (No prescaling)");
		comboClockSrc.add("CLK/8 - (From prescaler)");
		comboClockSrc.add("CLK/64 - (From prescaler)");
		comboClockSrc.add("CLK/256 - (From prescaler)");
		comboClockSrc.add("CLK/1024 - (From prescaler)");
		comboClockSrc.add("CLK from T1 (on faling edge)");
		comboClockSrc.add("CLK from T1 (on rising edge)");
		comboClockSrc.select(0);
		comboClockSrc.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				
				if (comboClockSrc.getSelectionIndex()==0) {
					comboClockMode.select(0);
					comboClockMode.redraw();
					comboClockMode.setEnabled(false);
				} else {
					comboClockMode.setEnabled(true);
				}
				//
			}
		});

		
		lblClockMode = new Label(grpTimer, SWT.NONE);
		lblClockMode.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblClockMode.setText("Clock/Timer Mode:");
		comboClockMode = new Combo(grpTimer, SWT.NONE);
		comboClockMode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		comboClockMode.setBackground(SWTResourceManager.getColor(200,255,200));
		comboClockMode.add("Normal Mode (TOP=0xFFFF)"); // WGM13..10 = 0000 - 
		comboClockMode.add("PWM, Phase corrected, 8bit  (TOP=0x00FF)"); // WGM13..10 = 0001 - 
		comboClockMode.add("PWM, Phase corrected, 9bit  (TOP=0x01FF)"); // WGM13..10 = 0010 - 
		comboClockMode.add("PWM, Phase corrected, 10bit (TOP=0x03FF)"); // WGM13..10 = 0011 - 
		comboClockMode.add("CTC Mode (TOP=OCR1A)"); // WGM13..10 = 0100 - 
		comboClockMode.add("Fast PWM, 8bit  (TOP=0x00FF)"); // WGM13..10 = 0101 - 
		comboClockMode.add("Fast PWM, 9bit  (TOP=0x01FF)"); // WGM13..10 = 0110 - 
		comboClockMode.add("Fast PWM, 10bit (TOP=0x03FF)"); // WGM13..10 = 0111 - 
		comboClockMode.add("PWM, Phase and freq. corrected (TOP=ICR1)"); // WGM13..10 = 1000 - 
		comboClockMode.add("PWM, Phase and freq. corrected (TOP=OCR1A)"); // WGM13..10 = 1001 - 
		comboClockMode.add("PWM, Phase corrected (TOP=ICR1)"); //WGM13..10 = 1010 - 
		comboClockMode.add("PWM, Phase corrected (TOP=OCR1A)"); // WGM13..10 = 1011 - 
		comboClockMode.add("CTC Mode (TOP=ICR1)"); // WGM13..10 = 1100 - 
					//comboClockMode.add("WGM13..10 = 1101 --- RESERVED (Do not use!) ---");
		comboClockMode.add("Fast PWM (TOP=ICR1)"); // WGM13..10 = 1110 - 
		comboClockMode.add("Fast PWM (TOP=OCR1A)"); //WGM13..10 = 1111 - 
		comboClockMode.select(selClockMode);
		comboClockMode.layout(true);
		comboClockMode.redraw();
		if (selClockMode==0) {
			comboClockMode.setEnabled(false);
		} else {
			comboClockMode.setEnabled(true);
		}
		comboClockMode.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Combo cmb =(Combo) e.getSource();
				Composite parent = ((Composite) e.getSource()).getParent();
				int s = cmb.getSelectionIndex(); 
				int store_clock_src = comboClockSrc.getSelectionIndex();
				
				switch (s) {
				case 0 : // WGM13..10 = 0000 - Normal   (TOP=0xFFFF)
					setNormalMode(grpTimer);
					break;
				case 1 : // WGM13..10 = 0001 - PWM, Phase Corrected, 8bit   (TOP=0x00FF)
					setPhaseCorrectedFixedMode(grpTimer,0xFF);
					break;
				case 2 : // WGM13..10 = 0010 - PWM, Phase Corrected, 9bit   (TOP=0x01FF)
					setPhaseCorrectedFixedMode(grpTimer,0x1FF);
					break;
				case 3 : // WGM13..10 = 0011 - PWM, Phase Corrected, 10bit   (TOP=0x03FF)
					setPhaseCorrectedFixedMode(grpTimer,0x3FF);
					break;
				case 4 : // WGM13..10 = 0100 - CTC   (TOP=OCR1A)
					setCTCModeOCR1A(grpTimer);					
					break;
				case 5 : // WGM13..10 = 0101 - Fast PWM, 8bit   (TOP=0x00FF)
					setFastPWMFixedMode(grpTimer,0xFF);
					break;
				case 6 : // WGM13..10 = 0110 - Fast PWM, 9bit   (TOP=0x01FF)
					setFastPWMFixedMode(grpTimer,0x1FF);
					break;
				case 7 : // WGM13..10 = 0111 - Fast PWM, 10bit   (TOP=0x03FF)
					setFastPWMFixedMode(grpTimer,0x3FF);
					break;
				case 8 : // WGM13..10 = 1000 - PWM, Phase and Frequency Corrected (TOP=ICR1)
					setPhaseFreqCorrectedICR1(grpTimer,8);
					break;
				case 9 : // WGM13..10 = 1001 - PWM, Phase and Frequency Corrected (TOP=OCR1A)
					setPhaseFreqCorrectedOCR1A(grpTimer,9);
					break;
				case 10 : // WGM13..10 = 1010 - PWM, Phase Correct   (TOP=ICR1)
					setPhaseCorrectedICR1Mode(grpTimer, 10);
					break;
				case 11 : // WGM13..10 = 1011 - PWM, Phase Correct   (TOP=OCR1A)
					setPhaseCorrectedOCR1AMode(grpTimer, 11);
					break;
				case 12 : // WGM13..10 = 1100 - CTC   (TOP=ICR1)
					setCTCModeICR1(grpTimer);
					break;
				case 13 : // WGM13..10 = 1110 - Fast PWM   (TOP=ICR1)
					setFastPWMICR1Mode(grpTimer, 13);
					break;
				case 14 : // WGM13..10 = 1111 - Fast PWM   (TOP=OCR1A)
					setFastPWMOCR1AMode(grpTimer, 14);
					break;
				default: 
					throw new RuntimeException("ERROR in comboClockMode.addModifyListener");
				}
				grpTimer.getParent().layout(true);
				grpTimer.layout(true);
				parent.layout(true);
				
				grpTimer.redraw();
				grpTimer.getParent().redraw();
				parent.redraw();

				//restore 
				comboClockSrc.select(store_clock_src);
				checkWidget();
				calcAll();
			}
		}); // ModifyListener...
		comboClockMode.layout(true);
		checkWidget();
	} // private void addCommonControls(...
	
	//--------------------------------------------------------------------------------------------
	// ^
	// |          +------------------------------+
	// |          |                              |
	// |          |                              |
	// +----------x---------a-------b------------+
	//          TCNT1     OCR1A    OCR1B        TIMER_OVF (0xFFFF)
	
	private void setNormalMode(Group comp) {
		addCommonControls(comp,0);

		//---------------------------------------------------------------------------------------------
		// wartoœæ TCNT od ktorej zaczynamy liczenie
		lblClockPreload = new Label(comp, SWT.NONE);
		lblClockPreload.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblClockPreload.setText("TCNT1 preload value:");
		spiTCNT1 = new Spinner(comp, SWT.BORDER);
		spiTCNT1.setMaximum(65535);
		spiTCNT1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiTCNT1.setBackground(SWTResourceManager.getColor(255,255,190));
		spiTCNT1.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				if (spiTCNT1.getSelection()>65535) spiTCNT1.setSelection(0);
				checkWidget();
				calcAll();
			}
		});
		// ------------------------------------------------
		// w rezultacie otrzymujemy czestotliwosc ... 
		lblTIMEROVFFreq = new Label(comp, SWT.NONE);
		lblTIMEROVFFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMEROVFFreq.setText("TIMER1_OVF freq. (Hz) :");
		txtTIMEROVFFreq = new Text(comp,SWT.BORDER);
		txtTIMEROVFFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMEROVFFreq.setText("0.00");
		txtTIMEROVFFreq.setEditable(false);
		//----------------------------------------------------------------------
		// i okres czestotliwosc ... 
		lblTIMEROVFPeriod = new Label(comp, SWT.NONE);
		lblTIMEROVFPeriod.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMEROVFPeriod.setText("TIMER1_OVF period (ms) :");
		txtTIMEROVFPeriod = new Text(comp,SWT.BORDER);
		txtTIMEROVFPeriod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMEROVFPeriod.setText("0.00");
		txtTIMEROVFPeriod.setEditable(false);		

		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1A (jeœli u¿ywana)
		lblOCR1A = new Label(grpTimer, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value :");
		spiOCR1A = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1A.setMaximum(65535);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1A.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();
				calcAll();
			}
		});
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1B (jeœli uzywana)
		lblOCR1B = new Label(grpTimer, SWT.NONE);
		lblOCR1B.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1B.setText("OCR1B value:");
		spiOCR1B = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1B.setMaximum(65535);
		spiOCR1B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1B.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();				
				calcAll();
			}
		});
		
		// ------------------------------------------------
		// przesuniecie OCR1A wzgledem preloaded TCNT1   
		lblOCR1ADelay = new Label(grpTimer, SWT.NONE);
		lblOCR1ADelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1ADelay.setText("TIMER1_COMPA delay after TCNT1 (ms) :");
		txtOCR1ADelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1ADelay .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1ADelay .setText("0.00");
		txtOCR1ADelay .setEditable(false);	
		//----------------------------------------------------------------------
		// przesuniecie OCR1B wzgledem preloaded TCNT1
		lblOCR1BDelay = new Label(grpTimer, SWT.NONE);
		lblOCR1BDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1BDelay.setText("TIMER1_COMPB delay after TCNT1 (ms) :");
		txtOCR1BDelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1BDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1BDelay.setText("0.00");
		txtOCR1BDelay.setEditable(false);

		lblOutputA = new Label(grpTimer, SWT.NONE);
		lblOutputA.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputA.setText("Output A mode:");
		cmbOutA = new Combo(grpTimer, SWT.None);
		cmbOutA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutA.add("Disconnected");
		cmbOutA.add("Toggle on compare match");
		cmbOutA.add("Clear on compare match");
		cmbOutA.add("Set on compare match");
		cmbOutA.select(0);

		lblOutputB = new Label(grpTimer, SWT.NONE);
		lblOutputB.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputB.setText("Output B mode:");
		cmbOutB = new Combo(grpTimer, SWT.None);
		cmbOutB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutB.add("Disconnected");
		cmbOutB.add("Toggle on compare match");
		cmbOutB.add("Clear on compare match");
		cmbOutB.add("Set on compare match");
		cmbOutB.select(0);

		comp.redraw();
		
		minFreqHz = mcuFreq / 1024 / 65536;
		maxDelayMs = 1000 / (mcuFreq / 1024 / 65536);  
		//---------------------------------------------------------------------------
		// wybrana czestotliwosc w Hz
		lblSelectedFreq = new Label(grpTimer, SWT.NONE);
		lblSelectedFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
		lblSelectedFreq.setText("Selected freq. (>" + Double.toString(minFreqHz) + ") (Hz) :");
		lblSelectedFreq.setBackground(SWTResourceManager.getColor(255, 255, 255));

		
		txtSelectedFreq = new Text(grpTimer,SWT.BORDER);
		txtSelectedFreq.setBackground(SWTResourceManager.getColor(255,255,190));
		txtSelectedFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtSelectedFreq.setText("0.00");
		txtSelectedFreq.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					double d = 1000 / Double.parseDouble(txtSelectedFreq.getText());
					txtSelectedDelay.setText(Double.toString(d));
					double minimalFrequencyHz = mcuFreq / 1024 / 65536;
					double maxDelayMs = 1000 / (mcuFreq / 1024 / 65536);
					if (Double.parseDouble(txtSelectedFreq.getText()) < minimalFrequencyHz) { 
						txtSelectedFreq.setBackground(SWTResourceManager.getColor(255, 190, 190));} 
					else {  
						txtSelectedFreq.setBackground(SWTResourceManager.getColor(255, 255, 190)); }

					if (Double.parseDouble(txtSelectedDelay.getText()) > maxDelayMs) {
						txtSelectedDelay.setBackground(SWTResourceManager.getColor(255, 190, 190)); }
					else {
						txtSelectedDelay.setBackground(SWTResourceManager.getColor(255, 255, 190)); }
					
					calcValuesByFrequencyAndSetAllControls(Double.parseDouble(txtSelectedFreq.getText()));
					txtSelectedDelay.redraw();
					txtSelectedFreq.redraw();
				} catch (Exception e2) {
				}
			}
			
		});
		//-------------------------------------------------------------------------------
		lblSelectedDelay = new Label(grpTimer, SWT.NONE);
		lblSelectedDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblSelectedDelay.setText("Selected period (< "+ Double.toString(maxDelayMs) +") (ms) :");
		lblSelectedDelay.setBackground(SWTResourceManager.getColor(255, 255, 255));
		txtSelectedDelay = new Text(grpTimer,SWT.BORDER);
		txtSelectedDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtSelectedDelay.setText("0.00");
		txtSelectedDelay.setBackground(SWTResourceManager.getColor(255,255,190));
		
		txtSelectedDelay.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					double d = 1000/ (Double.parseDouble(txtSelectedDelay.getText()));
					txtSelectedFreq.setText(Double.toString(d));
					double minimalFrequencyHz = mcuFreq / 1024 / 65536;
					double maxDelayMs = 1000 / (mcuFreq / 1024 / 65536);

					if (Double.parseDouble(txtSelectedFreq.getText()) < minimalFrequencyHz) {
						txtSelectedFreq.setBackground(SWTResourceManager.getColor(255, 190, 190));
					} else {
						txtSelectedFreq.setBackground(SWTResourceManager.getColor(255, 255, 190));
					}

					if (Double.parseDouble(txtSelectedDelay.getText()) > maxDelayMs) {
						txtSelectedDelay.setBackground(SWTResourceManager.getColor(255, 190, 190));
					} else {
						txtSelectedDelay.setBackground(SWTResourceManager.getColor(255, 255, 190));
					}
					calcValuesByFrequencyAndSetAllControls(Double.parseDouble(txtSelectedFreq.getText()));
					txtSelectedDelay.redraw();
					txtSelectedFreq.redraw();
				} catch (Exception e2) {
				}
			}
			
		});

		lblPercentError = new Label(grpTimer, SWT.NONE);
		lblPercentError.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblPercentError.setText("Percent of error (%) :");
		lblPercentError.setBackground(SWTResourceManager.getColor(255, 255, 255));
		txtPercentError = new Text(grpTimer,SWT.BORDER);
		txtPercentError.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtPercentError.setText("0.00");
		txtPercentError.setBackground(SWTResourceManager.getColor(190,190,190));
		txtPercentError.setEditable(false);
		
		
		
	}
	//-------------------------------------------------------------------------------------------------
	private void setCTCModeOCR1A(Group comp) {
		addCommonControls(comp,4);
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1A definiujaca TOP dla trybu CTC
		lblOCR1A = new Label(comp, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value (TOP):");
		spiOCR1A = new Spinner(comp, SWT.BORDER);
		spiOCR1A.setMaximum(65535);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		spiOCR1A.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();				
				calcAll();
			}
		});
		
		// ------------------------------------------------
		// w rezultacie otrzymujemy czestotliwosc CTC...  
		lblTIMERCOMPAFreq = new Label(grpTimer, SWT.NONE);
		lblTIMERCOMPAFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMERCOMPAFreq.setText("TIMER1_COMPA freq. (Hz) :");
		txtTIMERCOMPAFreq = new Text(grpTimer,SWT.BORDER);
		txtTIMERCOMPAFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMERCOMPAFreq.setText("0.00");
		txtTIMERCOMPAFreq.setEditable(false);
		// i okres czestotliwosc ... 
		lblTIMERCOMPAPeriod = new Label(comp, SWT.NONE);
		lblTIMERCOMPAPeriod.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMERCOMPAPeriod.setText("TIMER1_COMPA period (ms) :");
		txtTIMERCOMPAPeriod = new Text(comp,SWT.BORDER);
		txtTIMERCOMPAPeriod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMERCOMPAPeriod.setText("0.00");
		txtTIMERCOMPAPeriod.setEditable(false);		
		//------------------------------------------------------------------------
		lblOutputA = new Label(grpTimer,SWT.None);
		lblOutputA.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputA.setText("Output A mode:");
		cmbOutA = new Combo(grpTimer, SWT.NONE);
		cmbOutA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutA.add("Disconnected");
		cmbOutA.add("Toggle on compare match");
		cmbOutA.add("Clear on compare match");
		cmbOutA.add("Set on compare match");
		cmbOutA.select(0);
		
		// OCR1B moze byc uzywane, ale nie musi
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1B (jeœli uzywana)
		lblOCR1B = new Label(grpTimer, SWT.NONE);
		lblOCR1B.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1B.setText("OCR1B value (PWM):");
		spiOCR1B = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1B.setMaximum(65535);
		spiOCR1B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1B.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();				
				calcAll();
			}
		});
		//----------------------------------------------------------------------
		// obliczone przesuniecie TIMER_COMPB wzglêdem 0x0000 (jesli miesci sie w zakresie)  
		lblTIMERCOMPBPeriod = new Label(grpTimer, SWT.NONE);
		lblTIMERCOMPBPeriod.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMERCOMPBPeriod.setText("TIMER1_COMPB delay from START (ms) :");
		txtTIMERCOMPBPeriod = new Text(grpTimer,SWT.BORDER);
		txtTIMERCOMPBPeriod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMERCOMPBPeriod.setText("0.00");
		txtTIMERCOMPBPeriod.setEditable(false);	
		// outputB
		lblOutputB = new Label(grpTimer,SWT.None);
		lblOutputB.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputB.setText("Output B mode:");
		cmbOutB = new Combo(grpTimer, SWT.NONE);
		cmbOutB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutB.add("Disconnected");
		cmbOutB.add("Toggle on compare match");
		cmbOutB.add("Clear on compare match");
		cmbOutB.add("Set on compare match");
		cmbOutB.select(0);
		
		//---------------------------------------------------------------------------
		// wybrana czestotliwosc w Hz
		minFreqHz = mcuFreq / 1024 / 65536;
		maxDelayMs = 1000 / (mcuFreq / 1024 / 65536);  
		
		lblSelectedFreq = new Label(grpTimer, SWT.NONE);
		lblSelectedFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
		lblSelectedFreq.setText("Selected freq. (>" + Double.toString(minFreqHz) + ") (Hz) :");
		lblSelectedFreq.setBackground(SWTResourceManager.getColor(255, 255, 255));

		
		txtSelectedFreq = new Text(grpTimer,SWT.BORDER);
		txtSelectedFreq.setBackground(SWTResourceManager.getColor(255,255,190));
		txtSelectedFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtSelectedFreq.setText("0.00");
		txtSelectedFreq.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					double d = 1000 / Double.parseDouble(txtSelectedFreq.getText());
					txtSelectedDelay.setText(Double.toString(d));
					double minimalFrequencyHz = mcuFreq / 1024 / 65536;
					double maxDelayMs = 1000 / (mcuFreq / 1024 / 65536);
					if (Double.parseDouble(txtSelectedFreq.getText()) < minimalFrequencyHz) { 
						txtSelectedFreq.setBackground(SWTResourceManager.getColor(255, 190, 190));} 
					else {  
						txtSelectedFreq.setBackground(SWTResourceManager.getColor(255, 255, 190)); }

					if (Double.parseDouble(txtSelectedDelay.getText()) > maxDelayMs) {
						txtSelectedDelay.setBackground(SWTResourceManager.getColor(255, 190, 190)); }
					else {
						txtSelectedDelay.setBackground(SWTResourceManager.getColor(255, 255, 190)); }
					
					calcValuesByFrequencyAndSetAllControls(Double.parseDouble(txtSelectedFreq.getText()));
					txtSelectedDelay.redraw();
					txtSelectedFreq.redraw();
				} catch (Exception e2) {
				}
			}
			
		});
		//-------------------------------------------------------------------------------
		lblSelectedDelay = new Label(grpTimer, SWT.NONE);
		lblSelectedDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblSelectedDelay.setText("Selected period (< "+ Double.toString(maxDelayMs) +") (ms) :");
		lblSelectedDelay.setBackground(SWTResourceManager.getColor(255, 255, 255));
		txtSelectedDelay = new Text(grpTimer,SWT.BORDER);
		txtSelectedDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtSelectedDelay.setText("0.00");
		txtSelectedDelay.setBackground(SWTResourceManager.getColor(255,255,190));
		
		txtSelectedDelay.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					double d = 1000/ (Double.parseDouble(txtSelectedDelay.getText()));
					txtSelectedFreq.setText(Double.toString(d));
					double minimalFrequencyHz = mcuFreq / 1024 / 65536;
					double maxDelayMs = 1000 / (mcuFreq / 1024 / 65536);

					if (Double.parseDouble(txtSelectedFreq.getText()) < minimalFrequencyHz) {
						txtSelectedFreq.setBackground(SWTResourceManager.getColor(255, 190, 190));
					} else {
						txtSelectedFreq.setBackground(SWTResourceManager.getColor(255, 255, 190));
					}

					if (Double.parseDouble(txtSelectedDelay.getText()) > maxDelayMs) {
						txtSelectedDelay.setBackground(SWTResourceManager.getColor(255, 190, 190));
					} else {
						txtSelectedDelay.setBackground(SWTResourceManager.getColor(255, 255, 190));
					}
					calcValuesByFrequencyAndSetAllControls(Double.parseDouble(txtSelectedFreq.getText()));
					txtSelectedDelay.redraw();
					txtSelectedFreq.redraw();
				} catch (Exception e2) {
				}
			}
			
		});

		lblPercentError = new Label(grpTimer, SWT.NONE);
		lblPercentError.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblPercentError.setText("Percent of error (%) :");
		lblPercentError.setBackground(SWTResourceManager.getColor(255, 255, 255));
		txtPercentError = new Text(grpTimer,SWT.BORDER);
		txtPercentError.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtPercentError.setText("0.00");
		txtPercentError.setBackground(SWTResourceManager.getColor(190,190,190));
		txtPercentError.setEditable(false);		
		
		comp.redraw();
	}
	//-----------------------------------------------------------------------------------------------------------------------------------
	private void setCTCModeICR1(Group comp) {
		addCommonControls(comp,12);

		//---------------------------------------------------------------------------------------------
		// wartoœæ ICR1 definiujaca TOP dla trybu CTC
		lblICR1 = new Label(comp, SWT.NONE);
		lblICR1.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblICR1.setText("ICR1 value (TOP):");
		spiICR1 = new Spinner(comp, SWT.BORDER);
		spiICR1.setMaximum(65535);
		spiICR1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		// ------------------------------------------------
		// TIMOVF nie jest generowany, bo nie ma przepe³nienia licznika. Zamiast tego jest COMPA (od ICR1)
		// w rezultacie otrzymujemy czestotliwosc CTC... 
		lblTIMERCAPTFreq = new Label(grpTimer, SWT.NONE);
		lblTIMERCAPTFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMERCAPTFreq.setText("TIMER1_CAPT freq. (Hz) :");
		txtTIMERCAPTFreq = new Text(grpTimer,SWT.BORDER);
		txtTIMERCAPTFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMERCAPTFreq.setText("0.00");
		txtTIMERCAPTFreq.setEditable(false);
		// i period 
		lblTIMERCAPTPeriod = new Label(grpTimer, SWT.NONE);
		lblTIMERCAPTPeriod.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMERCAPTPeriod.setText("TIMER1_CAPT period (Hz) :");
		txtTIMERCAPTPeriod = new Text(grpTimer,SWT.BORDER);
		txtTIMERCAPTPeriod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMERCAPTPeriod.setText("0.00");
		txtTIMERCAPTPeriod.setEditable(false);		
		
		// ------------------------------------------------		
		// OCR1A mozemy uzywac 
		lblOCR1A = new Label(comp, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value (PWM):");
		spiOCR1A = new Spinner(comp, SWT.BORDER);
		spiOCR1A.setMaximum(65535);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		// i okres ... 
		lblTIMERCOMPAPeriod = new Label(grpTimer, SWT.NONE);
		lblTIMERCOMPAPeriod.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMERCOMPAPeriod.setText("TIMER1_COMPA delay from START (ms) :");
		txtTIMERCOMPAPeriod = new Text(grpTimer,SWT.BORDER);
		txtTIMERCOMPAPeriod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMERCOMPAPeriod.setText("0.00");
		txtTIMERCOMPAPeriod.setEditable(false);
		// outputA
		lblOutputA = new Label(grpTimer,SWT.None);
		lblOutputA.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputA.setText("Output A mode:");
		cmbOutA = new Combo(grpTimer, SWT.NONE);
		cmbOutA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutA.add("Disconnected");
		cmbOutA.add("Toggle on compare match");
		cmbOutA.add("Clear on compare match");
		cmbOutA.add("Set on compare match");
		cmbOutA.select(0);

		// ------------------------------------------------		
		// OCR1B te¿ mozemy uzywac
		lblOCR1B = new Label(comp, SWT.NONE);
		lblOCR1B.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1B.setText("OCR1B value (PWM):");
		spiOCR1B = new Spinner(comp, SWT.BORDER);
		spiOCR1B.setMaximum(65535);
		spiOCR1B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		// i okres ... 
		lblTIMERCOMPBPeriod = new Label(grpTimer, SWT.NONE);
		lblTIMERCOMPBPeriod.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMERCOMPBPeriod.setText("TIMER1_COMPB delay from START (ms) :");
		txtTIMERCOMPBPeriod = new Text(grpTimer,SWT.BORDER);
		txtTIMERCOMPBPeriod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMERCOMPBPeriod.setText("0.00");
		txtTIMERCOMPBPeriod.setEditable(false);	
		// outputB
		lblOutputB = new Label(grpTimer,SWT.None);
		lblOutputB.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputB.setText("Output B mode:");
		cmbOutB = new Combo(grpTimer, SWT.NONE);
		cmbOutB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutB.add("Disconnected");
		cmbOutB.add("Toggle on compare match");
		cmbOutB.add("Clear on compare match");
		cmbOutB.add("Set on compare match");
		cmbOutB.select(0);

		comp.redraw();
	}
	//------------------------------------------------------------------------------------------------------
	// set phase corrected PWM - OCR1A 
	private void setPhaseCorrectedOCR1AMode(Group comp, Integer top) {
		addCommonControls(comp, 11);
		
		//---------------------------------------------------------------------------------------------		
		// TOP VALUE
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1A definiujaca TOP dla trybu PWM Phase Corrected
		lblOCR1A = new Label(comp, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A (PWM TOP value):");
		spiOCR1A = new Spinner(comp, SWT.BORDER);
		spiOCR1A.setMaximum(65535);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		//---------------------------------------------------------------------------------------------		
		// czestotliwosc PWM
		lblTIMEROVFFreq = new Label(grpTimer, SWT.NONE);
		lblTIMEROVFFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMEROVFFreq.setText("PWM frequency (Hz) :");
		txtTIMEROVFFreq = new Text(grpTimer,SWT.BORDER);
		txtTIMEROVFFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMEROVFFreq.setText("0.00");
		txtTIMEROVFFreq.setEditable(false);
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1A (OCR1A...TOP...OCR1A)
		lblOCR1A = new Label(grpTimer, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value (PWM):");
		spiOCR1A = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1A.setMaximum(top);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1A.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();
				calcAll();
			}
		});
		
		// przesuniecie OCR1A wzgledem poczatku
		lblOCR1ADelay = new Label(grpTimer, SWT.NONE);
		lblOCR1ADelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1ADelay.setText("TIMER1_COMPA period (ms) :");
		txtOCR1ADelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1ADelay .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1ADelay .setText("0.00");
		txtOCR1ADelay .setEditable(false);	
		//
		lblOutputA = new Label(grpTimer, SWT.NONE);
		lblOutputA.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputA.setText("Output A mode:");
		cmbOutA = new Combo(grpTimer, SWT.None);
		cmbOutA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutA.add("Disconnected");
		cmbOutA.add("Toggle on compare match (/ and \\)");
		cmbOutA.add("Clear on / compare match, set on \\ comp");
		cmbOutA.add("Set on / compare match, clear on \\ comp");
		cmbOutA.select(0);
		
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1B (0...TOP)
		lblOCR1B = new Label(grpTimer, SWT.NONE);
		lblOCR1B.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1B.setText("OCR1B value (PWM):");
		spiOCR1B = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1B.setMaximum(top);
		spiOCR1B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1B.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();				
				calcAll();
			}
		});
		// przesuniecie OCR1B wzgledem preloaded poczatku
		lblOCR1BDelay = new Label(grpTimer, SWT.NONE);
		lblOCR1BDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1BDelay.setText("TIMER1_COMPB period TCNT1 (ms) :");
		txtOCR1BDelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1BDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1BDelay.setText("0.00");
		txtOCR1BDelay.setEditable(false);		
		//
		lblOutputB = new Label(grpTimer, SWT.NONE);
		lblOutputB.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputB.setText("Output B mode:");
		cmbOutB = new Combo(grpTimer, SWT.None);
		cmbOutB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutB.add("Disconnected");
		cmbOutB.add("Disconnected");
		cmbOutB.add("Clear on / compare match, set on \\");
		cmbOutB.add("Set on / compare match, clear on \\");
		cmbOutB.select(0);			
		
	}
	//------------------------------------------------------------------------------------------------------
	// set phase corrected PWM - ICR1
	private void setPhaseCorrectedICR1Mode(Group comp, Integer top) {
		addCommonControls(comp,10);

		//---------------------------------------------------------------------------------------------		
		// TOP VALUE
		//---------------------------------------------------------------------------------------------
		// wartoœæ ICR1 definiujaca TOP dla trybu PWM Phase Corrected
		lblICR1 = new Label(comp, SWT.NONE);
		lblICR1.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblICR1.setText("ICR1 (PWM TOP value):");
		spiICR1 = new Spinner(comp, SWT.BORDER);
		spiICR1.setMaximum(65535);
		spiICR1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		//---------------------------------------------------------------------------------------------		
		// czestotliwosc PWM
		lblTIMEROVFFreq = new Label(grpTimer, SWT.NONE);
		lblTIMEROVFFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMEROVFFreq.setText("PWM frequency (Hz) :");
		txtTIMEROVFFreq = new Text(grpTimer,SWT.BORDER);
		txtTIMEROVFFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMEROVFFreq.setText("0.00");
		txtTIMEROVFFreq.setEditable(false);
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1A (OCR1A...TOP...OCR1A)
		lblOCR1A = new Label(grpTimer, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value (PWM):");
		spiOCR1A = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1A.setMaximum(top);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1A.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();
				calcAll();
			}
		});
		
		// przesuniecie OCR1A wzgledem poczatku
		lblOCR1ADelay = new Label(grpTimer, SWT.NONE);
		lblOCR1ADelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1ADelay.setText("TIMER1_COMPA period (ms) :");
		txtOCR1ADelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1ADelay .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1ADelay .setText("0.00");
		txtOCR1ADelay .setEditable(false);	
		//
		lblOutputA = new Label(grpTimer, SWT.NONE);
		lblOutputA.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputA.setText("Output A mode:");
		cmbOutA = new Combo(grpTimer, SWT.None);
		cmbOutA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutA.add("Disconnected");
		cmbOutA.add("Toggle on compare match (/ and \\)");
		cmbOutA.add("Clear on / compare match, set on \\ comp");
		cmbOutA.add("Set on / compare match, clear on \\ comp");
		cmbOutA.select(0);
		
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1B (0...TOP)
		lblOCR1B = new Label(grpTimer, SWT.NONE);
		lblOCR1B.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1B.setText("OCR1B value (PWM):");
		spiOCR1B = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1B.setMaximum(top);
		spiOCR1B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1B.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();				
				calcAll();
			}
		});
		// przesuniecie OCR1B wzgledem preloaded poczatku
		lblOCR1BDelay = new Label(grpTimer, SWT.NONE);
		lblOCR1BDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1BDelay.setText("TIMER1_COMPB delay after TCNT1 (ms) :");
		txtOCR1BDelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1BDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1BDelay.setText("0.00");
		txtOCR1BDelay.setEditable(false);		
		//
		lblOutputB = new Label(grpTimer, SWT.NONE);
		lblOutputB.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputB.setText("Output B mode:");
		cmbOutB = new Combo(grpTimer, SWT.None);
		cmbOutB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutB.add("Disconnected");
		cmbOutB.add("Disconnected");
		cmbOutB.add("Clear on / compare match, set on \\");
		cmbOutB.add("Set on / compare match, clear on \\");
		cmbOutB.select(0);			
		
	}
	//------------------------------------------------------------------------------------------------------
	// set phase corrected PWM 
	private void setPhaseCorrectedFixedMode(Group comp, Integer top) {
		if (top==0xFF) {
			addCommonControls(comp,1);
		}
		if (top==0x1FF) {
			addCommonControls(comp,2);
		}
		if (top==0x3FF) {
			addCommonControls(comp,3);
		}
		//---------------------------------------------------------------------------------------------		
		// TOP VALUE
		lblTopValue = new Label(grpTimer, SWT.NONE);
		lblTopValue.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTopValue.setText("PWM top value (max) :");
		txtTopValue = new Text(grpTimer,SWT.BORDER);
		txtTopValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTopValue.setText(Integer.toString(top));
		txtTopValue.setEditable(false);	
		
		//---------------------------------------------------------------------------------------------		
		// czestotliwosc PWM
		lblTIMEROVFFreq = new Label(grpTimer, SWT.NONE);
		lblTIMEROVFFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMEROVFFreq.setText("PWM frequency (Hz) :");
		txtTIMEROVFFreq = new Text(grpTimer,SWT.BORDER);
		txtTIMEROVFFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMEROVFFreq.setText("0.00");
		txtTIMEROVFFreq.setEditable(false);
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1A (OCR1A...TOP...OCR1A)
		lblOCR1A = new Label(grpTimer, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value (PWM):");
		spiOCR1A = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1A.setMaximum(top);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1A.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();
				calcAll();
			}
		});
		
		// przesuniecie OCR1A wzgledem poczatku
		lblOCR1ADelay = new Label(grpTimer, SWT.NONE);
		lblOCR1ADelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1ADelay.setText("TIMER1_COMPA period (ms) :");
		txtOCR1ADelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1ADelay .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1ADelay .setText("0.00");
		txtOCR1ADelay .setEditable(false);	
		//
		lblOutputA = new Label(grpTimer, SWT.NONE);
		lblOutputA.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputA.setText("Output A mode:");
		cmbOutA = new Combo(grpTimer, SWT.None);
		cmbOutA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutA.add("Disconnected");
		cmbOutA.add("Toggle on compare match (/ and \\)");
		cmbOutA.add("Clear on / compare match, set on \\ comp");
		cmbOutA.add("Set on / compare match, clear on \\ comp");
		cmbOutA.select(0);
		
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1B (0...TOP)
		lblOCR1B = new Label(grpTimer, SWT.NONE);
		lblOCR1B.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1B.setText("OCR1B value (PWM):");
		spiOCR1B = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1B.setMaximum(top);
		spiOCR1B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1B.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();				
				calcAll();
			}
		});
		// przesuniecie OCR1B wzgledem preloaded poczatku
		lblOCR1BDelay = new Label(grpTimer, SWT.NONE);
		lblOCR1BDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1BDelay.setText("TIMER1_COMPB delay after TCNT1 (ms) :");
		txtOCR1BDelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1BDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1BDelay.setText("0.00");
		txtOCR1BDelay.setEditable(false);		
		//
		lblOutputB = new Label(grpTimer, SWT.NONE);
		lblOutputB.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputB.setText("Output B mode:");
		cmbOutB = new Combo(grpTimer, SWT.None);
		cmbOutB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutB.add("Disconnected");
		cmbOutB.add("Disconnected");
		cmbOutB.add("Clear on / compare match, set on \\");
		cmbOutB.add("Set on / compare match, clear on \\");
		cmbOutB.select(0);		
	}
	//------------------------------------------------------------------------------------------------------
	// set phase and freq corrected PWM - ICR1
	private void setPhaseFreqCorrectedICR1(Group comp,Integer top) {
		addCommonControls(comp,8);
		//---------------------------------------------------------------------------------------------
		// wartoœæ ICR1 definiujaca TOP dla trybu PWM Phase Corrected
		lblICR1 = new Label(comp, SWT.NONE);
		lblICR1.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblICR1.setText("ICR1 value (TOP):");
		spiICR1 = new Spinner(comp, SWT.BORDER);
		spiICR1.setMaximum(65535);
		spiICR1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
	}
	//------------------------------------------------------------------------------------------------------
	// set phase and freq corrected PWM - OCR1A
	private void setPhaseFreqCorrectedOCR1A(Group comp, Integer top) {
		addCommonControls(comp,9);
		//---------------------------------------------------------------------------------------------		
		// wartoœæ OCR1A definiujaca TOP dla tego trybu  
		lblOCR1A = new Label(comp, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value (TOP):");
		spiOCR1A = new Spinner(comp, SWT.BORDER);
		spiOCR1A.setMaximum(65535);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
	}
	//------------------------------------------------------------------------------------------------------
	// set fast PWM - ICR1
	private void setFastPWMICR1Mode(Group comp, Integer top) {
		addCommonControls(comp,13);
		//---------------------------------------------------------------------------------------------
		// wartoœæ ICR1 definiujaca TOP dla trybu PWM Phase Corrected
		lblICR1 = new Label(comp, SWT.NONE);
		lblICR1.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblICR1.setText("ICR1 value (TOP):");
		spiICR1 = new Spinner(comp, SWT.BORDER);
		spiICR1.setMaximum(65535);
		spiICR1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		// czestotliwosc PWM
		lblTIMERCAPTFreq = new Label(grpTimer, SWT.NONE);
		lblTIMERCAPTFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMERCAPTFreq.setText("PWM frequency (Hz) :");
		txtTIMERCAPTFreq = new Text(grpTimer,SWT.BORDER);
		txtTIMERCAPTFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMERCAPTFreq.setText("0.00");
		txtTIMERCAPTFreq.setEditable(false);	

		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1A (0...TOP)
		lblOCR1A = new Label(grpTimer, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value (PWM):");
		spiOCR1A = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1A.setMaximum(top);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1A.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();
				calcAll();
			}
		});
		
		// przesuniecie OCR1A wzgledem poczatku
		lblOCR1ADelay = new Label(grpTimer, SWT.NONE);
		lblOCR1ADelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1ADelay.setText("TIMER1_COMPA delay (ms) :");
		txtOCR1ADelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1ADelay .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1ADelay .setText("0.00");
		txtOCR1ADelay .setEditable(false);	
		//
		lblOutputA = new Label(grpTimer, SWT.NONE);
		lblOutputA.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputA.setText("Output A mode:");
		cmbOutA = new Combo(grpTimer, SWT.None);
		cmbOutA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutA.add("Disconnected");
		cmbOutA.add("Toggle on compare match");
		cmbOutA.add("Clear on compare match");
		cmbOutA.add("Set on compare match");
		cmbOutA.select(0);
		
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1B (0...TOP)
		lblOCR1B = new Label(grpTimer, SWT.NONE);
		lblOCR1B.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1B.setText("OCR1B value (PWM):");
		spiOCR1B = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1B.setMaximum(top);
		spiOCR1B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1B.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();				
				calcAll();
			}
		});
		// przesuniecie OCR1B wzgledem preloaded poczatku
		lblOCR1BDelay = new Label(grpTimer, SWT.NONE);
		lblOCR1BDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1BDelay.setText("TIMER1_COMPB delay (ms) :");
		txtOCR1BDelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1BDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1BDelay.setText("0.00");
		txtOCR1BDelay.setEditable(false);		
		//
		lblOutputB = new Label(grpTimer, SWT.NONE);
		lblOutputB.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputB.setText("Output B mode:");
		cmbOutB = new Combo(grpTimer, SWT.None);
		cmbOutB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutB.add("Disconnected");
		cmbOutB.add("Toggle on compare match");
		cmbOutB.add("Clear on compare match");
		cmbOutB.add("Set on compare match");
		cmbOutB.select(0);		
		
	}
	//------------------------------------------------------------------------------------------------------
	// set fast PWM - OCR1A 
	private void setFastPWMOCR1AMode(Group comp, Integer top) {
		addCommonControls(comp,14);
		// wartoœæ OCR1A definiujaca TOP dla trybu Fast PWM 
		lblOCR1A = new Label(comp, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value (TOP):");
		spiOCR1A = new Spinner(comp, SWT.BORDER);
		spiOCR1A.setMaximum(65535);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		// czestotliwosc PWM
		lblTIMERCOMPAFreq = new Label(grpTimer, SWT.NONE);
		lblTIMERCOMPAFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMERCOMPAFreq.setText("PWM frequency (Hz) :");
		txtTIMERCOMPAFreq = new Text(grpTimer,SWT.BORDER);
		txtTIMERCOMPAFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMERCOMPAFreq.setText("0.00");
		txtTIMERCOMPAFreq.setEditable(false);	

		//---------------------------------------------------------------------------------------------
		// przesuniecie OCR1A wzgledem poczatku
		lblOCR1ADelay = new Label(grpTimer, SWT.NONE);
		lblOCR1ADelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1ADelay.setText("TIMER1_COMPA delay after TCNT1 (ms) :");
		txtOCR1ADelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1ADelay .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1ADelay .setText("0.00");
		txtOCR1ADelay .setEditable(false);	
		//
		lblOutputA = new Label(grpTimer, SWT.NONE);
		lblOutputA.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputA.setText("Output A mode:");
		cmbOutA = new Combo(grpTimer, SWT.None);
		cmbOutA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutA.add("Disconnected");
		cmbOutA.add("Toggle on compare match");
		cmbOutA.add("Clear on compare match");
		cmbOutA.add("Set on compare match");
		cmbOutA.select(0);
		
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1B (0...TOP)
		lblOCR1B = new Label(grpTimer, SWT.NONE);
		lblOCR1B.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1B.setText("OCR1B value (PWM):");
		spiOCR1B = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1B.setMaximum(top);
		spiOCR1B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1B.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();				
				calcAll();
			}
		});
		// przesuniecie OCR1B wzgledem preloaded poczatku
		lblOCR1BDelay = new Label(grpTimer, SWT.NONE);
		lblOCR1BDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1BDelay.setText("TIMER1_COMPB delay after TCNT1 (ms) :");
		txtOCR1BDelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1BDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1BDelay.setText("0.00");
		txtOCR1BDelay.setEditable(false);		
		//
		lblOutputB = new Label(grpTimer, SWT.NONE);
		lblOutputB.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputB.setText("Output B mode:");
		cmbOutB = new Combo(grpTimer, SWT.None);
		cmbOutB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutB.add("Disconnected");
		cmbOutB.add("Toggle on compare match");
		cmbOutB.add("Clear on compare match");
		cmbOutB.add("Set on compare match");
		cmbOutB.select(0);		
	}
	//------------------------------------------------------------------------------------------------------
	// set fast PWM  
	private void setFastPWMFixedMode(Group comp, Integer top) {
		if (top==0xFF) {
			addCommonControls(comp,5);
		}
		if (top==0x1FF) {
			addCommonControls(comp,6);
		}
		if (top==0x3FF) {
			addCommonControls(comp,7);
		}
		
		//---------------------------------------------------------------------------------------------		
		// TOP VALUE
		lblTopValue = new Label(grpTimer, SWT.NONE);
		lblTopValue.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTopValue.setText("PWM top value (max) :");
		txtTopValue = new Text(grpTimer,SWT.BORDER);
		txtTopValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTopValue.setText(Integer.toString(top));
		txtTopValue.setEditable(false);	
		
		//---------------------------------------------------------------------------------------------		
		// czestotliwosc PWM
		lblTIMEROVFFreq = new Label(grpTimer, SWT.NONE);
		lblTIMEROVFFreq.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblTIMEROVFFreq.setText("PWM frequency (Hz) :");
		txtTIMEROVFFreq = new Text(grpTimer,SWT.BORDER);
		txtTIMEROVFFreq.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtTIMEROVFFreq.setText("0.00");
		txtTIMEROVFFreq.setEditable(false);	
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1A (0...TOP)
		lblOCR1A = new Label(grpTimer, SWT.NONE);
		lblOCR1A.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1A.setText("OCR1A value (PWM):");
		spiOCR1A = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1A.setMaximum(top);
		spiOCR1A.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1A.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();
				calcAll();
			}
		});
		
		// przesuniecie OCR1A wzgledem poczatku
		lblOCR1ADelay = new Label(grpTimer, SWT.NONE);
		lblOCR1ADelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1ADelay.setText("TIMER1_COMPA delay after TCNT1 (ms) :");
		txtOCR1ADelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1ADelay .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1ADelay .setText("0.00");
		txtOCR1ADelay .setEditable(false);	
		//
		lblOutputA = new Label(grpTimer, SWT.NONE);
		lblOutputA.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputA.setText("Output A mode:");
		cmbOutA = new Combo(grpTimer, SWT.None);
		cmbOutA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutA.add("Disconnected");
		cmbOutA.add("Toggle on compare match");
		cmbOutA.add("Clear on compare match");
		cmbOutA.add("Set on compare match");
		cmbOutA.select(0);
		
		
		//---------------------------------------------------------------------------------------------
		// wartoœæ OCR1B (0...TOP)
		lblOCR1B = new Label(grpTimer, SWT.NONE);
		lblOCR1B.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1B.setText("OCR1B value (PWM):");
		spiOCR1B = new Spinner(grpTimer, SWT.BORDER);
		spiOCR1B.setMaximum(top);
		spiOCR1B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		spiOCR1B.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				checkWidget();				
				calcAll();
			}
		});
		// przesuniecie OCR1B wzgledem preloaded poczatku
		lblOCR1BDelay = new Label(grpTimer, SWT.NONE);
		lblOCR1BDelay.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOCR1BDelay.setText("TIMER1_COMPB delay after TCNT1 (ms) :");
		txtOCR1BDelay = new Text(grpTimer,SWT.BORDER);
		txtOCR1BDelay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		txtOCR1BDelay.setText("0.00");
		txtOCR1BDelay.setEditable(false);		
		//
		lblOutputB = new Label(grpTimer, SWT.NONE);
		lblOutputB.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		lblOutputB.setText("Output B mode:");
		cmbOutB = new Combo(grpTimer, SWT.None);
		cmbOutB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbOutB.add("Disconnected");
		cmbOutB.add("Toggle on compare match");
		cmbOutB.add("Clear on compare match");
		cmbOutB.add("Set on compare match");
		cmbOutB.select(0);
	}
	
}  // end of class
