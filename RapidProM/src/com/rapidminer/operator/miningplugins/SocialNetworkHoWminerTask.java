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
//import com.rapidminer.util.Utilities;
//import com.rapidminer.parameter.*;
//import com.rapidminer.parameter.conditions.BooleanParameterCondition;
//import com.rapidminer.parameters.*;
//
//import org.processmining.framework.plugin.PluginContext;
//
//import com.rapidminer.ioobjects.ProMContextIOObject;
//
//import org.processmining.plugins.socialnetwork.miner.gui.PanelHandoverOfWork;
//
//import com.rapidminer.ioobjects.XLogIOObject;
//import com.rapidminer.ioobjects.SocialNetworkIOObject;
//
//import org.processmining.models.graphbased.directed.socialnetwork.SocialNetwork;
//
//import com.rapidminer.callprom.ClassLoaderUtils;
//import com.rapidminer.configuration.GlobalProMParameters;
//
//import java.io.File;
//
//public class SocialNetworkHoWminerTask extends Operator {
//
//	private List<Parameter> parametersSocialNetworkHoWminer = null;
//
//	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
//	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
//	private OutputPort outputSocialNetwork = getOutputPorts().createPort("model (Social Network)");
//
//	public SocialNetworkHoWminerTask(OperatorDescription description) {
//		super(description);
//		getTransformer().addRule( new GenerateNewMDRule(outputSocialNetwork, SocialNetworkIOObject.class));
//}
//
//	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Social Network (HoW) miner", LogService.NOTE);
//		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
//		PluginContext pluginContext = context.getPluginContext();
//		List<Object> pars = new ArrayList<Object>();
//		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
//		pars.add(XLogdata.getData());
//
//		PanelHandoverOfWork panelHandoverOfWork = getConfiguration(this.parametersSocialNetworkHoWminer);
//		pars.add(panelHandoverOfWork);
//		CallProm cp = new CallProm();
//		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Social Network (HoW) miner", pars);
//		SocialNetworkIOObject socialNetworkIOObject = new SocialNetworkIOObject((SocialNetwork) runPlugin[0]);
//		socialNetworkIOObject.setPluginContext(pluginContext);
//		outputSocialNetwork.deliver(socialNetworkIOObject);
//		logService.log("end do work Social Network (HoW) miner", LogService.NOTE);
//	}
//
//	public List<ParameterType> getParameterTypes() {
//		loadRequiredClasses();
//		this.parametersSocialNetworkHoWminer = new ArrayList<Parameter>();
//		List<ParameterType> parameterTypes = super.getParameterTypes();
//		
//		ParameterBoolean parameter1 = new ParameterBoolean(true, Boolean.class,"Consider multiple transfers within one instance", "Consider multiple transfers within one instance");
//		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getDefaultValueParameter());
//		parameterTypes.add(parameterType1);
//		parametersSocialNetworkHoWminer.add(parameter1);
//		
//		ParameterBoolean parameter2 = new ParameterBoolean(true, Boolean.class,"Consider only direct succession", "Consider only direct succession");
//		ParameterTypeBoolean parameterType2 = new ParameterTypeBoolean(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getDefaultValueParameter());
//		parameterTypes.add(parameterType2);
//		parametersSocialNetworkHoWminer.add(parameter2);
//
//		ParameterString parameter3 = new ParameterString("0.5", String.class,"Beta", "Beta");
//		ParameterTypeString parameterType3 = new ParameterTypeString(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getDefaultValueParameter());
//		parameterTypes.add(parameterType3);
//		parametersSocialNetworkHoWminer.add(parameter3);
//
//		ParameterString parameter4 = new ParameterString("5", String.class,"Depth of calculation", "Depth of calculation");
//		ParameterTypeString parameterType4 = new ParameterTypeString(parameter4.getNameParameter(), parameter4.getDescriptionParameter(), parameter4.getDefaultValueParameter());
//		parameterTypes.add(parameterType4);
//		parametersSocialNetworkHoWminer.add(parameter4);
//		
//		// register a dependency
//		parameterType3.registerDependencyCondition(new BooleanParameterCondition(this, "Consider only direct succession", false, false));
//		parameterType4.registerDependencyCondition(new BooleanParameterCondition(this, "Consider only direct succession", false, false));
//
//		return parameterTypes;
//	}
//
//	private PanelHandoverOfWork getConfiguration(List<Parameter> parametersSocialNetworkHoWminer) {
//		PanelHandoverOfWork panelHandoverOfWork = new PanelHandoverOfWork();
//		try {
//			Parameter parameter1 = parametersSocialNetworkHoWminer.get(0);
//			boolean valPar1 = getParameterAsBoolean(parameter1.getNameParameter());
//			panelHandoverOfWork.setConsiderMultipleTransfers(valPar1);
//			
//			Parameter parameter2 = parametersSocialNetworkHoWminer.get(1);
//			boolean valPar2 = getParameterAsBoolean(parameter2.getNameParameter());
//			panelHandoverOfWork.setConsiderDirectSuccesion(valPar2);
//			
//			Parameter parameter4 = parametersSocialNetworkHoWminer.get(3);
//			String par4str = getParameterAsString(parameter4.getNameParameter());
//			panelHandoverOfWork.setDepth(par4str);
//	
//			Parameter parameter3 = parametersSocialNetworkHoWminer.get(2);
//			String par3str = getParameterAsString(parameter3.getNameParameter());
//			panelHandoverOfWork.setBeta(par3str);
//		} catch (UndefinedParameterError e) {
//			e.printStackTrace();
//		}
//	return panelHandoverOfWork;
//	}
//
//	private void loadRequiredClasses () {
//		Utilities.loadRequiredClasses();
//	}
//}
