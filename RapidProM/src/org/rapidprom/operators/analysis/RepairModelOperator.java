package org.rapidprom.operators.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.tools.rmi.ObjectNotFoundException;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.modelrepair.Uma_RepairModel_Plugin;
import org.processmining.plugins.modelrepair.Uma_RepairModel_Plugin.RepairConfiguration;
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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;

public class RepairModelOperator extends Operator {

	private static final String PARAMETER_1 = "Detect loops",
			PARAMETER_2 = "Detect sub-processes",
			PARAMETER_3 = "Remove infrequent nodes",
			PARAMETER_4 = "Global cost alignment",
			PARAMETER_5 = "Align alignments",
			PARAMETER_6 = "Cost of loop model move",
			PARAMETER_7 = "Remove / Keep if more than",
			PARAMETER_8 = "Global cost max iterations";

	private InputPort inputXLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private InputPort inputPetrinet = getInputPorts().createPort(
			"model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts().createPort(
			"model (ProM Petri Net)");

	public RepairModelOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: repair model using event log");
		long time = System.currentTimeMillis();

		Uma_RepairModel_Plugin repairer = new Uma_RepairModel_Plugin();
		
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(Uma_RepairModel_Plugin.class);

		XLogIOObject xLog = inputXLog.getData(XLogIOObject.class);

		PetriNetIOObject petriNet = inputPetrinet
				.getData(PetriNetIOObject.class);

		List<Place> endPlaces = getEndPlaces(petriNet.getArtifact());
		Marking finalMarking = new Marking();
		for (Place place : endPlaces) {
			finalMarking.add(place);
		}

		

		Object[] result = null;
		try {
			result = repairer.repairModel(pluginContext, xLog.getArtifact(),
					petriNet.getArtifact(), petriNet.getInitialMarking(),
					finalMarking, getConfiguration());
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}

		PetriNetIOObject output = new PetriNetIOObject((Petrinet) result[0],
				pluginContext);
		output.setInitialMarking((Marking) result[1]);

		outputPetrinet.deliver(output);

		logger.log(
				Level.INFO,
				"End: repair model using event log ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeBoolean parameter1 = new ParameterTypeBoolean(PARAMETER_1,
				PARAMETER_1, true);
		parameterTypes.add(parameter1);

		ParameterTypeInt parameter6 = new ParameterTypeInt(PARAMETER_6,
				PARAMETER_6, 0, Integer.MAX_VALUE, 1);
		parameterTypes.add(parameter6);

		ParameterTypeBoolean parameter2 = new ParameterTypeBoolean(PARAMETER_2,
				PARAMETER_2, true);
		parameterTypes.add(parameter2);

		ParameterTypeBoolean parameter3 = new ParameterTypeBoolean(PARAMETER_3,
				PARAMETER_3, true);
		parameterTypes.add(parameter3);

		ParameterTypeInt parameter7 = new ParameterTypeInt(PARAMETER_7,
				PARAMETER_7, 0, Integer.MAX_VALUE, 0);
		parameterTypes.add(parameter7);

		ParameterTypeBoolean parameter4 = new ParameterTypeBoolean(PARAMETER_4,
				PARAMETER_4, true);
		parameterTypes.add(parameter4);

		ParameterTypeInt parameter8 = new ParameterTypeInt(PARAMETER_8,
				PARAMETER_8, 0, Integer.MAX_VALUE, 1);
		parameterTypes.add(parameter8);

		ParameterTypeBoolean parameter5 = new ParameterTypeBoolean(PARAMETER_5,
				PARAMETER_5, true);
		parameterTypes.add(parameter5);

		return parameterTypes;
	}

	private RepairConfiguration getConfiguration() {
		RepairConfiguration repairConfiguration = new RepairConfiguration();
		try {
			repairConfiguration.detectLoops = getParameterAsBoolean(PARAMETER_1);
			repairConfiguration.loopModelMoveCosts = getParameterAsInt(PARAMETER_6);
			repairConfiguration.detectSubProcesses = getParameterAsBoolean(PARAMETER_2);
			repairConfiguration.removeInfrequentNodes = getParameterAsBoolean(PARAMETER_3);
			repairConfiguration.remove_keepIfMoreThan = getParameterAsInt(PARAMETER_7);
			repairConfiguration.globalCostAlignment = getParameterAsBoolean(PARAMETER_4);
			repairConfiguration.globalCost_maxIterations = getParameterAsInt(PARAMETER_8);
			repairConfiguration.alignAlignments = getParameterAsBoolean(PARAMETER_5);

		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
		return repairConfiguration;
	}

	@SuppressWarnings("rawtypes")
	private List<Place> getEndPlaces(Petrinet net) {
		List<Place> places = new ArrayList<Place>();
		Iterator<Place> placesIt = net.getPlaces().iterator();
		while (placesIt.hasNext()) {
			Place nextPlace = placesIt.next();
			Collection outEdges = net.getOutEdges(nextPlace);
			if (outEdges.isEmpty()) {
				places.add(nextPlace);
			}
		}
		return places;
	}
}
