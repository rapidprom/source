//package com.rapidminer.ioobjects;
//
//import com.rapidminer.operator.ResultObjectAdapter;
//
//import org.processmining.contexts.cli.CLIPluginContext;
//import org.processmining.plugins.heuristicsnet.array.visualization.HeuristicsNetArrayObject;
//
//public class HeuristicsNetArrayObjectIOObject extends ResultObjectAdapter implements ProMIOObject {
//
//	private static final long serialVersionUID = 1L;
//
//	private CLIPluginContext pc = null;
//	private HeuristicsNetArrayObject heuristicsNetArrayObject = null;
//
//	public HeuristicsNetArrayObjectIOObject (HeuristicsNetArrayObject heuristicsNetArrayObject) {
//		this.heuristicsNetArrayObject = heuristicsNetArrayObject;
//	}
//
//	public void setPluginContext (CLIPluginContext pc) {
//		this.pc = pc;
//	}
//
//	public CLIPluginContext getPluginContext () {
//		return this.pc;
//	}
//
//	public void setHeuristicsNetArrayObject(HeuristicsNetArrayObject heuristicsNetArrayObject) {
//		this.heuristicsNetArrayObject = heuristicsNetArrayObject;
//	}
//
//	public HeuristicsNetArrayObject getHeuristicsNetArrayObject() {
//		return heuristicsNetArrayObject;
//	}
//
//	public String toResultString() {
//		String extractName = heuristicsNetArrayObject.toString();
//		return "HeuristicsNetArrayObjectIOObject:" + extractName;
//	}
//
//	public HeuristicsNetArrayObject getData() {
//		return heuristicsNetArrayObject;
//	}
//
//	@Override
//	public void clear() {
//		this.pc = null;
//		this.heuristicsNetArrayObject = null;		
//	}
//
//}
