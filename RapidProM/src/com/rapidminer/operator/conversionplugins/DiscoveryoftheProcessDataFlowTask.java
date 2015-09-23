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

import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.PetriNetWithDataIOObject;
import com.rapidminer.ioobjects.MarkingIOObject;

import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.prom.CallProm;

public class DiscoveryoftheProcessDataFlowTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputPetrinetGraph = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	
	private OutputPort outputPetriNetWithData = getOutputPorts().createPort("model (ProM Petri Net with Data)");
	private OutputPort outputInitialMarking = getOutputPorts().createPort("initial marking (ProM Marking)");
	private OutputPort outputFinalMarking = getOutputPorts().createPort("final marking (ProM Marking)");

	public DiscoveryoftheProcessDataFlowTask (OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPetriNetWithData, PetriNetWithDataIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputInitialMarking, MarkingIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputFinalMarking, MarkingIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Discovery of the Process Data-Flow (Decision-Tree Miner)", LogService.NOTE);
		
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		
		PetriNetIOObject PetrinetGraphdata = inputPetrinetGraph.getData(PetriNetIOObject.class);
		pars.add(PetrinetGraphdata.getData());

		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());
		
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Discovery of the Process Data-Flow (Decision-Tree Miner)", pars);
		
		PetriNetWithDataIOObject petriNetWithDataIOObject = new PetriNetWithDataIOObject((PetriNetWithData) runPlugin[0]);
		petriNetWithDataIOObject.setPluginContext(pluginContext);
		outputPetriNetWithData.deliver(petriNetWithDataIOObject);
		
		MarkingIOObject markinginitialIOObject = new MarkingIOObject((Marking) runPlugin[1]);
		markinginitialIOObject.setPluginContext(pluginContext);
		outputInitialMarking.deliver(markinginitialIOObject);
		
		MarkingIOObject markingfinalIOObject = new MarkingIOObject((Marking) runPlugin[2]);
		markingfinalIOObject.setPluginContext(pluginContext);
		outputFinalMarking.deliver(markingfinalIOObject);
		
		logService.log("end do work Discovery of the Process Data-Flow (Decision-Tree Miner)", LogService.NOTE);
	}

}
