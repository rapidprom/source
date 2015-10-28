package org.rapidprom.operators.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.tools.rmi.ObjectNotFoundException;
import nl.tue.astar.AStarException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.astar.petrinet.PrefixBasedPetrinetReplayer;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompletePruneAlg;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedprefix.CostBasedPrefixParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.PNRepResultIOObjectVisualizationType;
import org.rapidprom.ioobjects.PNRepResultIOObject;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.AbstractDataReader.AttributeColumn;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

public class ConformanceAnalysisOperator extends Operator {

	private static final String PARAMETER_1 = "Replay Algorithm";

	private static final String[] ALGORITHMS = new String[] {
			"A* Cost-based Fitness", "A* Cost-based Fitness Express",
			"Prefix based A* Cost-based Fitness",};

	private final String NAMECOL = "Name";
	private final String VALUECOL = "Value";

	// alignment
	private final String TRACEIDENTIFIER = "Trace Identifier";
	private final String TRACEINDEX = "Trace Index";
	private final String RELIABLE = "Unreliable Alignments Exist";

	private InputPort inputLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private InputPort inputPN = getInputPorts().createPort(
			"model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort output = getOutputPorts().createPort(
			"alignments (ProM PNRepResult)");

	private OutputPort outputData = getOutputPorts().createPort(
			"example set with metrics (Data Table)");
	private OutputPort outputAlignment = getOutputPorts().createPort(
			"example set with alignment values (Data Table)");
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
		AttributeMetaData amd1 = new AttributeMetaData(NAMECOL, Ontology.STRING);
		amd1.setRole(AttributeColumn.REGULAR);
		amd1.setNumberOfMissingValues(new MDInteger(0));
		metaData.addAttribute(amd1);
		AttributeMetaData amd2 = new AttributeMetaData(VALUECOL,
				Ontology.INTEGER);
		amd2.setRole(AttributeColumn.REGULAR);
		amd2.setNumberOfMissingValues(new MDInteger(0));
		metaData.addAttribute(amd2);
		metaData.setNumberOfExamples(1);
		getTransformer().addRule(
				new GenerateNewMDRule(outputData, this.metaData));
		// for the alignment
		this.metaData2 = new ExampleSetMetaData();
		AttributeMetaData alignAmd1 = new AttributeMetaData(this.TRACEINDEX,
				Ontology.STRING);
		alignAmd1.setRole(AttributeColumn.REGULAR);
		alignAmd1.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd1);
		AttributeMetaData alignAmd2 = new AttributeMetaData(
				PNRepResult.TRACEFITNESS, Ontology.STRING);
		alignAmd2.setRole(AttributeColumn.REGULAR);
		alignAmd2.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd2);
		AttributeMetaData alignAmd3 = new AttributeMetaData(
				PNRepResult.MOVELOGFITNESS, Ontology.STRING);
		alignAmd3.setRole(AttributeColumn.REGULAR);
		alignAmd3.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd3);
		AttributeMetaData alignAmd4 = new AttributeMetaData(
				PNRepResult.MOVEMODELFITNESS, Ontology.STRING);
		alignAmd4.setRole(AttributeColumn.REGULAR);
		alignAmd4.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd4);
		AttributeMetaData alignAmd5 = new AttributeMetaData(
				PNRepResult.RAWFITNESSCOST, Ontology.STRING);
		alignAmd5.setRole(AttributeColumn.REGULAR);
		alignAmd5.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd5);
		AttributeMetaData alignAmd6 = new AttributeMetaData(
				PNRepResult.NUMSTATEGENERATED, Ontology.STRING);
		alignAmd6.setRole(AttributeColumn.REGULAR);
		alignAmd6.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd6);
		getTransformer().addRule(
				new GenerateNewMDRule(outputAlignment, this.metaData2));
		// for the alignment per trace
		this.metaData3 = new ExampleSetMetaData();
		AttributeMetaData alignAmd11 = new AttributeMetaData(this.TRACEINDEX,
				Ontology.STRING);
		alignAmd11.setRole(AttributeColumn.REGULAR);
		alignAmd11.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd11);
		AttributeMetaData alignAmd12 = new AttributeMetaData(
				PNRepResult.TRACEFITNESS, Ontology.STRING);
		alignAmd12.setRole(AttributeColumn.REGULAR);
		alignAmd12.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd12);
		AttributeMetaData alignAmd13 = new AttributeMetaData(
				PNRepResult.MOVELOGFITNESS, Ontology.STRING);
		alignAmd13.setRole(AttributeColumn.REGULAR);
		alignAmd13.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd3);
		AttributeMetaData alignAmd14 = new AttributeMetaData(
				PNRepResult.MOVEMODELFITNESS, Ontology.STRING);
		alignAmd14.setRole(AttributeColumn.REGULAR);
		alignAmd14.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd14);
		AttributeMetaData alignAmd15 = new AttributeMetaData(
				PNRepResult.RAWFITNESSCOST, Ontology.STRING);
		alignAmd15.setRole(AttributeColumn.REGULAR);
		alignAmd15.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd15);
		AttributeMetaData alignAmd16 = new AttributeMetaData(
				PNRepResult.NUMSTATEGENERATED, Ontology.STRING);
		alignAmd16.setRole(AttributeColumn.REGULAR);
		alignAmd16.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd16);
		getTransformer().addRule(
				new GenerateNewMDRule(outputAlignmentTrace, this.metaData3));
		// md4
		this.metaData4 = new ExampleSetMetaData();
		getTransformer().addRule(
				new GenerateNewMDRule(outputReliable, this.metaData4));
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: replay log on petri net for conformance checking");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(PNLogReplayer.class);

		XLogIOObject xLog = inputLog.getData(XLogIOObject.class);
		PetriNetIOObject pNet = inputPN.getData(PetriNetIOObject.class);

		TransEvClassMapping mapping = getMapping(pNet.getArtifact(),
				xLog.getArtifact());

		IPNReplayParameter parameter = null;
		try {
			parameter = getParameter(mapping);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
		PNLogReplayer replayer = new PNLogReplayer();
		PNRepResult repResult = null;
		IPNReplayAlgorithm algorithm = null;
		try {
			algorithm = getAlgorithm(pluginContext, pNet.getArtifact(),
					xLog.getArtifact(), mapping);
		} catch (ObjectNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			repResult = replayer.replayLog(pluginContext, pNet.getArtifact(),
					xLog.getArtifact(), mapping, algorithm, parameter);
		} catch (AStarException e) {
			e.printStackTrace();
		}

		PNRepResultIOObject result = new PNRepResultIOObject(repResult,
				pluginContext, pNet, xLog.getArtifact(), mapping);
		result.setVisualizationType(PNRepResultIOObjectVisualizationType.PROJECT_ON_MODEL);

		output.deliver(result);

		Iterator<SyncReplayResult> iterator3 = repResult.iterator();
		boolean unreliable = false;
		while (iterator3.hasNext()) {
			SyncReplayResult next = iterator3.next();
			System.out.println("RELIABLE:" + next.isReliable());
			boolean reliable = next.isReliable();
			if (!reliable) {
				unreliable = true;
				break;
			}
		}
		Map<String, Object> info = repResult.getInfo();
		double trace_fitness = 0;
		try {
			trace_fitness = Double.parseDouble((String) info
					.get(PNRepResult.TRACEFITNESS));
		} catch (Exception e) {
			trace_fitness = (Double) info.get(PNRepResult.TRACEFITNESS);
		}
		double move_log_fitness = (Double) info.get(PNRepResult.MOVELOGFITNESS);
		double move_model_fitness = (Double) info
				.get(PNRepResult.MOVEMODELFITNESS);
		double raw_fitness_costs = (Double) info
				.get(PNRepResult.RAWFITNESSCOST);
		double num_state_gen = (Double) info.get(PNRepResult.NUMSTATEGENERATED);

		ExampleSet es = null;
		MemoryExampleTable table = null;
		List<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(AttributeFactory.createAttribute(this.NAMECOL,
				Ontology.STRING));
		attributes.add(AttributeFactory.createAttribute(this.VALUECOL,
				Ontology.NUMERICAL));
		table = new MemoryExampleTable(attributes);
		if (unreliable) {
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
			fillTableWithRow(table, "Generalization", Double.NaN, attributes);
			fillTableWithRow(table, "Precision", Double.NaN, attributes);

		} else {
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

		// output the trace alignment
		ExampleSet es2 = null;
		MemoryExampleTable table2 = null;
		List<Attribute> attributes2 = new LinkedList<Attribute>();
		attributes2.add(AttributeFactory.createAttribute(this.TRACEINDEX,
				Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(
				PNRepResult.TRACEFITNESS, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(
				PNRepResult.MOVELOGFITNESS, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(
				PNRepResult.MOVEMODELFITNESS, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(
				PNRepResult.RAWFITNESSCOST, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(
				PNRepResult.NUMSTATEGENERATED, Ontology.STRING));
		table2 = new MemoryExampleTable(attributes2);
		Iterator<SyncReplayResult> iterator = repResult.iterator();
		while (iterator.hasNext()) {
			SyncReplayResult next = iterator.next();
			DataRowFactory factory = new DataRowFactory(
					DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
			Object[] vals = new Object[6];
			vals[0] = next.getTraceIndex().toString();
			vals[1] = Double.toString(next.getInfo().get(
					PNRepResult.TRACEFITNESS));
			vals[2] = Double.toString(next.getInfo().get(
					PNRepResult.MOVELOGFITNESS));
			vals[3] = Double.toString(next.getInfo().get(
					PNRepResult.MOVEMODELFITNESS));
			vals[4] = Double.toString(next.getInfo().get(
					PNRepResult.RAWFITNESSCOST));
			vals[5] = Double.toString(next.getInfo().get(
					PNRepResult.NUMSTATEGENERATED));

			Attribute[] attribArray = new Attribute[attributes2.size()];
			for (int i = 0; i < attributes2.size(); i++) {
				attribArray[i] = attributes2.get(i);
			}
			DataRow dataRow = factory.create(vals, attribArray);
			table2.addDataRow(dataRow);
		}
		es2 = table2.createExampleSet();
		// add to list so that afterwards it can be cleared if needed
		outputAlignment.deliver(es2);

		// create the third exampleset
		ExampleSet es3 = null;
		MemoryExampleTable table3 = null;
		List<Attribute> attributes3 = new LinkedList<Attribute>();
		attributes3.add(AttributeFactory.createAttribute(this.TRACEIDENTIFIER,
				Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(
				PNRepResult.TRACEFITNESS, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(
				PNRepResult.MOVELOGFITNESS, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(
				PNRepResult.MOVEMODELFITNESS, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(
				PNRepResult.RAWFITNESSCOST, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(
				PNRepResult.NUMSTATEGENERATED, Ontology.STRING));
		table3 = new MemoryExampleTable(attributes3);
		Iterator<SyncReplayResult> iterator2 = repResult.iterator();
		while (iterator2.hasNext()) {
			SyncReplayResult next = iterator2.next();
			DataRowFactory factory = new DataRowFactory(
					DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
			Object[] vals = new Object[6];
			vals[1] = Double.toString(next.getInfo().get(
					PNRepResult.TRACEFITNESS));
			vals[2] = Double.toString(next.getInfo().get(
					PNRepResult.MOVELOGFITNESS));
			vals[3] = Double.toString(next.getInfo().get(
					PNRepResult.MOVEMODELFITNESS));
			vals[4] = Double.toString(next.getInfo().get(
					PNRepResult.RAWFITNESSCOST));
			vals[5] = Double.toString(next.getInfo().get(
					PNRepResult.NUMSTATEGENERATED));
			// convert the list to array
			Attribute[] attribArray = new Attribute[attributes3.size()];
			for (int i = 0; i < attributes3.size(); i++) {
				attribArray[i] = attributes3.get(i);
			}
			List<Integer> listArray = convertIntListToArray(next
					.getTraceIndex().toString());
			for (Integer s : listArray) {
				// get the right trace
				XTrace xTrace = xLog.getArtifact().get(s);
				String name = XConceptExtension.instance().extractName(xTrace);
				vals[0] = name;
				DataRow dataRow = factory.create(vals, attribArray);
				table3.addDataRow(dataRow);
			}
		}
		es3 = table3.createExampleSet();
		outputAlignmentTrace.deliver(es3);
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

		logger.log(Level.INFO,
				"End: replay log on petri net for conformance checking ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
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

	private TransEvClassMapping getMapping(Petrinet net, XLog log) {
		XLogInfo infoLog = XLogInfoFactory.createLogInfo(log,
				XLogInfoImpl.STANDARD_CLASSIFIER);
		XEventClasses ecLog = infoLog.getEventClasses();
		// create mapping for each transition to the event class of the repaired
		// log
		XEventClass evClassDummy = new XEventClass("DUMMY", -1);
		TransEvClassMapping mappingTransEvClass = new TransEvClassMapping(
				XLogInfoImpl.STANDARD_CLASSIFIER, evClassDummy);
		Iterator<Transition> transIt2 = net.getTransitions().iterator();
		while (transIt2.hasNext()) {
			Transition trans = transIt2.next();
			// System.out.println(trans.getId() + ":" + trans.getLabel());
			if (trans.getLabel().startsWith("tr") || trans.isInvisible()) {
				trans.setInvisible(true);
				mappingTransEvClass.put(trans, evClassDummy);
			} else {
				// search for event which starts with transition name
				for (XTrace trace : log) {
					for (XEvent evt : trace) {
						XEventClass ec = ecLog.getClassOf(evt);
						if (ec.getId().startsWith(trans.getLabel())) {
							// found the one
							mappingTransEvClass.put(trans, ec);
							break;
						}
					}
				}
			}
		}
		return mappingTransEvClass;
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

	private IPNReplayParameter getParameter(TransEvClassMapping map)
			throws UserError, ObjectNotFoundException {

		IPNReplayParameter parameter = null;
		switch (getParameterAsInt(PARAMETER_1)) {
		case 0:
		case 1:
		case 2:
			parameter = new CostBasedCompleteParam(map.values(),
					map.getDummyEventClass(), map.keySet(), 1, 1);
			break;
		case 3:
			parameter = new CostBasedPrefixParam();
			break;
		}

		parameter.setInitialMarking(inputPN.getData(PetriNetIOObject.class)
				.getInitialMarking());
		parameter.setFinalMarkings(getFinalMarking(inputPN.getData(
				PetriNetIOObject.class).getArtifact()));
		return parameter;
	}

	private IPNReplayAlgorithm getAlgorithm(PluginContext pc, Petrinet pn,
			XLog log, TransEvClassMapping mapping) throws UserError,
			ObjectNotFoundException {

		IPNReplayAlgorithm algorithm = null;
		switch (getParameterAsInt(PARAMETER_1)) {
		case 0:
			algorithm = new CostBasedCompletePruneAlg();
			break;
		case 1:
			algorithm = new PetrinetReplayerWithoutILP();
			break;
		case 2:
			algorithm = new PrefixBasedPetrinetReplayer();
			break;
		}
		if (algorithm.isAllReqSatisfied(pc, pn, log, mapping,
				getParameter(mapping)))
			return algorithm;
		else
			return null;
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

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeCategory parameterType1 = new ParameterTypeCategory(
				PARAMETER_1, PARAMETER_1, ALGORITHMS, 0);
		parameterTypes.add(parameterType1);

		return parameterTypes;
	}

}
