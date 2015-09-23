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
import com.rapidminer.ioobjects.XLogIOObject;

import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;

import com.rapidminer.ioobjects.MetricsRepositoryIOObject;
import com.rapidminer.ioobjectrenderers.MetricsRepositoryIOObjectRenderer;

import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;

import com.rapidminer.ioobjects.MutableFuzzyGraphIOObject;
import com.rapidminer.ioobjectrenderers.MutableFuzzyGraphIOObjectRenderer;

import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.rapidprom.prom.CallProm;

public class SelectBestFuzzyInstanceTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputMetricsRepository = getInputPorts().createPort("model (MetricsRepository)", MetricsRepositoryIOObject.class);
	private OutputPort outputMutableFuzzyGraph = getOutputPorts().createPort("instance (MutableFuzzyGraph)");

	public SelectBestFuzzyInstanceTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputMutableFuzzyGraph, MutableFuzzyGraphIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Select Best Fuzzy Instance", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		MetricsRepositoryIOObject MetricsRepositorydata = inputMetricsRepository.getData(MetricsRepositoryIOObject.class);
		pars.add(MetricsRepositorydata.getData());

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Select Best Fuzzy Instance", pars);
		MutableFuzzyGraphIOObject mutableFuzzyGraphIOObject = new MutableFuzzyGraphIOObject((MutableFuzzyGraph) runPlugin[0]);
		mutableFuzzyGraphIOObject.setPluginContext(pluginContext);
		outputMutableFuzzyGraph.deliver(mutableFuzzyGraphIOObject);
		logService.log("end do work Select Best Fuzzy Instance", LogService.NOTE);
	}

}
