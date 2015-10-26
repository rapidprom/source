package org.rapidprom.operators.io;

import java.io.File;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.log.plugins.ImportXEventClassifierListPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;
import org.rapidprom.operators.extract.ExtractXLogOperator;
import org.rapidprom.operators.ports.metadata.XLogIOObjectMetaData;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

/**
 * The ImportXLLogOperator uses public static methods from the
 * {@link ExtractXLogOperator} for actually importing the event log. This is
 * mainly due to the fact that java does not support multiple-inheritance.
 * 
 */
public class ImportXLogOperator
		extends AbstractRapidProMImportOperator<XLogIOObject> {

	private final static String[] SUPPORTED_FILE_FORMATS = new String[] { "xes",
			"xez", "xes.gz" };

	static {
		registerExtentions(SUPPORTED_FILE_FORMATS);
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
						ExtractXLogOperator.PARAMETER_OPTIONS_IMPORTER[getParameterAsInt(
								ExtractXLogOperator.PARAMETER_KEY_IMPORTER)],
						getParameterAsFile(PARAMETER_KEY_FILE)),
				ProMPluginContextManager.instance().getContext());
		obj.setVisualizationType(XLogIOObjectVisualizationType.DEFAULT);
		return obj;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		// we can not implement this in the super class due to a cyclic
		// call to getParameterTypes()
		types.add(new ParameterTypeFile(PARAMETER_KEY_FILE, PARAMETER_DESC_FILE,
				false, SUPPORTED_FILE_FORMATS));
		types.add(ExtractXLogOperator.createImporterParameterTypeCategory(
				ExtractXLogOperator.PARAMETER_KEY_IMPORTER,
				ExtractXLogOperator.PARAMETER_DESC_IMPORTER,
				ExtractXLogOperator.PARAMETER_OPTIONS_IMPORTER));
		return types;
	}
}
