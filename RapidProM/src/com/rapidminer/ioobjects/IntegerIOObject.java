package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import java.lang.Integer;

public class IntegerIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private Integer integer = null;

	public IntegerIOObject (Integer integer) {
		this.integer = integer;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setInteger(Integer integer) {
		this.integer = integer;
	}

	public Integer getInteger() {
		return integer;
	}

	public String toResultString() {
		String extractName = integer.toString();
		return "IntegerIOObject:" + extractName;
	}

	public Integer getData() {
		return integer;
	}

}
