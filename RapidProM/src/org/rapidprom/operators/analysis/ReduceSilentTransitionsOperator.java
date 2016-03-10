package org.rapidprom.operators.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.reduction.Murata;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class ReduceSilentTransitionsOperator extends Operator {

	private InputPort inputPetrinet = getInputPorts()
			.createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts()
			.createPort("model (ProM Petri Net)");

	public ReduceSilentTransitionsOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
	}

	// TO-DO : add parameters
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: reduce silent transitions");
		long time = System.currentTimeMillis();
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(Murata.class);
		Murata reducer = new Murata();
		Object[] result = null;
		try {
			result = reducer.run(pluginContext,
					inputPetrinet.getData(PetriNetIOObject.class).getArtifact(),
					inputPetrinet.getData(PetriNetIOObject.class)
							.getInitialMarking());
		} catch (Exception e) {
			e.printStackTrace();
		}
		PetriNetIOObject pn = new PetriNetIOObject((Petrinet) result[0],
				(Marking) result[1], null, pluginContext);
		outputPetrinet.deliver(pn);

		logger.log(Level.INFO, "End: reduce silent transitions ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

}
