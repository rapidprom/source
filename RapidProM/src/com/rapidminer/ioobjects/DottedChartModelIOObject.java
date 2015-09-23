//package com.rapidminer.ioobjects;
//
//import com.rapidminer.operator.ResultObjectAdapter;
//
//import org.processmining.contexts.cli.CLIPluginContext;
//import org.processmining.framework.plugin.PluginContext;
//import org.processmining.plugins.dottedchartanalysis.model.DottedChartModel;
//
//public class DottedChartModelIOObject extends ResultObjectAdapter implements ProMIOObject {
//
//	private static final long serialVersionUID = 1L;
//
//	private PluginContext pc = null;
//	private DottedChartModel dottedChartModel = null;
//
//	public DottedChartModelIOObject (DottedChartModel dottedChartModel) {
//		this.dottedChartModel = dottedChartModel;
//	}
//
//	public void setPluginContext (PluginContext pluginContext) {
//		this.pc = pluginContext;
//	}
//
//	public PluginContext getPluginContext () {
//		return this.pc;
//	}
//
//	public void setDottedChartModel(DottedChartModel dottedChartModel) {
//		this.dottedChartModel = dottedChartModel;
//	}
//
//	public DottedChartModel getDottedChartModel() {
//		return dottedChartModel;
//	}
//
//	public String toResultString() {
//		String extractName = dottedChartModel.toString();
//		return "DottedChartModelIOObject:" + extractName;
//	}
//
//	public DottedChartModel getData() {
//		return dottedChartModel;
//	}
//
//	@Override
//	public void clear() {
//		this.pc = null;
//		this.dottedChartModel = null;		
//	}
//
//}
