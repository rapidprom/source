package com.rapidminer.operator.miningplugins;

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

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.etm.parameters.ETMParam;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjectrenderers.XLogIOObjectRenderer;

import org.deckfour.xes.model.XLog;

import com.rapidminer.ioobjects.ProcessTreeIOObject;
import com.rapidminer.ioobjectrenderers.ProcessTreeIOObjectRenderer;

import org.processmining.processtree.ProcessTree;
import org.rapidprom.prom.CallProm;

public class MineaProcessTreewithETMdTask extends Operator {

	private List<Parameter> parametersMineaProcessTreewithETMd = null;

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputProcessTree = getOutputPorts().createPort("model (ProM ProcessTree)");

	public MineaProcessTreewithETMdTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputProcessTree, ProcessTreeIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Mine a Process Tree with ETMd", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

		ETMParam eTMParam = getConfiguration(this.parametersMineaProcessTreewithETMd);
		pars.add(eTMParam);
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine a Process Tree with ETMd", pars);
		ProcessTreeIOObject processTreeIOObject = new ProcessTreeIOObject((ProcessTree) runPlugin[0]);
		processTreeIOObject.setPluginContext(pluginContext);
		outputProcessTree.deliver(processTreeIOObject);
		logService.log("end do work Mine a Process Tree with ETMd", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		
		this.parametersMineaProcessTreewithETMd = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterInteger parameter1 = new ParameterInteger(20, 0, Integer.MAX_VALUE, 1, Integer.class, "Population Size", "");
		ParameterTypeInt parameterType1 = new ParameterTypeInt(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parametersMineaProcessTreewithETMd.add(parameter1);

		ParameterInteger parameter2 = new ParameterInteger(5, 0, Integer.MAX_VALUE, 1, Integer.class, "Elite Count", "");
		ParameterTypeInt parameterType2 = new ParameterTypeInt(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getMin(), parameter2.getMax(), parameter2.getDefaultValueParameter());
		parameterTypes.add(parameterType2);
		parametersMineaProcessTreewithETMd.add(parameter2);
		
		Object[] par3categories = new Object[] {"MXML Legacy Classifier", "Event Name", "Resource", "Lifecycle transition"};
		ParameterCategory parameter3 = new ParameterCategory(par3categories, "Event Name",String.class, "Event Classifier", "");
		ParameterTypeCategory parameterType3 = new ParameterTypeCategory(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getOptionsParameter(), parameter3.getIndexValue(parameter3.getDefaultValueParameter()));
		parameterTypes.add(parameterType3);
		parametersMineaProcessTreewithETMd.add(parameter3);

		ParameterInteger parameter4 = new ParameterInteger(10, 0, Integer.MAX_VALUE, 1, Integer.class, "Message Interval", "");
		ParameterTypeInt parameterType4 = new ParameterTypeInt(parameter4.getNameParameter(), parameter4.getDescriptionParameter(), parameter4.getMin(), parameter4.getMax(), parameter4.getDefaultValueParameter());
		parameterTypes.add(parameterType4);
		parametersMineaProcessTreewithETMd.add(parameter4);

		Object[] par5categories = new Object[] {"Edit distance - number of edits from reference model(s)", "Event Name", "Resource", "Lifecycle transition"};
		ParameterCategory parameter5 = new ParameterCategory(par5categories, "Event Name",String.class, "Event Classifier", "");
		ParameterTypeCategory parameterType5 = new ParameterTypeCategory(parameter5.getNameParameter(), parameter5.getDescriptionParameter(), parameter5.getOptionsParameter(), parameter5.getIndexValue(parameter5.getDefaultValueParameter()));
		parameterTypes.add(parameterType5);
		parametersMineaProcessTreewithETMd.add(parameter5);
		
		

		return parameterTypes;
	}

	private ETMParam getConfiguration(List<Parameter> parametersMineaProcessTreewithETMd) {
		ETMParam eTMParam = null;//new ETMParam();
		try {
		Parameter parameter2 = parametersMineaProcessTreewithETMd.get(1);
		int par2int = getParameterAsInt(parameter2.getNameParameter());
		eTMParam.setPopulationSize(par2int);

		Parameter parameter3 = parametersMineaProcessTreewithETMd.get(2);
		int par3int = getParameterAsInt(parameter3.getNameParameter());
		eTMParam.setEliteCount(par3int);

		Parameter parameter1 = parametersMineaProcessTreewithETMd.get(0);
		String par1str = getParameterAsString(parameter1.getNameParameter());
		eTMParam.setPath(par1str);

		Parameter parameter4 = parametersMineaProcessTreewithETMd.get(3);
		int par4int = getParameterAsInt(parameter4.getNameParameter());
		eTMParam.setMaxThreads(par4int);

		Parameter parameter5 = parametersMineaProcessTreewithETMd.get(4);
		int par5int = getParameterAsInt(parameter5.getNameParameter());
		eTMParam.setLogModulo(par5int);

		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
	return eTMParam;
	}

}
