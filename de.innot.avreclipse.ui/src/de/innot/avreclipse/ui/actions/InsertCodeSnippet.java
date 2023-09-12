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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.devexp.PluginPreferences;

/**
 * @author Michal Przybyl
 */
public class InsertCodeSnippet extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	private IProject			fProject;

	private final static String	TITLE_SHOW_DOCUMENTATION	= "InsertCodeSnippet";	
	private final static String	MSG_NOPROJECT			= "No AVR project selected";
	
	/**
	 * Constructor for this Action.
	 */
	public InsertCodeSnippet() {
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

		insertText("Dupa mariana");
		
		// Get the active build configuration
//		IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(fProject);
//		IConfiguration activecfg = bi.getDefaultConfiguration();

		// Get the avr properties for the active configuration
//		AVRProjectProperties targetprops = ProjectPropertyManager.getPropertyManager(fProject)
//				.getActiveProperties();

//		PluginPreferences.SwitchProject(fProject.getName());
//		String selectedChipName = PluginPreferences.get("MCUType","ERR");
//		
//		if (!selectedChipName.equals("ERR")) {
//			
//			try {
//				String url_string; // contain url string for documentation portal (if web page exist)
//		
//			
//				//create desired link for documentation web page for given MCU
//				url_string = new String();
//				url_string= "http://www.microchip.com/wwwproducts/en/";
//				url_string=url_string+ selectedChipName.toUpperCase(); // eg. https://www.microchip.com/wwwproducts/en/ATMEGA88
//				//System.out.println(url_string);
//				
//				boolean webPageExist=false;
//				webPageExist=true;
//				/* mickpr- 
//				//check if filepage exists
//			    try {
//			      HttpURLConnection.setFollowRedirects(false);
//			      // note : you may also need
//			      //  HttpURLConnection.setInstanceFollowRedirects(false)
//			      HttpURLConnection con = (HttpURLConnection) new URL(url_string).openConnection();
//			      
//			      con.setRequestMethod("HEAD");
//			      webPageExist= (con.getResponseCode() <= 400);  // od 400 zaczynaja sie kody bledow
//			      
//			    }
//			    catch (Exception eee) {
//			       eee.printStackTrace();
//			       webPageExist=false;
//			    }
//			    */
//			    if (webPageExist) 
//					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url_string));
//			    
//			} catch (PartInitException e1) {
//				e1.printStackTrace();
//			} catch (MalformedURLException e1) {
//				e1.printStackTrace();
//			} // end of try
//		} // end of if..
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

	//insert code in editor place by mickpr
	private static void insertText(String text)
	{
		if(text == null) return;

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage pg = win.getActivePage();
		IEditorPart editor = pg.getActiveEditor();
		
		if(editor == null) return;

		if(!(editor instanceof TextEditor)) return;
		
		TextEditor textEditor = (TextEditor) editor;
		
		IDocumentProvider dp = textEditor.getDocumentProvider();
		if(dp == null) return;

		IDocument doc = dp.getDocument(editor.getEditorInput());
		if(doc == null) return;
		
		ISelection sel = ((AbstractTextEditor) editor).getSelectionProvider().getSelection();
		if((sel == null) || (!(sel instanceof ITextSelection ))) return;

		ITextSelection txtSel = (ITextSelection) sel;

		int ofs = txtSel.getOffset();
		int len = txtSel.getLength();

		try
		{
			doc.replace(ofs, len, text);
			editor.setFocus();
		}
		catch (Exception ignore) {};
		} // func	
	
	}
