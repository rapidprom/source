package org.rapidprom.operators.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.AbstractPetrinetReplayer;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.PNRepResultIOObjectVisualizationType;
import org.rapidprom.ioobjects.PNRepResultIOObject;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractDataReader.AttributeColumn;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

import javassist.tools.rmi.ObjectNotFoundException;
import nl.tue.astar.AStarException;

public class ConformanceAnalysisOperator
		extends AbstractRapidProMDiscoveryOperator {

	private static final String PARAMETER_0_KEY = "Replay Algorithm",
			PARAMETER_0_DESCR = "The Petri net replayer algorithm that will be used to calculate alignments.",
			PARAMETER_1_KEY = "Max Explored States (in Thousands)",
			PARAMETER_1_DESCR = "The maximum number of states that are searched for a trace alignment.",
			PARAMETER_2_KEY = "Timeout (sec)",
			PARAMETER_2_DESCR = "The number of seconds that this operator will run before "
					+ "returning whatever it could manage to calculate (or null otherwise).",
			PARAMETER_3_KEY = "Number of Threads",
			PARAMETER_3_DESCR = "Specify the number of threads used to calculate alignments in parallel."
					+ " With each extra thread, more memory is used but less cpu time is required.";

	private static final String WITH_ILP = "ILP Replayer",
			WITHOUT_ILP = "non-ILP Replayer";

	private PNRepResultIOObject alignments;

	private final String NAMECOL = "Name";
	private final String VALUECOL = "Value";

	// alignment
	private final String TRACEIDENTIFIER = "Trace Identifier";
	private final String TRACEINDEX = "Trace Index";
	private final String RELIABLE = "Unreliable Alignments Exist";

	private InputPort inputPN = getInputPorts()
			.createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort output = getOutputPorts()
			.createPort("alignments (ProM PNRepResult)");

	private OutputPort outputData = getOutputPorts()
			.createPort("example set with metrics (Data Table)");
	private OutputPort outputAlignment = getOutputPorts()
			.createPort("example set with alignment values (Data Table)");
	private OutputPort outputAlignmentTrace = getOutputPorts().createPort(
			"example set with alignment values per trace (Data Table)");
	private OutputPort outputReliable = getOutputPorts().createPort(
			"example set with indicator for reliable traces (Data Table)");

	private ExampleSetMetaData metaData = null;
	private ExampleSetMetaData metaData2 = null;
	private ExampleSetMetaData metaData3 = null;
	private ExampleSetMetaData metaData4 = null;

	public ConformanceAnalysisOperator(OperatorDescription description) {
		super(description);

		getTransformer().addRule(
				new GenerateNewMDRule(output, PNRepResultIOObject.class));

		this.metaData = new ExampleSetMetaData();
		AttributeMetaData amd1 = new AttributeMetaData(NAMECOL,
				Ontology.STRING);
		amd1.setRole(AttributeColumn.REGULAR);
		amd1.setNumberOfMissingValues(new MDInteger(0));
		metaData.addAttribute(amd1);
		AttributeMetaData amd2 = new AttributeMetaData(VALUECOL,
				Ontology.NUMERICAL);
		amd2.setRole(AttributeColumn.REGULAR);
		amd2.setNumberOfMissingValues(new MDInteger(0));
		metaData.addAttribute(amd2);
		metaData.setNumberOfExamples(1);
		getTransformer()
				.addRule(new GenerateNewMDRule(outputData, this.metaData));
		// for the alignment
		this.metaData2 = new ExampleSetMetaData();
		AttributeMetaData alignAmd1 = new AttributeMetaData(this.TRACEINDEX,
				Ontology.STRING);
		alignAmd1.setRole(AttributeColumn.REGULAR);
		alignAmd1.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd1);
		AttributeMetaData alignAmd2 = new AttributeMetaData(
				PNRepResult.TRACEFITNESS, Ontology.NUMERICAL);
		alignAmd2.setRole(AttributeColumn.REGULAR);
		alignAmd2.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd2);
		AttributeMetaData alignAmd3 = new AttributeMetaData(
				PNRepResult.MOVELOGFITNESS, Ontology.NUMERICAL);
		alignAmd3.setRole(AttributeColumn.REGULAR);
		alignAmd3.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd3);
		AttributeMetaData alignAmd4 = new AttributeMetaData(
				PNRepResult.MOVEMODELFITNESS, Ontology.NUMERICAL);
		alignAmd4.setRole(AttributeColumn.REGULAR);
		alignAmd4.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd4);
		AttributeMetaData alignAmd5 = new AttributeMetaData(
				PNRepResult.RAWFITNESSCOST, Ontology.NUMERICAL);
		alignAmd5.setRole(AttributeColumn.REGULAR);
		alignAmd5.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd5);
		AttributeMetaData alignAmd6 = new AttributeMetaData(
				PNRepResult.NUMSTATEGENERATED, Ontology.NUMERICAL);
		alignAmd6.setRole(AttributeColumn.REGULAR);
		alignAmd6.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd6);
		metaData2.setNumberOfExamples(1);
		getTransformer().addRule(
				new GenerateNewMDRule(outputAlignment, this.metaData2));
		// for the alignment per trace
		this.metaData3 = new ExampleSetMetaData();
		AttributeMetaData alignAmd11 = new AttributeMetaData(this.TRACEINDEX,
				Ontology.STRING);
		alignAmd11.setRole(AttributeColumn.REGULAR);
		alignAmd11.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd11);
		AttributeMetaData alignAmd111 = new AttributeMetaData(
				this.TRACEIDENTIFIER, Ontology.STRING);
		alignAmd111.setRole(AttributeColumn.REGULAR);
		alignAmd111.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd111);
		AttributeMetaData alignAmd12 = new AttributeMetaData(
				PNRepResult.TRACEFITNESS, Ontology.NUMERICAL);
		alignAmd12.setRole(AttributeColumn.REGULAR);
		alignAmd12.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd12);
		AttributeMetaData alignAmd13 = new AttributeMetaData(
				PNRepResult.MOVELOGFITNESS, Ontology.NUMERICAL);
		alignAmd13.setRole(AttributeColumn.REGULAR);
		alignAmd13.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd3);
		AttributeMetaData alignAmd14 = new AttributeMetaData(
				PNRepResult.MOVEMODELFITNESS, Ontology.NUMERICAL);
		alignAmd14.setRole(AttributeColumn.REGULAR);
		alignAmd14.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd14);
		AttributeMetaData alignAmd15 = new AttributeMetaData(
				PNRepResult.RAWFITNESSCOST, Ontology.NUMERICAL);
		alignAmd15.setRole(AttributeColumn.REGULAR);
		alignAmd15.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd15);
		AttributeMetaData alignAmd16 = new AttributeMetaData(
				PNRepResult.NUMSTATEGENERATED, Ontology.NUMERICAL);
		alignAmd16.setRole(AttributeColumn.REGULAR);
		alignAmd16.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd16);
		metaData3.setNumberOfExamples(1);
		getTransformer().addRule(
				new GenerateNewMDRule(outputAlignmentTrace, this.metaData3));
		// md4
		this.metaData4 = new ExampleSetMetaData();
		getTransformer()
				.addRule(new GenerateNewMDRule(outputReliable, this.metaData4));

		alignments = null;
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: replay log on petri net for conformance checking");
		long time = System.currentTimeMillis();

		SimpleTimeLimiter limiter = new SimpleTimeLimiter(
				Executors.newSingleThreadExecutor());
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(PNLogReplayer.class);

		PNRepResult repResult = null;

		try {
			alignments = limiter.callWithTimeout(
					new ALIGNMENT_CALCULATOR(pluginContext),
					getParameterAsInt(PARAMETER_2_KEY), TimeUnit.SECONDS, true);
			repResult = alignments.getArtifact();

			output.deliver(alignments);

		} catch (UncheckedTimeoutException e1) {
			pluginContext.getProgress().cancel();
			logger.log(Level.INFO, "Conformance Checker timed out.");
			output.deliver(new PNRepResultIOObject(null, pluginContext, null, null, null));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		fillTables(repResult);

		logger.log(Level.INFO,
				"End: replay log on petri net for conformance checking ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	class ALIGNMENT_CALCULATOR implements Callable<PNRepResultIOObject> {

		PluginContext pluginContext;

		public ALIGNMENT_CALCULATOR(PluginContext input) {
			pluginContext = input;
		}

		@Override
		public PNRepResultIOObject call() throws Exception {

			XLogIOObject xLog = new XLogIOObject(getXLog(), pluginContext);
			PetriNetIOObject pNet = inputPN.getData(PetriNetIOObject.class);

			PNRepResult repResult = null;
			try {
				if (!pNet.hasFinalMarking())
					pNet.setFinalMarking(getFinalMarking(pNet.getArtifact()));
				repResult = getAlignment(pluginContext, pNet.getArtifact(),
						xLog.getArtifact(), pNet.getInitialMarking(),
						pNet.getFinalMarking());
			} catch (ObjectNotFoundException e1) {
				e1.printStackTrace();
			}

			PNRepResultIOObject result = new PNRepResultIOObject(repResult,
					pluginContext, pNet, xLog.getArtifact(),
					constructMapping(pNet.getArtifact(), xLog.getArtifact(),
							getXEventClassifier()));
			result.setVisualizationType(
					PNRepResultIOObjectVisualizationType.PROJECT_ON_MODEL);

			return result;
		}

	}

	private List<Integer> convertIntListToArray(String s) {
		List<Integer> result = new ArrayList<Integer>();
		s = s.replace("[", "");
		s = s.replace("]", "");
		String[] split = s.split(",");
		for (int i = 0; i < split.length; i++) {
			String string = split[i];
			String trim = string.trim();
			Integer in = Integer.parseInt(trim);
			result.add(in);
		}
		return result;
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
		Marking finalMarking = new Marking();
		for (Place place : places) {
			finalMarking.add(place);
		}
		return finalMarking;
	}

	private void fillTableWithRow(MemoryExampleTable table, String name,
			Object value, List<Attribute> attributes) {
		// fill table
		DataRowFactory factory = new DataRowFactory(
				DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
		Object[] vals = new Object[2];
		vals[0] = name;
		vals[1] = value;
		// convert the list to array
		Attribute[] attribArray = new Attribute[attributes.size()];
		for (int i = 0; i < attributes.size(); i++) {
			attribArray[i] = attributes.get(i);
		}
		DataRow dataRow = factory.create(vals, attribArray);
		table.addDataRow(dataRow);
	}

	// Boudewijn's methods for creating alignments

	public PNRepResult getAlignment(PluginContext pluginContext,
			PetrinetGraph net, XLog log, Marking initialMarking,
			Marking finalMarking) throws UndefinedParameterError {

		Map<Transition, Integer> costMOS = constructMOSCostFunction(net);
		XEventClassifier eventClassifier = getXEventClassifier();
		Map<XEventClass, Integer> costMOT = constructMOTCostFunction(net, log,
				eventClassifier);
		TransEvClassMapping mapping = constructMapping(net, log,
				eventClassifier);

		AbstractPetrinetReplayer<?, ?> replayEngine = null;
		if (getParameterAsString(PARAMETER_0_KEY).equals(WITH_ILP))
			replayEngine = new PetrinetReplayerWithILP();
		else
			replayEngine = new PetrinetReplayerWithoutILP();

		IPNReplayParameter parameters = new CostBasedCompleteParam(costMOT,
				costMOS);
		parameters.setInitialMarking(initialMarking);
		parameters.setFinalMarkings(finalMarking);
		parameters.setGUIMode(false);
		parameters.setCreateConn(false);
		parameters.setNumThreads(getParameterAsInt(PARAMETER_3_KEY));
		((CostBasedCompleteParam) parameters)
				.setMaxNumOfStates(getParameterAsInt(PARAMETER_1_KEY) * 1000);

		PNRepResult result = null;
		try {
			result = replayEngine.replayLog(pluginContext, net, log, mapping,
					parameters);

		} catch (AStarException e) {
			e.printStackTrace();
		}

		return result;
	}

	private static Map<Transition, Integer> constructMOSCostFunction(
			PetrinetGraph net) {
		Map<Transition, Integer> costMOS = new HashMap<Transition, Integer>();

		for (Transition t : net.getTransitions())
			if (t.isInvisible())
				costMOS.put(t, 0);
			else
				costMOS.put(t, 1);

		return costMOS;
	}

	private static Map<XEventClass, Integer> constructMOTCostFunction(
			PetrinetGraph net, XLog log, XEventClassifier eventClassifier) {
		Map<XEventClass, Integer> costMOT = new HashMap<XEventClass, Integer>();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (XEventClass evClass : summary.getEventClasses().getClasses()) {
			costMOT.put(evClass, 1);
		}

		return costMOT;
	}

	private static TransEvClassMapping constructMapping(PetrinetGraph net,
			XLog log, XEventClassifier eventClassifier) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier,
				new XEventClass("DUMMY", 99999));

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (Transition t : net.getTransitions()) {
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				String id = evClass.getId();

				if (t.getLabel().equals(id)) {
					mapping.put(t, evClass);
					break;
				}
			}

		}

		return mapping;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeCategory parameterType0 = new ParameterTypeCategory(
				PARAMETER_0_KEY, PARAMETER_0_DESCR,
				new String[] { WITH_ILP, WITHOUT_ILP }, 0);
		parameterTypes.add(parameterType0);

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

	public void fillTables(PNRepResult repResult) throws OperatorException {

		if (repResult != null && !repResult.isEmpty()) {

			Iterator<SyncReplayResult> iterator3 = repResult.iterator();
			boolean unreliable = false;
			while (iterator3.hasNext()) {
				SyncReplayResult next = iterator3.next();
				boolean reliable = next.isReliable();
				if (!reliable) {
					unreliable = true;
					break;
				}
			}

			fillFitnessTable(repResult, unreliable);
			fillTraceGroupAlignmentTable(repResult);
			fillTraceSingleAlignmentTable(repResult);
			fillUnreliableAlignmentsTable(unreliable);

		} else {

			fillFitnessTable(null, true);
			fillTraceGroupAlignmentTable(null);
			fillTraceSingleAlignmentTable(null);
			fillUnreliableAlignmentsTable(true);

		}

	}

	public void fillFitnessTable(PNRepResult repResult, boolean unreliable) {

		ExampleSet es = null;
		MemoryExampleTable table = null;
		List<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(AttributeFactory.createAttribute(this.NAMECOL,
				Ontology.STRING));
		attributes.add(AttributeFactory.createAttribute(this.VALUECOL,
				Ontology.NUMERICAL));
		table = new MemoryExampleTable(attributes);
		if (unreliable || repResult == null) {
			fillTableWithRow(table, PNRepResult.TRACEFITNESS, Double.NaN,
					attributes);
			fillTableWithRow(table, PNRepResult.MOVELOGFITNESS, Double.NaN,
					attributes);
			fillTableWithRow(table, PNRepResult.MOVEMODELFITNESS, Double.NaN,
					attributes);
			fillTableWithRow(table, PNRepResult.RAWFITNESSCOST, Double.NaN,
					attributes);
			fillTableWithRow(table, PNRepResult.NUMSTATEGENERATED, Double.NaN,
					attributes);

		} else {

			Map<String, Object> info = repResult.getInfo();
			double trace_fitness = 0;
			try {
				trace_fitness = Double.parseDouble(
						(String) info.get(PNRepResult.TRACEFITNESS));
			} catch (Exception e) {
				trace_fitness = (Double) info.get(PNRepResult.TRACEFITNESS);
			}
			double move_log_fitness = (Double) info
					.get(PNRepResult.MOVELOGFITNESS);
			double move_model_fitness = (Double) info
					.get(PNRepResult.MOVEMODELFITNESS);
			double raw_fitness_costs = (Double) info
					.get(PNRepResult.RAWFITNESSCOST);
			double num_state_gen = (Double) info
					.get(PNRepResult.NUMSTATEGENERATED);

			fillTableWithRow(table, PNRepResult.TRACEFITNESS, trace_fitness,
					attributes);
			fillTableWithRow(table, PNRepResult.MOVELOGFITNESS,
					move_log_fitness, attributes);
			fillTableWithRow(table, PNRepResult.MOVEMODELFITNESS,
					move_model_fitness, attributes);
			fillTableWithRow(table, PNRepResult.RAWFITNESSCOST,
					raw_fitness_costs, attributes);
			fillTableWithRow(table, PNRepResult.NUMSTATEGENERATED,
					num_state_gen, attributes);
		}
		es = table.createExampleSet();
		outputData.deliver(es);
	}

	public void fillTraceGroupAlignmentTable(PNRepResult repResult) {
		// output the trace alignment
		ExampleSet es2 = null;
		MemoryExampleTable table2 = null;
		List<Attribute> attributes2 = new LinkedList<Attribute>();
		attributes2.add(AttributeFactory.createAttribute(this.TRACEINDEX,
				Ontology.STRING));
		attributes2.add(AttributeFactory
				.createAttribute(PNRepResult.TRACEFITNESS, Ontology.NUMERICAL));
		attributes2.add(AttributeFactory.createAttribute(
				PNRepResult.MOVELOGFITNESS, Ontology.NUMERICAL));
		attributes2.add(AttributeFactory.createAttribute(
				PNRepResult.MOVEMODELFITNESS, Ontology.NUMERICAL));
		attributes2.add(AttributeFactory.createAttribute(
				PNRepResult.RAWFITNESSCOST, Ontology.NUMERICAL));
		attributes2.add(AttributeFactory.createAttribute(
				PNRepResult.NUMSTATEGENERATED, Ontology.NUMERICAL));
		table2 = new MemoryExampleTable(attributes2);

		if (repResult != null) {
			Iterator<SyncReplayResult> iterator = repResult.iterator();
			while (iterator.hasNext()) {
				SyncReplayResult next = iterator.next();
				DataRowFactory factory = new DataRowFactory(
						DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
				Object[] vals = new Object[6];
				vals[0] = next.getTraceIndex().toString();
				vals[1] = next.getInfo().get(PNRepResult.TRACEFITNESS);
				vals[2] = next.getInfo().get(PNRepResult.MOVELOGFITNESS);
				vals[3] = next.getInfo().get(PNRepResult.MOVEMODELFITNESS);
				vals[4] = next.getInfo().get(PNRepResult.RAWFITNESSCOST);
				vals[5] = next.getInfo().get(PNRepResult.NUMSTATEGENERATED);

				Attribute[] attribArray = new Attribute[attributes2.size()];
				for (int i = 0; i < attributes2.size(); i++) {
					attribArray[i] = attributes2.get(i);
				}
				DataRow dataRow = factory.create(vals, attribArray);
				table2.addDataRow(dataRow);
			}
		} else {
			DataRowFactory factory = new DataRowFactory(
					DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
			Object[] vals = new Object[6];
			vals[0] = "?";
			vals[1] = Double.NaN;
			vals[2] = Double.NaN;
			vals[3] = Double.NaN;
			vals[4] = Double.NaN;
			vals[5] = Double.NaN;

			Attribute[] attribArray = new Attribute[attributes2.size()];
			for (int i = 0; i < attributes2.size(); i++) {
				attribArray[i] = attributes2.get(i);
			}
			DataRow dataRow = factory.create(vals, attribArray);
			table2.addDataRow(dataRow);
		}

		es2 = table2.createExampleSet();
		outputAlignment.deliver(es2);
	}

	public void fillTraceSingleAlignmentTable(PNRepResult repResult)
			throws OperatorException {

		// create the third exampleset
		ExampleSet es3 = null;
		MemoryExampleTable table3 = null;
		List<Attribute> attributes3 = new LinkedList<Attribute>();
		attributes3.add(AttributeFactory.createAttribute(this.TRACEINDEX,
				Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(this.TRACEIDENTIFIER,
				Ontology.STRING));
		attributes3.add(AttributeFactory
				.createAttribute(PNRepResult.TRACEFITNESS, Ontology.NUMERICAL));
		attributes3.add(AttributeFactory.createAttribute(
				PNRepResult.MOVELOGFITNESS, Ontology.NUMERICAL));
		attributes3.add(AttributeFactory.createAttribute(
				PNRepResult.MOVEMODELFITNESS, Ontology.NUMERICAL));
		attributes3.add(AttributeFactory.createAttribute(
				PNRepResult.RAWFITNESSCOST, Ontology.NUMERICAL));
		attributes3.add(AttributeFactory.createAttribute(
				PNRepResult.NUMSTATEGENERATED, Ontology.NUMERICAL));
		table3 = new MemoryExampleTable(attributes3);

		if (repResult != null) {
			Iterator<SyncReplayResult> iterator2 = repResult.iterator();
			while (iterator2.hasNext()) {
				SyncReplayResult next = iterator2.next();
				DataRowFactory factory = new DataRowFactory(
						DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
				Object[] vals = new Object[7];
				vals[2] = next.getInfo().get(PNRepResult.TRACEFITNESS);
				vals[3] = next.getInfo().get(PNRepResult.MOVELOGFITNESS);
				vals[4] = next.getInfo().get(PNRepResult.MOVEMODELFITNESS);
				vals[5] = next.getInfo().get(PNRepResult.RAWFITNESSCOST);
				vals[6] = next.getInfo().get(PNRepResult.NUMSTATEGENERATED);
				// convert the list to array
				Attribute[] attribArray = new Attribute[attributes3.size()];
				for (int i = 0; i < attributes3.size(); i++) {
					attribArray[i] = attributes3.get(i);
				}
				List<Integer> listArray = convertIntListToArray(
						next.getTraceIndex().toString());
				for (Integer s : listArray) {
					// get the right trace
					XTrace xTrace = getXLog().get(s);
					String name = XConceptExtension.instance()
							.extractName(xTrace);
					vals[0] = s.toString();
					vals[1] = name;
					DataRow dataRow = factory.create(vals, attribArray);
					table3.addDataRow(dataRow);
				}
			}
		} else {
			DataRowFactory factory = new DataRowFactory(
					DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
			Object[] vals = new Object[7];
			vals[0] = "?";
			vals[1] = "?";
			vals[2] = Double.NaN;
			vals[3] = Double.NaN;
			vals[4] = Double.NaN;
			vals[5] = Double.NaN;
			vals[6] = Double.NaN;

			Attribute[] attribArray = new Attribute[attributes3.size()];
			for (int i = 0; i < attributes3.size(); i++) {
				attribArray[i] = attributes3.get(i);
			}
			DataRow dataRow = factory.create(vals, attribArray);
			table3.addDataRow(dataRow);
		}
		es3 = table3.createExampleSet();
		outputAlignmentTrace.deliver(es3);
	}

	public void fillUnreliableAlignmentsTable(boolean unreliable) {
		// CREATE THE fourth es
		ExampleSet es4 = null;
		MemoryExampleTable table4 = null;
		List<Attribute> attributes4 = new LinkedList<Attribute>();
		attributes4.add(AttributeFactory.createAttribute(this.NAMECOL,
				Ontology.STRING));
		attributes4.add(AttributeFactory.createAttribute(this.VALUECOL,
				Ontology.STRING));
		table4 = new MemoryExampleTable(attributes4);
		fillTableWithRow(table4, RELIABLE, Boolean.toString(unreliable),
				attributes4);
		es4 = table4.createExampleSet();
		outputReliable.deliver(es4);
	}

}
