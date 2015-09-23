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

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersEKS;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMin;

import com.rapidminer.ioobjects.ClassifierIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.PetriNetIOObject;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.rapidprom.prom.CallProm;

public class MinePetrinetwithInductiveMinerTask extends Operator {
	
	private static final String IM = "Inductive Miner";
	private static final String IMi = "Inductive Miner - Infrequent";
	private static final String IMin = "Inductive Miner - Incompleteness";
	private static final String IMeks = "Inductive Miner - exhaustive K-successor";
	
	private static final String EN = "Event Name";
	private static final String ST = "Standard Classifier (Event name + Lifecycle transition)";

	private List<Parameter> parametersMinePetrinetwithInductiveMiner = null;

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts().createPort("model (ProM Petri Net)");
	private OutputPort outputClassifier = getOutputPorts().createPort("classifier (ProM XEventClassifier)");

	public MinePetrinetwithInductiveMinerTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputClassifier, ClassifierIOObject.class));
	}

	public void doWork() throws OperatorException {
		
		LogService logService = LogService.getGlobal();
		logService.log("start do work Mine Petri net with Inductive Miner", LogService.NOTE);
		
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		
		List<Object> pars = new ArrayList<Object>();
		
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());
		
		MiningParameters param = (MiningParameters) getConfiguration(parametersMinePetrinetwithInductiveMiner);
		//param.setClassifier(XLogInfoImpl.STANDARD_CLASSIFIER);
		pars.add(param);
		
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine Petri net with Inductive Miner, with parameters", pars);
		
		PetriNetIOObject petrinetIOObject = new PetriNetIOObject((Petrinet) runPlugin[0]);
		petrinetIOObject.setPluginContext(pluginContext);
		outputPetrinet.deliver(petrinetIOObject);
		
		ClassifierIOObject  classifier = new ClassifierIOObject(param.getClassifier());
		classifier.setPluginContext(pluginContext);
		outputClassifier.deliver(classifier);
		
		logService.log(classifier.getName() + ": " + classifier.getClassifier().toString(), LogService.NOTE);
		logService.log("end do work Mine Petri net with Inductive Miner", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		Utilities.loadRequiredClasses();
		
		this.parametersMinePetrinetwithInductiveMiner = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		Object[] par0categories = new Object[] {IM, IMi, IMin, IMeks};
		ParameterCategory parameter0 = new ParameterCategory(par0categories, IM, String.class, "Inductive Miner Variant", "Inductive Miner Variant");
		ParameterTypeCategory parameterType0 = new ParameterTypeCategory(parameter0.getNameParameter(), parameter0.getDescriptionParameter(), parameter0.getOptionsParameter(), parameter0.getIndexValue(parameter0.getDefaultValueParameter()));
		parameterTypes.add(parameterType0);
		parametersMinePetrinetwithInductiveMiner.add(parameter0);

		ParameterDouble parameter1 = new ParameterDouble(0.2, 0, 1, 0.01, Double.class, "Noise Threshold", "Noise Threshold");
		ParameterTypeDouble parameterType1 = new ParameterTypeDouble(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), (Double) parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parametersMinePetrinetwithInductiveMiner.add(parameter1);
		
		Object[] par2categories = new Object[] {EN,ST};
		ParameterCategory parameter2 = new ParameterCategory(par2categories, ST, String.class, "Event Classifier", "Event Classifier");
		ParameterTypeCategory parameterType2 = new ParameterTypeCategory(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getOptionsParameter(), parameter2.getIndexValue(parameter2.getDefaultValueParameter()));
		parameterTypes.add(parameterType2);
		parametersMinePetrinetwithInductiveMiner.add(parameter2);

		return parameterTypes;
	}

	private Object getConfiguration(List<Parameter> parametersMinePetrinetwithInductiveMiner) {
		Object miningParameters = null;
		try 
		{
			Parameter parameter0 = parametersMinePetrinetwithInductiveMiner.get(0);
			int par0int = getParameterAsInt(parameter0.getNameParameter());
			String valPar0 = (String) parameter0.getValueParameter(par0int);
			
			Parameter parameter1 = parametersMinePetrinetwithInductiveMiner.get(1);
			float noiseThreshold = (float) getParameterAsDouble(parameter1.getNameParameter());
			
			Parameter parameter2 = parametersMinePetrinetwithInductiveMiner.get(2);
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
