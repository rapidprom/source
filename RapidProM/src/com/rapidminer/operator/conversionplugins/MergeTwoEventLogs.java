package com.rapidminer.operator.conversionplugins;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.ioobjectrenderers.XLogIOObjectVisualizationType;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.parameters.ParameterBoolean;
import com.rapidminer.parameters.ParameterString;
import com.rapidminer.util.ProMIOObjectList;

public class MergeTwoEventLogs extends Operator {

	private List<Parameter> parameters = null;

	private InputPort inputLog1 = getInputPorts().createPort(
			"event log 1 (ProM Event Log)", XLogIOObject.class);
	private InputPort inputLog2 = getInputPorts().createPort(
			"event log 2 (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputLog = getOutputPorts().createPort(
			"event log (ProM Event Log)");

	public MergeTwoEventLogs(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputLog, XLogIOObject.class));
	}

	public void doWork() throws OperatorException {
		XLogIOObject logIO1 = inputLog1.getData(XLogIOObject.class);
		XLog xLog1 = logIO1.getXLog();
		XLogIOObject logIO2 = inputLog2.getData(XLogIOObject.class);
		XLog xLog2 = logIO2.getXLog();

		// configuration
		boolean dontMergeDouble = getConfiguration(this.parameters);
		// first copy entire log1
		XLog result = XFactoryRegistry.instance().currentDefault()
				.createLog(xLog1.getAttributes());
		for (XTrace t : xLog1) {
			XTrace copy = XFactoryRegistry.instance().currentDefault()
					.createTrace(t.getAttributes());
			result.add(copy);
			for (XEvent e : t) {
				XEvent copyEvent = XFactoryRegistry.instance().currentDefault()
						.createEvent(e.getAttributes());
				copy.add(copyEvent);
			}
		}

		// copy log 2 into the copied log1
		for (XTrace t : xLog2) {
			copyIntoFirstLog(t, result, dontMergeDouble);
		}
		// report the result
		XLogIOObject xLogIOObject = new XLogIOObject(result);
		xLogIOObject.setPluginContext(null);
		xLogIOObject
				.setVisualizationType(XLogIOObjectVisualizationType.EXAMPLE_SET);
		outputLog.deliver(xLogIOObject);
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(xLogIOObject);
	}

	public List<ParameterType> getParameterTypes() {
		this.parameters = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterBoolean parameter1 = new ParameterBoolean(true, Boolean.class,
				"Don't merge traces with same identifier",
				"Don't merge trace with same identifier");
		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(
				parameter1.getNameParameter(),
				parameter1.getDescriptionParameter(),
				parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parameters.add(parameter1);
		return parameterTypes;
	}

	private boolean getConfiguration(List<Parameter> parameters) {
		Parameter parameter1 = parameters.get(0);
		Boolean valPar1 = getParameterAsBoolean(parameter1.getNameParameter());
		return valPar1;
	}

	private void copyIntoFirstLog(XTrace t, XLog result, boolean dontMergeDouble) {
		// check if in result log
		String nameTrace = XConceptExtension.instance().extractName(t);
		XTrace simTrace = null;
		for (XTrace trace : result) {
			String name = XConceptExtension.instance().extractName(trace);
			if (name.equals(nameTrace)) {
				// found trace with same name
				simTrace = trace;
				break;
			}
		}
		if (simTrace != null && !dontMergeDouble) {
			// I found a trace with similar name
			// add the events
			for (XEvent e : t) {
				XEvent copyEvent = XFactoryRegistry.instance().currentDefault()
						.createEvent(e.getAttributes());
				simTrace.add(copyEvent);
			}
		} else {
			// trace is new
			XTrace copy = XFactoryRegistry.instance().currentDefault()
					.createTrace(t.getAttributes());
			for (XEvent e : t) {
				XEvent copyEvent = XFactoryRegistry.instance().currentDefault()
						.createEvent(e.getAttributes());
				copy.add(copyEvent);
			}
			result.add(copy);
		}
	}

}
