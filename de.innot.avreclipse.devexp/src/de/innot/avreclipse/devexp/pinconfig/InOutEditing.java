package de.innot.avreclipse.devexp.pinconfig;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import de.innot.avreclipse.devexp.pinconfig.PinConfigData.InOutValue;

public final class InOutEditing extends EditingSupport {
    
    private ComboBoxViewerCellEditor cellEditor = null;
    
    InOutEditing(ColumnViewer viewer) {
        super(viewer);
        cellEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
        cellEditor.setLabelProvider(new LabelProvider());
        cellEditor.setContenProvider(new ArrayContentProvider());
        cellEditor.setInput(InOutValue.values());
        cellEditor.getControl().setBackground(SWTResourceManager.getColor(255,255,200));
    }
    
    @Override
    protected CellEditor getCellEditor(Object element) {
        return cellEditor;
    }
    
    @Override
    protected boolean canEdit(Object element) {
    	// we can always select if pin is input or output
        return true;
    }
    
    @Override
    protected Object getValue(Object element) {
        if (element instanceof PinConfigData) {
        	PinConfigData data = (PinConfigData)element;
            return data.getIsInput();
        }
        return null;
    }
    
    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof PinConfigData && value instanceof InOutValue) {
        		PinConfigData data = (PinConfigData) element;
	        	
    		if (value==InOutValue.Input)
    		{ 	// set input
    			data.setIsInput(true);
    		} 
    		else 
    		{ 
    			// set output
    			data.setIsInput(false);
    			// for output pullup is always disabled
    			data.setIsPullUp(false);
    		} 
        }
    } // end of setValue
    
} // class
    
