package com.rapidminer.parameters;

public interface Parameter {
	
	public String getNameParameter ();
	
	public String getDescriptionParameter ();
	
	public String[] getOptionsParameter ();
	
	public Object getValueParameter (Integer index);
	
	public int getIndexValue (Object obj);
	
	public Object getDefaultValueParameter ();

	public Class<?> getClazz();

}
