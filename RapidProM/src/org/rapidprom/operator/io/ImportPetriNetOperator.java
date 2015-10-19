package org.rapidprom.operator.io;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.pnml.importing.PnmlImportNet;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.operator.abstr.AbstractRapidProMImportOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.LogService;

public class ImportPetriNetOperator extends AbstractRapidProMImportOperator {

	private OutputPort outputPetriNet = getOutputPorts()
			.createPort("model (ProM Petri Net)");

	public ImportPetriNetOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputPetriNet, PetriNetIOObject.class));
	}

	private static String[] SUPPORTED_PETRI_NET_FORMATS = new String[] {
			"pnml" };

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: importing petri net");
		long time = System.currentTimeMillis();

		if (checkFileParameterMetaData(PARAMETER_LABEL_FILENAME)) {

			PluginContext context = ProMPluginContextManager.instance()
					.getFutureResultAwareContext(PnmlImportNet.class);
			PnmlImportNet importer = new PnmlImportNet();
			Object[] result = null;
			try {
				result = (Object[]) importer.importFile(context,
						getParameterAsFile(PARAMETER_LABEL_FILENAME));
			} catch (Exception e) {
				e.printStackTrace();
			}
			PetriNetIOObject petriNetIOObject = new PetriNetIOObject(
					(Petrinet) result[0], ProMPluginContextManager.instance().getContext());

			outputPetriNet.deliver(petriNetIOObject);

			logger.log(Level.INFO, "End: importing petri net ("
					+ (System.currentTimeMillis() - time) / 1000 + " sec)");
		}
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeFile pnmlFileParameter = new ParameterTypeFile(
				PARAMETER_LABEL_FILENAME, "File to open", false,
				SUPPORTED_PETRI_NET_FORMATS);
		parameterTypes.add(pnmlFileParameter);

		return parameterTypes;
	}
}