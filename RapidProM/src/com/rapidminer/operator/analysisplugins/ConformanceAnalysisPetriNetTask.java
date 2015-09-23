package com.rapidminer.operator.analysisplugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.rapidprom.prom.CallProm;

import com.rapidminer.callprom.ClassLoaderUtils;
import com.rapidminer.configuration.GlobalProMParameters;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.conversionplugins.NameListToPetrinetTask;
import com.rapidminer.operator.conversionplugins.NameListToPetrinetTask.NameComparator;
import com.rapidminer.operator.io.AbstractDataReader.AttributeColumn;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.parameters.ParameterBoolean;
import com.rapidminer.parameters.ParameterInteger;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.util.ProMIOObjectList;
import com.rapidminer.util.Utilities;

public class ConformanceAnalysisPetriNetTask extends Operator {

	private final String NAMECOL = "Name";
	private final String VALUECOL = "Value";
	
	// alignment
	private final String TRACEIDENTIFIER = "Trace Identifier";
	private final String TRACEINDEX = "Trace Index";
	private final String NODEINSTANCE = "Node Instance";
	private final String STEPTYPES = "Step Types";
	private final String RELIABLE = "Unreliable Alignments Exist";
	
	private List<Parameter> parameters = null;
	
	/** defining the ports */
	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private InputPort inputPN = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort output = getOutputPorts().createPort("example set with metrics (Data Table)");
	private OutputPort outputAlignment = getOutputPorts().createPort("example set with alignment values (Data Table)");
	private OutputPort outputAlignmentTrace = getOutputPorts().createPort("example set with alignment values per trace (Data Table)");
	private OutputPort outputReliable = getOutputPorts().createPort("example set with indicator for reliable traces (Data Table)");
	private OutputPort outputActivityAlignment = getOutputPorts().createPort("example set with activity alignments (Data Table)");
	
	private ExampleSetMetaData metaData = null;
	private ExampleSetMetaData metaData2 = null;
	private ExampleSetMetaData metaData3 = null;
	private ExampleSetMetaData metaData4 = null;

	public ConformanceAnalysisPetriNetTask(OperatorDescription description) {
		super(description);
		// hack 
		//loadRequiredClasses();
		/** Adding a rule for the output */
		this.metaData = new ExampleSetMetaData();
		AttributeMetaData amd1 = new AttributeMetaData(NAMECOL, Ontology.STRING);
		amd1.setRole(AttributeColumn.REGULAR);
		amd1.setNumberOfMissingValues(new MDInteger(0));
		metaData.addAttribute(amd1);
		AttributeMetaData amd2 = new AttributeMetaData(VALUECOL, Ontology.INTEGER);
		amd2.setRole(AttributeColumn.REGULAR);
		amd2.setNumberOfMissingValues(new MDInteger(0));
		metaData.addAttribute(amd2);
		metaData.setNumberOfExamples(1);
		getTransformer().addRule( new GenerateNewMDRule(output, this.metaData));
		// for the alignment
		this.metaData2 = new ExampleSetMetaData();
		AttributeMetaData alignAmd1 = new AttributeMetaData(this.TRACEINDEX, Ontology.STRING);
		alignAmd1.setRole(AttributeColumn.REGULAR);
		alignAmd1.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd1);
		AttributeMetaData alignAmd2 = new AttributeMetaData(PNRepResult.TRACEFITNESS, Ontology.STRING);
		alignAmd2.setRole(AttributeColumn.REGULAR);
		alignAmd2.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd2);
		AttributeMetaData alignAmd3 = new AttributeMetaData(PNRepResult.MOVELOGFITNESS, Ontology.STRING);
		alignAmd3.setRole(AttributeColumn.REGULAR);
		alignAmd3.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd3);
		AttributeMetaData alignAmd4 = new AttributeMetaData(PNRepResult.MOVEMODELFITNESS, Ontology.STRING);
		alignAmd4.setRole(AttributeColumn.REGULAR);
		alignAmd4.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd4);
		AttributeMetaData alignAmd5 = new AttributeMetaData(PNRepResult.RAWFITNESSCOST, Ontology.STRING);
		alignAmd5.setRole(AttributeColumn.REGULAR);
		alignAmd5.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd5);
		AttributeMetaData alignAmd6 = new AttributeMetaData(PNRepResult.NUMSTATEGENERATED, Ontology.STRING);
		alignAmd6.setRole(AttributeColumn.REGULAR);
		alignAmd6.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd6);
		AttributeMetaData alignAmd7 = new AttributeMetaData(PNRepResult.QUEUEDSTATE, Ontology.STRING);
		alignAmd7.setRole(AttributeColumn.REGULAR);
		alignAmd7.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd7);
		AttributeMetaData alignAmd8 = new AttributeMetaData(this.NODEINSTANCE, Ontology.STRING);
		alignAmd8.setRole(AttributeColumn.REGULAR);
		alignAmd8.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd8);
		AttributeMetaData alignAmd9 = new AttributeMetaData(this.STEPTYPES, Ontology.STRING);
		alignAmd9.setRole(AttributeColumn.REGULAR);
		alignAmd9.setNumberOfMissingValues(new MDInteger(0));
		metaData2.addAttribute(alignAmd9);
		getTransformer().addRule( new GenerateNewMDRule(outputAlignment, this.metaData2));
		// for the alignment per trace
		this.metaData3 = new ExampleSetMetaData();
		AttributeMetaData alignAmd11 = new AttributeMetaData(this.TRACEINDEX, Ontology.STRING);
		alignAmd11.setRole(AttributeColumn.REGULAR);
		alignAmd11.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd11);
		AttributeMetaData alignAmd12 = new AttributeMetaData(PNRepResult.TRACEFITNESS, Ontology.STRING);
		alignAmd12.setRole(AttributeColumn.REGULAR);
		alignAmd12.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd12);
		AttributeMetaData alignAmd13 = new AttributeMetaData(PNRepResult.MOVELOGFITNESS, Ontology.STRING);
		alignAmd13.setRole(AttributeColumn.REGULAR);
		alignAmd13.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd3);
		AttributeMetaData alignAmd14 = new AttributeMetaData(PNRepResult.MOVEMODELFITNESS, Ontology.STRING);
		alignAmd14.setRole(AttributeColumn.REGULAR);
		alignAmd14.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd14);
		AttributeMetaData alignAmd15 = new AttributeMetaData(PNRepResult.RAWFITNESSCOST, Ontology.STRING);
		alignAmd15.setRole(AttributeColumn.REGULAR);
		alignAmd15.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd15);
		AttributeMetaData alignAmd16 = new AttributeMetaData(PNRepResult.NUMSTATEGENERATED, Ontology.STRING);
		alignAmd16.setRole(AttributeColumn.REGULAR);
		alignAmd16.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd16);
		AttributeMetaData alignAmd17 = new AttributeMetaData(PNRepResult.QUEUEDSTATE, Ontology.STRING);
		alignAmd17.setRole(AttributeColumn.REGULAR);
		alignAmd17.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd17);
		AttributeMetaData alignAmd18 = new AttributeMetaData(this.NODEINSTANCE, Ontology.STRING);
		alignAmd18.setRole(AttributeColumn.REGULAR);
		alignAmd18.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd18);
		AttributeMetaData alignAmd19 = new AttributeMetaData(this.STEPTYPES, Ontology.STRING);
		alignAmd19.setRole(AttributeColumn.REGULAR);
		alignAmd19.setNumberOfMissingValues(new MDInteger(0));
		metaData3.addAttribute(alignAmd19);
		getTransformer().addRule( new GenerateNewMDRule(outputAlignmentTrace, this.metaData3));
		// md4
		this.metaData4 = new ExampleSetMetaData();
		AttributeMetaData amdRel1 = new AttributeMetaData(NAMECOL, Ontology.STRING);
		amdRel1.setRole(AttributeColumn.REGULAR);
		amdRel1.setNumberOfMissingValues(new MDInteger(0));
		metaData4.addAttribute(amdRel1);
		AttributeMetaData amdRel2 = new AttributeMetaData(VALUECOL, Ontology.INTEGER);
		amdRel2.setRole(AttributeColumn.REGULAR);
		amdRel2.setNumberOfMissingValues(new MDInteger(0));
		metaData4.addAttribute(amdRel2);
		metaData4.setNumberOfExamples(1);
		getTransformer().addRule( new GenerateNewMDRule(outputReliable, this.metaData4));
	}
	
	@Override
	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do Conformance analysis", LogService.NOTE);
		// get the plugincontext
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		// get the log
		XLogIOObject log = inputLog.getData(XLogIOObject.class);
		XLog promLog = log.getPromLog();
		// get the petri net
		PetriNetIOObject data = inputPN.getData(PetriNetIOObject.class);
		Petrinet pn = data.getPn();
		// run the plugin
		int maxNumberStates = getConfigurationMaxNumberStates(parameters);
		boolean noFinalMarking = getConfigurationFinalMarking(parameters);
		List<Object> pars = new ArrayList<Object>();
		pars.add(pn);
		pars.add(promLog);
		pars.add(maxNumberStates);
		pars.add(noFinalMarking);
		CallProm tp = new CallProm();
		Object[] runPlugin = tp.runPlugin(pluginContext, "10", "Replay a Log on Petri Net for Conformance Analysis", pars);
		PNRepResult repResult = (PNRepResult) runPlugin[0];
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
		double trace_fitness = (Double) info.get(PNRepResult.TRACEFITNESS);
		double move_log_fitness = (Double) info.get(PNRepResult.MOVELOGFITNESS);
		double move_model_fitness = (Double) info.get(PNRepResult.MOVEMODELFITNESS);
		double raw_fitness_costs = (Double) info.get(PNRepResult.RAWFITNESSCOST);
		double num_state_gen = (Double) info.get(PNRepResult.NUMSTATEGENERATED);
		double queuedstate = (Double) info.get(PNRepResult.QUEUEDSTATE);
		// now get precision and generalization, plugin Measure Precision/Generalization
		boolean configuration = getConfiguration(parameters);
		double generalization = 0.0;
		double precision = 0.0;
		if (configuration) {
			TransEvClassMapping mapping = getMapping(pn,promLog);
			Marking initMarking = getInitMarking(pn);
			List<Object> pars2 = new ArrayList<Object>();
			pars2.add(pn);
			pars2.add(promLog);
			pars2.add(repResult);
			pars2.add(mapping);
			pars2.add(initMarking);
			Object[] runPlugin2 = tp.runPlugin(pluginContext, "101", "Measure Precision/Generalization", pars2);
			AlignmentPrecGenRes precGenRes = (AlignmentPrecGenRes) runPlugin2[0];
			generalization = precGenRes.getGeneralization();
			precision = precGenRes.getPrecision();
		}
		// put this in the example set
		ExampleSet es = null;
		MemoryExampleTable table = null;
		List<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(AttributeFactory.createAttribute(this.NAMECOL, Ontology.STRING));
		attributes.add(AttributeFactory.createAttribute(this.VALUECOL, Ontology.NUMERICAL));
		table = new MemoryExampleTable(attributes);
		if (unreliable) {
			fillTableWithRow(table, PNRepResult.TRACEFITNESS, Double.NaN, attributes);
			fillTableWithRow(table, PNRepResult.MOVELOGFITNESS, Double.NaN, attributes);
			fillTableWithRow(table, PNRepResult.MOVEMODELFITNESS, Double.NaN, attributes);
			fillTableWithRow(table, PNRepResult.RAWFITNESSCOST, Double.NaN, attributes);
			fillTableWithRow(table, PNRepResult.NUMSTATEGENERATED, Double.NaN, attributes);
			fillTableWithRow(table, PNRepResult.QUEUEDSTATE, Double.NaN, attributes);
			fillTableWithRow(table, "Generalization", Double.NaN, attributes);
			fillTableWithRow(table, "Precision", Double.NaN, attributes);
			//fillTableWithRow(table, "Unreliable Alignments Exist", unreliable, attributes);
		}
		else {
			fillTableWithRow(table, PNRepResult.TRACEFITNESS, trace_fitness, attributes);
			fillTableWithRow(table, PNRepResult.MOVELOGFITNESS, move_log_fitness, attributes);
			fillTableWithRow(table, PNRepResult.MOVEMODELFITNESS, move_model_fitness, attributes);
			fillTableWithRow(table, PNRepResult.RAWFITNESSCOST, raw_fitness_costs, attributes);
			fillTableWithRow(table, PNRepResult.NUMSTATEGENERATED, num_state_gen, attributes);
			fillTableWithRow(table, PNRepResult.QUEUEDSTATE, queuedstate, attributes);
			fillTableWithRow(table, "Generalization", generalization, attributes);
			fillTableWithRow(table, "Precision", precision, attributes);
		}
		es = table.createExampleSet();
		output.deliver(es);
		
		// output the trace alignment
		ExampleSet es2 = null;
		MemoryExampleTable table2 = null;
		List<Attribute> attributes2 = new LinkedList<Attribute>();
		attributes2.add(AttributeFactory.createAttribute(this.TRACEINDEX, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(PNRepResult.TRACEFITNESS, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(PNRepResult.MOVELOGFITNESS, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(PNRepResult.MOVEMODELFITNESS, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(PNRepResult.RAWFITNESSCOST, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(PNRepResult.NUMSTATEGENERATED, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(PNRepResult.QUEUEDSTATE, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(this.NODEINSTANCE, Ontology.STRING));
		attributes2.add(AttributeFactory.createAttribute(this.STEPTYPES, Ontology.STRING));
		table2 = new MemoryExampleTable(attributes2); 		
		Iterator<SyncReplayResult> iterator = repResult.iterator();
		while (iterator.hasNext()) {
			SyncReplayResult next = iterator.next();
			DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
			Object[] vals = new Object[9];
			vals[0] = next.getTraceIndex().toString();
			vals[1] = Double.toString(next.getInfo().get(PNRepResult.TRACEFITNESS));
			vals[2] = Double.toString(next.getInfo().get(PNRepResult.MOVELOGFITNESS));
			vals[3] = Double.toString(next.getInfo().get(PNRepResult.MOVEMODELFITNESS));
			vals[4] = Double.toString(next.getInfo().get(PNRepResult.RAWFITNESSCOST));
			vals[5] = Double.toString(next.getInfo().get(PNRepResult.NUMSTATEGENERATED));
			vals[6] = Double.toString(next.getInfo().get(PNRepResult.QUEUEDSTATE));
			vals[7] = next.getNodeInstance().toString();
			vals[8] = next.getStepTypes().toString();
			// convert the list to array
			Attribute[] attribArray = new Attribute[attributes2.size()];
			for (int i=0; i<attributes2.size(); i++) {
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
		attributes3.add(AttributeFactory.createAttribute(this.TRACEIDENTIFIER, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(PNRepResult.TRACEFITNESS, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(PNRepResult.MOVELOGFITNESS, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(PNRepResult.MOVEMODELFITNESS, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(PNRepResult.RAWFITNESSCOST, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(PNRepResult.NUMSTATEGENERATED, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(PNRepResult.QUEUEDSTATE, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(this.NODEINSTANCE, Ontology.STRING));
		attributes3.add(AttributeFactory.createAttribute(this.STEPTYPES, Ontology.STRING));
		table3 = new MemoryExampleTable(attributes3); 		
		Iterator<SyncReplayResult> iterator2 = repResult.iterator();
		while (iterator2.hasNext()) {
			SyncReplayResult next = iterator2.next();
			DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
			Object[] vals = new Object[9];
			vals[1] = Double.toString(next.getInfo().get(PNRepResult.TRACEFITNESS));
			vals[2] = Double.toString(next.getInfo().get(PNRepResult.MOVELOGFITNESS));
			vals[3] = Double.toString(next.getInfo().get(PNRepResult.MOVEMODELFITNESS));
			vals[4] = Double.toString(next.getInfo().get(PNRepResult.RAWFITNESSCOST));
			vals[5] = Double.toString(next.getInfo().get(PNRepResult.NUMSTATEGENERATED));
			vals[6] = Double.toString(next.getInfo().get(PNRepResult.QUEUEDSTATE));
			vals[7] = next.getNodeInstance().toString();
			vals[8] = next.getStepTypes().toString();
			// convert the list to array
			Attribute[] attribArray = new Attribute[attributes3.size()];
			for (int i=0; i<attributes3.size(); i++) {
				attribArray[i] = attributes3.get(i);
			}
			List<Integer> listArray = convertIntListToArray(next.getTraceIndex().toString());
			for (Integer s : listArray) {
				// get the right trace
				XTrace xTrace = promLog.get(s);
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
		attributes4.add(AttributeFactory.createAttribute(this.NAMECOL, Ontology.STRING));
		attributes4.add(AttributeFactory.createAttribute(this.VALUECOL, Ontology.STRING));
		table4 = new MemoryExampleTable(attributes4);
		fillTableWithRow(table4, RELIABLE, Boolean.toString(unreliable), attributes4);
		es4 = table4.createExampleSet();
		outputReliable.deliver(es4);

		//create the fifth exa!! Moves per activity
		
		Iterator<SyncReplayResult> iterator4 = repResult.iterator();
		
		System.out.println("Number of SyncReplayResult: " + repResult.size());
		
		Map<String,Integer> moves_log = new HashMap<String,Integer>();
		Map<String,Integer> moves_mod = new HashMap<String,Integer>();
		Map<String,Integer> moves_sync = new HashMap<String,Integer>();
		
		SortedSet<String> sorted = new TreeSet<String>(new NameComparator());
		
		while(iterator4.hasNext())
		{
			SyncReplayResult next = iterator4.next();
			
			List<Object> steps = next.getNodeInstance();
			List<StepTypes> types = next.getStepTypes();
			
			int weight = next.getTraceIndex().size(); //number of traces in this specific alignment
			
			int LMgood = 0;	//counters to print on console
			int LMnogood = 0;
			int L = 0;
			int Minvi = 0;
			int Mreal = 0;
			int LMreplaced = 0;
			int LMswapped = 0;
			
			for(int i = 0 ; i < steps.size() ; i++) //fill the maps
			{
				String key = steps.get(i).toString().trim();
				if(key.endsWith("+"))
						key = key.substring(0, key.length()-1); // remove the '+' char
				
				StepTypes type = types.get(i);				
				
				
				if(type == StepTypes.LMGOOD)
					LMgood++;
				if(type == StepTypes.LMNOGOOD)
					LMnogood++;
				if(type == StepTypes.L)
					L++;
				if(type == StepTypes.MINVI)
					Minvi++;
				if(type == StepTypes.MREAL)
					Mreal++;
				if(type == StepTypes.LMREPLACED)
					LMreplaced++;
				if(type == StepTypes.LMSWAPPED)
					LMswapped++;
				
				
				
				if(type == StepTypes.L) 	//it was a move on log
				{
					if(moves_log.containsKey(key))
						moves_log.put(key, moves_log.get(key) + weight); 
					else
						moves_log.put(key, weight);
					
				}
				else if(type == StepTypes.MREAL) //it was a move on model (not invisible transition)
				{
					if(moves_mod.containsKey(key))
						moves_mod.put(key, moves_mod.get(key) + weight); 
					else
						moves_mod.put(key, weight);
				}
				else if(type == StepTypes.LMGOOD) //it was a synchronous move
				{
					if(moves_sync.containsKey(key))
						moves_sync.put(key, moves_sync.get(key) + weight); 
					else
						moves_sync.put(key, weight);
				}
			}
			
			System.out.println("Step " + weight + ": LMGOOD = " + LMgood + ", LMNOGOOD = " + LMnogood + ", L = " + L + ", MINVI = " + Minvi + ", MREAL = " + Mreal + ", LMREPLACED = " + LMreplaced + ", LMSWAPPED = " + LMswapped);
			//now consolidate the maps in a data table
			
			//first create the list of activities
			
			for(String s : moves_log.keySet())
				sorted.add(s);
			for(String s : moves_mod.keySet())
				sorted.add(s);
			for(String s : moves_sync.keySet())
				sorted.add(s);
			
			
			
			/*System.out.println("Steps: " + steps.size() + ", Types: " + types.size() );
			for(int i = 0 ; i < steps.size() ; i++)
			{
				System.out.println("Label: " + steps.get(i).toString() + ", Type: " + types.get(i).toString());
			}*/
		}
		//now create the consolidated matrix
		
		Object[][] moves = new Object[sorted.size()][4];
		
		Object[] names =  sorted.toArray();
		for(int i = 0 ; i < moves.length ; i++)
		{
			String name = (String) names[i];
			
			moves[i][0] = name; //activity name 
					
			if(moves_log.containsKey(name)) //moves on log
				moves[i][1] = moves_log.get(name);
			else
				moves[i][1] = 0;
			
			if(moves_mod.containsKey(name)) //moves on model
				moves[i][2] = moves_mod.get(name);
			else
				moves[i][2] = 0;
			
			if(moves_sync.containsKey(name)) //synchronous moves
				moves[i][3] = moves_sync.get(name);
			else
				moves[i][3] = 0;
			
			
		}
		// get to the choppa!!
		
		ExampleSet es5 =	ExampleSetFactory.createExampleSet(moves);
		outputActivityAlignment.deliver(es5);
	}
	
	private List<Integer> convertIntListToArray(String s) {
		List<Integer> result = new ArrayList<Integer>();
		s = s.replace("[", "");
		s = s.replace("]", "");
		String[] split = s.split(",");
		for (int i=0; i<split.length; i++) {
			String string = split[i];
			String trim = string.trim();
			Integer in = Integer.parseInt(trim);
			result.add(in);
		}
		return result;
	}

	private TransEvClassMapping getMapping(Petrinet net, XLog log ) {
		XLogInfo infoLog = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.STANDARD_CLASSIFIER);
		XEventClasses ecLog = infoLog.getEventClasses();
		// create mapping for each transition to the event class of the repaired log
		XEventClass evClassDummy = new XEventClass("DUMMY", -1);
		TransEvClassMapping mappingTransEvClass = new TransEvClassMapping(XLogInfoImpl.STANDARD_CLASSIFIER,
				evClassDummy);
		Iterator<Transition> transIt2 = net.getTransitions().iterator();
		while (transIt2.hasNext()) {
			Transition trans = transIt2.next();
			//System.out.println(trans.getId() + ":" + trans.getLabel());
			if (trans.getLabel().startsWith("tr")  || trans.isInvisible()) {
				trans.setInvisible(true);
				mappingTransEvClass.put(trans, evClassDummy);
			}
			else{
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

	private Marking getInitMarking(Petrinet pn) {
		List<Place> places = new ArrayList<Place>();
		Iterator<Place> placesIt = pn.getPlaces().iterator();
		while (placesIt.hasNext()) {
			Place nextPlace = placesIt.next();
			Collection inEdges = pn.getInEdges(nextPlace);
			if (inEdges.isEmpty()) {
				places.add(nextPlace);
			}
		}
		Marking initialMarking = new Marking();
		for (Place place : places) {
			initialMarking.add(place);
		}
		return initialMarking;
	}

	private void fillTableWithRow (MemoryExampleTable table, String name, Object value, List<Attribute> attributes) {		
		// fill table
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
		Object[] vals = new Object[2];
		vals[0] = name;
		vals[1] = value;
		// convert the list to array
		Attribute[] attribArray = new Attribute[attributes.size()];
		for (int i=0; i<attributes.size(); i++) {
			attribArray[i] = attributes.get(i);
		}
		DataRow dataRow = factory.create(vals, attribArray);
		table.addDataRow(dataRow);
	}
	
	public List<ParameterType> getParameterTypes() {
		this.parameters = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterBoolean parameter1 = new ParameterBoolean(true, Boolean.class, "Calculate Precision / Generalization", "Precision / Generalization");
		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parameters.add(parameter1);
		
		ParameterInteger parameter2 = new ParameterInteger(100, 1, Integer.MAX_VALUE, 1, Integer.class, "Max Explored States (in Hundreds)", "Maximum Explored States");
		ParameterTypeInt parameterType2 = new ParameterTypeInt(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getMin(), parameter2.getMax(), parameter2.getDefaultValueParameter());
		parameterTypes.add(parameterType2);
		parameters.add(parameter2);
		
		ParameterBoolean parameter3 = new ParameterBoolean(true, Boolean.class, "Create No Final Marking", "No Final Marking");
		ParameterTypeBoolean parameterType3 = new ParameterTypeBoolean(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getDefaultValueParameter());
		parameterTypes.add(parameterType3);
		parameters.add(parameter3);
		
		return parameterTypes;
	}
	
	private boolean getConfiguration(List<Parameter> parameters) {
		Parameter parameter1 = parameters.get(0);
		Boolean valPar1 = getParameterAsBoolean(parameter1.getNameParameter());
		return valPar1;
	}
	
	private int getConfigurationMaxNumberStates(List<Parameter> parameters) {
		Parameter parameter2 = parameters.get(1);
		Integer valPar2 = 1;
		try {
			valPar2 = getParameterAsInt(parameter2.getNameParameter());
		} catch (UndefinedParameterError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valPar2;
	}
	
	private boolean getConfigurationFinalMarking(List<Parameter> parameters) {
		Parameter parameter3 = parameters.get(2);
		Boolean valPar3 = false;
		valPar3 = getParameterAsBoolean(parameter3.getNameParameter());
		return valPar3;
	}
	
	private void loadRequiredClasses () {
		Utilities.loadRequiredClasses();	
	}
	
	public static void main(String [] args)	{
		String s = "[1, 2, 6, 12, 13, 19, 41, 51, 57, 69, 75, 79, 81, 83, 84, 91, 98]";
		s = s.replace("[", "");
		s = s.replace("]", "");
		String[] split = s.split(",");
		for (int i=0; i<split.length; i++) {
			String string = split[i];
			String trim = string.trim();
		}
		System.out.println();
	}

}
