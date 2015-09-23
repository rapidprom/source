package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

public class OutputFirstProMTaskIOObject extends ResultObjectAdapter implements ProMIOObject {
	
	/**
	 * automatically generated
	 */
	private static final long serialVersionUID = -2321155267735999570L;
	private Object obj = null;
	
	public OutputFirstProMTaskIOObject(Object obj) {
		this.obj = obj;
	}

	public Object getObject() {
		return this.obj;
	}
	
	
	@Override
	public String toResultString() {
		return "OutputFirstProMTask:" + this.obj.toString();
	}

	@Override
	public void clear() {
		this.obj = null;		
	}

}
