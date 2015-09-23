package com.rapidminer.operator.filterplugins;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.parameters.ParameterBoolean;
import com.rapidminer.parameters.ParameterInteger;
import com.rapidminer.tools.LogService;

public class AddArtificialStartEndEvent extends Operator {
	
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputEventLog = getOutputPorts().createPort("event log (ProM Event Log)");
	
	private List<Parameter> parameters = null;
	
	public AddArtificialStartEndEvent(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputEventLog, XLogIOObject.class));
	}
	
	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start add artificial event Filter", LogService.NOTE);
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		XLog log = XLogdata.getData();
		XLog filterLog = filterLog(log);
		XLogIOObject result = new XLogIOObject(filterLog);
		outputEventLog.deliver(result);
		logService.log("end do work Add Noise Log Filter", LogService.NOTE);
	}
	
	public List<ParameterType> getParameterTypes() {
		this.parameters = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterBoolean parameter1 = new ParameterBoolean(true, Boolean.class, "Add Start Event", "Add Start Event");
		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parameters.add(parameter1);
		
		ParameterBoolean parameter3 = new ParameterBoolean(true, Boolean.class, "Add End Event", "Add End Event");
		ParameterTypeBoolean parameterType3 = new ParameterTypeBoolean(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getDefaultValueParameter());
		parameterTypes.add(parameterType3);
		parameters.add(parameter3);
		
		return parameterTypes;
	}
	
	private boolean getStartValue(List<Parameter> parameters) {
		Parameter parameter1 = parameters.get(0);
		Boolean valPar1 = getParameterAsBoolean(parameter1.getNameParameter());
		return valPar1;
	}
	
	private boolean getEndValue(List<Parameter> parameters) {
		Parameter parameter1 = parameters.get(1);
		Boolean valPar1 = getParameterAsBoolean(parameter1.getNameParameter());
		return valPar1;
	}

	private XLog filterLog(XLog log) {
		XAttributeMap logattlist = copyAttMap(log.getAttributes());
		XLog newLog = new XLogImpl(logattlist);
		for (int i = 0; i < log.size(); i++) {
			XTrace oldTrace = log.get(i);
			XTrace newTrace = new XTraceImpl(copyAttMap(oldTrace.getAttributes()));
			String name = XConceptExtension.instance().extractName(oldTrace);
			System.out.println("ADD ARTIFICIAL EVENT: TRACE" + name + ", size: " + oldTrace.size());
			// add start event
		
			Date time = new Date();
			boolean changed = false;
			if(getStartValue(parameters))
			{
				try {
					time = getTime(oldTrace.get(0));
					if (time != null) {
						time.setTime(time.getTime() - 1);
					}
				} 
				catch (Exception ex) {
					ex.printStackTrace();
				}
				newTrace.add(makeEvent("START", time));
				for (int j = 0; j < oldTrace.size(); j++) {
					XEvent oldEvent = oldTrace.get(j);
					XEvent newEvent = new XEventImpl(copyAttMap(oldEvent.getAttributes()));
					newTrace.add(newEvent);
				}
				changed = true;
			}
			
			// add end event
			if(getEndValue(parameters))
			{
				time = new Date();
				try {
					time = getTime(oldTrace.get(oldTrace.size() - 1));
					if (time != null) {
						time.setTime(time.getTime() + 1);
					}
				} 
				catch (Exception ex) {
					ex.printStackTrace();
				}
				newTrace.add(makeEvent("END", time));
				changed = true;
			}
			if(changed)
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
	
	public static void putLiteral(XAttributeMap attMap, String key, String value) {
		attMap.put(key, new XAttributeLiteralImpl(key, value));
	}
	
	public static void putTimestamp(XAttributeMap attMap, String key, Date value) {
		attMap.put(key, new XAttributeTimestampImpl(key, value));
	}

}
