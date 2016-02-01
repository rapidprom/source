package org.rapidprom.operators.io;

import java.io.File;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.importing.PnmlImportNet;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

public class ImportPetriNetOperator
		extends AbstractRapidProMImportOperator<PetriNetIOObject> {

	private final static String[] SUPPORTED_FILE_FORMATS = new String[] {
			"pnml" };

	static {
		registerExtentions(SUPPORTED_FILE_FORMATS);
	}

	public ImportPetriNetOperator(OperatorDescription description) {
		super(description, PetriNetIOObject.class);
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
	protected PetriNetIOObject read(File file) throws Exception {
		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(PnmlImportNet.class);
		PnmlImportNet importer = new PnmlImportNet();
		Object[] result = null;
		try {
			result = (Object[]) importer.importFile(context,
					getParameterAsFile(PARAMETER_KEY_FILE));
		} catch (Exception e) {
			e.printStackTrace();
		}
		PetriNetIOObject pnResult = new PetriNetIOObject((Petrinet) result[0],
				ProMPluginContextManager.instance().getContext());
		pnResult.setInitialMarking((Marking) result[1]);
		return pnResult;
	}
}