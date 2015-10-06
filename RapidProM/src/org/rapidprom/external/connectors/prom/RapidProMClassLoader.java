package org.rapidprom.external.connectors.prom;

import com.rapidminer.tools.plugin.Plugin;
import org.rapidprom.properties.RapidProMProperties;

import java.util.List;

public class RapidProMClassLoader {

    private static ClassLoader loader = null;

    public static ClassLoader getRapidMinerClassLoader() {
        if (loader == null) {
            List<Plugin> allPlugins = Plugin.getAllPlugins();
            for (Plugin plugin : allPlugins) {
                if (plugin.getName().equals(RapidProMProperties.instance().getProperties().getProperty("extension.name"))) {
                    loader = plugin.getClassLoader();
                }
            }
        }
        return loader;
    }
}
