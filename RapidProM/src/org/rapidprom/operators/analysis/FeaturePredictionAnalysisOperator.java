package org.rapidprom.operators.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.prediction.PredictionPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PredictorIOObject;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class FeaturePredictionAnalysisOperator extends Operator {

	private InputPort inputXLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputPredictor = getOutputPorts().createPort(
			"model (ProM Predictor)");

	public FeaturePredictionAnalysisOperator(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(
						new GenerateNewMDRule(outputPredictor,
								PredictorIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: feature prediction");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(PredictionPlugin.class);

		PredictionPlugin predictor = new PredictionPlugin();

		PredictorIOObject predictorIOObject = null;
		try {
			predictorIOObject = new PredictorIOObject(
					predictor.performPrediction(pluginContext, inputXLog
							.getData(XLogIOObject.class).getArtifact()),
					pluginContext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outputPredictor.deliver(predictorIOObject);
		logger.log(Level.INFO,
				"End: feature prediction ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
