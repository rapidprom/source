package com.rapidminer.promcontext;

import java.io.File;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.prom.CallProm;

import com.rapidminer.configuration.GlobalProMParameters;

public class ProMContextSingleton {

	private static ProMContextSingleton singleton = new ProMContextSingleton();

	private static String nameContext = "context";
	private static PluginContext context = null;

	private ProMContextSingleton() {
	}

	public static ProMContextSingleton getInstance() {
		if (singleton == null) {
			// start ProM
			System.out.println("START INSTANCE OF PROM");
			// get parameters
			File promLocation = null;
			GlobalProMParameters instance = GlobalProMParameters.getInstance();
			String promLocationStr = instance.getProMLocation();
			System.out.println("promLocationStr:" + promLocationStr);
			// the location of prom.ini
			promLocation = new File(promLocationStr);
			CallProm tp = new CallProm();
			CLIPluginContext promContext = tp
					.instantiateProMContext(promLocation);
			PluginContext childContext = promContext
					.createChildContext(nameContext);
			context = childContext;
		}
		return singleton;
	}

	public PluginContext getContext() {
		return context;
	}

}
