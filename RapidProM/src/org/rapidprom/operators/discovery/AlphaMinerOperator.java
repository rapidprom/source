package org.rapidprom.operators.discovery;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.mining.alphaminer.AlphaMiner;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.MarkingIOObject;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class AlphaMinerOperator extends Operator {

	/** defining the ports */
	private InputPort inputLog = getInputPorts()
			.createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort output = getOutputPorts()
			.createPort("model (ProM Petri Net)");
	private OutputPort outputMarking = getOutputPorts()
			.createPort("marking (ProM marking)");

	/**
	 * The default constructor needed in exactly this signature
	 */
	public AlphaMinerOperator(OperatorDescription description) {
		super(description);

		/** Adding a rule for the output */
		getTransformer()
				.addRule(new GenerateNewMDRule(output, PetriNetIOObject.class));
		getTransformer().addRule(
				new GenerateNewMDRule(outputMarking, MarkingIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: alpha miner");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(AlphaMiner.class);
		XLogIOObject log = inputLog.getData(XLogIOObject.class);
		AlphaMiner miner = new AlphaMiner();

		try {
			Object[] result = miner.doMining(pluginContext, log.getXLog());

			PetriNetIOObject petriNetIOObject = new PetriNetIOObject(
					(Petrinet) result[0]);
			petriNetIOObject.setPluginContext(pluginContext);

			MarkingIOObject marking = new MarkingIOObject((Marking) result[1]);
			marking.setPluginContext(pluginContext);

			output.deliver(petriNetIOObject);
			outputMarking.deliver(marking);
		} catch (Exception e) {
			throw new OperatorException(e.getMessage());
		}

		logger.log(Level.INFO, "End: alpha miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

}
