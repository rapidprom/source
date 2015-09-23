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

import org.deckfour.xes.model.XLog;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjectrenderers.XLogIOObjectRenderer;

import org.deckfour.xes.model.XLog;

import com.rapidminer.callprom.ClassLoaderUtils;

import java.io.File;

public class CheckTimestampFilterTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("prom context", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("prom XLog", XLogIOObject.class);
	private OutputPort outputXLog = getOutputPorts().createPort("prom XLog");

	public CheckTimestampFilterTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputXLog, XLogIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Check Timestamp Filter", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Check Timestamp Filter", pars);
		XLogIOObject xLogIOObject = new XLogIOObject((XLog) runPlugin[0]);
		xLogIOObject.setPluginContext(pluginContext);
		outputXLog.deliver(xLogIOObject);
		logService.log("end do work Check Timestamp Filter", LogService.NOTE);
	}

}

