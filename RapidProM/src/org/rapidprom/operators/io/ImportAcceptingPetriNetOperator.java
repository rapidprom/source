package org.rapidprom.operators.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.AcceptingPetriNetIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

public class ImportAcceptingPetriNetOperator
		extends AbstractRapidProMImportOperator<AcceptingPetriNetIOObject> {

	private final static String[] SUPPORTED_FILE_FORMATS = new String[] {
			"pnml" };

	public ImportAcceptingPetriNetOperator(OperatorDescription description) {
		super(description, AcceptingPetriNetIOObject.class,
				SUPPORTED_FILE_FORMATS);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_KEY_FILE, PARAMETER_DESC_FILE,
				false, SUPPORTED_FILE_FORMATS));
		return types;
	}

	@Override
	protected AcceptingPetriNetIOObject read(File file) throws Exception {
		PluginContext context = ProMPluginContextManager.instance()
				.getContext();
		return new AcceptingPetriNetIOObject(AcceptingPetriNetFactory
				.importFromStream(context, new FileInputStream(file)), context);
	}

}
