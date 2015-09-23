package com.rapidminer.parameters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ParameterCategory implements Parameter {
	
	private Class clazz = null;
	private Object defaultValue = null;
	private Map<Integer,Object> mappingPar = new HashMap<Integer,Object>();
	private String nameParameter = "";
	private String descriptionParameter = "";
	
	public ParameterCategory (Object[] categories, Object def, Class clazz, String name, String description) {
		this.clazz = clazz;
		this.nameParameter = name;
		this.descriptionParameter = description;
		this.defaultValue = def;
		int counter = 0;
		for (Object obj : categories) {
			// Object cast = clazz.cast(obj);
			mappingPar.put(counter, obj);
			counter++;
		}
	}
	
	public Class getClazz() {
		return this.clazz;
	}
	
	public String getNameParameter () {
		return this.nameParameter;
	}
	
	public String getDescriptionParameter () {
		return this.descriptionParameter;
	}
	
	public String[] getOptionsParameter () {
		String [] options = new String[mappingPar.size()];
		Iterator<Entry<Integer, Object>> iterator = mappingPar.entrySet().iterator();
		int counter = 0;
		while (iterator.hasNext()) {
			Entry<Integer, Object> next = iterator.next();
			options[counter] = (clazz.cast(next.getValue())).toString();
			counter++;
		}
		return options;
	}
	
	public Object getValueParameter (Integer index) {
		return this.mappingPar.get(index);
	}
	
	public int getIndexValue (Object obj) {
		Iterator<Entry<Integer, Object>> iterator = mappingPar.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Object> next = iterator.next();
			if (next.getValue().equals(obj)) {
				return next.getKey(); 
			}
		}
		return -1;
	}
	
	public Object getDefaultValueParameter () {
		return this.defaultValue;
	}

}
