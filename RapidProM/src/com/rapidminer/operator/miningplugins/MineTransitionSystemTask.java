package com.rapidminer.operator.miningplugins;

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

import com.rapidminer.ioobjects.TSMinerTransitionSystemIOObject;
import com.rapidminer.ioobjects.DirectedGraphElementWeightsIOObject;
import com.rapidminer.ioobjects.StartStateSetIOObject;
import com.rapidminer.ioobjects.AcceptStateSetIOObject;
import com.rapidminer.ioobjectrenderers.TSMinerTransitionSystemIOObjectRenderer;
import com.rapidminer.ioobjectrenderers.DirectedGraphElementWeightsIOObjectRenderer;
import com.rapidminer.ioobjectrenderers.StartStateSetIOObjectRenderer;
import com.rapidminer.ioobjectrenderers.AcceptStateSetIOObjectRenderer;

import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.rapidprom.prom.CallProm;

public class MineTransitionSystemTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log", XLogIOObject.class);
	private OutputPort outputTSMinerTransitionSystem = getOutputPorts().createPort("TSMinerTransitionSystem");
	private OutputPort outputDirectedGraphElementWeights = getOutputPorts().createPort("DirectedGraphElementWeights");
	private OutputPort outputStartStateSet = getOutputPorts().createPort("StartStateSet");
	private OutputPort outputAcceptStateSet = getOutputPorts().createPort("AcceptStateSet");

	public MineTransitionSystemTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputTSMinerTransitionSystem, TSMinerTransitionSystemIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputDirectedGraphElementWeights, DirectedGraphElementWeightsIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputStartStateSet, StartStateSetIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputAcceptStateSet, AcceptStateSetIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Mine Transition System", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine Transition System", pars);
		TSMinerTransitionSystemIOObject tSMinerTransitionSystemIOObject = new TSMinerTransitionSystemIOObject((TSMinerTransitionSystem) runPlugin[0]);
		tSMinerTransitionSystemIOObject.setPluginContext(pluginContext);
		outputTSMinerTransitionSystem.deliver(tSMinerTransitionSystemIOObject);
		DirectedGraphElementWeightsIOObject directedGraphElementWeightsIOObject = new DirectedGraphElementWeightsIOObject((DirectedGraphElementWeights) runPlugin[1]);
		directedGraphElementWeightsIOObject.setPluginContext(pluginContext);
		outputDirectedGraphElementWeights.deliver(directedGraphElementWeightsIOObject);
		StartStateSetIOObject startStateSetIOObject = new StartStateSetIOObject((StartStateSet) runPlugin[2]);
		startStateSetIOObject.setPluginContext(pluginContext);
		outputStartStateSet.deliver(startStateSetIOObject);
		AcceptStateSetIOObject acceptStateSetIOObject = new AcceptStateSetIOObject((AcceptStateSet) runPlugin[3]);
		acceptStateSetIOObject.setPluginContext(pluginContext);
		outputAcceptStateSet.deliver(acceptStateSetIOObject);
		logService.log("end do work Mine Transition System", LogService.NOTE);
	}

}
