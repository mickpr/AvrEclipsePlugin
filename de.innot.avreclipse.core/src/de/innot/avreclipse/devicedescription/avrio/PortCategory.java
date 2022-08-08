/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/
package de.innot.avreclipse.devicedescription.avrio;

import de.innot.avreclipse.devicedescription.ICategory;

/**
 * Implements a ICategory for Port Elements.
 * 
 * @author Thomas Holland
 * 
 * @see RegisterCategory
 * @see IVecsCategory
 */
public class PortCategory extends BaseEntry implements ICategory {

	// The indices for Register Entry column fields
	final static int			IDX_NAME		= 0;
	final static int			IDX_DESCRIPTION	= 1;
	final static int			IDX_ADDRTYPE	= 2;
	final static int			IDX_ADDR		= 3;
	final static int			IDX_BITS		= 4;

	// The labels for Register Entry column data fields
	final static String			STR_NAME		= "Name";
	final static String			STR_DESCRIPTION	= "Description";
	final static String			STR_ADDRTYPE	= "";
	final static String			STR_ADDR		= "Addr";
	final static String			STR_BITS		= "Bits";

	final static String[]		fLabels			= { STR_NAME, STR_DESCRIPTION, STR_ADDRTYPE,
			STR_ADDR, STR_BITS					};
	final static int[]			fDefaultWidths	= { 10, 35, 4, 8, 6 };

	public final static String	CATEGORY_NAME	= "Ports";

	/**
	 * Instantiate a new PortCategory. The name is fixed to {@value #CATEGORY_NAME}
	 */
	public PortCategory() {
		super.setName(CATEGORY_NAME);
	}

	@Override
	public String getName() {
		return CATEGORY_NAME;
	}

	public int getColumnCount() {
		return fLabels.length;
	}

	public String[] getColumnLabels() {
		String[] labels = new String[fLabels.length];
		System.arraycopy(fLabels, 0, labels, 0, fLabels.length);
		return labels;
	}

	public int[] getColumnDefaultWidths() {
		int[] widths = new int[fDefaultWidths.length];
		System.arraycopy(fDefaultWidths, 0, widths, 0, fDefaultWidths.length);
		return widths;
	}

}
