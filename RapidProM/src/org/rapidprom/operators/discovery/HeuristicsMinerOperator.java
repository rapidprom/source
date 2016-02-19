package org.rapidprom.operators.discovery;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMinerPlugin;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.HeuristicsNetIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;

/**
 * This class executes the heuristics miner algorithm defined in
 * (http://dx.doi.org/10.1109/CIDM.2011.5949453)
 * 
 * @author abolt
 *
 */
public class HeuristicsMinerOperator
		extends AbstractRapidProMDiscoveryOperator {

	// Parameter keys (also used as description)
	public static final String PARAMETER_1_KEY = "Threshold: Relative-to-best",
			PARAMETER_1_DESCR = "Admissable distance between directly follows relations for an "
					+ "activity and the activity’s best one. At 0 only the best directly follows "
					+ "relation will be shown for every activity, at 100 all will be shown.",
			PARAMETER_2_KEY = "Threshold: Dependency",
			PARAMETER_2_DESCR = "Strength of the directly follows relations determines when to "
					+ "Show arcs (based on how frequently one activity is followed by another).",
			PARAMETER_3_KEY = "Threshold: Length-one-loops",
			PARAMETER_3_DESCR = "Show arcs based on frequency of L1L observations",
			PARAMETER_4_KEY = "Threshold: Length-two-loops",
			PARAMETER_4_DESCR = "Show arcs based on frequency of L2L observations",
			PARAMETER_5_KEY = "Threshold: Long distance",
			PARAMETER_5_DESCR = "Show arcs based on how frequently one activity is "
					+ "eventually followed by another",
			PARAMETER_6_KEY = "All tasks connected",
			PARAMETER_6_DESCR = "Every task needs to have at least one input and output arc, "
					+ "except one initial and one final activity.",
			PARAMETER_7_KEY = "Long distance dependency",
			PARAMETER_7_DESCR = "Show long distance relations in the model";

	private OutputPort outputHeuristicsNet = getOutputPorts()
			.createPort("model (ProM Heuristics Net)");

	public HeuristicsMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(outputHeuristicsNet,
				HeuristicsNetIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: heuristics miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(
						FlexibleHeuristicsMinerPlugin.class);

		HeuristicsMinerSettings heuristicsMinerSettings = getConfiguration(
				getXLog());

		HeuristicsNetIOObject heuristicsNetIOObject = new HeuristicsNetIOObject(
				FlexibleHeuristicsMinerPlugin.run(pluginContext, getXLog(),
						heuristicsMinerSettings),
				pluginContext);

		outputHeuristicsNet.deliver(heuristicsNetIOObject);

		logger.log(Level.INFO, "End: heuristics miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeDouble parameter1 = new ParameterTypeDouble(
				PARAMETER_1_KEY, PARAMETER_1_DESCR, 0, 100, 5);
		parameterTypes.add(parameter1);

		ParameterTypeDouble parameter2 = new ParameterTypeDouble(
				PARAMETER_2_KEY, PARAMETER_2_DESCR, 0, 100, 90);
		parameterTypes.add(parameter2);

		ParameterTypeDouble parameter3 = new ParameterTypeDouble(
				PARAMETER_3_KEY, PARAMETER_3_DESCR, 0, 100, 90);
		parameterTypes.add(parameter3);

		ParameterTypeDouble parameter4 = new ParameterTypeDouble(
				PARAMETER_4_KEY, PARAMETER_4_DESCR, 0, 100, 90);
		parameterTypes.add(parameter4);

		ParameterTypeDouble parameter5 = new ParameterTypeDouble(
				PARAMETER_5_KEY, PARAMETER_5_DESCR, 0, 100, 90);
		parameterTypes.add(parameter5);

		ParameterTypeBoolean parameter6 = new ParameterTypeBoolean(
				PARAMETER_6_KEY, PARAMETER_6_DESCR, true);
		parameterTypes.add(parameter6);

		ParameterTypeBoolean parameter7 = new ParameterTypeBoolean(
				PARAMETER_7_KEY, PARAMETER_6_DESCR, false);
		parameterTypes.add(parameter7);

		return parameterTypes;
	}

	private HeuristicsMinerSettings getConfiguration(XLog log) {
		HeuristicsMinerSettings heuristicsMinerSettings = new HeuristicsMinerSettings();
		try {
			heuristicsMinerSettings.setRelativeToBestThreshold(
					getParameterAsDouble(PARAMETER_1_KEY) / 100d);
			heuristicsMinerSettings.setDependencyThreshold(
					getParameterAsDouble(PARAMETER_2_KEY) / 100d);
			heuristicsMinerSettings.setL1lThreshold(
					getParameterAsDouble(PARAMETER_3_KEY) / 100d);
			heuristicsMinerSettings.setL2lThreshold(
					getParameterAsDouble(PARAMETER_4_KEY) / 100d);
			heuristicsMinerSettings.setLongDistanceThreshold(
					getParameterAsDouble(PARAMETER_5_KEY) / 100d);
			heuristicsMinerSettings.setUseAllConnectedHeuristics(
					getParameterAsBoolean(PARAMETER_6_KEY));
			heuristicsMinerSettings.setUseLongDistanceDependency(
					getParameterAsBoolean(PARAMETER_7_KEY));
			heuristicsMinerSettings.setCheckBestAgainstL2L(false);
			heuristicsMinerSettings.setAndThreshold(Double.NaN);
			heuristicsMinerSettings.setClassifier(getXEventClassifier());
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
		return heuristicsMinerSettings;
	}
}
