package com.rapidminer.operator.conversionplugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractDataReader.AttributeColumn;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.container.Range;

public class CaseDataExtractor extends Operator  {
	
	/** defining the ports */
	private InputPort inputLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort output = getOutputPorts().createPort("example set (Data Table)");
	
	
	private ExampleSetMetaData metaData = null;
	private Attribute[] attributes;
	
	private TableModel tm = null;
	private Map<XEventClass,Set<String>> mappingAttributesEventClass = new HashMap<XEventClass,Set<String>>();
	
	/**
	 * The default constructor needed in exactly this signature
	 */
	public CaseDataExtractor(OperatorDescription description) {
		super(description);
		/** Adding a rule for the output */
		getTransformer().addRule( new GenerateNewMDRule(output, ExampleSet.class));
	}
	
	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do case data extractor", LogService.NOTE);
		// get the log
		XLogIOObject log = inputLog.getData(XLogIOObject.class);
		XLog promLog = log.getPromLog();
		MemoryExampleTable table = null;
		ExampleSet es = null;
		try {
			// create the exampleset`
			XLogInfo summary = XLogInfoFactory.createLogInfo(promLog);
			createMappingEventClassesAndAttributes(summary,promLog);
			table = createStructureTable(promLog, summary);
			es = fillTable(table, promLog);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error when creating exampleset, creating empty exampleset");
			List<Attribute> attributes = new LinkedList<Attribute>();
			table = new MemoryExampleTable(attributes);
			es = table.createExampleSet();
		}
		/** Adding a rule for the output */
		getTransformer().addRule( new GenerateNewMDRule(output, this.metaData));
		output.deliver(es);
		logService.log("end do transformation XLog to ExampleSet", LogService.NOTE);

	}

	private void createMappingEventClassesAndAttributes(XLogInfo summary,
			XLog log) {
		for (int i=0; i<summary.getNameClasses().size(); i++) {
			XEventClass ec = summary.getNameClasses().getByIndex(i);
			mappingAttributesEventClass.put(ec, new TreeSet<String>());
		}
		for (XTrace t : log) {
			for (XEvent e : t) {
				XEventClass c = summary.getNameClasses().getClassOf(e);
				Set<String> set = mappingAttributesEventClass.get(c);
				Iterator<String> iterator = e.getAttributes().keySet().iterator();
				while (iterator.hasNext()) {
					String next = iterator.next();
					if (next.equals("concept:name") || next.equals("lifecycle:transition") || next.equals("time:timestamp") ||
							next.equals("org:resource") || next.equals("org:group") || next.equals("org:role")) {
						// ignore
					}
					else {
						set.add(next);
					}
				}
			}
		}
	}

	private ExampleSet fillTable(MemoryExampleTable table, XLog log) {
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
		// for each trace add the information
		for (XTrace t : log) {
			System.out.println("name:" + XConceptExtension.instance().extractName(t));
			String[] strings = new String[tm.getColumnCount()];
			// add the name
			String name = XConceptExtension.instance().extractName(t);
			strings[0] = name;
			// add the number of events
			strings[1] = t.size() + "";
			// sojourn time
			long startTime = XTimeExtension.instance().extractTimestamp(t.get(0)).getTime();
			long endTime = XTimeExtension.instance().extractTimestamp(t.get(t.size()-1)).getTime();
			strings[2] =  endTime-startTime + "";
			// now for the data
			Iterator<String> iterator = t.getAttributes().keySet().iterator();
			while (iterator.hasNext()) {
				String next = iterator.next();
				// now search for the right column
				int numberColumn = tm.getNumberColumn("T:data." + next);
				if (numberColumn > -1 && numberColumn < strings.length) {
					XAttribute xAttribute = t.getAttributes().get(next);
					if (xAttribute instanceof XAttributeLiteral) {
						XAttributeLiteral attribLit = (XAttributeLiteral) xAttribute; 
						String value = attribLit.getValue();
						strings[numberColumn] = value;
					}
					if (xAttribute instanceof XAttributeBoolean) {
						XAttributeBoolean attribBool = (XAttributeBoolean) xAttribute; 
						boolean value = attribBool.getValue();
						strings[numberColumn] = value + "";
					}
					if (xAttribute instanceof XAttributeContinuous) {
						XAttributeContinuous attribCont = (XAttributeContinuous) xAttribute; 
						double value = attribCont.getValue();
						strings[numberColumn] = value + "";
					}
					if (xAttribute instanceof XAttributeDiscrete) {
						XAttributeDiscrete attribDisc = (XAttributeDiscrete) xAttribute; 
						long value = attribDisc.getValue();
						strings[numberColumn] = value + "";
					}
					if (xAttribute instanceof XAttributeTimestamp) {
						XAttributeTimestamp attribTs = (XAttributeTimestamp) xAttribute; 
						long value = attribTs.getValue().getTime();
						strings[numberColumn] = value + "";
					}
				}
			}
			// now for the events
			for (int i=0; i<tm.getColumnCount(); i++) {
				Object descriptionColumn = tm.getDescriptionColumn(i);
				if (descriptionColumn instanceof EventRow) {
					EventRow er = (EventRow) descriptionColumn;
					String nameAct = er.getNameActivity();
					if (er.isActivityName()) {
						// do nothing now
					}
					else if (er.isNrInstances) {
						// count the number of occurrences
						int counter = 0;
						for (XEvent e : t) {
							String nameEvt = XConceptExtension.instance().extractName(e);
							if (nameEvt.equals(nameAct)) {
								counter++;
							}
						}
						strings[i] = counter+"";
					}
					else if (er.isTimestamp) {
						// get the timestamp of the last occurrence (in seconds)
						long ts = -1;
						for (XEvent e : t) {
							String nameEvt = XConceptExtension.instance().extractName(e);
							if (nameEvt.equals(nameAct)) {
								ts = XTimeExtension.instance().extractTimestamp(e).getTime() / 1000;
							}
						}
						if (ts==-1) {
							strings[i] = "";
						}
						else {
							strings[i] = ts+"";
						}
					}
					else if (er.isRelativeTimestamp) {
						// get the timestamp of the first event
						long timeFirst = XTimeExtension.instance().extractTimestamp(t.get(0)).getTime();
						// get the timestamp of the last occurrence (in seconds)
						long ts = -1;
						for (XEvent e : t) {
							String nameEvt = XConceptExtension.instance().extractName(e);
							if (nameEvt.equals(nameAct)) {
								ts = XTimeExtension.instance().extractTimestamp(e).getTime();
							}
						}
						if (ts != -1) {
							long relative = (ts - timeFirst) / 1000;
							strings[i] = relative + "";
						}
						else {
							strings[i] = "";
						}
					}
					else if (er.isLc && er.isResource) {
						// indicate if exists or not
						int counter = 0;
						for (XEvent e : t) {
							String nameEvt = XConceptExtension.instance().extractName(e);
							String resource = XOrganizationalExtension.instance().extractResource(e);
							String transition = XLifecycleExtension.instance().extractTransition(e);
							if (nameEvt.equals(nameAct) && er.getResource().equals(resource) && er.getLc().equals(transition)) {
								// found 
								counter++;
							}
						}
						strings[i]=counter+"";
					}
					else if (er.isData) {
						String nameData = er.getNameData();
						// search for the event and its data
						String valueData = "";
						for (XEvent e : t) {
							String nameEvt = XConceptExtension.instance().extractName(e);
							if (nameEvt.equals(nameAct) && e.getAttributes().containsKey(nameData)) {
								// get the one
								XAttribute xAttribute = e.getAttributes().get(nameData);
								if (xAttribute instanceof XAttributeLiteral) {
									XAttributeLiteral attribLit = (XAttributeLiteral) xAttribute; 
									//valueData = attribLit.getValue();
									valueData = valueData + attribLit.getValue() + ",";
								}
								if (xAttribute instanceof XAttributeBoolean) {
									XAttributeBoolean attribBool = (XAttributeBoolean) xAttribute; 
									//valueData = attribBool.getValue() + "";
									valueData = valueData + attribBool.getValue() + ",";
								}
								if (xAttribute instanceof XAttributeContinuous) {
									XAttributeContinuous attribCont = (XAttributeContinuous) xAttribute; 
									//valueData = attribCont.getValue() + "";
									valueData = valueData + attribCont.getValue() + ",";
								}
								if (xAttribute instanceof XAttributeDiscrete) {
									XAttributeDiscrete attribDisc = (XAttributeDiscrete) xAttribute; 
									//valueData = attribDisc.getValue() + "";
									valueData = valueData + attribDisc.getValue() + ",";
								}
								if (xAttribute instanceof XAttributeTimestamp) {
									XAttributeTimestamp attribTs = (XAttributeTimestamp) xAttribute; 
									//valueData = attribTs.getValue().getTime() + "";
									valueData = valueData + attribTs.getValue() + ",";
								}
							}
						}
						if (!valueData.equals("") && valueData.charAt(valueData.length()-1) == ',') {
							char charAt = valueData.charAt(valueData.length()-1);
							valueData = valueData.substring(0, valueData.length()-1);
							strings[i] = valueData;
							System.out.println("data:"+nameData + "," + valueData);
						}
						else {
							strings[i] = valueData;
							System.out.println("data:"+nameData + "," + valueData);
						}
					}
				}
			}
			DataRow dataRow = factory.create(strings, attributes);
			System.out.println("DATAROW");
			System.out.println(dataRow.toString());
			if (name.equals("100")) {
				// print everything
				for (int i=0; i<attributes.length; i++) {
					if (attributes[i].isNominal()) {
						double d = dataRow.get(attributes[i]);
						System.out.println(attributes[i].getName() + "," + strings[i]);
					}
					else {
						System.out.println(attributes[i].getName() + "," + strings[i]);
					}
				}
			}
			table.addDataRow(dataRow);
		}
		ExampleSet createExampleSet = table.createExampleSet();
		//createExampleSet.getExample(100).getValueAsString(attribute);
		return createExampleSet;
	}

	private MemoryExampleTable createStructureTable(XLog promLog, XLogInfo summary) {
		int numberOfTraces = summary.getNumberOfTraces();
		
		ExampleSetMetaData metaData = new ExampleSetMetaData();
		List<Attribute> attributes = new LinkedList<Attribute>();
		AttributeMetaData amd = null;
		this.tm = new TableModel(numberOfTraces);
		// first for the log
		// identifier of the trace
		attributes.add(AttributeFactory.createAttribute("T:concept:name", Ontology.NOMINAL));
		amd = new AttributeMetaData("T:concept:name", Ontology.NOMINAL);
		amd.setRole(AttributeColumn.REGULAR);
		amd.setNumberOfMissingValues(new MDInteger(0));
		tm.addDescriptionColumn(new CaseRow("T:concept:name",true,false,false,false,""));
		// number of events
		attributes.add(AttributeFactory.createAttribute("T:number_of_events", Ontology.NUMERICAL));
		amd = new AttributeMetaData("T:number_of_events", Ontology.NUMERICAL);
		amd.setRole(AttributeColumn.REGULAR);
		amd.setNumberOfMissingValues(new MDInteger(0));
		amd.setValueRange(new Range(0, Long.MAX_VALUE), SetRelation.EQUAL);
		metaData.addAttribute(amd);
		tm.addDescriptionColumn(new CaseRow("T:number_of_events",false,true,false,false,""));
		// sojourn time
		attributes.add(AttributeFactory.createAttribute("T:sojourn_time.seconds", Ontology.NUMERICAL));
		amd = new AttributeMetaData("T:sojourn_time.seconds", Ontology.NUMERICAL);
		amd.setRole(AttributeColumn.REGULAR);
		amd.setNumberOfMissingValues(new MDInteger(0));
		amd.setValueRange(new Range(0, Long.MAX_VALUE), SetRelation.EQUAL);
		metaData.addAttribute(amd);
		tm.addDescriptionColumn(new CaseRow("T:sojourn_time.seconds",false,false,true,false,""));
		// data of the trace
		Iterator<String> iterator2 = summary.getTraceAttributeInfo().getAttributeKeys().iterator();
		while (iterator2.hasNext()) {
			String next = iterator2.next();
			if (!next.equals("concept:name")) {
				attributes.add(AttributeFactory.createAttribute("T:data." + next, Ontology.NOMINAL));
				amd = new AttributeMetaData("T:data." + next, Ontology.NOMINAL);
				amd.setRole(AttributeColumn.REGULAR);
				amd.setNumberOfMissingValues(new MDInteger(0));
				tm.addDescriptionColumn(new CaseRow("T:data." + next,true,false,false,true,next));
			}
		}
		// now for the events/tasks
		for (int i=0; i<summary.getNameClasses().size(); i++) {
			XEventClass ec = summary.getNameClasses().getByIndex(i);
			// add name
//			attributes.add(AttributeFactory.createAttribute("E:concept:name" + ":" + ec.getId(), Ontology.NOMINAL));
//			amd = new AttributeMetaData("E:concept:name" + ":" + ec.getId(), Ontology.NOMINAL);
//			amd.setRole(AttributeColumn.REGULAR);
//			amd.setNumberOfMissingValues(new MDInteger(0));
//			tm.addDescriptionColumn(new EventRow("E:concept:name" + ":" + ec.getId(),true,false,false,false,false,false,false,ec.getId(),"","",""));
			// add nrInstances
			attributes.add(AttributeFactory.createAttribute("E:" + ec.getId() + ".nrInstances", Ontology.NUMERICAL));
			amd = new AttributeMetaData("E:" + ec.getId() + ".nrInstances", Ontology.NUMERICAL);
			amd.setRole(AttributeColumn.REGULAR);
			amd.setNumberOfMissingValues(new MDInteger(0));
			amd.setValueRange(new Range(0, Long.MAX_VALUE), SetRelation.EQUAL);
			metaData.addAttribute(amd);
			tm.addDescriptionColumn(new EventRow("E:" + ec.getId() + ".nrInstances",false,false,false,false,true,false,false,ec.getId(),"","",""));
			// add combination for lc and resource
			for (int j=0; j<summary.getTransitionClasses().size(); j++) {
				for (int k=0; k<summary.getResourceClasses().size(); k++) {
					XEventClass lc = summary.getTransitionClasses().getByIndex(j);
					XEventClass res = summary.getResourceClasses().getByIndex(k);
					if (lc != null && res != null) {
						// was NOMINAL
						attributes.add(AttributeFactory.createAttribute("E:" + ec.getId() + "." + lc + "." + res, Ontology.NUMERICAL));
						amd = new AttributeMetaData("E:" + ec.getId() + "." + lc + "." + res, Ontology.NUMERICAL);
						amd.setRole(AttributeColumn.REGULAR);
						amd.setNumberOfMissingValues(new MDInteger(0));
						amd.setValueRange(new Range(0, Long.MAX_VALUE), SetRelation.EQUAL);
						metaData.addAttribute(amd);
						tm.addDescriptionColumn(new EventRow("E:" + ec.getId() + "." + lc + "." + res,false,true,true,false,false,false,false,ec.getId(),lc.getId(),res.getId(),""));
					}
				}
			}
			// add for the timestamp
			attributes.add(AttributeFactory.createAttribute("E:" + ec.getId() + ".timestamp", Ontology.NUMERICAL));
			amd = new AttributeMetaData("E:" + ec.getId() + ".timestamp", Ontology.NUMERICAL);
			amd.setRole(AttributeColumn.REGULAR);
			amd.setNumberOfMissingValues(new MDInteger(0));
			amd.setValueRange(new Range(0, Long.MAX_VALUE), SetRelation.EQUAL);
			metaData.addAttribute(amd);
			tm.addDescriptionColumn(new EventRow("E:" + ec.getId() + ".timestamp",false,false,false,true,false,false,false,ec.getId(),"","",""));
			// add for the relative timestamp
			attributes.add(AttributeFactory.createAttribute("E:" + ec.getId() + ".timestamp.relative", Ontology.NUMERICAL));
			amd = new AttributeMetaData("E:" + ec.getId() + ".timestamp.relative", Ontology.NUMERICAL);
			amd.setRole(AttributeColumn.REGULAR);
			amd.setNumberOfMissingValues(new MDInteger(0));
			amd.setValueRange(new Range(0, Long.MAX_VALUE), SetRelation.EQUAL);
			metaData.addAttribute(amd);
			tm.addDescriptionColumn(new EventRow("E:" + ec.getId() + ".timestamp.relative",false,false,false,false,false,false,true,ec.getId(),"","",""));
			// add for the data
			Iterator<String> iterator = summary.getEventAttributeInfo().getAttributeKeys().iterator();
			while (iterator.hasNext()) {
				String next = iterator.next();
				// check whether the attribute exists for the event
				Set<String> set = mappingAttributesEventClass.get(ec);
				if (set != null && set.contains(next)) {
					attributes.add(AttributeFactory.createAttribute("E:data." + ec.getId() + "." + next, Ontology.NOMINAL));
					amd = new AttributeMetaData("E:data." + ec.getId() + "." + next, Ontology.NOMINAL);
					amd.setRole(AttributeColumn.REGULAR);
					amd.setNumberOfMissingValues(new MDInteger(0));
					metaData.addAttribute(amd);
					tm.addDescriptionColumn(new EventRow("E:data." + ec.getId() + "." + next,false,false,false,false,false,true,false,ec.getId(),"","",next));
				}
			}
		}
		// convert the list to array
		Attribute[] attribArray = new Attribute[attributes.size()];
		for (int i=0; i<attributes.size(); i++) {
			attribArray[i] = attributes.get(i);
		}
		metaData.setNumberOfExamples(numberOfTraces);
		this.metaData = metaData;
		this.attributes = attribArray;
		MemoryExampleTable memoryExampleTable = new MemoryExampleTable(attributes);
		return memoryExampleTable;
		
	}
	
	public class TableModel extends AbstractTableModel {
		
		/**
		 * generated
		 */
		private static final long serialVersionUID = 6419753586794813744L;
		private List<Object> descriptionColumns = new ArrayList<Object>();
		private List<Object[]> values = new ArrayList<Object[]>();
		
		public TableModel(int nrRows) {
			values = new ArrayList<Object[]>(nrRows);
		}
		
		public void addDescriptionColumn (Object obj) {
			this.descriptionColumns.add(obj);
		}
		
		public Object getDescriptionColumn (int nrColumn) {
			return this.descriptionColumns.get(nrColumn);
		}
		
		public String getNameColumn (int nrColumn) {
			Object object = this.descriptionColumns.get(nrColumn);
			if (object instanceof CaseRow) {
				CaseRow cr = (CaseRow) object;
				return cr.getNameColum();
			}
			if (object instanceof EventRow) {
				EventRow er = (EventRow) object;
				return er.getNameColumn();
			}
			return "";
		}
		
		public int getNumberColumn (String name) {
			for (int i=0; i<descriptionColumns.size(); i++) {
				Object obj = descriptionColumns.get(i);
				if (obj instanceof CaseRow) {
					CaseRow cr = (CaseRow) obj;
					if (cr.getNameColum().equals(name)) {
						// found 
						return i;
					}
				}
				if (obj instanceof EventRow) {
					EventRow er = (EventRow) obj;
					if (er.getNameColumn().equals(name)) {
						// found
						return i;
					}
				}
			}
			return -1;
		}

		@Override
		public int getRowCount() {
			return values.size();
		}

		@Override
		public int getColumnCount() {
			return descriptionColumns.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object[] list = values.get(rowIndex);
			Object object = list[columnIndex];
			return object;
		}
		
	}
	
	private class CaseRow {
		
		private String nameColum = "";
		private boolean isName = false;
		private boolean isNumberEvents = false;
		private boolean isSojournTime = false;
		private boolean isData = false;
		private String nameData = "";
		
		public CaseRow(String nameColumn, boolean isName, boolean isNumberEvents, boolean isSojournTime, boolean isData, String nameData) {
			this.nameColum = nameColumn;
			this.isName = isName;
			this.isNumberEvents = isNumberEvents;
			this.isSojournTime = isSojournTime;
			this.isData = isData;
			this.nameData = nameData;
		}

		public boolean isData() {
			return isData;
		}

		public void setData(boolean isData) {
			this.isData = isData;
		}

		public String getNameData() {
			return nameData;
		}

		public void setNameData(String nameData) {
			this.nameData = nameData;
		}

		public String getNameColum() {
			return nameColum;
		}

		public void setNameColum(String nameColum) {
			this.nameColum = nameColum;
		}

		public boolean isName() {
			return isName;
		}

		public void setName(boolean isName) {
			this.isName = isName;
		}

		public boolean isNumberEvents() {
			return isNumberEvents;
		}

		public void setNumberEvents(boolean isNumberEvents) {
			this.isNumberEvents = isNumberEvents;
		}

		public boolean isSojournTime() {
			return isSojournTime;
		}

		public void setSojournTime(boolean isSojournTime) {
			this.isSojournTime = isSojournTime;
		}
		
	}
	
	private class EventRow {

		private String nameColumn;
		private boolean isActivityName;
		private boolean isLc;
		private boolean isResource;
		private boolean isTimestamp;
		private boolean isRelativeTimestamp;
		private boolean isNrInstances;
		private boolean isData;
		private String nameActivity;
		private String lc;
		private String resource;
		private String nameData;
		
		public EventRow (String nameColumn, boolean isActivityName, boolean isLc, boolean isResource, boolean isTimestamp, 
				boolean isNrInstances, boolean isData, boolean isRelativeTimestamp, String nameActivity, String lc, String resource, String nameData) {
			this.nameColumn = nameColumn;
			this.isActivityName = isActivityName;
			this.isLc = isLc;
			this.isResource = isResource;
			this.isTimestamp = isTimestamp;
			this.isNrInstances = isNrInstances;
			this.isData = isData;
			this.isRelativeTimestamp = isRelativeTimestamp;
			this.nameActivity = nameActivity;
			this.lc = lc;
			this.resource = resource;
			this.nameData = nameData;
		}

		public boolean isRelativeTimestamp() {
			return isRelativeTimestamp;
		}

		public void setRelativeTimestamp(boolean isRelativeTimestamp) {
			this.isRelativeTimestamp = isRelativeTimestamp;
		}

		public String getLc() {
			return lc;
		}

		public void setLc(String lc) {
			this.lc = lc;
		}

		public String getResource() {
			return resource;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}

		public String getNameActivity() {
			return this.nameActivity;
		}
		
		public void setNameActivity(String nameActivity) {
			this.nameActivity = nameActivity;
		}

		public String getNameColumn() {
			return nameColumn;
		}

		public void setNameColumn(String nameColumn) {
			this.nameColumn = nameColumn;
		}

		public boolean isActivityName() {
			return isActivityName;
		}

		public void setActivityName(boolean isActivityName) {
			this.isActivityName = isActivityName;
		}

		public boolean isLc() {
			return isLc;
		}

		public void setLc(boolean isLc) {
			this.isLc = isLc;
		}

		public boolean isResource() {
			return isResource;
		}

		public void setResource(boolean isResource) {
			this.isResource = isResource;
		}

		public boolean isTimestamp() {
			return isTimestamp;
		}

		public void setTimestamp(boolean isTimestamp) {
			this.isTimestamp = isTimestamp;
		}

		public boolean isNrInstances() {
			return isNrInstances;
		}

		public void setNrInstances(boolean isNrInstances) {
			this.isNrInstances = isNrInstances;
		}
		
		public boolean isData() {
			return isData;
		}

		public void setData(boolean isData) {
			this.isData = isData;
		}
		
		public String getNameData() {
			return nameData;
		}

		public void setNameData(String nameData) {
			this.nameData = nameData;
		}
		
	}

}
