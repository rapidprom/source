package org.rapidprom.operator.io;

import java.util.List;

import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractWriter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDirectory;

public class ExportXLogOperator extends AbstractWriter<XLogIOObject> {

	private final static String PARAMETER_KEY_EVENT_LOG_FILE = "folder";

	public ExportXLogOperator(OperatorDescription description) {
		super(description, XLogIOObject.class);
	}

	@Override
	public XLogIOObject write(XLogIOObject ioobject) throws OperatorException {

		return ioobject;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterTypeDirectory parameter1 = new ParameterTypeDirectory(
				PARAMETER_KEY_EVENT_LOG_FILE, PARAMETER_KEY_EVENT_LOG_FILE,
				"log.xes");
		types.add(parameter1);
		return types;
	}

}
