package com.rapidminer.parameters;

public class ParameterString implements Parameter {
	
	private Class<?> clazz = null;
	private String defaultValue = "";
	private String nameParameter = "";
	private String descriptionParameter = "";
	
	public ParameterString (String def, Class<?> clazz, String name, String description) {
		this.defaultValue = def;
		this.clazz = clazz;
		this.nameParameter = name;
		this.descriptionParameter = description;
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
		return null;
	}

	@Override
	public Object getValueParameter(Integer index) {
		return null;
	}

	@Override
	public int getIndexValue(Object obj) {
		return 0;
	}

	@Override
	public String getDefaultValueParameter() {
		return this.defaultValue;
	}

	@Override
	public Class<?> getClazz() {
		return this.clazz;
	}

}
