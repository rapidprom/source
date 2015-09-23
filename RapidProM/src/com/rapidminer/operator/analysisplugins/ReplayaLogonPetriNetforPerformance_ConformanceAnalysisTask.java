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
import com.rapidminer.util.ProMIOObjectList;
import com.rapidminer.parameter.*;
import com.rapidminer.parameters.*;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.FitnessIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.ioobjects.ManifestIOObject;

import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.rapidprom.prom.CallProm;

public class ReplayaLogonPetriNetforPerformance_ConformanceAnalysisTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputPetrinetGraph = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputManifest = getOutputPorts().createPort("model (ProM Manifest)");
	private OutputPort outputFitness = getOutputPorts().createPort("example set (Data Table)");
	private ArrayList<Parameter> parametersPerfConf;

	public ReplayaLogonPetriNetforPerformance_ConformanceAnalysisTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputManifest, ManifestIOObject.class));
		//getTransformer().addRule( new GenerateNewMDRule(outputFitness, FitnessIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Replay a Log on Petri Net for Performance/Conformance Analysis", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		PetriNetIOObject PetrinetGraphdata = inputPetrinetGraph.getData(PetriNetIOObject.class);
		pars.add(PetrinetGraphdata.getData());

		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		pars.add(XLogdata.getData());
		
		int configurationMaxNumberStates = getConfigurationMaxNumberStates(parametersPerfConf);
		pars.add(configurationMaxNumberStates);

		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Replay a Log on Petri Net for Performance/Conformance Analysis", pars);
		String visualizationPreference = getVisualizationPreference(this.parametersPerfConf);
		ManifestIOObject manifestIOObject = new ManifestIOObject((Manifest) runPlugin[0]);
		manifestIOObject.setVisType(visualizationPreference);
		manifestIOObject.setPluginContext(pluginContext);
		outputManifest.deliver(manifestIOObject);
				
		double sum = 0;
		for(int j = 0; j < manifestIOObject.getManifest().getCasePointers().length ; j++)
		{
			sum = sum + manifestIOObject.getManifest().getTraceFitness(j);
		}
		
		ExampleSet es =	ExampleSetFactory.createExampleSet(new Object[][]{{"fitness", sum/(double)manifestIOObject.getManifest().getCasePointers().length},{"precision",0.0}});
		outputFitness.deliver(es);
		
		
		
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(manifestIOObject);
		logService.log("end do work Replay a Log on Petri Net for Performance/Conformance Analysis", LogService.NOTE);
	}
	
	public List<ParameterType> getParameterTypes() {
		this.parametersPerfConf = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		Object[] par1categories = new Object[] {"Performance Projection to Model", "Project Manifest to Log", "Project Manifest to Model for Conformance"};
		ParameterCategory parameter1 = new ParameterCategory(par1categories, "Project Manifest to Model for Conformance",String.class, "visualize performance", "visualize performance");
		ParameterTypeCategory parameterType1 = new ParameterTypeCategory(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getOptionsParameter(), parameter1.getIndexValue(parameter1.getDefaultValueParameter()));
		parameterTypes.add(parameterType1);
		parametersPerfConf.add(parameter1);	
		
		ParameterInteger parameter2 = new ParameterInteger(100, 1, Integer.MAX_VALUE, 1, Integer.class, "Max Explored States (in Hundreds)", "Maximum Explored States");
		ParameterTypeInt parameterType2 = new ParameterTypeInt(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getMin(), parameter2.getMax(), parameter2.getDefaultValueParameter());
		parameterTypes.add(parameterType2);
		parametersPerfConf.add(parameter2);
		
		return parameterTypes;
	}
	
	private String getVisualizationPreference(List<Parameter> parameters) {
		try {
			Parameter parameter1 = parameters.get(0);
			int par1int = getParameterAsInt(parameter1.getNameParameter());
			Object valPar1 = parameter1.getValueParameter(par1int);
			return (String) valPar1;
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private int getConfigurationMaxNumberStates(List<Parameter> parameters) {
		Parameter parameter2 = parameters.get(1);
		Integer valPar2 = 1;
		try {
			valPar2 = getParameterAsInt(parameter2.getNameParameter());
		} catch (UndefinedParameterError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valPar2;
	}

}
