package org.rapidprom.operators.logmanipulation;

import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.XLogIOObjectVisualizationType;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;

public class AddClassifierOperator extends Operator {

	public static final String PARAMETER_1_KEY = "Classifier",
			PARAMETER_1_DESCR = "Classifier to be added to the event log";

	public static final String NONE = "None (do not add classifier)",
			EN = "Event name", EN_LT = "Event name + Lifecycle transition",
			EN_LT_RE = "Event name + Lifecycle transition + Resource";

	private InputPort inputXLog = getInputPorts()
			.createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputEventLog = getOutputPorts()
			.createPort("event log (ProM Event Log)");

	public AddClassifierOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputEventLog, XLogIOObject.class));
	}

	public void doWork() throws OperatorException {

		XLogIOObject logObject = inputXLog.getData(XLogIOObject.class);

		XLog newLog = (XLog) logObject.getArtifact().clone();
		XLogInfo newinfo = new XLogInfoImpl(newLog, null, null);

		switch (getParameterAsString(PARAMETER_1_KEY)) {
		case NONE:
			break;
		case EN:
			newLog.getClassifiers().add(new XEventNameClassifier());
			
			break;
		case EN_LT:
			newLog.getClassifiers()
					.add(new XEventAndClassifier(new XEventNameClassifier(),
							new XEventLifeTransClassifier()));
			break;
		case EN_LT_RE:
			newLog.getClassifiers()
			.add(new XEventAndClassifier(new XEventNameClassifier(),
					new XEventLifeTransClassifier(), new XEventResourceClassifier()));
			break;
		}

		XLogIOObject result = new XLogIOObject(newLog,
				ProMPluginContextManager.instance().getContext());
		result.setVisualizationType(XLogIOObjectVisualizationType.DEFAULT);

		outputEventLog.deliver(result);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		String[] par2categories = new String[] { NONE, EN, EN_LT, EN_LT_RE };
		ParameterTypeCategory parameterType2 = new ParameterTypeCategory(
				PARAMETER_1_KEY, PARAMETER_1_DESCR, par2categories, 0);
		parameterTypes.add(parameterType2);

		return parameterTypes;
	}

}
