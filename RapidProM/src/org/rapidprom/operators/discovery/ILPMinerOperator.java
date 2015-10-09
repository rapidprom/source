package org.rapidprom.operators.discovery;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
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
import org.processmining.hybridilpminer.models.lp.configuration.parameters.LPObjectiveType;
import org.processmining.hybridilpminer.models.lp.configuration.parameters.LPVariableType;
import org.processmining.hybridilpminer.plugins.HybridILPMinerPlugin;
import org.processmining.lpengines.interfaces.LPEngine.EngineType;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;

import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.util.ProMIOObjectList;

public class ILPMinerOperator extends Operator {

	private InputPort inputXLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts().createPort(
			"model (ProM Petri Net)");
	private OutputPort outputMarking = getOutputPorts().createPort(
			"marking (ProM Marking)");

	private static final String PARAMETER_KEY_EVENT_CLASSIFIER = "classifier";
	private static final String PARAMETER_DESC_EVENT_CLASSIFIER = "Indicates how to classify events within the event log";
	private List<XEventClassifier> classifiers;

	public ILPMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
		getTransformer().addRule(
				new GenerateNewMDRule(outputMarking, MarkingIOObject.class));
	}

	public void doWork() throws OperatorException {
		PluginContext context = ProMPluginContextManager.instance()
				.getContext();
		XLog log = ((XLogIOObject) inputXLog.getData(XLogIOObject.class))
				.getData();
		// TODO: ASK USER FOR CLASSIFIER
		XEventClassifier classifier = getXEventClassifier();
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
		CausalActivityGraph graph = creator.run(context, matrix,
				creatorParameters);
		DiscoveryStrategy strategy = getDiscoveryStrategy();
		strategy.setCausalActivityGraph(graph);
		LPMinerConfiguration configuration = LPMinerConfigurationFactory
				.customConfiguration(log, EngineType.LPSOLVE, classifier,
						strategy, EnumSet.allOf(LPConstraintType.class),
						LPObjectiveType.WEIGHTED_ABSOLUTE_PARIKH,
						LPVariableType.DUAL, getFilter(), false);
		Object[] pnAndMarking = HybridILPMinerPlugin.mine(
				ProMPluginContextManager.instance().getContext(), log,
				configuration);

		Petrinet pn = (Petrinet) pnAndMarking[0];
		PetriNetIOObject petrinetIOObject = new PetriNetIOObject(pn);
		petrinetIOObject.setPluginContext(context);
		outputPetrinet.deliver(petrinetIOObject);
		MarkingIOObject markingIOObject = new MarkingIOObject(
				(Marking) pnAndMarking[1]);
		markingIOObject.setPluginContext(context);
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(markingIOObject);
		instance.addToList(petrinetIOObject);
		outputMarking.deliver(markingIOObject);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();
		// parameterTypes = addClassifierParameter(parameterTypes);
		parameterTypes.add(new ParameterTypeAttribute(
				PARAMETER_KEY_EVENT_CLASSIFIER,
				PARAMETER_DESC_EVENT_CLASSIFIER, inputXLog, false));
		return parameterTypes;
	}

	private List<ParameterType> addClassifierParameter(
			List<ParameterType> parameterTypes) {
		classifiers = new ArrayList<XEventClassifier>();
		String[] classifierNames = null;
		try {
			XLog log = ((XLogIOObject) inputXLog.getData(XLogIOObject.class))
					.getData();
			if (!(log.getClassifiers().isEmpty())) {
				classifierNames = new String[log.getClassifiers().size()];
				int i = 0;
				for (XEventClassifier c : log.getClassifiers()) {
					classifiers.add(c);
					classifierNames[i] = c.toString();
					i++;
				}
			}
		} catch (UserError e) {
			// NOP
		}
		if (classifierNames == null) {
			XEventClassifier defaultClassifier = new XEventAndClassifier(
					new XEventNameClassifier());
			classifiers.add(defaultClassifier);
			classifierNames = new String[] { defaultClassifier.toString() };
		}
		ParameterTypeCategory param = new ParameterTypeCategory(
				PARAMETER_KEY_EVENT_CLASSIFIER,
				PARAMETER_DESC_EVENT_CLASSIFIER, classifierNames, 0, false);
		parameterTypes.add(param);
		return parameterTypes;
	}

	private XEventClassifier getXEventClassifier() {
		return new XEventAndClassifier(new XEventNameClassifier());
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

	private LPFilter getFilter() {
		return new LPFilter();
	}

	private DiscoveryStrategy getDiscoveryStrategy() {
		return new DiscoveryStrategy(DiscoveryStrategyType.CAUSAL);
	}

}
