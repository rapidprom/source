package org.rapidprom.operators.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.heuristicsnet.miner.heuristics.converter.HeuristicsNetToPetriNetConverter;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.HeuristicsNetIOObject;
import org.rapidprom.ioobjects.PetriNetIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class HeuristicNetToPetriNetConversionOperator extends Operator {

	private InputPort input = getInputPorts().createPort(
			"model (ProM Heuristics Net)", HeuristicsNetIOObject.class);
	private OutputPort output = getOutputPorts()
			.createPort("model (ProM Petri Net)");

	public HeuristicNetToPetriNetConversionOperator(
			OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(output, PetriNetIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: heuristics net to petri net conversion");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(
						HeuristicsNetToPetriNetConverter.class);

		Object[] result = HeuristicsNetToPetriNetConverter.converter(
				pluginContext,
				input.getData(HeuristicsNetIOObject.class).getArtifact());

		PetriNetIOObject finalPetriNet = new PetriNetIOObject(
				(Petrinet) result[0], (Marking) result[1], null, pluginContext);
		output.deliver(finalPetriNet);

		logger.log(Level.INFO, "End: heuristics net to petri net conversion ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}
}
