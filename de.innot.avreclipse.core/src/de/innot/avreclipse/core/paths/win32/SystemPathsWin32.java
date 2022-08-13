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

package de.innot.avreclipse.core.paths.win32;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.SystemPathHelper;

/**
 * Gets the actual system paths to the Avr Atmel Toochain and AVR Tools applications.
 * <p>
 * Unlike the Posix variant of this class, which actually looks through the (almost) complete
 * filesystem, this class will retrieve the paths from the Windows registry. But even this has a bit
 * of overhead, so the {@link SystemPathHelper}, which uses this class, should cache the results.
 * </p>
 * 
 * @author Thomas Holland modified by Michal Przybyl
 * @since 2.1
 */
public class SystemPathsWin32 {
	//private static IPath		fWinAVRPath		= null;
	private static IPath		fAVRToolsPath	= null;

	private final static IPath	fEmptyPath		= new Path("");

	private SystemPathsWin32() {
		// prevent instantiation
	}

	/**
	 * Find the system path for the given {@link AVRPath} enum value.
	 * 
	 * @param avrpath
	 * @return a valid path or <code>null</code> if no path could be found.
	 */
	public static IPath getSystemPath(AVRPath avrpath) {

		switch (avrpath) {
			case AVRGCC:
				//return getWinAVRPath("bin");
				return getAvrToolchainBasePath("bin");
			case AVRINCLUDE:
				//return getWinAVRPath("avr/include");
				return getAvrToolchainBasePath("avr/include");
			case AVRDUDE:
				//return getWinAVRPath("bin");
				return getAvrDudePath("");
			case MAKE:
				//return getWinAVRPath("utils/bin");
				return getMakePath("");
			case PDFPATH:
				IPath basepath = getAVRToolsPath();
				if (basepath.isEmpty()) {
					return basepath;
				}
				return basepath.append("Partdescriptionfiles");
			default:
				// If we end up here the AVRPath Enum has new entries not yet covered.
				// Log this as an internal error and ignore otherwise
				IStatus status = new Status(
						IStatus.WARNING,
						AVRPlugin.PLUGIN_ID,
						"Internal problem! AVRPath with value ["
								+ avrpath.toString()
								+ "] is not covered. Please report to the AVR Eclipse plugin maintainer.",
						null);
				AVRPlugin.getDefault().log(status);
				return null;
		}
	}


	private static IPath getAvrToolchainBasePath(String append) {
		IPath binPath = getAvrToolchainBinPath();
		if (binPath.isEmpty()) {
			return binPath;
		}
		return binPath.removeLastSegments(1).append(append);

	}
	
	// mickpr
	private static IPath getAvrToolchainBinPath() {
		// get system path
		String path1 = System.getenv("PATH");
		Pattern pattern = Pattern.compile("(^|\\G)([^\"]*?)(;|$)|\"(.*?)\"(;|$)");
		Matcher matcher = pattern.matcher(path1);
		while(matcher.find()){
		    String group = matcher.group(2); //przypadek bez cudzyslowow  ([^\"]*?)
		    if (group == null){
		        group = matcher.group(4);  //z cudzyslowami (.*?)
		    }
			File f = new File(group + "/" + "avr-gcc.exe");
			if(f.exists() && !f.isDirectory()) { 
			    return new Path(group);
			}
		}		
		return new Path(null);
	}

	// mickpr	
	private static IPath getMakePath(String append) {
		// get system path
		String path1 = System.getenv("PATH");
		Pattern pattern = Pattern.compile("(^|\\G)([^\"]*?)(;|$)|\"(.*?)\"(;|$)");
		Matcher matcher = pattern.matcher(path1);
		while(matcher.find()){
		    String group = matcher.group(2); //przypadek bez cudzyslowow  ([^\"]*?)
		    if (group == null){
		        group = matcher.group(4);  //z cudzyslowami (.*?)
		    }
			File f = new File(group + "/" + "make.exe");
			if(f.exists() && !f.isDirectory()) { 
			    return new Path(group);
			}
		}		
		return new Path(null);
	}

	// mickpr	
	private static IPath getAvrDudePath(String append) {
		// get system path
		String path1 = System.getenv("PATH");
		Pattern pattern = Pattern.compile("(^|\\G)([^\"]*?)(;|$)|\"(.*?)\"(;|$)");
		Matcher matcher = pattern.matcher(path1);
		while(matcher.find()){
		    String group = matcher.group(2); //przypadek bez cudzyslowow  ([^\"]*?)
		    if (group == null){
		        group = matcher.group(4);  //z cudzyslowami (.*?)
		    }
			File f = new File(group + "/" + "avrdude.exe");
			if(f.exists() && !f.isDirectory()) { 
			    return new Path(group);
			}
		}		
		return new Path(null);
	}	

//	// mickpr(added by me)	
//	private static IPath getAvrDudePath(String append) {
//		// get system path
//		String path1 = System.getenv("PATH");
//		Pattern pattern = Pattern.compile("(^|\\G)([^\"]*?)(;|$)|\"(.*?)\"(;|$)");
//		Matcher matcher = pattern.matcher(path1);
//		while(matcher.find()){
//		    String group = matcher.group(2); //przypadek bez cudzyslowow  ([^\"]*?)
//		    if (group == null){
//		        group = matcher.group(4);  //z cudzyslowami (.*?)
//		    }
//			File f = new File(group + "/" + "avrdude.exe");
//			if(f.exists() && !f.isDirectory()) { 
//			    return new Path(group);
//			}
//		}		
//		return new Path(null);
//	}	
//	
	
//	private static IPath getWinAVRPath(String append) {
//		IPath basepath = getWinAVRBasePath();
//		if (basepath.isEmpty()) {
//			return basepath;
//		}
//		return basepath.append(append);
//	}

//	/**
//	 * Get the path to the winAVR base directory from the Windows registry.
//	 * 
//	 * @return IPath with the current path to the winAVR base directory
//	 */
//	private static IPath getWinAVRBasePath() {
//		if (fWinAVRPath != null) {
//			return fWinAVRPath;
//		}
//
//		MyWindowsRegistry registry = MyWindowsRegistry.getRegistry();
//		if (registry == null) {
//			// Fix for Bug 2872447
//			// can't access the registry. Fail gracefully by returning an empty String.
//			// This will cause errors pointing the user to set the paths manually.
//			fWinAVRPath = fEmptyPath;
//			return fEmptyPath;
//		}
//
//		// get the newest installed version of winAVR.
//		// There may be multiple versions of winAVR installed.
//		// Grab all versions and sort them alphabetically go find the
//		// most recent version
//		int i = 0;
//		List<String> winavrkeys = new ArrayList<String>();
//		String nextkey = null;
//		do {
//			nextkey = registry.getKeyName("HKLM\\SOFTWARE\\WinAVR", i);
//			if (nextkey != null) {
//				winavrkeys.add(nextkey);
//				i++;
//			}
//		} while (nextkey != null);
//
//		if (winavrkeys.size() > 0) {
//			Collections.sort(winavrkeys);
//			String winavrkey = winavrkeys.get(winavrkeys.size() - 1);
//
//			String winavr = registry.getKeyValue("HKLM\\SOFTWARE\\WinAVR", winavrkey);
//			if (winavr != null) {
//				fWinAVRPath = new Path(winavr);
//				return fWinAVRPath;
//			}
//		}
//
//		// No "HKLM\Software\WinAVR" key in the registry
//		// Lets try another location: "HKLM\Software\Free Software Foundation\WinAVR-xxxxx"
//		//
//
//		List<String> allkeys = registry
//				.getSubkeys("HKLM\\SOFTWARE\\Free Software Foundation");
//		winavrkeys.clear();
//
//		for (String key : allkeys) {
//			if (key.contains("WinAVR")) {
//				winavrkeys.add(key);
//			}
//		}
//
//		if (winavrkeys.size() > 0) {
//			Collections.sort(winavrkeys);
//			String winavrkey = winavrkeys.get(winavrkeys.size() - 1);
//
//			String winavr = registry.getKeyValue(winavrkey, "GCC");
//			if (winavr != null) {
//				fWinAVRPath = new Path(winavr);
//				return fWinAVRPath;
//			}
//		}
//
//		// Couldn't find anything, so just return an empty path.
//		// This will cause errors pointing the user to set the paths manually.
//		fWinAVRPath = fEmptyPath;
//		return fWinAVRPath;
//	}

	/**
	 * Get the path to the Atmel AVR Tools base directory from the Windows registry.
	 * @return IPath with the current path to the AVR Tools base directory
	 */
	private static IPath getAVRToolsPath() {
		if (fAVRToolsPath != null) {
			return fAVRToolsPath;
		}
		MyWindowsRegistry registry = MyWindowsRegistry.getRegistry();
		if (registry == null) {
			// brak dostepu do rejestru. Zwraca "fail" - pusty ciag.
			// To spowoduje blad, by uzytkownik ustawil sobie sciezki recznie.
			fAVRToolsPath = fEmptyPath;
			return fEmptyPath;
		}
		
		fAVRToolsPath = fEmptyPath;
		String avrtools = registry.getKeyValue(
				"HKLM\\SOFTWARE\\Atmel\\AVRTools", "AVRToolsPath");
		if (avrtools != null) {
			fAVRToolsPath = new Path(avrtools);
		}
		return fAVRToolsPath;
	} // end of : private static IPath getAVRToolsPath() {

}
