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
package de.innot.avreclipse.core.toolinfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;

import de.innot.avreclipse.PluginIDs;
import de.innot.avreclipse.core.IMCUProvider;
import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathProvider;
import de.innot.avreclipse.core.paths.IPathProvider;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * This class provides some information about the used gcc compiler in the toolchain.
 * <p>
 * It can return a list of all supported target mcus.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.1
 */
public class GCC extends BaseToolInfo implements IMCUProvider {

	private static final String	TOOL_ID			= PluginIDs.PLUGIN_TOOLCHAIN_TOOL_COMPILER;

	private static GCC			instance		= null;

	private Map<String, String>	fMCUmap			= null;

	private IPath				fCurrentPath	= null;

	private final IPathProvider	fPathProvider	= new AVRPathProvider(AVRPath.AVRGCC);

	/**
	 * Get an instance of this Tool.
	 */
	public static GCC getDefault() {
		if (instance == null)
			instance = new GCC();
		return instance;
	}

	private GCC() {
		// Let the superclass get the command name
		super(TOOL_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.IToolInfo#getToolPath()
	 */
	@Override
	public IPath getToolPath() {
		IPath path = fPathProvider.getPath();
		return path.append(getCommandName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUInfo(java.lang.String)
	 */
	public String getMCUInfo(String mcuid) {
		try {
			Map<String, String> internalmap = loadMCUList();
			return internalmap.get(mcuid);
		} catch (IOException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUList()
	 */
	public Set<String> getMCUList() throws IOException {
		Map<String, String> internalmap = loadMCUList();
		Set<String> idlist = internalmap.keySet();
		return new HashSet<String>(idlist);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#hasMCU(java.lang.String)
	 */
	public boolean hasMCU(String mcuid) {
		try {
			Map<String, String> internalmap = loadMCUList();
			return internalmap.containsKey(mcuid);
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * @return Map &lt;mcu id, UI name&gt; of all supported MCUs
	 */
	private Map<String, String> loadMCUList() throws IOException {

		if (!getToolPath().equals(fCurrentPath)) {
			// toolpath has changed, reload the list
			fMCUmap = null;
			fCurrentPath = getToolPath();
		}

		if (fMCUmap != null) {
			// return stored map
			return fMCUmap;
		}

		fMCUmap = new HashMap<String, String>();

		// Execute avr-gcc with the "--target-help" option and parse the
		// output
		// Also add -Wa,-mlist-devices so that it works in more recent versions
		// Of GCC and AVR CrossPack in Mac OS X
		List<String> stdout = runCommand("-Wa,-mlist-devices", "--target-help");
		if (stdout == null) {
			// Return empty map on failures
			return fMCUmap;
		}

		boolean start = false;

		// The parsing is done by reading the output line by line until a line
		// with "Known MCU names:" is found. Then the parsing starts and all
		// following lines are split into the mcu ids until a line is reached
		// that does not start with a space (the mcu id lines start always with
		// a space).
		//
		// Maybe this could be done with a Pattern matcher, but I don't know how
		// to do multiline pattern matching and this is probably faster anyway
		for (String line : stdout) {
			if ("Known MCU names:".equals(line)) {
				start = true;
			} else if (start && !line.startsWith(" ")) {
				// finished
				start = false;
			} else if (start) {
				String[] names = line.split(" ");
				for (String mcuid : names) {
					String mcuname = AVRMCUidConverter.id2name(mcuid);
					if (mcuname == null) {
						// some mcuid are generic and should not be
						// included
						continue;
					}
					fMCUmap.put(mcuid, mcuname);
// System.out.println("MCUID: " + mcuid + " MCUNAME: " + mcuname);
				}
			} else {
				// a line outside of the "Known MCU names:" section
			}
		}
		
		//mickpr: add some newer chips, that Microchip miss...(intentially!)
		fMCUmap.put("atmega808", "ATmega808");
		fMCUmap.put("atmega809", "ATmega809");	
		fMCUmap.put("atmega1608", "ATmega1608");
		fMCUmap.put("atmega1609", "ATmega1609");	
		fMCUmap.put("atmega3208", "ATmega3208");
		fMCUmap.put("atmega3209", "ATmega3209");	
		fMCUmap.put("atmega4808", "ATmega4808");
		fMCUmap.put("atmega4809", "ATmega4809");
		
		fMCUmap.put("atmega48pb", "ATmega48PB");
		fMCUmap.put("atmega88pb", "ATmega88PB");
		fMCUmap.put("atmega168pb", "ATmega168PB");
		fMCUmap.put("atmega328pb", "ATmega328PB");

		fMCUmap.put("attiny412", "ATtiny412");
		fMCUmap.put("attiny414", "ATtiny414");
		fMCUmap.put("attiny416", "ATtiny416");
		fMCUmap.put("attiny417", "ATtiny417");

		fMCUmap.put("attiny824", "ATtiny824");
		fMCUmap.put("attiny826", "ATtiny826");
		fMCUmap.put("attiny827", "ATtiny827");
		fMCUmap.put("attiny841", "ATtiny841");

		fMCUmap.put("avr16dd14", "AVR16DD14");
		fMCUmap.put("avr16dd20", "AVR16DD20");
		fMCUmap.put("avr16dd28", "AVR16DD28");
		fMCUmap.put("avr16dd32", "AVR16DD32");
		fMCUmap.put("avr16dd32", "AVR16DD32");

		fMCUmap.put("avr32da28", "AVR32DA28");
		fMCUmap.put("avr32da32", "AVR32DA32");
		fMCUmap.put("avr32da48", "AVR32DA48");
		

		fMCUmap.put("avr32db28", "AVR32DB28");
		fMCUmap.put("avr32db32", "AVR32DB32");
		fMCUmap.put("avr32db48", "AVR32DB48");

		fMCUmap.put("avr32dd14", "AVR32DD14");
		fMCUmap.put("avr32dd20", "AVR32DD20");
		fMCUmap.put("avr32dd28", "AVR32DD28");
		fMCUmap.put("avr32dd32", "AVR32DD32");
		
		fMCUmap.put("avr64da28", "AVR64DA28");
		fMCUmap.put("avr64da32", "AVR64DA32");
		fMCUmap.put("avr64da48", "AVR64DA48");
		fMCUmap.put("avr64da64", "AVR64DA64");

		fMCUmap.put("avr64db28", "AVR64DB28");
		fMCUmap.put("avr64db32", "AVR64DB32");
		fMCUmap.put("avr64db48", "AVR64DB48");
		fMCUmap.put("avr64db64", "AVR64DB64");

		fMCUmap.put("avr64dd14", "AVR64DD14");
		fMCUmap.put("avr64dd20", "AVR64DD20");
		fMCUmap.put("avr64dd28", "AVR64DD28");
		fMCUmap.put("avr64dd32", "AVR64DD32");
		
		fMCUmap.put("avr64ea28", "AVR64EA28");
		fMCUmap.put("avr64ea32", "AVR64EA32");
		fMCUmap.put("avr64ea48", "AVR64EA48");
		
		fMCUmap.put("avr128da28", "AVR128DA28");
		fMCUmap.put("avr128da32", "AVR128DA32");
		fMCUmap.put("avr128da48", "AVR128DA48");
		fMCUmap.put("avr128da64", "AVR128DA64");

		fMCUmap.put("avr128db28", "AVR128DB28");
		fMCUmap.put("avr128db32", "AVR128DB32");
		fMCUmap.put("avr128db48", "AVR128DB48");
		fMCUmap.put("avr128db64", "AVR128DB64");
		
		return fMCUmap;
	}

	/**
	 * Get the command name and the current version of GCC.
	 * <p>
	 * The name comes from the buildDefinition. The version is gathered by executing with the "-v"
	 * option and parsing the output.
	 * </p>
	 * 
	 * @return <code>String</code> with the command name and version
	 * @throws IOException
	 *             if the avr-gcc command could not be executed.
	 */
	public String getNameAndVersion() throws IOException {

		// Execute avr-gcc with the "-v" option and parse the
		// output
		List<String> stdout = runCommand("-v");
		if (stdout == null) {
			// Return default name on failures
			return getCommandName() + " n/a";
		}

		// look for a line matching "gcc version TheVersionNumber"
		Pattern mcuPat = Pattern.compile("gcc version\\s*(.*)");
		Matcher m;
		for (String line : stdout) {
			m = mcuPat.matcher(line);
			if (!m.matches()) {
				continue;
			}
			return getCommandName() + " " + m.group(1);
		}

		// could not read the version from the output, probably the regex has a
		// mistake. Return a reasonable default.
		return getCommandName() + "?.?";
	}

	/**
	 * Runs the GCC with the given arguments.
	 * <p>
	 * The Output of stdout and stderr are merged and returned in a <code>List&lt;String&gt;</code>.
	 * </p>
	 * <p>
	 * If the command fails to execute an entry is written to the log and <code>null</code> is
	 * returned
	 * 
	 * @param arguments
	 *            Zero or more arguments for gcc
	 * @throws IOException
	 *             if the command could not be executed
	 * @return A list of all output lines, or <code>null</code> if the command could not be
	 *         launched.
	 */
	private List<String> runCommand(String... arguments) throws IOException {

		String command = getToolPath().toOSString();
		List<String> arglist = new ArrayList<String>(1);
		for (String arg : arguments) {
			arglist.add(arg);
		}

		ExternalCommandLauncher gcc = new ExternalCommandLauncher(command, arglist);
		gcc.redirectErrorStream(true);
		gcc.launch();

		List<String> stdout = gcc.getStdOut();

		return stdout;
	}
}
