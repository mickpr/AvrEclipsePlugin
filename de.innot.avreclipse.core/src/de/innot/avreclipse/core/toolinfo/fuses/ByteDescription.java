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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Description for a single Fuse or Lockbits byte.
 * <p>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.3
 * @version 2.4 Added description
 * 
 */
public class ByteDescription implements IFuseObjectDescription {

	protected final static String			ATTR_BYTE_INDEX		= "index";
	protected final static String			ATTR_DESCRIPTION	= "description";
	protected final static String			ATTR_BYTE_NAME		= "name";
	protected final static String			ATTR_DEFAULT_VALUE	= "default";
	protected final static String			ATTR_SIZE			= "size";

	/** The type of this byte. Either FUSE or LOCKBITS */
	private final FuseType					fType;
	// TODO: Do we need this?

	/** Fuse byte name (from the part description file). */
	private final String					fName;

	/** A short description of this Byte. */
	private final String					fDescription;

	/** The index of the Byte. (0 up to 5 for fuse bytes, 0 for the lockbits byte) */
	private final int						fIndex;

	/**
	 * The number of bytes in this object.
	 * 
	 * @since 2.4
	 */
	private final int						fSize;

	/** The default values. <code>-1</code> if no default value is defined. */
	private final int						fDefaultValue;

	/** List with all BitFieldDescriptions for this byte. */
	private final List<BitFieldDescription>	fBitFieldList;

	/**
	 * Create a new ByteDescription for the byte with the given number parameters.
	 * 
	 * @param type
	 *            Either {@link FuseType#FUSE} or {@link FuseType#LOCKBITS}
	 * @param name
	 *            The name of this byte as defined in the part description file, e.g "LOW",
	 *            "FUSEBYTE3" or "LOCKBITS".
	 * @param index
	 *            The index of this byte within its memory. <code>0</code> up to <code>5</code> for
	 *            fuse bytes (depending on MCU) or <code>0</code> for the lockbits byte.
	 */
	public ByteDescription(FuseType type, String description, String name, int index, int size,
			int defaultvalue) {
		fType = type;
		fDescription = description;
		fName = name;
		fIndex = index;
		fSize = size;
		fDefaultValue = defaultvalue;
		fBitFieldList = new ArrayList<BitFieldDescription>();
	}

	/**
	 * Construct a new ByteDescription from a XML &lt;fusebyte&gt; or &lt;lockbitsbyte&gt; node.
	 * <p>
	 * This constructor will take the node and parse the values from the "index", "name" and
	 * "default" attributes.
	 * </p>
	 * <p>
	 * Then the list of {@link BitFieldDescription}s is filled from all &lt;bitfield&gt; elements.
	 * </p>
	 * 
	 * @param bitfieldnode
	 *            A &lt;bitfield&gt; document node.
	 */
	public ByteDescription(Node byteelement) throws IllegalArgumentException {

		// Default / Error check values
		fBitFieldList = new ArrayList<BitFieldDescription>();

		// Get the type of this byte from the given node.
		if (byteelement.getNodeName().equalsIgnoreCase(FuseType.FUSE.getElementName())) {
			fType = FuseType.FUSE;
		} else {
			fType = FuseType.LOCKBITS;
		}

		// The byte element has up to five attributes: "index" and "name" are required
		// "default", "description" and "size" are optional, depending on the version
		// of the generator.

		NamedNodeMap attrs = byteelement.getAttributes();

		Node indexnode = attrs.getNamedItem(ATTR_BYTE_INDEX);
		if (indexnode == null) {
			throw new IllegalArgumentException("Required attribute \"" + ATTR_BYTE_INDEX
					+ "\" for element <" + fType.getElementName() + "> missing.");
		}
		fIndex = Integer.decode(indexnode.getTextContent());

		Node namenode = attrs.getNamedItem(ATTR_BYTE_NAME);
		if (namenode == null) {
			throw new IllegalArgumentException("Required attribute \"" + ATTR_BYTE_NAME
					+ "\" for element <" + fType.getElementName() + "> missing.");
		}
		fName = namenode.getNodeValue();

		Node sizenode = attrs.getNamedItem(ATTR_SIZE);
		if (sizenode != null) {
			fSize = Integer.decode(sizenode.getNodeValue());
		} else {
			fSize = 1;
		}

		Node descriptionNode = attrs.getNamedItem(ATTR_DESCRIPTION);
		if (descriptionNode != null) {
			fDescription = descriptionNode.getNodeValue();
		} else {
			fDescription = fName;
		}


		Node defaultnode = attrs.getNamedItem(ATTR_DEFAULT_VALUE);
		if (defaultnode == null) {
			fDefaultValue = -1;
		} else {
			fDefaultValue = Integer.decode(defaultnode.getNodeValue());
		}

		// Now read the children of the byte element:
		// one default element (optional) and at least one <bitfield>
		NodeList children = byteelement.getChildNodes();

		for (int n = 0; n < children.getLength(); n++) {
			Node child = children.item(n);
			if (BitFieldDescription.TAG_BITFIELD.equalsIgnoreCase(child.getNodeName())) {
				// get the BitfieldDescription Object and add it to the list
				BitFieldDescription bfd = new BitFieldDescription(child);
				fBitFieldList.add(bfd);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteDescription#getBitFieldDescriptions()
	 */
	public List<BitFieldDescription> getBitFieldDescriptions() {
		return new ArrayList<BitFieldDescription>(fBitFieldList);
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteDescription#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteDescription#getDescription()
	 */
	public String getDescription() {
		return fDescription;
	}

	/* (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IFuseObjectDescription#getSize()
	 */
	public int getSize() {
		return fSize;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteDescription#getIndex()
	 */
	public int getIndex() {
		return fIndex;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteDescription#getDefaultValue()
	 */
	public int getDefaultValue() {
		if (fType == FuseType.LOCKBITS) {
			return 0xff;
		}
		return fDefaultValue;
	}

	/**
	 * Add a <code>BitFieldDescription</code> object to this ByteDescription.
	 * <p>
	 * The list of all BitFieldDescriptions can be retrieved with {@link #getBitFieldDescriptions()}
	 * .
	 * </p>
	 * 
	 * @param bitfielddescription
	 *            A single <code>BitFieldDescription</code>
	 */
	public void addBitFieldDescription(BitFieldDescription bitfielddescription) {
		fBitFieldList.add(bitfielddescription);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.toolinfo.fuses.IByteDescription#isCompatibleWith(de.innot.avreclipse
	 * .core.toolinfo.fuses.IByteDescription)
	 */
	public boolean isCompatibleWith(IFuseObjectDescription target) {

		// Get the list of the target BitFieldDescriptions and convert it into a map with BitField
		// names and their masks.
		List<BitFieldDescription> targetlist = target.getBitFieldDescriptions();
		Map<String, Integer> targetmap = new HashMap<String, Integer>();
		for (BitFieldDescription bfd : targetlist) {
			targetmap.put(bfd.getName(), bfd.getMask());
		}

		// now we can compare them with our bitfields.
		for (BitFieldDescription ourbitfield : fBitFieldList) {
			String name = ourbitfield.getName();
			Integer mask = targetmap.get(name);
			if (mask == null || mask != ourbitfield.getMask()) {
				return false;
			}
		}

		// All bitfields match -> success
		return true;
	}

	/**
	 * Convert this ByteDescription Object to XML.
	 * 
	 * @see MCUDescription#toXML(Document) for the DTD of the generated xml
	 * 
	 * @param parentelement
	 *            A &lt;fusedescription&gt; element node to which this description is added
	 */
	public void toXML(Node parentnode) {
		Document document = parentnode.getOwnerDocument();

		// Create the byte node
		Element bytenode = document.createElement(fType.getElementName());
		bytenode.setAttribute(ATTR_DESCRIPTION, fDescription);
		bytenode.setAttribute(ATTR_BYTE_INDEX, Integer.toString(fIndex));
		bytenode.setAttribute(ATTR_BYTE_NAME, fName);
		bytenode.setAttribute(ATTR_SIZE, Integer.toString(fSize));
		if (fDefaultValue != -1) {
			bytenode.setAttribute(ATTR_DEFAULT_VALUE, toHex(fDefaultValue));

		}

		// add the bitfield description elements
		for (BitFieldDescription bfd : fBitFieldList) {
			bfd.toXML(bytenode);
		}

		parentnode.appendChild(bytenode);
	}

	/**
	 * Format the given integer to a String with the format "0xXX".
	 * <p>
	 * Unlike the normal <code>Integer.toHexString(i)</code> method, this method will always produce
	 * two digits, even with the high nibble at zero, and will output the hex value in uppercase.
	 * This should make the value more readable than the standard <code>Integer.toHexString</code>
	 * output.
	 * </p>
	 * 
	 * @param value
	 *            Single byte value
	 * @return String with the byte value as "0xXX"
	 */
	public static String toHex(int value) {
		String hex = "00" + Integer.toHexString(value);
		return "0x" + hex.substring(hex.length() - 2).toUpperCase();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(fType.getElementName());
		sb.append(" [");
		sb.append(" default=" + fDefaultValue);
		for (BitFieldDescription bfd : fBitFieldList) {
			sb.append(", " + bfd.toString());
		}
		sb.append("]");

		return sb.toString();
	}
}
