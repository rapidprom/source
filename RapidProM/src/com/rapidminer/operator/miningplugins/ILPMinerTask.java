package com.rapidminer.operator.miningplugins;

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
import org.rapidprom.operators.abstracts.AbstractRapidProMOperator;

import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.util.ProMIOObjectList;

public class ILPMinerTask extends AbstractRapidProMOperator {

	private List<Parameter> parametersILPMiner = null;

	private InputPort inputXLog = getInputPorts()
			.createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts()
			.createPort("model (ProM Petri Net)");
	private OutputPort outputMarking = getOutputPorts()
			.createPort("marking (ProM Marking)");

	public ILPMinerTask(OperatorDescription description) {
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
