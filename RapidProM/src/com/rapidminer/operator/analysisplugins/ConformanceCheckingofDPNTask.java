package com.rapidminer.operator.analysisplugins;

import java.util.*;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.parameter.*;
import com.rapidminer.parameters.*;

import org.processmining.datapetrinets.ui.ImprovedEvClassLogMappingUI;
import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithDataFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;

import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.PetriNetWithDataIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.BalancedReplayResultIOObject;

import org.processmining.plugins.balancedconformance.result.BalancedReplayResult;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.rapidprom.prom.CallProm;


public class ConformanceCheckingofDPNTask extends Operator {

	private List<Parameter> parametersConformanceCheckingofDPN = null;

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputPetriNet = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	//private InputPort inputInitialMarking = getInputPorts().createPort("ini (ProM Marking)", MarkingIOObject.class);
	//private InputPort inputFinalMarking = getInputPorts().createPort("fin (ProM Marking)", MarkingIOObject.class);
	
	private OutputPort outputBalancedReplayResult = getOutputPorts().createPort("prom BalancedReplayResult");
	private OutputPort outputFitness = getOutputPorts().createPort("example set with metrics (Data Table)");

	public ConformanceCheckingofDPNTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputBalancedReplayResult, BalancedReplayResultIOObject.class));
		//getTransformer().addRule( new GenerateNewMDRule(outputBalancedReplayResult, BalancedReplayResultIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Conformance Checking of DPN ", LogService.NOTE);
		List<Object> pars = new ArrayList<Object>();
		
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		
		PetriNetIOObject PetriNetWithDatadata = inputPetriNet.getData(PetriNetIOObject.class);
		PetriNetWithDataIOObject pn = convertPetriNet(PetriNetWithDatadata);
		pars.add(pn.getData());

		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());
		
		//MarkingIOObject iniMarking = inputInitialMarking.getData(MarkingIOObject.class);
		
		//final markings are automatically detected from sinks, FIX THIS ELLEGANTLY!!

		BalancedProcessorConfiguration balancedProcessorConfiguration = getConfiguration(this.parametersConformanceCheckingofDPN, pn, getInitMarking(pn), getFinalMarkings(pn), XLogdata);
		pars.add(balancedProcessorConfiguration);
		
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Conformance Checking of DPN (Balanced)", pars);
		BalancedReplayResultIOObject balancedReplayResultIOObject = new BalancedReplayResultIOObject((BalancedReplayResult) runPlugin[0]);
		
		ExampleSet es =	ExampleSetFactory.createExampleSet(new Object[][]{{"fitness", balancedReplayResultIOObject.getData().meanFitness}});
		outputFitness.deliver(es);
		
		balancedReplayResultIOObject.setPluginContext(pluginContext);
		outputBalancedReplayResult.deliver(balancedReplayResultIOObject);
		logService.log("end do work Conformance Checking of DPN ", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		
		this.parametersConformanceCheckingofDPN = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		Object[] par3categories = new Object[] {"MXML Legacy Classifier", "Event Name", "Resource", "Lifecycle transition"};
		ParameterCategory parameter3 = new ParameterCategory(par3categories, "Event Name",String.class, "Event Classifier", "");
		ParameterTypeCategory parameterType3 = new ParameterTypeCategory(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getOptionsParameter(), parameter3.getIndexValue(parameter3.getDefaultValueParameter()));
		parameterTypes.add(parameterType3);
		parametersConformanceCheckingofDPN.add(parameter3);

		ParameterInteger parameter2 = new ParameterInteger(1, 0, Integer.MAX_VALUE, 1, Integer.class, "Move on model cost", "Move on model cost");
		ParameterTypeInt parameterType2 = new ParameterTypeInt(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getMin(), parameter2.getMax(), parameter2.getDefaultValueParameter());
		parameterTypes.add(parameterType2);
		parametersConformanceCheckingofDPN.add(parameter2);
		
		ParameterInteger parameter8 = new ParameterInteger(1, 0, Integer.MAX_VALUE, 1, Integer.class, "Move on log cost", "Move on log cost");
		ParameterTypeInt parameterType8 = new ParameterTypeInt(parameter8.getNameParameter(), parameter8.getDescriptionParameter(), parameter8.getMin(), parameter8.getMax(), parameter8.getDefaultValueParameter());
		parameterTypes.add(parameterType8);
		parametersConformanceCheckingofDPN.add(parameter8);

		ParameterInteger parameter10 = new ParameterInteger(1, 0, Integer.MAX_VALUE, 1, Integer.class, "Missing write operation cost", "Missing write operation cost");
		ParameterTypeInt parameterType10 = new ParameterTypeInt(parameter10.getNameParameter(), parameter10.getDescriptionParameter(), parameter10.getMin(), parameter10.getMax(), parameter10.getDefaultValueParameter());
		parameterTypes.add(parameterType10);
		parametersConformanceCheckingofDPN.add(parameter10);

		ParameterInteger parameter11 = new ParameterInteger(1, 0, Integer.MAX_VALUE, 1, Integer.class, "Incorrect write operation cost", "Incorrect write operation cost");
		ParameterTypeInt parameterType11 = new ParameterTypeInt(parameter11.getNameParameter(), parameter11.getDescriptionParameter(), parameter11.getMin(), parameter11.getMax(), parameter11.getDefaultValueParameter());
		parameterTypes.add(parameterType11);
		parametersConformanceCheckingofDPN.add(parameter11);

		return parameterTypes;
	}

	private BalancedProcessorConfiguration getConfiguration(List<Parameter> parametersConformanceCheckingofDPN, PetriNetWithDataIOObject net, Marking initialMarking, Marking[] finalMarkings, XLogIOObject log) 
	{
		
		BalancedProcessorConfiguration balancedProcessorConfiguration = null;
		
		try 
		{
			
			XEventClassifier classifier = null;
			
			Parameter parameter0 = parametersConformanceCheckingofDPN.get(0);
			int par0int = getParameterAsInt(parameter0.getNameParameter());
			String valPar0 = (String) parameter0.getValueParameter(par0int);	
			
			if(valPar0.matches("MXML Legacy Classifier"))
				classifier =  new XEventAttributeClassifier("MXML Legacy Classifier", XConceptExtension.KEY_NAME, XLifecycleExtension.KEY_TRANSITION);
			
			else if(valPar0.matches("Resource"))
				classifier = new XEventResourceClassifier();
			
			else if(valPar0.matches("Lifecycle transition"))
				classifier = new XEventLifeTransClassifier();
			
			else 
				classifier = new XEventNameClassifier();
			
			
			Parameter parameter1 = parametersConformanceCheckingofDPN.get(1);
			int par1int = getParameterAsInt(parameter1.getNameParameter());
		
			Parameter parameter2 = parametersConformanceCheckingofDPN.get(2);
			int par2int = getParameterAsInt(parameter2.getNameParameter());
			
			Parameter parameter3 = parametersConformanceCheckingofDPN.get(3);
			int par3int = getParameterAsInt(parameter3.getNameParameter());
			
			Parameter parameter4 = parametersConformanceCheckingofDPN.get(4);
			int par4int = getParameterAsInt(parameter4.getNameParameter());
		
			
			balancedProcessorConfiguration = BalancedProcessorConfiguration.newDefaultInstance(net.getData(), initialMarking, finalMarkings, log.getData(), classifier, par1int, par2int, par3int, par4int);
			balancedProcessorConfiguration.setActivityMapping(createDefaultMappingTransitionsToEventClasses(net.getData(), classifier, XLogInfoFactory.createLogInfo(log.getData())));
		} 
		catch (UndefinedParameterError e) 
		{
			//balancedProcessorConfiguration = new BalancedProcessorConfiguration();
			e.printStackTrace();
		}
	return balancedProcessorConfiguration;
	}
	
	private Marking[] getFinalMarkings(PetriNetWithDataIOObject pn)
	{
		List<Place> places = new ArrayList<Place>();
		Iterator<Place> placesIt = pn.getData().getPlaces().iterator();
		while (placesIt.hasNext()) {
			Place nextPlace = placesIt.next();
			Collection outEdges = pn.getData().getOutEdges(nextPlace);
			if (outEdges.isEmpty()) {
				places.add(nextPlace);
			}
		}
		Marking[] finalMarking = new Marking[places.size()];
		for(int i = 0; i < places.size() ; i++)
		{
			finalMarking[i] = new Marking();
			finalMarking[i].add(places.get(i));
		}
		return finalMarking;
	}
	
	private Marking getInitMarking(PetriNetWithDataIOObject pn) {
		List<Place> places = new ArrayList<Place>();
		Iterator<Place> placesIt = pn.getData().getPlaces().iterator();
		while (placesIt.hasNext()) {
			Place nextPlace = placesIt.next();
			Collection inEdges = pn.getData().getInEdges(nextPlace);
			if (inEdges.isEmpty()) {
				places.add(nextPlace);
			}
		}
		Marking initialMarking = new Marking();
		for (Place place : places) {
			initialMarking.add(place);
		}
		return initialMarking;
	}
	
	private static PetriNetWithDataIOObject convertPetriNet(PetriNetIOObject net)
	{
		PetriNetWithData dpnNet = null;
		try
		{
			//PetriNetWithDataFactory dpnFactory = new PetriNetWithDataFactory(net.getData(), net.getData().getLabel(), false);
			PetriNetWithDataFactory dpnFactory = new PetriNetWithDataFactory((PetrinetGraph) net.getPn().getGraph(), net.getData().getLabel(), false);
			dpnNet = dpnFactory.getRetValue();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	    return new PetriNetWithDataIOObject(dpnNet);
	}
	
	public static TransEvClassMapping createDefaultMappingTransitionsToEventClasses(PetriNetWithData net,

            XEventClassifier classifier, XLogInfo info) {

    TransEvClassMapping activityMapping = new TransEvClassMapping(classifier, ImprovedEvClassLogMappingUI.DUMMY);

    for (Transition t : net.getTransitions()) {

            for (XEventClass eventClass : info.getEventClasses().getClasses()) {

                    if (eventClass.getId().equalsIgnoreCase(t.getLabel())) {

                            activityMapping.put(t, eventClass);

                    }

            }

    }

    for (Transition t : net.getTransitions()) {

            if (!activityMapping.containsKey(t)) {

                    activityMapping.put(t, activityMapping.getDummyEventClass());

            }

    }

    return activityMapping;

}


}
