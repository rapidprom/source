package org.rapidprom.operator.io;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;
import org.processmining.xeslite.plugin.OpenLogFileDiskImplPlugin;
import org.processmining.xeslite.plugin.OpenLogFileLiteImplPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operator.ports.metadata.XLogIOObjectMetaData;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractReader;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;

public class ImportXLogOperator extends AbstractReader<XLogIOObject> {

	private final static String PARAMETER_KEY_EVENT_LOG_FILE = "file";
	private final static String PARAMETER_DESC_EVENT_LOG_FILE = "Select a file (.xes, .xez or .xes.gz) that represents an event log.";
	private final static String[] SUPPORTED_EVENT_LOG_FORMATS = new String[] {
			"xes", "xez", "xes.gz" };

	protected static enum ImplementingPlugin {
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

	private final static String PARAMETER_KEY_IMPORTER = "importer";
	private final static String PARAMETER_DESC_IMPORTER = "Select the implementing importer, importers differ in terms of performance";
	private final static ImplementingPlugin[] PARAMETER_OPTIONS_IMPORTER = EnumSet
			.allOf(ImplementingPlugin.class).toArray(
					new ImplementingPlugin[EnumSet.allOf(
							ImplementingPlugin.class).size()]);

	static {
		for (String ext : SUPPORTED_EVENT_LOG_FORMATS) {
			AbstractReader.registerReaderDescription(new ReaderDescription(ext,
					ImportXLogOperator.class, PARAMETER_KEY_EVENT_LOG_FILE));
		}
	}

	public ImportXLogOperator(OperatorDescription description) {
		super(description, XLogIOObject.class);
	}

	@Override
	public MetaData getGeneratedMetaData() throws OperatorException {
		getLogger().fine("Generating meta data for " + this.getName());
		XLog log = null;
		try {
			log = importLog(
					PARAMETER_OPTIONS_IMPORTER[getParameterAsInt(PARAMETER_KEY_IMPORTER)],
					getParameterAsFile(PARAMETER_KEY_EVENT_LOG_FILE));
		} catch (Exception e) {
			return new XLogIOObjectMetaData();
		}
		if (log != null) {
			return new XLogIOObjectMetaData(log);
		} else {
			return new XLogIOObjectMetaData();
		}
	}

	@Override
	public XLogIOObject read() throws OperatorException {
		XLogIOObject xLogIOObject = null;
		try {
			xLogIOObject = new XLogIOObject(
					importLog(
							PARAMETER_OPTIONS_IMPORTER[getParameterAsInt(PARAMETER_KEY_IMPORTER)],
							getParameterAsFile(PARAMETER_KEY_EVENT_LOG_FILE)));
			xLogIOObject.setPluginContext(ProMPluginContextManager.instance()
					.getContext());
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.DEFAULT);
		} catch (Exception e) {
			throw new OperatorException(
					"Importing XLog failed, please check if the file specified exists");
		}
		return xLogIOObject;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_KEY_EVENT_LOG_FILE,
				PARAMETER_DESC_EVENT_LOG_FILE, false,
				SUPPORTED_EVENT_LOG_FORMATS));

		String[] importers = new String[PARAMETER_OPTIONS_IMPORTER.length];
		for (int i = 0; i < importers.length; i++) {
			importers[i] = PARAMETER_OPTIONS_IMPORTER[i].toString();
		}

		types.add(new ParameterTypeCategory(PARAMETER_KEY_IMPORTER,
				PARAMETER_DESC_IMPORTER, importers, 0, true));

		return types;
	}

	private XLog importLog(ImplementingPlugin p, File file) throws Exception {
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

	private XLog importLeightWeight(File file) throws Exception {
		XLog result = null;
		OpenLogFileLiteImplPlugin plugin = new OpenLogFileLiteImplPlugin();
		result = (XLog) plugin.importFile(ProMPluginContextManager.instance()
				.getFutureResultAwareContext(OpenLogFileLiteImplPlugin.class),
				file);
		return result;
	}

	private XLog importMapDb(File file) throws Exception {
		XLog result = null;
		OpenLogFileDiskImplPlugin plugin = new OpenLogFileDiskImplPlugin();
		result = (XLog) plugin.importFile(ProMPluginContextManager.instance()
				.getFutureResultAwareContext(OpenLogFileDiskImplPlugin.class),
				file);
		return result;
	}

	private XLog importLogNaive(File file) throws Exception {
		XLog result = null;
		OpenNaiveLogFilePlugin plugin = new OpenNaiveLogFilePlugin();
		result = (XLog) plugin.importFile(ProMPluginContextManager.instance()
				.getFutureResultAwareContext(OpenNaiveLogFilePlugin.class),
				file);
		return result;
	}

}
