package org.rapidprom.operators.discovery;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.ProcessTreeIOObjectVisualizationType;
import org.rapidprom.ioobjects.ProcessTreeIOObject;
import org.rapidprom.operators.abstr.AbstractInductiveMinerOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class InductiveMinerPTOperator extends AbstractInductiveMinerOperator {

	OutputPort output = getOutputPorts().createPort("model (ProM ProcessTree)");
	
	public InductiveMinerPTOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(output, ProcessTreeIOObject.class));		
	}

	public void doWork() throws OperatorException 
 {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: inductive miner - pt");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();
		MiningParameters param = getConfiguration();

		ProcessTreeIOObject result = new ProcessTreeIOObject(
				IMProcessTree.mineProcessTree(getXLog(), param),pluginContext);
		result.setVisualizationType(ProcessTreeIOObjectVisualizationType.DEFAULT);

		output.deliver(result);
		logger.log(Level.INFO,
				"End: inductive miner - pt ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}
}
