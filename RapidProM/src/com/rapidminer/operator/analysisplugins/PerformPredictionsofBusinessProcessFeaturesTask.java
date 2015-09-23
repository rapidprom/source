package com.rapidminer.operator.analysisplugins;

import java.util.*;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.parameter.*;
import com.rapidminer.parameters.*;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.deckfour.xes.model.XLog;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjectrenderers.XLogIOObjectRenderer;

import org.deckfour.xes.model.XLog;

import com.rapidminer.ioobjects.PredictorIOObject;
import com.rapidminer.ioobjectrenderers.PredictorIOObjectRenderer;

import org.processmining.prediction.Predictor;
import org.rapidprom.prom.CallProm;

public class PerformPredictionsofBusinessProcessFeaturesTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputPredictor = getOutputPorts().createPort("model (ProM Predictor)");

	public PerformPredictionsofBusinessProcessFeaturesTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPredictor, PredictorIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Perform Predictions of Business Process Features", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Perform Predictions of Business Process Features", pars);
		PredictorIOObject predictorIOObject = new PredictorIOObject((Predictor) runPlugin[0]);
		predictorIOObject.setPluginContext(pluginContext);
		outputPredictor.deliver(predictorIOObject);
		logService.log("end do work Perform Predictions of Business Process Features", LogService.NOTE);
	}

}
