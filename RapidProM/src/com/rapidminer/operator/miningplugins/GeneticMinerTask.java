//package com.rapidminer.operator.miningplugins;
//
//import java.util.*;
//
//import com.rapidminer.callprom.CallProm;
//import com.rapidminer.operator.Operator;
//import com.rapidminer.operator.OperatorDescription;
//import com.rapidminer.operator.OperatorException;
//import com.rapidminer.operator.ports.InputPort;
//import com.rapidminer.operator.ports.OutputPort;
//import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
//import com.rapidminer.tools.LogService;
//import com.rapidminer.tools.config.ConfigurationManager;
//import com.rapidminer.util.ProMIOObjectList;
//import com.rapidminer.util.Utilities;
//import com.rapidminer.parameter.*;
//import com.rapidminer.parameters.*;
//
//import org.processmining.framework.plugin.PluginContext;
//
//import com.rapidminer.ioobjects.HeuristicsNetIOObject;
//import com.rapidminer.ioobjects.ProMContextIOObject;
//
//import org.processmining.models.heuristics.HeuristicsNet;
//import org.processmining.plugins.heuristicsnet.miner.genetic.miner.settings.GeneticMinerSettings;
//
//import com.rapidminer.ioobjects.XLogIOObject;
//
//import org.processmining.plugins.heuristicsnet.array.visualization.HeuristicsNetArrayObject;
//
//import com.rapidminer.callprom.ClassLoaderUtils;
//import com.rapidminer.configuration.GlobalProMParameters;
//
//import java.io.File;
//
//public class GeneticMinerTask extends Operator {
//
//	private List<Parameter> parametersGeneticMiner = null;
//
//	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
//	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
//	private OutputPort outputHeuristicsNetArrayObject = getOutputPorts().createPort("model (ProM Heuristics Net)");
//
//	public GeneticMinerTask(OperatorDescription description) {
//		super(description);
//		getTransformer().addRule( new GenerateNewMDRule(outputHeuristicsNetArrayObject, HeuristicsNetIOObject.class));
//}
//
//	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Genetic Miner", LogService.NOTE);
//		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
//		PluginContext pluginContext = context.getPluginContext();
//		List<Object> pars = new ArrayList<Object>();
//		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
//		pars.add(XLogdata.getData());
//
//		GeneticMinerSettings geneticMinerSettings = getConfiguration(this.parametersGeneticMiner);
//		pars.add(geneticMinerSettings);
//		CallProm cp = new CallProm();
//		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Genetic Miner", pars);
//		HeuristicsNetArrayObject hna = ((HeuristicsNetArrayObject) runPlugin[0]);
//		// go through the array and find the net with the highest fitness.
//		double fitnessCurr = Double.MIN_VALUE;
//		int index  = -1;
//		for (int i=0; i<hna.getPopulation().length; i++) {
//			double fitness = hna.getPopulation()[i].getFitness();
//			if (fitness > fitnessCurr) {
//				// found a better one
//				index  = i;
//				fitnessCurr = fitness;
//			}
//		}
//		// take the best one
//		logService.log("selected net number " + index + " with fitness " + fitnessCurr);
//		HeuristicsNet heuristicsNet = hna.getPopulation()[index];
//		HeuristicsNetIOObject heuristicsNetIOObject = new HeuristicsNetIOObject(heuristicsNet);
//		heuristicsNetIOObject.setPluginContext(pluginContext);
//		// add to list so that afterwards it can be cleared if needed
//		ProMIOObjectList instance = ProMIOObjectList.getInstance();
//		instance.addToList(heuristicsNetIOObject);
//		outputHeuristicsNetArrayObject.deliver(heuristicsNetIOObject);
//		logService.log("end do work Genetic Miner", LogService.NOTE);
//	}
//
//	public List<ParameterType> getParameterTypes() {
//		loadRequiredClasses();
//		this.parametersGeneticMiner = new ArrayList<Parameter>();
//		List<ParameterType> parameterTypes = super.getParameterTypes();
//
//		ParameterInteger parameter1 = new ParameterInteger(10, 0, Integer.MAX_VALUE, 1, Integer.class, "getPopulationSize", "getPopulationSize");
//		ParameterTypeInt parameterType1 = new ParameterTypeInt(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), parameter1.getDefaultValueParameter());
//		parameterTypes.add(parameterType1);
//		parametersGeneticMiner.add(parameter1);
//
//		ParameterString parameter2 = new ParameterString("", String.class,"getFilename", "getFilename");
//		ParameterTypeString parameterType2 = new ParameterTypeString(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getDefaultValueParameter());
//		parameterTypes.add(parameterType2);
//		parametersGeneticMiner.add(parameter2);
//
//		ParameterLong parameter3 = new ParameterLong(1, 0, Integer.MAX_VALUE, 1, Long.class, "getSeed", "getSeed");
//		ParameterTypeLong parameterType3 = new ParameterTypeLong(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getMin(), parameter3.getMax(), parameter3.getDefaultValueParameter());
//		parameterTypes.add(parameterType3);
//		parametersGeneticMiner.add(parameter3);
//
//		ParameterDouble parameter4 = new ParameterDouble(1, 0, Double.MAX_VALUE, 1, Double.class, "getPower", "getPower");
//		ParameterTypeDouble parameterType4 = new ParameterTypeDouble(parameter4.getNameParameter(), parameter4.getDescriptionParameter(), parameter4.getMin(), parameter4.getMax(), (Double) parameter4.getDefaultValueParameter());
//		parameterTypes.add(parameterType4);
//		parametersGeneticMiner.add(parameter4);
//
//		ParameterDouble parameter5 = new ParameterDouble(0.2, 0, Double.MAX_VALUE, 1, Double.class, "getElitismRate", "getElitismRate");
//		ParameterTypeDouble parameterType5 = new ParameterTypeDouble(parameter5.getNameParameter(), parameter5.getDescriptionParameter(), parameter5.getMin(), parameter5.getMax(), (Double) parameter5.getDefaultValueParameter());
//		parameterTypes.add(parameterType5);
//		parametersGeneticMiner.add(parameter5);
//
//		ParameterDouble parameter6 = new ParameterDouble(0.2, 0, Double.MAX_VALUE, 1, Double.class, "getMutationRate", "getMutationRate");
//		ParameterTypeDouble parameterType6 = new ParameterTypeDouble(parameter6.getNameParameter(), parameter6.getDescriptionParameter(), parameter6.getMin(), parameter6.getMax(), (Double) parameter6.getDefaultValueParameter());
//		parameterTypes.add(parameterType6);
//		parametersGeneticMiner.add(parameter6);
//
//		ParameterInteger parameter7 = new ParameterInteger(0, 0, Integer.MAX_VALUE, 1, Integer.class, "getInitialPopulationType", "getInitialPopulationType");
//		ParameterTypeInt parameterType7 = new ParameterTypeInt(parameter7.getNameParameter(), parameter7.getDescriptionParameter(), parameter7.getMin(), parameter7.getMax(), parameter7.getDefaultValueParameter());
//		parameterTypes.add(parameterType7);
//		parametersGeneticMiner.add(parameter7);
//
//		ParameterInteger parameter8 = new ParameterInteger(0, 0, Integer.MAX_VALUE, 1, Integer.class, "getCrossoverType", "getCrossoverType");
//		ParameterTypeInt parameterType8 = new ParameterTypeInt(parameter8.getNameParameter(), parameter8.getDescriptionParameter(), parameter8.getMin(), parameter8.getMax(), parameter8.getDefaultValueParameter());
//		parameterTypes.add(parameterType8);
//		parametersGeneticMiner.add(parameter8);
//
//		ParameterInteger parameter9 = new ParameterInteger(0, 0, Integer.MAX_VALUE, 1, Integer.class, "getMutationType", "getMutationType");
//		ParameterTypeInt parameterType9 = new ParameterTypeInt(parameter9.getNameParameter(), parameter9.getDescriptionParameter(), parameter9.getMin(), parameter9.getMax(), parameter9.getDefaultValueParameter());
//		parameterTypes.add(parameterType9);
//		parametersGeneticMiner.add(parameter9);
//
//		ParameterDouble parameter10 = new ParameterDouble(0.8, 0, Double.MAX_VALUE, 1, Double.class, "getCrossoverRate", "getCrossoverRate");
//		ParameterTypeDouble parameterType10 = new ParameterTypeDouble(parameter10.getNameParameter(), parameter10.getDescriptionParameter(), parameter10.getMin(), parameter10.getMax(), (Double) parameter10.getDefaultValueParameter());
//		parameterTypes.add(parameterType10);
//		parametersGeneticMiner.add(parameter10);
//
//		ParameterInteger parameter11 = new ParameterInteger(4, 0, Integer.MAX_VALUE, 1, Integer.class, "getFitnessType", "getFitnessType");
//		ParameterTypeInt parameterType11 = new ParameterTypeInt(parameter11.getNameParameter(), parameter11.getDescriptionParameter(), parameter11.getMin(), parameter11.getMax(), parameter11.getDefaultValueParameter());
//		parameterTypes.add(parameterType11);
//		parametersGeneticMiner.add(parameter11);
//
//		ParameterDouble parameter12 = new ParameterDouble(0.8, 0, Double.MAX_VALUE, 1, Double.class, "getStopFitness", "getStopFitness");
//		ParameterTypeDouble parameterType12 = new ParameterTypeDouble(parameter12.getNameParameter(), parameter12.getDescriptionParameter(), parameter12.getMin(), parameter12.getMax(), (Double) parameter12.getDefaultValueParameter());
//		parameterTypes.add(parameterType12);
//		parametersGeneticMiner.add(parameter12);
//
//		ParameterInteger parameter13 = new ParameterInteger(1000, 0, Integer.MAX_VALUE, 1, Integer.class, "getMaxGeneration", "getMaxGeneration");
//		ParameterTypeInt parameterType13 = new ParameterTypeInt(parameter13.getNameParameter(), parameter13.getDescriptionParameter(), parameter13.getMin(), parameter13.getMax(), parameter13.getDefaultValueParameter());
//		parameterTypes.add(parameterType13);
//		parametersGeneticMiner.add(parameter13);
//
//		ParameterInteger parameter14 = new ParameterInteger(1, 0, Integer.MAX_VALUE, 1, Integer.class, "getSelectionType", "getSelectionType");
//		ParameterTypeInt parameterType14 = new ParameterTypeInt(parameter14.getNameParameter(), parameter14.getDescriptionParameter(), parameter14.getMin(), parameter14.getMax(), parameter14.getDefaultValueParameter());
//		parameterTypes.add(parameterType14);
//		parametersGeneticMiner.add(parameter14);
//
////		ParameterBoolean parameter15 = new ParameterBoolean(false, Boolean.class, "isKeepStatistics","isKeepStatistics");
////		ParameterTypeBoolean parameterType15 = new ParameterTypeBoolean(parameter15.getNameParameter(), parameter15.getDescriptionParameter(), parameter15.getDefaultValueParameter());
////		parameterTypes.add(parameterType15);
////		parametersGeneticMiner.add(parameter15);
//		
//		ParameterBoolean parameter15 = new ParameterBoolean(false, Boolean.class, "generate seed","generate seed");
//		ParameterTypeBoolean parameterType15 = new ParameterTypeBoolean(parameter15.getNameParameter(), parameter15.getDescriptionParameter(), parameter15.getDefaultValueParameter());
//		parameterTypes.add(parameterType15);
//		parametersGeneticMiner.add(parameter15);
//
//		return parameterTypes;
//	}
//
//	private GeneticMinerSettings getConfiguration(List<Parameter> parametersGeneticMiner) {
//		GeneticMinerSettings geneticMinerSettings = new GeneticMinerSettings();
//		try {
//		Parameter parameter3 = parametersGeneticMiner.get(2);
//		int par3int = getParameterAsInt(parameter3.getNameParameter());
//		geneticMinerSettings.setSeed(par3int);
//
//		Parameter parameter1 = parametersGeneticMiner.get(0);
//		int par1int = getParameterAsInt(parameter1.getNameParameter());
//		geneticMinerSettings.setPopulationSize(par1int);
//
//		Parameter parameter2 = parametersGeneticMiner.get(1);
//		String par2str = getParameterAsString(parameter2.getNameParameter());
//		geneticMinerSettings.setFilename(par2str);
//
//		Parameter parameter4 = parametersGeneticMiner.get(3);
//		double par4int = getParameterAsDouble(parameter4.getNameParameter());
//		geneticMinerSettings.setPower(par4int);
//
//		Parameter parameter12 = parametersGeneticMiner.get(11);
//		double par12int = getParameterAsDouble(parameter12.getNameParameter());
//		geneticMinerSettings.setStopFitness(par12int);
//
//		Parameter parameter7 = parametersGeneticMiner.get(6);
//		int par7int = getParameterAsInt(parameter7.getNameParameter());
//		geneticMinerSettings.setInitialPopulationType(par7int);
//
//		Parameter parameter5 = parametersGeneticMiner.get(4);
//		double par5int = getParameterAsDouble(parameter5.getNameParameter());
//		geneticMinerSettings.setElitismRate(par5int);
//
//		Parameter parameter13 = parametersGeneticMiner.get(12);
//		int par13int = getParameterAsInt(parameter13.getNameParameter());
//		geneticMinerSettings.setMaxGeneration(par13int);
//
//		Parameter parameter11 = parametersGeneticMiner.get(10);
//		int par11int = getParameterAsInt(parameter11.getNameParameter());
//		geneticMinerSettings.setFitnessType(par11int);
//
//		Parameter parameter14 = parametersGeneticMiner.get(13);
//		int par14int = getParameterAsInt(parameter14.getNameParameter());
//		geneticMinerSettings.setSelectionType(par14int);
//
//		Parameter parameter10 = parametersGeneticMiner.get(9);
//		double par10int = getParameterAsDouble(parameter10.getNameParameter());
//		geneticMinerSettings.setCrossoverRate(par10int);
//
//		Parameter parameter8 = parametersGeneticMiner.get(7);
//		int par8int = getParameterAsInt(parameter8.getNameParameter());
//		geneticMinerSettings.setCrossoverType(par8int);
//
//		Parameter parameter6 = parametersGeneticMiner.get(5);
//		double par6int = getParameterAsDouble(parameter6.getNameParameter());
//		geneticMinerSettings.setMutationRate(par6int);
//
//		Parameter parameter9 = parametersGeneticMiner.get(8);
//		int par9int = getParameterAsInt(parameter9.getNameParameter());
//		geneticMinerSettings.setMutationType(par9int);
//
////		Parameter parameter15 = parametersGeneticMiner.get(14);
////		boolean valPar15 = getParameterAsBoolean(parameter15.getNameParameter());
////		geneticMinerSettings.setKeepStatistics(valPar15);
//		
//		Parameter parameter15 = parametersGeneticMiner.get(14);
//		boolean valPar15 = getParameterAsBoolean(parameter15.getNameParameter());
//		if (valPar15) {
//			int generatedInt  = 0;
//			Random r = new Random();
//			generatedInt = r.nextInt();
//			geneticMinerSettings.setSeed(generatedInt);
//		}
//
//		} catch (UndefinedParameterError e) {
//			e.printStackTrace();
//		}
//	return geneticMinerSettings;
//	}
//
//	private void loadRequiredClasses () {
//		Utilities.loadRequiredClasses();
//	}
//}
