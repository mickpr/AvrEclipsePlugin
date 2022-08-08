package de.innot.avreclipse.devexp.timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class TIMER extends Composite {
	double mcuFreq;
	String mcuName; 
	
	public TIMER(Composite parent, int style, String mcuName, double mcuFreq) {
		super(parent, style);
		setLayout(new FillLayout(SWT.VERTICAL));
		this.mcuFreq = mcuFreq;
		this.mcuName = mcuName;
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL);
		scrolledComposite.setMinWidth(200);
		scrolledComposite.setMinHeight(200);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setShowFocusedControl(true);
		scrolledComposite.setExpandHorizontal(true);
		final Composite compos = new Composite(scrolledComposite,SWT.NONE);
		GridLayout gl_compos = new GridLayout(1, false);
		gl_compos.marginWidth = 10;
		gl_compos.marginHeight = 10;
		compos.setLayout(gl_compos);

		
	
		
		if (mcuName.equalsIgnoreCase("atmega8")) { 
			Timer0Atmega8 t0= new Timer0Atmega8(compos,mcuFreq);
			//Timer1Atmega8 t1= new Timer1Atmega8(compos,mcuFreq);
			
		
			//jesli by wspoldzieli³y ten sam prescaler 
//			final Timer0Atmega8 t0= new Timer0Atmega8(compos,mcuFreq);
//			final Timer1Atmega8 t1= new Timer1Atmega8(compos,mcuFreq);
//			t0.comboClockSrc.addModifyListener(new ModifyListener() { // konieczne, bo timery 0 i 1 w Atmega8 maj¹ ten sam prescaler, wiêc musimy kazdorazowo
//				// go zmieniac, wzajemnie - jesli modyfikujemy timer0 czy timer1. Timer2 ma odrêbny prescaler.
//
//				@Override
//				public void modifyText(ModifyEvent e) {
//					// TODO Auto-generated method stub
//					System.out.println("dupsko");
//					t1.comboClockSrc.select(t0.comboClockSrc.getSelectionIndex());
//				}
//			});
//			
//			t1.comboClockSrc.addModifyListener(new ModifyListener() {
//				@Override
//				public void modifyText(ModifyEvent e) {
//					// TODO Auto-generated method stub
//					System.out.println("dupsko");
//					t0.comboClockSrc.select(t1.comboClockSrc.getSelectionIndex());
//				}
//			});
			
			//Timer0Atmega8 t3= new Timer0Atmega8(compos,mcuFreq);
		}else {
			//Timer0Atmega8 t0= new Timer0Atmega8(compos,mcuFreq);
		
			
		}

		
		scrolledComposite.setContent(compos);
		scrolledComposite.addControlListener(new ControlAdapter() {
		    public void controlResized(ControlEvent e) {
		        Rectangle r = scrolledComposite.getClientArea();
		        scrolledComposite.setMinSize(compos
		                .computeSize(r.width, SWT.DEFAULT));
		    }			
		});		
	} 
}
