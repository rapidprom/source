package org.rapidprom.operators.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.petrinets.list.factory.PetriNetListFactory;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.PetriNetListIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class PetriNetsToPetriNetListConversionOperator extends Operator {

	private final InputPortExtender extender = new InputPortExtender(
			"model (Petri net)", getInputPorts(), null, 1);

	private final OutputPort output = getOutputPorts()
			.createPort("list (Petri net)");

	public PetriNetsToPetriNetListConversionOperator(
			OperatorDescription description) {
		super(description);
		extender.start();
		getTransformer().addRule(
				new GenerateNewMDRule(output, PetriNetListIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: petri nets to petri net list conversion");
		long time = System.currentTimeMillis();
		List<Petrinet> nets = new ArrayList<Petrinet>();

		for (InputPort port : extender.getManagedPorts()) {
			nets.add(port.getData(PetriNetIOObject.class).getArtifact());
		}

		output.deliver(
				new PetriNetListIOObject(
						PetriNetListFactory.createPetriNetList(
								nets.toArray(new Petrinet[nets.size()])),
				null));

		logger.log(Level.INFO, "End: heuristics net to petri net conversion ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}
}
