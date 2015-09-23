package com.rapidminer.operator.miningplugins;

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
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.InteractiveMinerLauncherIOObject;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner.*;
import org.rapidprom.prom.CallProm;

public class MinewithInductivevisualMinerTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputInteractiveMinerLauncher = getOutputPorts().createPort("model (ProM InteractiveVisualMiner)");

	public MinewithInductivevisualMinerTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputInteractiveMinerLauncher, InteractiveMinerLauncherIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Mine with Inductive visual Miner", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		
		List<Object> pars = new ArrayList<Object>();
		
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());
	
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine with Inductive visual Miner", pars);
		/*InductiveVisualMiner iminer = new InductiveVisualMiner();
		InteractiveMinerLauncher im = iminer.new InteractiveMinerLauncher(XLogdata.getData());*/
		InteractiveMinerLauncherIOObject interactiveMinerLauncherIOObject = new InteractiveMinerLauncherIOObject((InteractiveMinerLauncher) /*im*/ runPlugin[0]);
		interactiveMinerLauncherIOObject.setPluginContext(pluginContext);
		outputInteractiveMinerLauncher.deliver(interactiveMinerLauncherIOObject);
		
		logService.log("end do work Mine with Inductive visual Miner", LogService.NOTE);
	}

}
