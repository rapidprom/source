package org.rapidprom.operators.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.transitionsystem.regions.TransitionSystem2Petrinet;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.TransitionSystemIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class TransitionSystemtoPetriNetConversionOperator extends Operator {

	private InputPort input = getInputPorts().createPort(
			"model (ProM Transition System)", TransitionSystemIOObject.class);
	private OutputPort output = getOutputPorts()
			.createPort("model (ProM Petri Net)");

	public TransitionSystemtoPetriNetConversionOperator(
			OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(output, PetriNetIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: transition system to petri net conversion");
		long time = System.currentTimeMillis();

		TransitionSystem2Petrinet converter = new TransitionSystem2Petrinet();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(TransitionSystem2Petrinet.class);

		Object[] result;
		try {
			result = converter.convertToPetrinet(pluginContext, input
					.getData(TransitionSystemIOObject.class).getArtifact());
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(
					"There was an error obtaining connected elements for this transition system");
		}

		PetriNetIOObject petriNet = new PetriNetIOObject((Petrinet) result[0],
				(Marking) result[1], null,
				input.getData(TransitionSystemIOObject.class)
						.getPluginContext());

		output.deliver(petriNet);

		logger.log(Level.INFO,
				"End: transition system to petri net conversion ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
