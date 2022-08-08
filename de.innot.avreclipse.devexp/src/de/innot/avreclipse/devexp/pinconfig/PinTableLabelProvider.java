package de.innot.avreclipse.devexp.pinconfig;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public final class PinTableLabelProvider  extends LabelProvider implements ITableLabelProvider {
	  public Image getColumnImage(Object element, int columnIndex) {
		    return null;
	  }

	  public String getColumnText(Object element, int columnIndex) {
		    PinConfigData data = (PinConfigData) element;
		    switch (columnIndex) {
		      case 0:
		        return data.getPort();
		      case 1:
		    	  return data.getPinName();
		      case 2:
		    	  return (data.getIsInput()?"Input":"Output");
		      case 3:
		    	  return (data.getIsPullUp()?"Yes":"No");
		      case 4:
		    	  return data.getLabel();
		    	  
		      default:		    	  
		        return "";
		      }
	  }	  
}
