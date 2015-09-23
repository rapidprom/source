package com.rapidminer.operator.analysisplugins;

import java.util.*;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.Utilities;
import com.rapidminer.parameter.*;
import com.rapidminer.parameters.*;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import com.rapidminer.ioobjects.MutableFuzzyGraphIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.FuzzyAnimationIOObject;
import org.processmining.plugins.fuzzymodel.anim.FuzzyAnimation;

public class AnimateEventLoginFuzzyInstanceTask extends Operator {

	private List<Parameter> parametersAnimateEventLoginFuzzyInstance = null;
	
	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputMutableFuzzyGraph = getInputPorts().createPort("instance (MutableFuzzyGraph)", MutableFuzzyGraphIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputFuzzyAnimation = getOutputPorts().createPort("model (FuzzyAnimation)");

	public AnimateEventLoginFuzzyInstanceTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputFuzzyAnimation, FuzzyAnimationIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Animate Event Log in Fuzzy Instance", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		MutableFuzzyGraphIOObject MutableFuzzyGraphdata = inputMutableFuzzyGraph.getData(MutableFuzzyGraphIOObject.class);
		pars.add(MutableFuzzyGraphdata.getData());

		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());
		
		int[] parameters = getConfiguration(parametersAnimateEventLoginFuzzyInstance);

		/*CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Animate Event Log in Fuzzy Instance", pars);*/
		
		FuzzyAnimation animation = new FuzzyAnimation(pluginContext, MutableFuzzyGraphdata.getData(), XLogdata.getData(), parameters[1],parameters[2]);
		animation.initialize(pluginContext, MutableFuzzyGraphdata.getData(), XLogdata.getData());
		
		
		FuzzyAnimationIOObject fuzzyAnimationIOObject = new FuzzyAnimationIOObject(animation);
		fuzzyAnimationIOObject.setPluginContext(pluginContext);
		outputFuzzyAnimation.deliver(fuzzyAnimationIOObject);
		logService.log("end do work Animate Event Log in Fuzzy Instance", LogService.NOTE);
	}
	
	public List<ParameterType> getParameterTypes() {
		loadRequiredClasses();
		this.parametersAnimateEventLoginFuzzyInstance = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		ParameterBoolean parameter1 = new ParameterBoolean(false, Boolean.class, "Discrete animation (inject timestamps)", "Enable this is if you want to inject timestamps and make the dots move accordingly to these.");
		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), false, false);
		parameterTypes.add(parameterType1);
		parametersAnimateEventLoginFuzzyInstance.add(parameter1);
		
		ParameterInteger parameter2 = new ParameterInteger(5, 1, 25, 1, Integer.class, "Lookahead", "");
		ParameterTypeInt parameterType2 = new ParameterTypeInt(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), 1, 25, 5, false);
		parameterTypes.add(parameterType2);
		parametersAnimateEventLoginFuzzyInstance.add(parameter2);
		
		ParameterInteger parameter3 = new ParameterInteger(3, 0, 15, 1, Integer.class, "Extra lookahead", "");
		ParameterTypeInt parameterType3 = new ParameterTypeInt(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), 0, 15, 3, false);
		parameterTypes.add(parameterType3);
		parametersAnimateEventLoginFuzzyInstance.add(parameter3);

		return parameterTypes;
	}

	private int[] getConfiguration(List<Parameter> parametersAnimateEventLoginFuzzyInstance) { //only use the parameters you need
		
		int[] result = new int[3];
		
		try {
			if(getParameterAsBoolean(parametersAnimateEventLoginFuzzyInstance.get(0).getNameParameter()) ==true)
				result[0] = 1;
			else
				result[0] = 0;
			result[1] = getParameterAsInt(parametersAnimateEventLoginFuzzyInstance.get(1).getNameParameter());
			result[2] = getParameterAsInt(parametersAnimateEventLoginFuzzyInstance.get(2).getNameParameter());
		} 
		catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	private void loadRequiredClasses () {
		Utilities.loadRequiredClasses();
	}
}
