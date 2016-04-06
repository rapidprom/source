package org.rapidprom.operators.logmanipulation;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
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
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

public class AddEventAttributesToLogOperator extends Operator {

	private static final String PARAMETER_1 = "Case id column",
			PARAMETER_2 = "Event id column";

	private Attribute traceIdColumnAttrib = null;
	private Attribute eventIdColumnAttrib = null;

	private InputPort inputExampleSet = getInputPorts().createPort(
			"example set (Data Table)", new ExampleSetMetaData());
	private InputPort inputLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputLog = getOutputPorts().createPort(
			"event log (ProM Event Log)");

	public AddEventAttributesToLogOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputLog, XLogIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: add event attributes");
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
			}
			if (next.getName().equals(getParameterAsString(PARAMETER_2))) {
				eventIdColumnAttrib = next;
			}
			if (traceIdColumnAttrib != null && eventIdColumnAttrib != null) {
				break;
			}
		}

		if (traceIdColumnAttrib != null && eventIdColumnAttrib != null) {
			XLog adaptedLog = mergeExampleSetIntoLog(xLog, es,
					traceIdColumnAttrib, eventIdColumnAttrib);
			XLogIOObject xLogIOObject = new XLogIOObject(adaptedLog,
					logIO.getPluginContext());
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.EXAMPLE_SET);
			outputLog.deliverMD(md);
			outputLog.deliver(xLogIOObject);

		} else {
			// show warning
			JOptionPane.showMessageDialog(null,
					"Case ID column or event ID column was not found",
					"Case ID / Event ID column not found",
					JOptionPane.ERROR_MESSAGE);
			outputLog.deliverMD(md);			
			outputLog.deliver(null);
		}
		logger.log(Level.INFO,
				"End: add event attributes ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeString parameterType1 = new ParameterTypeString(
				PARAMETER_1, PARAMETER_1, "T:concept:name");
		parameterTypes.add(parameterType1);

		ParameterTypeString parameterType2 = new ParameterTypeString(
				PARAMETER_2, PARAMETER_2, "E:concept:name");
		parameterTypes.add(parameterType2);

		return parameterTypes;
	}

	private XLog mergeExampleSetIntoLog(XLog xLog, ExampleSet es,
			Attribute traceIdColumnAttrib, Attribute eventIdColumnAttrib) {
		Iterator<Example> iterator = es.iterator();
		while (iterator.hasNext()) {
			Example example = iterator.next();
			// get the case id and see if a corresponding trace can be found
			String caseid = example.getValueAsString(traceIdColumnAttrib);
			XTrace t = findTrace(caseid, xLog);
			if (t != null) {
				XEvent e = findEvent(eventIdColumnAttrib, t);
				if (e != null) {
					Attributes attributes = example.getAttributes();
					Iterator<Attribute> iterator2 = attributes.iterator();
					while (iterator2.hasNext()) {
						Attribute attrib = iterator2.next();
						XAttribute newAttrib = null;
						if (!attrib.getName().equals(
								traceIdColumnAttrib.getName())
								&& !attrib.getName().equals(
										eventIdColumnAttrib.getName())) {
							if (attrib.getValueType() == Ontology.NUMERICAL
									|| attrib.getValueType() == Ontology.INTEGER
									|| attrib.getValueType() == Ontology.REAL) {
								double numericalValue = example
										.getNumericalValue(attrib);
								XAttributeLiteralImpl attribLit = new XAttributeLiteralImpl(
										attrib.getName(),
										Double.toString(numericalValue));
								newAttrib = attribLit;
							} else if (attrib.getValueType() == Ontology.NOMINAL
									|| attrib.getValueType() == Ontology.BINOMINAL) {
								String nominalValue = example
										.getNominalValue(attrib);
								XAttributeLiteralImpl attribLit = new XAttributeLiteralImpl(
										attrib.getName(), nominalValue);
								newAttrib = attribLit;
							} else if (attrib.getValueType() == Ontology.DATE_TIME) {
								Date dateValue = example.getDateValue(attrib);
								XAttributeLiteralImpl attribLit = new XAttributeLiteralImpl(
										attrib.getName(), dateValue.toString());
								newAttrib = attribLit;
							}
						}
						// add attribute to the log
						if (newAttrib != null) {
							e.getAttributes().put(attrib.getName(), newAttrib);
						}
					}
				}
			}
		}
		return xLog;
	}

	private XEvent findEvent(Attribute eventAttrib, XTrace t) {
		for (XEvent e : t) {
			String name = eventAttrib.getName();
			String nameEvent = XConceptExtension.instance().extractName(e);
			if (name.equals(nameEvent)) {
				// found the event
				return e;
			}
		}
		return null;
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
