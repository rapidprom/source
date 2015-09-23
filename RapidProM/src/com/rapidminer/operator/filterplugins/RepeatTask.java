package com.rapidminer.operator.filterplugins;

import java.util.*;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.tools.LogService;
import com.rapidminer.parameter.*;
import com.rapidminer.parameters.*;

public class RepeatTask extends Operator {

	private List<Parameter> parametersRepeat = null;
	
	private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());

	public RepeatTask(OperatorDescription description) {
		super(description);
		
		dummyPorts.start();

		getTransformer().addRule(dummyPorts.makePassThroughRule());
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Repeat", LogService.NOTE);
		int configuration = getConfiguration(parametersRepeat);
		dummyPorts.passDataThrough();
		logService.log("end do work Repeat", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		this.parametersRepeat = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterInteger parameter1 = new ParameterInteger(1, 1, Integer.MAX_VALUE, 1, Integer.class, "getNumberRepetitions", "getNumberRepetitions");
		ParameterTypeInt parameterType1 = new ParameterTypeInt(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parametersRepeat.add(parameter1);

		return parameterTypes;
	}

	private int getConfiguration(List<Parameter> parametersDuplicateLog) {
		try {
		Parameter parameter1 = parametersDuplicateLog.get(0);
		int par1int = getParameterAsInt(parameter1.getNameParameter());
		return par1int;

		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
	return 1;
	}
}
