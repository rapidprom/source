package com.rapidminer.operator.filterplugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.conversionplugins.NameListToPetrinetTask.NameComparator;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;

public class TimestampSort extends Operator {
	
	/** defining the ports */
	private InputPort inputLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputLog = getOutputPorts().createPort("event log (ProM Event Log)");
	
	public TimestampSort(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
		getTransformer().addRule( new GenerateNewMDRule(outputLog, XLogIOObject.class));
	}
	
	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do work", LogService.NOTE);
		// get the log
		XLogIOObject log = inputLog.getData(XLogIOObject.class);
		XLog promLog = log.getData();
		XLog resultLog = timeStampSortTraces(timeStampSortEvents(promLog));
		XLogIOObject result = new XLogIOObject(resultLog);
		outputLog.deliver(result);
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(result);
		logService.log("end do work", LogService.NOTE);

	}

	private XLog timeStampSortEvents(XLog log) {
		System.out.println("Time Stamp Sorting");
		final String depSortingKey = "concept:name";
		//final String depSortingKey = "org:resource"; // "org:group"
		//final String depSortingKey = "AfdelingCode"; // "org:group"
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog outputLog = factory.createLog();
		for (XExtension extension : log.getExtensions())
			outputLog.getExtensions().add(extension);

		XAttributeMap logAttributeMap = (XAttributeMap) log.getAttributes()
				.clone();
		outputLog.setAttributes(logAttributeMap);

		XTrace outputTrace;
		XAttributeMap traceAttributeMap, eventAttributeMap;
		XEvent outputEvent;

		List<Date> eventDateList = new ArrayList<Date>();
		List<Date> sortedEventDateList = new ArrayList<Date>();
		List<Integer> dateStartIndexList = new ArrayList<Integer>();
		List<Integer> dateEndIndexList = new ArrayList<Integer>();

		TreeSet<String> orgActivityNameSet = new TreeSet<String>();

		int noEvents;
		int traceIndex = 0;
		String organizationName, activityName;

		List<Boolean> isEventProcessedList = new ArrayList<Boolean>();

		for (XTrace trace : log) {
			outputTrace = factory.createTrace();
			traceAttributeMap = (XAttributeMap) trace.getAttributes().clone();
			outputTrace.setAttributes(traceAttributeMap);

			eventDateList.clear();
			sortedEventDateList.clear();
			dateStartIndexList.clear();
			dateEndIndexList.clear();

			for (XEvent event : trace) {
				XAttributeTimestampImpl timeStampAttribute = (XAttributeTimestampImpl) event
						.getAttributes().get(XTimeExtension.KEY_TIMESTAMP);
				eventDateList.add(timeStampAttribute.getValue());
			}
			sortedEventDateList.addAll(eventDateList);
			Collections.sort(sortedEventDateList);

			noEvents = trace.size();

			int startIndex = 0, endIndex = 0;
			while (startIndex < noEvents) {
				dateStartIndexList.add(startIndex);
				endIndex = startIndex;
				while (endIndex < noEvents
						&& sortedEventDateList.get(endIndex).equals(
								sortedEventDateList.get(startIndex)))
					endIndex++;
				dateEndIndexList.add(endIndex - 1);
				startIndex = endIndex;
			}

			System.out
					.println("Trace " + traceIndex + " NoEvents: " + noEvents);
			System.out.println(eventDateList.size() + " @ "
					+ sortedEventDateList.size());

			int noIntervals = dateStartIndexList.size();

			isEventProcessedList.clear();
			for (int i = 0; i < noEvents; i++)
				isEventProcessedList.add(false);

			for (int i = 0; i < noIntervals; i++) {
				System.out.println(dateStartIndexList.get(i) + " @ "
						+ dateEndIndexList.get(i) + " @ "
						+ sortedEventDateList.get(dateStartIndexList.get(i))
						+ " @ "
						+ sortedEventDateList.get(dateEndIndexList.get(i)));

				orgActivityNameSet.clear();

				for (int j = dateStartIndexList.get(i); j <= dateEndIndexList
						.get(i); j++) {
					eventAttributeMap = trace.get(j).getAttributes();

					if (eventAttributeMap.containsKey(depSortingKey))
						organizationName = eventAttributeMap.get(depSortingKey)
								.toString().toLowerCase();
					else
						organizationName = "null";

					if (eventAttributeMap.containsKey("concept:name"))
						activityName = eventAttributeMap.get("concept:name")
								.toString().toLowerCase();
					else
						activityName = "null";

					orgActivityNameSet.add(organizationName + "@"
							+ activityName);
				}

				// Now the set orgActivityNameSet contains the sorted names
				for (String orgActivityName : orgActivityNameSet) {
					for (int j = dateStartIndexList.get(i); j <= dateEndIndexList
							.get(i); j++) {
						if (isEventProcessedList.get(j))
							continue;

						eventAttributeMap = trace.get(j).getAttributes();

						if (eventAttributeMap.containsKey(depSortingKey))
							organizationName = eventAttributeMap.get(
									depSortingKey).toString();
						else
							organizationName = "null";

						if (eventAttributeMap.containsKey("concept:name"))
							activityName = eventAttributeMap
									.get("concept:name").toString();
						else
							activityName = "null";

						if (orgActivityName.equalsIgnoreCase(organizationName
								+ "@" + activityName)) {
							outputTrace.add((XEvent) trace.get(j).clone());
							isEventProcessedList.set(j, true);
						}
					}
				}
			}

			traceIndex++;
			// if(traceIndex == 10)
			// break;
			outputLog.add(outputTrace);
		}

		System.out.println("-----------");
		int noTraces = log.size();
		for (int i = 0; i < noTraces; i++)
			System.out.println("Trace " + i + " " + log.get(i).size() + " @ "
					+ outputLog.get(i).size());
		
		return outputLog;

//		context.getProvidedObjectManager().createProvidedObject(
//				"Log with Sorted Activity Names Based on TimeStamps",
//				outputLog, XLog.class, context);
//		context.getGlobalContext().getResourceManager()
//				.getResourceForInstance(outputLog).setFavorite(true);
	}
	
	private XLog timeStampSortTraces(XLog log) {
		
		List<XTrace> traces = new Vector<XTrace>();
		Iterator<XTrace> iterator = log.iterator();
		while(iterator.hasNext())
			traces.add(iterator.next());
		
		Collections.sort(traces, new TraceComparator());
		
		for(int i = 0 ; i < log.size() ; i++)
			log.set(i, traces.get(i));
		
		return log;
	}	
		
	public class TraceComparator implements Comparator<XTrace> {

		@Override
		public int compare(XTrace t1, XTrace t2) {
			
			Date d1 = ((XAttributeTimestampImpl)t1.get(0).getAttributes().get(XTimeExtension.KEY_TIMESTAMP)).getValue();
			Date d2 = ((XAttributeTimestampImpl)t2.get(0).getAttributes().get(XTimeExtension.KEY_TIMESTAMP)).getValue();
			
			return d1.compareTo(d2);
		}

	}
		
		
		
		
		
		
		
		
		
		
		
		
}

	
	
	