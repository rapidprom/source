package org.rapidprom.operators.logmanipulation;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
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
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

public class AddTraceAttributesToLogOperator extends Operator {

	private static final String PARAMETER_1 = "Case id column";

	private InputPort inputExampleSet = getInputPorts().createPort(
			"example set (Data Table)", new ExampleSetMetaData());
	private InputPort inputLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputLog = getOutputPorts().createPort(
			"event log (ProM Event Log)");

	public AddTraceAttributesToLogOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputLog, XLogIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: add trace attributes");
		long time = System.currentTimeMillis();
		MetaData md = inputLog.getMetaData();
		
		ExampleSet es = inputExampleSet.getData(ExampleSet.class);

		XLogIOObject logIO = inputLog.getData(XLogIOObject.class);
		XLog xLog = logIO.getArtifact();

		Attribute idColumnAttrib = null;
		boolean found = false;
		Iterator<Attribute> iterator = es.getAttributes().iterator();
		while (iterator.hasNext()) {
			Attribute next = iterator.next();
			if (next.getName().equals(getParameterAsString(PARAMETER_1))) {
				idColumnAttrib = next;
				found = true;
				break;
			}
		}

		if (found) {
			XLog adaptedLog = mergeExampleSetIntoLog(xLog, es,
					getParameterAsString(PARAMETER_1), idColumnAttrib);
			XLogIOObject xLogIOObject = new XLogIOObject(adaptedLog,
					logIO.getPluginContext());
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.EXAMPLE_SET);
			outputLog.deliverMD(md);
			outputLog.deliver(xLogIOObject);

		} else {
			// show warning
			JOptionPane.showMessageDialog(null, "Case ID was not found",
					"Case ID column not found", JOptionPane.ERROR_MESSAGE);
			outputLog.deliverMD(md);
			outputLog.deliver(null);
		}
		logger.log(Level.INFO,
				"End: add trace attributes ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeString parameterType1 = new ParameterTypeString(
				PARAMETER_1, PARAMETER_1, "T:concept:name");
		parameterTypes.add(parameterType1);

		return parameterTypes;
	}

	private HashMap<String,XTrace> buildTraceMap(XLog xlog) {
		HashMap<String,XTrace> map = new HashMap<>();
		
		for (XTrace t : xlog) {
			String name = XConceptExtension.instance().extractName(t);
			if (name != null) {
				map.put(name, t);
			}
		}
		
		return map;
	}
	
	private XLog mergeExampleSetIntoLog(XLog xLog, ExampleSet es,
			String nameIDcolumn, Attribute idColumnAttrib) throws UndefinedParameterError {
		
		HashMap<String,XTrace> traceMap = buildTraceMap(xLog);
		
		Iterator<Example> iterator = es.iterator();
		while (iterator.hasNext()) {
			Example example = iterator.next();
			// get the case id and see if a corresponding trace can be found
			String caseid = example.getValueAsString(idColumnAttrib);
			XTrace t = traceMap.get(caseid);
			if (t != null) {
				Attributes attributes = example.getAttributes();
				Iterator<Attribute> iterator2 = attributes.iterator();
				while (iterator2.hasNext()) {
					Attribute attrib = iterator2.next();
					XAttribute newAttrib = null;
					if (!attrib.getName().equals(getParameterAsString(PARAMETER_1))) {
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
								|| attrib.getValueType() == Ontology.BINOMINAL
								|| attrib.getValueType() == Ontology.STRING
								|| attrib.getValueType() == Ontology.POLYNOMINAL) {
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
						t.getAttributes().put(attrib.getName(), newAttrib);
					}
				}
			}
		}
		return xLog;
	}

}
