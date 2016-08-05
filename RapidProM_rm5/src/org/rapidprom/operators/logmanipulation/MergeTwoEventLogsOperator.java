package org.rapidprom.operators.logmanipulation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.ports.metadata.XLogIOObjectMetaData;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.LogService;

public class MergeTwoEventLogsOperator extends Operator {

	private static final String PARAMETER_1_KEY = "Merge traces with same identifier",
			PARAMETER_1_DESCR  = "If two traces have the same identifier, the traces are merged (true) and their event will be put under the same collection, or they are kept separately as independent traces (false).";

	private InputPort inputLog1 = getInputPorts().createPort(
			"event log 1 (ProM Event Log)", XLogIOObject.class);
	private InputPort inputLog2 = getInputPorts().createPort(
			"event log 2 (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputLog = getOutputPorts().createPort(
			"event log (ProM Event Log)");

	public MergeTwoEventLogsOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputLog, XLogIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: merge event logs");
		long time = System.currentTimeMillis();
		
		MetaData md1 = inputLog1.getMetaData();

		XLogIOObject logIO1 = inputLog1.getData(XLogIOObject.class);
		XLog xLog1 = logIO1.getArtifact();
		XLogIOObject logIO2 = inputLog2.getData(XLogIOObject.class);
		XLog xLog2 = logIO2.getArtifact();

		// configuration
		boolean dontMergeDouble = !getParameterAsBoolean(PARAMETER_1_KEY);
		// first copy entire log1
		XLog result = XFactoryRegistry.instance().currentDefault()
				.createLog(xLog1.getAttributes());
		
		
		Set<XEventClassifier> classifiers = new HashSet<XEventClassifier>();
		classifiers.addAll(xLog1.getClassifiers());
		classifiers.addAll(xLog2.getClassifiers());
		
		result.getClassifiers().addAll(classifiers);
		
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

		for (XTrace t : xLog2) {
			copyIntoFirstLog(t, result, dontMergeDouble);
		}
		// report the result
		XLogIOObject xLogIOObject = new XLogIOObject(result,
				logIO1.getPluginContext());
		xLogIOObject
				.setVisualizationType(XLogIOObjectVisualizationType.EXAMPLE_SET);
		
		XLogIOObjectMetaData mdC = null;
		if (md1 != null && md1 instanceof XLogIOObjectMetaData)
			mdC = (XLogIOObjectMetaData) md1;
		
		if (mdC != null) {
			mdC.getXEventClassifiers().clear();
			mdC.getXEventClassifiers().addAll(classifiers);
			outputLog.deliverMD(md1);
		}
		
		outputLog.deliver(xLogIOObject);

		logger.log(Level.INFO,
				"End: merge event logs (" + (System.currentTimeMillis() - time)
						/ 1000 + " sec)");

	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(
				PARAMETER_1_KEY, PARAMETER_1_DESCR, false);
		parameterTypes.add(parameterType1);

		return parameterTypes;
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
