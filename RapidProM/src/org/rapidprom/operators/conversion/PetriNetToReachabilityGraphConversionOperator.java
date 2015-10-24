package org.rapidprom.operators.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;
import org.processmining.plugins.petrinet.behavioralanalysis.TSGenerator;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.ReachabilityGraphIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class PetriNetToReachabilityGraphConversionOperator extends Operator {

	private InputPort input = getInputPorts().createPort(
			"model (ProM Petri Net)", PetriNetIOObject.class);

	private OutputPort output = getOutputPorts().createPort(
			"model (ProM Reachability Graph)");

	public PetriNetToReachabilityGraphConversionOperator(
			OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(output, ReachabilityGraphIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: Petri Net to Reachability Graph conversion");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(TSGenerator.class);

		PetriNetIOObject petriNet = input.getData(PetriNetIOObject.class);

		TSGenerator converter = new TSGenerator();
		Object[] result = null;
		try {
			result = converter.calculateTS(pluginContext,
					petriNet.getArtifact(), petriNet.getInitialMarking());
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException("The marking could not be found");
		}

		ReachabilityGraphIOObject reachabilityGraphIOObject = new ReachabilityGraphIOObject(
				(ReachabilityGraph) result[0], pluginContext);
		output.deliver(reachabilityGraphIOObject);

		logger.log(
				Level.INFO,
				"End: Petri Net to Reachability Graph conversion ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}
}
