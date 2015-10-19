package org.rapidprom.operator.abstr;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;
import org.processmining.xeslite.plugin.OpenLogFileDiskImplPlugin;
import org.processmining.xeslite.plugin.OpenLogFileLiteImplPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.parameters.ParameterCategory;
import com.rapidminer.tools.LogService;

public abstract class AbstractImportXLogOperator
		extends AbstractRapidProMImportOperator {

	public AbstractImportXLogOperator(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(output, XLogIOObject.class));
	}

	protected OutputPort output = getOutputPorts()
			.createPort("event Log (XLog)");

	protected Parameter importerParameter = null;

	protected abstract File getFile() throws UserError;

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: importing event log");
		long time = System.currentTimeMillis();

		ImplementingPlugin importPlugin = (ImplementingPlugin) importerParameter
				.getValueParameter(getParameterAsInt(
						importerParameter.getNameParameter()));
		XLog log;
		if (checkFileParameterMetaData(PARAMETER_LABEL_FILENAME)) {
			log = importLog(importPlugin, getFile());
			XLogIOObject xLogIOObject = new XLogIOObject(log,ProMPluginContextManager.instance().getContext());
			xLogIOObject.setVisualizationType(
					XLogIOObjectVisualizationType.DEFAULT);
			output.deliver(xLogIOObject);

			logger.log(Level.INFO, "End: importing event log ("
					+ (System.currentTimeMillis() - time) / 1000 + " sec)");
		}
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterCategory importersParameterCategory = new ParameterCategory(
				EnumSet.allOf(ImplementingPlugin.class).toArray(),
				ImplementingPlugin.NAIVE, ImplementingPlugin.class,
				PARAMETER_LABEL_IMPORTERS, PARAMETER_LABEL_IMPORTERS);

		ParameterTypeCategory importersParameterTypeCategory = new ParameterTypeCategory(
				importersParameterCategory.getNameParameter(),
				importersParameterCategory.getDescriptionParameter(),
				importersParameterCategory.getOptionsParameter(),
				importersParameterCategory.getIndexValue(
						importersParameterCategory.getDefaultValueParameter()));
		importersParameterTypeCategory.setHidden(false);
		parameterTypes.add(importersParameterTypeCategory);
		importerParameter = importersParameterCategory;
		return parameterTypes;
	}

	private XLog importLog(ImplementingPlugin p, File file) {
		XLog result = null;
		switch (p) {
		case LIGHT_WEIGHT_SEQ_ID:
			result = importLeightWeight(file);
			break;
		case MAP_DB:
			result = importMapDb(file);
			break;
		case NAIVE:
		default:
			result = importLogNaive(file);
			break;
		}
		return result;
	}

	private XLog importLeightWeight(File file) {
		XLog result = null;
		OpenLogFileLiteImplPlugin plugin = new OpenLogFileLiteImplPlugin();
		try {
			result = (XLog) plugin
					.importFile(
							ProMPluginContextManager.instance()
									.getFutureResultAwareContext(
											OpenLogFileLiteImplPlugin.class),
							file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private XLog importMapDb(File file) {
		XLog result = null;
		OpenLogFileDiskImplPlugin plugin = new OpenLogFileDiskImplPlugin();
		try {
			result = (XLog) plugin
					.importFile(
							ProMPluginContextManager.instance()
									.getFutureResultAwareContext(
											OpenLogFileDiskImplPlugin.class),
							file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private XLog importLogNaive(File file) {
		XLog result = null;
		OpenNaiveLogFilePlugin plugin = new OpenNaiveLogFilePlugin();
		try {
			result = (XLog) plugin
					.importFile(
							ProMPluginContextManager.instance()
									.getFutureResultAwareContext(
											OpenNaiveLogFilePlugin.class),
							file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	protected enum ImplementingPlugin {
		LIGHT_WEIGHT_SEQ_ID("Lightweight & Sequential IDs"), MAP_DB(
				"Buffered by MAPDB"), NAIVE("Naive");

		private final String name;

		private ImplementingPlugin(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
