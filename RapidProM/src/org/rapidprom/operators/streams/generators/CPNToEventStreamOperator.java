package org.rapidprom.operators.streams.generators;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.eventstream.authors.cpn.parameters.CPN2XSStreamCaseIdentification;
import org.processmining.eventstream.authors.cpn.parameters.CPN2XSStreamParameters;
import org.processmining.eventstream.authors.cpn.plugins.CPNModelToXSEventStreamAuthorPlugin;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.enums.CommunicationType;
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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualStringCondition;
import com.rapidminer.tools.LogService;

public class CPNToEventStreamOperator extends Operator {

	/**
	 * Parameter definitions; For categories (single selection lists) we can not
	 * use enums as the operator can only select the index of the selected
	 * string.
	 */
	private static final String PARAMETER_KEY_MAX_STEPS = "max_steps";
	private static final String PARAMETER_LABEL_MAX_STEPS = "Max steps (-1 for no limit) within one simulation of the CPN model.";

	private static final String PARAMETER_KEY_REPETITIONS = "repetitions";
	private static final String PARAMETER_LABEL_REPETITIONS = "Number of simulations.";

	private static final String PARAMETER_KEY_STEP_DELAY = "step_delay";
	private static final String PARAMETER_LABEL_STEP_DELAY = "Step delay (ms) inbetween two consecutive emissions.";

	/**
	 * TODO: bind these to CPN2XSStreamCaseIdentification, when these classes
	 * allow for "toString()" methods.
	 */
	private static final String PARAMETER_KEY_CASE_IDENTIFICATION = "case_identification";
	private static final String PARAMETER_LABEL_CASE_IDENTIFICATION = "Case identification, specifying what how to identify cases within the stream.";
	private static final String PARAMETER_OPTION_CASE_IDENTIFICATION_REPETITION = "by repetition";
	private static final String PARAMETER_OPTION_CASE_IDENTIFICATION_VARIABELE = "by CPN variable";
	private static final String[] PARAMETER_OPTIONS_CASE_IDENTIFICATION = new String[] {
			PARAMETER_OPTION_CASE_IDENTIFICATION_REPETITION,
			PARAMETER_OPTION_CASE_IDENTIFICATION_VARIABELE };

	private static final String PARAMETER_KEY_CASE_IDENTIFICATION_VARIABLE = "cpn_variable";
	private static final String PARAMETER_LABEL_CASE_IDENTIFICATION_VARIABLE = "Case identification by CPN variable, denotes what CPN variable to track.";
	private static final String PARAMETER_DEFAULT_VALUE_CASE_IDENTIFICATION_VARIABLE = CPN2XSStreamCaseIdentification.CPN_VARIABLE_DEFAULT_VALUE;

	private static final String PARAMETER_KEY_INCLUDE_ADDITIONAL_DATA = "include_other_variables";
	private static final String PARAMETER_LABEL_INCLUDE_ADDITIONAL_DATA = "If this parameter is set to true, the events will include other variable values as well";

	// TODO: bind to toString() methods of correspdoning enums in (event)stream
	// package.
	private static final String PARAMETER_KEY_COMMUNICATION_TYPE = "communication_type";
	private static final String PARAMETER_LABEL_COMMUNIATION_TYPE = "The communicationtype of the selected stream, in synchronous mode, events will only be emitted if the receiving end is ready to accept new packages.";
	private static final String PARAMETER_OPTION_COMMUNICATION_TYPE_ASYNC = "asynchronous";
	private static final String PARAMETER_OPTION_COMMUNICATION_TYPE_SYNC = "synchronous";
	private static final String[] PARAMETER_OPTIONS_COMMUNICATION_TYPE = new String[] {
			PARAMETER_OPTION_COMMUNICATION_TYPE_ASYNC,
			PARAMETER_OPTION_COMMUNICATION_TYPE_SYNC };

	private InputPort inputCPNModel = getInputPorts()
			.createPort("model (CPN model)", CPNModelIOObject.class);

	private OutputPort outputPublisher = getOutputPorts()
			.createPort("publisher)");
	private OutputPort outputStream = getOutputPorts()
			.createPort("event stream");

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

		CPN2XSStreamParameters parameters = getStreamParameters();

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

	private CPN2XSStreamParameters getStreamParameters()
			throws UndefinedParameterError {
		CPN2XSStreamParameters streamParams = new CPN2XSStreamParameters();
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
		return streamParams;
	}

	private CPN2XSStreamParameters determineCaseIdentification(
			CPN2XSStreamParameters params) throws UndefinedParameterError {
		String caseIdentificationType = PARAMETER_OPTIONS_CASE_IDENTIFICATION[getParameterAsInt(
				PARAMETER_KEY_CASE_IDENTIFICATION)];
		if (caseIdentificationType
				.equals(PARAMETER_OPTION_CASE_IDENTIFICATION_REPETITION)) {
			params.setCaseIdentificationType(
					CPN2XSStreamCaseIdentification.REPITITION);
		} else if (caseIdentificationType
				.equals(PARAMETER_OPTION_CASE_IDENTIFICATION_VARIABELE)) {
			params.setCaseIdentificationType(
					CPN2XSStreamCaseIdentification.CPN_VARIABLE);
			params.setCaseIdentifier(getParameterAsString(
					PARAMETER_KEY_CASE_IDENTIFICATION_VARIABLE));
		}
		return params;
	}

	private CPN2XSStreamParameters determineCommunicationType(
			CPN2XSStreamParameters params) throws UndefinedParameterError {
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

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();
		parameterTypes = setupMaxStepsParameter(parameterTypes);
		parameterTypes = setupRepetitionsParameter(parameterTypes);
		parameterTypes = setupStepDelayParameter(parameterTypes);
		parameterTypes = setupCaseIdentificationParameter(parameterTypes);
		parameterTypes = setupAdditionalVariables(parameterTypes);
		parameterTypes = setupCommunicationType(parameterTypes);
		return parameterTypes;
	}

	private List<ParameterType> setupMaxStepsParameter(
			List<ParameterType> parameterTypes) {
		ParameterTypeInt maxSteps = new ParameterTypeInt(
				PARAMETER_KEY_MAX_STEPS, PARAMETER_LABEL_MAX_STEPS, -1,
				Integer.MAX_VALUE, -1, true);
		maxSteps.setOptional(false);
		parameterTypes.add(maxSteps);
		return parameterTypes;
	}

	private List<ParameterType> setupRepetitionsParameter(
			List<ParameterType> parameterTypes) {
		ParameterTypeInt repetitions = new ParameterTypeInt(
				PARAMETER_KEY_REPETITIONS, PARAMETER_LABEL_REPETITIONS, 1,
				Integer.MAX_VALUE, 1, true);
		repetitions.setOptional(false);
		parameterTypes.add(repetitions);
		return parameterTypes;
	}

	private List<ParameterType> setupStepDelayParameter(
			List<ParameterType> parameterTypes) {
		ParameterTypeInt stepDelay = new ParameterTypeInt(
				PARAMETER_KEY_STEP_DELAY, PARAMETER_LABEL_STEP_DELAY, 0,
				Integer.MAX_VALUE, 0, true);
		stepDelay.setOptional(false);
		parameterTypes.add(stepDelay);
		return parameterTypes;
	}

	private List<ParameterType> setupCaseIdentificationParameter(
			List<ParameterType> parameterTypes) {
		ParameterTypeCategory caseIdentificationCat = new ParameterTypeCategory(
				PARAMETER_KEY_CASE_IDENTIFICATION,
				PARAMETER_LABEL_CASE_IDENTIFICATION,
				PARAMETER_OPTIONS_CASE_IDENTIFICATION, 0, true);

		parameterTypes.add(caseIdentificationCat);

		ParameterTypeString caseIdentificationVariable = new ParameterTypeString(
				PARAMETER_KEY_CASE_IDENTIFICATION_VARIABLE,
				PARAMETER_LABEL_CASE_IDENTIFICATION_VARIABLE,
				new String(
						PARAMETER_DEFAULT_VALUE_CASE_IDENTIFICATION_VARIABLE),
				true);
		caseIdentificationVariable.setOptional(true);
		caseIdentificationVariable
				.registerDependencyCondition(new EqualStringCondition(this,
						PARAMETER_KEY_CASE_IDENTIFICATION, true, new String[] {
								PARAMETER_OPTION_CASE_IDENTIFICATION_VARIABELE }));
		parameterTypes.add(caseIdentificationVariable);
		return parameterTypes;
	}

	private List<ParameterType> setupAdditionalVariables(
			List<ParameterType> parameterTypes) {
		ParameterTypeBoolean includeVariablesParam = new ParameterTypeBoolean(
				PARAMETER_KEY_INCLUDE_ADDITIONAL_DATA,
				PARAMETER_LABEL_INCLUDE_ADDITIONAL_DATA, false);
		includeVariablesParam.setOptional(false);
		includeVariablesParam.setExpert(true);
		parameterTypes.add(includeVariablesParam);
		return parameterTypes;
	}

	private List<ParameterType> setupCommunicationType(
			List<ParameterType> parameterTypes) {
		ParameterTypeCategory communicationTypeParam = new ParameterTypeCategory(
				PARAMETER_KEY_COMMUNICATION_TYPE,
				PARAMETER_LABEL_COMMUNIATION_TYPE,
				PARAMETER_OPTIONS_COMMUNICATION_TYPE, 0);
		communicationTypeParam.setOptional(false);
		communicationTypeParam.setExpert(true);
		parameterTypes.add(communicationTypeParam);
		return parameterTypes;
	}

}
