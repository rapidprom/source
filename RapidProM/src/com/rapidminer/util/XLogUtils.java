package com.rapidminer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class XLogUtils {
	
	public enum AttributeTypes {BOOLEAN, DISCRETE, CONTINUOUS, STRING, DATE}
	
	public static TableModelXLog convertLogToStringTable (XLog log, boolean timeIsSeconds) throws Exception {
		final List<String> columnNames = new ArrayList<String>();
		final List<List<String>> values = new ArrayList<List<String>>();
		// go through log for values
		ColumnNamesLog columnNames2 = XLogUtils.getColumnNames(log);
		columnNames.addAll(columnNames2.getAttribsTrace());
		columnNames.addAll(columnNames2.getAttribsEvents());
		// generate mapping from string to column number
		Map<String,Integer> mapping = new HashMap<String,Integer>();
		for (int i=0; i<columnNames.size(); i++) {
			mapping.put(columnNames.get(i), i);
		}
		AttributeTypes[] columnTypesArray = new AttributeTypes[columnNames.size()];
		// go through log for values
		for (XTrace t: log) {
			String[] valuesTrace = new String[columnNames.size()];
			Iterator<Entry<String, XAttribute>> iterator = t.getAttributes().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, XAttribute> next = iterator.next();
				String key = next.getKey();
				XAttribute value = next.getValue();
				String stringFromAttribute = getStringFromAttribute(value);
				// put at right position
				Integer integer = mapping.get("T:" + key);
				if (integer!=null) {
					valuesTrace[integer] = stringFromAttribute;
					// try to find type
					AttributeTypes type = getType(value);
					if (columnTypesArray[integer] == null) {
						columnTypesArray[integer] = type;
					}
					else if (!columnTypesArray[integer].equals(type)) {
						// something wrong, basically should not happen
						throw new Exception("type is different!");
					}
				}
			}
			// now for event attributes
			for (XEvent e : t) {
				Iterator<Entry<String, XAttribute>> iterator2 = e.getAttributes().entrySet().iterator();
				String[] valuesEvent = new String[columnNames.size()];
				// copy values from trace
				for (int i=0; i<columnNames.size(); i++) {
					valuesEvent[i] = valuesTrace[i];
				}
				while (iterator2.hasNext()) {
					Entry<String, XAttribute> next = iterator2.next();
					String key = next.getKey();
					XAttribute value = next.getValue();
					String stringFromAttribute = getStringFromAttribute(value);
					// put at right position
					Integer integer = mapping.get("E:" + key);
					if (integer!=null) {
						// check if is time
						AttributeTypes type = getType(value);
						if (timeIsSeconds && type.equals(AttributeTypes.DATE)) {
							XAttributeTimestamp ts = (XAttributeTimestamp) value;
							valuesEvent[integer] = Long.toString(ts.getValue().getTime());
						}
						else {
							valuesEvent[integer] = stringFromAttribute;
						}
						// try to find type
						if (columnTypesArray[integer] == null) {
							columnTypesArray[integer] = type;
						}
						else if (!columnTypesArray[integer].equals(type)) {
							// something wrong, basically should not happen
							throw new Exception("type is different!");
						}
					}
				}
				// check whether any value is null
				for (int i=0; i<valuesEvent.length; i++) {
					if (valuesEvent[i] == null) {
						valuesEvent[i] = "";
					}
					
				}
				// put in list
				List<String> asList = Arrays.asList(valuesEvent);
				values.add(asList);				
			}
		}
		List<AttributeTypes> columnTypes = Arrays.asList(columnTypesArray);
		XLogUtils utilsInst = new XLogUtils();
		return utilsInst.new TableModelXLog(columnNames, values, columnTypes);
	}
	
	public class TableModelXLog extends AbstractTableModel {
		
		/**
		 * generated
		 */
		private static final long serialVersionUID = -6973564987055884713L;
		private List<String> columnNames = new ArrayList<String>();
		private List<List<String>> values = new ArrayList<List<String>>();
		private List<AttributeTypes> columnTypes = new ArrayList<AttributeTypes>();
		
		public TableModelXLog(List<String> columnNames, List<List<String>> values, List<AttributeTypes> columnTypes) {
			this.columnNames = columnNames;
			this.values = values;
			this.columnTypes = columnTypes;
		}
		
		@Override
		public int getColumnCount() {
			return columnNames.size();
		}

		@Override
		public String getColumnName(int column) {
			return columnNames.get(column);
		}
		
		public int getNameForColumn(String name) {
			for (int i=0; i<columnNames.size(); i++) {
				if (columnNames.get(i).equals(name)) {
					return i;
				}
			}
			return -1;
		}
		
		@Override
		public int getRowCount() {
			return values.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return values.get(rowIndex).get(columnIndex);
		}
		
		public AttributeTypes getColumnType(int columnIndex) {
			return columnTypes.get(columnIndex);
		}
		
	}
	
	public static ColumnNamesLog getColumnNames (XLog log) {
		Set<String> attribsTrace = new TreeSet<String>();
		Set<String> attribsEvents = new TreeSet<String>();
		for (XTrace t : log) {
			Iterator<Entry<String, XAttribute>> iterator = t.getAttributes().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, XAttribute> next = iterator.next();
				attribsTrace.add("T:" + next.getKey());
			}
			for (XEvent e : t) {
				Iterator<Entry<String, XAttribute>> iterator2 = e.getAttributes().entrySet().iterator();
				while (iterator2.hasNext()) {
					Entry<String, XAttribute> next = iterator2.next();
					attribsEvents.add("E:" + next.getKey());
				}
			}
		}
		return new XLogUtils().new ColumnNamesLog(attribsTrace, attribsEvents);
	}
	
	public static String getStringFromAttribute (XAttribute value) {
		String valueString = "";
		if (value instanceof XAttributeLiteral) {
			XAttributeLiteral attribLit = (XAttributeLiteral) value;
			valueString = attribLit.getValue();
		}
		else if (value instanceof XAttributeBoolean) {
			XAttributeBoolean attribBool = (XAttributeBoolean) value;
			valueString = Boolean.toString(attribBool.getValue());
		}
		else if (value instanceof XAttributeContinuous) {
			XAttributeContinuous attribContin = (XAttributeContinuous) value;
			valueString = Double.toString(attribContin.getValue());
		}
		else if (value instanceof XAttributeDiscrete) {
			XAttributeDiscrete attribDisc = (XAttributeDiscrete) value;
			valueString = Long.toString(attribDisc.getValue());
		}
		else if (value instanceof XAttributeTimestamp) {
			XAttributeTimestamp attribTimestamp = (XAttributeTimestamp) value;
			valueString = attribTimestamp.getValue().toString();
		}
		return valueString;
	}
	
	public static AttributeTypes getType(XAttribute attribute){
		if(attribute instanceof XAttributeBoolean)
			return AttributeTypes.BOOLEAN;
		else if(attribute instanceof XAttributeDiscrete)
			return AttributeTypes.DISCRETE;
		else if(attribute instanceof XAttributeContinuous)
			return AttributeTypes.CONTINUOUS;
		else if(attribute instanceof XAttributeTimestamp)
			return AttributeTypes.DATE;
		else 
			return AttributeTypes.STRING;
	}
	
	public class ColumnNamesLog {
		
		private Set<String> attribsTrace =  null;
		private Set<String> attribsEvents = null;
		
		ColumnNamesLog (Set<String> attribsTrace, Set<String> attribsEvents) {
			this.attribsTrace = attribsTrace;
			this.attribsEvents = attribsEvents;
		}

		public Set<String> getAttribsTrace() {
			return attribsTrace;
		}

		public void setAttribsTrace(Set<String> attribsTrace) {
			this.attribsTrace = attribsTrace;
		}

		public Set<String> getAttribsEvents() {
			return attribsEvents;
		}

		public void setAttribsEvents(Set<String> attribsEvents) {
			this.attribsEvents = attribsEvents;
		}
		
	}
}
