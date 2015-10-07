package org.rapidprom.operators.streams.generators;

import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.eventstream.authors.cpn.parameters.CPN2XSStreamParameters;
import org.processmining.eventstream.authors.cpn.plugins.CPNModelToXSEventStreamAuthorPlugin;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSPublisher;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;

import com.rapidminer.ioobjects.CPNModelIOObject;
import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.ioobjects.XSEventStreamIOObject;
import com.rapidminer.ioobjects.XSPublisherIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.EqualStringCondition;
import com.rapidminer.tools.LogService;

public class CPNToEventStreamOperator extends Operator {

	private static String PARAMETER_KEY_MAX_STEPS = "max_steps";
	private static String PARAMETER_LABEL_MAX_STEPS = "Max steps (-1 for no limit)";

	private static String PARAMETER_KEY_REPETITIONS = "repetitions";
	private static String PARAMETER_LABEL_REPETITIONS = "Repetitions";

	private static String PARAMETER_KEY_STEP_DELAY = "step_delay";
	private static String PARAMETER_LABEL_STEP_DELAY = "Step delay (ms) inbetween two consecutive emissions.";

	private static String PARAMETER_KEY_CASE_IDENTIFICATION = "case_identification";
	private static String PARAMETER_LABEL_CASE_IDENTIFICATION = "Case identification, specifying what how to identify cases within the stream.";

	private static String PARAMETER_KEY_CASE_IDENTIFICATION_VARIABLE = "cpn_variable";
	private static String PARAMETER_LABEL_CASE_IDENTIFICATION_VARIABLE = "Case identification by CPN variable, denotes what CPN variable to track.";
	private static String PARAMETER_DEFAULT_VALUE_CASE_IDENTIFICATION_VARIABLE = "trace";

	private enum CaseIdentificationTechniques {
		REPITITION("by repitition"), CPN_VARIABLE("by a CPN variable");

		private String toString;

		CaseIdentificationTechniques(String toString) {
			this.toString = toString;
		}

		@Override
		public String toString() {
			return toString;
		}
	}

	private InputPort inputCPNModel = getInputPorts()
			.createPort("model (ProM CPN model)", CPNModelIOObject.class);

	private OutputPort outputPublisher = getOutputPorts()
			.createPort("publisher (ProM)");
	private OutputPort outputStream = getOutputPorts()
			.createPort("Event Stream (ProM)");

	public CPNToEventStreamOperator(OperatorDescription description) {
		super(description);

		getTransformer().addRule(new GenerateNewMDRule(outputPublisher,
				XSPublisherIOObject.class));
		getTransformer().addRule(
				new GenerateNewMDRule(outputStream, MarkingIOObject.class));
		// TODO Auto-generated constructor stub
	}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "start do work Stream Generator");

		// ProMContextIOObject context =
		// inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		CPN2XSStreamParameters parameters = new CPN2XSStreamParameters();

		Object[] result = CPNModelToXSEventStreamAuthorPlugin
				.cpnToXSEventStreamPlugin(pluginContext,
						inputCPNModel.getData(CPNModelIOObject.class).getData(),
						parameters);

		outputPublisher
				.deliver(new XSPublisherIOObject((XSPublisher) result[0]));
		outputStream
				.deliver(new XSEventStreamIOObject((XSEventStream) result[1]));

		logger.log(Level.INFO, "start do work Stream Generator");
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();
		setupMaxStepsParameter(parameterTypes);
		setupRepetitionsParameter(parameterTypes);
		setupStepDelayParameter(parameterTypes);
		setupCaseIdentificationParameter(parameterTypes);
		return parameterTypes;
	}

	private void setupMaxStepsParameter(List<ParameterType> parameterTypes) {
		ParameterTypeInt maxSteps = new ParameterTypeInt(
				PARAMETER_KEY_MAX_STEPS, PARAMETER_LABEL_MAX_STEPS, -1,
				Integer.MAX_VALUE, -1, true);
		maxSteps.setOptional(false);
		parameterTypes.add(maxSteps);
	}

	private void setupRepetitionsParameter(List<ParameterType> parameterTypes) {
		ParameterTypeInt repetitions = new ParameterTypeInt(
				PARAMETER_KEY_REPETITIONS, PARAMETER_LABEL_REPETITIONS, 1,
				Integer.MAX_VALUE, 1, true);
		repetitions.setOptional(false);
		parameterTypes.add(repetitions);
	}

	private void setupStepDelayParameter(List<ParameterType> parameterTypes) {
		ParameterTypeInt stepDelay = new ParameterTypeInt(
				PARAMETER_KEY_STEP_DELAY, PARAMETER_LABEL_STEP_DELAY, 0,
				Integer.MAX_VALUE, 0, true);
		stepDelay.setOptional(false);
	}

	private void setupCaseIdentificationParameter(
			List<ParameterType> parameterTypes) {

		Object[] caseIdentificationTechniques = EnumSet
				.allOf(CaseIdentificationTechniques.class).toArray();
		String[] caseIdentificationTechniqueNames = new String[caseIdentificationTechniques.length];
		for (int i = 0; i < caseIdentificationTechniques.length; i++) {
			caseIdentificationTechniqueNames[i] = caseIdentificationTechniques[i]
					.toString();
		}

		ParameterTypeCategory caseIdentificationCat = new ParameterTypeCategory(
				PARAMETER_KEY_CASE_IDENTIFICATION,
				PARAMETER_LABEL_CASE_IDENTIFICATION,
				caseIdentificationTechniqueNames, 0, true);

		parameterTypes.add(caseIdentificationCat);

		ParameterTypeString caseIdentificationVariable = new ParameterTypeString(
				PARAMETER_KEY_CASE_IDENTIFICATION_VARIABLE,
				PARAMETER_LABEL_CASE_IDENTIFICATION_VARIABLE,
				PARAMETER_DEFAULT_VALUE_CASE_IDENTIFICATION_VARIABLE, true);
		caseIdentificationVariable.setOptional(true);
		caseIdentificationVariable
				.registerDependencyCondition(new EqualStringCondition(this,
						PARAMETER_KEY_CASE_IDENTIFICATION, true, new String[] {
								CaseIdentificationTechniques.CPN_VARIABLE.toString }));
		parameterTypes.add(caseIdentificationVariable);
	}

}
