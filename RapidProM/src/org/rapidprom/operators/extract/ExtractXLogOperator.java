package org.rapidprom.operators.extract;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.plugins.ImportXEventClassifierListPlugin;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;
import org.processmining.xeslite.plugin.OpenLogFileDiskImplPlugin;
import org.processmining.xeslite.plugin.OpenLogFileLiteImplPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMExtractorOperator;
import org.rapidprom.operators.ports.metadata.XLogIOObjectMetaData;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.LogService;

/**
 * Extracts a log from a file operator. Note that this class also contains some
 * public static utility methods that can be used by other XLog extractors /
 * importers.
 *
 */
public class ExtractXLogOperator
		extends AbstractRapidProMExtractorOperator<XLogIOObject> {

	public static enum ImplementingPlugin {
		NAIVE("Naive"), LIGHT_WEIGHT_SEQ_ID("Lightweight & Sequential IDs"), MAP_DB(
				"Buffered by MAPDB");

		private final String name;

		private ImplementingPlugin(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private final static String PARAMETER_KEY_IMPORTER = "importer";
	private final static String PARAMETER_DESC_IMPORTER = 
			"Select the implementing importer, importers differ in terms of performance: "
			+ "The \"Naive\" importer loads the Log completely in memory (faster, but more memory usage). "
			+ "The \"Buffered by MAPDB\" importer loads only log, trace and event ids, "
			+ "and the rest of the data (mainly attribute values) are stored in disk by MapDB "
			+ "(slower, but less memory usage). "
			+ "The \"Lightweight & Sequential IDs\" importer is a balance between the \"Naive\" and the \"Buffered by MapDB\" importers";
	
	private final static ImplementingPlugin[] PARAMETER_OPTIONS_IMPORTER = EnumSet
			.allOf(ImplementingPlugin.class)
			.toArray(new ImplementingPlugin[EnumSet
					.allOf(ImplementingPlugin.class).size()]);

	private File currentFile = null;

	public ExtractXLogOperator(OperatorDescription description) {
		super(description, XLogIOObject.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MetaData getGeneratedMetaData() throws OperatorException {
		getLogger().fine("Generating meta data for " + this.getName());
		ImportXEventClassifierListPlugin plugin = new ImportXEventClassifierListPlugin();
		List<XEventClassifier> classifiers;
		try {
			classifiers = (List<XEventClassifier>) plugin.importFile(
					ProMPluginContextManager.instance()
							.getFutureResultAwareContext(
									ImportXEventClassifierListPlugin.class),
					getFile());
		} catch (Exception e) {
			return new XLogIOObjectMetaData();
		}
		if (classifiers != null)
			return new XLogIOObjectMetaData(classifiers);
		else
			return new XLogIOObjectMetaData();
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(createImporterParameterTypeCategory(PARAMETER_KEY_IMPORTER,
				PARAMETER_DESC_IMPORTER, PARAMETER_OPTIONS_IMPORTER));
		return types;
	}

	private ParameterType createImporterParameterTypeCategory(String key,
			String desc, ImplementingPlugin[] importers) {
		String[] importersStr = new String[importers.length];
		for (int i = 0; i < importersStr.length; i++) {
			importersStr[i] = importers[i].toString();
		}
		return new ParameterTypeCategory(key, desc, importersStr, 0, true);
	}

	public static XLog importLog(ImplementingPlugin p, File file)
			throws Exception {
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

	private static XLog importLeightWeight(File file) throws Exception {
		XLog result = null;
		OpenLogFileLiteImplPlugin plugin = new OpenLogFileLiteImplPlugin();
		result = (XLog) plugin.importFile(ProMPluginContextManager.instance()
				.getFutureResultAwareContext(OpenLogFileLiteImplPlugin.class),
				file);
		return result;
	}

	private static XLog importMapDb(File file) throws Exception {
		XLog result = null;
		OpenLogFileDiskImplPlugin plugin = new OpenLogFileDiskImplPlugin();
		result = (XLog) plugin.importFile(ProMPluginContextManager.instance()
				.getFutureResultAwareContext(OpenLogFileDiskImplPlugin.class),
				file);
		return result;
	}

	private static XLog importLogNaive(File file) throws Exception {
		XLog result = null;
		OpenNaiveLogFilePlugin plugin = new OpenNaiveLogFilePlugin();
		result = (XLog) plugin.importFile(ProMPluginContextManager.instance()
				.getFutureResultAwareContext(OpenNaiveLogFilePlugin.class),
				file);
		return result;
	}

	protected File getFile() throws UserError {
		try {
			File file = inputfile.getData(FileObject.class).getFile();
			this.currentFile = file;
		} catch (OperatorException e) {
			// Do nothing
		}
		return currentFile;
	}

	@Override
	public XLogIOObject read() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: importing event log");
		long time = System.currentTimeMillis();

		ImplementingPlugin importPlugin = PARAMETER_OPTIONS_IMPORTER[getParameterAsInt(
				PARAMETER_KEY_IMPORTER)];
		XLog log;
		try {
			log = importLog(importPlugin, getFile());
		} catch (Exception e) {
			throw new OperatorException("Loading the event log failed!");
		}
		XLogIOObject xLogIOObject = new XLogIOObject(log,
				ProMPluginContextManager.instance().getContext());
		xLogIOObject
				.setVisualizationType(XLogIOObjectVisualizationType.DEFAULT);
		logger.log(Level.INFO, "End: importing event log ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
		return xLogIOObject;
	}

}
