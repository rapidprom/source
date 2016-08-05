package org.rapidprom.operators.logmanipulation;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

public class AddEventsToLogOperator extends Operator {

	private static final String PARAMETER_1 = "Case id column",
			PARAMETER_2 = "Event id column", PARAMETER_3 = "Lifecycle column",
			PARAMETER_4 = "Timestamp column", PARAMETER_5 = "Resource column";
	private Attribute traceIdColumnAttrib = null;

	private InputPort inputExampleSet = getInputPorts().createPort(
			"example set (Data Table)", new ExampleSetMetaData());
	private InputPort inputLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputLog = getOutputPorts().createPort(
			"event log (ProM Event Log)");

	public AddEventsToLogOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputLog, XLogIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: add event");
		long time = System.currentTimeMillis();
		MetaData md = inputLog.getMetaData();

		ExampleSet es = inputExampleSet.getData(ExampleSet.class);

		XLogIOObject logIO = inputLog.getData(XLogIOObject.class);
		XLog xLog = logIO.getArtifact();

		Iterator<Attribute> iterator = es.getAttributes().iterator();
		while (iterator.hasNext()) {
			Attribute next = iterator.next();
			if (next.getName().equals(getParameterAsString(PARAMETER_1))) {
				traceIdColumnAttrib = next;
				break;
			}
		}
		if (traceIdColumnAttrib != null) {
			System.out.println("DUMPFIRST");
			dumpSizeTraces(xLog);
			XLog adaptedLog = mergeExampleSetIntoLog(xLog, es,
					traceIdColumnAttrib);
			XLogIOObject xLogIOObject = new XLogIOObject(adaptedLog,
					logIO.getPluginContext());
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.EXAMPLE_SET);
			outputLog.deliverMD(md);
			outputLog.deliver(xLogIOObject);
			System.out.println("DUMPSECOND");
			dumpSizeTraces(adaptedLog);

		}
		logger.log(Level.INFO, "End: add event ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	private void dumpSizeTraces(XLog xLog) {
		for (XTrace t : xLog) {
			System.out.println(XConceptExtension.instance().extractName(t)
					+ ":" + t.size());
		}

	}

	private XLog mergeExampleSetIntoLog(XLog xLog, ExampleSet es,
			Attribute traceIdAttrib) throws UndefinedParameterError {
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
					} else if (nameAttrib
							.equals(getParameterAsString(PARAMETER_2))
							&& !getParameterAsString(PARAMETER_2).equals("")) {
						// concept:name
						String value = row.getValueAsString(next);
						XAttributeLiteral attribNameEvent = factory
								.createAttributeLiteral("concept:name", value,
										XConceptExtension.instance());
						attribMapEvent.put("concept:name", attribNameEvent);
					} else if (nameAttrib
							.equals(getParameterAsString(PARAMETER_3))
							&& !getParameterAsString(PARAMETER_3).equals("")) {
						// lifecycle:transition
						String value = row.getValueAsString(next);
						XAttributeLiteral attribLC = factory
								.createAttributeLiteral("lifecycle:transition",
										value, XLifecycleExtension.instance());
						attribMapEvent.put("lifecycle:transition", attribLC);
					} else if (nameAttrib
							.equals(getParameterAsString(PARAMETER_4))
							&& !getParameterAsString(PARAMETER_4).equals("")) {
						// timestamp
						Date dateValue = row.getDateValue(next);
						XAttributeTimestamp attribTimestampEvent = factory
								.createAttributeTimestamp("time:timestamp",
										dateValue, XTimeExtension.instance());
						attribMapEvent.put("time:timestamp",
								attribTimestampEvent);
					} else if (nameAttrib
							.equals(getParameterAsString(PARAMETER_5))
							&& !getParameterAsString(PARAMETER_5).equals("")) {
						// resource
						String value = row.getValueAsString(next);
						XAttributeLiteral attribResource = factory
								.createAttributeLiteral("org:resource", value,
										XOrganizationalExtension.instance());
						attribMapEvent.put("org:resource", attribResource);
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

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeString parameterType1 = new ParameterTypeString(
				PARAMETER_1, PARAMETER_1, "T:concept:name");
		parameterTypes.add(parameterType1);

		ParameterTypeString parameterType2 = new ParameterTypeString(
				PARAMETER_2, PARAMETER_2, "E:concept:name");
		parameterTypes.add(parameterType2);

		ParameterTypeString parameterType3 = new ParameterTypeString(
				PARAMETER_3, PARAMETER_3, "E:lifecycle:transition");
		parameterTypes.add(parameterType3);

		ParameterTypeString parameterType4 = new ParameterTypeString(
				PARAMETER_4, PARAMETER_4, "E:time:timestamp");
		parameterTypes.add(parameterType4);

		ParameterTypeString parameterType5 = new ParameterTypeString(
				PARAMETER_5, PARAMETER_5, "E:org:resource");
		parameterTypes.add(parameterType5);

		return parameterTypes;
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
