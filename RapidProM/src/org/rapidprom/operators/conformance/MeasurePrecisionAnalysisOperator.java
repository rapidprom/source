package org.rapidprom.operators.conformance;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PNRepResultIOObject;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.LogService;

import javassist.tools.rmi.ObjectNotFoundException;

public class MeasurePrecisionAnalysisOperator extends Operator {

	private static final String PARAMETER_1 = "Consider traces with the same activity sequence as the same trace";
	private InputPort input = getInputPorts().createPort(
			"alignments (ProM PNRepResult)", PNRepResultIOObject.class);

	private OutputPort outputMetrics = getOutputPorts()
			.createPort("example set (Data Table)");

	public MeasurePrecisionAnalysisOperator(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: measure precision/generalization based on alignments");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		PNRepResultIOObject alignment = input
				.getData(PNRepResultIOObject.class);

		ExampleSet es;

		if (alignment.getArtifact() != null) {
			AlignmentPrecGen aligner = new AlignmentPrecGen();
			AlignmentPrecGenRes result = null;
			try {
				result = aligner.measureConformanceAssumingCorrectAlignment(
						pluginContext, alignment.getMapping(),
						alignment.getArtifact(),
						alignment.getPn().getArtifact(),
						alignment.getPn().getInitialMarking(),
						getParameterAsBoolean(PARAMETER_1));

				es = ExampleSetFactory.createExampleSet(new Object[][] {
						{ "precision", result.getPrecision() },
						{ "generalization", result.getGeneralization() } });
				outputMetrics.deliver(es);
				
			} catch (ObjectNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			es = ExampleSetFactory.createExampleSet(new Object[][] {
					{ "precision", "?" }, { "generalization", "?" } });
			outputMetrics.deliver(es);
		}

		logger.log(Level.INFO,
				"End: measure precision/generalization based on alignments ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(
				PARAMETER_1, PARAMETER_1, true);
		parameterTypes.add(parameterType1);

		return parameterTypes;
	}
}
