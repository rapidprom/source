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

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.prom.CallProm;

public class TransitionSystemtoPetrinetTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputTransitionSystem = getInputPorts().createPort("model (ProM Reachability Graph)", ReachabilityGraphIOObject.class);
	
	private OutputPort outputPetrinet = getOutputPorts().createPort("model (ProM Petri Net)");
	private OutputPort outputMarking = getOutputPorts().createPort("marking (ProM Marking)");

	public TransitionSystemtoPetrinetTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputMarking, MarkingIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		
		logService.log("start do work Transition System to Petrinet", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		
		ReachabilityGraphIOObject TransitionSystemdata = inputTransitionSystem.getData(ReachabilityGraphIOObject.class);
		pars.add(TransitionSystemdata.getData());

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Transition System to Petrinet", pars);
		
		PetriNetIOObject petrinetIOObject = new PetriNetIOObject((Petrinet) runPlugin[0]);
		petrinetIOObject.setPluginContext(pluginContext);
		outputPetrinet.deliver(petrinetIOObject);
		
		MarkingIOObject markingIOObject = new MarkingIOObject((Marking) runPlugin[1]);
		markingIOObject.setPluginContext(pluginContext);
		outputMarking.deliver(markingIOObject);
		
		logService.log("end do work Transition System to Petrinet", LogService.NOTE);
	}

}
