package de.innot.avreclipse.devexp.pinconfig;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public final class LabelEditing extends EditingSupport {
    
    protected static final String Text = null;
	private TextCellEditor cellEditor = null;
    LabelEditing(ColumnViewer viewer) {
        super(viewer);
        cellEditor = new TextCellEditor((Composite) getViewer().getControl());
        ((Text) cellEditor.getControl()).setTextLimit(20);
        cellEditor.getControl().setBackground(SWTResourceManager.getColor(255,255,200));  //light yellow
         
//        ((Text) cellEditor.getControl()).addVerifyListener(new VerifyListener() {
//			@Override
//			public void verifyText(VerifyEvent e) {
//				Text text = (Text) cellEditor.getControl();
//	            String oldText = text.getText();
//	            String leftText = oldText.substring(0, e.start);
//	            String rightText = oldText.substring(e.end, oldText.length());
//	            GC gc = new GC(text);
//	            Point size = gc.textExtent(leftText + e.text + rightText);
//	            gc.dispose();
//	            if (size.x != 0)
//	                size = text.computeSize(size.x, SWT.DEFAULT);
//	            cellEditor.getControl().setSize(size.x, size.y);
//			}
//        	
//        });
        //cellEditor.setLabelProvider(new LabelProvider());
        //cellEditor.setContenProvider(new ArrayContentProvider());
        
    }
    
    @Override
    protected CellEditor getCellEditor(Object element) {
        return cellEditor;
    }
    
    @Override
    protected boolean canEdit(Object element) {
    	return true;
    }
    
    @Override
    protected Object getValue(Object element) {
        if (element instanceof PinConfigData) {
        	PinConfigData data = (PinConfigData)element;
            return data.getLabel();
        }
        return null;
    }
    
    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof PinConfigData) {
        	PinConfigData data = (PinConfigData) element;
        	String string = (value!=null ? value.toString() : "");
        	data.setLabel(string);
        }
    }
}