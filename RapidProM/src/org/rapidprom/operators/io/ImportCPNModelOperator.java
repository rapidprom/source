package org.rapidprom.operators.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.processmining.plugins.cpnet.ColouredPetriNet;
import org.processmining.plugins.cpnet.LoadCPNModelFromFile;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;

import com.rapidminer.ioobjects.CPNModelIOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

public class ImportCPNModelOperator
		extends AbstractRapidProMImportOperator<CPNModelIOObject> {

	private final static String[] SUPPORTED_FILE_FORMATS = new String[] {
			"cpn" };

	static {
		registerExtentions(SUPPORTED_FILE_FORMATS);
	}

	public ImportCPNModelOperator(OperatorDescription description) {
		super(description, CPNModelIOObject.class);
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
	protected CPNModelIOObject read(File file) throws Exception {
		ColouredPetriNet net = null;
		net = LoadCPNModelFromFile.importColouredPetriNetFromStream(
				ProMPluginContextManager.instance().getFutureResultAwareContext(
						LoadCPNModelFromFile.class),
				new FileInputStream(file), file.getName(), file.length());
		return new CPNModelIOObject(net);
	}
}