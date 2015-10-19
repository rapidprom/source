package org.rapidprom.operator.discovery;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.etm.parameters.ETMParam;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.plugins.etm.ui.plugins.ETMPlugin;
import org.processmining.plugins.etm.ui.plugins.ETMwithoutGUI;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.ProcessTreeIOObject;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;

public class ETMdMinerOperator extends Operator {

	private static final String PARAMETER_1 = "Population Size",
			PARAMETER_2 = "Elite Count",
			PARAMETER_3 = "Number of Random Trees",
			PARAMETER_4 = "Crossover Probability",
			PARAMETER_5 = "Mutation Probability",
			PARAMETER_6 = "Maximum Generations",
			PARAMETER_7 = "Target Fitness",
			PARAMETER_8 = "Max Allowed Fitness",
			PARAMETER_9 = "Single Trace Alignment Timeout",
			PARAMETER_10 = "Weight: Replay Fitness",
			PARAMETER_11 = "Weight: Precision",
			PARAMETER_12 = "Weight: Generalization",
			PARAMETER_13 = "Weight: Simplicity";

	private InputPort inputXLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputProcessTree = getOutputPorts().createPort(
			"model (ProM ProcessTree)");

	public ETMdMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputProcessTree,
						ProcessTreeIOObject.class));
	}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: evolutionary tree miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(ETMwithoutGUI.class);
		XLogIOObject xLog = inputXLog.getData(XLogIOObject.class);
		
		ETMParam eTMParam = getConfiguration(xLog, pluginContext);

		ProcessTreeIOObject processTreeIOObject = new ProcessTreeIOObject(
				ETMwithoutGUI.minePTWithParameters(pluginContext, xLog.getArtifact(),
						new XEventAndClassifier(new XEventNameClassifier()),
						eTMParam),pluginContext);
		outputProcessTree.deliver(processTreeIOObject);

		logger.log(
				Level.INFO,
				"End: evolutionary tree miner " + "("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeInt parameter1 = new ParameterTypeInt(PARAMETER_1,
				PARAMETER_1, 0, Integer.MAX_VALUE, 20);
		parameterTypes.add(parameter1);

		ParameterTypeInt parameter2 = new ParameterTypeInt(PARAMETER_2,
				PARAMETER_2, 0, Integer.MAX_VALUE, 5);
		parameterTypes.add(parameter2);

		ParameterTypeInt parameter3 = new ParameterTypeInt(PARAMETER_3,
				PARAMETER_3, 0, Integer.MAX_VALUE, 0);
		parameterTypes.add(parameter3);

		ParameterTypeDouble parameter4 = new ParameterTypeDouble(PARAMETER_4,
				PARAMETER_4, 0, 1, 0.25);
		parameterTypes.add(parameter4);

		ParameterTypeDouble parameter5 = new ParameterTypeDouble(PARAMETER_5,
				PARAMETER_5, 0, 1, 0.25);
		parameterTypes.add(parameter5);

		ParameterTypeInt parameter6 = new ParameterTypeInt(PARAMETER_6,
				PARAMETER_6, 0, Integer.MAX_VALUE, 1000);
		parameterTypes.add(parameter6);

		ParameterTypeDouble parameter7 = new ParameterTypeDouble(PARAMETER_7,
				PARAMETER_7, 0, 1, 1);
		parameterTypes.add(parameter7);

		ParameterTypeDouble parameter8 = new ParameterTypeDouble(PARAMETER_8,
				PARAMETER_8, 0, 1, 0);
		parameterTypes.add(parameter8);

		ParameterTypeInt parameter9 = new ParameterTypeInt(PARAMETER_9,
				PARAMETER_9, 0, Integer.MAX_VALUE, 1000);
		parameterTypes.add(parameter9);

		ParameterTypeInt parameter10 = new ParameterTypeInt(PARAMETER_10,
				PARAMETER_10, 0, Integer.MAX_VALUE, 10);
		parameterTypes.add(parameter10);

		ParameterTypeInt parameter11 = new ParameterTypeInt(PARAMETER_11,
				PARAMETER_11, 0, Integer.MAX_VALUE, 5);
		parameterTypes.add(parameter11);

		ParameterTypeInt parameter12 = new ParameterTypeInt(PARAMETER_12,
				PARAMETER_12, 0, Integer.MAX_VALUE, 1);
		parameterTypes.add(parameter12);

		ParameterTypeInt parameter13 = new ParameterTypeInt(PARAMETER_13,
				PARAMETER_13, 0, Integer.MAX_VALUE, 1);
		parameterTypes.add(parameter13);

		return parameterTypes;
	}

	private ETMParam getConfiguration(XLogIOObject log, PluginContext context) {
		ETMParam param;
		try {
			 param = ETMParamFactory.buildParam(
				    log.getArtifact(),
					context,
					getParameterAsInt(PARAMETER_1),
					getParameterAsInt(PARAMETER_2),
					getParameterAsInt(PARAMETER_3),
					getParameterAsDouble(PARAMETER_4),
					getParameterAsDouble(PARAMETER_5),
					true, // always remove duplicates
					getParameterAsInt(PARAMETER_6),
					getParameterAsDouble(PARAMETER_7),
					getParameterAsDouble(PARAMETER_10),
					getParameterAsDouble(PARAMETER_8),
					getParameterAsDouble(PARAMETER_9),
					getParameterAsDouble(PARAMETER_11),
					getParameterAsDouble(PARAMETER_12),
					getParameterAsDouble(PARAMETER_13),
					null,
					0.0);
			 
			
			 param.setRng(ETMParam.createRNG());
			 
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
			param = null;
		}
		return param;
	}

}
