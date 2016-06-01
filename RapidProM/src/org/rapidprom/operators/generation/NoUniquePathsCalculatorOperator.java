package org.rapidprom.operators.generation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.models.NoUniqueTracesModel;
import org.processmining.ptandloggenerator.plugins.CalculateNoUniqueTraces;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.experimental.NewickTreeIOObject;

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
import com.rapidminer.tools.LogService;

public class NoUniquePathsCalculatorOperator extends Operator {

	public static final String PARAMETER_1_KEY = "Max number of iterations of a loop",
			PARAMETER_1_DESCR = "This parameter limits the number of iterations that are "
					+ "considered for a loop. This is done to prevent state space explosion "
					+ "when calculation the number of unique paths.";

	private InputPort input = getInputPorts().createPort(
			"newick tree (NewickTreeIOObject)", NewickTreeIOObject.class);
	private OutputPort output = getOutputPorts()
			.createPort("exampleset (ExampleSet)");

	public NoUniquePathsCalculatorOperator(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(output, ExampleSet.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: calculating number or unique paths on a newick tree");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		NewickTreeIOObject tree = input.getData(NewickTreeIOObject.class);

		CalculateNoUniqueTraces calculator = new CalculateNoUniqueTraces();
		NoUniqueTracesModel result = calculator.NoUniqueTracesModel_SingleTree(
				pluginContext, tree.getArtifact(),
				getParameterAsInt(PARAMETER_1_KEY));

		Object[][] outputData = new Object[1][1];
		outputData[0][0] = result.getNoUniqueTraces();
		ExampleSet es = ExampleSetFactory.createExampleSet(outputData);

		output.deliver(es);

		logger.log(Level.INFO,
				"End: calculating number or unique paths on a newick tree ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeInt parameter1 = new ParameterTypeInt(PARAMETER_1_KEY,
				PARAMETER_1_DESCR, 0, 100, 1);
		parameterTypes.add(parameter1);

		return parameterTypes;
	}
}
