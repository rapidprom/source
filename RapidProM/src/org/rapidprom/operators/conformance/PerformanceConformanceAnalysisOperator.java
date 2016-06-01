package org.rapidprom.operators.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
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

public class PerformanceConformanceAnalysisOperator
		extends AbstractRapidProMDiscoveryOperator {

	private static final String PARAMETER_1_KEY = "Max Explored States (in Thousands)",
			PARAMETER_1_DESCR = "The maximum number of states that are searched for a trace alignment.",
			PARAMETER_2_KEY = "Timeout (sec)",
			PARAMETER_2_DESCR = "The number of seconds that this operator will run before "
					+ "returning whatever it could manage to calculate (or null otherwise).",
			PARAMETER_3_KEY = "Number of Threads",
			PARAMETER_3_DESCR = "Specify the number of threads used to calculate alignments in parallel."
					+ " With each extra thread, more memory is used but less cpu time is required.";

	private InputPort inputPN = getInputPorts()
			.createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputManifest = getOutputPorts()
			.createPort("model (ProM Manifest)");

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

		ManifestIOObject manifestIOObject;
		SimpleTimeLimiter limiter = new SimpleTimeLimiter(
				Executors.newSingleThreadExecutor());

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(PNManifestReplayer.class);
		try {
			manifestIOObject = limiter.callWithTimeout(
					new PERFORMANCE_CALCULATOR(pluginContext),
					getParameterAsInt(PARAMETER_2_KEY), TimeUnit.SECONDS, true);
			outputManifest.deliver(manifestIOObject);
		} catch (UncheckedTimeoutException e1) {
			pluginContext.getProgress().cancel();
			logger.log(Level.INFO, "Performance Ckecker timed out.");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		logger.log(Level.INFO,
				"End: replay log on petri net for performance/conformance checking ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	class PERFORMANCE_CALCULATOR implements Callable<ManifestIOObject> {

		PluginContext pluginContext;

		public PERFORMANCE_CALCULATOR(PluginContext input) {
			pluginContext = input;
		}

		@Override
		public ManifestIOObject call() throws Exception {

			PetriNetIOObject pNet = inputPN.getData(PetriNetIOObject.class);
			XLog xLog = getXLog();

			PNManifestReplayerParameter manifestParameters = getParameterObject(
					pNet, xLog);

			PNManifestFlattener flattener = new PNManifestFlattener(
					pNet.getArtifact(), manifestParameters);

			CostBasedCompleteManifestParam parameter = new CostBasedCompleteManifestParam(
					flattener.getMapEvClass2Cost(),
					flattener.getMapTrans2Cost(), flattener.getMapSync2Cost(),
					flattener.getInitMarking(), flattener.getFinalMarkings(),
					manifestParameters.getMaxNumOfStates(),
					flattener.getFragmentTrans());
			parameter.setGUIMode(false);
			parameter.setCreateConn(false);
			parameter.setNumThreads(getParameterAsInt(PARAMETER_3_KEY));

			PNLogReplayer replayer = new PNLogReplayer();
			PetrinetReplayerNoILPRestrictedMoveModel replayAlgorithm = new PetrinetReplayerNoILPRestrictedMoveModel();

			Manifest result = null;
			try {
				PNRepResult alignment = replayer.replayLog(pluginContext,
						flattener.getNet(), xLog, flattener.getMap(),
						replayAlgorithm, parameter);
				result = ManifestFactory.construct(pNet.getArtifact(),
						manifestParameters.getInitMarking(),
						manifestParameters.getFinalMarkings(), xLog, flattener,
						alignment, manifestParameters.getMapping());

				return new ManifestIOObject(result, pluginContext);

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

	}

	private PNManifestReplayerParameter getParameterObject(
			PetriNetIOObject pNet, XLog log) throws UndefinedParameterError {
		PNManifestReplayerParameter parameter = new PNManifestReplayerParameter();
		try {
			parameter.setGUIMode(false);
			parameter.setInitMarking(pNet.getInitialMarking());
			if (!pNet.hasFinalMarking())
				pNet.setFinalMarking(getFinalMarking(pNet.getArtifact()));
			parameter.setFinalMarkings(pNet.getFinalMarkingAsArray());

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
				line: for (XEventClass clazz : eventClasses)
					// look for exact matches on the id
					if (clazz.getId().equals(t.getId())) {
						EvClassPattern pat = new EvClassPattern();
						pat.add(clazz);
						p.add(pat);
						pattern.put(t, p);
						break line;
					}

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

		ParameterTypeInt parameterType1 = new ParameterTypeInt(PARAMETER_1_KEY,
				PARAMETER_1_DESCR, 0, Integer.MAX_VALUE, 200);
		parameterTypes.add(parameterType1);

		ParameterTypeInt parameterType2 = new ParameterTypeInt(PARAMETER_2_KEY,
				PARAMETER_2_DESCR, 0, Integer.MAX_VALUE, 60);
		parameterTypes.add(parameterType2);

		ParameterTypeInt parameterType3 = new ParameterTypeInt(PARAMETER_3_KEY,
				PARAMETER_3_DESCR, 1, Integer.MAX_VALUE,
				Runtime.getRuntime().availableProcessors());
		parameterTypes.add(parameterType3);

		return parameterTypes;
	}

	@SuppressWarnings("rawtypes")
	public static Marking getFinalMarking(Petrinet pn) {
		List<Place> places = new ArrayList<Place>();
		Iterator<Place> placesIt = pn.getPlaces().iterator();
		while (placesIt.hasNext()) {
			Place nextPlace = placesIt.next();
			Collection inEdges = pn.getOutEdges(nextPlace);
			if (inEdges.isEmpty()) {
				places.add(nextPlace);
			}
		}
		Marking m = new Marking();
		m.addAll(places);
		return m;
	}
}
