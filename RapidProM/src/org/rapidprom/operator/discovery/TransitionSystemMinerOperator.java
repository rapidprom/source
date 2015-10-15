package org.rapidprom.operator.discovery;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.transitionsystem.miner.TSMinerPlugin;
import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.TransitionSystemIOObject;
import org.rapidprom.operator.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class TransitionSystemMinerOperator extends AbstractRapidProMDiscoveryOperator {

	private OutputPort output = getOutputPorts().createPort("model (ProM TransitionSystem)");

	public TransitionSystemMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(output, TransitionSystemIOObject.class));
}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: transition system miner");
		long time = System.currentTimeMillis();
		
		PluginContext pluginContext = ProMPluginContextManager.instance().getFutureResultAwareContext(TSMinerPlugin.class);
		Object[] result = TSMinerPlugin.main(pluginContext, getXLog());

		//TO-DO: for now we use default parameters, we should use the same parameters used in prom.
		TransitionSystemIOObject ts = new TransitionSystemIOObject((TSMinerTransitionSystem) result[0]);
		ts.setPluginContext(pluginContext);
		output.deliver(ts);
		
		logger.log(Level.INFO, "End: transition system miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
