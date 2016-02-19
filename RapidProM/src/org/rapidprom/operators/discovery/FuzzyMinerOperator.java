package org.rapidprom.operators.discovery;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.parameter.*;

import org.processmining.framework.plugin.PluginContext;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.NRootAttenuation;
import org.processmining.plugins.fuzzymodel.miner.FuzzyMinerPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.MetricsRepositoryIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

public class FuzzyMinerOperator extends AbstractRapidProMDiscoveryOperator {

	private static final String PARAMETER_1_KEY = "Frequency significance metric (Unitary)",
			PARAMETER_1_DESCR = "Unary significance describes the relative importance of an event class, "
					+ "which will be represented as a node in the process model. "
					+ "As our approach is based on removing less significant behavior, "
					+ "and as removing a node implies removing all of its connected arcs, "
					+ "unary significance is the primary driver of simplification. "
					+ "For this metric: the more often acertain event class was observed in the log, "
					+ "the more significant it is",

			PARAMETER_2_KEY = "Routing significance metric (Unitary)",
			PARAMETER_2_DESCR = "The idea behind routing significance is that points, at which the "
					+ "process either forks (i.e., split nodes) or synchronizes (i.e., join nodes), "
					+ "are interesting in that they substantially define the structure of a process. "
					+ "These points in the process are routing nodes. They are characterized by the fact "
					+ "that they have much fewer ingoing arcs than outgoing arcs, or the other way around. "
					+ "The more unbalanced the number of ingoing and outgoing arcs of a node, the greater "
					+ "its significance for routing. Therefore, the higher the number and significance of "
					+ "predecessors for a node (i.e., its incoming arcs) differs from the number and "
					+ "significance of its successors (i.e., outgoing arcs), the more important that "
					+ "node is for routing in the process. Routing significance is important as amplifier "
					+ "metric, i.e. it helps separating important routing nodes (whose significance it "
					+ "increases) from those less important",

			PARAMETER_3_KEY = "Frequency significance (Binary)",
			PARAMETER_3_DESCR = "Binary significance describes the relative importance of a precedence "
					+ "relation between two event classes, i.e. an edge in the process model. Its purpose "
					+ "is to amplify and to isolate the observed behavior that is supposed to be of the "
					+ "greatest interest. In our simplification approach, it primarily influences the "
					+ "selection of edges that will be included in the simplified process model. Like "
					+ "for unary significance, the log-based frequency significance metric is also the "
					+ "most important implementation for binary significance. The more often two event "
					+ "classes are observed after one another, the more significant their precedence "
					+ "relation",

			PARAMETER_4_KEY = "Distance significance (Binary)",
			PARAMETER_4_DESCR = "The distance significance metric is a derivative implementation of binary "
					+ "significance. The more the significance of a relation differs from its source and "
					+ "target nodes’ significances, the less its distance significance value. The "
					+ "rationale behind this metric is that globally important relations are also "
					+ "always the most important relations for their endpoints. Distance significance "
					+ "locally amplifies crucial key relations between event classes, and weakens "
					+ "already insignificant relations. Thereby, it can clarify ambiguous situations "
					+ "in edge abstraction, where many relations “compete” over being included in the "
					+ "simplified process model. Especially in very unstructured execution logs, this"
					+ " metric is an indispensible tool for isolating behavior of interest.",

			PARAMETER_5_KEY = "Proximity correlation (Binary)",
			PARAMETER_5_DESCR = "Binary correlation measures the distance of events in a precedence "
					+ "relation, i.e. how closely related two events following one another are. "
					+ "Distance, in the process domain, can be equated to the magnitude of context "
					+ "change between two activity executions. Subsequently occurring activities "
					+ "that have a more similar context (e.g., that are executed by the same person "
					+ "or in a short timeframe) are thus evaluated to be higher correlated. Binary "
					+ "correlation is the main driver of the decision between aggregation or abstraction "
					+ "of less-significant behavior. Proximity correlation evaluates event classes that "
					+ "occur shortly after one another, i.e. within a small timeframe, as highly "
					+ "correlated. This is important for identifying clusters of events that correspond "
					+ "to one logical activity, as these are commonly executed within a short timeframe.",

			PARAMETER_6_KEY = "Endpoint correlation (Binary)",
			PARAMETER_6_DESCR = "Endpoint correlation is quite similar, however, instead of resources it "
					+ "compares the activity names of subsequent events. More similar names will be "
					+ "interpreted as higher correlation. This is important for low-level logs including "
					+ "a large amount of less significant events that are closely related. Most of "
					+ "the time, events that reflect similar tasks also are given similar names "
					+ "(e.g., “open valve13” and “close valve13”), and this metric can unveil these "
					+ "implicit dependencies.",

			PARAMETER_7_KEY = "Originator correlation (Binary)",
			PARAMETER_7_DESCR = "One feature of clusters of events occurring within the realm of one "
					+ "higher-level activity is that they are executed by the same person. Originator "
					+ "correlation between event classes is determined from the names of the persons "
					+ "that have triggered two subsequent events. The more similar these names, the "
					+ "higher correlated the respective event classes. In real applications, user names "
					+ "often include job titles or function identifiers (e.g.“sales John” and “sales "
					+ "Paul”). Therefore, this metric implementation is a valuable tool also for "
					+ "unveiling implicit correlation between events. ",

			PARAMETER_8_KEY = "Data type correlation (Binary)",
			PARAMETER_8_DESCR = "In most logs, events also include additional attributes, containing "
					+ "snapshots from the data perspective of the process (e.g., the value of an "
					+ "insurance claim). In such cases, the selection of attributes logged for each "
					+ "event can be interpreted as its context. Thus, the data type correlation metric "
					+ "evaluates event classes, where subsequent events share a large amount of data "
					+ "types (i.e., attribute keys), as highly correlated. ",

			PARAMETER_9_KEY = "Data value correlation (Binary)",
			PARAMETER_9_DESCR = "Data value correlation is more specific, in that it also takes the "
					+ "values of these common attributes into account. In that, it uses relative "
					+ "similarity, i.e. small changes of an attribute value will compromise correlation "
					+ "less than a completely different value.",

			PARAMETER_10_KEY = "Maximum Distance",
			PARAMETER_10_DESCR = "Defines the maximum length of long-term relations";

	private OutputPort outputMetricsRepository = getOutputPorts()
			.createPort("model (ProM MetricsRepository)");

	public FuzzyMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(outputMetricsRepository,
				MetricsRepositoryIOObject.class));
	}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: fuzzy miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(FuzzyMinerPlugin.class);

		MetricsRepository metricsRepository = getMetricsConfiguration();
		Attenuation attenuation = new NRootAttenuation(2.7, 5);
		int maxDistance = getParameterAsInt(PARAMETER_10_KEY);

		FuzzyMinerPlugin executer = new FuzzyMinerPlugin();
		MetricsRepositoryIOObject metricsRepositoryIOObject = new MetricsRepositoryIOObject(
				executer.mineGeneric(pluginContext, getXLog(),
						metricsRepository, attenuation, maxDistance),
				pluginContext);

		outputMetricsRepository.deliver(metricsRepositoryIOObject);
		logger.log(Level.INFO, "End: fuzzy miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeDouble parameter1 = new ParameterTypeDouble(
				PARAMETER_1_KEY, PARAMETER_1_DESCR, 0, 1, 1);
		parameterTypes.add(parameter1);

		ParameterTypeDouble parameter2 = new ParameterTypeDouble(
				PARAMETER_2_KEY, PARAMETER_2_DESCR, 0, 1, 1);
		parameterTypes.add(parameter2);

		ParameterTypeDouble parameter3 = new ParameterTypeDouble(
				PARAMETER_3_KEY, PARAMETER_3_DESCR, 0, 1, 1);
		parameterTypes.add(parameter3);

		ParameterTypeDouble parameter4 = new ParameterTypeDouble(
				PARAMETER_4_KEY, PARAMETER_4_DESCR, 0, 1, 1);
		parameterTypes.add(parameter4);

		ParameterTypeDouble parameter5 = new ParameterTypeDouble(
				PARAMETER_5_KEY, PARAMETER_5_DESCR, 0, 1, 1);
		parameterTypes.add(parameter5);

		ParameterTypeDouble parameter7 = new ParameterTypeDouble(
				PARAMETER_7_KEY, PARAMETER_7_DESCR, 0, 1, 1);
		parameterTypes.add(parameter7);

		ParameterTypeDouble parameter6 = new ParameterTypeDouble(
				PARAMETER_6_KEY, PARAMETER_6_DESCR, 0, 1, 1);
		parameterTypes.add(parameter6);

		ParameterTypeDouble parameter8 = new ParameterTypeDouble(
				PARAMETER_8_KEY, PARAMETER_8_DESCR, 0, 1, 1);
		parameterTypes.add(parameter8);

		ParameterTypeDouble parameter9 = new ParameterTypeDouble(
				PARAMETER_9_KEY, PARAMETER_9_DESCR, 0, 1, 1);
		parameterTypes.add(parameter9);

		ParameterTypeInt parameter10 = new ParameterTypeInt(PARAMETER_10_KEY,
				PARAMETER_10_DESCR, 0, 100, 1);
		parameterTypes.add(parameter10);

		return parameterTypes;
	}

	private MetricsRepository getMetricsConfiguration() {

		XLogInfo logInfo = null;
		try {
			logInfo = XLogInfoFactory.createLogInfo(getXLog(),
					getXEventClassifier());
		} catch (UserError e) {
			e.printStackTrace();
		}
		MetricsRepository metrics = MetricsRepository.createRepository(logInfo);
		try {
			metrics.getUnaryLogMetrics().get(0).setNormalizationMaximum(
					getParameterAsDouble(PARAMETER_1_KEY));
			metrics.getUnaryDerivateMetrics().get(0).setNormalizationMaximum(
					getParameterAsDouble(PARAMETER_2_KEY));
			metrics.getSignificanceBinaryLogMetrics().get(0)
					.setNormalizationMaximum(
							getParameterAsDouble(PARAMETER_3_KEY));
			metrics.getCorrelationBinaryLogMetrics().get(0)
					.setNormalizationMaximum(
							getParameterAsDouble(PARAMETER_5_KEY));
			metrics.getCorrelationBinaryLogMetrics().get(1)
					.setNormalizationMaximum(
							getParameterAsDouble(PARAMETER_6_KEY));
			metrics.getCorrelationBinaryLogMetrics().get(2)
					.setNormalizationMaximum(
							getParameterAsDouble(PARAMETER_7_KEY));
			metrics.getCorrelationBinaryLogMetrics().get(3)
					.setNormalizationMaximum(
							getParameterAsDouble(PARAMETER_8_KEY));
			metrics.getCorrelationBinaryLogMetrics().get(4)
					.setNormalizationMaximum(
							getParameterAsDouble(PARAMETER_9_KEY));
			metrics.getSignificanceBinaryMetrics().get(1)
					.setNormalizationMaximum(
							getParameterAsDouble(PARAMETER_4_KEY));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metrics;
	}
}
