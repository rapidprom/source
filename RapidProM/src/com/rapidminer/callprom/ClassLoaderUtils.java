package com.rapidminer.callprom;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.processmining.framework.plugin.PluginManager;

import com.rapidminer.ClassLoaderRapidMiner;
import com.rapidminer.tools.plugin.Plugin;

public class ClassLoaderUtils {
	
	public static void loadJarsFromDir (File dir) {
		System.out.println("ADD JAR TO CLASSPATH, dir is:" + dir.getAbsolutePath());
		if (dir.exists()) {
			for (File f : dir.listFiles()) {
				if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
					URL url;
					try {
						url = f.toURI().toURL();
						System.out.println("ADD JAR TO CLASSPATH, url is:" + url.toString());
						addURLToClasspath(url);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
				else if (f.isDirectory()) {
					loadJarsFromDir (f);
				}
			}
		}
		else {
			System.out.println("FOLDER DOES NOT EXIST!");
		}
	}

	public static void addURLToClasspath(URL url) {
		try {
			URLClassLoader sysloader = (URLClassLoader) ClassLoaderRapidMiner.getRapidMinerClassLoader();
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class<?>[] { URL.class });

			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { url });
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
