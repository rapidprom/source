package com.rapidminer.operator.miningplugins;

import java.util.*;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.util.ProMIOObjectList;
import com.rapidminer.util.Utilities;
import com.rapidminer.parameter.*;
import com.rapidminer.parameters.*;

import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.plugins.ilpminer.ILPMinerSettings;
import org.processmining.plugins.ilpminer.templates.javailp.PetriNetILPModel;
import org.processmining.plugins.ilpminer.templates.javailp.EmptyAfterCompletionILPModel;
import org.processmining.plugins.ilpminer.templates.javailp.FewestArcsILPModel;
import org.processmining.plugins.ilpminer.templates.javailp.FewestArcsSingleILPModel;
import org.processmining.plugins.ilpminer.templates.javailp.FreeChoiceILPModel;
import org.processmining.plugins.ilpminer.templates.javailp.PetriNetSingleLPModel;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.MarkingIOObject;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.prom.CallProm;

import com.rapidminer.callprom.ClassLoaderUtils;
import com.rapidminer.configuration.GlobalProMParameters;

import java.io.File;

public class ILPMinerTask extends Operator {

	private List<Parameter> parametersILPMiner = null;

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts().createPort("model (ProM Petri Net)");
	private OutputPort outputMarking = getOutputPorts().createPort("marking (ProM Marking)");

	public ILPMinerTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputMarking, MarkingIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work ILP Miner", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		
		System.out.println(PackageManager.getInstance().toString());
		//for (PackageDescriptor desc : PackageManager.getInstance().getInstalledPackages())
		//	System.out.println(desc.getName());
		
		
		
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());

//		XLogInfoIOObject XLogInfodata = inputXLogInfo.getData(XLogInfoIOObject.class);
		XLogInfo createLogInfo = XLogInfoFactory.createLogInfo(XLogdata.getData());
		pars.add(createLogInfo);

		ILPMinerSettings iLPMinerSettings = getConfiguration(this.parametersILPMiner);
		pars.add(iLPMinerSettings);
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "ILP Miner", pars);
		Petrinet pn = (Petrinet) runPlugin[0];
		addSinkPlace(pn);
		PetriNetIOObject petrinetIOObject = new PetriNetIOObject(pn);
		petrinetIOObject.setPluginContext(pluginContext);
		outputPetrinet.deliver(petrinetIOObject);
		MarkingIOObject markingIOObject = new MarkingIOObject((Marking) runPlugin[1]);
		markingIOObject.setPluginContext(pluginContext);
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(markingIOObject);
		instance.addToList(petrinetIOObject);
		outputMarking.deliver(markingIOObject);
		logService.log("end do work ILP Miner", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		loadRequiredClasses();
		this.parametersILPMiner = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		// 		Object[] par1categories = new Object[] {GTMFeatureType.Sequence, GTMFeatureType.Alphabet, GTMFeatureType.WholeTrace};
		//		ParameterCategory parameter1 = new ParameterCategory(par1categories, GTMFeatureType.Alphabet, GTMFeatureType.class, "Feature type", "Select feature type");
		//		ParameterTypeCategory parameterType1 = new ParameterTypeCategory(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), 
		//				parameter1.getOptionsParameter(), parameter1.getIndexValue(parameter1.getDefaultValueParameter()));
		//		parameterTypes.add(parameterType1);
		//		parametersGuideTreeMiner.add(parameter1);
		Object[] par1categories = new Object[] {EmptyAfterCompletionILPModel.class.getSimpleName(), FewestArcsILPModel.class.getSimpleName(), 
				PetriNetILPModel.class.getSimpleName(), PetriNetSingleLPModel.class.getSimpleName(), FewestArcsSingleILPModel.class.getSimpleName(), 
				FreeChoiceILPModel.class.getSimpleName()};
		ParameterCategory parameter1 = new ParameterCategory(par1categories, EmptyAfterCompletionILPModel.class.getSimpleName(), String.class, "Variant", "Variant");
		ParameterTypeCategory parameterType1 = new ParameterTypeCategory(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), 
				parameter1.getOptionsParameter(), parameter1.getIndexValue(parameter1.getDefaultValueParameter()));
		parameterTypes.add(parameterType1);
		parametersILPMiner.add(parameter1);
		return parameterTypes;
	}

	private ILPMinerSettings getConfiguration(List<Parameter> parametersILPMiner) {
		ILPMinerSettings iLPMinerSettings = new ILPMinerSettings();
		try {
			Parameter parameter1 = parametersILPMiner.get(0);
			int par1int = getParameterAsInt(parameter1.getNameParameter());
			Object valPar1 = parameter1.getValueParameter(par1int);
			String stringNameClass = (String) valPar1;
			Class<?> clazz = null;
			if (stringNameClass.equals(EmptyAfterCompletionILPModel.class.getSimpleName())) {
				clazz = EmptyAfterCompletionILPModel.class;
			}
			else if (stringNameClass.equals(FewestArcsILPModel.class.getSimpleName())) {
				clazz = FewestArcsILPModel.class;
			}
			else if (stringNameClass.equals(PetriNetILPModel.class.getSimpleName())) {
				clazz = PetriNetILPModel.class;
			}
			else if (stringNameClass.equals(PetriNetSingleLPModel.class.getSimpleName())) {
				clazz = PetriNetSingleLPModel.class;
			}
			else if (stringNameClass.equals(FewestArcsSingleILPModel.class.getSimpleName())) {
				clazz = FewestArcsSingleILPModel.class;
			}
			else if (stringNameClass.equals(FreeChoiceILPModel.class.getSimpleName())) {
				clazz = FreeChoiceILPModel.class;
			}
			iLPMinerSettings.setVariant(clazz);
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
	return iLPMinerSettings;
	}
	
	private void addSinkPlace(Petrinet pn) {
		// search for transitions with no outgoing places
		List<Transition> transList = new ArrayList<Transition>();
		Iterator<Transition> iterator = pn.getTransitions().iterator();
		while (iterator.hasNext()) {
			Transition next = iterator.next();
			Collection<PetrinetEdge<? extends PetrinetNode,? extends PetrinetNode>> outEdges = pn.getOutEdges(next);
			if (outEdges.size() == 0) {
				// has no outgoing places
				transList.add(next);
			}
		}
		if (transList.size()>1) {
			System.out.println("TRANSLIST SIZE IS BIGGER THAN 1");
		}
		Place sinkPlace = pn.addPlace("sink");
		// connect all transition with the place
		for (Transition t : transList) {
			pn.addArc(t, sinkPlace);
		}
	}


	private void loadRequiredClasses () {
		Utilities.loadRequiredClasses();
	}
}

