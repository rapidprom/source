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
//import com.rapidminer.parameters.*;
//
//import org.processmining.framework.plugin.PluginContext;
//
//import com.rapidminer.ioobjects.ProMContextIOObject;
//
//import org.processmining.plugins.socialnetwork.miner.gui.PanelReassignment;
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
//public class SocialNetworkRAminerTask extends Operator {
//
//	private List<Parameter> parametersSocialNetworkRAminer = null;
//
//	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
//	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
//	private OutputPort outputSocialNetwork = getOutputPorts().createPort("model (Social Network)");
//
//	public SocialNetworkRAminerTask(OperatorDescription description) {
//		super(description);
//		getTransformer().addRule( new GenerateNewMDRule(outputSocialNetwork, SocialNetworkIOObject.class));
//}
//
//	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Social Network (RA) miner", LogService.NOTE);
//		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
//		PluginContext pluginContext = context.getPluginContext();
//		List<Object> pars = new ArrayList<Object>();
//		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
//		pars.add(XLogdata.getData());
//
//		PanelReassignment panelReassignment = getConfiguration(this.parametersSocialNetworkRAminer);
//		pars.add(panelReassignment);
//		CallProm cp = new CallProm();
//		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Social Network (RA) miner", pars);
//		SocialNetworkIOObject socialNetworkIOObject = new SocialNetworkIOObject((SocialNetwork) runPlugin[0]);
//		socialNetworkIOObject.setPluginContext(pluginContext);
//		outputSocialNetwork.deliver(socialNetworkIOObject);
//		logService.log("end do work Social Network (RA) miner", LogService.NOTE);
//	}
//
//	public List<ParameterType> getParameterTypes() {
//		loadRequiredClasses();
//		this.parametersSocialNetworkRAminer = new ArrayList<Parameter>();
//		List<ParameterType> parameterTypes = super.getParameterTypes();
//
//		ParameterBoolean parameter1 = new ParameterBoolean(false, Boolean.class, "Ignore multiple transfers within one instance","Ignore multiple transfers within one instance");
//		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getDefaultValueParameter());
//		parameterTypes.add(parameterType1);
//		parametersSocialNetworkRAminer.add(parameter1);
//
//		return parameterTypes;
//	}
//
//	private PanelReassignment getConfiguration(List<Parameter> parametersSocialNetworkRAminer) {
//		PanelReassignment panelReassignment = new PanelReassignment();
//		Parameter parameter1 = parametersSocialNetworkRAminer.get(0);
//		boolean valPar1 = getParameterAsBoolean(parameter1.getNameParameter());
//		panelReassignment.setIgnoreMultipleTransfers(valPar1);
//		return panelReassignment;
//	}
//
//	private void loadRequiredClasses () {
//		Utilities.loadRequiredClasses();
//	}
//}
