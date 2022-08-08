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
package de.innot.avreclipse.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.forms.IManagedForm;

import de.innot.avreclipse.ui.actions.ActionType;
import de.innot.avreclipse.ui.dialogs.ChangeMCUDialog;

/**
 * A <code>IFormPart</code> that adds an action to the form toolbar to change the MCU type.
 * 
 * @see AbstractActionPart
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class MCUChangeActionPart extends AbstractActionPart {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.editors.AbstractActionPart#getAction()
	 */
	@Override
	protected IAction[] getAction() {

		ActionType type = ActionType.CHANGE_MCU;

		Action changeAction = new Action() {

			@Override
			public void run() {

				// Open the "Change MCU" dialog
				String filename = "";
				IManagedForm mform = getManagedForm();
				Object container = mform.getContainer();
				if (container instanceof ByteValuesFormEditor) {
					ByteValuesFormEditor page = (ByteValuesFormEditor) container;
					filename = page.getFilename();
				}
				ChangeMCUDialog changeMCUDialog = new ChangeMCUDialog(getManagedForm().getForm()
						.getShell(), getByteValues(), filename);

				if (changeMCUDialog.open() == ChangeMCUDialog.OK) {

					String newmcuid = changeMCUDialog.getResult();
					String oldmcuid = getByteValues().getMCUId();
					if (oldmcuid.equals(newmcuid)) {
						return;
					}

					// Now we can change the MCU to the new type, converting the old bitfield values
					// as far as possible.
					// The actual form is registered as a ByteValuesChangeListener, so it will be
					// notified about the new MCU.
					getByteValues().setMCUId(newmcuid, true);

					// Mark the ByteValues dirty due to the changed MCU.
					markDirty();
				}
			}
		};

		type.setupAction(changeAction);

		IAction[] allactions = new IAction[1];
		allactions[0] = changeAction;

		return allactions;

	}
}
