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
//import org.processmining.plugins.guidetreeminer.types.GTMFeature;
//import org.processmining.plugins.socialnetwork.miner.gui.PanelSimilarTask;
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
//public class SocialNetworkSTminerTask extends Operator {
//
//	private List<Parameter> parametersSocialNetworkSTminer = null;
//
//	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
//	private InputPort inputXLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
//	private OutputPort outputSocialNetwork = getOutputPorts().createPort("model (Social Network)");
//
//	public SocialNetworkSTminerTask(OperatorDescription description) {
//		super(description);
//		getTransformer().addRule( new GenerateNewMDRule(outputSocialNetwork, SocialNetworkIOObject.class));
//}
//
//	public void doWork() throws OperatorException {
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Social Network (ST) miner", LogService.NOTE);
//		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
//		PluginContext pluginContext = context.getPluginContext();
//		List<Object> pars = new ArrayList<Object>();
//		XLogIOObject XLogdata = inputXLog.getData(XLogIOObject.class);
//		pars.add(XLogdata.getData());
//
//		PanelSimilarTask panelSimilarTask = getConfiguration(this.parametersSocialNetworkSTminer);
//		pars.add(panelSimilarTask);
//		CallProm cp = new CallProm();
//		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Social Network (ST) miner", pars);
//		SocialNetworkIOObject socialNetworkIOObject = new SocialNetworkIOObject((SocialNetwork) runPlugin[0]);
//		socialNetworkIOObject.setPluginContext(pluginContext);
//		outputSocialNetwork.deliver(socialNetworkIOObject);
//		logService.log("end do work Social Network (ST) miner", LogService.NOTE);
//	}
//
//	public List<ParameterType> getParameterTypes() {
//		loadRequiredClasses();
//		this.parametersSocialNetworkSTminer = new ArrayList<Parameter>();
//		List<ParameterType> parameterTypes = super.getParameterTypes();
//		
//		Object[] par1categories = new Object[] {"Euclidean Distance", "Correlation Coefficient", "Similarity Coefficient", "Hamming Distance"};
//		ParameterCategory parameter1 = new ParameterCategory(par1categories, "Correlation Coefficient",String.class, "Settings", "Settings");
//		ParameterTypeCategory parameterType1 = new ParameterTypeCategory(parameter1.getNameParameter(), parameter1.getDescriptionParameter(), parameter1.getOptionsParameter(), parameter1.getIndexValue(parameter1.getDefaultValueParameter()));
//		parameterTypes.add(parameterType1);
//		parametersSocialNetworkSTminer.add(parameter1);
//		return parameterTypes;
//	}
//
//	private PanelSimilarTask getConfiguration(List<Parameter> parametersSocialNetworkSTminer) {
//		PanelSimilarTask panelSimilarTask = new PanelSimilarTask();
//		try {
//			Parameter parameter1 = parametersSocialNetworkSTminer.get(0);
//			int par1int = getParameterAsInt(parameter1.getNameParameter());
//			String valPar1 = (String) parameter1.getValueParameter(par1int);
//			if (valPar1.equals("Euclidean Distance")) {
//				panelSimilarTask.setEuclideanDistance(true);
//				panelSimilarTask.setCorrelationCoefficient(false);
//				panelSimilarTask.setSimilarityCoefficient(false);
//				panelSimilarTask.setHammingDistance(false);
//			}
//			else if (valPar1.equals("Correlation Coefficient")) {
//				panelSimilarTask.setEuclideanDistance(false);
//				panelSimilarTask.setCorrelationCoefficient(true);
//				panelSimilarTask.setSimilarityCoefficient(false);
//				panelSimilarTask.setHammingDistance(false);
//			}
//			else if (valPar1.equals("Similarity Coefficient")) {
//				panelSimilarTask.setEuclideanDistance(false);
//				panelSimilarTask.setCorrelationCoefficient(false);
//				panelSimilarTask.setSimilarityCoefficient(true);
//				panelSimilarTask.setHammingDistance(false);
//			}
//			else {
//				panelSimilarTask.setEuclideanDistance(false);
//				panelSimilarTask.setCorrelationCoefficient(false);
//				panelSimilarTask.setSimilarityCoefficient(false);
//				panelSimilarTask.setHammingDistance(true);
//			}
//		} catch (UndefinedParameterError e) {
//			e.printStackTrace();
//		}
//	return panelSimilarTask;
//	}
//
//	private void loadRequiredClasses () {
//		Utilities.loadRequiredClasses();
//	}
//}
