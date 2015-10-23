package org.rapidprom.operator.analysis;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.plugins.fuzzymodel.adapter.FuzzyAdapterPlugin;
import org.processmining.plugins.fuzzymodel.anim.FuzzyAnimation;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.FuzzyAnimationIOObject;
import org.rapidprom.ioobjects.MetricsRepositoryIOObject;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;

public class AnimateLogInFuzzyModelAnalysisOperator extends Operator {

	private static final String PARAMETER_1 = "Lookahead",
			PARAMETER_2 = "Extra lookahead";

	private InputPort inputMetricsRepository = getInputPorts().createPort(
			"model (MetricsRepository)", MetricsRepositoryIOObject.class);
	private InputPort inputXLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputFuzzyAnimation = getOutputPorts().createPort(
			"model (FuzzyAnimation)");

	public AnimateLogInFuzzyModelAnalysisOperator(
			OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputFuzzyAnimation,
						FuzzyAnimationIOObject.class));
	}

	@SuppressWarnings("deprecation")
	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: animate event log in fuzzy model");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(FuzzyAdapterPlugin.class);
		MetricsRepositoryIOObject metricsRepository = inputMetricsRepository
				.getData(MetricsRepositoryIOObject.class);
		XLogIOObject xLog = inputXLog.getData(XLogIOObject.class);

		FuzzyAdapterPlugin adapter = new FuzzyAdapterPlugin();
		MutableFuzzyGraph fuzzyInstance = adapter.mineGeneric(pluginContext,
				metricsRepository.getArtifact());

		FuzzyAnimation animation = new FuzzyAnimation(pluginContext,
				fuzzyInstance, xLog.getArtifact(),
				getParameterAsInt(PARAMETER_1), getParameterAsInt(PARAMETER_2));
		animation.initialize(pluginContext, fuzzyInstance, xLog.getArtifact());

		outputFuzzyAnimation.deliver(new FuzzyAnimationIOObject(animation,
				pluginContext));
		logger.log(Level.INFO, "End: animate event log in fuzzy model ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeInt parameterType2 = new ParameterTypeInt(PARAMETER_1,
				PARAMETER_1, 1, 25, 5, false);
		parameterTypes.add(parameterType2);

		ParameterTypeInt parameterType3 = new ParameterTypeInt(PARAMETER_2,
				PARAMETER_2, 0, 15, 3, false);
		parameterTypes.add(parameterType3);

		return parameterTypes;
	}

}
