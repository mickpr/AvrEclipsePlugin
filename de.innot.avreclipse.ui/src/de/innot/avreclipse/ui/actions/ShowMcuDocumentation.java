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
package de.innot.avreclipse.ui.actions;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.progress.UIJob;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.avrdude.AVRDudeAction;
import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.AVRDudeSchedulingRule;
import de.innot.avreclipse.core.avrdude.BaseBytesProperties;
import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.properties.AVRProjectProperties;
import de.innot.avreclipse.core.properties.ProjectPropertyManager;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.core.toolinfo.fuses.FuseType;
import de.innot.avreclipse.core.util.AVRMCUidConverter;
import de.innot.avreclipse.devexp.PluginPreferences;
import de.innot.avreclipse.mbs.BuildMacro;
import de.innot.avreclipse.ui.dialogs.AVRDudeErrorDialogJob;

/**
 * @author Thomas Holland
 * @since 2.2
 * @since 2.3 Added optional delay between avrdude invocations
 * 
 */
public class ShowMcuDocumentation extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	private IProject			fProject;

	private final static String	TITLE_SHOW_DOCUMENTATION	= "Show documentation";	
	private final static String	MSG_NOPROJECT				= "No AVR project selected";
	
	/**
	 * Constructor for this Action.
	 */
	public ShowMcuDocumentation() {
		super();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		// The user has selected a different Workbench object.
		// If it is an IProject we keep it.

		Object item;

		if (selection instanceof IStructuredSelection) {
			item = ((IStructuredSelection) selection).getFirstElement();
		} else {
			return;
		}
		if (item == null) {
			return;
		}
		IProject project = null;

		// See if the given is an IProject (directly or via IAdaptable)
		if (item instanceof IProject) {
			project = (IProject) item;
		} else if (item instanceof IResource) {
			project = ((IResource) item).getProject();
		} else if (item instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) item;
			project = (IProject) adaptable.getAdapter(IProject.class);
			if (project == null) {
				// Try ICProject -> IProject
				ICProject cproject = (ICProject) adaptable.getAdapter(ICProject.class);
				if (cproject == null) {
					// Try ICElement -> ICProject -> IProject
					ICElement celement = (ICElement) adaptable.getAdapter(ICElement.class);
					if (celement != null) {
						cproject = celement.getCProject();
					}
				}
				if (cproject != null) {
					project = cproject.getProject();
				}
			}
		}

		fProject = project;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {

		// Check that we have a AVR Project
		try {
			if (fProject == null || !fProject.hasNature("de.innot.avreclipse.core.avrnature")) {
				MessageDialog.openError(getShell(), TITLE_SHOW_DOCUMENTATION, MSG_NOPROJECT);
				return;
			}
		} catch (CoreException e) {
			// Log the Exception
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
					"Can't access project nature", e);
			AVRPlugin.getDefault().log(status);
		}

		// Get the active build configuration
		IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(fProject);
		IConfiguration activecfg = bi.getDefaultConfiguration();

		// Get the avr properties for the active configuration
		AVRProjectProperties targetprops = ProjectPropertyManager.getPropertyManager(fProject)
				.getActiveProperties();

		String selectedChipName = PluginPreferences.get("MCUType","ERR");
		
		if (!selectedChipName.equals("ERR")) {
			
			try {
				String url_string; // contain url string for documentation portal (if web page exist)
				
				//create desired link for documentation web page for given MCU
				url_string = new String();
				url_string= "https://www.microchip.com/wwwproducts/en/";
				url_string=url_string+ selectedChipName.toUpperCase(); // eg. https://www.microchip.com/wwwproducts/en/ATMEGA88 
				boolean webPageExist=false;
				
				//check if filepage exists
			    try {
			      HttpURLConnection.setFollowRedirects(false);
			      // note : you may also need
			      //        HttpURLConnection.setInstanceFollowRedirects(false)
			      HttpURLConnection con = (HttpURLConnection) new URL(url_string).openConnection();
			      con.setRequestMethod("HEAD");
			      webPageExist= (con.getResponseCode() == HttpURLConnection.HTTP_OK);
			    }
			    catch (Exception eee) {
			       eee.printStackTrace();
			       webPageExist=false;
			    }
			    if (webPageExist) 
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url_string));
			    
			} catch (PartInitException e1) {
				e1.printStackTrace();
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} // end of try
	
			
			
		} // end of if..
	}


	/**
	 * Get the current Shell.
	 * 
	 * @return <code>Shell</code> of the active Workbench window.
	 */
	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
