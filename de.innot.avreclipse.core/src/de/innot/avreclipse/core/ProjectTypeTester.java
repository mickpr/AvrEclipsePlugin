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
package de.innot.avreclipse.core;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Special property tester for CDT/AVR Projects.
 * <p>
 * This tester can be used to test some properties of AVR projects and can be used in plugin.xml
 * Expressions.
 * </p>
 * <p>
 * Currently the following properties can be tested:
 * <ul>
 * <li><code>isStaticLib</code>: returns <code>true</code> if the given resource is an AVR static
 * library project.</li>
 * <li><code>isApp</code>: returns <code>true</code> if the given resource is an AVR application
 * project.</li>
 * </ul>
 * </p>
 * <p>
 * Example to inhibit a feature for a static library project:
 * 
 * <pre>
 * &lt;enabledWhen&gt;
 * 	&lt;not&gt;
 * 		&lt;test property="de.innot.avreclipse.core.isStaticLib" /&gt;
 * 	&lt;/not&gt;
 * &lt;/enabledWhen&gt;
 * </pre>
 * 
 * @author Thomas Holland
 * @since 2.3.2
 * 
 */
public class ProjectTypeTester extends PropertyTester {

	/** Keyword for the "isStaticLib" test. */
	private final static String	KEY_STATIC_LIB	= "isStaticLib";

	/** Keyword for the "isApp" test. */
	private final static String	KEY_APP			= "isApp";

	/**
	 * Default Constructor
	 */
	public ProjectTypeTester() {
		// nothing to initialize
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String,
	 * java.lang.Object[], java.lang.Object)
	 */
	// @Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		if (!(receiver instanceof IResource)) {
			// Can't handle non-IResources.
			return false;
		}

		if (KEY_STATIC_LIB.equalsIgnoreCase(property)) {
			IManagedProject p = getManagedProject((IResource) receiver);
			if (p != null) {
				IBuildObjectProperties props = p.getBuildProperties();
				IBuildProperty prop = props
						.getProperty("org.eclipse.cdt.build.core.buildArtefactType");

				// Bug 3023252: Makefile Projects don't have any properties, so prop may be null
				if (prop != null) {
					IBuildPropertyValue value = prop.getValue();
					if (value.getId().equals("de.innot.avreclipse.buildArtefactType.staticLib")) {
						return true;
					}
				}
			}
		}

		if (KEY_APP.equalsIgnoreCase(property)) {
			IManagedProject p = getManagedProject((IResource) receiver);
			if (p != null) {
				IBuildObjectProperties props = p.getBuildProperties();
				IBuildProperty prop = props
						.getProperty("org.eclipse.cdt.build.core.buildArtefactType");

				// Bug 3023252: Makefile Projects don't have any properties, so prop may be null
				if (prop != null) {
					IBuildPropertyValue value = prop.getValue();
					if (value.getId().equals("de.innot.avreclipse.buildArtefactType.app")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Get the IManagedProject for an IResource.
	 * <p>
	 * If the given resource is not or does not belong to a managed build project, then
	 * <code>null</code> is returned.
	 * </p>
	 * 
	 * @param resource
	 * @return managed build project object
	 */
	private IManagedProject getManagedProject(IResource resource) {
		IProject project = resource.getProject();
		if (project == null) {
			// Resource is not or does not belong to a project
			return null;
		}

		// Get the managed Project
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			// Project is not a managed build project
			return null;
		}

		IManagedProject p = buildInfo.getManagedProject();
		return p;
	}
}
