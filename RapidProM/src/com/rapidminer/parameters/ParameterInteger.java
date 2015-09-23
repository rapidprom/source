package com.rapidminer.parameters;

import java.util.ArrayList;
import java.util.List;

public class ParameterInteger implements Parameter {
	
	private Class<?> clazz = null;
	private int defaultValue = -1;
	private String nameParameter = "";
	private String descriptionParameter = "";
	
	private int min = Integer.MIN_VALUE;
	private int max = Integer.MAX_VALUE;
	private int step = 1;
	
	public ParameterInteger (int def, int min, int max, int step, Class clazz, String name, String description) {
		assert (min<=max);
		assert (step>0);
		this.defaultValue = def;
		this.min = min;
		this.max = max;
		this.step = step;
		this.clazz = clazz;
		this.nameParameter = name;
		this.descriptionParameter = description;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public int getStep() {
		return step;
	}

	@Override
	public String getNameParameter() {
		return this.nameParameter;
	}

	@Override
	public String getDescriptionParameter() {
		return this.descriptionParameter;
	}

	@Override
	public String[] getOptionsParameter() {
		List<String> options = new ArrayList<String>();
		for (int i=min; i<=max; i=i+step) {
			options.add(Integer.toString(i));
		}
		String[] array = new String[options.size()];
		for(int i = 0; i < options.size(); i++) {
			array[i] = options.get(i);
		}
		return array;
	}

	@Override
	public Object getValueParameter(Integer index) {
		return index;
	}

	@Override
	public int getIndexValue(Object obj) {
		if (obj instanceof Integer) {
			return (Integer) obj;
		}
		return -1;
	}

	@Override
	public Integer getDefaultValueParameter() {
		return this.defaultValue;
	}

	@Override
	public Class getClazz() {
		return this.clazz;
	}

}
