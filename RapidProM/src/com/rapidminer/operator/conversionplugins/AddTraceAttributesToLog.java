package com.rapidminer.operator.conversionplugins;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
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

public class AddTraceAttributesToLog extends Operator {

	private List<Parameter> parameters = null;
	private String nameIDcolumn = "";

	private InputPort inputExampleSet = getInputPorts().createPort(
			"example set (Data Table)", new ExampleSetMetaData());
	private InputPort inputLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputLog = getOutputPorts().createPort(
			"event log (ProM Event Log)");

	public AddTraceAttributesToLog(OperatorDescription description) {
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
		Attribute idColumnAttrib = null;
		boolean found = false;
		Iterator<Attribute> iterator = es.getAttributes().iterator();
		while (iterator.hasNext()) {
			Attribute next = iterator.next();
			if (next.getName().equals(nameIDcolumn)) {
				idColumnAttrib = next;
				found = true;
				break;
			}
		}

		if (found) {
			XLog adaptedLog = mergeExampleSetIntoLog(xLog, es, nameIDcolumn,
					idColumnAttrib);
			XLogIOObject xLogIOObject = new XLogIOObject(adaptedLog);
			xLogIOObject.setPluginContext(null);
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.EXAMPLE_SET);
			outputLog.deliver(xLogIOObject);
			// add to list so that afterwards it can be cleared if needed
			ProMIOObjectList instance = ProMIOObjectList.getInstance();
			instance.addToList(xLogIOObject);
		} else {
			// show warning
			JOptionPane.showMessageDialog(null, "Case ID was not found",
					"Case ID column not found", JOptionPane.ERROR_MESSAGE);
			outputLog.deliver(null);
		}
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

		return parameterTypes;
	}

	private void getConfiguration(List<Parameter> parameters) {
		try {
			Parameter parameter1 = parameters.get(0);
			String valPar1 = getParameterAsString(parameter1.getNameParameter());
			nameIDcolumn = valPar1;
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
	}

	private XLog mergeExampleSetIntoLog(XLog xLog, ExampleSet es,
			String nameIDcolumn, Attribute idColumnAttrib) {
		Iterator<Example> iterator = es.iterator();
		while (iterator.hasNext()) {
			Example example = iterator.next();
			// get the case id and see if a corresponding trace can be found
			String caseid = example.getValueAsString(idColumnAttrib);
			XTrace t = findTrace(caseid, xLog);
			if (t != null) {
				Attributes attributes = example.getAttributes();
				Iterator<Attribute> iterator2 = attributes.iterator();
				while (iterator2.hasNext()) {
					Attribute attrib = iterator2.next();
					XAttribute newAttrib = null;
					if (!attrib.getName().equals(this.nameIDcolumn)) {
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
