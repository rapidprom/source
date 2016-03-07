package org.rapidprom.operators.streams.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetArrayFactory;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.readers.acceptingpetrinet.XSEventStreamToAcceptingPetriNetReader;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.streamanalysis.core.interfaces.XSStreamAnalyzer;
import org.processmining.streamanalysis.parameters.ProjRecPrecAnalyzerParametersImpl;
import org.processmining.streamanalysis.parameters.XSEventStreamAnalyzerParameters.AnalysisScheme;
import org.processmining.streamanalysis.plugins.ProjRecPrecAutomataXSEventStreamAPN2APNAnalyzerPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.AcceptingPetriNetIOObject;
import org.rapidprom.ioobjects.streams.XSStreamAnalyzerIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamToAcceptingPetriNetReaderIOObject;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;

public class ProjRecPrecAPNStreamAnalyzerOperator extends
		AbstractEventStreamBasedDiscoveryAlgorithmAnalyzer<ProjRecPrecAnalyzerParametersImpl> {

	private final InputPortExtender referenceModelsPort = new InputPortExtender(
			"accepting petri nets", getInputPorts(), null, 1);

	@Deprecated
	private static final String PARAMETER_KEY_ANALYSIS_SCHEME = "analysis_scheme";
	@Deprecated
	private static final String PARAMETER_DESC_ANALYSIS_SCHEME = "Determine the analysis scheme of the analyzer.";
	@Deprecated
	private static final String[] PARAMETER_OPTIONS_ANALYSIS_SCHEME = getAnalysisSchemesString();
	@SuppressWarnings("unused")
	@Deprecated
	private static final AnalysisScheme[] PARAMETER_REFERENCE_ANALYSIS_SCHEME = getAnalysisSchemes();

	@Deprecated
	private static final String PARAMETER_KEY_INTERVAL = "interval";
	@Deprecated
	private static final String PARAMETER_DESC_INTERVAL = "Determines at what points in time the analyser should work.";

	private final static String PARAMETER_KEY_MAX_STATE_SPACE = "max_state_space";
	private final static String PARAMETER_DESC_MAX_STATE_SPACE = "Determine the maximal size of the state space of the underlying automaton.";
	private final static int PARAMETER_DEFAULT_MAX_STATE_SPACE = 2000;

	private final static String PARAMETER_KEY_PROJECTION_SIZE = "projection_size";
	private final static String PARAMETER_DESC_PROJECTION_SIZE = "Determine the number of activities taken into account per projection";
	private final static int PARAMETER_DEFAULT_PROJECTION_SIZE = 2;

	public ProjRecPrecAPNStreamAnalyzerOperator(
			OperatorDescription description) {
		super(description, new ProjRecPrecAnalyzerParametersImpl());
		referenceModelsPort.start();
	}

	@Override
	public void doWork() throws OperatorException {
		ProjRecPrecAnalyzerParametersImpl params;
		try {
			params = parseParameters();
		} catch (IOException e) {
			throw new OperatorException(e.getMessage());
		}
		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(
						ProjRecPrecAutomataXSEventStreamAPN2APNAnalyzerPlugin.class);
		XSEventStream stream = getStreamPort()
				.getData(XSEventStreamIOObject.class).getArtifact();
		AcceptingPetriNetArray arr = AcceptingPetriNetArrayFactory
				.createAcceptingPetriNetArray();
		for (InputPort i : referenceModelsPort.getManagedPorts()) {
			try {
				arr.addNet(i.getData(AcceptingPetriNetIOObject.class)
						.getArtifact());
			} catch (UserError e) {
			}
		}
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
		XSStreamAnalyzer<XSEvent, List<List<Double>>, AcceptingPetriNet> analyzer = ProjRecPrecAutomataXSEventStreamAPN2APNAnalyzerPlugin
				.run(context, stream, arr, params,
						algos.toArray(
								new XSEventStreamToAcceptingPetriNetReader[algos
										.size()]));
		getAnalyzerPort().deliver(
				new XSStreamAnalyzerIOObject<XSEvent, List<List<Double>>, AcceptingPetriNet>(
						analyzer, context));
	}

	@Override
	protected ProjRecPrecAnalyzerParametersImpl parseParameters()
			throws UserError, IOException {
		ProjRecPrecAnalyzerParametersImpl params = super.parseParameters();
		CompareParameters compareParameters = new CompareParameters(
				getParameterAsInt(PARAMETER_KEY_PROJECTION_SIZE));
		compareParameters.setMaxStatesReachabilityGraph(
				getParameterAsInt(PARAMETER_KEY_MAX_STATE_SPACE));
		params.setProjRecParams(compareParameters);
		return params;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();
		params.add(createMaxStateSpaceParameterType());
		params.add(createProjectionSizeParameterType());
		return params;
	}

	@SuppressWarnings("unused")
	@Deprecated
	private ParameterType createIntervalParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_INTERVAL,
				PARAMETER_DESC_INTERVAL, 0, Integer.MAX_VALUE, 50);
	}

	@SuppressWarnings("unused")
	@Deprecated
	private ParameterType createAnalysisSchemeParameterType() {
		return new ParameterTypeCategory(PARAMETER_KEY_ANALYSIS_SCHEME,
				PARAMETER_DESC_ANALYSIS_SCHEME,
				PARAMETER_OPTIONS_ANALYSIS_SCHEME, 0);
	}

	private ParameterTypeInt createMaxStateSpaceParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_MAX_STATE_SPACE,
				PARAMETER_DESC_MAX_STATE_SPACE, 1, Integer.MAX_VALUE,
				PARAMETER_DEFAULT_MAX_STATE_SPACE, true);
	}

	private ParameterTypeInt createProjectionSizeParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_PROJECTION_SIZE,
				PARAMETER_DESC_PROJECTION_SIZE, 2, 3,
				PARAMETER_DEFAULT_PROJECTION_SIZE, true);
	}

	@Deprecated
	private static AnalysisScheme[] getAnalysisSchemes() {
		return EnumSet.allOf(AnalysisScheme.class).toArray(
				new AnalysisScheme[EnumSet.allOf(AnalysisScheme.class).size()]);
	}

	@Deprecated
	private static String[] getAnalysisSchemesString() {
		AnalysisScheme[] schemes = getAnalysisSchemes();
		String[] res = new String[schemes.length];
		for (int i = 0; i < getAnalysisSchemes().length; i++) {
			res[i] = schemes[i].toString();
		}
		return res;
	}

}
