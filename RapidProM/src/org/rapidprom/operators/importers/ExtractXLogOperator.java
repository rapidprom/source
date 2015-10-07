package org.rapidprom.operators.importers;

import java.io.File;

import org.rapidprom.operators.abstr.AbstractImportXLogOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.ports.InputPort;

public class ExtractXLogOperator extends AbstractImportXLogOperator {

	private InputPort inputfile = getInputPorts().createPort("file",
			FileObject.class);


	public ExtractXLogOperator(OperatorDescription description) {
		super(description);
	}
	

	@Override
	protected File getFile() throws UserError {
		try {
			return inputfile.getData(FileObject.class).getFile();
		} catch (OperatorException e) {
			e.printStackTrace();
			return null;
		}
	}

}
