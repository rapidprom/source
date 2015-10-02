package org.rapidprom.external.connectors.prom;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.impl.PluginExecutionResultImpl;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.PathHacker;
import org.rapidprom.properties.RapidProMProperties;

import com.rapidminer.ClassLoaderRapidMiner;
import com.rapidminer.gui.tools.ProgressThread;

/**
 * The ProM Plugin Context Manager is a singleton class that provides a Plugin
 * Context to be used internally within RapidProM. The PluginContext object is
 * only functional when explicitly bound to a set of "installed ProM packages",
 * hence a vast amount of code within the context manager shares a great deal of
 * similarity with the ordinary ProM boot script. However, this particular
 * script is modified to be able to handle ivy-based ProM plugin/package
 * definitions.
 * 
 * @author svzelst
 *
 */
public class ProMPluginContextManager extends ProgressThread {

	private static ProMPluginContextManager instance = null;

	private PluginContext context;

	private Boot.Level verbose = Level.NONE;

	private ProMPluginContextManager() {
		super("LOAD_PROM_PACKAGES", false);
	}

	@Override
	public void run() {
		setup();
	}

	public static ProMPluginContextManager instance() {
		if (instance == null) {
			instance = new ProMPluginContextManager();
		}
		return instance;
	}

	public static ProMPluginContextManager instance(Boot.Level verbose) {
		instance();
		instance.setVerboseLevel(verbose);
		return instance;
	}

	public void setVerboseLevel(Boot.Level verbose) {
		this.verbose = verbose;
	}

	protected void setup() {
		context = promPackageLoader();
	}

	public PluginContext getContext() {
		return context;
	}

	private PluginContext promPackageLoader() {
		PluginContext result = null;
		PathHacker.addLibraryPathFromDirectory(new File(RapidProMProperties
				.instance().getRapidProMPackagesLocationString()));
		addJarsFromDirectory(new File(RapidProMProperties.instance()
				.getRapidProMPackagesLocationString()));
		setupProMAnnotationAwareness();
		try {
			result = setupPluginContext(RapidProMCLI.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void setupProMAnnotationAwareness() {
		ProMIvyBasedPackageManager packages = ProMIvyBasedPackageManager
				.getInstance();
		PluginManagerImpl.initialize(CLIPluginContext.class);
		PluginManager plugins = PluginManagerImpl.getInstance();
		// OsUtil.setWorkingDirectoryAtStartup();
		packages.initialize(verbose);
		for (PackageDescriptor p : packages.getPackages()) {
			System.out.println("Processing Package: " + p.getName());
			findAndRegisterProMPluginAnnotations(p.getLocalPackageDirectory(),
					p, verbose, plugins);
		}
	}

	private void findAndRegisterProMPluginAnnotations(File dir,
			PackageDescriptor pack, Boot.Level verbose, PluginManager plugins) {

		for (File f : dir.listFiles()) {
			if (f.isDirectory() && f.exists() && f.canRead()) {
				findAndRegisterProMPluginAnnotations(f, pack, verbose, plugins);
			}
		}
		for (File f : dir.listFiles()) {
			if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)
					&& f.getName().contains(pack.getName())) {
				try {
					URL url = f.toURI().toURL();
					if (f.getName().contains(pack.getName())) {
						System.out.println("Regiser Plugin " + f.getName());
						plugins.register(url, pack, ClassLoaderRapidMiner
								.getRapidMinerClassLoader());
					}
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

	private PluginContext setupPluginContext(Class<?> bootClass, String... args)
			throws Exception {

		Method bootMethod = null;
		System.out.println("BOOTCLASS:" + bootClass);
		for (Method method : bootClass.getMethods()) {
			if (method.isAnnotationPresent(Bootable.class)) {
				if (bootMethod == null) {
					bootMethod = method;
				} else {
					throw new IllegalArgumentException(
							"Cannot have more than one @Bootable method in a class");
				}
			}
		}
		if (bootMethod == null) {
			throw new IllegalArgumentException(
					"No @Bootable annotation found: " + bootClass.getName());
		}
		CommandLineArgumentList argList = new CommandLineArgumentList();
		for (String arg : args) {
			argList.add(arg);
		}
		return (PluginContext) bootMethod
				.invoke(bootMethod.getDeclaringClass().newInstance(), argList);
	}

	private void addJarsFromDirectory(File dir) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				addJarsFromDirectory(f);
			}
		}
		for (File f : dir.listFiles()) {
			if (f.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
				try {
					URL url = f.toURI().toURL();
					System.out.println("  adding to classpath: " + url);
					addURLToClasspath(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addURLToClasspath(URL url) {
		try {
			URLClassLoader sysloader = (URLClassLoader) ClassLoaderRapidMiner
					.getRapidMinerClassLoader();
			Method method = URLClassLoader.class.getDeclaredMethod("addURL",
					new Class<?>[] { URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { url });
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * This method prepares a PluginContext object, which is a child object of
	 * the PluginContext provided by the "PluginContextManager". Basically this
	 * method mimics some of the internal workings of the ProM framework, e.g.
	 * setting the future result objects.
	 * 
	 * @param classContainingProMPlugin
	 *            the class that contains the ProM plugin code
	 * @return
	 */
	public PluginContext getFutureResultAwareContext(
			Class<?> classContainingProMPlugin) {
		final PluginContext result = ProMPluginContextManager.instance()
				.getContext().createChildContext(
						"RapidProMPluginContext_" + System.currentTimeMillis());
		Plugin pluginAnn = findAnnotation(
				classContainingProMPlugin.getAnnotations(), Plugin.class);

		PluginExecutionResult per = new PluginExecutionResultImpl(
				pluginAnn.returnTypes(), pluginAnn.returnLabels(),
				PluginManagerImpl.getInstance().getPlugin(
						classContainingProMPlugin.getCanonicalName()));
		ProMFuture<?>[] futures = createProMFutures(pluginAnn);
		Method m;
		try {
			m = PluginExecutionResultImpl.class.getDeclaredMethod("setResult",
					Object[].class);
			m.setAccessible(true);
			m.invoke(per, new Object[] { futures });
			result.setFuture(per);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result;
	}

	private ProMFuture<?>[] createProMFutures(Plugin pluginAnn) {
		ProMFuture<?>[] futures = new ProMFuture<?>[pluginAnn
				.returnTypes().length];
		for (int i = 0; i < pluginAnn.returnTypes().length; i++) {
			futures[i] = new ProMFuture<Object>(pluginAnn.returnTypes()[i],
					pluginAnn.returnLabels()[i]) {
				@Override
				protected Object doInBackground() throws Exception {
					// NOP
					return null;
				}
			};
		}
		return futures;
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> T findAnnotation(Annotation[] annotations,
			Class<T> clazz) {
		T result = null;
		for (Annotation a : annotations) {
			if (a.annotationType().equals(clazz)) {
				result = (T) a;
				break;
			}
		}
		return result;
	}
}
