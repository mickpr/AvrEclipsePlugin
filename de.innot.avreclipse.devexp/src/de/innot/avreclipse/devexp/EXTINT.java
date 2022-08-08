package de.innot.avreclipse.devexp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class EXTINT extends Composite {

	public EXTINT(Composite parent, Tree tree, int style) {
		super(parent, style); // SWT.FILL z parametru by³o "int style"
		setLayout(new FillLayout(SWT.VERTICAL));
		
	
		
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL);
		scrolledComposite.setMinWidth(200);
		scrolledComposite.setMinHeight(200);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setShowFocusedControl(true);
		scrolledComposite.setExpandHorizontal(true);
		final Composite compos = new Composite(scrolledComposite,SWT.NONE);
		GridLayout gl_compos = new GridLayout(2, false);
		gl_compos.marginWidth = 10;
		gl_compos.marginHeight = 10;
		compos.setLayout(gl_compos);
		

		//System.out.println("d-------------------------EXTINT----------------------d");
		//System.out.println(tree.getItemCount());
	    Map<String,Integer> mapextints= new HashMap<String,Integer>();
	    
		for (TreeItem tiResourceItem : tree.getItems()) {
			
			if (tiResourceItem.getText().startsWith("EXTINT")) {
				//System.out.println("Items: " + tiResourceItem.getItemCount());
				for (TreeItem tiPinItem: tiResourceItem.getItems()) {
					String text=tiPinItem.getText();
					mapextints.put(tiPinItem.getText(), 0);
					if (text.startsWith("INT")) { 
						mapextints.put(tiPinItem.getText(), 0);
					} else {
						mapextints.put(tiPinItem.getText(), 1);
					}
				} // for
			} // if 
		} // for	 		
		
		//mapextints.put("INT0",0 );
		//mapextints.put("INT1",0 );
		
		
		for (Entry<String, Integer> entry : mapextints.entrySet()) {
		    String s= entry.getKey();
		    Integer i = entry.getValue();
			
		    if (i==0) {
		    	Button btnCheckButton = new Button(compos, SWT.CHECK);
		    	btnCheckButton.setText(s);
		    	Combo cmbButton = new Combo(compos,SWT.NONE);
		    	cmbButton.add("On Low Level");
		    	cmbButton.add("Any Change");
		    	cmbButton.add("On falling edge");
		    	cmbButton.add("On rising edge");
		    }
		    else if (i==1) {
		    	Button btnCheckButton = new Button(compos,SWT.CHECK);
		    	btnCheckButton.setText("Any change on " + s);
		    	new Label(compos, SWT.NONE);
		    }
			
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
