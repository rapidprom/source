package com.rapidminer.operator.conversionplugins;

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
import com.rapidminer.ioobjects.ProcessTreeIOObject;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.MarkingIOObject;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.prom.CallProm;

public class ConvertProcessTreetoPetriNetTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputProcessTree = getInputPorts().createPort("model (ProM ProcessTree)", ProcessTreeIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts().createPort("model (ProM Petri Net)");
	private OutputPort outputMarkingInitial = getOutputPorts().createPort("marking initial (ProM Marking)");
	private OutputPort outputMarkingFinal = getOutputPorts().createPort("marking final (ProM Marking)");

	public ConvertProcessTreetoPetriNetTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputMarkingInitial, MarkingIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputMarkingFinal, MarkingIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Convert Process Tree to Petri Net", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		ProcessTreeIOObject ProcessTreedata = inputProcessTree.getData(ProcessTreeIOObject.class);
		pars.add(ProcessTreedata.getData());

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Convert Process Tree to Petri Net", pars);
		PetriNetIOObject petrinetIOObject = new PetriNetIOObject((Petrinet) runPlugin[0]);
		petrinetIOObject.setPluginContext(pluginContext);
		outputPetrinet.deliver(petrinetIOObject);
		MarkingIOObject markingIOObjectInitial = new MarkingIOObject((Marking) runPlugin[1]);
		markingIOObjectInitial.setPluginContext(pluginContext);
		outputMarkingInitial.deliver(markingIOObjectInitial);
		MarkingIOObject markingIOObjectFinal = new MarkingIOObject((Marking) runPlugin[2]);
		markingIOObjectFinal.setPluginContext(pluginContext);
		outputMarkingFinal.deliver(markingIOObjectFinal);
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(markingIOObjectFinal);
		instance.addToList(markingIOObjectInitial);
		instance.addToList(petrinetIOObject);
		logService.log("end do work Convert Process Tree to Petri Net", LogService.NOTE);
	}

}
