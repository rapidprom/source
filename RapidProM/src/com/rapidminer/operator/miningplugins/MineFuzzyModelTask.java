package com.rapidminer.operator.miningplugins;

import java.util.*;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.Utilities;
import com.rapidminer.parameter.*;
import com.rapidminer.parameters.*;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.NRootAttenuation;
import org.rapidprom.prom.CallProm;

import java.lang.Integer;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.MetricsRepositoryIOObject;

public class MineFuzzyModelTask extends Operator {

	private List<Parameter> parametersMineFuzzyModel = null;

	private InputPort inputContext = getInputPorts().createPort("context", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log", XLogIOObject.class);
	private OutputPort outputMetricsRepository = getOutputPorts().createPort("model MetricsRepository");

	public MineFuzzyModelTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputMetricsRepository, MetricsRepositoryIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Mine Fuzzy Model", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

		MetricsRepository metricsRepository = getMetricsConfiguration(this.parametersMineFuzzyModel);
		pars.add(metricsRepository);
		
		Attenuation attenuation = new NRootAttenuation(2.7, 5);
		pars.add(attenuation);
		
		int maxDistance = getIntegerConfiguration(this.parametersMineFuzzyModel);
		pars.add(maxDistance);
		
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine Fuzzy Model", pars);
		MetricsRepositoryIOObject metricsRepositoryIOObject = new MetricsRepositoryIOObject((MetricsRepository) runPlugin[0]);
		metricsRepositoryIOObject.setPluginContext(pluginContext);
		outputMetricsRepository.deliver(metricsRepositoryIOObject);
		logService.log("end do work Mine Fuzzy Model", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		loadRequiredClasses();
		this.parametersMineFuzzyModel = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		//MetricsRepository parameters

		ParameterDouble parameter1 = new ParameterDouble(1, 0, 1, 0, Double.class, "Frequency significance metric (Unitary)", "Measures the significance of events by their relative frequency (Weight)");
		ParameterTypeDouble parameterType1 = new ParameterTypeDouble(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), 1 );
		parameterTypes.add(parameterType1);
		parametersMineFuzzyModel.add(parameter1);
		
		ParameterDouble parameter2 = new ParameterDouble(1, 0, 1, 0, Double.class, "Routing significance metric (Unitary)", "Measures the significance of a node by weighting incoming against outgoing relations (Weight)");
		ParameterTypeDouble parameterType2 = new ParameterTypeDouble(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getMin(), parameter2.getMax(), 1 );
		parameterTypes.add(parameterType2);
		parametersMineFuzzyModel.add(parameter2);
		
		ParameterDouble parameter3 = new ParameterDouble(1, 0, 1, 0, Double.class, "Frequency significance (Binary)", "Measures the significance of two events by the frequency of their consecutive observation (Weight)");
		ParameterTypeDouble parameterType3 = new ParameterTypeDouble(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getMin(), parameter3.getMax(), 1 );
		parameterTypes.add(parameterType3);
		parametersMineFuzzyModel.add(parameter3);

		ParameterDouble parameter4 = new ParameterDouble(1, 0, 1, 0, Double.class, "Distance significance (Binary)", "Measures the significance by the distance in significance of a link with its endpoints (Weight)");
		ParameterTypeDouble parameterType4 = new ParameterTypeDouble(parameter4.getNameParameter(), parameter4.getDescriptionParameter(), parameter4.getMin(), parameter4.getMax(), 1 );
		parameterTypes.add(parameterType4);
		parametersMineFuzzyModel.add(parameter4);
		
		ParameterDouble parameter5 = new ParameterDouble(1, 0, 1, 0, Double.class, "Proximity correlation (Binary)", "Measures the correlation of two events by their temporal proximity (Weight)");
		ParameterTypeDouble parameterType5 = new ParameterTypeDouble(parameter5.getNameParameter(), parameter5.getDescriptionParameter(), parameter5.getMin(), parameter5.getMax(), 1 );
		parameterTypes.add(parameterType5);
		parametersMineFuzzyModel.add(parameter5);
		
		ParameterDouble parameter6 = new ParameterDouble(1, 0, 1, 0, Double.class, "Endpoint correlation (Binary)", "Measures the correlation of two events by the similarity of their element name strings (Weight)");
		ParameterTypeDouble parameterType6 = new ParameterTypeDouble(parameter6.getNameParameter(), parameter6.getDescriptionParameter(), parameter6.getMin(), parameter6.getMax(), 1 );
		parameterTypes.add(parameterType6);
		parametersMineFuzzyModel.add(parameter6);
		
		ParameterDouble parameter7 = new ParameterDouble(1, 0, 1, 0, Double.class, "Originator correlation (Binary)", "Measures the coorrelation of two events by the similarity of their originator strings (Weight)");
		ParameterTypeDouble parameterType7 = new ParameterTypeDouble(parameter7.getNameParameter(), parameter7.getDescriptionParameter(), parameter7.getMin(), parameter7.getMax(), 1 );
		parameterTypes.add(parameterType7);
		parametersMineFuzzyModel.add(parameter7);
		
		ParameterDouble parameter8 = new ParameterDouble(1, 0, 1, 0, Double.class, "Data type correlation (Binary)", "Measures the coorrelation of two events by their relative overlap of attribute types (Weight)");
		ParameterTypeDouble parameterType8 = new ParameterTypeDouble(parameter8.getNameParameter(), parameter8.getDescriptionParameter(), parameter8.getMin(), parameter8.getMax(), 1 );
		parameterTypes.add(parameterType8);
		parametersMineFuzzyModel.add(parameter8);
		
		ParameterDouble parameter9 = new ParameterDouble(1, 0, 1, 0, Double.class, "Data value correlation (Binary)", "Measures the significance of two events by their relative overlap of attribute values (Weight)");
		ParameterTypeDouble parameterType9 = new ParameterTypeDouble(parameter9.getNameParameter(), parameter9.getDescriptionParameter(), parameter9.getMin(), parameter9.getMax(), 1 );
		parameterTypes.add(parameterType9);
		parametersMineFuzzyModel.add(parameter9);
		
		//maxDistance

		ParameterInteger parameter10 = new ParameterInteger(1, 0, 100, 1, Integer.class, "Maximum Distance", "");
		ParameterTypeInt parameterType10 = new ParameterTypeInt(parameter10.getNameParameter(), parameter10.getDescriptionParameter(), parameter10.getMin(), parameter10.getMax(), 1 );
		parameterTypes.add(parameterType10);
		parametersMineFuzzyModel.add(parameter10);
		
		
		
		return parameterTypes;
	}

	private MetricsRepository getMetricsConfiguration(List<Parameter> parametersMineFuzzyModel) { //only use the parameters you need
		
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		XLogInfo logInfo = null;
		try 
		{
			logInfo = XLogInfoFactory.createLogInfo(inputXLog.getData(XLogIOObject.class).getXLog(), classifier);
		} 
		catch (UserError e) 
		{
			e.printStackTrace();
		}
		
		MetricsRepository metrics = MetricsRepository.createRepository(logInfo);
		
		try
		{
			metrics.getUnaryLogMetrics().get(0).setNormalizationMaximum(getParameterAsDouble((parametersMineFuzzyModel.get(0)).getNameParameter()));
			metrics.getUnaryDerivateMetrics().get(0).setNormalizationMaximum(getParameterAsDouble((parametersMineFuzzyModel.get(1)).getNameParameter()));
			metrics.getSignificanceBinaryLogMetrics().get(0).setNormalizationMaximum(getParameterAsDouble((parametersMineFuzzyModel.get(2)).getNameParameter()));
			metrics.getCorrelationBinaryLogMetrics().get(0).setNormalizationMaximum(getParameterAsDouble((parametersMineFuzzyModel.get(4)).getNameParameter()));
			metrics.getCorrelationBinaryLogMetrics().get(1).setNormalizationMaximum(getParameterAsDouble((parametersMineFuzzyModel.get(5)).getNameParameter()));
			metrics.getCorrelationBinaryLogMetrics().get(2).setNormalizationMaximum(getParameterAsDouble((parametersMineFuzzyModel.get(6)).getNameParameter()));
			metrics.getCorrelationBinaryLogMetrics().get(3).setNormalizationMaximum(getParameterAsDouble((parametersMineFuzzyModel.get(7)).getNameParameter()));
			metrics.getCorrelationBinaryLogMetrics().get(4).setNormalizationMaximum(getParameterAsDouble((parametersMineFuzzyModel.get(8)).getNameParameter()));
			metrics.getSignificanceBinaryMetrics().get(1).setNormalizationMaximum(getParameterAsDouble((parametersMineFuzzyModel.get(3)).getNameParameter()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		return metrics;
	}
	
	private int getIntegerConfiguration(List<Parameter> parametersMineFuzzyModel) { //only use the parameters you need
		
		
		int result = 4;
		try 
		{
			result = getParameterAsInt(parametersMineFuzzyModel.get(9).getNameParameter());
		} 
		catch (UndefinedParameterError e) 
		{
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	private void loadRequiredClasses () {
		Utilities.loadRequiredClasses();
	}

}
