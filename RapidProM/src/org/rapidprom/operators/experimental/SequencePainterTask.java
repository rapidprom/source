package org.rapidprom.operators.experimental;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.videolectureanalysis.parameters.SequencePainterParameters;
import org.processmining.videolectureanalysis.plugins.SequenceFrequencyMiner_WithDeviations;
import org.processmining.videolectureanalysis.plugins.SequencePerformanceMiner_DF;
import org.processmining.videolectureanalysis.plugins.SequencePerformanceMiner_WithDeviations;
import org.processmining.videolectureanalysis.plugins.abstr.AbstractSequenceMiner;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.DotPanelIOObject;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.LogService;

public class SequencePainterTask extends Operator {

	private InputPort inputLog = getInputPorts()
			.createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort output = getOutputPorts().createPort("model (DotPanel)");

	public static final String PARAMETER_1 = "Variation (Directly Follows)",
			PARAMETER_2 = "Characters removed",
			PARAMETER_3 = "Activities per column",
			PARAMETER_4 = "Max deviations", PARAMETER_5 = "Frequency threshold",
			PARAMETER_6 = "Start date (yyyy-mm-dd)";

	public static final String OPTION_1 = "Frequencies + Deviations",
			OPTION_2 = "Performance", OPTION_3 = "Performance + Deviations";

	public SequencePainterTask(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(output, DotPanelIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: sequence miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();
		XLog log = FitnessCalculatorOperator.cloneXLog(inputLog.getData(XLogIOObject.class).getArtifact());

		AbstractSequenceMiner miner = null;
		switch (getParameterAsString(PARAMETER_1)) {
		case OPTION_1:
			miner = new SequenceFrequencyMiner_WithDeviations();
			break;
		case OPTION_2:
			miner = new SequencePerformanceMiner_DF();
			break;
		case OPTION_3:
			miner = new SequencePerformanceMiner_WithDeviations();
			break;
		}

		DotPanel result = miner.run(log, getParameterObject());
		output.deliver(new DotPanelIOObject(result, pluginContext));

		logger.log(Level.INFO, "End: sequence miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeCategory parameter1 = new ParameterTypeCategory(
				PARAMETER_1, PARAMETER_1,
				new String[] { OPTION_1, OPTION_2, OPTION_3 }, 0);
		parameterTypes.add(parameter1);

		ParameterTypeInt parameter2 = new ParameterTypeInt(PARAMETER_2,
				PARAMETER_2, 0, 100, 8);
		parameterTypes.add(parameter2);

		ParameterTypeInt parameter3 = new ParameterTypeInt(PARAMETER_3,
				PARAMETER_3, 0, 100, 10);
		parameterTypes.add(parameter3);

		ParameterTypeInt parameter4 = new ParameterTypeInt(PARAMETER_4,
				PARAMETER_4, 0, 100, 10);
		parameterTypes.add(parameter4);

		ParameterTypeDouble parameter5 = new ParameterTypeDouble(PARAMETER_5,
				PARAMETER_5, 0, 1, 0.05);
		parameterTypes.add(parameter5);

		ParameterTypeString parameter6 = new ParameterTypeString(PARAMETER_6,
				PARAMETER_6, "2015-08-31");
		parameterTypes.add(parameter6);

		return parameterTypes;
	}

	private SequencePainterParameters getParameterObject()
			throws OperatorException {

		switch (getParameterAsString(PARAMETER_1)) {
		case OPTION_2:
			return new SequencePainterParameters(getParameterAsInt(PARAMETER_2),
					getParameterAsInt(PARAMETER_3), 0,
					getParameterAsDouble(PARAMETER_5),
					getParameterAsString(PARAMETER_6));
		default:
			return new SequencePainterParameters(getParameterAsInt(PARAMETER_2),
					getParameterAsInt(PARAMETER_3),
					getParameterAsInt(PARAMETER_4),
					getParameterAsDouble(PARAMETER_5),
					getParameterAsString(PARAMETER_6));
		}

	}

}
