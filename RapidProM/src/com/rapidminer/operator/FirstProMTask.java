package com.rapidminer.operator;

import java.util.ArrayList;

import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.OutputFirstProMTaskIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.LogService;

public class FirstProMTask extends Operator {
	
	/** defining the ports */
	private InputPort input = getInputPorts().createPort("prom context", ProMContextIOObject.class);
	private OutputPort output = getOutputPorts().createPort("first prom task");
	
	/**
	 * The default constructor needed in exactly this signature
	 */
	public FirstProMTask(OperatorDescription description) {
		super(description);
		
		/** Adding a rule for meta data transformation: GameData will be passed through */
		//getTransformer().addPassThroughRule(gameDataInput, gameDataOutput);
	}

	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do work first prom task", LogService.NOTE);
		ProMContextIOObject context = input.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		CallProm tp = new CallProm();
		if (pluginContext == null) {
			System.out.println("pluginContext is null");
		}
		else {
			System.out.println("pluginContext is not null");
		}
		//Object runPlugin = tp.runPlugin(pluginContext, "1", "Create simple scientific workflow", new ArrayList<Class<?>>(), new ArrayList<Object>());
		Object runPlugin = tp.runPlugin(pluginContext, "1", "Create simple scientific workflow", new ArrayList<Object>());
		output.deliver(new OutputFirstProMTaskIOObject(runPlugin));
		logService.log("end do work first prom task", LogService.NOTE);
	}

}
