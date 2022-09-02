package de.innot.avreclipse.devexp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class USART extends Composite {

	public USART(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.VERTICAL));
		
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
		
		//Usart1 com1 = new Usart1(compos,SWT.FILL);
		//Usart1 com2 = new Usart1(compos,SWT.FILL);

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
