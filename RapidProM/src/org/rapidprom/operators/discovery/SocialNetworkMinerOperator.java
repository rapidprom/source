package org.rapidprom.operators.discovery;

import java.util.List;

import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.ioobjects.SocialNetworkIOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.EqualTypeCondition;

public class SocialNetworkMinerOperator extends AbstractRapidProMDiscoveryOperator {

	private static final String VARIATION = "Analysis variation",
			PARAMETER_A_1 = "Consider multiple transfers within one instance",
			PARAMETER_A_2 = "Consider only direct succession",
			PARAMETER_A_3 = "Beta",
			PARAMETER_A_4 = "Depth of calculation",
			
			PARAMETER_B_1 = "Ignore multiple transfers within one instance",
			
			PARAMETER_C_1 = "Euclidian distance",
			PARAMETER_C_2 = "Correlation coefficient",
			PARAMETER_C_3 = "Similarity coefficient",
			PARAMETER_C_4 = "Hamming distance",
			
			PARAMETER_D_1 = "Simultaneous appearence ratio",
			PARAMETER_D_2 = "Consider distance without causality (beta = 0.5)";
					
	private static final String
		HANDOVER_OF_WORK = "Handover of work",
		REASSIGNMENT = "Reassignment",
		SIMILAR_TASK = "Similar task",
		SUBCONTRACTING = "Subcontracting",
		WORKING_TOGETHER = "Working together";
		
	
	private OutputPort outputSocialNetwork = getOutputPorts().createPort("model (ProM Social Network)");

	public SocialNetworkMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputSocialNetwork, SocialNetworkIOObject.class));
}

	public void doWork() throws OperatorException {
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
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		String[] options = new String[] { HANDOVER_OF_WORK, REASSIGNMENT,
				SIMILAR_TASK, SUBCONTRACTING, WORKING_TOGETHER };

		ParameterTypeCategory variation = new ParameterTypeCategory(VARIATION,
				VARIATION, options, 0);
		parameterTypes.add(variation);

		ParameterTypeBoolean parameter1 = new ParameterTypeBoolean(
				PARAMETER_A_1, PARAMETER_A_1, true);
		parameter1.setOptional(true);
		parameter1.registerDependencyCondition(new EqualTypeCondition(this,
				VARIATION, options, false, 0,3));
		parameterTypes.add(parameter1);

		ParameterTypeBoolean parameter2 = new ParameterTypeBoolean(
				PARAMETER_A_2, PARAMETER_A_2, true);
		parameter2.setOptional(true);
		parameter2.registerDependencyCondition(new EqualTypeCondition(this,
				VARIATION, options, false, 0,3));
		parameterTypes.add(parameter2);

		ParameterTypeDouble parameter3 = new ParameterTypeDouble(PARAMETER_A_3,
				PARAMETER_A_3, 0, 1, 0.5, false);
		parameter3.setOptional(true);
		parameter3.registerDependencyCondition(new BooleanParameterCondition(
				this, PARAMETER_A_2, false, false));
		parameterTypes.add(parameter3);

		ParameterTypeInt parameter4 = new ParameterTypeInt(PARAMETER_A_4,
				PARAMETER_A_4, 0, 100, 5, false);
		parameter4.setOptional(true);
		parameter4.registerDependencyCondition(new BooleanParameterCondition(
				this, PARAMETER_A_2, false, false));
		parameterTypes.add(parameter4);

		ParameterTypeBoolean parameter5 = new ParameterTypeBoolean(
				PARAMETER_B_1, PARAMETER_B_1, false);
		parameter5.setOptional(true);
		parameter5.registerDependencyCondition(new EqualTypeCondition(this,
				VARIATION, options, false, 1));
		parameterTypes.add(parameter5);

		ParameterTypeCategory parameter6 = new ParameterTypeCategory(
				SIMILAR_TASK, SIMILAR_TASK, new String[] { PARAMETER_C_1,
						PARAMETER_C_2, PARAMETER_C_3, PARAMETER_C_4 }, 0);
		parameter6.setOptional(true);
		parameter6.registerDependencyCondition(new EqualTypeCondition(this,
				VARIATION, options, false, 2));
		parameterTypes.add(parameter6);
		
		ParameterTypeCategory parameter7 = new ParameterTypeCategory(
				SIMILAR_TASK, SIMILAR_TASK, new String[] { PARAMETER_D_1,
						PARAMETER_D_2}, 0);
		parameter7.setOptional(true);
		parameter7.registerDependencyCondition(new EqualTypeCondition(this,
				VARIATION, options, false, 4));
		parameterTypes.add(parameter7);
		
		return parameterTypes;
	}

//	private PanelHandoverOfWork getConfiguration() {
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
}
