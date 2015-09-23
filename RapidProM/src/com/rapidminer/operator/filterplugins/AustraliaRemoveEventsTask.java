package com.rapidminer.operator.filterplugins;

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

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.deckfour.xes.model.XLog;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjectrenderers.XLogIOObjectRenderer;
import com.rapidminer.callprom.ClassLoaderUtils;

import java.io.File;

public class AustraliaRemoveEventsTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("prom context", ProMContextIOObject.class);
	private InputPort inputLog = getInputPorts().createPort("prom XLog", XLogIOObject.class);
	private OutputPort outputXLog = getOutputPorts().createPort("prom XLog");

	public AustraliaRemoveEventsTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputXLog, XLogIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Australia Remove Events", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		XLogIOObject data = inputLog.getData(XLogIOObject.class);
		List<Object> pars = new ArrayList<Object>();
		XLog xLog = data.getPromLog();
		pars.add(xLog);
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Australia Remove Events", pars);
		XLogIOObject xLogIOObject = new XLogIOObject((XLog) runPlugin[0]);
		//xLogIOObject.setPluginContext(pluginContext);
		outputXLog.deliver(xLogIOObject);
		logService.log("end do work Australia Remove Events", LogService.NOTE);
	}

}

