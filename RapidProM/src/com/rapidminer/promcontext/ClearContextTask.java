package com.rapidminer.promcontext;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorException;

import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class ClearContextTask extends Operator {

	private InputPort input = getInputPorts().createPort(
			"context (ProM Context)", ProMContextIOObject.class);
	private OutputPort output = getOutputPorts().createPort(
			"context (ProM Context)");

	public ClearContextTask(OperatorDescription description) {
		super(description);

		getTransformer().addRule(
				new GenerateNewMDRule(output, ProMContextIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do work Clean Context", LogService.NOTE);
		ProMContextIOObject context = input.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();

		pluginContext.clear();
		pluginContext.getProvidedObjectManager().clear();
		pluginContext.getConnectionManager().clear();
		pluginContext.setFuture(null);

		output.deliver(context);
	}

}
