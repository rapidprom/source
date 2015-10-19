package org.rapidprom.operator.discovery;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.mining.alphaminer.AlphaMiner;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.operator.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class AlphaMinerOperator extends AbstractRapidProMDiscoveryOperator {

	
	private OutputPort output = getOutputPorts()
			.createPort("model (ProM Petri Net)");

	/**
	 * The default constructor needed in exactly this signature
	 */
	public AlphaMinerOperator(OperatorDescription description) {
		super(description);

		/** Adding a rule for the output */
		getTransformer()
				.addRule(new GenerateNewMDRule(output, PetriNetIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: alpha miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(AlphaMiner.class);
		AlphaMiner miner = new AlphaMiner();

		try {
			Object[] result = miner.doMining(pluginContext, getXLog());

			PetriNetIOObject petriNetIOObject = new PetriNetIOObject(
					(Petrinet) result[0],pluginContext);
			output.deliver(petriNetIOObject);
		} catch (Exception e) {
			throw new OperatorException(e.getMessage());
		}

		logger.log(Level.INFO, "End: alpha miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

}
