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

import de.innot.avreclipse.devexp.pinconfig.PinConfigData.PullUpValue;

public final class PullUpEditing extends EditingSupport {
    
    private ComboBoxViewerCellEditor cellEditor = null;
    //private org.eclipse.jface.viewers.TextCellEditor;
    @SuppressWarnings("deprecation")
	PullUpEditing(ColumnViewer viewer) {
        super(viewer);
        cellEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
        cellEditor.setLabelProvider(new LabelProvider());
        cellEditor.setContenProvider(new ArrayContentProvider());
        cellEditor.setInput(PullUpValue.values());
        cellEditor.getControl().setBackground(SWTResourceManager.getColor(255,255,200));
    }
    
    @Override
    protected CellEditor getCellEditor(Object element) {
        return cellEditor;
    }
    
    @Override
    protected boolean canEdit(Object element) {
    	if (element instanceof PinConfigData) {
        	PinConfigData data = (PinConfigData)element;
            if (data.getIsInput()==true) {
            	return true;
            	}
            else
            {
            	// output disables editing PullUp state 
            	return false;
            }
        }
    	return false;
    }
    
    @Override
    protected Object getValue(Object element) {
        if (element instanceof PinConfigData) {
        	PinConfigData data = (PinConfigData)element;
            return data.getIsPullUp();
        }
        return null;
    }
    
    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof PinConfigData && value instanceof PullUpValue) {
        	PinConfigData data = (PinConfigData) element;
            	if (value==PullUpValue.Yes)
            		{ data.setIsPullUp(true); } 
            		else 
            		{ data.setIsPullUp(false); } 
            }
        }
    }
    
