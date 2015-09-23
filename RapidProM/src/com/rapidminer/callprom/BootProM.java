package com.rapidminer.callprom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.OsUtil;

import com.rapidminer.ClassLoaderRapidMiner;
import com.rapidminer.tools.LogService;


public class BootProM extends Boot {
	
	public static String ProMFolder = "RapidProM";
	
	private File promLocation = null;
	
	static {
		System.out.println("");
	}
	
	private void init() {
		LogService log = LogService.getGlobal();
		log.log("HERE bootprom", LogService.NOTE);
		
		
		Properties ini = new Properties();
		FileInputStream is = null;
		boolean notFound = false;
		try {
			//String prom_ini_location = System.getProperty("user.home", "").replace("/", File.separator) + File.separator + "ProM.ini";
			//is = new FileInputStream(prom_ini_location);
			is = new FileInputStream(promLocation);
		} catch (FileNotFoundException e) {
			notFound = true;
		}
		
		if (notFound) {
			// try in other folder
			try {
				is = new FileInputStream("ProM.ini");
			}
			catch (Exception e) {
				throw new RuntimeException("ProM.ini file not found. Exiting ProM.", e);
			}
		}

		try {
			ini.load(is);
			is.close();
		} catch (IOException e) {
			throw new RuntimeException("Error while reading ProM.ini file. Exiting ProM.", e);
		}

		if (!ini.containsKey("PROM_VERSION") || !ini.containsKey("RELEASE_PACKAGE")) {
			throw new RuntimeException("Error while reading ProM.ini file, missing required"
					+ " keys PROM_VERSION and or RELEASE_PACKAGE . Exiting ProM.");
		}

		PROM_VERSION = ini.getProperty("PROM_VERSION");

		RELEASE_PACKAGE = ini.getProperty("RELEASE_PACKAGE");

//		OPENXES_SHADOW_SIZE = Integer.parseInt(ini.getProperty("OPENXES_SHADOW_SIZE", "4"));
//		NikeFS2FileAccessMonitor.instance(OPENXES_SHADOW_SIZE);
		
		try {
			VERBOSE = Level.valueOf(ini.getProperty("VERBOSE", Level.ALL.name()));
		} catch (IllegalArgumentException e) {
			if (e.getMessage().toLowerCase().endsWith(".true")) {
				VERBOSE = Level.ALL;
			} else if (e.getMessage().toLowerCase().endsWith(".false")) {
				VERBOSE = Level.NONE;
			} else {
				throw e;
			}
		}

		LIB_FOLDER = ini.getProperty("LIB_FOLDER", "lib").replace("/", File.separator);
		PathHacker2.addLibraryPathFromDirectory(new File("." + File.separator + LIB_FOLDER));

		IMAGES_FOLDER = LIB_FOLDER + File.separator
				+ ini.getProperty("IMAGES_FOLDER", "images").replace("/", File.separator);
		PathHacker2.addLibraryPathFromDirectory(new File("." + File.separator + IMAGES_FOLDER));

		MACRO_FOLDER = LIB_FOLDER + File.separator
				+ ini.getProperty("MACRO_FOLDER", "macros").replace("/", File.separator);
		PathHacker2.addLibraryPathFromDirectory(new File("." + File.separator + MACRO_FOLDER));

		try {
			DEFAULT_REPOSITORY = new URL(ini.getProperty("PACKAGE_URL",
					"http://www.promtools.org/prom6/packages/packages.xml"));
		} catch (MalformedURLException e) {
			try {
				DEFAULT_REPOSITORY = new URL("http://www.promtools.org/prom6/packages/packages.xml");
			} catch (MalformedURLException e1) {
				assert (false);
			}
		}

		String prom_user_folder = ini.getProperty("PROM_USER_FOLDER", "").replace("/", File.separator);
		if (prom_user_folder.equals("")) {
			PROM_USER_FOLDER = System.getProperty("user.home", "") + File.separator + ".ProM";
		} else {
			PROM_USER_FOLDER = prom_user_folder;
		}

		PACKAGE_FOLDER = PROM_USER_FOLDER + File.separator
				+ ini.getProperty("PACKAGE_FOLDER", "packages").replace("/", File.separator);

		WORKSPACE_FOLDER = PROM_USER_FOLDER + File.separator
				+ ini.getProperty("WORKSPACE_FOLDER", "workspace").replace("/", File.separator);

		if (VERBOSE == Level.ALL) {
			System.out.println("Ini file processed");
		}

	}
	
	public BootProM(File promLocation) {
		this.promLocation = promLocation;
		init();
	}

	public Object bootForSWF(Class<?> bootClass, Class<? extends PluginContext> pluginContextClass, String... args) throws Exception{
		boolean wrong = false;
		File f = null;
		String libPath = "";
		try {
			f = new File("." + File.separator + LIB_FOLDER);
			libPath = f.getCanonicalPath();
		}
		catch (Exception e) {
			wrong = true;
		}
		
		if (wrong) {
			try {
				f = new File(LIB_FOLDER);
				libPath = f.getCanonicalPath();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("libPath:" + libPath);
		long start = System.currentTimeMillis();
		// First instantiate the package manager
		PackageManager packages = PackageManager.getInstance();
		// Then the plugin manager, as it listens to the package manager
		PluginManagerImpl.initialize(pluginContextClass);
		PluginManager plugins = PluginManagerImpl.getInstance();
		
		System.out.println("plugins:" + plugins.getAllPlugins().size());

		OsUtil.setWorkingDirectoryAtStartup();

		long startPackages = System.currentTimeMillis();
		packages.initialize(VERBOSE);
		if (VERBOSE == Level.ALL) {
			System.out.println(">>> Scanning for packages took " + (System.currentTimeMillis() - startPackages)
					/ 1000.0 + " seconds");
		}

		long startPlugins = System.currentTimeMillis();
		URLClassLoader sysloader = (URLClassLoader) ClassLoaderRapidMiner.getRapidMinerClassLoader();
		/*
		System.out.println("ONE START");
		listLoadedClasses(ClassLoaderRapidMiner.getRapidMinerClassLoader());
		System.out.println("ONE END");
		*/
		URL[] defaultURLs = sysloader.getURLs();

		
		// try to add jars to classpath
		System.out.println("LIB FOLDER:" + LIB_FOLDER);
		File dirProMJars = new File(LIB_FOLDER + File.separator + ProMFolder);
		loadJarsFromDir(dirProMJars);
		
		if (VERBOSE == Level.ALL) {
			for (URL def : defaultURLs) {
				System.out.println("known jar file: " + def);
			}
			System.out.println("Loading plugins from packages.");
		}
		
		for (PackageDescriptor pack : packages.getEnabledPackages()) {
			if (VERBOSE == Level.ALL) {
				System.out.println("Processing Package: " + pack.getName());
			}
			addJarsForPackage(pack, VERBOSE, plugins);
		}

		if (VERBOSE == Level.ALL) {
			System.out.println("Loading from: classpath");
		}
		
		//TEST
		/*
		File f = new File("." + File.separator + LIB_FOLDER);
		String libPath = f.getCanonicalPath();
		addURLToClasspath(f.toURI().toURL());
		for (URL url : defaultURLs) {
			if (VERBOSE == Level.ALL) {
				System.out.println("Processing url: " + url);
			}
			if (!(new File(url.toURI()).getCanonicalPath().startsWith(libPath))) {
				if (VERBOSE == Level.ALL) {
					System.out.println("Scanning for plugins: " + url);
				}
				plugins.register(url, null);
			} else {
				if (VERBOSE == Level.ALL) {
					System.out.println("Skipping: " + url.getFile() + " while scanning for plugins.");
				}
			}
		}
		*/
		//
		
		if (VERBOSE == Level.ALL) {
			System.out.println(">>> Scanning for plugins took " + (System.currentTimeMillis() - startPlugins) / 1000.0
					+ " seconds");
		}
		
		System.out.println("plugins:" + plugins.getAllPlugins().size());

		Object obj = bootSWF(bootClass, args);
		
		if (VERBOSE == Level.ALL) {
			System.out.println(">>> Total startup took " + (System.currentTimeMillis() - start) / 1000.0 + " seconds");
		}
		
		return obj;
	}
	
	public static void addJarsForPackage(PackageDescriptor pack, Boot.Level verbose, PluginManager plugins) {
		if (verbose == Level.ALL) {
			System.out.println("Scanning package: " + pack);
		}
		File dir = pack.getLocalPackageDirectory();
		if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
			if (verbose == Level.ALL) {
				System.out.println("  Error: package directory does not exist: " + dir);
			}
			return;
		}
		// First, recusively iterate subfolders, where no scanning for plugins is necessary
		// this ensures all requires libraries are known when scanning for plugins
		for (File f : dir.listFiles()) {
			// Scan for jars. Only jars in the root of the package will be scanned for
			// plugins and other annotations.
			if (f.isDirectory()) {
				addJarsFromPackageDirectory(f, verbose, plugins);
				try {
					addURLToClasspath(f.toURI().toURL());
				} catch (MalformedURLException e) {
				}
			}
		}
		// Now scan the jar files in the package root folder.
		for (File f : dir.listFiles()) {
			if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
				URL url;
				try {
					url = f.toURI().toURL();
					if (verbose == Level.ALL) {
						System.out.println("  scanning for plugins: " + url);
					}
					addURLToClasspath(url);
					if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
						//System.out.println("REGISTER");
						//System.out.println(url);
						//System.out.println(pack);
						//plugins.register(url, pack);
						plugins.register(url, pack, ClassLoaderRapidMiner.getRapidMinerClassLoader());
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

		}
		PathHacker2.addLibraryPathFromDirectory(pack.getLocalPackageDirectory());
		try {
			PathHacker2.addJar(pack.getLocalPackageDirectory().toURI().toURL());
			for (File f : pack.getLocalPackageDirectory().listFiles()) {
				if (f.isDirectory()) {
					PathHacker2.addJar(f.toURI().toURL());
				}
			}
		} catch (MalformedURLException e) {
			assert (false);
		}

	}
	
	public static void listLoadedClasses(ClassLoader byClassLoader) {
		int counter = 0;
	    Class clKlass = byClassLoader.getClass();
	    System.out.println("Classloader: " + clKlass.getCanonicalName());
	    while (clKlass != java.lang.ClassLoader.class) {
	        clKlass = clKlass.getSuperclass();
	    }
	    try {
	        java.lang.reflect.Field fldClasses = clKlass
	                .getDeclaredField("classes");
	        fldClasses.setAccessible(true);
	        Vector classes = (Vector) fldClasses.get(byClassLoader);
	        for (Iterator iter = classes.iterator(); iter.hasNext();) {
	            System.out.println("   Loaded " + iter.next());
	            counter++;
	        }
	    } catch (SecurityException e) {
	        e.printStackTrace();
	    } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	    } catch (NoSuchFieldException e) {
	        e.printStackTrace();
	    } catch (IllegalAccessException e) {
	        e.printStackTrace();
	    }
	    System.out.println("counter is " + counter);
	}
	
	public Object bootSWF(Class<?> bootClass, String... args) throws Exception {
		
		Method bootMethod = null;
		System.out.println("BOOTCLASS:" + bootClass);
		for (Method method : bootClass.getMethods()) {
			if (method.isAnnotationPresent(Bootable.class)) {
				if (bootMethod == null) {
					bootMethod = method;
				} else {
					throw new IllegalArgumentException("Cannot have more than one @Bootable method in a class");
				}
			}
		}
		if (bootMethod == null) {
			throw new IllegalArgumentException("No @Bootable annotation found: " + bootClass.getName());
		}

		CommandLineArgumentList argList = new CommandLineArgumentList();
		for (String arg : args) {
			argList.add(arg);
		}

		//Object newInstance = bootMethod.getDeclaringClass().newInstance();
		//return bootMethod.invoke(newInstance, argList);
		return bootMethod.invoke(bootMethod.getDeclaringClass().newInstance(), argList);
	}
		
		/**
		 * Scan for jars and add them to the classpath.
		 * 
		 * @param dir
		 *            the folder (or jar file) to scan
		 * @param verbose
		 *            true if output required
		 * @param plugins
		 *            the plugin manager
		 * @param scanClasses
		 *            If true, then all classes are scanned for annotations and for
		 *            plugins. This property recusively propagates to sub-folders.
		 */
		private static void addJarsFromPackageDirectory(File dir, Boot.Level verbose, PluginManager plugins) {

			for (File f : dir.listFiles()) {
				if (f.isDirectory()) {
					addJarsFromPackageDirectory(f, verbose, plugins);
				}
			}
			for (File f : dir.listFiles()) {
				if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
					try {
						URL url = f.toURI().toURL();
						if (verbose == Level.ALL) {
							System.out.println("  adding to classpath: " + url);
						}
						addURLToClasspath(url);
					} catch (MalformedURLException e) {
						e.printStackTrace();

					}
				}
			}
		}
		
		private static void loadJarsFromDir (File dir) {
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
	
				}
			}
			else {
				System.out.println("FOLDER DOES NOT EXIST!");
			}
		}

		private static void addURLToClasspath(URL url) {
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
