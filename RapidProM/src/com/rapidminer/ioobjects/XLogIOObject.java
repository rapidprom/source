package com.rapidminer.ioobjects;

import com.rapidminer.ioobjectrenderers.XLogIOObjectVisualizationType;
import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.framework.plugin.PluginContext;
import org.deckfour.xes.model.XLog;

public class XLogIOObject extends ResultObjectAdapter implements ProMIOObject {

	private static final long serialVersionUID = 1L;

	private XLogIOObjectVisualizationType vt = XLogIOObjectVisualizationType.EXAMPLE_SET;
	private PluginContext pc = null;
	private XLog xLog = null;

	public XLogIOObject() {

	}

	public XLogIOObject(XLog xLog) {
		this.xLog = xLog;
	}

	public void setPluginContext(PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext() {
		return this.pc;
	}

	public void setXLog(XLog xLog) {
		this.xLog = xLog;
	}

	public XLog getXLog() {
		return xLog;
	}

	public String toResultString() {
		String extractName = xLog.toString();
		return "XLogIOObject:" + extractName;
	}

	public XLog getData() {
		return xLog;
	}

	public XLog getPromLog() {
		return xLog;
	}

	public void setPromLog(XLog log) {
		this.xLog = log;
	}

	public void setVisualizationType(XLogIOObjectVisualizationType vt) {
		this.vt = vt;
	}

	public XLogIOObjectVisualizationType getVisualizationType() {
		return this.vt;
	}

	@Override
	public void clear() {
		this.pc = null;
		xLog.clear();
		this.xLog = null;
		this.vt = null;
	}

}
