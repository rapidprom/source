package org.rapidprom.operators.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.cpnet.ColouredPetriNet;
import org.processmining.plugins.cpnet.LoadCPNModelFromFile;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.CPNModelIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

public class ImportCPNModelOperator
		extends AbstractRapidProMImportOperator<CPNModelIOObject> {

	private final static String[] SUPPORTED_FILE_FORMATS = new String[] {
			"cpn" };

	public ImportCPNModelOperator(OperatorDescription description) {
		super(description, CPNModelIOObject.class, SUPPORTED_FILE_FORMATS);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_KEY_FILE, PARAMETER_DESC_FILE,
				false, SUPPORTED_FILE_FORMATS));
		return types;
	}

	@Override
	protected CPNModelIOObject read(File file) throws Exception {
		ColouredPetriNet net = null;
		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(LoadCPNModelFromFile.class);
		net = LoadCPNModelFromFile.importColouredPetriNetFromStream(context,
				new FileInputStream(file), file.getName(), file.length());
		return new CPNModelIOObject(net, context);
	}
}