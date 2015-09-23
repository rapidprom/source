package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.contexts.cli.CLIPluginContext;
import org.deckfour.xes.info.XLogInfo;

public class XLogInfoIOObject extends ResultObjectAdapter implements ProMIOObject {

	private static final long serialVersionUID = 1L;

	private CLIPluginContext pc = null;
	private XLogInfo xLogInfo = null;

	public XLogInfoIOObject (XLogInfo xLogInfo) {
		this.xLogInfo = xLogInfo;
	}

	public void setPluginContext (CLIPluginContext pc) {
		this.pc = pc;
	}

	public CLIPluginContext getPluginContext () {
		return this.pc;
	}

	public void setXLogInfo(XLogInfo xLogInfo) {
		this.xLogInfo = xLogInfo;
	}

	public XLogInfo getXLogInfo() {
		return xLogInfo;
	}

	public String toResultString() {
		String extractName = xLogInfo.toString();
		return "XLogInfoIOObject:" + extractName;
	}

	public XLogInfo getData() {
		return xLogInfo;
	}

	@Override
	public void clear() {
		this.pc = null;
		this.xLogInfo = null;
	}

}
