package com.rapidminer.operator.miningplugins;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjectrenderers.PetriNetIOObjectRenderer;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;

public class AlphaMinerTask extends Operator {
	
	/** defining the ports */
	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort output = getOutputPorts().createPort("model (ProM Petri Net)");
	private OutputPort outputMarking = getOutputPorts().createPort("marking (ProM marking)");
	
	/**
	 * The default constructor needed in exactly this signature
	 */
	public AlphaMinerTask(OperatorDescription description) {
		super(description);
		
		/** Adding a rule for the output */
		getTransformer().addRule( new GenerateNewMDRule(output, PetriNetIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputMarking, MarkingIOObject.class));
	}
	
	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do work alpha miner", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		// get the log
		XLogIOObject log = inputLog.getData(XLogIOObject.class);
		XLog promLog = log.getData();
		CallProm tp = new CallProm();
		if (pluginContext == null) {
			System.out.println("pluginContext is null");
		}
		else {
			System.out.println("pluginContext is not null");
		}
		
		List<Object> pars = new ArrayList<Object>();
		pars.add(promLog);
		Object[] runPlugin = tp.runPlugin(pluginContext, "3", "Alpha Miner", pars);
		
		PetriNetIOObject petriNetIOObject = new PetriNetIOObject((Petrinet) runPlugin[0]);
		petriNetIOObject.setPluginContext(pluginContext);
		// add to list so that afterwards it can be cleared if needed
		
		MarkingIOObject marking = new MarkingIOObject((Marking) runPlugin[1]);
		marking.setPluginContext(pluginContext);
		
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(petriNetIOObject);
		
		
		output.deliver(petriNetIOObject);
		outputMarking.deliver(marking);
		
		System.out.println("LABEL IS " + ((Petrinet) runPlugin[0]).getLabel());
		logService.log("end do work alpha miner", LogService.NOTE);

	}
	
}
