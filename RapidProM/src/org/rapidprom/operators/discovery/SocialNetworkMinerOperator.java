package org.rapidprom.operators.discovery;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.socialnetwork.SocialNetwork;
import org.processmining.plugins.socialnetwork.miner.SNHoWMiner;
import org.processmining.plugins.socialnetwork.miner.SNRAMiner;
import org.processmining.plugins.socialnetwork.miner.SNSCMiner;
import org.processmining.plugins.socialnetwork.miner.SNSTMiner;
import org.processmining.plugins.socialnetwork.miner.SNWTMiner;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.SocialNetworkIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.LogService;

public class SocialNetworkMinerOperator extends
		AbstractRapidProMDiscoveryOperator {

	private static final String VARIATION = "Analysis variation";

	private static final String HANDOVER_OF_WORK = "Handover of work",
			REASSIGNMENT = "Reassignment", SIMILAR_TASK = "Similar task",
			SUBCONTRACTING = "Subcontracting",
			WORKING_TOGETHER = "Working together";

	private OutputPort outputSocialNetwork = getOutputPorts().createPort(
			"model (ProM Social Network)");

	public SocialNetworkMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputSocialNetwork,
						SocialNetworkIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: social network miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = null;

		SocialNetwork result = null;
		switch (getParameterAsInt(VARIATION)) {
			case 0:
				pluginContext = ProMPluginContextManager.instance()
						.getFutureResultAwareContext(SNHoWMiner.class);
				SNHoWMiner miner0 = new SNHoWMiner();
				result = miner0.socialnetwork(pluginContext, getXLog());
				break;
			case 1:
				pluginContext = ProMPluginContextManager.instance()
						.getFutureResultAwareContext(SNRAMiner.class);
				SNRAMiner  miner1 = new SNRAMiner();
				result = miner1.socialnetwork(pluginContext, getXLog());
				break;
			case 2:
				pluginContext = ProMPluginContextManager.instance()
						.getFutureResultAwareContext(SNSTMiner.class);
				SNSTMiner miner2 = new SNSTMiner();
				result = miner2.socialnetwork(pluginContext, getXLog());
				break;
			case 3:
				pluginContext = ProMPluginContextManager.instance()
						.getFutureResultAwareContext(SNSCMiner.class);
				SNSCMiner miner3 = new SNSCMiner();
				result = miner3.socialnetwork(pluginContext, getXLog());
				break;
			case 4:
				pluginContext = ProMPluginContextManager.instance()
						.getFutureResultAwareContext(SNWTMiner.class);
				SNWTMiner miner4 = new SNWTMiner();
				result = miner4.socialnetwork(pluginContext, getXLog());
				break;			
		}
		
		outputSocialNetwork.deliver(new SocialNetworkIOObject(result,pluginContext));

		logger.log(Level.INFO,
				"End: alpha miner (" + (System.currentTimeMillis() - time)
						/ 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		String[] options = new String[] { HANDOVER_OF_WORK, REASSIGNMENT,
				SIMILAR_TASK, SUBCONTRACTING, WORKING_TOGETHER };

		ParameterTypeCategory variation = new ParameterTypeCategory(VARIATION,
				VARIATION, options, 0);
		parameterTypes.add(variation);

		return parameterTypes;
	}
}
