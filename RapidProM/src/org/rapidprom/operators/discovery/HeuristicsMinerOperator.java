package org.rapidprom.operators.discovery;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.parameter.*;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMinerPlugin;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.HeuristicsNetIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

public class HeuristicsMinerOperator extends AbstractRapidProMDiscoveryOperator {

	// Parameter keys (also used as description)
	public static final String PARAMETER_1 = "Threshold: Relative-to-best",
			PARAMETER_2 = "Threshold: Dependency",
			PARAMETER_3 = "Threshold: Length-one-loops",
			PARAMETER_4 = "Threshold: Length-two-loops",
			PARAMETER_5 = "Threshold: Long distance",
			PARAMETER_6 = "All tasks connected",
			PARAMETER_7 = "Long distance dependency",
			PARAMETER_8 = "Ignore loop dependency thresholds";

	private OutputPort outputHeuristicsNet = getOutputPorts().createPort(
			"model (ProM Heuristics Net)");

	public HeuristicsMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputHeuristicsNet,
						HeuristicsNetIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: heuristics miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(
						FlexibleHeuristicsMinerPlugin.class);

		HeuristicsMinerSettings heuristicsMinerSettings = getConfiguration(getXLog());

		HeuristicsNetIOObject heuristicsNetIOObject = new HeuristicsNetIOObject(
				FlexibleHeuristicsMinerPlugin.run(pluginContext, getXLog(),
						heuristicsMinerSettings), pluginContext);

		outputHeuristicsNet.deliver(heuristicsNetIOObject);

		logger.log(Level.INFO,
				"End: heuristics miner (" + (System.currentTimeMillis() - time)
						/ 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeDouble parameter1 = new ParameterTypeDouble(PARAMETER_1,
				PARAMETER_1, 0, 100, 5);
		parameterTypes.add(parameter1);

		ParameterTypeDouble parameter2 = new ParameterTypeDouble(PARAMETER_2,
				PARAMETER_2, 0, 100, 90);
		parameterTypes.add(parameter2);

		ParameterTypeDouble parameter3 = new ParameterTypeDouble(PARAMETER_3,
				PARAMETER_3, 0, 100, 90);
		parameterTypes.add(parameter3);

		ParameterTypeDouble parameter4 = new ParameterTypeDouble(PARAMETER_4,
				PARAMETER_4, 0, 100, 90);
		parameterTypes.add(parameter4);

		ParameterTypeDouble parameter5 = new ParameterTypeDouble(PARAMETER_5,
				PARAMETER_5, 0, 100, 90);
		parameterTypes.add(parameter5);

		ParameterTypeBoolean parameter6 = new ParameterTypeBoolean(PARAMETER_6,
				PARAMETER_6, true);
		parameterTypes.add(parameter6);

		ParameterTypeBoolean parameter7 = new ParameterTypeBoolean(PARAMETER_7,
				PARAMETER_7, false);
		parameterTypes.add(parameter7);

		ParameterTypeBoolean parameter8 = new ParameterTypeBoolean(PARAMETER_8,
				PARAMETER_8, true);
		parameterTypes.add(parameter8);

		return parameterTypes;
	}

	private HeuristicsMinerSettings getConfiguration(XLog log) {
		HeuristicsMinerSettings heuristicsMinerSettings = new HeuristicsMinerSettings();
		try {
			heuristicsMinerSettings
					.setRelativeToBestThreshold(getParameterAsDouble(PARAMETER_1) / 100d);
			heuristicsMinerSettings
					.setDependencyThreshold(getParameterAsDouble(PARAMETER_2) / 100d);
			heuristicsMinerSettings
					.setL1lThreshold(getParameterAsDouble(PARAMETER_3) / 100d);
			heuristicsMinerSettings
					.setL2lThreshold(getParameterAsDouble(PARAMETER_4) / 100d);
			heuristicsMinerSettings
					.setLongDistanceThreshold(getParameterAsDouble(PARAMETER_5) / 100d);
			heuristicsMinerSettings
					.setUseAllConnectedHeuristics(getParameterAsBoolean(PARAMETER_6));
			heuristicsMinerSettings
					.setUseLongDistanceDependency(getParameterAsBoolean(PARAMETER_7));
			heuristicsMinerSettings
					.setCheckBestAgainstL2L(getParameterAsBoolean(PARAMETER_8));
			heuristicsMinerSettings.setAndThreshold(Double.NaN);
			heuristicsMinerSettings.setClassifier(getXEventClassifier());
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
		return heuristicsMinerSettings;
	}
}
