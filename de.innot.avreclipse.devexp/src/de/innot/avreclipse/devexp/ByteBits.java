package de.innot.avreclipse.devexp;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.RowData;

public class ByteBits extends Canvas {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ByteBits(Composite parent, int style) {
		super(parent, SWT.BORDER);
		setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Button b7 = new Button(this, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		b7.setText("0");
		b7.setLayoutData(new RowData(24, 24));
		Button b6 = new Button(this, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		b6.setText("0");
		b6.setLayoutData(new RowData(24, 24));
		Button b5 = new Button(this, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		b5.setText("0");
		b5.setLayoutData(new RowData(24, 24));
		Button b4 = new Button(this, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		b4.setText("1");
		b4.setLayoutData(new RowData(24, 24));
		Button b3 = new Button(this, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		b3.setText("0");
		b3.setLayoutData(new RowData(24, 24));
		Button b2 = new Button(this, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		b2.setText("0");
		b2.setLayoutData(new RowData(24, 24));
		Button b1 = new Button(this, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		b1.setText("0");
		b1.setLayoutData(new RowData(24, 24));
		Button b0 = new Button(this, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		b0.setText("1");
		b0.setLayoutData(new RowData(24, 24));
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
