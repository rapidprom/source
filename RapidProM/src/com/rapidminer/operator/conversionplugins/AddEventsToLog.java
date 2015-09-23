package com.rapidminer.operator.conversionplugins;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ioobjectrenderers.XLogIOObjectVisualizationType;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.parameters.ParameterString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.util.ProMIOObjectList;

public class AddEventsToLog extends Operator {

	private List<Parameter> parameters = null;
	private Attribute traceIdColumnAttrib = null;
	private String nameTraceIDcolumn = "";

	private String nameEventIDcolumn = "";
	private String lifeCycleColumn = "";
	private String timestampColumn = "";
	private String resourceColumn = "";
	private String roleColumn = "";
	private String groupColumn = "";

	private InputPort inputExampleSet = getInputPorts().createPort(
			"example set (Data Table)", new ExampleSetMetaData());
	private InputPort inputLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputLog = getOutputPorts().createPort(
			"event log (ProM Event Log)");

	public AddEventsToLog(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputLog, XLogIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		ExampleSet es = inputExampleSet.getData(ExampleSet.class);

		XLogIOObject logIO = inputLog.getData(XLogIOObject.class);
		XLog xLog = logIO.getXLog();

		getConfiguration(parameters);

		// check first if there is a column for the case id
		Iterator<Attribute> iterator = es.getAttributes().iterator();
		while (iterator.hasNext()) {
			Attribute next = iterator.next();
			if (next.getName().equals(nameTraceIDcolumn)) {
				traceIdColumnAttrib = next;
				break;
			}
		}
		if (traceIdColumnAttrib != null) {
			System.out.println("DUMPFIRST");
			dumpSizeTraces(xLog);
			XLog adaptedLog = mergeExampleSetIntoLog(xLog, es,
					traceIdColumnAttrib);
			XLogIOObject xLogIOObject = new XLogIOObject(adaptedLog);
			xLogIOObject.setPluginContext(null);
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.EXAMPLE_SET);
			outputLog.deliver(xLogIOObject);
			System.out.println("DUMPSECOND");
			dumpSizeTraces(adaptedLog);
			// add to list so that afterwards it can be cleared if needed
			ProMIOObjectList instance = ProMIOObjectList.getInstance();
			instance.addToList(xLogIOObject);
		}
	}

	private void dumpSizeTraces(XLog xLog) {
		for (XTrace t : xLog) {
			System.out.println(XConceptExtension.instance().extractName(t)
					+ ":" + t.size());
		}

	}

	private XLog mergeExampleSetIntoLog(XLog xLog, ExampleSet es,
			Attribute traceIdAttrib) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		Iterator<Example> iterator = es.iterator();
		while (iterator.hasNext()) {
			Example row = iterator.next();
			String caseID = row.getValueAsString(traceIdAttrib);
			XTrace t = findTrace(caseID, xLog);
			if (t != null) {
				XAttributeMap attribMapEvent = new XAttributeMapImpl();
				Iterator<Attribute> iterator2 = row.getAttributes().iterator();
				while (iterator2.hasNext()) {
					Attribute next = iterator2.next();
					String nameAttrib = next.getName();
					if (nameAttrib.equals(traceIdAttrib.getName())) {
						// do nothing
					} else if (nameAttrib.equals(nameEventIDcolumn)
							&& !nameEventIDcolumn.equals("")) {
						// concept:name
						String value = row.getValueAsString(next);
						XAttributeLiteral attribNameEvent = factory
								.createAttributeLiteral("concept:name", value,
										XConceptExtension.instance());
						attribMapEvent.put("concept:name", attribNameEvent);
					} else if (nameAttrib.equals(lifeCycleColumn)
							&& !lifeCycleColumn.equals("")) {
						// lifecycle:transition
						String value = row.getValueAsString(next);
						XAttributeLiteral attribLC = factory
								.createAttributeLiteral("lifecycle:transition",
										value, XLifecycleExtension.instance());
						attribMapEvent.put("lifecycle:transition", attribLC);
					} else if (nameAttrib.equals(timestampColumn)
							&& !timestampColumn.equals("")) {
						// timestamp
						Date dateValue = row.getDateValue(next);
						XAttributeTimestamp attribTimestampEvent = factory
								.createAttributeTimestamp("time:timestamp",
										dateValue, XTimeExtension.instance());
						attribMapEvent.put("time:timestamp",
								attribTimestampEvent);
					} else if (nameAttrib.equals(resourceColumn)
							&& !resourceColumn.equals("")) {
						// resource
						String value = row.getValueAsString(next);
						XAttributeLiteral attribResource = factory
								.createAttributeLiteral("org:resource", value,
										XOrganizationalExtension.instance());
						attribMapEvent.put("org:resource", attribResource);
					} else if (nameAttrib.equals(roleColumn)
							&& !roleColumn.equals("")) {
						// role
						String value = row.getValueAsString(next);
						XAttributeLiteral attribRole = factory
								.createAttributeLiteral("org:role", value,
										XOrganizationalExtension.instance());
						attribMapEvent.put("org:role", attribRole);
					} else if (nameAttrib.equals(groupColumn)
							&& !groupColumn.equals("")) {
						// group
						String value = row.getValueAsString(next);
						XAttributeLiteral attribGroup = factory
								.createAttributeLiteral("group:resource",
										value,
										XOrganizationalExtension.instance());
						attribMapEvent.put("group:resource", attribGroup);
					} else {
						if (next.getValueType() == Ontology.DATE
								|| next.getValueType() == Ontology.DATE_TIME) {
							Date dateValue = row.getDateValue(next);
							XAttributeTimestamp attribTimestampEvent = factory
									.createAttributeTimestamp(nameAttrib,
											dateValue,
											XTimeExtension.instance());
							attribMapEvent
									.put(nameAttrib, attribTimestampEvent);
						} else {
							String value = row.getValueAsString(next);
							XAttributeLiteralImpl attribLit = new XAttributeLiteralImpl(
									nameAttrib, value);
							attribMapEvent.put(nameAttrib, attribLit);
						}
					}

				}
				XEvent event = factory.createEvent(attribMapEvent);
				t.add(event);
			}
		}
		return xLog;
	}

	public List<ParameterType> getParameterTypes() {
		this.parameters = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterString parameter1 = new ParameterString("", String.class,
				"Name of Case ID column", "Case ID column");
		ParameterTypeString parameterType1 = new ParameterTypeString(
				parameter1.getNameParameter(),
				parameter1.getDescriptionParameter(),
				parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parameters.add(parameter1);

		ParameterString parameter2 = new ParameterString("", String.class,
				"Name of Event concept:name column",
				"Event concept:name column");
		ParameterTypeString parameterType2 = new ParameterTypeString(
				parameter2.getNameParameter(),
				parameter2.getDescriptionParameter(),
				parameter2.getDefaultValueParameter());
		parameterTypes.add(parameterType2);
		parameters.add(parameter2);

		ParameterString parameter3 = new ParameterString("", String.class,
				"Name of lifecycle:transition column",
				"Lifecycle:transition column");
		ParameterTypeString parameterType3 = new ParameterTypeString(
				parameter3.getNameParameter(),
				parameter3.getDescriptionParameter(),
				parameter3.getDefaultValueParameter());
		parameterTypes.add(parameterType3);
		parameters.add(parameter3);

		ParameterString parameter4 = new ParameterString("", String.class,
				"Name of time:timestamp column", "Time:timestamp column");
		ParameterTypeString parameterType4 = new ParameterTypeString(
				parameter4.getNameParameter(),
				parameter4.getDescriptionParameter(),
				parameter4.getDefaultValueParameter());
		parameterTypes.add(parameterType4);
		parameters.add(parameter4);

		ParameterString parameter5 = new ParameterString("", String.class,
				"Name of org:resource column", "Org:resource column");
		ParameterTypeString parameterType5 = new ParameterTypeString(
				parameter5.getNameParameter(),
				parameter5.getDescriptionParameter(),
				parameter5.getDefaultValueParameter());
		parameterTypes.add(parameterType5);
		parameters.add(parameter5);

		ParameterString parameter6 = new ParameterString("", String.class,
				"Name of org:resource column", "Org:resource column");
		ParameterTypeString parameterType6 = new ParameterTypeString(
				parameter6.getNameParameter(),
				parameter6.getDescriptionParameter(),
				parameter6.getDefaultValueParameter());
		parameterTypes.add(parameterType6);
		parameters.add(parameter6);

		ParameterString parameter7 = new ParameterString("", String.class,
				"Name of org:role column", "Org:role column");
		ParameterTypeString parameterType7 = new ParameterTypeString(
				parameter7.getNameParameter(),
				parameter7.getDescriptionParameter(),
				parameter7.getDefaultValueParameter());
		parameterTypes.add(parameterType7);
		parameters.add(parameter7);

		ParameterString parameter8 = new ParameterString("", String.class,
				"Name of group:resource column", "Group:resource column");
		ParameterTypeString parameterType8 = new ParameterTypeString(
				parameter8.getNameParameter(),
				parameter8.getDescriptionParameter(),
				parameter8.getDefaultValueParameter());
		parameterTypes.add(parameterType8);
		parameters.add(parameter8);

		return parameterTypes;
	}

	private void getConfiguration(List<Parameter> parameters) {
		try {
			Parameter parameter1 = parameters.get(0);
			String valPar1 = getParameterAsString(parameter1.getNameParameter());
			nameTraceIDcolumn = valPar1;

			Parameter parameter2 = parameters.get(1);
			String valPar2 = getParameterAsString(parameter2.getNameParameter());
			nameEventIDcolumn = valPar2;

			Parameter parameter3 = parameters.get(2);
			String valPar3 = getParameterAsString(parameter3.getNameParameter());
			lifeCycleColumn = valPar3;

			Parameter parameter4 = parameters.get(3);
			String valPar4 = getParameterAsString(parameter4.getNameParameter());
			timestampColumn = valPar4;

			Parameter parameter5 = parameters.get(4);
			String valPar5 = getParameterAsString(parameter5.getNameParameter());
			resourceColumn = valPar5;

			Parameter parameter6 = parameters.get(5);
			String valPar6 = getParameterAsString(parameter6.getNameParameter());
			roleColumn = valPar6;

			Parameter parameter7 = parameters.get(6);
			String valPar7 = getParameterAsString(parameter7.getNameParameter());
			groupColumn = valPar7;

		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
	}

	private XTrace findTrace(String caseid, XLog xLog) {
		for (XTrace t : xLog) {
			String name = XConceptExtension.instance().extractName(t);
			if (name.equals(caseid)) {
				return t;
			}
		}
		return null;
	}

}
