package org.rapidprom.operators.streams.generators;

import java.util.List;

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
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;

public class CPNToEventStreamOperator extends Operator {

	private static String PARAMETER_KEY_MAX_STEPS = "max_steps";
	private static String PARAMETER_LABEL_MAX_STEPS = "Max steps (-1 for no limit)";

	private static String PARAMETER_KEY_REPETITIONS = "repetitions";
	private static String PARAMETER_LABEL_REPETITIONS = "Repetitions";

	private static String PARAMETER_KEY_STEP_DELAY = "step_delay";
	private static String PARAMETER_LABEL_STEP_DELAY = "Step delay (ms)";

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

		LogService logService = LogService.getGlobal();
		logService.log("start do work Stream Generator", LogService.NOTE);

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

		logService.log("end do work Stream Generator", LogService.NOTE);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();
		parameterTypes.add(setupMaxStepsParameter());
		parameterTypes.add(setupRepetitionsParameter());
		parameterTypes.add(setupStepDelayParameter());
		return parameterTypes;
	}

	private ParameterTypeInt setupMaxStepsParameter() {
		ParameterTypeInt maxSteps = new ParameterTypeInt(
				PARAMETER_KEY_MAX_STEPS, PARAMETER_LABEL_MAX_STEPS, -1,
				Integer.MAX_VALUE, -1, true);
		maxSteps.setOptional(false);
		return maxSteps;
	}

	private ParameterTypeInt setupRepetitionsParameter() {
		ParameterTypeInt repetitions = new ParameterTypeInt(
				PARAMETER_KEY_REPETITIONS, PARAMETER_LABEL_REPETITIONS, 1,
				Integer.MAX_VALUE, 1, true);
		repetitions.setOptional(false);
		return repetitions;
	}

	private ParameterTypeInt setupStepDelayParameter() {
		ParameterTypeInt stepDelay = new ParameterTypeInt(
				PARAMETER_KEY_STEP_DELAY, PARAMETER_LABEL_STEP_DELAY, 0,
				Integer.MAX_VALUE, 0, true);
		stepDelay.setOptional(false);
		return stepDelay;
	}

}
