package org.rapidprom.operators.discovery;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.etm.parameters.ETMParam;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.plugins.etm.ui.plugins.ETMwithoutGUI;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.ProcessTreeIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;

public class ETMdMinerOperator extends AbstractRapidProMDiscoveryOperator {

	private static final String PARAMETER_1_KEY = "Population Size",
			PARAMETER_1_DESCR = "the number of candidate process models to change and evaluate "
					+ "in each generation (/round). Recommendation: 20",
			PARAMETER_2_KEY = "Elite Count",
			PARAMETER_2_DESCR = "The number of candidate process models to keep unchanged, "
					+ "e.g. the top X of process models. Recommendation: 20% to 25% of the "
					+ "population size, minimally 1 otherwise quality can be reduced.",
			PARAMETER_3_KEY = "Number of Random Trees",
			PARAMETER_3_DESCR = "The number of completely random process models/trees to be "
					+ "added in each round. A high number of random trees helps in finding "
					+ "process models/trees that are different that the current ones, but "
					+ "at the same time slows the ETM down. Recommendation: ~10% of the "
					+ "population size, minimum 1, maximum 50% of the population size.",
			PARAMETER_4_KEY = "Crossover Probability",
			PARAMETER_4_DESCR = "The probability for 2 process models/trees to ‘mate’: "
					+ "e.g. to have parts swapped between them to create offspring. "
					+ "Experiments show that crossover should be kept low, possibly "
					+ "even at 0.0, maximum 0.25.",
			PARAMETER_5_KEY = "Mutation Probability", 
			PARAMETER_5_DESCR = "The probability for a process model/tree to have a (random) "
					+ "mutation applied. We recommend this to be set high, e.g. close to 1.0.",
			PARAMETER_6_KEY = "Maximum Generations", 
			PARAMETER_6_DESCR = "The number of generations/rounds the ETM goes through. "
					+ "The more rounds the higher the quality of the process model/tree "
					+ "but the longer it takes for the ETM to finish. Recommendation: "
					+ "set to 100+. When the population size is around 20, then 500+ is recommended.",
			PARAMETER_7_KEY = "Target Fitness",
			PARAMETER_7_DESCR = "The fitness, or process model/tree quality, at which the "
					+ "ETM is allowed to stop. When set to 1.0 then the number of generations "
					+ "will effectively determine when to stop.",
			PARAMETER_8_KEY = "Fitness Limit",
			PARAMETER_8_DESCR = "Stop calculations for a particular process tree as soon "
					+ "as the replay fitness is lower than the provided value (double between "
					+ "0 and 1), or -1 to disable.  This is used to save time and not waste "
					+ "it on bad process trees.",
			PARAMETER_9_KEY = "Single Trace Alignment Timeout",
			PARAMETER_9_DESCR = "Maximum time (in milliseconds (so 1000 = 1 second)) "
					+ "after which the calculation time for a single trace is cancelled. "
					+ "Recommendation: keep at default, or -1 to disable.",
			PARAMETER_10_KEY = "Weight: Replay Fitness",
			PARAMETER_10_DESCR = "The weight used for the replay fitness quality dimension "
					+ "in the overall quality/fitness of a process model/tree. Recommendation "
					+ "is to have this as the highest of all four quality dimensions. Replay "
					+ "fitness is the same as recall in data mining: the fraction of the observed "
					+ "data that can be replayed correctly on the process model. Recommended is a "
					+ "weight of 10 to 15.",
			PARAMETER_11_KEY = "Weight: Precision",
			PARAMETER_11_DESCR = "The weight used for the precision quality dimension in the "
					+ "overall quality/fitness of the process model/tree. Precision punishes "
					+ "the process model/tree if it allows for more behaviour than seen in "
					+ "the data. A delicate balance between replay fitness and precision "
					+ "results in a ‘good’ process model. Recommended is a weight of 5 to 10.",
			PARAMETER_12_KEY = "Weight: Generalization",
			PARAMETER_12_DESCR = "The weight used for the generalization quality dimension in "
					+ "the overall quality/fitness of the process model/tree. This dimension "
					+ "is required next to replay fitness and precision, but plays a less important "
					+ "role. Recommended setting is a weight of 1.",
			PARAMETER_13_KEY = "Weight: Simplicity",
			PARAMETER_13_DESCR = "The weight used for the simplicity quality dimension in the "
					+ "overall quality/fitness of the process model/tree. This dimension is "
					+ "required next to replay fitness and precision, but plays a less important "
					+ "role. Recommended setting is a weight of 1.";

	private OutputPort outputProcessTree = getOutputPorts()
			.createPort("model (ProM ProcessTree)");

	public ETMdMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(outputProcessTree,
				ProcessTreeIOObject.class));
	}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: evolutionary tree miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(ETMwithoutGUI.class);
		XLog xLog = getXLog();

		ETMParam eTMParam = getConfiguration(xLog, pluginContext);

		ProcessTreeIOObject processTreeIOObject = new ProcessTreeIOObject(
				ETMwithoutGUI.minePTWithParameters(pluginContext, xLog,
						getXEventClassifier(), eTMParam),
				pluginContext);
		outputProcessTree.deliver(processTreeIOObject);

		logger.log(Level.INFO, "End: evolutionary tree miner " + "("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeInt parameter1 = new ParameterTypeInt(PARAMETER_1_KEY,
				PARAMETER_1_DESCR, 0, Integer.MAX_VALUE, 20);
		parameterTypes.add(parameter1);

		ParameterTypeInt parameter2 = new ParameterTypeInt(PARAMETER_2_KEY,
				PARAMETER_2_DESCR, 0, Integer.MAX_VALUE, 5);
		parameterTypes.add(parameter2);

		ParameterTypeInt parameter3 = new ParameterTypeInt(PARAMETER_3_KEY,
				PARAMETER_3_DESCR, 0, Integer.MAX_VALUE, 2);
		parameterTypes.add(parameter3);

		ParameterTypeDouble parameter4 = new ParameterTypeDouble(
				PARAMETER_4_KEY, PARAMETER_4_DESCR, 0, 1, 0.2);
		parameterTypes.add(parameter4);

		ParameterTypeDouble parameter5 = new ParameterTypeDouble(
				PARAMETER_5_KEY, PARAMETER_5_DESCR, 0, 1, 0.8);
		parameterTypes.add(parameter5);

		ParameterTypeInt parameter6 = new ParameterTypeInt(PARAMETER_6_KEY,
				PARAMETER_6_DESCR, 0, Integer.MAX_VALUE, 500);
		parameterTypes.add(parameter6);

		ParameterTypeDouble parameter7 = new ParameterTypeDouble(PARAMETER_7_KEY,
				PARAMETER_7_DESCR, 0, 1, 1);
		parameterTypes.add(parameter7);

		ParameterTypeDouble parameter8 = new ParameterTypeDouble(PARAMETER_8_KEY,
				PARAMETER_8_DESCR, -1, 1, 1);
		parameterTypes.add(parameter8);

		ParameterTypeInt parameter9 = new ParameterTypeInt(PARAMETER_9_KEY,
				PARAMETER_9_DESCR, -1, Integer.MAX_VALUE, 100);
		parameterTypes.add(parameter9);

		ParameterTypeInt parameter10 = new ParameterTypeInt(PARAMETER_10_KEY,
				PARAMETER_10_DESCR, 0, Integer.MAX_VALUE, 10);
		parameterTypes.add(parameter10);

		ParameterTypeInt parameter11 = new ParameterTypeInt(PARAMETER_11_KEY,
				PARAMETER_11_DESCR, 0, Integer.MAX_VALUE, 5);
		parameterTypes.add(parameter11);

		ParameterTypeInt parameter12 = new ParameterTypeInt(PARAMETER_12_KEY,
				PARAMETER_12_DESCR, 0, Integer.MAX_VALUE, 1);
		parameterTypes.add(parameter12);

		ParameterTypeInt parameter13 = new ParameterTypeInt(PARAMETER_13_KEY,
				PARAMETER_13_DESCR, 0, Integer.MAX_VALUE, 1);
		parameterTypes.add(parameter13);

		return parameterTypes;
	}

	private ETMParam getConfiguration(XLog log, PluginContext context) {
		ETMParam param;
		try {
			param = ETMParamFactory.buildParam(log, context,
					getParameterAsInt(PARAMETER_1_KEY),
					getParameterAsInt(PARAMETER_2_KEY),
					getParameterAsInt(PARAMETER_3_KEY),
					getParameterAsDouble(PARAMETER_4_KEY),
					getParameterAsDouble(PARAMETER_5_KEY), true, 
					getParameterAsInt(PARAMETER_6_KEY),
					getParameterAsDouble(PARAMETER_7_KEY),
					getParameterAsDouble(PARAMETER_10_KEY),
					getParameterAsDouble(PARAMETER_8_KEY),
					getParameterAsDouble(PARAMETER_9_KEY),
					getParameterAsDouble(PARAMETER_11_KEY),
					getParameterAsDouble(PARAMETER_12_KEY),
					getParameterAsDouble(PARAMETER_13_KEY), null, 0.0);

		} catch (UndefinedParameterError e) {
			e.printStackTrace();
			param = null;
		}
		return param;
	}

}
