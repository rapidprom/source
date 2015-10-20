package org.rapidprom.operator.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.ProcessTreeIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class ProcessTreeToPetriNetConversionOperator extends Operator {

	private InputPort input = getInputPorts().createPort(
			"model (ProM ProcessTree)", ProcessTreeIOObject.class);
	private OutputPort output = getOutputPorts().createPort(
			"model (ProM Petri Net)");

	public ProcessTreeToPetriNetConversionOperator(
			OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(output, PetriNetIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: Process Tree to Petri Net conversion");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(ProcessTree2Petrinet.class);

		ProcessTree2Petrinet converter = new ProcessTree2Petrinet();

		Object[] result = null;
		try {
			result = converter.convert(pluginContext,
					input.getData(ProcessTreeIOObject.class).getArtifact());
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException("Invalid Process Tree");
		}

		PetriNetIOObject petriNet = new PetriNetIOObject((Petrinet) result[0],
				pluginContext);
		petriNet.setInitialMarking((Marking) result[1]);

		output.deliver(petriNet);

		logger.log(Level.INFO, "End: Process Tree to Petri Net conversion ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
