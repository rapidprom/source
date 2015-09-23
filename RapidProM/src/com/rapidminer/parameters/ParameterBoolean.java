package com.rapidminer.parameters;

public class ParameterBoolean implements Parameter {
	
	private Class clazz = null;
	private Boolean defaultValue = null;
	private String nameParameter = "";
	private String descriptionParameter = "";
	
	public ParameterBoolean(Boolean def, Class clazz, String name, String description) {
		if (def instanceof Boolean) {
			Boolean b = (Boolean) def;
			this.defaultValue = b;
		}
		this.clazz = clazz;
		this.nameParameter= name;
		this.descriptionParameter = description;
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
		String[] options = new String[2];
		options[0] = Boolean.toString(true);
		options[1] = Boolean.toString(false);
		return options;
	}
	
	public Boolean getValueParameter (Integer index) {
		if (index == 0) {
			return true;
		}
		else if (index == 1) {
			return false;
		}
		return false;
	}
	
	public int getIndexValue (Object obj) {
		if (obj instanceof Boolean) {
			Boolean b = (Boolean) obj;
			if (b) {
				return 0;
			}
			else {
				return 1;
			}
		}
		return -1;
	}
	
	public Boolean getDefaultValueParameter () {
		return this.defaultValue;
	}

}
