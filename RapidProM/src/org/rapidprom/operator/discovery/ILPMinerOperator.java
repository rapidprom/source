package org.rapidprom.operator.discovery;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitygraphcreator.parameters.ConvertCausalActivityMatrixToCausalActivityGraphParameters;
import org.processmining.causalactivitygraphcreator.plugins.ConvertCausalActivityMatrixToCausalActivityGraphPlugin;
import org.processmining.causalactivitymatrix.models.CausalActivityMatrix;
import org.processmining.causalactivitymatrixminer.miners.MatrixMiner;
import org.processmining.causalactivitymatrixminer.miners.MatrixMinerParameters;
import org.processmining.causalactivitymatrixminer.miners.impl.HAFMiniMatrixMiner;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.hybridilpminer.models.lp.configuration.factories.LPMinerConfigurationFactory;
import org.processmining.hybridilpminer.models.lp.configuration.interfaces.LPMinerConfiguration;
import org.processmining.hybridilpminer.models.lp.configuration.parameters.DiscoveryStrategy;
import org.processmining.hybridilpminer.models.lp.configuration.parameters.DiscoveryStrategyType;
import org.processmining.hybridilpminer.models.lp.configuration.parameters.LPConstraintType;
import org.processmining.hybridilpminer.models.lp.configuration.parameters.LPFilter;
import org.processmining.hybridilpminer.models.lp.configuration.parameters.LPFilterType;
import org.processmining.hybridilpminer.models.lp.configuration.parameters.LPObjectiveType;
import org.processmining.hybridilpminer.models.lp.configuration.parameters.LPVariableType;
import org.processmining.hybridilpminer.plugins.HybridILPMinerPlugin;
import org.processmining.lpengines.interfaces.LPEngine.EngineType;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.operator.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualStringCondition;

public class ILPMinerOperator extends AbstractRapidProMDiscoveryOperator {

	private OutputPort outputPetrinet = getOutputPorts().createPort(
			"model (ProM Petri Net)");

	private static final String PARAMETER_KEY_EAC = "enforce_emptiness_after_completion";
	private static final String PARAMETER_DESC_EAC = "Indicates whether the net is empty after replaying the event log";

	private static final String PARAMETER_KEY_FILTER = "filter";
	private static final String PARAMETER_DESC_FILTER = "We can either apply no filtering, which guarantees perfect replay-fitness, or filter using Sequence Encoding Filtering (SEF)";
	private static final String[] PARAMETER_OPTIONS_FITLER = new String[] {
			LPFilterType.NONE.toString(),
			LPFilterType.SEQUENCE_ENCODING.toString() };
	private static final LPFilterType[] PARAMETER_REFERENCE_FILTER = new LPFilterType[] {
			LPFilterType.NONE, LPFilterType.SEQUENCE_ENCODING };

	private static final String PARAMETER_KEY_FILTER_THRESHOLD = "filter_threshold";
	private static final String PARAMETER_DESC_FILTER_THRESHOLD = "Set the sequence encoding threshold t, for which 0 <= t <= 1.";

	public ILPMinerOperator(OperatorDescription description) {
		super(description);

		getTransformer().addRule(
				new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
	}

	public void doWork() throws OperatorException {
		PluginContext context = ProMPluginContextManager.instance()
				.getContext();
		XLog log = getXLog();
		XEventClassifier classifier = getXEventClassifier();
		for (XTrace t : log) {
			for (XEvent e : t) {
				System.out.println(classifier.getClassIdentity(e).toString());
			}
		}
		DiscoveryStrategy strategy = new DiscoveryStrategy(
				DiscoveryStrategyType.CAUSAL);
		CausalActivityGraph cag = getCausalActivityGraph(context, log,
				classifier);
		strategy.setCausalActivityGraph(cag);
		LPMinerConfiguration configuration = LPMinerConfigurationFactory
				.customConfiguration(log, EngineType.LPSOLVE, classifier,
						strategy, getConstraintTypes(),
						LPObjectiveType.WEIGHTED_ABSOLUTE_PARIKH,
						LPVariableType.DUAL, getFilter(), false);

		Object[] pnAndMarking = HybridILPMinerPlugin.mine(
				ProMPluginContextManager.instance().getContext(), log,
				configuration);

		Petrinet pn = (Petrinet) pnAndMarking[0];
		PetriNetIOObject petrinetIOObject = new PetriNetIOObject(pn, context);
		petrinetIOObject.setInitialMarking((Marking) pnAndMarking[1]);
		outputPetrinet.deliver(petrinetIOObject);
	}

	private Collection<LPConstraintType> getConstraintTypes() {
		Collection<LPConstraintType> constraints = EnumSet.of(
				LPConstraintType.THEORY_OF_REGIONS,
				LPConstraintType.NO_TRIVIAL_REGION);
		if (getParameterAsBoolean(PARAMETER_KEY_EAC)) {
			constraints.add(LPConstraintType.EMPTY_AFTER_COMPLETION);
		}
		return constraints;
	}

	private CausalActivityGraph getCausalActivityGraph(PluginContext context,
			XLog log, XEventClassifier classifier) {
		MatrixMiner miner = getMatrixMiner();
		MatrixMinerParameters minerParameters = getMatrixMinerParameters(log);
		minerParameters.setClassifier(classifier);
		CausalActivityMatrix matrix = miner.mineMatrix(context, log,
				minerParameters);
		ConvertCausalActivityMatrixToCausalActivityGraphPlugin creator = getMatrixToCagPlugin();
		ConvertCausalActivityMatrixToCausalActivityGraphParameters creatorParameters = getMatrixToCagParameters();
		creatorParameters.setZeroValue(miner.getZeroValue());
		creatorParameters.setConcurrencyRatio(miner.getConcurrencyRatio());
		creatorParameters.setIncludeThreshold(miner.getIncludeThreshold());
		return creator.run(context, matrix, creatorParameters);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();
		addEmptinessAfterCompletionParameter(params);
		addFilterParameter(params);
		return params;
	}

	private List<ParameterType> addEmptinessAfterCompletionParameter(
			List<ParameterType> params) {
		params.add(new ParameterTypeBoolean(PARAMETER_KEY_EAC,
				PARAMETER_DESC_EAC, false));
		return params;
	}

	private List<ParameterType> addFilterParameter(List<ParameterType> params) {
		params.add(new ParameterTypeCategory(PARAMETER_KEY_FILTER,
				PARAMETER_DESC_FILTER, PARAMETER_OPTIONS_FITLER, 0, false));

		ParameterType filterThreshold = new ParameterTypeDouble(
				PARAMETER_KEY_FILTER_THRESHOLD,
				PARAMETER_DESC_FILTER_THRESHOLD, 0, 1, 0.25, false);
		filterThreshold.setOptional(true);
		filterThreshold.registerDependencyCondition(new EqualStringCondition(
				this, PARAMETER_KEY_FILTER, true,
				LPFilterType.SEQUENCE_ENCODING.toString()));

		params.add(filterThreshold);
		return params;
	}

	private MatrixMiner getMatrixMiner() {
		return new HAFMiniMatrixMiner();
	}

	private MatrixMinerParameters getMatrixMinerParameters(XLog log) {
		return new MatrixMinerParameters(log);
	}

	private ConvertCausalActivityMatrixToCausalActivityGraphPlugin getMatrixToCagPlugin() {
		return new ConvertCausalActivityMatrixToCausalActivityGraphPlugin();
	}

	private ConvertCausalActivityMatrixToCausalActivityGraphParameters getMatrixToCagParameters() {
		return new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
	}

	private LPFilter getFilter() throws UndefinedParameterError {
		LPFilter filter = new LPFilter();
		LPFilterType type = PARAMETER_REFERENCE_FILTER[getParameterAsInt(PARAMETER_KEY_FILTER)];
		filter.setFilterType(type);
		switch (type) {
		case SEQUENCE_ENCODING:
			filter.setThreshold(getParameterAsDouble(PARAMETER_KEY_FILTER_THRESHOLD));
			break;
		default:
			break;
		}
		return filter;
	}
}
