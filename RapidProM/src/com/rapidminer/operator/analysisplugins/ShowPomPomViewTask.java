package com.rapidminer.operator.analysisplugins;

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
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.PomPomViewIOObject;

import org.processmining.plugins.pompom.PomPomView;
import org.rapidprom.prom.CallProm;

public class ShowPomPomViewTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputPetrinet = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputPomPomView = getOutputPorts().createPort("model (ProM PomPomView)");

	public ShowPomPomViewTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPomPomView, PomPomViewIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Show PomPom View", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		
		List<Object> pars = new ArrayList<Object>();
		PetriNetIOObject Petrinetdata = inputPetrinet.getData(PetriNetIOObject.class);
		pars.add(Petrinetdata.getData());

		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Show PomPom View", pars);
		
		PomPomViewIOObject pomPomViewIOObject = new PomPomViewIOObject((PomPomView) runPlugin[0]);
		pomPomViewIOObject.setPluginContext(pluginContext);
		outputPomPomView.deliver(pomPomViewIOObject);
		
		logService.log("end do work Show PomPom View", LogService.NOTE);
	}

}
