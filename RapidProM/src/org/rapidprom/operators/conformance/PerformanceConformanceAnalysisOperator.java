package org.rapidprom.operators.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerNoILPRestrictedMoveModel;
import org.processmining.plugins.astar.petrinet.manifestreplay.CostBasedCompleteManifestParam;
import org.processmining.plugins.astar.petrinet.manifestreplay.ManifestFactory;
import org.processmining.plugins.astar.petrinet.manifestreplay.PNManifestFlattener;
import org.processmining.plugins.petrinet.manifestreplayer.EvClassPattern;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayer;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.ManifestIOObject;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;

import javassist.tools.rmi.ObjectNotFoundException;
import nl.tue.astar.AStarException;

public class PerformanceConformanceAnalysisOperator
		extends AbstractRapidProMDiscoveryOperator {

	private static final String PARAMETER_1_KEY = "Max Explored States (in Thousands)",
			PARAMETER_1_DESCR = "The maximum number of states that are searched for a trace alignment.";

	private InputPort inputPN = getInputPorts()
			.createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputManifest = getOutputPorts()
			.createPort("model (ProM Manifest)");
	private OutputPort outputFitness = getOutputPorts()
			.createPort("example set (Data Table)");

	public PerformanceConformanceAnalysisOperator(
			OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputManifest, ManifestIOObject.class));
		// getTransformer().addRule( new GenerateNewMDRule(outputFitness,
		// FitnessIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: replay log on petri net for performance/conformance checking");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(PNManifestReplayer.class);

		PetriNetIOObject pNet = inputPN.getData(PetriNetIOObject.class);
		XLog xLog = getXLog();

		PNManifestReplayerParameter manifestParameters = getParameterObject(
				pNet, xLog);

		PNManifestFlattener flattener = new PNManifestFlattener(
				pNet.getArtifact(), manifestParameters);

		CostBasedCompleteManifestParam parameter = new CostBasedCompleteManifestParam(
				flattener.getMapEvClass2Cost(), flattener.getMapTrans2Cost(),
				flattener.getMapSync2Cost(), flattener.getInitMarking(),
				flattener.getFinalMarkings(),
				manifestParameters.getMaxNumOfStates(),
				flattener.getFragmentTrans());
		parameter.setGUIMode(false);
		parameter.setCreateConn(false);

		PNLogReplayer replayer = new PNLogReplayer();
		PetrinetReplayerNoILPRestrictedMoveModel replayAlgorithm = new PetrinetReplayerNoILPRestrictedMoveModel();

		Manifest result = null;
		try {
			PNRepResult alignment = replayer.replayLog(
					manifestParameters.isGUIMode() ? pluginContext : null,
					flattener.getNet(), xLog, flattener.getMap(),
					replayAlgorithm, parameter);
			result = ManifestFactory.construct(pNet.getArtifact(),
					manifestParameters.getInitMarking(),
					manifestParameters.getFinalMarkings(), xLog, flattener,
					alignment, manifestParameters.getMapping());
		} catch (AStarException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ManifestIOObject manifestIOObject = new ManifestIOObject(result,
				pluginContext);
		outputManifest.deliver(manifestIOObject);

		double sum = 0;
		for (int j = 0; j < manifestIOObject.getArtifact()
				.getCasePointers().length; j++) {
			sum = sum + manifestIOObject.getArtifact().getTraceFitness(j);
		}

		ExampleSet es = ExampleSetFactory.createExampleSet(new Object[][] {
				{ "fitness",
						sum / (double) manifestIOObject.getArtifact()
								.getCasePointers().length },
				{ "precision", 0.0 } });
		outputFitness.deliver(es);

		logger.log(Level.INFO,
				"End: replay log on petri net for performance/conformance checking ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	private PNManifestReplayerParameter getParameterObject(
			PetriNetIOObject pNet, XLog log) throws UndefinedParameterError {
		PNManifestReplayerParameter parameter = new PNManifestReplayerParameter();
		try {
			parameter.setGUIMode(false);
			parameter.setInitMarking(pNet.getInitialMarking());
			parameter.setFinalMarkings(getFinalMarking(pNet.getArtifact()));
			parameter.setMaxNumOfStates(
					getParameterAsInt(PARAMETER_1_KEY) * 1000);
			TransClasses tc = new TransClasses(pNet.getArtifact());
			Map<TransClass, Set<EvClassPattern>> pattern = new HashMap<TransClass, Set<EvClassPattern>>();

			XEventClassifier classifier = getXEventClassifier();
			Collection<XEventClass> eventClasses = XLogInfoFactory
					.createLogInfo(log, classifier).getEventClasses()
					.getClasses();

			for (TransClass t : tc.getTransClasses()) {
				Set<EvClassPattern> p = new HashSet<EvClassPattern>();
				for (XEventClass clazz : eventClasses)
					// look for exact matches on the id
					if (clazz.getId().equals(t.getId())) {
						EvClassPattern pat = new EvClassPattern();
						pat.add(clazz);
						p.add(pat);
					}
				pattern.put(t, p);
			}
			TransClass2PatternMap mapping = new TransClass2PatternMap(log,
					pNet.getArtifact(), classifier, tc, pattern);
			parameter.setMapping(mapping);

			Map<XEventClass, Integer> mapEvClass2Cost = new HashMap<XEventClass, Integer>();
			for (XEventClass c : eventClasses) {
				mapEvClass2Cost.put(c, 1);
			}

			parameter.setMapEvClass2Cost(mapEvClass2Cost);

			Map<TransClass, Integer> costs = new HashMap<TransClass, Integer>();
			Map<TransClass, Integer> costsSync = new HashMap<TransClass, Integer>();
			for (TransClass t : tc.getTransClasses()) {
				costs.put(t, 1);
				costsSync.put(t, 0);
			}
			parameter.setTrans2Cost(costs);
			parameter.setTransSync2Cost(costsSync);

		} catch (ObjectNotFoundException e1) {
			e1.printStackTrace();
		}

		return parameter;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeInt parameterType2 = new ParameterTypeInt(PARAMETER_1_KEY,
				PARAMETER_1_DESCR, 0, Integer.MAX_VALUE, 200);
		parameterTypes.add(parameterType2);

		return parameterTypes;
	}

	@SuppressWarnings("rawtypes")
	public static Marking[] getFinalMarking(Petrinet pn) {
		List<Place> places = new ArrayList<Place>();
		Iterator<Place> placesIt = pn.getPlaces().iterator();
		while (placesIt.hasNext()) {
			Place nextPlace = placesIt.next();
			Collection inEdges = pn.getOutEdges(nextPlace);
			if (inEdges.isEmpty()) {
				places.add(nextPlace);
			}
		}
		List<Marking> finalMarking = new ArrayList<Marking>();
		for (Place place : places) {
			Marking m = new Marking();
			m.add(place);
			finalMarking.add(m);
		}
		return finalMarking.toArray(new Marking[finalMarking.size()]);
	}
}
