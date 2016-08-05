package org.rapidprom.operators.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.modelrepair.parameters.RepairConfiguration;
import org.processmining.modelrepair.plugins.Uma_RepairModel_Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

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

import javassist.tools.rmi.ObjectNotFoundException;

public class RepairModelOperator extends AbstractRapidProMDiscoveryOperator {

	private static final String PARAMETER_1_KEY = "Detect loops",
			PARAMETER_1_DESCR = "If set to 'true', the plugin will apply a few heuristics "
					+ "to detect whether the event log contains cyclic behavior (repetitions) "
					+ "of a certain number of steps where the process model contains no cycle "
					+ "with all these steps. If such a cycle is found, the plugin tests whether "
					+ "introducing a single \"loop back\" transitions increases fitness of the log "
					+ "to the model. If yes, the transition is added, if not, the model remains "
					+ "unchanged. This parameter is optional and reduces the number of "
					+ "sub-processes added due to the parameter \"Detect sub-processes\"",
			PARAMETER_2_KEY = "Detect sub-processes",
			PARAMETER_2_DESCR = "If set to 'true', the plugin will extend the process model in "
					+ "two ways. (1) If the log requires certain process steps to be skipped "
					+ "(by a model move in the alignment), the plugin adds a 'skip' transition "
					+ "for this step that allows to proceed in the process without taking the "
					+ "process step. (2) If the log requires additional process steps that are "
					+ "currently not in the model (due to log moves in the alignment), the "
					+ "plugin idenfities the exact locations where consecutive sequences of "
					+ "additional steps should be added and inserts subprocesses that fit the "
					+ "missing behavior. This parameter is mandatory to obtain a model that "
					+ "perfectly fits the given log ",
			PARAMETER_3_KEY = "Remove infrequent nodes",
			PARAMETER_3_DESCR = "If set to 'true', the plugin identifies process steps which "
					+ "are never or rarely executed according to the log and removes any "
					+ "step that is infrequent without breaking the flow in the model. "
					+ "Use \"Cost of loop model move\" to set the threshold for when a noce is "
					+ "considered infrequent. This parameter is optional and should be used to"
					+ " obtain a simpler model",
			PARAMETER_4_KEY = "Global cost alignment",
			PARAMETER_4_DESCR = "If set to 'true', the plugin analyzes the deviations between "
					+ "model and log on a global level to identify the smallest set of process "
					+ "steps that are missing or should be skipped. This parameter is optional. "
					+ "It causes higher runtime cost in the deviation analysis as several "
					+ "alignments are computed, but it results in simpler models with a higher "
					+ "similarity to the original model. In both cases, the resulting model "
					+ "will perfectly fit the log (if \"Detect sub-processes\" is set to 'true')",
			PARAMETER_5_KEY = "Align alignments",
			PARAMETER_5_DESCR = "Use in conjunction with subprocess detection parameter "
					+ "(\"Detect sub-processes\"). "
					+ "If set to 'true', the identified sequences of steps that have to be "
					+ "added to the model as sub-processes are analyzed for similarities. "
					+ "Subsequences of similar events are grouped together which leads to "
					+ "smaller subprocesses that are inserted at more specific locations in "
					+ "the process. This parameter is optional and may lead to simpler models "
					+ "with a higher similarity to the original model. In both cases, the "
					+ "resulting model will perfectly fit the log (if \"Detect sub-processes\" "
					+ "is set to 'true').",
			PARAMETER_6_KEY = "Cost of loop model move",
			PARAMETER_6_DESCR = "A technical parameter used during loop detection (\"Detect loops\"). "
					+ "When set to '0' (default value), loop detection will ignore that some "
					+ "iterations of a loop may require to skip certain process steps within the "
					+ "loop. If set to a value >= 1, loop detection will balance between the "
					+ "'skip transitions' that have to be added if the loop is added and the "
					+ "sub-process that has to be added if the loop is not added. Generally, "
					+ "the parameter should be set to '0' to ease loop detection and preserve "
					+ "similarity to the original model. However, if the possible loop has a "
					+ "complex inner structure, the analysis for loops may incur very high running"
					+ " times. In this case, set a value >= 1 to ensure faster completion. ",
			PARAMETER_7_KEY = "Remove / Keep if more than",
			PARAMETER_7_DESCR = "The threshold value for when a node is considered 'infrequent' "
					+ "in the removal of infrequent nodes (\"Remove infrequent nodes\"). The "
					+ "threshold is specified "
					+ "as the absolute number of occurrences of a process step in the log. Set to '0' "
					+ "(default) to remove only process steps which never occur in the log (this "
					+ "ensures a fitting model); set to > 0 to also remove parts of the model used "
					+ "only infrequently (gives a simpler model that does not show all behaviors "
					+ "of the log).",
			PARAMETER_8_KEY = "Global cost max iterations",
			PARAMETER_8_DESCR = "Parameter used by computation of a global cost alignment "
					+ "(\"Global cost alignment\"). It specifies the number of analysis iterations "
					+ "done to identify the smallest number of process steps in the model that "
					+ "require a repair. Usually, the smallest number is found after one global "
					+ "analysis (default value '1'). ";

	private InputPort inputPetrinet = getInputPorts()
			.createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputPetrinet = getOutputPorts()
			.createPort("model (ProM Petri Net)");

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

		XLogIOObject xLog = new XLogIOObject(getXLog(), pluginContext);

		PetriNetIOObject petriNet = inputPetrinet
				.getData(PetriNetIOObject.class);

		Object[] result = null;
		try {
			if (!petriNet.hasFinalMarking())
				petriNet.setFinalMarking(
						getFinalMarking(petriNet.getArtifact()));
			result = repairer.repairModel_buildT2Econnection(pluginContext,
					xLog.getArtifact(), petriNet.getArtifact(),
					petriNet.getInitialMarking(), petriNet.getFinalMarking(),
					getConfiguration(), getXEventClassifier());
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}

		PetriNetIOObject output = new PetriNetIOObject((Petrinet) result[0],
				(Marking) result[1], null, pluginContext);

		outputPetrinet.deliver(output);

		logger.log(Level.INFO, "End: repair model using event log ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeBoolean parameter1 = new ParameterTypeBoolean(
				PARAMETER_1_KEY, PARAMETER_1_DESCR, true);
		parameterTypes.add(parameter1);

		ParameterTypeBoolean parameter2 = new ParameterTypeBoolean(
				PARAMETER_2_KEY, PARAMETER_2_DESCR, true);
		parameterTypes.add(parameter2);

		ParameterTypeBoolean parameter5 = new ParameterTypeBoolean(
				PARAMETER_5_KEY, PARAMETER_5_DESCR, true);
		parameterTypes.add(parameter5);

		ParameterTypeBoolean parameter3 = new ParameterTypeBoolean(
				PARAMETER_3_KEY, PARAMETER_3_DESCR, true);
		parameterTypes.add(parameter3);

		ParameterTypeInt parameter7 = new ParameterTypeInt(PARAMETER_7_KEY,
				PARAMETER_7_DESCR, 0, Integer.MAX_VALUE, 0);
		parameterTypes.add(parameter7);

		ParameterTypeBoolean parameter4 = new ParameterTypeBoolean(
				PARAMETER_4_KEY, PARAMETER_4_DESCR, true);
		parameterTypes.add(parameter4);

		ParameterTypeInt parameter6 = new ParameterTypeInt(PARAMETER_6_KEY,
				PARAMETER_6_DESCR, 0, Integer.MAX_VALUE, 0);
		parameterTypes.add(parameter6);

		ParameterTypeInt parameter8 = new ParameterTypeInt(PARAMETER_8_KEY,
				PARAMETER_8_DESCR, 0, Integer.MAX_VALUE, 1);
		parameterTypes.add(parameter8);

		return parameterTypes;
	}

	private RepairConfiguration getConfiguration() {
		RepairConfiguration repairConfiguration = new RepairConfiguration();
		try {
			repairConfiguration.detectLoops = getParameterAsBoolean(
					PARAMETER_1_KEY);
			repairConfiguration.loopModelMoveCosts = getParameterAsInt(
					PARAMETER_6_KEY);
			repairConfiguration.detectSubProcesses = getParameterAsBoolean(
					PARAMETER_2_KEY);
			repairConfiguration.removeInfrequentNodes = getParameterAsBoolean(
					PARAMETER_3_KEY);
			repairConfiguration.remove_keepIfMoreThan = getParameterAsInt(
					PARAMETER_7_KEY);
			repairConfiguration.globalCostAlignment = getParameterAsBoolean(
					PARAMETER_4_KEY);
			repairConfiguration.globalCost_maxIterations = getParameterAsInt(
					PARAMETER_8_KEY);
			repairConfiguration.alignAlignments = getParameterAsBoolean(
					PARAMETER_5_KEY);

		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
		return repairConfiguration;
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
		Marking m = new Marking();
		m.addAll(places);
		return m;
	}
}
