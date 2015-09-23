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

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.processmining.plugins.etm.parameters.ETMParam;
import org.processmining.plugins.etm.parameters.ETMParamFactory;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.ProcessTreeIOObject;

import org.processmining.processtree.ProcessTree;
import org.rapidprom.prom.CallProm;

public class MineaProcessTreeusingtheETMalgorithmTask extends Operator {

	private List<Parameter> parametersMineaProcessTreeusingtheETMalgorithm = null;

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputProcessTree = getOutputPorts().createPort("model (ProM ProcessTree)");

	public MineaProcessTreeusingtheETMalgorithmTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputProcessTree, ProcessTreeIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Mine a Process Tree using the ETM algorithm", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

		ETMParam eTMParam = getConfiguration(this.parametersMineaProcessTreeusingtheETMalgorithm, pluginContext, XLogdata.getData());
		pars.add(eTMParam);
		CallProm cp = new CallProm();
		//Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine a Process Tree with ETMd", pars);
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine a Process Tree using the ETM algorithm", pars);
		ProcessTreeIOObject processTreeIOObject = new ProcessTreeIOObject((ProcessTree) runPlugin[0]);
		processTreeIOObject.setPluginContext(pluginContext);
	
		outputProcessTree.deliver(processTreeIOObject);
		logService.log("end do work Mine a Process Tree using the ETM algorithm", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		loadRequiredClasses();
		this.parametersMineaProcessTreeusingtheETMalgorithm = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		ParameterInteger parameter1 = new ParameterInteger(100, 0, Integer.MAX_VALUE, 1, Integer.class, "getPopulationSize", "getPopulationSize");
		ParameterTypeInt parameterType1 = new ParameterTypeInt(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter1);

		ParameterInteger parameter2 = new ParameterInteger(25, 0, Integer.MAX_VALUE, 1, Integer.class, "getEliteCount", "getEliteCount");
		ParameterTypeInt parameterType2 = new ParameterTypeInt(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getMin(), parameter2.getMax(), parameter2.getDefaultValueParameter());
		parameterTypes.add(parameterType2);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter2);

		ParameterInteger parameter3 = new ParameterInteger(0, 0, Integer.MAX_VALUE, 1, Integer.class, "getNrRandomTrees", "getNrRandomTrees");
		ParameterTypeInt parameterType3 = new ParameterTypeInt(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getMin(), parameter3.getMax(), parameter3.getDefaultValueParameter());
		parameterTypes.add(parameterType3);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter3);
		
		ParameterDouble parameter4 = new ParameterDouble(0.01, 0, Double.MAX_VALUE, 0.01, Double.class, "getCrossOverChance", "getCrossOverChance");
		ParameterTypeDouble parameterType4 = new ParameterTypeDouble(parameter4.getNameParameter(), parameter4.getDescriptionParameter(), parameter4.getMin(), parameter4.getMax(), (Double) parameter4.getDefaultValueParameter());
		parameterTypes.add(parameterType4);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter4);
		
		ParameterDouble parameter5 = new ParameterDouble(0.25, 0, Double.MAX_VALUE, 0.01, Double.class, "getChanceOfRandomMutation", "getChanceOfRandomMutation");
		ParameterTypeDouble parameterType5 = new ParameterTypeDouble(parameter5.getNameParameter(), parameter5.getDescriptionParameter(), parameter5.getMin(), parameter5.getMax(), (Double) parameter5.getDefaultValueParameter());
		parameterTypes.add(parameterType5);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter5);
		
		ParameterBoolean parameter5b = new ParameterBoolean(false, Boolean.class, "preventDuplicates", "preventDuplicates");
		ParameterTypeBoolean parameterType5b = new ParameterTypeBoolean(parameter5b.getNameParameter(), parameter5b.getDescriptionParameter(), parameter5b.getDefaultValueParameter());
		parameterTypes.add(parameterType5b);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter5b);
		
		ParameterInteger parameter6 = new ParameterInteger(100, 0, Integer.MAX_VALUE, 1, Integer.class, "getMaxGen", "getMaxGen");
		ParameterTypeInt parameterType6 = new ParameterTypeInt(parameter6.getNameParameter(), parameter6.getDescriptionParameter(), parameter6.getMin(), parameter6.getMax(), parameter6.getDefaultValueParameter());
		parameterTypes.add(parameterType6);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter6);
		
		ParameterDouble parameter7 = new ParameterDouble(1.0, 0, Double.MAX_VALUE, 1, Double.class, "getTargetFitness", "getTargetFitness");
		ParameterTypeDouble parameterType7 = new ParameterTypeDouble(parameter7.getNameParameter(), parameter7.getDescriptionParameter(), parameter7.getMin(), parameter7.getMax(), (Double) parameter7.getDefaultValueParameter());
		parameterTypes.add(parameterType7);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter7);
		
		ParameterDouble parameter8 = new ParameterDouble(10.0, 0, Double.MAX_VALUE, 1, Double.class, "getFrWeight", "getFrWeight");
		ParameterTypeDouble parameterType8 = new ParameterTypeDouble(parameter8.getNameParameter(), parameter8.getDescriptionParameter(), parameter8.getMin(), parameter8.getMax(), (Double) parameter8.getDefaultValueParameter());
		parameterTypes.add(parameterType8);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter8);
		
		ParameterDouble parameter9 = new ParameterDouble(0.75, -1, Double.MAX_VALUE, 1, Double.class, "getFitnessLimit", "getFitnessLimit");
		ParameterTypeDouble parameterType9 = new ParameterTypeDouble(parameter9.getNameParameter(), parameter9.getDescriptionParameter(), parameter9.getMin(), parameter9.getMax(), (Double) parameter9.getDefaultValueParameter());
		parameterTypes.add(parameterType9);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter9);
		
		ParameterDouble parameter10 = new ParameterDouble(10.0, -1, Double.MAX_VALUE, 1, Double.class, "getMaxFTime", "getMaxFTime");
		ParameterTypeDouble parameterType10 = new ParameterTypeDouble(parameter10.getNameParameter(), parameter10.getDescriptionParameter(), parameter10.getMin(), parameter10.getMax(), (Double) parameter10.getDefaultValueParameter());
		parameterTypes.add(parameterType10);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter10);
		
		ParameterDouble parameter11 = new ParameterDouble(1.0, 0, Double.MAX_VALUE, 1, Double.class, "getPeWeight", "getPeWeight");
		ParameterTypeDouble parameterType11 = new ParameterTypeDouble(parameter11.getNameParameter(), parameter11.getDescriptionParameter(), parameter11.getMin(), parameter11.getMax(), (Double) parameter11.getDefaultValueParameter());
		parameterTypes.add(parameterType11);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter11);
		
		ParameterDouble parameter12 = new ParameterDouble(1.0, 0, Double.MAX_VALUE, 1, Double.class, "getGeWeight", "getGeWeight");
		ParameterTypeDouble parameterType12 = new ParameterTypeDouble(parameter12.getNameParameter(), parameter12.getDescriptionParameter(), parameter12.getMin(), parameter11.getMax(), (Double) parameter11.getDefaultValueParameter());
		parameterTypes.add(parameterType12);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter12);
		
		ParameterDouble parameter13 = new ParameterDouble(1.0, 0, Double.MAX_VALUE, 1, Double.class, "getSdWeight", "getSdWeight");
		ParameterTypeDouble parameterType13 = new ParameterTypeDouble(parameter13.getNameParameter(), parameter13.getDescriptionParameter(), parameter13.getMin(), parameter13.getMax(), (Double) parameter13.getDefaultValueParameter());
		parameterTypes.add(parameterType13);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter13);
		
		ParameterInteger parameter14 = new ParameterInteger(10, 0, Integer.MAX_VALUE, 1, Integer.class, "getLogModulo", "getLogModulo");
		ParameterTypeInt parameterType14 = new ParameterTypeInt(parameter14.getNameParameter(), parameter14.getDescriptionParameter(), parameter14.getMin(), parameter14.getMax(), parameter14.getDefaultValueParameter());
		parameterTypes.add(parameterType14);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter14);
		
		ParameterInteger parameter15 = new ParameterInteger(6, 0, Integer.MAX_VALUE, 1, Integer.class, "getMaxThreads", "getMaxThreads");
		ParameterTypeInt parameterType15 = new ParameterTypeInt(parameter15.getNameParameter(), parameter15.getDescriptionParameter(), parameter15.getMin(), parameter3.getMax(), parameter15.getDefaultValueParameter());
		parameterTypes.add(parameterType15);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter15);

		return parameterTypes;
	}

	private ETMParam getConfiguration(List<Parameter> parametersMineaProcessTreeusingtheETMalgorithm, PluginContext context, XLog eventlog) {
//		ETMParam eTMParam = ETMParameterWizard.getExistingParamObject(context, ETMParam.class);
//		if (eTMParam == null) {
//			CentralRegistry registry = new CentralRegistry(context, eventlog, new Random());
//			eTMParam = new ETMParam(registry, null, null, -1, -1);
//		}
		
		// some standard settings
//		//Add some of the standard things that we don't need a GUI for
//		ETMParamFactory.addOrReplaceTerminationCondition(eTMParam,
//				ETMParamFactory.constructProMCancelTerminationCondition(context));
//		//We should always have this such that an external tool can look for it and stop the ETM
//		eTMParam.addTerminationCondition(new ExternalTerminationCondition());
//		eTMParam.setFactory(new TreeFactory(eTMParam.getCentralRegistry()));
//
//		//Add standard overallFitness evaluator
//		eTMParam.setFitnessEvaluator(ETMParamFactory.createStandardOverallFitness(eTMParam.getCentralRegistry()));
//
//		//Add a logger to output to the context
//		List<EvolutionObserver<NAryTree>> evolutionObservers = new ArrayList<EvolutionObserver<NAryTree>>();
//		evolutionObservers.add(new EvolutionLogger<NAryTree>(context, eTMParam.getCentralRegistry(), false));
//		eTMParam.setEvolutionObservers(evolutionObservers);
//		// end some standard settings
		ETMParam eTMParam = null;
		try {
		Parameter parameter1 = parametersMineaProcessTreeusingtheETMalgorithm.get(0);
		int popSize = getParameterAsInt(parameter1.getNameParameter());	
			
		Parameter parameter2 = parametersMineaProcessTreeusingtheETMalgorithm.get(1);
		int eliteSize = getParameterAsInt(parameter2.getNameParameter());

		Parameter parameter3 = parametersMineaProcessTreeusingtheETMalgorithm.get(2);
		int nrRandomTrees = getParameterAsInt(parameter3.getNameParameter());

		Parameter parameter4 = parametersMineaProcessTreeusingtheETMalgorithm.get(3);
		double crossOverChance = getParameterAsDouble(parameter4.getNameParameter());

		Parameter parameter5 = parametersMineaProcessTreeusingtheETMalgorithm.get(4);
		double chanceOfRandomMutation = getParameterAsDouble(parameter5.getNameParameter());
		
		Parameter parameter5b = parametersMineaProcessTreeusingtheETMalgorithm.get(5);
		boolean preventDuplicates = getParameterAsBoolean(parameter5b.getNameParameter());
		
		Parameter parameter6 = parametersMineaProcessTreeusingtheETMalgorithm.get(6);
		int maxGen = getParameterAsInt(parameter6.getNameParameter());
		
		Parameter parameter7 = parametersMineaProcessTreeusingtheETMalgorithm.get(7);
		double targetFitness = getParameterAsDouble(parameter7.getNameParameter());
		
		Parameter parameter8 = parametersMineaProcessTreeusingtheETMalgorithm.get(8);
		double frWeight = getParameterAsDouble(parameter8.getNameParameter());
		
		Parameter parameter9 = parametersMineaProcessTreeusingtheETMalgorithm.get(9);
		double maxF = getParameterAsDouble(parameter9.getNameParameter());
		
		Parameter parameter10 = parametersMineaProcessTreeusingtheETMalgorithm.get(10);
		double maxFTime = getParameterAsDouble(parameter10.getNameParameter());
		
		Parameter parameter11 = parametersMineaProcessTreeusingtheETMalgorithm.get(11);
		double peWeight = getParameterAsDouble(parameter11.getNameParameter());
		
		Parameter parameter12 = parametersMineaProcessTreeusingtheETMalgorithm.get(12);
		double geWeight = getParameterAsDouble(parameter12.getNameParameter());
		
		Parameter parameter13 = parametersMineaProcessTreeusingtheETMalgorithm.get(13);
		double sdWeight = getParameterAsDouble(parameter13.getNameParameter());
		
		Parameter parameter14 = parametersMineaProcessTreeusingtheETMalgorithm.get(14);
		int logModulo = getParameterAsInt(parameter14.getNameParameter());
		
		Parameter parameter15 = parametersMineaProcessTreeusingtheETMalgorithm.get(15);
		int maxThreads = getParameterAsInt(parameter15.getNameParameter());
		
		eTMParam = ETMParamFactory.buildParam(eventlog, context, popSize, eliteSize, nrRandomTrees, crossOverChance, chanceOfRandomMutation, preventDuplicates, maxGen, targetFitness, frWeight, maxF, maxFTime, peWeight, geWeight, sdWeight);
		eTMParam.setLogModulo(logModulo);
		eTMParam.setMaxThreads(maxThreads);
		} catch (Exception e) {
			e.printStackTrace();
		}
	return eTMParam;
	}
	
	private void loadRequiredClasses () {
		Utilities.loadRequiredClasses();
	}
}








/*package com.rapidminer.operator.miningplugins;

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

import org.deckfour.xes.model.XLog;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.processmining.plugins.callprom.CallProm;
import org.processmining.plugins.etm.ETM;
import org.processmining.plugins.etm.model.narytree.test.LogCreator;
import org.processmining.plugins.etm.parameters.ETMParam;
import org.processmining.plugins.etm.parameters.ETMParamFactory;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.ProcessTreeIOObject;

import org.processmining.processtree.ProcessTree;
//import org.uncommons.watchmaker.framework.EvolutionObserver;

public class MineaProcessTreeusingtheETMalgorithmTask extends Operator {

	private List<Parameter> parametersMineaProcessTreeusingtheETMalgorithm = null;

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputProcessTree = getOutputPorts().createPort("model (ProM ProcessTree)");

	public MineaProcessTreeusingtheETMalgorithmTask(OperatorDescription description) {
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

	
		ETMParam eTMParam = getConfiguration(this.parametersMineaProcessTreeusingtheETMalgorithm, pluginContext, XLogdata.getData());
		pars.add(eTMParam);
		
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine a Process Tree using the ETM algorithm", pars);
		
		ProcessTreeIOObject processTreeIOObject = new ProcessTreeIOObject((ProcessTree) runPlugin[0]);
		processTreeIOObject.setPluginContext(pluginContext);
		
		outputProcessTree.deliver(processTreeIOObject);
		logService.log("end do work Mine a Process Tree using the ETM algorithm", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		loadRequiredClasses();
		this.parametersMineaProcessTreeusingtheETMalgorithm = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		ParameterInteger parameter1 = new ParameterInteger(20, 0, Integer.MAX_VALUE, 1, Integer.class, "Population Size", "Population Size");
		ParameterTypeInt parameterType1 = new ParameterTypeInt(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter1);

		ParameterInteger parameter2 = new ParameterInteger(5, 0, Integer.MAX_VALUE, 1, Integer.class, "Elite Count", "Elite Count");
		ParameterTypeInt parameterType2 = new ParameterTypeInt(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getMin(), parameter2.getMax(), parameter2.getDefaultValueParameter());
		parameterTypes.add(parameterType2);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter2);

		ParameterInteger parameter3 = new ParameterInteger(0, 0, Integer.MAX_VALUE, 1, Integer.class, "Random Trees", "Number of Worst trees to be replaced with random trees");
		ParameterTypeInt parameterType3 = new ParameterTypeInt(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getMin(), parameter3.getMax(), parameter3.getDefaultValueParameter());
		parameterTypes.add(parameterType3);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter3);
		
		ParameterDouble parameter4 = new ParameterDouble(0.01, 0, Double.MAX_VALUE, 0.01, Double.class, "Crossover Chance", "the chance that corssover will be applied to two trees");
		ParameterTypeDouble parameterType4 = new ParameterTypeDouble(parameter4.getNameParameter(), parameter4.getDescriptionParameter(), parameter4.getMin(), parameter4.getMax(), (Double) parameter4.getDefaultValueParameter());
		parameterTypes.add(parameterType4);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter4);
		
		ParameterDouble parameter5 = new ParameterDouble(0.25, 0, Double.MAX_VALUE, 0.01, Double.class, "Chance of Random Mutation", "Chance of random mutation for a tree");
		ParameterTypeDouble parameterType5 = new ParameterTypeDouble(parameter5.getNameParameter(), parameter5.getDescriptionParameter(), parameter5.getMin(), parameter5.getMax(), (Double) parameter5.getDefaultValueParameter());
		parameterTypes.add(parameterType5);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter5);
		
		ParameterBoolean parameter5b = new ParameterBoolean(false, Boolean.class, "Prevent Duplicates", "Prevent Duplicates");
		ParameterTypeBoolean parameterType5b = new ParameterTypeBoolean(parameter5b.getNameParameter(), parameter5b.getDescriptionParameter(), parameter5b.getDefaultValueParameter());
		parameterTypes.add(parameterType5b);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter5b);
		
		ParameterInteger parameter6 = new ParameterInteger(100, 0, Integer.MAX_VALUE, 1, Integer.class, "Maximum Generations", "Number of generations to run");
		ParameterTypeInt parameterType6 = new ParameterTypeInt(parameter6.getNameParameter(), parameter6.getDescriptionParameter(), parameter6.getMin(), parameter6.getMax(), parameter6.getDefaultValueParameter());
		parameterTypes.add(parameterType6);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter6);
		
		ParameterDouble parameter7 = new ParameterDouble(1.0, 0, 1, 0.01, Double.class, "Fitness Target", "Fitness overall value that stops the iterations");
		ParameterTypeDouble parameterType7 = new ParameterTypeDouble(parameter7.getNameParameter(), parameter7.getDescriptionParameter(), parameter7.getMin(), parameter7.getMax(), (Double) parameter7.getDefaultValueParameter());
		parameterTypes.add(parameterType7);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter7);
		
		ParameterDouble parameter8 = new ParameterDouble(10.0, 0, Double.MAX_VALUE, 1, Double.class, "Weight of Fitness Replay", "getFrWeight");
		ParameterTypeDouble parameterType8 = new ParameterTypeDouble(parameter8.getNameParameter(), parameter8.getDescriptionParameter(), parameter8.getMin(), parameter8.getMax(), (Double) parameter8.getDefaultValueParameter());
		parameterTypes.add(parameterType8);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter8);
		
		ParameterDouble parameter9 = new ParameterDouble(-1.0, 0, 1, 0.01, Double.class, "Max Fitness", "Maximum Fitness allowed");
		ParameterTypeDouble parameterType9 = new ParameterTypeDouble(parameter9.getNameParameter(), parameter9.getDescriptionParameter(), parameter9.getMin(), parameter9.getMax(), (Double) parameter9.getDefaultValueParameter());
		parameterTypes.add(parameterType9);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter9);
		
		ParameterDouble parameter10 = new ParameterDouble(-1.0, 0, Double.MAX_VALUE, 1, Double.class, "Max Alignment Time", "Maximum time ");
		ParameterTypeDouble parameterType10 = new ParameterTypeDouble(parameter10.getNameParameter(), parameter10.getDescriptionParameter(), parameter10.getMin(), parameter10.getMax(), (Double) parameter10.getDefaultValueParameter());
		parameterTypes.add(parameterType10);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter10);
		
		ParameterDouble parameter11 = new ParameterDouble(5.0, 0, Double.MAX_VALUE, 1, Double.class, "Weight of Precision", "getPeWeight");
		ParameterTypeDouble parameterType11 = new ParameterTypeDouble(parameter11.getNameParameter(), parameter11.getDescriptionParameter(), parameter11.getMin(), parameter11.getMax(), (Double) parameter11.getDefaultValueParameter());
		parameterTypes.add(parameterType11);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter11);
		
		ParameterDouble parameter12 = new ParameterDouble(1.0, 0, Double.MAX_VALUE, 1, Double.class, "Weight of Generalization", "getGeWeight");
		ParameterTypeDouble parameterType12 = new ParameterTypeDouble(parameter12.getNameParameter(), parameter12.getDescriptionParameter(), parameter12.getMin(), parameter11.getMax(), (Double) parameter11.getDefaultValueParameter());
		parameterTypes.add(parameterType12);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter12);
		
		ParameterDouble parameter13 = new ParameterDouble(1.0, 0, Double.MAX_VALUE, 1, Double.class, "Weight of Simplicity", "getSdWeight");
		ParameterTypeDouble parameterType13 = new ParameterTypeDouble(parameter13.getNameParameter(), parameter13.getDescriptionParameter(), parameter13.getMin(), parameter13.getMax(), (Double) parameter13.getDefaultValueParameter());
		parameterTypes.add(parameterType13);
		parametersMineaProcessTreeusingtheETMalgorithm.add(parameter13);

		return parameterTypes;
	}

	private ETMParam getConfiguration(List<Parameter> parametersMineaProcessTreeusingtheETMalgorithm, PluginContext context, XLog eventlog) {

		ETMParam eTMParam = null;
		try {
		Parameter parameter1 = parametersMineaProcessTreeusingtheETMalgorithm.get(0);
		int popSize = getParameterAsInt(parameter1.getNameParameter());	
			
		Parameter parameter2 = parametersMineaProcessTreeusingtheETMalgorithm.get(1);
		int eliteSize = getParameterAsInt(parameter2.getNameParameter());

		Parameter parameter3 = parametersMineaProcessTreeusingtheETMalgorithm.get(2);
		int nrRandomTrees = getParameterAsInt(parameter3.getNameParameter());

		Parameter parameter4 = parametersMineaProcessTreeusingtheETMalgorithm.get(3);
		double crossOverChance = getParameterAsDouble(parameter4.getNameParameter());

		Parameter parameter5 = parametersMineaProcessTreeusingtheETMalgorithm.get(4);
		double chanceOfRandomMutation = getParameterAsDouble(parameter5.getNameParameter());
		
		Parameter parameter5b = parametersMineaProcessTreeusingtheETMalgorithm.get(5);
		boolean preventDuplicates = getParameterAsBoolean(parameter5b.getNameParameter());
		
		Parameter parameter6 = parametersMineaProcessTreeusingtheETMalgorithm.get(6);
		int maxGen = getParameterAsInt(parameter6.getNameParameter());
		
		Parameter parameter7 = parametersMineaProcessTreeusingtheETMalgorithm.get(7);
		double targetFitness = getParameterAsDouble(parameter7.getNameParameter());
		
		Parameter parameter8 = parametersMineaProcessTreeusingtheETMalgorithm.get(8);
		double frWeight = getParameterAsDouble(parameter8.getNameParameter());
		
		Parameter parameter9 = parametersMineaProcessTreeusingtheETMalgorithm.get(9);
		double maxF = getParameterAsDouble(parameter9.getNameParameter());
		
		Parameter parameter10 = parametersMineaProcessTreeusingtheETMalgorithm.get(10);
		double maxFTime = getParameterAsDouble(parameter10.getNameParameter());
		
		Parameter parameter11 = parametersMineaProcessTreeusingtheETMalgorithm.get(11);
		double peWeight = getParameterAsDouble(parameter11.getNameParameter());
		
		Parameter parameter12 = parametersMineaProcessTreeusingtheETMalgorithm.get(12);
		double geWeight = getParameterAsDouble(parameter12.getNameParameter());
		
		Parameter parameter13 = parametersMineaProcessTreeusingtheETMalgorithm.get(13);
		double sdWeight = getParameterAsDouble(parameter13.getNameParameter());		
		
		
		eTMParam = ETMParamFactory.buildParam(eventlog, context, popSize, eliteSize, nrRandomTrees, crossOverChance, chanceOfRandomMutation, preventDuplicates, maxGen, targetFitness, frWeight, maxF, maxFTime, peWeight, geWeight, sdWeight);
		//eTMParam = ETMParamFactory.buildParam(LogCreator.createLog(new String[][] { { "a", "A", "B" } }), null, 20,2, 1, 0.1, 0.25, true, 15, 1, 10, 1, 1000, 5, 1, 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	return eTMParam;
	}
	
	private void loadRequiredClasses () {
		Utilities.loadRequiredClasses();
	}
}
	*/


