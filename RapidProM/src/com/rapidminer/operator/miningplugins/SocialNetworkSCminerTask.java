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
//import org.processmining.contexts.cli.CLIPluginContext;
//import org.processmining.framework.plugin.PluginContext;
//
//import com.rapidminer.ioobjects.ProMContextIOObject;
//
//import org.deckfour.xes.model.XLog;
//import org.processmining.plugins.socialnetwork.miner.gui.PanelSubcontracting;
//
//import com.rapidminer.ioobjects.XLogIOObject;
//import com.rapidminer.ioobjectrenderers.XLogIOObjectRenderer;
//import com.rapidminer.ioobjects.SocialNetworkIOObject;
//import com.rapidminer.ioobjectrenderers.SocialNetworkIOObjectRenderer;
//
//import org.processmining.models.graphbased.directed.socialnetwork.SocialNetwork;
//
//import com.rapidminer.callprom.ClassLoaderUtils;
//import com.rapidminer.configuration.GlobalProMParameters;
//
//import java.io.File;
//
//public class SocialNetworkSCminerTask extends Operator {
//
//	private List<Parameter> parametersSocialNetworkSCminer = null;
//
//	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
//	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
//	private OutputPort outputSocialNetwork = getOutputPorts().createPort("model (Social Network)");
//
//	public SocialNetworkSCminerTask(OperatorDescription description) {
//		super(description);
//		getTransformer().addRule( new GenerateNewMDRule(outputSocialNetwork, SocialNetworkIOObject.class));
//}
//
//	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Social Network (SC) miner", LogService.NOTE);
//		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
//		PluginContext pluginContext = context.getPluginContext();
//		List<Object> pars = new ArrayList<Object>();
//		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
//		pars.add(XLogdata.getData());
//
//		PanelSubcontracting panelSubcontracting = getConfiguration(this.parametersSocialNetworkSCminer);
//		pars.add(panelSubcontracting);
//		CallProm cp = new CallProm();
//		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Social Network (SC) miner", pars);
//		SocialNetworkIOObject socialNetworkIOObject = new SocialNetworkIOObject((SocialNetwork) runPlugin[0]);
//		socialNetworkIOObject.setPluginContext(pluginContext);
//		outputSocialNetwork.deliver(socialNetworkIOObject);
//		logService.log("end do work Social Network (SC) miner", LogService.NOTE);
//	}
//
//	public List<ParameterType> getParameterTypes() {
//		loadRequiredClasses();
//		this.parametersSocialNetworkSCminer = new ArrayList<Parameter>();
//		List<ParameterType> parameterTypes = super.getParameterTypes();
//
//		ParameterBoolean parameter1 = new ParameterBoolean(true, Boolean.class,"Consider multiple transfers within one instance", "Consider multiple transfers within one instance");
//		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getDefaultValueParameter());
//		parameterTypes.add(parameterType1);
//		parametersSocialNetworkSCminer.add(parameter1);
//		
//		ParameterBoolean parameter2 = new ParameterBoolean(true, Boolean.class,"Consider only direct succession", "Consider only direct succession");
//		ParameterTypeBoolean parameterType2 = new ParameterTypeBoolean(parameter2.getNameParameter(), parameter2.getDescriptionParameter(), parameter2.getDefaultValueParameter());
//		parameterTypes.add(parameterType2);
//		parametersSocialNetworkSCminer.add(parameter2);
//
//		ParameterString parameter3 = new ParameterString("0.5", String.class,"Beta", "Beta");
//		ParameterTypeString parameterType3 = new ParameterTypeString(parameter3.getNameParameter(), parameter3.getDescriptionParameter(), parameter3.getDefaultValueParameter());
//		parameterTypes.add(parameterType3);
//		parametersSocialNetworkSCminer.add(parameter3);
//
//		ParameterString parameter4 = new ParameterString("5", String.class,"Depth of calculation", "Depth of calculation");
//		ParameterTypeString parameterType4 = new ParameterTypeString(parameter4.getNameParameter(), parameter4.getDescriptionParameter(), parameter4.getDefaultValueParameter());
//		parameterTypes.add(parameterType4);
//		parametersSocialNetworkSCminer.add(parameter4);
//		
//		// register a dependency
//		parameterType3.registerDependencyCondition(new BooleanParameterCondition(this, "Consider only direct succession", false, false));
//		parameterType4.registerDependencyCondition(new BooleanParameterCondition(this, "Consider only direct succession", false, false));
//
//		return parameterTypes;
//	}
//
//	private PanelSubcontracting getConfiguration(List<Parameter> parametersSocialNetworkSCminer) {
//		PanelSubcontracting panelSubcontracting = new PanelSubcontracting();
//		try {
//			Parameter parameter1 = parametersSocialNetworkSCminer.get(0);
//			boolean valPar1 = getParameterAsBoolean(parameter1.getNameParameter());
//			panelSubcontracting.setConsiderMultipeTransfers(valPar1);
//			
//			Parameter parameter2 = parametersSocialNetworkSCminer.get(1);
//			boolean valPar2 = getParameterAsBoolean(parameter2.getNameParameter());
//			panelSubcontracting.setConsiderDirectSuccesion(valPar2);
//			
//			Parameter parameter4 = parametersSocialNetworkSCminer.get(3);
//			String par4str = getParameterAsString(parameter4.getNameParameter());
//			panelSubcontracting.setDepth(par4str);
//	
//			Parameter parameter3 = parametersSocialNetworkSCminer.get(2);
//			String par3str = getParameterAsString(parameter3.getNameParameter());
//			panelSubcontracting.setBeta(par3str);
//		} catch (UndefinedParameterError e) {
//			e.printStackTrace();
//		}
//	return panelSubcontracting;
//	}
//
//	private void loadRequiredClasses () {
//		Utilities.loadRequiredClasses();
//	}
//}
