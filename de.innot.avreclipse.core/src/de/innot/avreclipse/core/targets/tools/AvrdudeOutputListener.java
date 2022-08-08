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

package de.innot.avreclipse.core.targets.tools;

import org.eclipse.core.runtime.IProgressMonitor;

import de.innot.avreclipse.core.avrdude.AVRDudeException.Reason;
import de.innot.avreclipse.core.toolinfo.ICommandOutputListener;

/**
 * Class to listen to the line-by-line output of avrdude and cancels the operation if certain key
 * strings appears in the output.
 * <p>
 * They are:
 * <ul>
 * <li><code>timeout</code></li>
 * <li><code>Can't open device</code></li>
 * <li><code>can't open config file</code></li>
 * <li><code>Can't find programmer id</code></li>
 * <li><code>AVR Part ???? not found</code></li>
 * </ul>
 * </p>
 * <p>
 * Once any of these Strings is found in the output the associated Reason is set and avrdude is
 * aborted via the ProgressMonitor.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class AvrdudeOutputListener implements ICommandOutputListener {

	private IProgressMonitor	fProgressMonitor;
	private Reason				fAbortReason;
	private String				fAbortLine;

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.ICommandOutputListener#init(org.eclipse.core.runtime
	 * .IProgressMonitor)
	 */
	public void init(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
		fAbortLine = null;
		fAbortReason = null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.ICommandOutputListener#handleLine(java.lang.String,
	 * de.innot.avreclipse.core.toolinfo.ICommandOutputListener.StreamSource)
	 */
	public void handleLine(String line, StreamSource source) {

		boolean abort = false;

		if (line.contains("timeout")) {
			abort = true;
			fAbortReason = Reason.TIMEOUT;
		} else if (line.contains("can't open device")) {
			abort = true;
			fAbortReason = Reason.PORT_BLOCKED;
		} else if (line.contains("can't open config file")) {
			abort = true;
			fAbortReason = Reason.CONFIG_NOT_FOUND;
		} else if (line.contains("Can't find programmer id")) {
			abort = true;
			fAbortReason = Reason.UNKNOWN_PROGRAMMER;
		} /*
		 * else if (line.contains("no programmer has been specified")) { abort = true; fAbortReason
		 * = Reason.NO_PROGRAMMER; }
		 */else if (line.matches("AVR Part.+not found")) {
			abort = true;
			fAbortReason = Reason.UNKNOWN_MCU;
		} else if (line.endsWith("execution aborted")) {
			abort = true;
			fAbortReason = Reason.USER_CANCEL;
		} else if (line.contains("usbdev_open(): Found ")) {
			// This is not an error, but probably a message due to -v option
		} else if (line.contains("usbdev_open")) {
			abort = true;
			fAbortReason = Reason.NO_USB;
		} else if (line.contains("failed to sync with")) {
			abort = true;
			fAbortReason = Reason.SYNC_FAIL;
		} else if (line.contains("initialization failed")) {
			abort = true;
			fAbortReason = Reason.INIT_FAIL;
		} else if (line.contains("NO_TARGET_POWER")) {
			abort = true;
			fAbortReason = Reason.NO_TARGET_POWER;
		}
		if (abort) {
			fProgressMonitor.setCanceled(true);
			fAbortLine = line;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.ICommandOutputListener#getAbortLine()
	 */
	public String getAbortLine() {
		return fAbortLine;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.ICommandOutputListener#getAbortReason()
	 */
	public Reason getAbortReason() {
		return fAbortReason;
	}

}
