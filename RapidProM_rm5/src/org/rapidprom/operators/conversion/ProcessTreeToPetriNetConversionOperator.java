package org.rapidprom.operators.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.PetrinetWithMarkings;
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

	private InputPort input = getInputPorts()
			.createPort("model (ProM ProcessTree)", ProcessTreeIOObject.class);
	private OutputPort output = getOutputPorts()
			.createPort("model (ProM Petri Net)");

	public ProcessTreeToPetriNetConversionOperator(
			OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(output, PetriNetIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: Process Tree to Petri Net conversion");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		PetrinetWithMarkings result = null;
		try {
			result = ProcessTree2Petrinet.convert(
					input.getData(ProcessTreeIOObject.class).getArtifact());
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(
					"The process tree could not be converted to a petri net");
		}

		PetriNetIOObject petriNet = new PetriNetIOObject(result.petrinet,
				result.initialMarking, result.finalMarking, pluginContext);

		output.deliver(petriNet);

		logger.log(Level.INFO, "End: Process Tree to Petri Net conversion ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}
}
