package com.rapidminer.operator.importplugins;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;
import org.processmining.xeslite.plugin.OpenLogFileDiskImplPlugin;
import org.processmining.xeslite.plugin.OpenLogFileLiteImplPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.operators.abstracts.AbstractProMOperator;

import com.rapidminer.ioobjectrenderers.XLogIOObjectVisualizationType;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.parameters.ParameterCategory;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;

public class ImportXESLogOperator extends AbstractProMOperator {

	public enum ImplementingPlugin {
		LIGHT_WEIGHT_SEQ_ID("Lightweight & Sequential IDs",
				OpenLogFileLiteImplPlugin.class, "importFile", new Class<?>[] {
						PluginContext.class, File.class }), MAP_DB(
				"Buffered by MAPDB", OpenLogFileDiskImplPlugin.class,
				"importFile",
				new Class<?>[] { PluginContext.class, File.class }), NAIVE(
				"Naive", OpenNaiveLogFilePlugin.class, "importFile",
				new Class<?>[] { PluginContext.class, File.class });

		private final String name;

		private final Class<?> clazz;
		private final String method;
		private final Class<?>[] args;

		private ImplementingPlugin(final String name, Class<?> clazz,
				String method, Class<?>[] args) {
			this.name = name;
			this.clazz = clazz;
			this.method = method;
			this.args = args;
		}

		@Override
		public String toString() {
			return name;
		}

		public Class<?> getImplementingClass() {
			return clazz;
		}

		public String getImplementingMethod() {
			return method;
		}

		public Class<?>[] getMethodArguments() {
			return args;
		}
	}

	private static final String PARAMETER_LABEL_FILENAME = "Filename";
	private static final String PARAMETER_LABEL_IMPORTERS = "Importer";

	private Parameter importerParameter = null;
	private OutputPort output = getOutputPorts().createPort("Event Log (XLog)");

	public ImportXESLogOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(output, XLogIOObject.class));
	}

	protected boolean checkMetaData() throws UserError, UndefinedParameterError {
		boolean result = false;
		File file = getParameterAsFile(PARAMETER_LABEL_FILENAME);
		if (!file.exists()) {
			throw new UserError(this, "301", file);
		} else if (!file.canRead()) {
			throw new UserError(this, "302", file, "");
		} else {
			result = true;
		}
		return result;
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start importing event log");
		ImplementingPlugin importPlugin = (ImplementingPlugin) importerParameter
				.getValueParameter(getParameterAsInt(importerParameter
						.getNameParameter()));
		XLog log = null;
		if (checkMetaData()) {
			log = importLog(
					importPlugin,
					new Object[] {
							prepareChildContext(ImplementingPlugin.NAIVE
									.getImplementingClass()),
							getParameterAsFile(PARAMETER_LABEL_FILENAME) });
			XLogIOObject xLogIOObject = new XLogIOObject(log);
			xLogIOObject.setPluginContext(ProMPluginContextManager.instance()
					.getContext());
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.DEFAULT);
			output.deliver(xLogIOObject);
			ProMIOObjectList instance = ProMIOObjectList.getInstance();
			instance.addToList(xLogIOObject);
			logger.log(Level.INFO, "End importing .xes log");
		}
	}

	private XLog importLog(ImplementingPlugin p, Object[] args) {
		XLog result = null;
		try {
			Object importer = p.getImplementingClass().newInstance();
			Method importMethod = p.getImplementingClass().getMethod(
					p.getImplementingMethod(), p.getMethodArguments());
			result = (XLog) importMethod.invoke(importer, args);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result;

	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeFile logFileParameter = new ParameterTypeFile(
				PARAMETER_LABEL_FILENAME, "File to open", null, true, false);

		ParameterCategory importersParameterCategory = new ParameterCategory(
				EnumSet.allOf(ImplementingPlugin.class).toArray(),
				ImplementingPlugin.NAIVE, ImplementingPlugin.class,
				PARAMETER_LABEL_IMPORTERS, PARAMETER_LABEL_IMPORTERS);

		ParameterTypeCategory importersParameterTypeCategory = new ParameterTypeCategory(
				importersParameterCategory.getNameParameter(),
				importersParameterCategory.getDescriptionParameter(),
				importersParameterCategory.getOptionsParameter(),
				importersParameterCategory
						.getIndexValue(importersParameterCategory
								.getDefaultValueParameter()));
		parameterTypes.add(logFileParameter);
		parameterTypes.add(importersParameterTypeCategory);
		importerParameter = importersParameterCategory;
		return parameterTypes;
	}
}
