package org.rapidprom.operators.io;

import java.io.File;
import java.util.List;

import org.processmining.openslex.metamodel.SLEXMMStorageMetaModelImpl;
import org.rapidprom.ioobjectrenderers.SLEXMMIOObjectVisualizationType;
import org.rapidprom.ioobjects.SLEXMMIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;
import org.rapidprom.operators.extract.ExtractXLogOperator;
import org.rapidprom.operators.extract.ExtractXLogOperator.ImplementingPlugin;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;

/**
 * The ImportXLLogOperator uses public static methods from the
 * {@link ExtractXLogOperator} for actually importing the event log. This is
 * mainly due to the fact that java does not support multiple-inheritance.
 * 
 */
public class ImportSLEXMMOperator
		extends AbstractRapidProMImportOperator<SLEXMMIOObject> {

//	private final static String PARAMETER_KEY_IMPORTER = "importer";
//	private final static String PARAMETER_DESC_IMPORTER = "Select the implementing importer, importers differ in terms of performance: "
//			+ "The \"Naive\" importer loads the Log completely in memory (faster, but more memory usage). "
//			+ "The \"Buffered by MAPDB\" importer loads only log, trace and event ids, "
//			+ "and the rest of the data (mainly attribute values) are stored in disk by MapDB "
//			+ "(slower, but less memory usage). "
//			+ "The \"Lightweight & Sequential IDs\" importer is a balance between the \"Naive\" and the \"Buffered by MapDB\" importers";

//	private final static ImplementingPlugin[] PARAMETER_OPTIONS_IMPORTER = EnumSet
//			.allOf(ImplementingPlugin.class)
//			.toArray(new ImplementingPlugin[EnumSet
//					.allOf(ImplementingPlugin.class).size()]);

	private final static String[] SUPPORTED_FILE_FORMATS = new String[] { "slexmm" };

	public ImportSLEXMMOperator(OperatorDescription description) {
		super(description, SLEXMMIOObject.class, SUPPORTED_FILE_FORMATS);
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public MetaData getGeneratedMetaData() throws OperatorException {
//		getLogger().fine("Generating meta data for " + this.getName());
//		return new SLEXMMIOObjectMetaData();
//	}

	protected SLEXMMIOObject read(File file) throws Exception {
		String fileDir = file.getParent();
		String fileName = file.getName();
		SLEXMMStorageMetaModelImpl mm = new SLEXMMStorageMetaModelImpl(fileDir, fileName);
		SLEXMMIOObject obj = new SLEXMMIOObject(mm);
		
		obj.setVisualizationType(SLEXMMIOObjectVisualizationType.DEFAULT);
		return obj;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_KEY_FILE, PARAMETER_DESC_FILE,
				false, SUPPORTED_FILE_FORMATS));
//		types.add(createImporterParameterTypeCategory(PARAMETER_KEY_IMPORTER,
//				PARAMETER_DESC_IMPORTER, PARAMETER_OPTIONS_IMPORTER));
		return types;
	}

//	private ParameterType createImporterParameterTypeCategory(String key,
//			String desc, ImplementingPlugin[] importers) {
//		String[] importersStr = new String[importers.length];
//		for (int i = 0; i < importersStr.length; i++) {
//			importersStr[i] = importers[i].toString();
//		}
//		return new ParameterTypeCategory(key, desc, importersStr, 0, true);
//	}
}
