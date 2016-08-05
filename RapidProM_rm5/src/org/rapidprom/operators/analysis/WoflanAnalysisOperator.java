package org.rapidprom.operators.analysis;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.behavioralanalysis.woflan.Woflan;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.WoflanDiagnosisIOObject;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;

public class WoflanAnalysisOperator extends Operator {

	private static final String PARAMETER_0_KEY = "Enable Time limit",
			PARAMETER_0_DESCR = "Tries to evaluate soundness within a given time period.",
			PARAMETER_1_KEY = "Time limit (sec)",
			PARAMETER_1_DESCR = "Time limit before the analysis is cancelled. "
					+ "Helpful when analyzing large Petri nets.";

	private InputPort input = getInputPorts()
			.createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputWoflan = getOutputPorts()
			.createPort("woflan diagnosis (ProM WoflanDiagnosis)");
	private OutputPort outputWoflanString = getOutputPorts()
			.createPort("woflan diagnosis (String)");

	public WoflanAnalysisOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(outputWoflan,
				WoflanDiagnosisIOObject.class));
		getTransformer().addRule(
				new GenerateNewMDRule(outputWoflanString, ExampleSet.class));
	}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: woflan analysis");
		long time = System.currentTimeMillis();

		WoflanDiagnosisIOObject woflanDiagnosisIOObject = null;
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(Woflan.class);
		SimpleTimeLimiter limiter = new SimpleTimeLimiter(
				Executors.newSingleThreadExecutor());
		Object[][] outputString = new Object[1][1];

		try {
			if (getParameterAsBoolean(PARAMETER_0_KEY))
				woflanDiagnosisIOObject = limiter.callWithTimeout(
						new WOFLANER(pluginContext),
						getParameterAsInt(PARAMETER_1_KEY), TimeUnit.SECONDS,
						true);
			else
				woflanDiagnosisIOObject = limiter.callWithTimeout(
						new WOFLANER(pluginContext), Long.MAX_VALUE,
						TimeUnit.SECONDS, true);

			outputString[0][0] = woflanDiagnosisIOObject.getArtifact()
					.toString();
			outputWoflan.deliver(woflanDiagnosisIOObject);

		} catch (UncheckedTimeoutException e) {

			outputString[0][0] = " Woflan could not evaluate soundness in the given time.";
			logger.log(Level.INFO, "Woflan timed out.");

			pluginContext.getProgress().cancel();

		} catch (Exception e1) {

			e1.printStackTrace();
			outputString[0][0] = " Error checking soundness.";
			pluginContext.getProgress().cancel();
		}

		ExampleSet es = ExampleSetFactory.createExampleSet(outputString);

		outputWoflanString.deliver(es);

		logger.log(Level.INFO, "End: woflan analysis ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeBoolean parameter0 = new ParameterTypeBoolean(
				PARAMETER_0_KEY, PARAMETER_0_DESCR, true);
		parameterTypes.add(parameter0);

		ParameterTypeInt parameter1 = new ParameterTypeInt(PARAMETER_1_KEY,
				PARAMETER_1_DESCR, 0, 10000, 60);
		parameterTypes.add(parameter1);

		return parameterTypes;
	}

	class WOFLANER implements Callable<WoflanDiagnosisIOObject> {

		private PluginContext pluginContext;

		public WOFLANER(PluginContext input) {
			pluginContext = input;
		}

		@Override
		public WoflanDiagnosisIOObject call() throws Exception {
			PetriNetIOObject petriNet = input.getData(PetriNetIOObject.class);
			Woflan woflan = new Woflan();
			return new WoflanDiagnosisIOObject(
					woflan.diagnose(pluginContext, petriNet.getArtifact()),
					pluginContext);
		}

	}

}
