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
//import org.processmining.contexts.cli.CLIPluginContext;
//import org.processmining.framework.plugin.PluginContext;
//
//import com.rapidminer.ioobjects.ProMContextIOObject;
//
//import org.deckfour.xes.model.XLog;
//import org.processmining.plugins.guidetreeminer.types.GTMFeature;
//import org.processmining.plugins.socialnetwork.miner.gui.PanelWorkingTogether;
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
//public class SocialNetworkWTminerTask extends Operator {
//
//	private List<Parameter> parametersSocialNetworkWTminer = null;
//
//	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
//	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
//	private OutputPort outputSocialNetwork = getOutputPorts().createPort("model (Social Network)");
//
//	public SocialNetworkWTminerTask(OperatorDescription description) {
//		super(description);
//		getTransformer().addRule( new GenerateNewMDRule(outputSocialNetwork, SocialNetworkIOObject.class));
//}
//
//	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Social Network (WT) miner", LogService.NOTE);
//		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
//		PluginContext pluginContext = context.getPluginContext();
//		List<Object> pars = new ArrayList<Object>();
//		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
//		pars.add(XLogdata.getData());
//
//		PanelWorkingTogether panelWorkingTogether = getConfiguration(this.parametersSocialNetworkWTminer);
//		pars.add(panelWorkingTogether);
//		CallProm cp = new CallProm();
//		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Social Network (WT) miner", pars);
//		SocialNetworkIOObject socialNetworkIOObject = new SocialNetworkIOObject((SocialNetwork) runPlugin[0]);
//		socialNetworkIOObject.setPluginContext(pluginContext);
//		outputSocialNetwork.deliver(socialNetworkIOObject);
//		logService.log("end do work Social Network (WT) miner", LogService.NOTE);
//	}
//
//	public List<ParameterType> getParameterTypes() {
//		loadRequiredClasses();
//		this.parametersSocialNetworkWTminer = new ArrayList<Parameter>();
//		List<ParameterType> parameterTypes = super.getParameterTypes();
//		
//		Object[] par1categories = new Object[] {"Simultaneous appearance ratio", "Consider distance without causality (beta=0.5)"};
//		ParameterCategory parameter1 = new ParameterCategory(par1categories, "Simultaneous appearance ratio", String.class, "Settings", "Settings");
//		ParameterTypeCategory parameterType1 = new ParameterTypeCategory(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getOptionsParameter(), parameter1.getIndexValue(parameter1.getDefaultValueParameter()));
//		parameterTypes.add(parameterType1);
//		parametersSocialNetworkWTminer.add(parameter1);
//
//		return parameterTypes;
//	}
//
//	private PanelWorkingTogether getConfiguration(List<Parameter> parametersSocialNetworkWTminer) {
//		PanelWorkingTogether panelWorkingTogether = new PanelWorkingTogether();
//		try {
//			Parameter parameter1 = parametersSocialNetworkWTminer.get(0);
//			int par1int = getParameterAsInt(parameter1.getNameParameter());
//			String valPar1 = (String) parameter1.getValueParameter(par1int);
//			if (valPar1.equals("Simultaneous appearance ratio")) {
//				panelWorkingTogether.setSimultaneousAppearance(true);
//				panelWorkingTogether.setDistanceWithoutCausality(false);
//			}
//			else {
//				panelWorkingTogether.setSimultaneousAppearance(false);
//				panelWorkingTogether.setDistanceWithoutCausality(true);
//			}
//
//		} catch (UndefinedParameterError e) {
//			e.printStackTrace();
//		}
//	return panelWorkingTogether;
//	}
//
//	private void loadRequiredClasses () {
//		Utilities.loadRequiredClasses();
//	}
//}
