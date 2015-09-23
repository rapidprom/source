package com.rapidminer.operator.analysisplugins;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
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

import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import com.rapidminer.ioobjects.WoflanDiagnosisIOObject;
import com.rapidminer.ioobjectrenderers.WoflanDiagnosisIOObjectRenderer;

import org.processmining.plugins.InductiveMiner.mining.MiningParametersEKS;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMin;
import org.processmining.plugins.petrinet.behavioralanalysis.woflan.WoflanDiagnosis;
import org.rapidprom.prom.CallProm;

public class AnalyzewithWoflanTask extends Operator {

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputPetrinet = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputWoflanDiagnosis = getOutputPorts().createPort("woflan diagnosis (ProM WoflanDiagnosis)");
	private OutputPort outputWoflanDiagnosisString = getOutputPorts().createPort("woflan diagnosis (String)");
	private List<Parameter> parameters = null;
	
	public AnalyzewithWoflanTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputWoflanDiagnosis, WoflanDiagnosisIOObject.class));
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Analyze with Woflan", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		List<Object> pars = new ArrayList<Object>();
		PetriNetIOObject Petrinetdata = inputPetrinet.getData(PetriNetIOObject.class);
		pars.add(Petrinetdata.getData());
		WoflanDiagnosisIOObject woflanDiagnosisIOObject = null;

		SimpleTimeLimiter limiter = new SimpleTimeLimiter();
		
		double timer = getConfiguration(parameters);
		
		try 
		{
			woflanDiagnosisIOObject = limiter.callWithTimeout(new WOFLANER(pluginContext,Petrinetdata), (long) timer,  TimeUnit.SECONDS, true);
		} 
		catch (Exception e) 
		{
			woflanDiagnosisIOObject = new WoflanDiagnosisIOObject(new WoflanDiagnosis(Petrinetdata.getData()));
			e.printStackTrace();
		}
		
		woflanDiagnosisIOObject.setPluginContext(pluginContext);
		outputWoflanDiagnosis.deliver(woflanDiagnosisIOObject);
		
		Object[][] outputString = new Object[1][1];
		outputString[0][0] = woflanDiagnosisIOObject.getData().toString();
		ExampleSet es =	ExampleSetFactory.createExampleSet(outputString);
		
		outputWoflanDiagnosisString.deliver(es);
		logService.log("end do work Analyze with Woflan", LogService.NOTE);
	}
	
	public List<ParameterType> getParameterTypes() {
		Utilities.loadRequiredClasses();
		
		this.parameters = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
	
		ParameterDouble parameter1 = new ParameterDouble(60, 0, 1000, 1, Double.class, "Time limit (seconds)", "Time limit (seconds)");
		ParameterTypeDouble parameterType1 = new ParameterTypeDouble(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), (Double) parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parameters.add(parameter1);

		return parameterTypes;
	}
	
	private double getConfiguration(List<Parameter> parameters) {
		double timer = 0;
		try 
		{
			Parameter parameter1 = parameters.get(0);
			timer = getParameterAsDouble(parameter1.getNameParameter());
		}
		catch (UndefinedParameterError e) 
		{
			e.printStackTrace();
		}
			
		return timer;
	}
	
	class WOFLANER implements Callable<WoflanDiagnosisIOObject>{

		PluginContext pc;
		PetriNetIOObject pn;
		public WOFLANER(PluginContext pc, PetriNetIOObject pn ) {
			this.pc = pc;
			this.pn = pn;
		}
		@Override
		public WoflanDiagnosisIOObject call() throws Exception {
			CallProm cp = new CallProm();
			List<Object> pars = new ArrayList<Object>();
			pars.add(pn.getData());
			Object[] runPlugin = cp.runPlugin(pc, "XX", "Analyze with Woflan", pars);
			WoflanDiagnosisIOObject woflanDiagnosisIOObject = new WoflanDiagnosisIOObject((WoflanDiagnosis) runPlugin[0]);
			return woflanDiagnosisIOObject;
		}
		
	}

}
