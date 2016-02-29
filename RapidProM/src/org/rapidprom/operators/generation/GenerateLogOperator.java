package org.rapidprom.operators.generation;

import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.algorithms.LogGenerator;
import org.processmining.ptandloggenerator.parameters.LogParameters;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.NewickTreeIOObject;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;

public class GenerateLogOperator extends Operator {

	private static final String PARAMETER_1_KEY = "Number of Traces",
			PARAMETER_1_DESCR = "This parameter defines the number of traces that will be "
					+ "created for the log. The uniqueness of these traces is not guaranteed.";

	protected final InputPort inputNewickTreeCollection = getInputPorts()
			.createPort("newick tree", NewickTreeIOObject.class);
	protected final OutputPort outputPort = getOutputPorts()
			.createPort("event log");

	public GenerateLogOperator(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(outputPort, XLogIOObject.class));
	}

	public void doWork() throws OperatorException {

		PluginContext context = ProMPluginContextManager.instance()
				.getContext();
		NewickTreeIOObject wrapper = inputNewickTreeCollection
				.getData(NewickTreeIOObject.class);

		LogGenerator generator = new LogGenerator(wrapper.getArtifact(),
				getParameterValues());

		XLogIOObject result = new XLogIOObject(
				generator.getResultingTraces().getXLog(), context);

		outputPort.deliver(result);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeInt parameter1 = new ParameterTypeInt(PARAMETER_1_KEY,
				PARAMETER_1_DESCR, 0, Integer.MAX_VALUE, 100);
		parameterTypes.add(parameter1);

		return parameterTypes;
	}

	private LogParameters getParameterValues() {

		try {
			return new LogParameters(getParameterAsInt(PARAMETER_1_KEY));
		} catch (UndefinedParameterError e) {

			e.printStackTrace();
		}
		return null;
	}

}
