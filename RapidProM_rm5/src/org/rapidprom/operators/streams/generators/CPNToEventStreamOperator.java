package org.rapidprom.operators.streams.generators;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.eventstream.authors.cpn.parameters.CPN2XSEventStreamCaseIdentification;
import org.processmining.eventstream.authors.cpn.parameters.CPN2XSEventStreamParameters;
import org.processmining.eventstream.authors.cpn.plugins.CPNModelToXSEventStreamAuthorPlugin;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.enums.CommunicationType;
import org.processmining.stream.core.interfaces.XSAuthor;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.CPNModelIOObject;
import org.rapidprom.ioobjects.streams.XSAuthorIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualStringCondition;
import com.rapidminer.tools.LogService;

public class CPNToEventStreamOperator extends Operator {

	private static final String PARAMETER_DEFAULT_VALUE_CASE_IDENTIFICATION_VARIABLE = CPN2XSEventStreamCaseIdentification.CPN_VARIABLE
			.getDefaultValue();
	private static final String PARAMETER_KEY_CASE_IDENTIFICATION = "case_identification";

	private static final String PARAMETER_KEY_CASE_IDENTIFICATION_VARIABLE = "cpn_variable";
	private static final String PARAMETER_KEY_COMMUNICATION_TYPE = "communication_type";

	private static final String PARAMETER_KEY_INCLUDE_ADDITIONAL_DATA = "include_other_variables";

	private static final String PARAMETER_KEY_MAX_STEPS = "max_steps";

	private static final String PARAMETER_KEY_REPETITIONS = "repetitions";
	private static final String PARAMETER_KEY_STEP_DELAY = "step_delay";
	private static final String PARAMETER_LABEL_CASE_IDENTIFICATION = "Case identification, specifying what how to identify cases within the stream.";
	private static final String PARAMETER_LABEL_CASE_IDENTIFICATION_VARIABLE = "Case identification by CPN variable, denotes what CPN variable to track.";
	private static final String PARAMETER_LABEL_COMMUNIATION_TYPE = "The communicationtype of the selected stream, in synchronous mode, events will only be emitted if the receiving end is ready to accept new packages.";

	private static final String PARAMETER_LABEL_INCLUDE_ADDITIONAL_DATA = "If this parameter is set to true, the events will include other variable values as well";
	private static final String PARAMETER_LABEL_MAX_STEPS = "Max steps (-1 for no limit) within one simulation of the CPN model.";
	private static final String PARAMETER_LABEL_REPETITIONS = "Number of simulations.";

	private static final String PARAMETER_LABEL_STEP_DELAY = "Step delay (ms) inbetween two consecutive emissions.";
	private static final String PARAMETER_OPTION_CASE_IDENTIFICATION_REPETITION = CPN2XSEventStreamCaseIdentification.REPITITION
			.toString();

	private static final String PARAMETER_OPTION_CASE_IDENTIFICATION_VARIABELE = CPN2XSEventStreamCaseIdentification.CPN_VARIABLE
			.toString();
	private static final String PARAMETER_OPTION_COMMUNICATION_TYPE_ASYNC = CommunicationType.ASYNC
			.toString();
	private static final String PARAMETER_OPTION_COMMUNICATION_TYPE_SYNC = CommunicationType.SYNC
			.toString();
	private static final String[] PARAMETER_OPTIONS_CASE_IDENTIFICATION = new String[] {
			PARAMETER_OPTION_CASE_IDENTIFICATION_VARIABELE,
			PARAMETER_OPTION_CASE_IDENTIFICATION_REPETITION };
	private static final String[] PARAMETER_OPTIONS_COMMUNICATION_TYPE = new String[] {
			PARAMETER_OPTION_COMMUNICATION_TYPE_SYNC,
			PARAMETER_OPTION_COMMUNICATION_TYPE_ASYNC };

	private static final String PARAMETER_KEY_IGNORE_PAGE = "ignore_page";
	private static final String PARAMETER_LABEL_IGNORE_PAGE = "Ignore CPN model's page information in emitted events.";

	private static final String PARAMETER_KEY_IGNORE_PATTERSNS = "ignore_patterns";
	private static final String PARAMETER_LABEL_IGNORE_PATTERNS = "Provide a comma separated list of patterns to ignore for event emission";

	private InputPort inputCPNModel = getInputPorts().createPort("cpn model",
			CPNModelIOObject.class);
	private OutputPort outputAuthor = getOutputPorts().createPort("generator");
	private OutputPort outputStream = getOutputPorts().createPort("stream");

	public CPNToEventStreamOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputAuthor, XSAuthorIOObject.class));
		getTransformer().addRule(new GenerateNewMDRule(outputStream,
				XSEventStreamIOObject.class));
	}

	private CPN2XSEventStreamParameters determineCaseIdentification(
			CPN2XSEventStreamParameters params) throws UndefinedParameterError {
		String caseIdentificationType = PARAMETER_OPTIONS_CASE_IDENTIFICATION[getParameterAsInt(
				PARAMETER_KEY_CASE_IDENTIFICATION)];
		if (caseIdentificationType
				.equals(PARAMETER_OPTION_CASE_IDENTIFICATION_REPETITION)) {
			params.setCaseIdentificationType(
					CPN2XSEventStreamCaseIdentification.REPITITION);
		} else if (caseIdentificationType
				.equals(PARAMETER_OPTION_CASE_IDENTIFICATION_VARIABELE)) {
			params.setCaseIdentificationType(
					CPN2XSEventStreamCaseIdentification.CPN_VARIABLE);
			params.setCaseIdentifier(getParameterAsString(
					PARAMETER_KEY_CASE_IDENTIFICATION_VARIABLE));
		}
		return params;
	}

	private CPN2XSEventStreamParameters determineCommunicationType(
			CPN2XSEventStreamParameters params) throws UndefinedParameterError {
		String communicationType = PARAMETER_OPTIONS_COMMUNICATION_TYPE[getParameterAsInt(
				PARAMETER_KEY_COMMUNICATION_TYPE)];
		if (communicationType
				.equals(PARAMETER_OPTION_COMMUNICATION_TYPE_ASYNC)) {
			params.setCommunicationType(CommunicationType.ASYNC);
		} else if (communicationType
				.equals(PARAMETER_OPTION_COMMUNICATION_TYPE_SYNC)) {
			params.setCommunicationType(CommunicationType.SYNC);
		}
		return params;
	}

	@SuppressWarnings("unchecked")
	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "start do work Stream Generator");

		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(
						CPNModelToXSEventStreamAuthorPlugin.class);

		CPN2XSEventStreamParameters parameters = getStreamParameters();

		Object[] result = CPNModelToXSEventStreamAuthorPlugin.apply(context,
				inputCPNModel.getData(CPNModelIOObject.class).getArtifact(),
				parameters);

		outputAuthor.deliver(new XSAuthorIOObject<XSEvent>(
				(XSAuthor<XSEvent>) result[0], context));
		outputStream.deliver(
				new XSEventStreamIOObject((XSEventStream) result[1], context));

		logger.log(Level.INFO, "end do work Stream Generator");
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();
		parameterTypes = setupMaxStepsParameter(parameterTypes);
		parameterTypes = setupRepetitionsParameter(parameterTypes);
		parameterTypes = setupStepDelayParameter(parameterTypes);
		parameterTypes = setupCaseIdentificationParameter(parameterTypes);
		parameterTypes = setupAdditionalVariables(parameterTypes);
		parameterTypes = setupCommunicationType(parameterTypes);
		parameterTypes = setupIgnorePageParameter(parameterTypes);
		parameterTypes = setupIgnorePatternsParameter(parameterTypes);
		return parameterTypes;
	}

	private List<ParameterType> setupIgnorePatternsParameter(
			List<ParameterType> parameterTypes) {
		ParameterType ignorePatterns = new ParameterTypeString(
				PARAMETER_KEY_IGNORE_PATTERSNS, PARAMETER_LABEL_IGNORE_PATTERNS,
				true, false);
		parameterTypes.add(ignorePatterns);
		return parameterTypes;
	}

	private List<ParameterType> setupIgnorePageParameter(
			List<ParameterType> parameterTypes) {
		ParameterType ignorePageBool = new ParameterTypeBoolean(
				PARAMETER_KEY_IGNORE_PAGE, PARAMETER_LABEL_IGNORE_PAGE, true,
				false);
		ignorePageBool.setOptional(false);
		parameterTypes.add(ignorePageBool);
		return parameterTypes;
	}

	private CPN2XSEventStreamParameters getStreamParameters()
			throws UndefinedParameterError {
		CPN2XSEventStreamParameters streamParams = new CPN2XSEventStreamParameters();
		streamParams.setMaximumNumberOfStepsPerRepetition(
				getParameterAsInt(PARAMETER_KEY_MAX_STEPS));
		streamParams.setTotalNumberOfRepetitions(
				getParameterAsInt(PARAMETER_KEY_REPETITIONS));
		streamParams.setTransitionDelayMs(
				getParameterAsInt(PARAMETER_KEY_STEP_DELAY));
		streamParams = determineCaseIdentification(streamParams);
		streamParams.setIncludeVariables(
				getParameterAsBoolean(PARAMETER_KEY_INCLUDE_ADDITIONAL_DATA));
		streamParams = determineCommunicationType(streamParams);
		streamParams.setIgnorePage(
				getParameterAsBoolean(PARAMETER_KEY_IGNORE_PAGE));
		String[] ignorePatterns = getParameterAsString(
				PARAMETER_KEY_IGNORE_PATTERSNS) == null ? new String[0]
						: getParameterAsString(PARAMETER_KEY_IGNORE_PATTERSNS)
								.split(",");
		streamParams.setIgnorePatterns(ignorePatterns);
		return streamParams;
	}

	private List<ParameterType> setupAdditionalVariables(
			List<ParameterType> parameterTypes) {
		ParameterTypeBoolean includeVariablesParam = new ParameterTypeBoolean(
				PARAMETER_KEY_INCLUDE_ADDITIONAL_DATA,
				PARAMETER_LABEL_INCLUDE_ADDITIONAL_DATA, false);
		includeVariablesParam.setOptional(false);
		includeVariablesParam.setExpert(false);
		parameterTypes.add(includeVariablesParam);
		return parameterTypes;
	}

	private List<ParameterType> setupCaseIdentificationParameter(
			List<ParameterType> parameterTypes) {
		ParameterTypeCategory caseIdentificationCat = new ParameterTypeCategory(
				PARAMETER_KEY_CASE_IDENTIFICATION,
				PARAMETER_LABEL_CASE_IDENTIFICATION,
				PARAMETER_OPTIONS_CASE_IDENTIFICATION, 0, false);

		parameterTypes.add(caseIdentificationCat);

		ParameterTypeString caseIdentificationVariable = new ParameterTypeString(
				PARAMETER_KEY_CASE_IDENTIFICATION_VARIABLE,
				PARAMETER_LABEL_CASE_IDENTIFICATION_VARIABLE,
				new String(
						PARAMETER_DEFAULT_VALUE_CASE_IDENTIFICATION_VARIABLE),
				false);
		caseIdentificationVariable.setOptional(true);
		caseIdentificationVariable
				.registerDependencyCondition(new EqualStringCondition(this,
						PARAMETER_KEY_CASE_IDENTIFICATION, true, new String[] {
								PARAMETER_OPTION_CASE_IDENTIFICATION_VARIABELE }));
		parameterTypes.add(caseIdentificationVariable);
		return parameterTypes;
	}

	private List<ParameterType> setupCommunicationType(
			List<ParameterType> parameterTypes) {
		ParameterTypeCategory communicationTypeParam = new ParameterTypeCategory(
				PARAMETER_KEY_COMMUNICATION_TYPE,
				PARAMETER_LABEL_COMMUNIATION_TYPE,
				PARAMETER_OPTIONS_COMMUNICATION_TYPE, 0);
		communicationTypeParam.setOptional(false);
		communicationTypeParam.setExpert(false);
		parameterTypes.add(communicationTypeParam);
		return parameterTypes;
	}

	private List<ParameterType> setupMaxStepsParameter(
			List<ParameterType> parameterTypes) {
		ParameterTypeInt maxSteps = new ParameterTypeInt(
				PARAMETER_KEY_MAX_STEPS, PARAMETER_LABEL_MAX_STEPS, -1,
				Integer.MAX_VALUE, -1, false);
		maxSteps.setOptional(false);
		parameterTypes.add(maxSteps);
		return parameterTypes;
	}

	private List<ParameterType> setupRepetitionsParameter(
			List<ParameterType> parameterTypes) {
		ParameterTypeInt repetitions = new ParameterTypeInt(
				PARAMETER_KEY_REPETITIONS, PARAMETER_LABEL_REPETITIONS, 1,
				Integer.MAX_VALUE, 1, false);
		repetitions.setOptional(false);
		parameterTypes.add(repetitions);
		return parameterTypes;
	}

	private List<ParameterType> setupStepDelayParameter(
			List<ParameterType> parameterTypes) {
		ParameterTypeInt stepDelay = new ParameterTypeInt(
				PARAMETER_KEY_STEP_DELAY, PARAMETER_LABEL_STEP_DELAY, 0,
				Integer.MAX_VALUE, 0, false);
		stepDelay.setOptional(false);
		parameterTypes.add(stepDelay);
		return parameterTypes;
	}

}
