package com.rapidminer.operator.conversionplugins;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
//import org.processmining.models.xes.XEventPassageClassifier;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.util.ProMIOObjectList;

public class ExampleSetToXLog  extends Operator {
	
	private final String CONCEPT_NAME_TRACE = "T:concept:name";
	private final String CONCEPT_NAME_EVENT = "E:concept:name";
	private final String LIFECYCLE_TRANSITION_EVENT = "E:lifecycle:transition";
	private final String TIME_TIMESTAMP_EVENT = "E:time:timestamp";
	private final String ORG_RESOURCE_EVENT = "E:org:resource";
	private final String ORG_ROLE_EVENT = "E:org:role";
	private final String ORG_GROUP_EVENT = "E:group:resource";
	
	/** defining the ports */
	private InputPort inputExampleSet = getInputPorts().createPort("example set (Data Table)", new ExampleSetMetaData());
	private OutputPort outputLog = getOutputPorts().createPort("event log (ProM Event Log)");
	private XLogIOObject log = null;
	
	/**
	 * The default constructor needed in exactly this signature
	 */
	public ExampleSetToXLog(OperatorDescription description) {
		super(description);
		
		/** Adding a rule for the output */
		this.log = new XLogIOObject();
		getTransformer().addRule( new GenerateNewMDRule(outputLog, log.getClass()));
		
		inputExampleSet.addPrecondition(new ExampleSetPrecondition(inputExampleSet, 
				new String[] { CONCEPT_NAME_TRACE, CONCEPT_NAME_EVENT  }, 
				Ontology.STRING));
		inputExampleSet.addPrecondition(new ExampleSetPrecondition(inputExampleSet,
				new String[] {TIME_TIMESTAMP_EVENT},
				Ontology.DATE_TIME));
		
	}
	
	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do transformation ExampleSet to XLog", LogService.NOTE);
		// get the exampleset
		ExampleSet data = inputExampleSet.getData(ExampleSet.class);
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XAttributeLiteral attribNameLog = factory.createAttributeLiteral("concept:name", "log created by RapidMiner", XConceptExtension.instance());
		XAttributeLiteral attribLifecycleLog = factory.createAttributeLiteral("lifecycle:model", "standard", XLifecycleExtension.instance());
		XAttributeMap attribMapLog = new XAttributeMapImpl();
		attribMapLog.put("concept:name", attribNameLog);
		attribMapLog.put("lifecycle:model", attribLifecycleLog);
		XLog log = factory.createLog(attribMapLog);
		
		//log.getClassifiers().add(new XEventAttributeClassifier("Activity","concept:name"));
		//log.getClassifiers().add(new XEventAttributeClassifier("Another","concept:name system"));
		log.getExtensions().add(XConceptExtension.instance());
		log.getExtensions().add(XOrganizationalExtension.instance());
		log.getExtensions().add(XTimeExtension.instance());
		log.getExtensions().add(XLifecycleExtension.instance());
		log.getGlobalTraceAttributes().add(new XAttributeLiteralImpl("concept:name", "__INVALID__"));
		log.getGlobalEventAttributes().add(new XAttributeLiteralImpl("system", "__INVALID__"));
		log.getGlobalEventAttributes().add(new XAttributeTimestampImpl("time:timestamp", 0));

		// iterate over traces and events
		Iterator<Example> iterator = data.iterator();
		Map<String,XTrace> mapping = new HashMap<String,XTrace>();
		while (iterator.hasNext()) {
			Example ex = iterator.next();
			String nameTrace = ex.getValueAsString(data.getAttributes().get(CONCEPT_NAME_TRACE));
			XTrace xTrace = mapping.get(nameTrace);
			if (xTrace==null) {
				XAttributeLiteral attribNameTrace = factory.createAttributeLiteral("concept:name", nameTrace, XConceptExtension.instance());
				XAttributeMap attribMapTrace = new XAttributeMapImpl();
				attribMapTrace.put("concept:name", attribNameTrace);
				XTrace trace = factory.createTrace(attribMapTrace);
				log.add(trace);
				xTrace = trace;
				mapping.put(nameTrace, xTrace);
			}
			// add event
			XAttributeMap attribMapEvent = new XAttributeMapImpl();
			Iterator<Attribute> iterator2 = data.getAttributes().iterator();
			while (iterator2.hasNext()) {
				Attribute next = iterator2.next();
				if (next.getName().equals(CONCEPT_NAME_TRACE)) {
					// do nothing
				}
				else if (next.getName().equals(CONCEPT_NAME_EVENT)) {
					String value = ex.getValueAsString(next);
					XAttributeLiteral attribNameEvent = factory.createAttributeLiteral("concept:name", value, XConceptExtension.instance());
					attribMapEvent.put("concept:name", attribNameEvent);
				}
				else if (next.getName().equals(LIFECYCLE_TRANSITION_EVENT)) {
					String value = ex.getValueAsString(next);
					XAttributeLiteral attribNameEvent = factory.createAttributeLiteral("lifecycle:transition", value, XLifecycleExtension.instance());
					attribMapEvent.put("lifecycle:transition", attribNameEvent);
				}
				else if (next.getName().equals(TIME_TIMESTAMP_EVENT)) {
					Date dateValue = ex.getDateValue(next);
					XAttributeTimestamp attribTimestampEvent = factory.createAttributeTimestamp("time:timestamp", dateValue, XTimeExtension.instance());
					attribMapEvent.put("time:timestamp", attribTimestampEvent);
				}
				else if (next.getName().equals(ORG_RESOURCE_EVENT)) {
					String value = ex.getValueAsString(next);
					XAttributeLiteral attribNameEvent = factory.createAttributeLiteral("org:resource", value, XOrganizationalExtension.instance());
					attribMapEvent.put("org:resource", attribNameEvent);
				}
				else if (next.getName().equals(ORG_ROLE_EVENT)) {
					String value = ex.getValueAsString(next);
					XAttributeLiteral attribNameEvent = factory.createAttributeLiteral("org:role", value, XOrganizationalExtension.instance());
					attribMapEvent.put("org:role", attribNameEvent);
				}
				else if (next.getName().equals(ORG_GROUP_EVENT)) {
					String value = ex.getValueAsString(next);
					XAttributeLiteral attribNameEvent = factory.createAttributeLiteral("group:resource", value, XOrganizationalExtension.instance());
					attribMapEvent.put("group:resource", attribNameEvent);
				}
				else {
					// event with other name
					// remove "E:" / "T:" from the attribute
					String newName = next.getName();
					if (newName.startsWith("E:")) {
						newName = next.getName().replaceFirst("E:", "");
					}
					else if (newName.startsWith("T:")) {
						newName = next.getName().replaceFirst("T:", "");
					}
					else {
						// do nothing
					}
					String value = ex.getValueAsString(next);
					XAttributeLiteral attribNameEvent = factory.createAttributeLiteral(newName, value, null);
					attribMapEvent.put(next.getName(), attribNameEvent);
				}
			}
			XEvent event = factory.createEvent(attribMapEvent);
			xTrace.add(event);
		}
		
		this.log.setPromLog(log);
		outputLog.deliver(this.log);
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(this.log);
		logService.log("end do transformation ExampleSet to XLog", LogService.NOTE);

	}

}
