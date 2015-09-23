//package com.rapidminer.operator.analysisplugins;
//
//import java.util.*;
//
//import com.rapidminer.operator.Operator;
//import com.rapidminer.operator.OperatorDescription;
//import com.rapidminer.operator.OperatorException;
//import com.rapidminer.operator.ports.InputPort;
//import com.rapidminer.operator.ports.OutputPort;
//import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
//import com.rapidminer.parameter.*;
//import com.rapidminer.parameters.*;
//import com.rapidminer.tools.LogService;
//import com.rapidminer.tools.config.ConfigurationManager;
//import com.rapidminer.util.ProMIOObjectList;
//import com.rapidminer.util.Utilities;
//
//import org.processmining.framework.plugin.PluginContext;
//
//import com.rapidminer.ioobjects.ProMContextIOObject;
//
//import org.processmining.plugins.modelrepair.Uma_RepairModel_Plugin.RepairConfiguration;
//
//import com.rapidminer.ioobjects.MarkingIOObject;
//import com.rapidminer.ioobjects.XLogIOObject;
//import com.rapidminer.ioobjects.PetriNetIOObject;
//
//import org.processmining.models.graphbased.directed.petrinet.Petrinet;
//import org.processmining.models.graphbased.directed.petrinet.elements.Place;
//import org.processmining.models.semantics.petrinet.Marking;
//import org.rapidprom.prom.CallProm;
//
//import com.rapidminer.callprom.ClassLoaderUtils;
//import com.rapidminer.configuration.GlobalProMParameters;
//
//import java.io.File;
//
//public class RepairModelTask extends Operator {
//
//	private List<Parameter> parametersRepairModel = null;
//
//	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
//	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
//	private InputPort inputPetrinet = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
//	private OutputPort outputPetrinet = getOutputPorts().createPort("model (ProM Petri Net)");
//	private OutputPort outputMarking = getOutputPorts().createPort("marking (ProM Marking)");
//
//	public RepairModelTask(OperatorDescription description) {
//		super(description);
//		getTransformer().addRule( new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
//		getTransformer().addRule( new GenerateNewMDRule(outputMarking, MarkingIOObject.class));
//}
//
//	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Repair Model", LogService.NOTE);
//		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
//		PluginContext pluginContext = context.getPluginContext();
//		List<Object> pars = new ArrayList<Object>();
//		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
//		pars.add(XLogdata.getData());
//
//		PetriNetIOObject Petrinetdata = inputPetrinet.getData(PetriNetIOObject.class);
//		pars.add(Petrinetdata.getData());
//		
//		Petrinet pn = Petrinetdata.getData();
//		// calculate input marking
//		List<Place> startPlaces = getStartPlaces(pn); 
//		Marking initialMarking = new Marking();
//		for (Place place : startPlaces) {
//			initialMarking.add(place);
//		}
//		pars.add(initialMarking);
//		// calculate output marking
//		List<Place> endPlaces = getEndPlaces(pn);
//		Marking finalMarking = new Marking();
//		for (Place place : endPlaces) {
//			finalMarking.add(place);
//		}
//		pars.add(finalMarking);
////		MarkingIOObject Markingdata = inputMarking.getData(MarkingIOObject.class);
////		pars.add(Markingdata.getData());
////
////		MarkingIOObject Markingdata = inputMarking.getData(MarkingIOObject.class);
////		pars.add(Markingdata.getData());
//
//		RepairConfiguration repairConfiguration = getConfiguration(this.parametersRepairModel);
//		pars.add(repairConfiguration);
//		CallProm cp = new CallProm();
//		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Repair Model", pars);
//		PetriNetIOObject petrinetIOObject = new PetriNetIOObject((Petrinet) runPlugin[0]);
//		petrinetIOObject.setPluginContext(pluginContext);
//		outputPetrinet.deliver(petrinetIOObject);
//		MarkingIOObject markingIOObject = new MarkingIOObject((Marking) runPlugin[1]);
//		markingIOObject.setPluginContext(pluginContext);
//		outputMarking.deliver(markingIOObject);
//		// add to list so that afterwards it can be cleared if needed
//		ProMIOObjectList instance = ProMIOObjectList.getInstance();
//		instance.addToList(markingIOObject);
//		instance.addToList(petrinetIOObject);
//		logService.log("end do work Repair Model", LogService.NOTE);
//	}
//
//	public List<ParameterType> getParameterTypes() {
//		loadRequiredClasses();
//		this.parametersRepairModel = new ArrayList<Parameter>();
//		List<ParameterType> parameterTypes = super.getParameterTypes();
//
//		// boolean 	detectLoops = true;
//		ParameterBoolean parameter1 = new ParameterBoolean(false, Boolean.class, "detectLoops","detectLoops");
//		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getDefaultValueParameter());
//		parameterTypes.add(parameterType1);
//		parametersRepairModel.add(parameter1);
//		// boolean  detectSubProcesses  = true;
//		ParameterBoolean parameter2 = new ParameterBoolean(false, Boolean.class, "detectSubProcesses","detectSubProcesses");
//		ParameterTypeBoolean parameterType2 = new ParameterTypeBoolean(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getDefaultValueParameter());
//		parameterTypes.add(parameterType2);
//		parametersRepairModel.add(parameter2);
//		// boolean  removeInfrequentNodes = true;
//		ParameterBoolean parameter3 = new ParameterBoolean(false, Boolean.class, "removeInfrequentNodes","removeInfrequentNodes");
//		ParameterTypeBoolean parameterType3 = new ParameterTypeBoolean(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getDefaultValueParameter());
//		parameterTypes.add(parameterType3);
//		parametersRepairModel.add(parameter3);
//		// boolean  globalCostAlignment = true;
//		ParameterBoolean parameter4 = new ParameterBoolean(false, Boolean.class, "removeInfrequentNodes","removeInfrequentNodes");
//		ParameterTypeBoolean parameterType4 = new ParameterTypeBoolean(parameter4.getNameParameter(), parameter4.getDescriptionParameter(), parameter4.getDefaultValueParameter());
//		parameterTypes.add(parameterType4);
//		parametersRepairModel.add(parameter4);
//		// boolean  alignAlignments = true;
//		ParameterBoolean parameter5 = new ParameterBoolean(false, Boolean.class, "removeInfrequentNodes","removeInfrequentNodes");
//		ParameterTypeBoolean parameterType5 = new ParameterTypeBoolean(parameter5.getNameParameter(), parameter5.getDescriptionParameter(), parameter5.getDefaultValueParameter());
//		parameterTypes.add(parameterType5);
//		parametersRepairModel.add(parameter5);
//		// int		loopModelMoveCosts = 0;
//		ParameterInteger parameter6 = new ParameterInteger(1, 0, Integer.MAX_VALUE, 1, Integer.class, "loopModelMoveCosts", "loopModelMoveCosts");
//		ParameterTypeInt parameterType6 = new ParameterTypeInt(parameter6.getNameParameter(), parameter6.getDescriptionParameter(), parameter6.getMin(), parameter6.getMax(), parameter6.getDefaultValueParameter());
//		parameterTypes.add(parameterType6);
//		parametersRepairModel.add(parameter6);
//		// int      remove_keepIfMoreThan = 0;
//		ParameterInteger parameter7 = new ParameterInteger(1, 0, Integer.MAX_VALUE, 1, Integer.class, "remove_keepIfMoreThan", "remove_keepIfMoreThan");
//		ParameterTypeInt parameterType7 = new ParameterTypeInt(parameter7.getNameParameter(), parameter7.getDescriptionParameter(), parameter7.getMin(), parameter7.getMax(), parameter7.getDefaultValueParameter());
//		parameterTypes.add(parameterType7);
//		parametersRepairModel.add(parameter7);
//		// int		globalCost_maxIterations = 1;
//		ParameterInteger parameter8 = new ParameterInteger(1, 0, Integer.MAX_VALUE, 1, Integer.class, "globalCost_maxIterations", "globalCost_maxIterations");
//		ParameterTypeInt parameterType8 = new ParameterTypeInt(parameter8.getNameParameter(), parameter8.getDescriptionParameter(), parameter8.getMin(), parameter8.getMax(), parameter8.getDefaultValueParameter());
//		parameterTypes.add(parameterType8);
//		parametersRepairModel.add(parameter8);
//		return parameterTypes;
//	}
//
//	private RepairConfiguration getConfiguration(List<Parameter> parametersRepairModel) {
//		RepairConfiguration repairConfiguration = new RepairConfiguration();
//		try {
//			// boolean 	detectLoops = true;
//			Parameter parameter1 = parametersRepairModel.get(0);
//			boolean valPar1 = getParameterAsBoolean(parameter1.getNameParameter());
//			repairConfiguration.detectLoops = valPar1;
//			// boolean  detectSubProcesses  = true;
//			Parameter parameter2 = parametersRepairModel.get(1);
//			boolean valPar2 = getParameterAsBoolean(parameter2.getNameParameter());
//			repairConfiguration.detectSubProcesses = valPar2;
//			// boolean  removeInfrequentNodes = true;
//			Parameter parameter3 = parametersRepairModel.get(2);
//			boolean valPar3 = getParameterAsBoolean(parameter3.getNameParameter());
//			repairConfiguration.removeInfrequentNodes = valPar3;
//			// boolean  globalCostAlignment = true;
//			Parameter parameter4 = parametersRepairModel.get(3);
//			boolean valPar4 = getParameterAsBoolean(parameter4.getNameParameter());
//			repairConfiguration.globalCostAlignment = valPar4;
//			// boolean  alignAlignments = true;
//			Parameter parameter5 = parametersRepairModel.get(4);
//			boolean valPar5 = getParameterAsBoolean(parameter5.getNameParameter());
//			repairConfiguration.globalCostAlignment = valPar5;
//			// int		loopModelMoveCosts = 0;
//			Parameter parameter6 = parametersRepairModel.get(5);
//			int par6int = getParameterAsInt(parameter6.getNameParameter());
//			repairConfiguration.loopModelMoveCosts = par6int;
//			// int      remove_keepIfMoreThan = 0;
//			Parameter parameter7 = parametersRepairModel.get(6);
//			int par7int = getParameterAsInt(parameter7.getNameParameter());
//			repairConfiguration.remove_keepIfMoreThan = par7int;
//			// int		globalCost_maxIterations = 1;
//			Parameter parameter8 = parametersRepairModel.get(7);
//			int par8int = getParameterAsInt(parameter8.getNameParameter());
//			repairConfiguration.globalCost_maxIterations = par8int;
//		} catch (UndefinedParameterError e) {
//			e.printStackTrace();
//		}
//	return repairConfiguration;
//	}
//
//	private void loadRequiredClasses () {
//		Utilities.loadRequiredClasses();
//	}
//	
//	private List<Place> getEndPlaces(Petrinet net) {
//		List<Place> places = new ArrayList<Place>();
//		Iterator<Place> placesIt = net.getPlaces().iterator();
//		while (placesIt.hasNext()) {
//			Place nextPlace = placesIt.next();
//			Collection outEdges = net.getOutEdges(nextPlace);
//			if (outEdges.isEmpty()) {
//				places.add(nextPlace);
//			}
//		}
//		return places;
//	}
//
//	private List<Place> getStartPlaces(Petrinet net) {
//		List<Place> places = new ArrayList<Place>();
//		Iterator<Place> placesIt = net.getPlaces().iterator();
//		while (placesIt.hasNext()) {
//			Place nextPlace = placesIt.next();
//			Collection inEdges = net.getInEdges(nextPlace);
//			if (inEdges.isEmpty()) {
//				places.add(nextPlace);
//			}
//		}
//		return places;
//	}
//}
//
