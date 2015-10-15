package org.rapidprom.external.connectors.prom;

import org.rapidprom.util.RapidProMUtils;

public class RapidProMClassLoader {

    private static ClassLoader loader = null;

    public static ClassLoader getRapidMinerClassLoader() {
        if (loader == null) {
            loader = RapidProMUtils.getRapidProMPlugin().getClassLoader();
        }
        return loader;
    }
}
