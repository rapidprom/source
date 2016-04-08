package org.rapidprom.operators.discovery;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.csmminer.CSMMinerResults;
import org.processmining.csmminer.plugins.CSMMinerPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.transitionsystem.miner.TSMinerPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.CSMMinerResultIOObject;
import org.rapidprom.ioobjects.TransitionSystemIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class CSMMinerOperator extends AbstractRapidProMDiscoveryOperator {

	private OutputPort output = getOutputPorts().createPort("model (ProM CSMMinerResults)");
	
	public CSMMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(output, CSMMinerResultIOObject.class));
	}
	
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: CSM Miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance().getFutureResultAwareContext(CSMMinerPlugin.class);
		Object[] result = new CSMMinerPlugin().mineCompositeStateMachine(pluginContext, getXLog());
		
		if (result[0] != null) {
			CSMMinerResultIOObject results = new CSMMinerResultIOObject((CSMMinerResults) result[0], pluginContext);
			output.deliver(results);
		}
		else {
			throw new OperatorException("Input invalid.");
		}
		
		logger.log(Level.INFO, "End: CSM Miner (" + (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
