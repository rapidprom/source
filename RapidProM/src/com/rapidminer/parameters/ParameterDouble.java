package com.rapidminer.parameters;

import java.util.ArrayList;
import java.util.List;

public class ParameterDouble implements Parameter {
	
	private Class<?> clazz = null;
	private double defaultValue = 0.0;
	private String nameParameter = "";
	private String descriptionParameter = "";
	
	private double min = Double.MIN_VALUE;
	private double max = Double.MAX_VALUE;
	private double step = 1;

	public ParameterDouble(double def, double min, double max, double step, Class clazz, String name, String description) {
		this.defaultValue = def;
		this.min = min;
		this.max = max;
		this.step = step;
		this.clazz = clazz;
		this.nameParameter = name;
		this.descriptionParameter = description;
	}
	
	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getStep() {
		return step;
	}

	public String getNameParameter() {
		return this.nameParameter;
	}

	public String getDescriptionParameter() {
		return this.descriptionParameter;
	}

	public String[] getOptionsParameter() {
		List<String> options = new ArrayList<String>();
		double counter = min;
		while (counter <= max) {
			options.add(Double.toString(counter));
		}
		String[] array = new String[options.size()];
		for(int i = 0; i < options.size(); i++) {
			array[i] = options.get(i);
		}
		return array;
	}

	public Object getValueParameter(Integer index) {
		return index;
	}

	public int getIndexValue(Object obj) {
		if (obj instanceof Double) {
			return -1;
		}
		return -1;
	}

	public Object getDefaultValueParameter() {
		return this.defaultValue;
	}

	public Class getClazz() {
		return this.clazz;
	}

}
