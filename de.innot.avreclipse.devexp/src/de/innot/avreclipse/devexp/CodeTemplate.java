package de.innot.avreclipse.devexp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.GridLayout;

public class CodeTemplate extends Composite {
	private Text text;

	public CodeTemplate(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		Composite compositeTop = new Composite(this, SWT.BORDER);
		compositeTop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_compositeTop = new GridLayout(3, false);
		compositeTop.setLayout(gl_compositeTop);
				
		Label lblNewLabel = new Label(compositeTop, SWT.NONE);
		lblNewLabel.setText("Filter: ");
		
		text = new Text(compositeTop, SWT.BORDER);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 492;
		text.setLayoutData(gd_text);
		
		Button btnNewButton = new Button(compositeTop, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.widthHint = 110;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.setText("Search");
		
		Composite compositeBottom = new Composite(this, SWT.NONE);
		compositeBottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeBottom.setLayout(new GridLayout(2, false));
		
		Tree tree = new Tree(compositeBottom, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tree.heightHint = 454;
		gd_tree.widthHint = 207;
		tree.setLayoutData(gd_tree);
		tree.setLinesVisible(true);
		
		TreeItem trtmNewTreeitem = new TreeItem(tree, SWT.NONE);
		trtmNewTreeitem.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		trtmNewTreeitem.setText("Timer 1 sec");
		
		TreeItem trtmNewTreeitem_1 = new TreeItem(tree, SWT.NONE);
		trtmNewTreeitem_1.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		trtmNewTreeitem_1.setText("Simple PWM");
		
		TreeItem trtmNewTreeitem_2 = new TreeItem(tree, SWT.NONE);
		trtmNewTreeitem_2.setFont(SWTResourceManager.getFont("Courier New", 10, SWT.NORMAL));
		trtmNewTreeitem_2.setText("Internal EEPROM reading/writing");
		
		StyledText styledText = new StyledText(compositeBottom, SWT.BORDER);
		GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_styledText.widthHint = 455;
		styledText.setLayoutData(gd_styledText);
		// TODO Auto-generated constructor stub
	}
}


