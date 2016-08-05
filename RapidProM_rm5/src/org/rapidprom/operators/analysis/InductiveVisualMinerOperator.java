package org.rapidprom.operators.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner.InteractiveMinerLauncher;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.InteractiveMinerLauncherIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class InductiveVisualMinerOperator extends
		AbstractRapidProMDiscoveryOperator {

	private OutputPort outputInteractiveMinerLauncher = getOutputPorts()
			.createPort("model (ProM InteractiveVisualMiner)");

	public InductiveVisualMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputInteractiveMinerLauncher,
						InteractiveMinerLauncherIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: inductive visual miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();
		InductiveVisualMiner wrapper = new InductiveVisualMiner();
		InteractiveMinerLauncher im = wrapper.mineGuiProcessTree(pluginContext,
				getXLog());

		InteractiveMinerLauncherIOObject interactiveMinerLauncherIOObject = new InteractiveMinerLauncherIOObject(
				im, pluginContext);

		outputInteractiveMinerLauncher
				.deliver(interactiveMinerLauncherIOObject);

		logger.log(Level.INFO,
				"End: inductive visual miner ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
