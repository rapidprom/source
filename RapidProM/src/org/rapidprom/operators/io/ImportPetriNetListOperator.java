package org.rapidprom.operators.io;

import java.io.File;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.petrinets.list.PetriNetList;
import org.processmining.petrinets.list.plugin.ImportPetriNetListPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetListIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

public class ImportPetriNetListOperator
		extends AbstractRapidProMImportOperator<PetriNetListIOObject> {

	private static String[] SUPPORTED_FILE_FORMATS = { "pnlist" };

	static {
		registerExtentions(SUPPORTED_FILE_FORMATS);
	}

	public ImportPetriNetListOperator(OperatorDescription description) {
		super(description, PetriNetListIOObject.class);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		// we can not implement this in the super class due to a cyclic
		// call to getParameterTypes()
		types.add(new ParameterTypeFile(PARAMETER_KEY_FILE, PARAMETER_DESC_FILE,
				false, SUPPORTED_FILE_FORMATS));
		return types;
	}

	@Override
	protected PetriNetListIOObject read(File file) {
		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(ImportPetriNetListPlugin.class);
		ImportPetriNetListPlugin importer = new ImportPetriNetListPlugin();
		PetriNetList pnlist = null;
		try {
			pnlist = importer.apply(context, getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new PetriNetListIOObject(pnlist, context);
	}
}
