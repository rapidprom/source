//package com.rapidminer.operator.miningplugins;
//
//import java.util.*;
//
//import com.rapidminer.callprom.CallProm;
//import com.rapidminer.operator.Operator;
//import com.rapidminer.operator.OperatorDescription;
//import com.rapidminer.operator.OperatorException;
//import com.rapidminer.operator.ports.InputPort;
//import com.rapidminer.operator.ports.OutputPort;
//import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
//import com.rapidminer.tools.LogService;
//import com.rapidminer.util.ProMIOObjectList;
//
//import org.processmining.framework.plugin.PluginContext;
//
//import com.rapidminer.ioobjects.ProMContextIOObject;
//import com.rapidminer.ioobjects.XLogIOObject;
//import com.rapidminer.ioobjects.DottedChartModelIOObject;
//
//import org.processmining.plugins.dottedchartanalysis.model.DottedChartModel;
//
//public class DottedChartAnalysisTask extends Operator {
//
//	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
//	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
//	private OutputPort outputDottedChartModel = getOutputPorts().createPort("model (ProM Dotted Chart)");
//
//	public DottedChartAnalysisTask(OperatorDescription description) {
//		super(description);
//		getTransformer().addRule( new GenerateNewMDRule(outputDottedChartModel, DottedChartModelIOObject.class));
//}
//
//	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Dotted Chart Analysis", LogService.NOTE);
//		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
//		PluginContext pluginContext = context.getPluginContext();
//		List<Object> pars = new ArrayList<Object>();
//		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
//		pars.add(XLogdata.getData());
//
//		CallProm cp = new CallProm();
//		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Dotted Chart Analysis", pars);
//		DottedChartModelIOObject dottedChartModelIOObject = new DottedChartModelIOObject((DottedChartModel) runPlugin[0]);
//		dottedChartModelIOObject.setPluginContext(pluginContext);
//		// add to list so that afterwards it can be cleared if needed
//		ProMIOObjectList instance = ProMIOObjectList.getInstance();
//		instance.addToList(dottedChartModelIOObject);
//		outputDottedChartModel.deliver(dottedChartModelIOObject);
//		logService.log("end do work Dotted Chart Analysis", LogService.NOTE);
//	}
//
//}
