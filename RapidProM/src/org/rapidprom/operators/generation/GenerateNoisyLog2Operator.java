package org.rapidprom.operators.generation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.plugins.Alfredo_GenerateNoisyLog;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class GenerateNoisyLog2Operator extends Operator {

	public static final String PARAMETER_1_KEY = "Probability of Add Event",
			PARAMETER_1_DESCR = "The probability of, for a given trace, adding an event in a random position",
			PARAMETER_2_KEY = "Probability of Remove Event",
			PARAMETER_2_DESCR = "The probability of, for a given trace, removing an event in a random position",
			PARAMETER_3_KEY = "Probability of Duplicate Events",
			PARAMETER_3_DESCR = "The probability of, for a given trace, duplicating an event in a random position. The duplicated events will be consecutive",
			PARAMETER_4_KEY = "Probability of Swap Consecutive Events",
			PARAMETER_4_DESCR = "The probability of, for a given trace, swapping two consecutive events",
			PARAMETER_5_KEY = "Probability of Swap Random Events",
			PARAMETER_5_DESCR = "The probability of, for a given trace, swapping two events selected randomly",
			PARAMETER_6_KEY = "Probability of Remove Head",
			PARAMETER_6_DESCR = "The probability of, for a given trace, remove up to 1/3 of its first events.",
			PARAMETER_7_KEY = "Probability of Remove Body",
			PARAMETER_7_DESCR = "The probability of, for a given trace, remove up to 1/3 of its middle events.",
			PARAMETER_8_KEY = "Probability of Remove Tail",
			PARAMETER_8_DESCR = "The probability of, for a given trace, remove up to 1/3 of its last events.";

	private InputPort inputLog = getInputPorts().createPort("event log",
			XLogIOObject.class);

	private InputPort inputNet = getInputPorts().createPort("petri net",
			PetriNetIOObject.class);

	private OutputPort output = getOutputPorts().createPort("event log");

	public GenerateNoisyLog2Operator(OperatorDescription description) {
		super(description);
		getTransformer()
		.addRule(new GenerateNewMDRule(output, XLogIOObject.class));
	}
	
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: generating noisy event log");
		long time = System.currentTimeMillis();

		XLogIOObject log = inputLog.getData(XLogIOObject.class);
		PetriNetIOObject petriNet = inputNet.getData(PetriNetIOObject.class);
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();
		
		Alfredo_GenerateNoisyLog noiseGenerator = new Alfredo_GenerateNoisyLog();
		
		XLog result = noiseGenerator.addNoiseWithDefaultParameters(context, log, net, initialMarking, finMarking, classifier)
		output.deliver(null);
		logger.log(Level.INFO, "End: generating noisy event log ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
