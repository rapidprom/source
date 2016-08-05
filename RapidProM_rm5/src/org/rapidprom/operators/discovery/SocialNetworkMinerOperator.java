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

public class SocialNetworkMinerOperator
		extends AbstractRapidProMDiscoveryOperator {

	private static final String HANDOVER_OF_WORK = "Handover of work",
			HANDOVER_OF_WORK_DESCR = "Handover of work metric: Within a case (i.e., process "
					+ "instance) there is a handover of work from individual i to individual "
					+ "j if there are two subsequent activities where the first is completed "
					+ "by i and the second by j. This notion can be refined in various ways. "
					+ "For example, knowledge of the process structure can be used to detect "
					+ "whether there is really a causal dependency between both activities. "
					+ "It is also possible to not only consider direct succession but also "
					+ "indirect succession using a “causality fall factor” beta, i.e., if there "
					+ "are 3 activities in-between an activity completed by i and an activity "
					+ "completed by j, the causality fall factor is beta^3.",

			REASSIGNMENT = "Reassignment",
			REASSIGNMENT_DESCR = "Reassignment metric: It considers the type of event. Thus "
					+ "far we assumed that events correspond to the execution of activities. "
					+ "However, there are also events like reassigning an activity from one "
					+ "individual to another. For example, if i frequently delegates work to "
					+ "j but not vice versa it is likely that i is in a hierarchical relation "
					+ "with j. From a SNA point of view these observations are particularly "
					+ "interesting since they represent explicit power relations.",

			SUBCONTRACTING = "Subcontracting",
			SUBCONTRACTING_DESCR = "Subcontracting metric: The main idea is to count the number "
					+ "of times individual j executed an activity in-between two activities "
					+ "executed by individual i. This may indicate that work was subcontracted "
					+ "from i to j. All kinds of refinements mentioned in Handover of work metric "
					+ "are also possible.",

			WORKING_TOGETHER = "Working together",
			WORKING_TOGETHER_DESCR = "Working together metric: This ignores causal dependencies "
					+ "but simply counts how frequently two individuals are performing activities "
					+ "for the same case. If individuals work together on cases, they will have a "
					+ "stronger relation than individuals rarely working together. There are three "
					+ "kinds of methods to calcuate working together metric. The first one is dividing "
					+ "the number of joint cases by the number of cases in which individual i appeared. "
					+ "It is important to use a relative notation. For example, suppose that individual "
					+ "i participates in three cases, individual j participates in six cases, and they "
					+ "work together three times. In this situation, i always work together with j, but "
					+ "j does not. Thus, the value for i to j has to be larger than the value for j to i. "
					+ "Alternative metrics can be composed by taking the distance between activities "
					+ "into account.";

	private static final String VARIATION = "Analysis variation",
			VARIATION_DESCR = HANDOVER_OF_WORK_DESCR + "\n" + REASSIGNMENT_DESCR
					+ "\n" + SUBCONTRACTING_DESCR + "\n"
					+ WORKING_TOGETHER_DESCR;

	private OutputPort outputSocialNetwork = getOutputPorts()
			.createPort("model (ProM Social Network)");

	public SocialNetworkMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(outputSocialNetwork,
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
			SNRAMiner miner1 = new SNRAMiner();
			result = miner1.socialnetwork(pluginContext, getXLog());
			break;		
		case 2:
			pluginContext = ProMPluginContextManager.instance()
					.getFutureResultAwareContext(SNSCMiner.class);
			SNSCMiner miner3 = new SNSCMiner();
			result = miner3.socialnetwork(pluginContext, getXLog());
			break;
		case 3:
			pluginContext = ProMPluginContextManager.instance()
					.getFutureResultAwareContext(SNWTMiner.class);
			SNWTMiner miner4 = new SNWTMiner();
			result = miner4.socialnetwork(pluginContext, getXLog());
			break;
		}

		outputSocialNetwork
				.deliver(new SocialNetworkIOObject(result, pluginContext));

		logger.log(Level.INFO, "End: social network miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		String[] options = new String[] { HANDOVER_OF_WORK, REASSIGNMENT,
				SUBCONTRACTING, WORKING_TOGETHER };

		ParameterTypeCategory variation = new ParameterTypeCategory(VARIATION,
				VARIATION_DESCR, options, 0);
		parameterTypes.add(variation);

		return parameterTypes;
	}
}
