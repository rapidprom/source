package org.rapidprom.operators.abstracts;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.events.Logger;
import org.processmining.framework.plugin.impl.PluginExecutionResultImpl;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.framework.providedobjects.SubstitutionType;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;

public abstract class AbstractProMOperator extends Operator {

	public AbstractProMOperator(OperatorDescription description) {
		super(description);
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
	protected PluginContext prepareChildContext(
			Class<?> classContainingProMPlugin) {
		final PluginContext result = ProMPluginContextManager
				.instance()
				.getContext()
				.createChildContext(
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
		ProMFuture<?>[] futures = new ProMFuture<?>[pluginAnn.returnTypes().length];
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

	/**
	 * This code is partly inherited from a previous version of RapidProm.
	 * (author R. Mans). It allows to invoke a plugin "by name". It is advised
	 * not to use this method as changes to a plugin's name are not visible at
	 * development time. Hence, accessing ProM code by means of this method
	 * leads to less fault intorlerant code.
	 * 
	 * @param pc
	 * @param nameChild
	 * @param namePlugin
	 * @param parameters
	 * @return
	 * 
	 * @see prepareChildContext
	 */
	@Deprecated
	public Object[] runPluginByName(PluginContext pc, String nameChild,
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
