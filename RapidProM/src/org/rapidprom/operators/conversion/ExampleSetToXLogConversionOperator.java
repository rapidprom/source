package org.rapidprom.operators.conversion;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
//import org.processmining.models.xes.XEventPassageClassifier;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.ports.metadata.ExampleSetNumberOfAttributesPrecondition;
import org.rapidprom.parameter.ParameterTypeExampleSetAttributesDynamicCategory;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;

public class ExampleSetToXLogConversionOperator extends Operator {

	private static final String DEFAULT_VALUE_OPTIONAL = "<ignore>";
	private static final String GLOBAL_INVALID = "__INVALID__";
	private static final String PARAMETER_DEFAULT_EVENT_LIFECYCLE_TRANSITION = DEFAULT_VALUE_OPTIONAL;
	private static final String PARAMETER_DEFAULT_EVENT_RESOURCE = DEFAULT_VALUE_OPTIONAL;
	private static final String PARAMETER_DEFAULT_EVENT_RESOURCE_GROUP = DEFAULT_VALUE_OPTIONAL;
	private static final String PARAMETER_DEFAULT_EVENT_RESOURCE_ROLE = DEFAULT_VALUE_OPTIONAL;
	private static final String PARAMETER_DEFAULT_EVENT_TIMESTAMP = DEFAULT_VALUE_OPTIONAL;
	private static final String PARAMETER_DESC_EVENT_IDENTIFIER = "Please select an attribute of the example set to act as an event identifier";
	private static final String PARAMETER_DESC_EVENT_LIFECYCLE_TRANSITION = "Please select an (optional) attribute of the example set to act as an event identifier";
	private static final String PARAMETER_DESC_EVENT_RESOURCE = "Please select an (optional) attribute of the example set that signifies the resource that executed the event";
	private static final String PARAMETER_DESC_EVENT_RESOURCE_GROUP = "Please select an (optional) attribute of the example set that signifies the resource group of the resource that executed the event";
	private static final String PARAMETER_DESC_EVENT_RESOURCE_ROLE = "Please select an (optional) attribute of the example set that signifies the role of the resource that executed the event";
	private static final String PARAMETER_DESC_EVENT_TIMESTAMP = "Please select an (optional) attribute of the example set to act as an event timestamp";
	//private static final String PARAMETER_DESC_REORDER_BY_TIMESTAMP = "If the example set contains timestamps, this option will reorder the events within traces based on their time-stamps";
	private static final String PARAMETER_DESC_TRACE_IDENTIFIER = "Please select an attribute of the example set to act as a trace identifier";
	//private static final boolean PARAMETER_KEY_DEFAULT_REORDER_BY_TIMESTAMP = false;
	private static final String PARAMETER_KEY_EVENT_IDENTIFIER = "event_identifier";
	private static final String PARAMETER_KEY_EVENT_LIFECYCLE_TRANSITION = "event_lifecycle_transition";
	private static final String PARAMETER_KEY_EVENT_RESOURCE = "event_resource";
	private static final String PARAMETER_KEY_EVENT_RESOURCE_GROUP = "event_resource_role";
	private static final String PARAMETER_KEY_EVENT_RESOURCE_ROLE = "event_resource_role";
	private static final String PARAMETER_KEY_EVENT_TIMESTAMP = "event_time_stamp";
	//private static final String PARAMETER_KEY_REORDER_BY_TIMESTAMP = "reorder_by_time_stamp";
	private static final String PARAMETER_KEY_TRACE_IDENTIFIER = "trace_identifier";

	/** defining the ports */
	private InputPort inputExampleSet = getInputPorts()
			.createPort("example set (Data Table)", new ExampleSetMetaData());
	private OutputPort outputLog = getOutputPorts()
			.createPort("event log (ProM Event Log)");

	public ExampleSetToXLogConversionOperator(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(outputLog, XLogIOObject.class));
		inputExampleSet.addPrecondition(
				new ExampleSetNumberOfAttributesPrecondition(inputExampleSet,
						2));
	}

	private XLog addClassifiers(XLog log, boolean lifecycle) {
		log.getClassifiers().add(new XEventNameClassifier());
		if (lifecycle) {
			log.getClassifiers()
					.add(new XEventAndClassifier(new XEventNameClassifier(),
							new XEventLifeTransClassifier()));
		}
		return log;
	}

	private List<ParameterType> addEventIdentificationParameterType(
			List<ParameterType> params) {
		ParameterType eventIdentification = setupDynamicExampleSetBasedParameterType(
				PARAMETER_KEY_EVENT_IDENTIFIER, PARAMETER_DESC_EVENT_IDENTIFIER,
				new String[] {}, -1, false, inputExampleSet);
		eventIdentification.setOptional(false);
		params.add(eventIdentification);
		return params;
	}

	private XLog addExtensions(XLog log, boolean lifecycle, boolean time,
			boolean oragnizational) {
		log.getExtensions().add(XConceptExtension.instance());
		if (lifecycle) {
			log.getExtensions().add(XLifecycleExtension.instance());
		}
		if (time) {
			log.getExtensions().add(XTimeExtension.instance());
		}
		if (oragnizational) {
			log.getExtensions().add(XOrganizationalExtension.instance());
		}
		return log;
	}

	private XLog addGlobals(XLog log, boolean useLifeCycle,
			boolean useOrganizational, boolean useTimeStamp) {
		log.getGlobalTraceAttributes()
				.add(new XAttributeLiteralImpl(XConceptExtension.KEY_NAME,
						GLOBAL_INVALID, XConceptExtension.instance()));
		log.getGlobalEventAttributes()
				.add(new XAttributeLiteralImpl(XConceptExtension.KEY_NAME,
						GLOBAL_INVALID, XConceptExtension.instance()));
		if (useLifeCycle) {
			log.getGlobalEventAttributes()
					.add(new XAttributeLiteralImpl(
							XLifecycleExtension.KEY_TRANSITION, GLOBAL_INVALID,
							XLifecycleExtension.instance()));
		}
		if (useTimeStamp) {
			log.getGlobalEventAttributes()
					.add(new XAttributeTimestampImpl(
							XTimeExtension.KEY_TIMESTAMP, 0,
							XTimeExtension.instance()));
		}
		if (useOrganizational) {
			if (!getDynamicParameterTypeValue(PARAMETER_KEY_EVENT_RESOURCE)
					.equals(DEFAULT_VALUE_OPTIONAL)) {
				log.getGlobalEventAttributes().add(new XAttributeLiteralImpl(
						XOrganizationalExtension.KEY_RESOURCE, GLOBAL_INVALID,
						XOrganizationalExtension.instance()));
			}
			if (!getDynamicParameterTypeValue(PARAMETER_KEY_EVENT_RESOURCE_ROLE)
					.equals(DEFAULT_VALUE_OPTIONAL)) {
				log.getGlobalEventAttributes().add(new XAttributeLiteralImpl(
						XOrganizationalExtension.KEY_ROLE, GLOBAL_INVALID,
						XOrganizationalExtension.instance()));
			}
			if (!getDynamicParameterTypeValue(
					PARAMETER_KEY_EVENT_RESOURCE_GROUP)
							.equals(DEFAULT_VALUE_OPTIONAL)) {
				log.getGlobalEventAttributes().add(new XAttributeLiteralImpl(
						XOrganizationalExtension.KEY_GROUP, GLOBAL_INVALID,
						XOrganizationalExtension.instance()));
			}
		}
		return log;
	}

	private List<ParameterType> addLifecycleTransitionParameterTypes(
			List<ParameterType> params) {
		ParameterType lifecycleTransition = setupDynamicExampleSetBasedParameterType(
				PARAMETER_KEY_EVENT_LIFECYCLE_TRANSITION,
				PARAMETER_DESC_EVENT_LIFECYCLE_TRANSITION,
				new String[] { PARAMETER_DEFAULT_EVENT_LIFECYCLE_TRANSITION },
				0, true, inputExampleSet);
		lifecycleTransition.setOptional(true);
		params.add(lifecycleTransition);
		return params;
	}

	private List<ParameterType> addResourceGroupParameterType(
			List<ParameterType> params) {
		ParameterType group = setupDynamicExampleSetBasedParameterType(
				PARAMETER_KEY_EVENT_RESOURCE_GROUP,
				PARAMETER_DESC_EVENT_RESOURCE_GROUP,
				new String[] { PARAMETER_DEFAULT_EVENT_RESOURCE_GROUP }, 0,
				true, inputExampleSet);
		group.setOptional(true);
		params.add(group);
		return params;
	}

	private List<ParameterType> addResourceParameterType(
			List<ParameterType> params) {
		ParameterType resource = setupDynamicExampleSetBasedParameterType(
				PARAMETER_KEY_EVENT_RESOURCE, PARAMETER_DESC_EVENT_RESOURCE,
				new String[] { PARAMETER_DEFAULT_EVENT_RESOURCE }, 0, true,
				inputExampleSet);
		resource.setOptional(true);
		params.add(resource);
		return params;
	}

	private List<ParameterType> addResourceRoleParameterType(
			List<ParameterType> params) {
		ParameterType role = setupDynamicExampleSetBasedParameterType(
				PARAMETER_KEY_EVENT_RESOURCE_ROLE,
				PARAMETER_DESC_EVENT_RESOURCE_ROLE,
				new String[] { PARAMETER_DEFAULT_EVENT_RESOURCE_ROLE }, 0, true,
				inputExampleSet);
		role.setOptional(true);
		params.add(role);
		return params;
	}

	private List<ParameterType> addTimeStampParameterTypes(
			List<ParameterType> params) {
		ParameterType eventTimeStamp = setupDynamicExampleSetBasedParameterType(
				PARAMETER_KEY_EVENT_TIMESTAMP, PARAMETER_DESC_EVENT_TIMESTAMP,
				new String[] { PARAMETER_DEFAULT_EVENT_TIMESTAMP }, 0, false,
				inputExampleSet);
		eventTimeStamp.setOptional(true);
		params.add(eventTimeStamp);

		// FIXME we do not allow for reordering as the ProM reordering operator
		// does not copy all extensions / attributes etc.
		// ParameterType eventTimeStampReorder = new ParameterTypeBoolean(
		// PARAMETER_KEY_REORDER_BY_TIMESTAMP,
		// PARAMETER_DESC_REORDER_BY_TIMESTAMP,
		// PARAMETER_KEY_DEFAULT_REORDER_BY_TIMESTAMP, false);
		// eventTimeStampReorder.setOptional(true);
		// eventTimeStampReorder.registerDependencyCondition(
		// new NonEqualStringCondition(this, PARAMETER_KEY_EVENT_TIMESTAMP,
		// true, new String[] { DEFAULT_VALUE_OPTIONAL }));
		// params.add(eventTimeStampReorder);
		return params;
	}

	private List<ParameterType> addTraceIdentificationParameterType(
			List<ParameterType> params) {
		ParameterType traceIdentification = setupDynamicExampleSetBasedParameterType(
				PARAMETER_KEY_TRACE_IDENTIFIER, PARAMETER_DESC_TRACE_IDENTIFIER,
				new String[] {}, -1, false, inputExampleSet);
		traceIdentification.setOptional(false);
		params.add(traceIdentification);
		return params;
	}

	private XEvent constructEvent(XFactory factory, ExampleSet data,
			Example example) {
		XAttributeMap attributes = new XAttributeMapImpl();
		String eventName = example.getValueAsString(data.getAttributes().get(
				getDynamicParameterTypeValue(PARAMETER_KEY_EVENT_IDENTIFIER)));
		attributes.put(XConceptExtension.KEY_NAME,
				new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, eventName,
						XConceptExtension.instance()));
		XEvent event = factory.createEvent(attributes);
		return decorateEvent(event, data, example);
	}

	private XLog constructLogByExampleSet(ExampleSet data) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();

		XLog log = createLog(factory,
				!getDynamicParameterTypeValue(
						PARAMETER_KEY_EVENT_LIFECYCLE_TRANSITION)
								.equals(DEFAULT_VALUE_OPTIONAL));

		log = addExtensions(log, isUseLifeCycle(), isUseTime(),
				isUseOrganizational());

		log = addGlobals(log, isUseLifeCycle(), isUseOrganizational(),
				isUseTime());

		log = addClassifiers(log, isUseLifeCycle());

		// iterate over traces and events
		Iterator<Example> iterator = data.iterator();
		Map<String, XTrace> mapping = new HashMap<String, XTrace>();
		while (iterator.hasNext()) {
			log = processExampleAsEvent(factory, log, data, iterator.next(),
					mapping);
		}
		return log;
	}

	/**
	 * given some trace identifier, this function returns a corresponding XTrace
	 * object. if it already exists in the map, the corresponding object will be
	 * returned. If it is a new instance, the trace will be added to the given
	 * event log and, the map will be updated.
	 * 
	 * @param factory
	 * @param log
	 * @param traceIdentifier
	 * @param mapping
	 * @return
	 */
	private XTrace constructTrace(XFactory factory, XLog log,
			String traceIdentifier, Map<String, XTrace> mapping) {
		if (mapping.containsKey(traceIdentifier))
			return mapping.get(traceIdentifier);
		XAttributeLiteral attribNameTrace = factory.createAttributeLiteral(
				"concept:name", traceIdentifier, XConceptExtension.instance());
		XAttributeMap attribMapTrace = new XAttributeMapImpl();
		attribMapTrace.put(XConceptExtension.KEY_NAME, attribNameTrace);
		XTrace trace = factory.createTrace(attribMapTrace);
		log.add(trace);
		mapping.put(traceIdentifier, trace);
		return trace;
	}

	private XLog createLog(XFactory factory, boolean useLifeCycleModel) {
		XAttributeLiteral attribNameLog = factory.createAttributeLiteral(
				XConceptExtension.KEY_NAME,
				"Event Log (created by RapidMiner @ "
						+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
								.format(Calendar.getInstance().getTime())
						+ ")",
				XConceptExtension.instance());
		XAttributeMap attribMapLog = new XAttributeMapImpl();
		attribMapLog.put(XConceptExtension.KEY_NAME, attribNameLog);

		if (useLifeCycleModel) {
			XAttributeLiteral attribLifecycleLog = factory
					.createAttributeLiteral(XLifecycleExtension.KEY_MODEL,
							XLifecycleExtension.VALUE_MODEL_STANDARD,
							XLifecycleExtension.instance());
			attribMapLog.put(XLifecycleExtension.KEY_MODEL, attribLifecycleLog);
		}
		XLog log = factory.createLog(attribMapLog);

		return log;
	}

	private XEvent decorateEvent(XEvent event, ExampleSet data,
			Example example) {
		event = decorateEventWithTime(event, data, example);
		event = decorateEventWithLifeCycle(event, data, example);
		if (isUseOrganizational()) {
			event = decorateEventWithResource(event, data, example);
			event = decorateEventWithRole(event, data, example);
			event = decorateEventWithGroup(event, data, example);
		}
		return event;
	}

	private XEvent decorateEventWithGroup(XEvent event, ExampleSet data,
			Example example) {
		String resourceGroupAttr = getDynamicParameterTypeValue(
				PARAMETER_KEY_EVENT_RESOURCE_GROUP);
		if (!resourceGroupAttr.equals(DEFAULT_VALUE_OPTIONAL)) {
			String group = example.getValueAsString(
					data.getAttributes().get(resourceGroupAttr));
			event.getAttributes().put(XOrganizationalExtension.KEY_GROUP,
					new XAttributeLiteralImpl(
							XOrganizationalExtension.KEY_GROUP, group,
							XOrganizationalExtension.instance()));
		}
		return event;
	}

	private XEvent decorateEventWithLifeCycle(XEvent event, ExampleSet data,
			Example example) {
		String ltAttr = getDynamicParameterTypeValue(
				PARAMETER_KEY_EVENT_LIFECYCLE_TRANSITION);
		if (!ltAttr.equals(DEFAULT_VALUE_OPTIONAL)) {
			String lifecycle = example
					.getValueAsString(data.getAttributes().get(ltAttr));
			event.getAttributes().put(XLifecycleExtension.KEY_TRANSITION,
					new XAttributeLiteralImpl(
							XLifecycleExtension.KEY_TRANSITION, lifecycle,
							XLifecycleExtension.instance()));
		}
		return event;
	}

	private XEvent decorateEventWithResource(XEvent event, ExampleSet data,
			Example example) {
		String resourceAttr = getDynamicParameterTypeValue(
				PARAMETER_KEY_EVENT_RESOURCE);
		if (!resourceAttr.equals(DEFAULT_VALUE_OPTIONAL)) {
			String resource = example
					.getValueAsString(data.getAttributes().get(resourceAttr));
			event.getAttributes().put(XOrganizationalExtension.KEY_RESOURCE,
					new XAttributeLiteralImpl(
							XOrganizationalExtension.KEY_RESOURCE, resource,
							XOrganizationalExtension.instance()));
		}
		return event;
	}

	private XEvent decorateEventWithRole(XEvent event, ExampleSet data,
			Example example) {
		String resourceRoleAttr = getDynamicParameterTypeValue(
				PARAMETER_KEY_EVENT_RESOURCE_ROLE);
		if (!resourceRoleAttr.equals(DEFAULT_VALUE_OPTIONAL)) {
			String role = example.getValueAsString(
					data.getAttributes().get(resourceRoleAttr));
			event.getAttributes().put(XOrganizationalExtension.KEY_ROLE,
					new XAttributeLiteralImpl(XOrganizationalExtension.KEY_ROLE,
							role, XOrganizationalExtension.instance()));

		}
		return event;
	}

	private XEvent decorateEventWithTime(XEvent event, ExampleSet data,
			Example example) {
		String timeAttr = getDynamicParameterTypeValue(
				PARAMETER_KEY_EVENT_TIMESTAMP);
		if (!timeAttr.equals(DEFAULT_VALUE_OPTIONAL)) {
			Date time = example
					.getDateValue(data.getAttributes().get(timeAttr));
			event.getAttributes().put(XTimeExtension.KEY_TIMESTAMP,
					new XAttributeTimestampImpl(XTimeExtension.KEY_TIMESTAMP,
							time, XTimeExtension.instance()));
		}
		return event;
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: ExampleSet to XLog conversion");
		long time = System.currentTimeMillis();
		XLog log = constructLogByExampleSet(
				inputExampleSet.getData(ExampleSet.class));
		// FIXME time based reordering in ProM does not properly copy all log
		// extensions, classifiers etc.
		// if (!getDynamicParameterTypeValue(PARAMETER_KEY_EVENT_TIMESTAMP)
		// .equals(DEFAULT_VALUE_OPTIONAL)
		// && getParameterAsBoolean(PARAMETER_KEY_REORDER_BY_TIMESTAMP)) {
		// log = ReSortLog.removeEdgePoints(
		// ProMPluginContextManager.instance().getContext(), log);
		// }
		outputLog.deliver(new XLogIOObject(log,
				ProMPluginContextManager.instance().getContext()));
		logger.log(Level.INFO, "End: Table to Event Log conversion ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

	private String getDynamicParameterTypeValue(String key) {
		try {
			return ((ParameterTypeExampleSetAttributesDynamicCategory) getParameterType(
					key)).getValues()[getParameterAsInt(key)];
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
		return DEFAULT_VALUE_OPTIONAL;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();
		params = addTraceIdentificationParameterType(params);
		params = addEventIdentificationParameterType(params);
		params = addTimeStampParameterTypes(params);
		params = addLifecycleTransitionParameterTypes(params);
		params = addResourceParameterType(params);
		params = addResourceRoleParameterType(params);
		params = addResourceGroupParameterType(params);
		return params;
	}

	private boolean isUseLifeCycle() {
		return !getDynamicParameterTypeValue(
				PARAMETER_KEY_EVENT_LIFECYCLE_TRANSITION)
						.equals(DEFAULT_VALUE_OPTIONAL);
	}

	private boolean isUseOrganizational() {
		return !getDynamicParameterTypeValue(PARAMETER_KEY_EVENT_RESOURCE)
				.equals(DEFAULT_VALUE_OPTIONAL)
				|| !getDynamicParameterTypeValue(
						PARAMETER_KEY_EVENT_RESOURCE_ROLE)
								.equals(DEFAULT_VALUE_OPTIONAL)
				|| !getDynamicParameterTypeValue(
						PARAMETER_KEY_EVENT_RESOURCE_GROUP)
								.equals(DEFAULT_VALUE_OPTIONAL);
	}

	private boolean isUseTime() {
		return !getDynamicParameterTypeValue(PARAMETER_KEY_EVENT_TIMESTAMP)
				.equals(DEFAULT_VALUE_OPTIONAL);
	}

	private XLog processExampleAsEvent(XFactory factory, XLog log,
			ExampleSet data, Example example, Map<String, XTrace> mapping) {
		XTrace trace = constructTrace(factory, log,
				example.getValueAsString(
						data.getAttributes()
								.get(getDynamicParameterTypeValue(
										PARAMETER_KEY_TRACE_IDENTIFIER))),
				mapping);
		trace.add(constructEvent(factory, data, example));
		return log;

	}

	private ParameterType setupDynamicExampleSetBasedParameterType(String key,
			String desc, String[] values, int defaultValue, boolean expert,
			InputPort inputPort) {
		return new ParameterTypeExampleSetAttributesDynamicCategory(key, desc,
				values, values, defaultValue, expert, inputPort);
	}

}
