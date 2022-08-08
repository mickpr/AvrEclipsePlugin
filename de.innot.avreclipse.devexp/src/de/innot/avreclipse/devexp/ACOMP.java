package de.innot.avreclipse.devexp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class ACOMP extends Composite {
	private Table table;

	public ACOMP(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL);
		scrolledComposite.setMinWidth(200);
		scrolledComposite.setMinHeight(200);
		scrolledComposite.setAlwaysShowScrollBars(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setShowFocusedControl(true);
		scrolledComposite.setExpandHorizontal(true);
		final Composite compos = new Composite(scrolledComposite,SWT.FILL);
		GridLayout gl_compos = new GridLayout(1, false);
		gl_compos.marginHeight = 10;
		gl_compos.marginWidth = 10;
		compos.setLayout(gl_compos);
		
		table = new Table(compos, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd_table = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2);
		gd_table.widthHint = 392;
		gd_table.heightHint = 131;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		Button btnNewButton = new Button(compos, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnNewButton.setText("New Button");
		
		Tree tree = new Tree(compos, SWT.BORDER);
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tree.heightHint = 150;
		tree.setLayoutData(gd_tree);
		
		TreeItem trtmNewTreeitem = new TreeItem(tree, SWT.NONE);
		trtmNewTreeitem.setText("New TreeItem");
		
		TreeItem trtmNewTreeitem_1 = new TreeItem(tree, SWT.NONE);
		trtmNewTreeitem_1.setText("New TreeItem");
		
		TreeItem trtmNewTreeitem_2 = new TreeItem(trtmNewTreeitem_1, SWT.NONE);
		trtmNewTreeitem_2.setText("New TreeItem");
		trtmNewTreeitem_1.setExpanded(true);
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
