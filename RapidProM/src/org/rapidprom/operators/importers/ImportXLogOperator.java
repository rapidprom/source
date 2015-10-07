package org.rapidprom.operators.importers;

import java.io.File;
import java.util.List;

import org.rapidprom.operators.abstr.AbstractImportXLogOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

public class ImportXLogOperator extends AbstractImportXLogOperator {

	public ImportXLogOperator(OperatorDescription description) {
		super(description);
	}

	private static String[] SUPPORTED_EVENT_LOG_FORMATS = new String[] { "xes",
			"xez", "xes.gz" };

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();
		ParameterTypeFile logFileParameter = new ParameterTypeFile(
				PARAMETER_LABEL_FILENAME, "File to open", false,
				SUPPORTED_EVENT_LOG_FORMATS);
		parameterTypes.add(logFileParameter);
		return parameterTypes;
	}

	@Override
	protected File getFile() throws UserError {
		return getParameterAsFile(PARAMETER_LABEL_FILENAME);
	}
}
