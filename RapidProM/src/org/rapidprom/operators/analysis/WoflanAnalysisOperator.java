package org.rapidprom.operators.analysis;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.behavioralanalysis.woflan.Woflan;
import org.processmining.plugins.petrinet.behavioralanalysis.woflan.WoflanDiagnosis;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.WoflanDiagnosisIOObject;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;

public class WoflanAnalysisOperator extends Operator {

	private static final String PARAMETER_1 = "Time limit (sec)";

	private InputPort input = getInputPorts().createPort(
			"model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputWoflan = getOutputPorts().createPort(
			"woflan diagnosis (ProM WoflanDiagnosis)");
	private OutputPort outputWoflanString = getOutputPorts().createPort(
			"woflan diagnosis (String)");

	public WoflanAnalysisOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputWoflan,
						WoflanDiagnosisIOObject.class));
		getTransformer().addRule(
				new GenerateNewMDRule(outputWoflanString, ExampleSet.class));
	}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: woflan analysis");
		long time = System.currentTimeMillis();

		PetriNetIOObject petriNet = input.getData(PetriNetIOObject.class);
		WoflanDiagnosisIOObject woflanDiagnosisIOObject = null;
		SimpleTimeLimiter limiter = new SimpleTimeLimiter();

		try {
			woflanDiagnosisIOObject = limiter.callWithTimeout(new WOFLANER(
					petriNet.getPluginContext(), petriNet), getTimer(),
					TimeUnit.SECONDS, true);
		} catch (Exception e) {
			woflanDiagnosisIOObject = new WoflanDiagnosisIOObject(
					new WoflanDiagnosis(petriNet.getArtifact()),petriNet.getPluginContext());
			e.printStackTrace();
		}

		outputWoflan.deliver(woflanDiagnosisIOObject);

		Object[][] outputString = new Object[1][1];
		outputString[0][0] = woflanDiagnosisIOObject.getArtifact().toString();
		ExampleSet es = ExampleSetFactory.createExampleSet(outputString);

		outputWoflanString.deliver(es);

		logger.log(Level.INFO,
				"End: woflan analysis (" + (System.currentTimeMillis() - time)
						/ 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeInt parameter1 = new ParameterTypeInt(PARAMETER_1,
				PARAMETER_1, 0, 1000, 60);
		parameterTypes.add(parameter1);

		return parameterTypes;
	}

	private long getTimer() {
		try {
			return (long) getParameterAsInt(PARAMETER_1);
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
		return 0;
	}

	class WOFLANER implements Callable<WoflanDiagnosisIOObject> {

		PluginContext pc;
		PetriNetIOObject pn;

		public WOFLANER(PluginContext pc, PetriNetIOObject pn) {
			this.pc = pc;
			this.pn = pn;
		}

		@Override
		public WoflanDiagnosisIOObject call() throws Exception {
			Woflan woflan = new Woflan();
			return new WoflanDiagnosisIOObject(woflan.diagnose(pc,
					pn.getArtifact()),pc);
		}

	}

}
