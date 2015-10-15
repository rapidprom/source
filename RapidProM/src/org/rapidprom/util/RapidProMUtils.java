package org.rapidprom.util;

import org.rapidprom.properties.RapidProMProperties;

import com.rapidminer.tools.plugin.Plugin;

public class RapidProMUtils {

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
