package com.rapidminer.operator.analysisplugins;

import java.util.*;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.MarkingIOObject;

public class ReduceSilentTransitionsTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputPetrinet = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private InputPort inputMarking = getInputPorts().createPort("marking (ProM Marking)", MarkingIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts().createPort("model (ProM Petri Net)");
	private OutputPort outputMarking = getOutputPorts().createPort("marking (ProM Marking)");

	public ReduceSilentTransitionsTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputMarking, MarkingIOObject.class));
}

	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Reduce Silent Transitions", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		PetriNetIOObject Petrinetdata = inputPetrinet.getData(PetriNetIOObject.class);
		pars.add(Petrinetdata.getData());

		MarkingIOObject Markingdata = inputMarking.getData(MarkingIOObject.class);
		pars.add(Markingdata.getData());

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Reduce Silent Transitions", pars);
		PetriNetIOObject petrinetIOObject = new PetriNetIOObject((Petrinet) runPlugin[0]);
		petrinetIOObject.setPluginContext(pluginContext);
		outputPetrinet.deliver(petrinetIOObject);
		MarkingIOObject markingIOObject = new MarkingIOObject((Marking) runPlugin[1]);
		markingIOObject.setPluginContext(pluginContext);
		outputMarking.deliver(markingIOObject);
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(markingIOObject);
		instance.addToList(petrinetIOObject);
		//logService.log("end do work Reduce Silent Transitions", LogService.NOTE);
	}

}
