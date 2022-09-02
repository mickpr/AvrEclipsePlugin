package de.innot.avreclipse.devexp;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class PluginPreferences {
	private static IEclipsePreferences preferences; 
	
	
	public static boolean SwitchProject(String projectName) {
		
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IWorkspaceRoot root = workspace.getRoot();
	    IProject project = root.getProject(projectName); // wskazany otwarty projekt
	    PluginPreferences.preferences = getPreferences(project);
	    // return true if project is open, otherwise return false
	    return project.isOpen();
	}
	
	public static String get(String name) {
		return preferences.get(name, "");
	}
	
	public static String get(String name, String defaultValue) {
		return preferences.get(name, defaultValue);
	}
	
	
	public static void set(String key, String value) {
		preferences.put(key, value);
		try {
			preferences.flush(); 
			preferences.sync(); }
		catch (Exception e) { 
			e.printStackTrace();
		}
	}
	
	private static IEclipsePreferences getPreferences(IProject project) {
		IScopeContext scopeContext;
		if (project == null) {
			scopeContext=InstanceScope.INSTANCE;
		}
		else {
			scopeContext=new ProjectScope(project);
		}
		IEclipsePreferences prefs=scopeContext.getNode("de.innot.avreclipse.core/avrtarget");
		return prefs;
	}
}
