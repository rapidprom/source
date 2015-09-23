package com.rapidminer.operator.conversionplugins;

import java.util.*;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.ioobjects.ReachabilityGraphIOObject;

import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;
import org.rapidprom.prom.CallProm;

public class ConstructReachabilityGraphofaPetriNetTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputPetrinet = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private InputPort inputMarking = getInputPorts().createPort("marking (ProM Marking)", MarkingIOObject.class);

	private OutputPort outputReachabilityGraph = getOutputPorts().createPort("model (ProM Reachability Graph)");
	//private OutputPort outputReachabilitySet = getOutputPorts().createPort("prom ReachabilitySet");
	//private OutputPort outputStartStateSet = getOutputPorts().createPort("prom StartStateSet");
	//private OutputPort outputAcceptStateSet = getOutputPorts().createPort("prom AcceptStateSet");

	public ConstructReachabilityGraphofaPetriNetTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputReachabilityGraph, ReachabilityGraphIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Construct Reachability Graph of a Petri Net", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		
		PetriNetIOObject Petrinetdata = inputPetrinet.getData(PetriNetIOObject.class);
		pars.add(Petrinetdata.getData());

		MarkingIOObject Markingdata = inputMarking.getData(MarkingIOObject.class);
		pars.add(Markingdata.getData());
		
		if(Petrinetdata.getData() == null || Markingdata.getData() == null)
			System.out.println("Inputs are null");
		

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XXX12", "Construct Reachability Graph of a Petri Net", pars);
		ReachabilityGraphIOObject reachabilityGraphIOObject = new ReachabilityGraphIOObject((ReachabilityGraph) runPlugin[0]);
		reachabilityGraphIOObject.setPluginContext(pluginContext);
		outputReachabilityGraph.deliver(reachabilityGraphIOObject);
		
		logService.log("end do work Construct Reachability Graph of a Petri Net", LogService.NOTE);
	}

}
