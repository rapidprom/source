package org.rapidprom.operators.generation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.ptandloggenerator.plugins.GenerateNoisyLog;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;

import javassist.tools.rmi.ObjectNotFoundException;

public class GenerateNoisyLogOperator extends Operator {

	public static final String PARAMETER_2_KEY = "Maximum Swap Probability",
			PARAMETER_2_DESCR = "The probability of any event to be swapped with the subsequent.",
			PARAMETER_3_KEY = "Maximum Add Probability",
			PARAMETER_3_DESCR = "The probability of replacing any event for any activity A with "
					+ "two subsequent events for activity A (i.e. an event for A is added",
			PARAMETER_4_KEY = "Maximum Remove Probability",
			PARAMETER_4_DESCR = "The probability of removing any event",
			PARAMETER_5_KEY = "Number of Buckets",
			PARAMETER_5_DESCR = "The number N of buckets in which the event log is split. "
					+ "For the first bucket, the probabilities above are multiplied by 1/N; "
					+ "for the second bucket, they are multiplied by 2/N; etc. until the last"
					+ " bucket where they are multiplied by N/N (i.e. they are not reduced)";

	private InputPort inputLog = getInputPorts().createPort("event log",
			XLogIOObject.class);

	private InputPort inputNet = getInputPorts().createPort("petri net",
			PetriNetIOObject.class);

	private OutputPort output = getOutputPorts().createPort("event log");

	public GenerateNoisyLogOperator(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(output, XLogIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: generating noisy event log");
		long time = System.currentTimeMillis();

		XLogIOObject log = inputLog.getData(XLogIOObject.class);
		PetriNetIOObject petriNet = inputNet.getData(PetriNetIOObject.class);
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		GenerateNoisyLog generator = new GenerateNoisyLog();
		XLog aux = null;
		try {
			if (!petriNet.hasFinalMarking())
				petriNet.setFinalMarking(
						getFinalMarking(petriNet.getArtifact()));
			aux = generator.addNoise(pluginContext, log.getArtifact(),
					petriNet.getArtifact(), petriNet.getInitialMarking(),
					petriNet.getFinalMarking(),
					getParameterAsDouble(PARAMETER_2_KEY),
					getParameterAsDouble(PARAMETER_3_KEY),
					getParameterAsDouble(PARAMETER_4_KEY),
					getParameterAsInt(PARAMETER_5_KEY));
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}

		XLogIOObject result = new XLogIOObject(aux, pluginContext);
		result.setVisualizationType(log.getVisualizationType());

		output.deliver(result);
		logger.log(Level.INFO, "End: generating noisy event log ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeDouble parameter2 = new ParameterTypeDouble(
				PARAMETER_2_KEY, PARAMETER_2_DESCR, 0, 1, 0.3);
		parameterTypes.add(parameter2);

		ParameterTypeDouble parameter3 = new ParameterTypeDouble(
				PARAMETER_3_KEY, PARAMETER_3_DESCR, 0, 1, 0.3);
		parameterTypes.add(parameter3);

		ParameterTypeDouble parameter4 = new ParameterTypeDouble(
				PARAMETER_4_KEY, PARAMETER_4_DESCR, 0, 1, 0.3);
		parameterTypes.add(parameter4);

		ParameterTypeInt parameter5 = new ParameterTypeInt(PARAMETER_5_KEY,
				PARAMETER_5_DESCR, 1, 10, 3);
		parameterTypes.add(parameter5);

		return parameterTypes;
	}

	@SuppressWarnings("rawtypes")
	public static Marking getFinalMarking(Petrinet pn) {
		List<Place> places = new ArrayList<Place>();
		Iterator<Place> placesIt = pn.getPlaces().iterator();
		while (placesIt.hasNext()) {
			Place nextPlace = placesIt.next();
			Collection inEdges = pn.getOutEdges(nextPlace);
			if (inEdges.isEmpty()) {
				places.add(nextPlace);
			}
		}
		Marking finalMarking = new Marking();
		for (Place place : places) {
			finalMarking.add(place);
		}
		return finalMarking;
	}

}
