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

import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.util.ProMIOObjectList;

public class ILPMinerTask extends Operator {

	private List<Parameter> parametersILPMiner = null;

	private InputPort inputXLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts().createPort(
			"model (ProM Petri Net)");
	private OutputPort outputMarking = getOutputPorts().createPort(
			"marking (ProM Marking)");

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
		XLog eventLog = ((XLogIOObject) inputXLog.getData(XLogIOObject.class))
				.getData();
		// TODO: ASK USER FOR CLASSIFIER
		XEventClassifier classifier = new XEventAndClassifier(
				new XEventNameClassifier());
		MatrixMiner miner = new HAFMiniMatrixMiner();
		MatrixMinerParameters minerParameters = new MatrixMinerParameters(
				eventLog);
		minerParameters.setClassifier(classifier);
		CausalActivityMatrix matrix = miner.mineMatrix(context, eventLog,
				minerParameters);
		ConvertCausalActivityMatrixToCausalActivityGraphPlugin creator = new ConvertCausalActivityMatrixToCausalActivityGraphPlugin();
		ConvertCausalActivityMatrixToCausalActivityGraphParameters creatorParameters = new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
		creatorParameters.setZeroValue(miner.getZeroValue());
		creatorParameters.setConcurrencyRatio(miner.getConcurrencyRatio());
		creatorParameters.setIncludeThreshold(miner.getIncludeThreshold());
		CausalActivityGraph graph = creator.run(context, matrix,
				creatorParameters);
		DiscoveryStrategy strategy = new DiscoveryStrategy(
				DiscoveryStrategyType.CAUSAL);
		strategy.setCausalActivityGraph(graph);
		LPMinerConfiguration configuration = LPMinerConfigurationFactory
				.customConfiguration(eventLog, EngineType.LPSOLVE, classifier,
						strategy, EnumSet.allOf(LPConstraintType.class),
						LPObjectiveType.WEIGHTED_ABSOLUTE_PARIKH,
						LPVariableType.DUAL, new LPFilter(), false);
		Object[] pnAndMarking = HybridILPMinerPlugin.mine(eventLog,
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

}
