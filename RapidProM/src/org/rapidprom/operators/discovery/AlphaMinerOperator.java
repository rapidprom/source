package org.rapidprom.operators.discovery;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.alphaminer.plugins.AlphaMinerPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.LogService;

public class AlphaMinerOperator extends AbstractRapidProMDiscoveryOperator {

	private static final String PARAMETER_1 = "Variant";

	private static final String CLASSIC = "AlphaMiner classic",
			PLUS = "AlphaMiner +", PLUSPLUS = "AlphaMiner ++",
			SHARP = "AlphaMiner #";

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
				.getFutureResultAwareContext(AlphaMinerPlugin.class);

		Object[] result = null;
		switch (getParameterAsString(PARAMETER_1)) {
		case CLASSIC:
			result = AlphaMinerPlugin.applyAlphaClassic(pluginContext,
					getXLog(), getXEventClassifier());
			break;
		case PLUS:
			result = AlphaMinerPlugin.applyAlphaPlus(pluginContext, getXLog(),
					getXEventClassifier());
			break;
		case PLUSPLUS:
			result = AlphaMinerPlugin.applyAlphaPlusPlus(pluginContext,
					getXLog(), getXEventClassifier());
			break;
		case SHARP:
			result = AlphaMinerPlugin.applyAlphaSharp(pluginContext, getXLog(),
					getXEventClassifier());
			break;
		}

		PetriNetIOObject petriNetIOObject = new PetriNetIOObject(
				(Petrinet) result[0], pluginContext);
		petriNetIOObject.setInitialMarking((Marking) result[1]);

		output.deliver(petriNetIOObject);

		logger.log(Level.INFO, "End: alpha miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeCategory parameter1 = new ParameterTypeCategory(
				PARAMETER_1, PARAMETER_1,
				new String[] { CLASSIC, PLUS, PLUSPLUS, SHARP }, 1);
		parameterTypes.add(parameter1);

		return parameterTypes;
	}

}
