package org.rapidprom.operators.generation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.algorithms.LogFactory;
import org.processmining.ptandloggenerator.algorithms.LogGenerator;
import org.processmining.ptandloggenerator.parameters.LogParameters;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.ioobjects.experimental.NewickTreeIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;

public class GenerateEventLogOperator extends Operator {

	public static final String PARAMETER_1_KEY = "number of traces",
			PARAMETER_1_DESCR = "the number of traces that will be generated in the resulting "
					+ "event log. trace uniqueness  is not guaranteed.";

	private InputPort input = getInputPorts().createPort("newick tree",
			NewickTreeIOObject.class);

	private OutputPort output = getOutputPorts().createPort("event log");
	
	public static LogFactory factory = null;

	public GenerateEventLogOperator(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(output, XLogIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: generating event log from newick tree");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();
		LogParameters parameters = new LogParameters(
				getParameterAsInt(PARAMETER_1_KEY));
		if(factory == null)
			factory = new LogFactory();
		LogGenerator generator = new LogGenerator(
				input.getData(NewickTreeIOObject.class).getArtifact(),
				parameters, factory);

		output.deliver(new XLogIOObject(
				generator.getResultingTraces().getXLog(), pluginContext));

		logger.log(Level.INFO, "End: generating event log from newick tree ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeInt parameter1 = new ParameterTypeInt(PARAMETER_1_KEY,
				PARAMETER_1_DESCR, 1, Integer.MAX_VALUE, 100);
		parameterTypes.add(parameter1);

		return parameterTypes;
	}

}
