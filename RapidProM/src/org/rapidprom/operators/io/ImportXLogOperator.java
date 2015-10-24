package org.rapidprom.operators.io;

import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.log.plugins.ImportXEventClassifierListPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.extract.ExtractXLogOperator;
import org.rapidprom.operators.ports.metadata.XLogIOObjectMetaData;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractReader;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

/**
 * The ImportXLLogOperator uses public static methods from the
 * {@link ExtractXLogOperator} for actually importing the event log. This is
 * mainly due to the fact that java does not support multiple-inheritance.
 * 
 * @author svzelst
 *
 */
public class ImportXLogOperator extends AbstractReader<XLogIOObject> {

	private final static String PARAMETER_KEY_EVENT_LOG_FILE = "file";
	private final static String PARAMETER_DESC_EVENT_LOG_FILE = "Select a file (.xes, .xez or .xes.gz) that represents an event log.";
	private final static String[] SUPPORTED_EVENT_LOG_FORMATS = new String[] {
			"xes", "xez", "xes.gz" };

	static {
		for (String ext : SUPPORTED_EVENT_LOG_FORMATS) {
			AbstractReader.registerReaderDescription(new ReaderDescription(ext,
					ImportXLogOperator.class, PARAMETER_KEY_EVENT_LOG_FILE));
		}
	}

	public ImportXLogOperator(OperatorDescription description) {
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
					getParameterAsFile(PARAMETER_KEY_EVENT_LOG_FILE));
		} catch (Exception e) {
			return new XLogIOObjectMetaData();
		}
		if (classifiers != null)
			return new XLogIOObjectMetaData(classifiers);
		else
			return new XLogIOObjectMetaData();
	}

	@Override
	public XLogIOObject read() throws OperatorException {
		XLogIOObject xLogIOObject = null;
		try {
			xLogIOObject = new XLogIOObject(
					ExtractXLogOperator.importLog(
							ExtractXLogOperator.PARAMETER_OPTIONS_IMPORTER[getParameterAsInt(
									ExtractXLogOperator.PARAMETER_KEY_IMPORTER)],
							getParameterAsFile(PARAMETER_KEY_EVENT_LOG_FILE)),
					ProMPluginContextManager.instance().getContext());
			xLogIOObject.setVisualizationType(
					XLogIOObjectVisualizationType.DEFAULT);
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
		types.add(ExtractXLogOperator.createImporterParameterTypeCategory(
				ExtractXLogOperator.PARAMETER_KEY_IMPORTER,
				ExtractXLogOperator.PARAMETER_DESC_IMPORTER,
				ExtractXLogOperator.PARAMETER_OPTIONS_IMPORTER));
		return types;
	}
}
