package org.rapidprom.operator.discovery;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.parameter.*;

import org.processmining.framework.plugin.PluginContext;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.NRootAttenuation;
import org.processmining.plugins.fuzzymodel.miner.FuzzyMinerPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.MetricsRepositoryIOObject;
import org.rapidprom.operator.abstr.AbstractRapidProMDiscoveryOperator;

public class FuzzyMinerOperator extends AbstractRapidProMDiscoveryOperator {

	private static final String
	PARAMETER_1 = "Frequency significance metric (Unitary)",
	PARAMETER_2 = "Routing significance metric (Unitary)",
	PARAMETER_3 = "Frequency significance (Binary)",
	PARAMETER_4 = "Distance significance (Binary)",
	PARAMETER_5 = "Proximity correlation (Binary)",
	PARAMETER_6 = "Endpoint correlation (Binary)",
	PARAMETER_7 = "Originator correlation (Binary)",
	PARAMETER_8 = "Data type correlation (Binary)",
	PARAMETER_9 = "Data value correlation (Binary)",
	PARAMETER_10 = "Maximum Distance";
	
	private OutputPort outputMetricsRepository = getOutputPorts().createPort(
			"model (ProM MetricsRepository)");

	public FuzzyMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputMetricsRepository,
						MetricsRepositoryIOObject.class));
	}

	public void doWork() throws OperatorException {
		
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: fuzzy miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(FuzzyMinerPlugin.class);

		MetricsRepository metricsRepository = getMetricsConfiguration();
		Attenuation attenuation = new NRootAttenuation(2.7, 5);
		int maxDistance = getParameterAsInt(PARAMETER_10);

		FuzzyMinerPlugin executer = new FuzzyMinerPlugin();
		MetricsRepositoryIOObject metricsRepositoryIOObject = new MetricsRepositoryIOObject(
				executer.mineGeneric(pluginContext, getXLog(),
						metricsRepository, attenuation, maxDistance),pluginContext);

		outputMetricsRepository.deliver(metricsRepositoryIOObject);
		logger.log(Level.INFO,
				"End: heuristics miner (" + (System.currentTimeMillis() - time)
						/ 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeDouble parameter1 = new ParameterTypeDouble(
				PARAMETER_1,
				"Measures the significance of events by their relative frequency (Weight)",
				0, 1, 1);
		parameterTypes.add(parameter1);

		ParameterTypeDouble parameter2 = new ParameterTypeDouble(
				PARAMETER_2,
				"Measures the significance of a node by weighting incoming against outgoing relations (Weight)",
				0, 1, 1);
		parameterTypes.add(parameter2);		

		ParameterTypeDouble parameter3 = new ParameterTypeDouble(
				PARAMETER_3,
				"Measures the significance of two events by the frequency of their consecutive observation (Weight)",
				0, 1, 1);
		parameterTypes.add(parameter3);
		
		ParameterTypeDouble parameter4 = new ParameterTypeDouble(
				PARAMETER_4,
				"Measures the significance by the distance in significance of a link with its endpoints (Weight)", 
				0,
				1, 1);
		parameterTypes.add(parameter4);
		
		ParameterTypeDouble parameter5 = new ParameterTypeDouble(
				PARAMETER_5,
				"Measures the correlation of two events by their temporal proximity (Weight)",
				0, 1, 1);
		parameterTypes.add(parameter5);
		
		ParameterTypeDouble parameter6 = new ParameterTypeDouble(
				PARAMETER_6,
				"Measures the correlation of two events by the similarity of their element name strings (Weight)",
				0, 1, 1);
		parameterTypes.add(parameter6);

		ParameterTypeDouble parameter7 = new ParameterTypeDouble(
				PARAMETER_7,
				"Measures the coorrelation of two events by the similarity of their originator strings (Weight)",
				0, 1, 1);
		parameterTypes.add(parameter7);
		
		ParameterTypeDouble parameter8 = new ParameterTypeDouble(
				PARAMETER_8,
				"Measures the coorrelation of two events by their relative overlap of attribute types (Weight)", 0,
				1, 1);
		parameterTypes.add(parameter8);

		ParameterTypeDouble parameter9 = new ParameterTypeDouble(
				PARAMETER_9,
				"Measures the significance of two events by their relative overlap of attribute values (Weight)",
				0,
				1, 1);
		parameterTypes.add(parameter9);

		// maxDistance
		
		ParameterTypeInt parameter10 = new ParameterTypeInt(PARAMETER_10,
				PARAMETER_10, 0, 100, 1);
		parameterTypes.add(parameter10);		

		return parameterTypes;
	}

	private MetricsRepository getMetricsConfiguration() { 												
		
		XLogInfo logInfo = null;
		try {
			logInfo = XLogInfoFactory
					.createLogInfo(getXLog(), getXEventClassifier());
		} catch (UserError e) {
			e.printStackTrace();
		}
		MetricsRepository metrics = MetricsRepository.createRepository(logInfo);
		try {
			metrics.getUnaryLogMetrics().get(0)
					.setNormalizationMaximum(getParameterAsDouble(PARAMETER_1));
			metrics.getUnaryDerivateMetrics().get(0)
					.setNormalizationMaximum(getParameterAsDouble(PARAMETER_2));
			metrics.getSignificanceBinaryLogMetrics().get(0)
					.setNormalizationMaximum(getParameterAsDouble(PARAMETER_3));
			metrics.getCorrelationBinaryLogMetrics().get(0)
					.setNormalizationMaximum(getParameterAsDouble(PARAMETER_5));
			metrics.getCorrelationBinaryLogMetrics().get(1)
					.setNormalizationMaximum(getParameterAsDouble(PARAMETER_6));
			metrics.getCorrelationBinaryLogMetrics().get(2)
					.setNormalizationMaximum(getParameterAsDouble(PARAMETER_7));
			metrics.getCorrelationBinaryLogMetrics().get(3)
					.setNormalizationMaximum(getParameterAsDouble(PARAMETER_8));
			metrics.getCorrelationBinaryLogMetrics().get(4)
					.setNormalizationMaximum(getParameterAsDouble(PARAMETER_9));
			metrics.getSignificanceBinaryMetrics().get(1)
					.setNormalizationMaximum(getParameterAsDouble(PARAMETER_4));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metrics;
	}
}
