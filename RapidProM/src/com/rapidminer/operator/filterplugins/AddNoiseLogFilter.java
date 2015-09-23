package com.rapidminer.operator.filterplugins;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.parameters.ParameterCategory;
import com.rapidminer.parameters.ParameterInteger;
import com.rapidminer.tools.LogService;

public class AddNoiseLogFilter extends Operator {
	
	public static final String HEAD = "Remove Head";
	public static final String BODY = "Remove Body";
	public static final String EXTRA = "Add Event";
	public static final String SWAP = "Swap Tasks";
	public static final String REMOVE = "Remove Task";
	
	String noiseType = REMOVE;
	private int seed = 123456789;
	private double noisePercentage = 0.05;
	
	private List<Parameter> parametersNoiseLogFilter = null;
	
	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputEventLog = getOutputPorts().createPort("event log (ProM Event Log)");
	
	public AddNoiseLogFilter(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputEventLog, XLogIOObject.class));
	}
	
	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Add Noise Log Filter", LogService.NOTE);
		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
		XLog log = XLogdata.getData();
		getConfiguration(parametersNoiseLogFilter);
		System.out.println("NOISE FILTER: " + this.noiseType + "," + this.noisePercentage + "," + this.seed);
		XLog filterLog = filterLog(log,noiseType);
		XLogIOObject result = new XLogIOObject(filterLog);
		outputEventLog.deliver(result);
		logService.log("end do work Add Noise Log Filter", LogService.NOTE);
	}

	private XLog filterLog(XLog log, String noiseType) {
		XLog result = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		XFactoryRegistry.instance().setCurrentDefault(new XFactoryNaiveImpl());
		int traceCounter = 0;
		Random rOverall = new Random(seed);
		for (XTrace t : log) {
			XTrace copy = XFactoryRegistry.instance().currentDefault().createTrace(t.getAttributes());
			Random r = new Random(seed + new Integer(traceCounter).hashCode());
			double nextDouble = rOverall.nextDouble();
//			System.out.println("nextDouble:" + nextDouble);
			if (nextDouble < noisePercentage) {
				double oneThird = t.size() / 3.0;
				if (noiseType.equals(HEAD)) {
					int start = safeNextInt(r, (int) oneThird);
					for (int i=start; i<t.size(); i++) {
						XEvent e = t.get(i);
						XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent(e.getAttributes());
						copy.add(copyEvent);
					}
				}
				else if (noiseType.equals(BODY)) {
					int stopFirst = safeNextInt(r,(int) oneThird);
					for (int i=0; i<stopFirst; i++) {
						XEvent e = t.get(i);
						XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent(e.getAttributes());
						copy.add(copyEvent);
					}
					
					int startLast = t.size() - safeNextInt(r,(int) oneThird);
					for (int i=startLast; i<t.size(); i++) {
						XEvent e = t.get(i);
						XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent(e.getAttributes());
						copy.add(copyEvent);
					}
				}
				else if (noiseType.equals(EXTRA)) {
					for (XEvent e : t) {
						XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent(e.getAttributes());
						copy.add(copyEvent);
					}
					// add event
					int pos = safeNextInt(r,t.size());
					
					System.out.println("Pos: " + pos);

					// get the previous event to check for timestamp
					Date lowb = (pos != 0) ? XTimeExtension.instance().extractTimestamp(copy.get(pos - 1)) : null;
					Date upb = (pos != t.size()) ? XTimeExtension.instance().extractTimestamp(copy.get(pos)) : null;
					
//					if(lowb!= null)
//						System.out.println("Low: " + lowb.toString());
//					
//					if(upb!=null)
//						System.out.println("Up: " + upb.toString());
					
					if ((lowb != null) && (upb != null)) {
						// the new event has timestamp in between					
						copy.add(pos, createEvent(log, log.size(), r, new Date((upb.getTime() + lowb.getTime())/2), XTimeExtension.instance()));
					} else if (lowb != null) {
						// there is a lower bound
						copy.add(pos, createEvent(log, log.size(), r, new Date(lowb.getTime() + 1), XTimeExtension.instance()));
					} else if (upb != null) {
						// there is an upper bound
						copy.add(pos, createEvent(log, log.size(), r, new Date(upb.getTime() - 1), XTimeExtension.instance()));
					} else {
						// there is neither a lower or an upper bound
						copy.add(pos, createEvent(log, log.size(), r, null, XTimeExtension.instance()));
					}
				}
				else if (noiseType.equals(SWAP)) {
					int indexFirstTaskToSwap = safeNextInt(r,t.size());
                    int indexSecondTaskToSwap = safeNextInt(r,t.size());
                    XEvent firstTaskToSwap = null;
                    XEvent secondTaskToSwap = null;
                    XEvent event = null;
                    if (indexFirstTaskToSwap != indexSecondTaskToSwap) {
                    	// it makes sense to swap
                    	firstTaskToSwap = t.get(indexSecondTaskToSwap);
                    	secondTaskToSwap = t.get(indexFirstTaskToSwap);
                    	// swap also the timestamps
                    	Date firstTimestamp = XTimeExtension.instance().extractTimestamp(firstTaskToSwap);
                    	Date secondTimestamp = XTimeExtension.instance().extractTimestamp(secondTaskToSwap);
                    	
                    	for (int i=0; i<t.size(); i++) {
                    		if (i==indexFirstTaskToSwap) {
                    			event = (XEvent) firstTaskToSwap.clone();
                    			XTimeExtension.instance().assignTimestamp(event, secondTimestamp);
                    		}
                    		else if (i==indexSecondTaskToSwap) {
                    			event = (XEvent) secondTaskToSwap.clone();
                    			XTimeExtension.instance().assignTimestamp(event, firstTimestamp);
                    		}
                    		else {
                    			event = t.get(i);
                    		}
                    		XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent((XAttributeMap)event.getAttributes().clone());
    						copy.add(copyEvent);
    						
                    	}
                    }
                    else {
                    	// we still need to copy
                    	for (XEvent e : t) {
	                    	XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent((XAttributeMap) e.getAttributes().clone());
							copy.add(copyEvent);
                    	}
                    }
				}
				else {
					// remove an event
					int pos = Math.abs(r.nextInt()) % (t.size() + 1);
					for (int i=0; i<t.size(); i++) {
						if (i!=pos) {
							XEvent event = t.get(i);
							XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent(event.getAttributes());
    						copy.add(copyEvent);
						}
					}
				}
			}
			else {
				for (XEvent e : t) {
					XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent(e.getAttributes());
					copy.add(copyEvent);
				}
			}
			traceCounter++;
			result.add(copy);
		}
		return result;
	}
	
	private int safeNextInt(Random r, int maxInt) {
        return r.nextInt(maxInt > 0 ? maxInt : 1);
     }
	
	protected XEvent createEvent(XLog log, int logSize, Random rand, Date date, XTimeExtension xTime) {
		// both date are null
		XTrace tr = log.get(Math.abs(rand.nextInt()) % logSize);
		int pos = safeNextInt(rand,tr.size());
		
		if(pos==0 && pos < tr.size() -1) //so it does not create "start" events			
			pos++;
		
		XEvent newEvt = XFactoryRegistry.instance().currentDefault().createEvent((XAttributeMap) tr.get(pos).getAttributes().clone());
		if (date != null) {
			xTime.assignTimestamp(newEvt, date);
		}
		return newEvt;
	}
	
	public List<ParameterType> getParameterTypes() {
		this.parametersNoiseLogFilter = new ArrayList<Parameter>();
		List<ParameterType> parameterTypes = super.getParameterTypes();
		
		ParameterInteger parameter1 = new ParameterInteger(5, 0, 100, 1, Integer.class, "Noise Percentage", "Noise Percentage");
		ParameterTypeInt parameterType1 = new ParameterTypeInt(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getMin(), parameter1.getMax(), parameter1.getDefaultValueParameter());
		parameterTypes.add(parameterType1);
		parametersNoiseLogFilter.add(parameter1);

		Object[] par2categories = new Object[] {HEAD, BODY, EXTRA, SWAP, REMOVE};
		ParameterCategory parameter2 = new ParameterCategory(par2categories, REMOVE, String.class, "Noise Type", "Noise Type");
		ParameterTypeCategory parameterType2 = new ParameterTypeCategory(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getOptionsParameter(), parameter2.getIndexValue(parameter2.getDefaultValueParameter()));
		parameterTypes.add(parameterType2);
		parametersNoiseLogFilter.add(parameter2);
		
		ParameterInteger parameter3 = new ParameterInteger(1, 0, Integer.MAX_VALUE, 1, Integer.class, "Seed", "Seed");
		ParameterTypeInt parameterType3 = new ParameterTypeInt(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getMin(), parameter3.getMax(), parameter3.getDefaultValueParameter());
		parameterTypes.add(parameterType3);
		parametersNoiseLogFilter.add(parameter3);
		
		return parameterTypes;
	}
	
	private void getConfiguration(List<Parameter> parametersNoiseLogFilter) {
		try {
			Parameter parameter2 = parametersNoiseLogFilter.get(1);
			int par2int = getParameterAsInt(parameter2.getNameParameter());
			String valPar2 = (String) parameter2.getValueParameter(par2int);
			noiseType = valPar2;
			
			Parameter parameter1 = parametersNoiseLogFilter.get(0);
			int par1int = getParameterAsInt(parameter1.getNameParameter());
			noisePercentage = (double) par1int / 100.0;
			
			Parameter parameter3 = parametersNoiseLogFilter.get(2);
			int par3int = getParameterAsInt(parameter3.getNameParameter());
			seed = par3int;
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
	}
	
}
