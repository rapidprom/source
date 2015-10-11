package org.rapidprom.operator.abstr;

import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.parameter.ParameterTypeXEventClassifierCategory;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.UndefinedParameterError;

public class AbstractRapidProMDiscoveryOperator extends Operator {

	private InputPort inputXLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);

	private static final String PARAMETER_KEY_EVENT_CLASSIFIER = "event_classifier";
	private static final String PARAMETER_DESC_EVENT_CLASSIFIER = "Specifies how to identify events within the event log";
	private static XEventClassifier[] PARAMETER_DEFAULT_CLASSIFIERS = new XEventClassifier[] { new XEventAndClassifier(
			new XEventNameClassifier()) };

	public AbstractRapidProMDiscoveryOperator(OperatorDescription description) {
		super(description);
		// TODO: make the precondition give a more meaningful warning if the
		// metadata is null
		// inputXLog.addPrecondition(new
		// XLogContainsXEventClassifiersPreCondition(inputXLog));
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();
		params.add(new ParameterTypeXEventClassifierCategory(
				PARAMETER_KEY_EVENT_CLASSIFIER,
				PARAMETER_DESC_EVENT_CLASSIFIER,
				new String[] { PARAMETER_DEFAULT_CLASSIFIERS[0].toString() },
				PARAMETER_DEFAULT_CLASSIFIERS, 0, false, inputXLog));
		return params;
	}

	protected XEventClassifier getXEventClassifier()
			throws UndefinedParameterError {
		ParameterTypeXEventClassifierCategory eClassParam = (ParameterTypeXEventClassifierCategory) getParameterType(PARAMETER_KEY_EVENT_CLASSIFIER);
		return eClassParam
				.getCorrespondingObjectForIndex(getParameterAsInt(PARAMETER_KEY_EVENT_CLASSIFIER));
	}

	protected XLog getXLog() throws UserError {
		return ((XLogIOObject) inputXLog.getData(XLogIOObject.class)).getData();
	}

}
