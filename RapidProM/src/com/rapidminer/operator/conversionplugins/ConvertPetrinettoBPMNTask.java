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
import com.rapidminer.ioobjects.BPMNDiagramIOObject;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.rapidprom.prom.CallProm;

public class ConvertPetrinettoBPMNTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputPetrinet = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputBPMNDiagram = getOutputPorts().createPort("model (ProM BPMN)");

	public ConvertPetrinettoBPMNTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputBPMNDiagram, BPMNDiagramIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Convert Petrinet to BPMN", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		
		List<Object> pars = new ArrayList<Object>();
		
		PetriNetIOObject Petrinetdata = inputPetrinet.getData(PetriNetIOObject.class);
		pars.add(Petrinetdata.getData());

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Convert Petrinet to BPMN", pars);
		
		BPMNDiagramIOObject bPMNDiagramIOObject = new BPMNDiagramIOObject((BPMNDiagram) runPlugin[0]);
		bPMNDiagramIOObject.setPluginContext(pluginContext);
		outputBPMNDiagram.deliver(bPMNDiagramIOObject);
		
		logService.log("end do work Convert Petrinet to BPMN", LogService.NOTE);
	}

}
