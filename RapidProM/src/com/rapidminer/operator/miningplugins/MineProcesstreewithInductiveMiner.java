package com.rapidminer.operator.miningplugins;

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

import com.rapidminer.ioobjects.ClassifierIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersEKS;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMin;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjectrenderers.XLogIOObjectRenderer;

import org.deckfour.xes.model.XLog;

import com.rapidminer.ioobjects.ProcessTreeIOObject;
import com.rapidminer.ioobjectrenderers.ProcessTreeIOObjectRenderer;

import org.processmining.processtree.ProcessTree;
import org.rapidprom.prom.CallProm;

public class MineProcesstreewithInductiveMiner extends Operator {

	private static final String IM = "Inductive Miner";
	private static final String IMi = "Inductive Miner - Infrequent";
	private static final String IMin = "Inductive Miner - Incompleteness";
	private static final String IMeks = "Inductive Miner - exhaustive K-successor";
	
	private static final String Processtree = "Process Tree";
	private static final String DOT = "DOT visualizer";
	
	private static final String EN = "Event Name";
	private static final String ST = "Standard Classifier (Event name + Lifecycle transition)";
	
	private List<Parameter> parametersMineProcesstreewithInductiveMiner = null;

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputProcessTree = getOutputPorts().createPort("model (ProM ProcessTree)");
	private OutputPort outputClassifier = getOutputPorts().createPort("classifier (ProM XEventClassifier)");

	public MineProcesstreewithInductiveMiner(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputProcessTree, ProcessTreeIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputClassifier, ClassifierIOObject.class));
}

	public void doWork() throws OperatorException 
	{
		LogService logService = LogService.getGlobal();
		logService.log("start do work Mine Process tree with Inductive Miner, with parameters", LogService.NOTE);
		
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		
		List<Object> pars = new ArrayList<Object>();
		
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

		MiningParameters param = (MiningParameters) getConfiguration(parametersMineProcesstreewithInductiveMiner);
		pars.add(param);
		
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine Process tree with Inductive Miner, with parameters", pars);
		
		ProcessTreeIOObject processTreeIOObject = new ProcessTreeIOObject((ProcessTree) runPlugin[0]);
		processTreeIOObject.setPluginContext(pluginContext);
		
		Parameter parameter0 = parametersMineProcesstreewithInductiveMiner.get(2);
		int par0int = getParameterAsInt(parameter0.getNameParameter());
		String valPar0 = (String) parameter0.getValueParameter(par0int);
		
		if (valPar0.equals(Processtree)) 
		{
			processTreeIOObject.setVisualizationType(ProcessTreeIOObject.VisualizationType.Processtree);
		}
		else
		{
			processTreeIOObject.setVisualizationType(ProcessTreeIOObject.VisualizationType.DOT);
		}
		
		outputProcessTree.deliver(processTreeIOObject);
		
		ClassifierIOObject  classifier = new ClassifierIOObject(param.getClassifier());
		classifier.setPluginContext(pluginContext);
		outputClassifier.deliver(classifier);
		
		logService.log("end do work Mine Process tree with Inductive Miner, with parameters", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		Utilities.loadRequiredClasses();
		
		this.parametersMineProcesstreewithInductiveMiner = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		Object[] par0categories = new Object[] {IM, IMi, IMin, IMeks};
		ParameterCategory parameter0 = new ParameterCategory(par0categories, IM, String.class, "Inductive Miner Variant", "Inductive Miner Variant");
		ParameterTypeCategory parameterType0 = new ParameterTypeCategory(parameter0.getNameParameter(), parameter0.getDescriptionParameter(), parameter0.getOptionsParameter(), parameter0.getIndexValue(parameter0.getDefaultValueParameter()));
		parameterTypes.add(parameterType0);
		parametersMineProcesstreewithInductiveMiner.add(parameter0);

		ParameterDouble parameter1 = new ParameterDouble(0.2, 0, 1, 0.01, Double.class, "Noise Threshold", "Noise Threshold");
		ParameterTypeDouble parameterType1 = new ParameterTypeDouble(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), (Double) parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parametersMineProcesstreewithInductiveMiner.add(parameter1);
		
		Object[] par2categories = new Object[] {Processtree, DOT};
		ParameterCategory parameter2 = new ParameterCategory(par2categories, Processtree, String.class, "Visualization", "Visualization");
		ParameterTypeCategory parameterType2 = new ParameterTypeCategory(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getOptionsParameter(), parameter2.getIndexValue(parameter2.getDefaultValueParameter()));
		parameterTypes.add(parameterType2);
		parametersMineProcesstreewithInductiveMiner.add(parameter2);
		
		Object[] par3categories = new Object[] {EN,ST};
		ParameterCategory parameter3 = new ParameterCategory(par3categories, ST, String.class, "Event Classifier", "Event Classifier");
		ParameterTypeCategory parameterType3 = new ParameterTypeCategory(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getOptionsParameter(), parameter3.getIndexValue(parameter3.getDefaultValueParameter()));
		parameterTypes.add(parameterType3);
		parametersMineProcesstreewithInductiveMiner.add(parameter3);

		return parameterTypes;
	}

	private Object getConfiguration(List<Parameter> parametersMineProcesstreewithInductiveMiner) {
		Object miningParameters = null;
		try 
		{
			Parameter parameter0 = parametersMineProcesstreewithInductiveMiner.get(0);
			int par0int = getParameterAsInt(parameter0.getNameParameter());
			String valPar0 = (String) parameter0.getValueParameter(par0int);
			
			Parameter parameter1 = parametersMineProcesstreewithInductiveMiner.get(1);
			float noiseThreshold = (float) getParameterAsDouble(parameter1.getNameParameter());
			
			Parameter parameter2 = parametersMineProcesstreewithInductiveMiner.get(3);
			int par2int = getParameterAsInt(parameter2.getNameParameter());
			String valPar2 = (String) parameter2.getValueParameter(par2int);
			
			if (valPar0.equals(IM)) 
			{
				miningParameters = new MiningParametersIM();
				((MiningParametersIM)miningParameters).setNoiseThreshold(noiseThreshold);
				if(valPar2.equals(ST))
					((MiningParametersIM)miningParameters).setClassifier(XLogInfoImpl.STANDARD_CLASSIFIER);
			}
			
			else if (valPar0.equals(IMi)) 
			{
				miningParameters = new MiningParametersIMi();
				((MiningParametersIMi)miningParameters).setNoiseThreshold(noiseThreshold);
				if(valPar2.equals(ST))
					((MiningParametersIMi)miningParameters).setClassifier(XLogInfoImpl.STANDARD_CLASSIFIER);
			}
			
			else if(valPar0.equals(IMin))
			{
				miningParameters = new MiningParametersIMin();
				((MiningParametersIMin)miningParameters).setNoiseThreshold(noiseThreshold);
				if(valPar2.equals(ST))
					((MiningParametersIMin)miningParameters).setClassifier(XLogInfoImpl.STANDARD_CLASSIFIER);
			}
			
			else
			{
				miningParameters = new MiningParametersEKS();
				((MiningParametersEKS)miningParameters).setNoiseThreshold(noiseThreshold);
				if(valPar2.equals(ST))
					((MiningParametersEKS)miningParameters).setClassifier(XLogInfoImpl.STANDARD_CLASSIFIER);
			}
			
		} 
		catch (UndefinedParameterError e) 
		{
			e.printStackTrace();
		}
		return miningParameters;
	}

}
