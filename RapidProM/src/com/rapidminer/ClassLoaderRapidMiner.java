package com.rapidminer;

import java.util.List;

import com.rapidminer.tools.plugin.Plugin;

public class ClassLoaderRapidMiner {
	
	public static String ProM_RM_name = "RapidProM";
	
	public static ClassLoader getRapidMinerClassLoader () {
		// ProM
		List<Plugin> allPlugins = Plugin.getAllPlugins();
		for (Plugin plugin : allPlugins) {
			if (plugin.getName().equals(ProM_RM_name)) {
				return plugin.getClassLoader();
			}
		}
		return null;
	}
}
