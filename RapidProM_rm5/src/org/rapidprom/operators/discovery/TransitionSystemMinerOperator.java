package org.rapidprom.operators.discovery;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.transitionsystem.miner.TSMinerInput;
import org.processmining.plugins.transitionsystem.miner.TSMinerPlugin;
import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;
import org.processmining.plugins.transitionsystem.miner.modir.TSMinerModirInput;
import org.processmining.plugins.transitionsystem.miner.util.TSAbstractions;
import org.processmining.plugins.transitionsystem.miner.util.TSDirections;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.TransitionSystemIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;

public class TransitionSystemMinerOperator
		extends AbstractRapidProMDiscoveryOperator {

	public static final String PARAMETER_1_KEY = "Abstraction",
			PARAMETER_1_DESCR = "Defines the abstraction used to define a state: "
					+ "sequence (order and cardinality of events matter), "
					+ "bag / multiset (cardinality of events matter, but not order), "
					+ "set (cardinality and order of event does not matter, only distinct "
					+ "event classes are considered) or fixed length set (set that considers events "
					+ "until having X different event classes, where X = horizon).",

			PARAMETER_2_KEY = "Horizon",
			PARAMETER_2_DESCR = "This number defines the length of the event window considered to "
					+ "defines states: only use the last 'X' events of a (partial) trace will be used.";
	private OutputPort output = getOutputPorts()
			.createPort("model (ProM TransitionSystem)");

	public TransitionSystemMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(output, TransitionSystemIOObject.class));
	}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: transition system miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(TSMinerPlugin.class);

		XEventClassifier[] classifiers = new XEventClassifier[1];
		classifiers[0] = getXEventClassifier();

		XEventClassifier transitionClassifier = getXEventClassifier();

		Object[] result = TSMinerPlugin.main(pluginContext, getXLog(),
				classifiers, transitionClassifier, getConfiguration(
						pluginContext, classifiers, transitionClassifier));

		// TO-DO: for now we use default parameters, we should use the same
		// parameters used in prom.
		TransitionSystemIOObject ts = new TransitionSystemIOObject(
				(TSMinerTransitionSystem) result[0], pluginContext);
		output.deliver(ts);

		logger.log(Level.INFO, "End: transition system miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	private TSMinerInput getConfiguration(PluginContext pluginContext,
			XEventClassifier[] classifiers,
			XEventClassifier transitionClassifier) throws UserError {

		TSMinerInput input = new TSMinerInput(pluginContext, getXLog(),
				Arrays.asList(classifiers), transitionClassifier);

		TSMinerModirInput setting = input
				.getModirSettings(TSDirections.BACKWARD, getXEventClassifier());

		if (getParameterAsString(PARAMETER_1_KEY)
				.equals(TSAbstractions.SET.getLabel()))
			setting.setAbstraction(TSAbstractions.SET);
		else if (getParameterAsString(PARAMETER_1_KEY)
				.equals(TSAbstractions.BAG.getLabel()))
			setting.setAbstraction(TSAbstractions.BAG);
		else if (getParameterAsString(PARAMETER_1_KEY)
				.equals(TSAbstractions.FIXED_LENGTH_SET.getLabel()))
			setting.setAbstraction(TSAbstractions.FIXED_LENGTH_SET);
		else 
			setting.setAbstraction(TSAbstractions.SEQUENCE);
		
		setting.setUse(true);
		setting.setFilteredHorizon(getParameterAsInt(PARAMETER_2_KEY));

		input.setModirSettings(TSDirections.BACKWARD, getXEventClassifier(),
				setting);
		return input;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeCategory parameter1 = new ParameterTypeCategory(
				PARAMETER_1_KEY, PARAMETER_1_DESCR,
				new String[] { TSAbstractions.SET.getLabel(),
						TSAbstractions.BAG.getLabel(),
						TSAbstractions.SEQUENCE.getLabel(),
						TSAbstractions.FIXED_LENGTH_SET.getLabel() },
				1);
		parameterTypes.add(parameter1);

		ParameterTypeInt parameter10 = new ParameterTypeInt(PARAMETER_2_KEY,
				PARAMETER_2_DESCR, 0, 100, 1);
		parameterTypes.add(parameter10);

		return parameterTypes;
	}

}
