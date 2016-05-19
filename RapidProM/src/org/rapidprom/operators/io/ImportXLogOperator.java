package org.rapidprom.operators.io;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.log.plugins.ImportXEventClassifierListPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;
import org.rapidprom.operators.extract.ExtractXLogOperator;
import org.rapidprom.operators.extract.ExtractXLogOperator.ImplementingPlugin;
import org.rapidprom.operators.ports.metadata.XLogIOObjectMetaData;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;

/**
 * The ImportXLLogOperator uses public static methods from the
 * {@link ExtractXLogOperator} for actually importing the event log. This is
 * mainly due to the fact that java does not support multiple-inheritance.
 * 
 */
public class ImportXLogOperator
		extends AbstractRapidProMImportOperator<XLogIOObject> {

	private final static String PARAMETER_KEY_IMPORTER = "importer";
	private final static String PARAMETER_DESC_IMPORTER = "Select the implementing importer, importers differ in terms of performance: "
			+ "The \"Naive\" importer loads the Log completely in memory (faster, but more memory usage). "
			+ "The \"Buffered by MAPDB\" importer loads only log, trace and event ids, "
			+ "and the rest of the data (mainly attribute values) are stored in disk by MapDB "
			+ "(slower, but less memory usage). "
			+ "The \"Lightweight & Sequential IDs\" importer is a balance between the \"Naive\" and the \"Buffered by MapDB\" importers";

	private final static ImplementingPlugin[] PARAMETER_OPTIONS_IMPORTER = EnumSet
			.allOf(ImplementingPlugin.class)
			.toArray(new ImplementingPlugin[EnumSet
					.allOf(ImplementingPlugin.class).size()]);

	private final static String[] SUPPORTED_FILE_FORMATS = new String[] { "xes" };

	public ImportXLogOperator(OperatorDescription description) {
		super(description, XLogIOObject.class, SUPPORTED_FILE_FORMATS);
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
					getParameterAsFile(PARAMETER_KEY_FILE));
		} catch (Exception e) {
			return new XLogIOObjectMetaData();
		}
		if (classifiers != null)
			return new XLogIOObjectMetaData(classifiers);
		else
			return new XLogIOObjectMetaData();
	}

	protected XLogIOObject read(File file) throws Exception {
		XLogIOObject obj = new XLogIOObject(
				ExtractXLogOperator.importLog(
						PARAMETER_OPTIONS_IMPORTER[getParameterAsInt(
								PARAMETER_KEY_IMPORTER)],
						getParameterAsFile(PARAMETER_KEY_FILE)),
				ProMPluginContextManager.instance().getContext());
		obj.setVisualizationType(XLogIOObjectVisualizationType.DEFAULT);
		return obj;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_KEY_FILE, PARAMETER_DESC_FILE,
				false, SUPPORTED_FILE_FORMATS));
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
}
