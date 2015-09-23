package com.rapidminer.operator.conversionplugins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

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
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.parameters.ParameterString;
import com.rapidminer.tools.LogService;

public class AddTableColumnToXLog extends Operator {
	
	private List<Parameter> parametersAddTableColumnToXLog = null;
	
	private String idColumn = "";
	private String sourceColumn = "";
	
	private InputPort inputExampleSet = getInputPorts().createPort("example set (Data Table)", new ExampleSetMetaData());
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputXLog = getOutputPorts().createPort("event log (ProM Event Log)");

	public AddTableColumnToXLog(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputXLog, XLogIOObject.class));
	}
	
	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Add Table Column to XLog", LogService.NOTE);
		ExampleSet es = inputExampleSet.getData(ExampleSet.class);
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		XLog log = XLogdata.getData();
		// create new log
		XLog result = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		for (XTrace t : log) {
			XTrace copy = XFactoryRegistry.instance().currentDefault().createTrace(t.getAttributes());
			result.add(copy);
			for (XEvent e : t) {
				XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent(e.getAttributes());
				copy.add(copyEvent);
			}
		}
		getConfiguration(parametersAddTableColumnToXLog);
		// find associated attribute
		Attribute columnAttrib = null;
		Attribute columnCaseID = null;
		// print all column names
		System.out.println("column names");
		Iterator<Attribute> iterator2 = es.getAttributes().iterator();
		while (iterator2.hasNext()) {
			Attribute next = iterator2.next();
			System.out.println(next.getName());
		}
		Iterator<Attribute> iteratorAttributes = es.getAttributes().iterator();
		while (iteratorAttributes.hasNext()) {
			Attribute attrib = iteratorAttributes.next();
			if (attrib.getName().equals(sourceColumn)) {
				columnAttrib = attrib;
				break;
			}
		}
		Iterator<Attribute> iteratorAttributes2 = es.getAttributes().iterator();
		while (iteratorAttributes2.hasNext()) {
			Attribute attrib = iteratorAttributes2.next();
			if (attrib.getName().equals(idColumn)) {
				columnCaseID = attrib;
				break;
			}
		}
		if (columnAttrib != null && columnCaseID != null) {
			Iterator<Example> iterator = es.iterator();
			while (iterator.hasNext()) {
				Example row = iterator.next();
				String caseID = row.getValueAsString(columnCaseID);
				String valueAsString = row.getValueAsString(columnAttrib);
				// search for associated trace in log
				for (XTrace t : result) {
					String nameTrace = XConceptExtension.instance().extractName(t);
					if (nameTrace.equals(caseID)) {
						// found, add attribute
						XAttributeLiteralImpl newAttrib = new XAttributeLiteralImpl(sourceColumn, valueAsString);
						t.getAttributes().put(sourceColumn, newAttrib);
						break;
					}
				}
			}
		}
		else {
			// give warning and return original log
			JOptionPane.showMessageDialog(null, "Case ID column or column for attribute not found", "Column not found",JOptionPane.ERROR_MESSAGE);
			result = log;
		}
		XLogIOObject resultIOObject = new XLogIOObject(result);
		outputXLog.deliver(resultIOObject);
	}
	
	public List<ParameterType> getParameterTypes() {
		this.parametersAddTableColumnToXLog = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterString parameter1 = new ParameterString("", String.class, "Name column of case id","Case id");
		ParameterTypeString parameterType1 = new ParameterTypeString(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parametersAddTableColumnToXLog.add(parameter1);
		
		ParameterString parameter2 = new ParameterString("", String.class, "Name of the source column to be added to the log","source column to be added to the log");
		ParameterTypeString parameterType2 = new ParameterTypeString(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getDefaultValueParameter());
		parameterTypes.add(parameterType2);
		parametersAddTableColumnToXLog.add(parameter2);
		
		return parameterTypes;
	}
	
	private void getConfiguration(List<Parameter> parametersAddTableColumnToXLog) {
		try {
			Parameter parameter1 = parametersAddTableColumnToXLog.get(0);
			String valPar1 = getParameterAsString(parameter1.getNameParameter());
			idColumn = valPar1;
			Parameter parameter2 = parametersAddTableColumnToXLog.get(1);
			String valPar2 = getParameterAsString(parameter2.getNameParameter());
			sourceColumn = valPar2;
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
	}
}
