package com.rapidminer.operator.miningplugins;

import java.util.*;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;
import com.rapidminer.util.Utilities;
import com.rapidminer.parameter.*;
import com.rapidminer.parameters.*;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.LogUtility;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.HeuristicsNetIOObject;

import org.processmining.models.heuristics.HeuristicsNet;
import org.rapidprom.prom.CallProm;

public class MineforaHeuristicsNetusingHeuristicsMinerTask extends Operator {

	private List<Parameter> parametersMineforaHeuristicsNetusingHeuristicsMiner = null;

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputHeuristicsNet = getOutputPorts().createPort("model (ProM Heuristics Net)");

	public MineforaHeuristicsNetusingHeuristicsMinerTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputHeuristicsNet, HeuristicsNetIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Mine for a Heuristics Net using Heuristics Miner", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

		HeuristicsMinerSettings heuristicsMinerSettings = getConfiguration(this.parametersMineforaHeuristicsNetusingHeuristicsMiner, XLogdata.getData());
		pars.add(heuristicsMinerSettings);
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Mine for a Heuristics Net using Heuristics Miner", pars);
		HeuristicsNetIOObject heuristicsNetIOObject = new HeuristicsNetIOObject((HeuristicsNet) runPlugin[0]);
		heuristicsNetIOObject.setPluginContext(pluginContext);
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(heuristicsNetIOObject);
		outputHeuristicsNet.deliver(heuristicsNetIOObject);
		logService.log("end do work Mine for a Heuristics Net using Heuristics Miner", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		loadRequiredClasses();
		this.parametersMineforaHeuristicsNetusingHeuristicsMiner = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterDouble parameter1 = new ParameterDouble(5, 0, 100, 0.01, Double.class, "Threshold: Relative-to-best", "getRelativeToBestThreshold");
		ParameterTypeDouble parameterType1 = new ParameterTypeDouble(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), (Double) parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parametersMineforaHeuristicsNetusingHeuristicsMiner.add(parameter1);
		
		ParameterDouble parameter2 = new ParameterDouble(90, 0, 100, 0.01, Double.class, "Threshold: Dependency", "getDependencyThreshold");
		ParameterTypeDouble parameterType2 = new ParameterTypeDouble(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getMin(), parameter2.getMax(), (Double) parameter2.getDefaultValueParameter());
		parameterTypes.add(parameterType2);
		parametersMineforaHeuristicsNetusingHeuristicsMiner.add(parameter2);
		
		ParameterDouble parameter3 = new ParameterDouble(90, 0, 100, 0.01, Double.class, "Threshold: Length-one-loops", "getL1lThreshold");
		ParameterTypeDouble parameterType3 = new ParameterTypeDouble(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getMin(), parameter3.getMax(), (Double) parameter3.getDefaultValueParameter());
		parameterTypes.add(parameterType3);
		parametersMineforaHeuristicsNetusingHeuristicsMiner.add(parameter3);

		ParameterDouble parameter4 = new ParameterDouble(90, 0, 100, 0.01, Double.class, "Threshold: Length-two-loops", "getL2lThreshold");
		ParameterTypeDouble parameterType4 = new ParameterTypeDouble(parameter4.getNameParameter(), parameter4.getDescriptionParameter(), parameter4.getMin(), parameter4.getMax(), (Double) parameter4.getDefaultValueParameter());
		parameterTypes.add(parameterType4);
		parametersMineforaHeuristicsNetusingHeuristicsMiner.add(parameter4);

		ParameterDouble parameter5 = new ParameterDouble(90, 0, 100, 0.01, Double.class, "Threshold: Long distance", "getLongDistanceThreshold");
		ParameterTypeDouble parameterType5 = new ParameterTypeDouble(parameter5.getNameParameter(), parameter5.getDescriptionParameter(), parameter5.getMin(), parameter5.getMax(), (Double) parameter5.getDefaultValueParameter());
		parameterTypes.add(parameterType5);
		parametersMineforaHeuristicsNetusingHeuristicsMiner.add(parameter5);

	
		ParameterBoolean parameter6 = new ParameterBoolean(true, Boolean.class, "All tasks connected","isUseAllConnectedHeuristics");
		ParameterTypeBoolean parameterType6 = new ParameterTypeBoolean(parameter6.getNameParameter(), parameter6.getDescriptionParameter(), parameter6.getDefaultValueParameter());
		parameterTypes.add(parameterType6);
		parametersMineforaHeuristicsNetusingHeuristicsMiner.add(parameter6);

		ParameterBoolean parameter7 = new ParameterBoolean(false, Boolean.class, "Long distance dependency","isUseLongDistanceDependency");
		ParameterTypeBoolean parameterType7 = new ParameterTypeBoolean(parameter7.getNameParameter(), parameter7.getDescriptionParameter(), parameter7.getDefaultValueParameter());
		parameterTypes.add(parameterType7);
		parametersMineforaHeuristicsNetusingHeuristicsMiner.add(parameter7);

		ParameterBoolean parameter8 = new ParameterBoolean(true, Boolean.class, "Ignore loop dependency thresholds","Ignore loop dependency thresholds");
		ParameterTypeBoolean parameterType8 = new ParameterTypeBoolean(parameter8.getNameParameter(), parameter8.getDescriptionParameter(), parameter8.getDefaultValueParameter());
		parameterTypes.add(parameterType8);
		parametersMineforaHeuristicsNetusingHeuristicsMiner.add(parameter8);

		return parameterTypes;
	}

	private HeuristicsMinerSettings getConfiguration(List<Parameter> parametersMineforaHeuristicsNetusingHeuristicsMiner, XLog log) {
		HeuristicsMinerSettings heuristicsMinerSettings = new HeuristicsMinerSettings();
		try {


		Parameter parameter3 = parametersMineforaHeuristicsNetusingHeuristicsMiner.get(0);
		double par3int = getParameterAsDouble(parameter3.getNameParameter());
		heuristicsMinerSettings.setRelativeToBestThreshold(par3int/100d);

		Parameter parameter4 = parametersMineforaHeuristicsNetusingHeuristicsMiner.get(1);
		double par4int = getParameterAsDouble(parameter4.getNameParameter());
		heuristicsMinerSettings.setDependencyThreshold(par4int/100d);

		Parameter parameter5 = parametersMineforaHeuristicsNetusingHeuristicsMiner.get(2);
		double par5int = getParameterAsDouble(parameter5.getNameParameter());
		heuristicsMinerSettings.setL1lThreshold(par5int/100d);

		Parameter parameter6 = parametersMineforaHeuristicsNetusingHeuristicsMiner.get(3);
		double par6int = getParameterAsDouble(parameter6.getNameParameter());
		heuristicsMinerSettings.setL2lThreshold(par6int/100d);

		Parameter parameter7 = parametersMineforaHeuristicsNetusingHeuristicsMiner.get(4);
		double par7int = getParameterAsDouble(parameter7.getNameParameter());
		heuristicsMinerSettings.setLongDistanceThreshold(par7int/100d);

		Parameter parameter8 = parametersMineforaHeuristicsNetusingHeuristicsMiner.get(5);
		boolean valPar8 = getParameterAsBoolean(parameter8.getNameParameter());
		heuristicsMinerSettings.setUseAllConnectedHeuristics(valPar8);

		Parameter parameter10 = parametersMineforaHeuristicsNetusingHeuristicsMiner.get(6);
		boolean valPar10 = getParameterAsBoolean(parameter10.getNameParameter());
		heuristicsMinerSettings.setUseLongDistanceDependency(valPar10);

		Parameter parameter11 = parametersMineforaHeuristicsNetusingHeuristicsMiner.get(7);
		boolean valPar11 = getParameterAsBoolean(parameter11.getNameParameter());
		heuristicsMinerSettings.setCheckBestAgainstL2L(valPar11);
		
		heuristicsMinerSettings.setAndThreshold(Double.NaN);
		
		
		/*
		 * XEventClassifier nameCl = new XEventNameClassifier();
            XEventClassifier lifeTransCl = new XEventLifeTransClassifier();
            XEventAttributeClassifier attrClass = new XEventAndClassifier(nameCl, lifeTransCl);
		 */
		for(XEventClassifier clas :  LogUtility.getEventClassifiers(log))
			if(clas != null)
			{
				heuristicsMinerSettings.setClassifier(clas);
				break;
			}
		
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
	return heuristicsMinerSettings;
	}
	
	private void loadRequiredClasses () {
		Utilities.loadRequiredClasses();
	}
}
