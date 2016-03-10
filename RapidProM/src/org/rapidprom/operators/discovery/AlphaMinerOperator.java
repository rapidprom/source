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

/**
 * This operator implements different variants of the alpha miner algorithm The
 * "classic" variant is defined in http://dx.doi.org/10.1109/TKDE.2004.47 The
 * "+" variant is defined in http://dx.doi.org/10.1007/978-3-540-30188-2_12 The
 * "++" variant is defined in http://dx.doi.org/10.1007/s10618-007-0065-y The
 * "#" variant is defined in http://dx.doi.org/10.1016/j.datak.2010.06.001
 * 
 * @author abolt
 *
 */
public class AlphaMinerOperator extends AbstractRapidProMDiscoveryOperator {

	private static final String PARAMETER_1_KEY = "Variant",
			PARAMETER_1_DESCR = "Defines which version of the AlphaMiner will be used: "
					+ "The \"classic\" variant is defined in http://dx.doi.org/10.1109/TKDE.2004.47 . "
					+ "The \"+\" variant is defined in http://dx.doi.org/10.1007/978-3-540-30188-2_12 . "
					+ "The \"++\" variant is defined in http://dx.doi.org/10.1007/s10618-007-0065-y . "
					+ "The \"#\" variant is defined in http://dx.doi.org/10.1016/j.datak.2010.06.001 .";

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
		switch (getParameterAsString(PARAMETER_1_KEY)) {
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
				(Petrinet) result[0], (Marking) result[1], null, pluginContext);

		output.deliver(petriNetIOObject);

		logger.log(Level.INFO, "End: alpha miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeCategory parameter1 = new ParameterTypeCategory(
				PARAMETER_1_KEY, PARAMETER_1_DESCR,
				new String[] { CLASSIC, PLUS, PLUSPLUS, SHARP }, 1);
		parameterTypes.add(parameter1);

		return parameterTypes;
	}

}
