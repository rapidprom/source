package org.rapidprom.operators.streams.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.readers.acceptingpetrinet.XSEventStreamToAcceptingPetriNetReader;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSReader;
import org.processmining.streamanalysis.core.interfaces.XSStreamAnalyzer;
import org.processmining.streamanalysis.parameters.AlignmentAnalyzerParametersImpl;
import org.processmining.streamanalysis.plugins.AlignmentAPNAnalyzerPlugin;
import org.processmining.streamanalysis.utils.XLogArray;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.ioobjects.streams.XSStreamAnalyzerIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamToAcceptingPetriNetReaderIOObject;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;

public class AlignmentAPNAnalyzerOperator extends
		AbstractEventStreamBasedDiscoveryAlgorithmAnalyzer<AlignmentAnalyzerParametersImpl> {

	private final static String PARAMETER_KEY_MAX_STATE_SPACE = "max_state_space";
	private final static String PARAMETER_DESC_MAX_STATE_SPACE = "Determine the maximal size of the state space of the underlying automaton.";
	private final static int PARAMETER_DEFAULT_MAX_STATE_SPACE = 25000;

	private final static String PARAMETER_KEY_TIMEOUT = "timeout";
	private final static String PARAMETER_DESC_TIMEOUT = "Determine the timeout in ms for calcuating the alignments";
	private final static int PARAMETER_DEFAULT_TIMEOUT = 5000;

	private final InputPortExtender referenceLogsPort = new InputPortExtender(
			"logs", getInputPorts(), null, 1);

	public AlignmentAPNAnalyzerOperator(OperatorDescription description) {
		super(description, new AlignmentAnalyzerParametersImpl());
		referenceLogsPort.start();
	}

	@Override
	public void doWork() throws OperatorException {
		AlignmentAnalyzerParametersImpl params;
		try {
			params = parseParameters();
		} catch (IOException e) {
			throw new OperatorException(e.getMessage());
		}
		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(AlignmentAPNAnalyzerPlugin.class);
		XSEventStream stream = getStreamPort()
				.getData(XSEventStreamIOObject.class).getArtifact();
		XLogArray arr = new XLogArray();
		for (InputPort i : referenceLogsPort.getManagedPorts()) {
			try {
				arr.add(i.getData(XLogIOObject.class).getArtifact());
			} catch (UserError e) {
			}
		}
		// TODO: use meta-data to process classifiers!
		List<XEventClassifier> classifiers = fetchClassifiers(arr);
		params.setClassifier(classifiers.get(0));
		List<XSEventStreamToAcceptingPetriNetReader> algos = new ArrayList<XSEventStreamToAcceptingPetriNetReader>();
		for (InputPort i : getAlgorithmsPort().getManagedPorts()) {
			try {
				algos.add((XSEventStreamToAcceptingPetriNetReader) i
						.getData(
								XSEventStreamToAcceptingPetriNetReaderIOObject.class)
						.getArtifact());
			} catch (UserError e) {
			}
		}
		XSStreamAnalyzer<XSEvent, Map<XSReader<XSEvent, AcceptingPetriNet>, Map<Long, Iterable<Iterable<Double>>>>, AcceptingPetriNet> analyzer = AlignmentAPNAnalyzerPlugin
				.run(context, stream, arr, params,
						algos.toArray(
								new XSEventStreamToAcceptingPetriNetReader[algos
										.size()]));
		getAnalyzerPort().deliver(
				new XSStreamAnalyzerIOObject<XSEvent, Map<XSReader<XSEvent, AcceptingPetriNet>, Map<Long, Iterable<Iterable<Double>>>>, AcceptingPetriNet>(
						analyzer, context));
	}

	private List<XEventClassifier> fetchClassifiers(XLogArray array) {
		List<XEventClassifier> classifiers = new ArrayList<>();
		classifiers.addAll(array.get(0).getClassifiers());
		for (int i = 1; i < array.size(); i++) {
			classifiers.retainAll(array.get(i).getClassifiers());
		}
		return classifiers;
	}

	@Override
	protected AlignmentAnalyzerParametersImpl parseParameters()
			throws UserError, IOException {
		AlignmentAnalyzerParametersImpl params = super.parseParameters();
		params.setMaxNumberOfStates(
				getParameterAsInt(PARAMETER_KEY_MAX_STATE_SPACE));
		params.setTimeoutMili(getParameterAsInt(PARAMETER_KEY_TIMEOUT));
		return params;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();
		params.add(createMaxStateSpaceParameterType());
		params.add(createTimeoutParameterType());
		return params;
	}

	private ParameterTypeInt createMaxStateSpaceParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_MAX_STATE_SPACE,
				PARAMETER_DESC_MAX_STATE_SPACE, 1, Integer.MAX_VALUE,
				PARAMETER_DEFAULT_MAX_STATE_SPACE, true);
	}

	private ParameterTypeInt createTimeoutParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_TIMEOUT,
				PARAMETER_DESC_TIMEOUT, 0, Integer.MAX_VALUE,
				PARAMETER_DEFAULT_TIMEOUT, true);
	}

	@Override
	protected AlignmentAnalyzerParametersImpl renewParameters() {
		return new AlignmentAnalyzerParametersImpl();
	}

}
