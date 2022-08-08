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
package de.innot.avreclipse.core.toolinfo.fuses;

/**
 * Interface for objects which are interested in getting informed about changes in a
 * {@link ByteValues} object.
 * <p>
 * To improve performance the listener can receive more than one event in case multiple BitFields
 * are effected by the change of a complete byte.
 * </p>
 * 
 * 
 * @author Thomas Holland
 * @since 2.3
 */
public interface IByteValuesChangeListener {

	/**
	 * Callback method that is called whenever the source <code>ByteValues</code> object has
	 * changed.
	 * <p>
	 * The implementation receives an array of {@link ByteValueChangeEvent}s. This array has either
	 * a single Event for a single BitField change or multiple events for all effected BitFields in
	 * case a complete byte was changed.
	 * </p>
	 * <p>
	 * A single special event is send when either the MCU or the comment of a ByteValues object has
	 * changed.
	 * </p>
	 * 
	 * @param events
	 *            Array with at least one event object.
	 */
	public void byteValuesChanged(ByteValueChangeEvent[] events);

}
