package org.rapidprom.prom;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.events.Logger;
import org.processmining.framework.providedobjects.SubstitutionType;
import org.processmining.framework.util.Pair;
import org.rapidprom.external.connectors.prom.RapidProMCLI;

import com.rapidminer.callprom.BootProM;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;

@Deprecated
public class CallProm {

	@Deprecated
	public CallProm() {
	}

	@Deprecated
	public CLIPluginContext instantiateProMContext(File promLocation) {
		return instantiateCLIContext(promLocation);
	}

	@Deprecated
	public Object[] runPlugin(PluginContext pc, String nameChild,
			String namePlugin, List<Object> parameters) {

		PluginExecutionResult result = null;
		Logger logger = new ChildLogger();
		final PluginContext child = pc.createChildContext(nameChild);
		pc.getPluginLifeCycleEventListeners().firePluginCreated(child);
		child.getLoggingListeners().add(logger);

		Object[] u = new Object[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			u[i] = parameters.get(i);
		}

		Class<?>[] t = new Class<?>[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			t[i] = getSubstitutedType(parameters.get(i).getClass());
		}
		PluginManager pm = pc.getPluginManager();
		Set<PluginParameterBinding> plugins = pm.getPluginsAcceptingOrdered(
				child.getClass(), false, t);
		System.out.println("TEST");
		System.out.println("PRINT PLUGINS");
		for (PluginParameterBinding pBinding : plugins) {
			System.out.println("plugin: " + pBinding.getPlugin().getName());
			if (pBinding.getPlugin().getName().equals(namePlugin)) {
				try {
					result = pBinding.invoke(child, u);
					pc.getProvidedObjectManager().createProvidedObjects(child);
				} catch (Exception e) {
					e.printStackTrace();
					pc.clear();
					pc.getProvidedObjectManager().clear();
					pc.getConnectionManager().clear();
					return null;
				}
				try {
					result.synchronize();
				} catch (CancellationException e) {
					pc.getFutureResult(0).cancel(true);
					e.printStackTrace();
				} catch (ExecutionException e) {
					pc.getFutureResult(0).cancel(true);
					e.printStackTrace();
				} catch (InterruptedException e) {
					pc.getFutureResult(0).cancel(true);
					e.printStackTrace();
				}
				break;
			}
		}
		Object[] results = null;
		try {
			results = result.getResults();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pc.clear();
		pc.getProvidedObjectManager().clear();
		pc.getConnectionManager().clear();
		pc.setFuture(null);
		child.setFuture(null);
		if (results == null) {
			JOptionPane.showMessageDialog(null, "Plugin " + namePlugin
					+ " did not return any result as an error occurred!");
		}
		return results;
	}

	@Deprecated
	public JComponent runVisualizationPlugin(PluginContext pc,
			String nameChild, List<Object> parameters) {

		Object[] u = new Object[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			u[i] = parameters.get(i);
		}

		Class<?>[] t = new Class<?>[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			t[i] = getSubstitutedType(parameters.get(i).getClass());
		}

		try {
			JComponent promVis = pc.tryToFindOrConstructFirstObject(
					JComponent.class, Connection.class, "", u);
			return promVis;
		} catch (ConnectionCannotBeObtained e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@Deprecated
	public JComponent runVisualizationPluginUsingName(PluginContext pc,
			String nameChild, String namePlugin, List<Object> parameters) {

		Object[] u = new Object[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			u[i] = parameters.get(i);
		}

		Class<?>[] t = new Class<?>[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			t[i] = getSubstitutedType(parameters.get(i).getClass());
		}

		try {
			JComponent promVis = pc.tryToFindOrConstructFirstNamedObject(
					JComponent.class, namePlugin, Connection.class, "u", u);
			return promVis;
		} catch (ConnectionCannotBeObtained e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private Class<?> getSubstitutedType(Class<?> type) {
		if (type.isAnnotationPresent(SubstitutionType.class)) {
			Class<?> declaredType = type.getAnnotation(SubstitutionType.class)
					.substitutedType();
			if (declaredType.isAssignableFrom(type)) {
				return declaredType;
			}
		}
		return type;
	}

	private CLIPluginContext instantiateCLIContext(File promLocation) {
		try {
			java.util.logging.Logger logger = LogService.getRoot();
			logger.log(Level.INFO, "Call ProM: instantiating cli context");
			BootProM bswf = new BootProM(promLocation);
			String[] args = new String[0];
			Object obj;
			obj = bswf.bootForSWF(RapidProMCLI.class, CLIPluginContext.class,
					args);
			return (CLIPluginContext) obj;
		} catch (Throwable e) {
			e.printStackTrace();
			java.util.logging.Logger logger = LogService.getRoot();
			logger.log(Level.SEVERE, e.getMessage());
		}
		return null;
	}

	public static void main(String[] args) throws Throwable {
		String path = "c:\\data\\test\\prom.ini";
		path = path.replace("\\", "/");
		System.out.println(path);
	}

	static class ChildLogger implements Logger {

		public void log(String message, PluginContextID contextID,
				MessageLevel messageLevel) {
			// NOP
		}

		public void log(Throwable t, PluginContextID contextID) {
			// NOP
		}

	}

}
