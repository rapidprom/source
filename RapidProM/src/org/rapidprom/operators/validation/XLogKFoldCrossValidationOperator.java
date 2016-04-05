package org.rapidprom.operators.validation;

import java.util.List;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.PassThroughRule;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;

public class XLogKFoldCrossValidationOperator extends OperatorChain {

	public static final String PARAMETER_ITERATION_MACRO = "iteration_macro";

	/**
	 * The parameter name for &quot;Number of subsets for the
	 * crossvalidation.&quot;
	 */
	public static final String PARAMETER_NUMBER_OF_VALIDATIONS = "number_of_validations";

	private int number;

	private final InputPort inputXLog = getInputPorts().createPort("event log",
			XLogIOObject.class);

	private final OutputPort trainingProcessSource = getSubprocess(0)
			.getInnerSources().createPort("training");
	private final InputPort trainingProcessSink = getSubprocess(0)
			.getInnerSinks().createPort("model");

	// training -> testing
	private final PortPairExtender throughExtender = new PortPairExtender(
			"through", getSubprocess(0).getInnerSinks(),
			getSubprocess(1).getInnerSources());

	// testing
	private final OutputPort testProcessModelSource = getSubprocess(1)
			.getInnerSources().createPort("model");
	private final OutputPort testProcessSource = getSubprocess(1)
			.getInnerSources().createPort("test");
	private final InputPort testProcessSink = getSubprocess(1)
			.getInnerSinks().createPort("final data");

	// output
	private final OutputPort output = getOutputPorts()
			.createPort("result data");

	public XLogKFoldCrossValidationOperator(OperatorDescription description) {
		super(description, "Training", "Test");

		throughExtender.start();

		getTransformer().addRule(new GenerateNewMDRule(
				trainingProcessSource, XLogIOObject.class));
		getTransformer().addRule(new GenerateNewMDRule(
				testProcessSource, XLogIOObject.class));

		getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
		getTransformer().addRule(new PassThroughRule(trainingProcessSink,
				testProcessModelSource, false));
		getTransformer().addRule(throughExtender.makePassThroughRule());
		getTransformer().addRule(new SubprocessTransformRule(getSubprocess(1)));
		getTransformer().addPassThroughRule(testProcessSink,
				output);
	}

	@Override
	public void doWork() throws OperatorException {

		number = getParameterAsInt(PARAMETER_NUMBER_OF_VALIDATIONS);

		XLog original = inputXLog.getData(XLogIOObject.class).getArtifact();
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		for (int i = 0; i < number; i++) {

			if (isParameterSet(PARAMETER_ITERATION_MACRO)) {
				getProcess().getMacroHandler().addMacro(
						getParameterAsString(PARAMETER_ITERATION_MACRO),
						Integer.toString(i));
			}

			XLog[] logs = getSubLogs(original, i, number);
			trainingProcessSource
					.deliver(new XLogIOObject(logs[0], pluginContext));
			getSubprocess(0).execute();

			testProcessSource
					.deliver(new XLogIOObject(logs[1], pluginContext));
			throughExtender.passDataThrough();

			testProcessModelSource
					.deliver(trainingProcessSink.getData(IOObject.class));
			getSubprocess(1).execute();

			inApplyLoop();
		}

		output
				.deliver(testProcessSink.getData(IOObject.class));
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(
				PARAMETER_NUMBER_OF_VALIDATIONS,
				"Number of folds for the crossvalidation.", 2,
				Integer.MAX_VALUE, 10, false);
		types.add(type);
		ParameterTypeString type2 = new ParameterTypeString(
				PARAMETER_ITERATION_MACRO, PARAMETER_ITERATION_MACRO,
				"iteration");
		types.add(type2);
		return types;
	}

	private XLog[] getSubLogs(XLog original, int index, int folds) {

		XFactory factory = new XFactoryNaiveImpl();

		XLog training = factory.createLog(original.getAttributes());
		training.getClassifiers().addAll(original.getClassifiers());
		XLog test = factory.createLog(original.getAttributes());
		test.getClassifiers().addAll(original.getClassifiers());

		for (int i = 0; i < original.size(); i++) {
			XTrace newTrace = factory
					.createTrace(original.get(i).getAttributes());
			newTrace.addAll(original.get(i));

			if (i % folds == index)
				test.add(newTrace);
			else
				training.add(newTrace);
		}
		return new XLog[] { training, test };
	}
}
