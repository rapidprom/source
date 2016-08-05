package org.rapidprom.operators.logmanipulation;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.rapidprom.ioobjects.XLogIOObject;

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

public class AddArtificialStartEndEventOperator extends Operator {

	private static final String PARAMETER_1_KEY = "Add Start Event",
			PARAMETER_1_DESCR = "Adds a \"start\" event before the first event of the trace.",
			PARAMETER_2_KEY = "Add End Event",
			PARAMETER_2_DESCR = "Adds an \"end\" event after the last event of the trace.";

	private InputPort inputXLog = getInputPorts()
			.createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputEventLog = getOutputPorts()
			.createPort("event log (ProM Event Log)");

	public AddArtificialStartEndEventOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputEventLog, XLogIOObject.class));
	}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: add artificial start and end event to all traces");
		long time = System.currentTimeMillis();
		
		MetaData md = inputXLog.getMetaData();

		XLogIOObject xLogIOObject = inputXLog.getData(XLogIOObject.class);
		XLog logOriginal = xLogIOObject.getArtifact();
		XLog logModified = filterLog(logOriginal);
		XLogIOObject result = new XLogIOObject(logModified,
				xLogIOObject.getPluginContext());
		
		outputEventLog.deliverMD(md);
		outputEventLog.deliver(result);
		logger.log(Level.INFO,
				"End: add artificial start and end event to all traces ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(
				PARAMETER_1_KEY, PARAMETER_1_DESCR, true);
		parameterTypes.add(parameterType1);

		ParameterTypeBoolean parameterType3 = new ParameterTypeBoolean(
				PARAMETER_2_KEY, PARAMETER_2_DESCR, true);
		parameterTypes.add(parameterType3);

		return parameterTypes;
	}

	private XLog filterLog(XLog log) {
		XAttributeMap logattlist = copyAttMap(log.getAttributes());
		XLog newLog = new XLogImpl(logattlist);
		for (int i = 0; i < log.size(); i++) {
			XTrace oldTrace = log.get(i);
			XTrace newTrace = new XTraceImpl(
					copyAttMap(oldTrace.getAttributes()));
			String name = XConceptExtension.instance().extractName(oldTrace);
			System.out.println("ADD ARTIFICIAL EVENT: TRACE" + name + ", size: "
					+ oldTrace.size());
			// add start event

			Date time = new Date();
			boolean changed = false;
			if (getParameterAsBoolean(PARAMETER_1_KEY)) {
				try {
					time = getTime(oldTrace.get(0));
					if (time != null) {
						time.setTime(time.getTime() - 1);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				newTrace.add(makeEvent("START", time));
				for (int j = 0; j < oldTrace.size(); j++) {
					XEvent oldEvent = oldTrace.get(j);
					XEvent newEvent = new XEventImpl(
							copyAttMap(oldEvent.getAttributes()));
					newTrace.add(newEvent);
				}
				changed = true;
			}

			// add end event
			if (getParameterAsBoolean(PARAMETER_2_KEY)) {
				time = new Date();
				try {
					time = getTime(oldTrace.get(oldTrace.size() - 1));
					if (time != null) {
						time.setTime(time.getTime() + 1);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				newTrace.add(makeEvent("END", time));
				changed = true;
			}
			if (changed)
				newLog.add(newTrace);
		}
		return newLog;
	}

	private XEvent makeEvent(String name, Date time) {
		XAttributeMap attMap = new XAttributeMapImpl();
		putLiteral(attMap, "concept:name", name);
		putLiteral(attMap, "lifecycle:transition", "complete");
		putLiteral(attMap, "org:resource", "artificial");
		if (time != null) {
			putTimestamp(attMap, "time:timestamp", time);
		}
		XEvent newEvent = new XEventImpl(attMap);
		return newEvent;
	}

	public static XAttributeMap copyAttMap(XAttributeMap srcAttMap) {
		XAttributeMap destAttMap = new XAttributeMapImpl();
		Iterator<XAttribute> attit = srcAttMap.values().iterator();
		while (attit.hasNext()) {
			XAttribute att = attit.next();
			String key = att.getKey();
			att = (XAttribute) att.clone();
			destAttMap.put(key, att);
		}
		return destAttMap;
	}

	public static Date getTime(XEvent event) {
		Date res = new Date();
		try {
			res = XTimeExtension.instance().extractTimestamp(event);
		} catch (Exception ex) {
		}
		return res;
	}

	public static void putLiteral(XAttributeMap attMap, String key,
			String value) {
		attMap.put(key, new XAttributeLiteralImpl(key, value));
	}

	public static void putTimestamp(XAttributeMap attMap, String key,
			Date value) {
		attMap.put(key, new XAttributeTimestampImpl(key, value));
	}

}
