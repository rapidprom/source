package org.rapidprom.operators.generation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.parameters.NoiseGeneratorSettings;
import org.processmining.ptandloggenerator.plugins.Alfredo_GenerateNoisyLog;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.LogService;

import javassist.tools.rmi.ObjectNotFoundException;

public class GenerateNoisyLog2Operator
		extends AbstractRapidProMDiscoveryOperator {

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
			PARAMETER_8_DESCR = "The probability of, for a given trace, remove up to 1/3 of its last events.",
			PARAMETER_9_KEY = "Enforce Single Noise Operation",
			PARAMETER_9_DESCR = "When enabled, the algorithm will enforce a single noise operation (e.g., add an event) on each iteration, selected randomly from all the noise types with a probability above 0 (regardless of the actual value). This option gives the smalles possible ammount of noise so that the traces do not fit the model.";

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
		logger.log(Level.INFO, "Start: generating noisy event log (alf)");
		long time = System.currentTimeMillis();

		XLog log = getXLog();
		PetriNetIOObject petriNet = inputNet.getData(PetriNetIOObject.class);
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		Alfredo_GenerateNoisyLog noiseGenerator = new Alfredo_GenerateNoisyLog();

		// create the settings object
		NoiseGeneratorSettings settings = new NoiseGeneratorSettings();
		settings.setProbAddEvent(getParameterAsDouble(PARAMETER_1_KEY));
		settings.setProbRemoveEvent(getParameterAsDouble(PARAMETER_2_KEY));
		settings.setProbDuplicateEvent(getParameterAsDouble(PARAMETER_3_KEY));
		settings.setProbSwapConsecutiveEvents(
				getParameterAsDouble(PARAMETER_4_KEY));
		settings.setProbSwapRandomEvents(getParameterAsDouble(PARAMETER_5_KEY));
		settings.setProbRemoveHead(getParameterAsDouble(PARAMETER_6_KEY));
		settings.setProbRemoveBody(getParameterAsDouble(PARAMETER_7_KEY));
		settings.setProbRemoveTail(getParameterAsDouble(PARAMETER_8_KEY));
		settings.setForceSingleNoiseOp(getParameterAsBoolean(PARAMETER_9_KEY));

		XLog result = null;
		try {
			result = noiseGenerator.addNoise(pluginContext, log,
					petriNet.getArtifact(), petriNet.getInitialMarking(),
					petriNet.getFinalMarking(), getXEventClassifier(),
					settings);
		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output.deliver(new XLogIOObject(result, pluginContext));
		logger.log(Level.INFO, "End: generating noisy event log (alf) ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeBoolean parameter9 = new ParameterTypeBoolean(
				PARAMETER_9_KEY, PARAMETER_9_DESCR, false);
		parameterTypes.add(parameter9);
		
		ParameterTypeDouble parameter1 = new ParameterTypeDouble(
				PARAMETER_1_KEY, PARAMETER_1_DESCR, 0, 1, 0.1);
		parameterTypes.add(parameter1);

		ParameterTypeDouble parameter2 = new ParameterTypeDouble(
				PARAMETER_2_KEY, PARAMETER_2_DESCR, 0, 1, 0.1);
		parameterTypes.add(parameter2);

		ParameterTypeDouble parameter3 = new ParameterTypeDouble(
				PARAMETER_3_KEY, PARAMETER_3_DESCR, 0, 1, 0.1);
		parameterTypes.add(parameter3);

		ParameterTypeDouble parameter4 = new ParameterTypeDouble(
				PARAMETER_4_KEY, PARAMETER_4_DESCR, 0, 1, 0.1);
		parameterTypes.add(parameter4);

		ParameterTypeDouble parameter5 = new ParameterTypeDouble(
				PARAMETER_5_KEY, PARAMETER_5_DESCR, 0, 1, 0.1);
		parameterTypes.add(parameter5);

		ParameterTypeDouble parameter6 = new ParameterTypeDouble(
				PARAMETER_6_KEY, PARAMETER_6_DESCR, 0, 1, 0.1);
		parameterTypes.add(parameter6);

		ParameterTypeDouble parameter7 = new ParameterTypeDouble(
				PARAMETER_7_KEY, PARAMETER_7_DESCR, 0, 1, 0.1);
		parameterTypes.add(parameter7);

		ParameterTypeDouble parameter8 = new ParameterTypeDouble(
				PARAMETER_8_KEY, PARAMETER_8_DESCR, 0, 1, 0.1);
		parameterTypes.add(parameter8);
		
		return parameterTypes;
	}

}
