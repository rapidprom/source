package org.rapidprom.operators.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.tools.rmi.ObjectNotFoundException;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.pompom.PomPomView;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.ioobjects.PomPomViewIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMDiscoveryOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class ShowPomPomViewOperator extends AbstractRapidProMDiscoveryOperator {

	private InputPort inputPetrinet = getInputPorts().createPort(
			"model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputPomPomView = getOutputPorts().createPort(
			"model (ProM PomPomView)");

	public ShowPomPomViewOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputPomPomView,
						PomPomViewIOObject.class));
	}

	public void doWork() throws OperatorException {

		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: create pompom view");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(PomPomView.class);
		PomPomViewIOObject result = null;
		try {
			result = new PomPomViewIOObject(
					new PomPomView(pluginContext, inputPetrinet.getData(
							PetriNetIOObject.class).getArtifact(),
							inputPetrinet.getData(PetriNetIOObject.class)
									.getInitialMarking(), getXLog(),
							getXEventClassifier()), pluginContext);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}

		outputPomPomView.deliver(result);

		logger.log(Level.INFO,
				"End: create pompom view ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
