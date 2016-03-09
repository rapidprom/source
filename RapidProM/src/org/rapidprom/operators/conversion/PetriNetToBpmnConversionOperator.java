package org.rapidprom.operators.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.plugins.converters.PetriNetToBPMNConverterPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.BPMNIOObject;
import org.rapidprom.ioobjects.PetriNetIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class PetriNetToBpmnConversionOperator extends Operator {

	private InputPort input = getInputPorts()
			.createPort("model (ProM Petri Net)", PetriNetIOObject.class);

	private OutputPort output = getOutputPorts()
			.createPort("model (ProM BPMN)");

	public PetriNetToBpmnConversionOperator(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(output, BPMNIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException { // TO_DO : deliver the
													// output converted BPMN

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: Petri Net to BPMN conversion");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(
						PetriNetToBPMNConverterPlugin.class);

		PetriNetToBPMNConverterPlugin converter = new PetriNetToBPMNConverterPlugin();
		Object[] result = converter.convert(pluginContext,
				input.getData(PetriNetIOObject.class).getArtifact());
		// BPMN2PetriNetConverter_Plugin converter = new
		// BPMN2PetriNetConverter_Plugin();
		// Object[] result = converter.convert(pluginContext,
		// input.getData(PetriNetIOObject.class).getData()));

		output.deliver(
				new BPMNIOObject((BPMNDiagram) result[0], pluginContext));

		logger.log(Level.INFO, "End: Petri Net to BPMN conversion ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
