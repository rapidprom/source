package org.rapidprom.util;

import org.rapidprom.properties.RapidProMProperties;

import com.rapidminer.tools.plugin.Plugin;

public class RapidProMUtils {
	
	/**
	* This static code is called to make sure that the LpSolve library gets loaded.
	*/

	static private boolean lpSolveLoaded = false;

	static {
		if (!lpSolveLoaded) {
			init();
		}
	}

	public static void init() {
		System.loadLibrary("lpsolve55");
		System.loadLibrary("lpsolve55j");
		lpSolveLoaded = true;
	}

	public static Plugin getRapidProMPlugin() {
		for (Plugin plugin : Plugin.getAllPlugins()) {
			if (plugin.getName().equals(
					RapidProMProperties.instance().getProperties()
							.getProperty("extension.name"))) {
				return plugin;
			}
		}
		return null;
	}
}
