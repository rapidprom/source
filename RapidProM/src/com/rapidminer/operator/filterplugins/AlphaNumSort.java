package com.rapidminer.operator.filterplugins;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;

public class AlphaNumSort extends Operator{

	private InputPort input = getInputPorts().createPort("example set (Data Table)", new ExampleSetMetaData());
	private OutputPort output = getOutputPorts().createPort("example set (Data Table)");
	
	
	public AlphaNumSort(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
		getTransformer().addRule( new GenerateNewMDRule(output, ExampleSet.class));
	}

}
