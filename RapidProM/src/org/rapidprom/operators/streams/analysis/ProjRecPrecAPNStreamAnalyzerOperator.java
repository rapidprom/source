package org.rapidprom.operators.streams.analysis;

import java.io.File;
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
import org.processmining.streamanalysis.core.interfaces.XSStreamAnalyzer;
import org.processmining.streamanalysis.parameters.XSEventStreamAnalyzerParameters;
import org.processmining.streamanalysis.parameters.XSEventStreamAnalyzerParameters.AnalysisScheme;
import org.processmining.streamanalysis.plugins.ProjRecPrecAutomataXSEventStreamAPN2APNAnalyzerPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.AcceptingPetriNetIOObject;
import org.rapidprom.ioobjects.streams.XSStreamAnalyzerIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamToAcceptingPetriNetReaderIOObject;
import org.rapidprom.util.IOUtils;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDirectory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.EqualStringCondition;

public class ProjRecPrecAPNStreamAnalyzerOperator extends Operator {

	private final InputPort streamPort = getInputPorts()
			.createPort("event stream", XSEventStreamIOObject.class);

	private final InputPortExtender referenceModelsPort = new InputPortExtender(
			"accepting petri nets", getInputPorts(), null, 1);

	private final InputPortExtender algorithmsPort = new InputPortExtender(
			"algorithms", getInputPorts(), null, 1);

	private final OutputPort analyzerPort = getOutputPorts()
			.createPort("analyzer");

	private static final String PARAMETER_KEY_ANALYSIS_SCHEME = "analysis_scheme";
	private static final String PARAMETER_DESC_ANALYSIS_SCHEME = "Determine the analysis scheme of the analyzer.";
	private static final String[] PARAMETER_OPTIONS_ANALYSIS_SCHEME = getAnalysisSchemesString();
	private static final AnalysisScheme[] PARAMETER_REFERENCE_ANALYSIS_SCHEME = getAnalysisSchemes();

	private static final String PARAMETER_KEY_END_POINT = "end_point";
	private static final String PARAMETER_DESC_END_POINT = "Determines at what point should the analysis stop.";

	private static final String PARAMETER_KEY_INTERVAL = "interval";
	private static final String PARAMETER_DESC_INTERVAL = "Determines at what points in time the analyser should work.";

	private static final String PARAMETER_KEY_WRITE_TO_FILE = "write_to_file";
	private static final String PARAMETER_DESC_WRITE_TO_FILE = "Indicates whether the analyzer should write the results to a file";

	private final static String PARAMETER_KEY_FOLDER = "folder";
	private final static String PARAMETER_DESC_FOLDER = "The folder where the exported event log should be stored.";

	private final static String PARAMETER_KEY_FILE_NAME = "file_name";
	private final static String PARAMETER_DESC_FILE_NAME = "The file name of the exported event log.";

	private final static String EXPORT_FILE_FORMAT = "csv";

	private final static String PARAMETER_KEY_STORE_MODEL_SEQUENCE = "store_model_sequence";
	private final static String PARAMETER_DESC_STORE_MODEL_SEQUENCE = "Store a sequence of models (showing model evolution), potentially memory expensive";

	private final static String PARAMETER_KEY_STORE_MODEL_DIR = "store_model_dir";
	private final static String PARAMETER_DESC_STORE_MODEL_DIR = "Directory where to store the model sequence";

	public ProjRecPrecAPNStreamAnalyzerOperator(
			OperatorDescription description) {
		super(description);
		referenceModelsPort.start();
		algorithmsPort.start();
		getTransformer().addRule(new GenerateNewMDRule(analyzerPort,
				XSStreamAnalyzerIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		XSEventStreamAnalyzerParameters params;
		try {
			params = getParameterObject();
		} catch (IOException e) {
			throw new OperatorException(e.getMessage());
		}
		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(
						ProjRecPrecAutomataXSEventStreamAPN2APNAnalyzerPlugin.class);
		XSEventStream stream = streamPort.getData(XSEventStreamIOObject.class)
				.getArtifact();
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
		for (InputPort i : algorithmsPort.getManagedPorts()) {
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
		analyzerPort.deliver(
				new XSStreamAnalyzerIOObject<XSEvent, List<List<Double>>, AcceptingPetriNet>(
						analyzer, context));
	}

	private XSEventStreamAnalyzerParameters getParameterObject()
			throws UserError, IOException {
		XSEventStreamAnalyzerParameters params = new XSEventStreamAnalyzerParameters();
		AnalysisScheme scheme = PARAMETER_REFERENCE_ANALYSIS_SCHEME[getParameterAsInt(
				PARAMETER_KEY_ANALYSIS_SCHEME)];
		params.setScheme(scheme);
		params.setEndPoint(getParameterAsInt(PARAMETER_KEY_END_POINT));
		if (scheme.equals(AnalysisScheme.INTERVAL)) {
			params.setInterval(getParameterAsInt(PARAMETER_KEY_INTERVAL));
		}
		if (getParameterAsBoolean(PARAMETER_KEY_WRITE_TO_FILE)) {
			File target = IOUtils.prepareTargetFile(
					getParameterAsFile(PARAMETER_KEY_FOLDER).getCanonicalPath(),
					getParameterAsString(PARAMETER_KEY_FILE_NAME),
					EXPORT_FILE_FORMAT);
			if (target.exists()) {
				target.delete();
			}
			target.createNewFile();
			params.setMetricsFile(target);
		}
		if (getParameterAsBoolean(PARAMETER_KEY_STORE_MODEL_SEQUENCE)) {
			File dir = getParameterAsFile(PARAMETER_KEY_STORE_MODEL_DIR);
			assert (dir.exists() && dir.isDirectory());
			params.setStoreModelSequence(true);
			params.setModelSequenceDirectory(dir);
		}
		params.setVerbose(true);
		return params;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();
		params.add(createAnalysisSchemeParameterType());
		params.add(createEndPointParameterType());

		ParameterType interval = createIntervalParameterType();
		interval.setOptional(true);
		interval.registerDependencyCondition(
				new EqualStringCondition(this, PARAMETER_KEY_ANALYSIS_SCHEME,
						true, AnalysisScheme.INTERVAL.toString()));
		params.add(interval);

		params.add(createWriteToFileParameterType());

		ParameterType dir = createDirectoryChooserParameterType();
		dir.setOptional(true);
		dir.registerDependencyCondition(new BooleanParameterCondition(this,
				PARAMETER_KEY_WRITE_TO_FILE, true, true));
		params.add(dir);

		ParameterType fileName = createFileNameParameterType();
		fileName.setOptional(true);
		fileName.registerDependencyCondition(new BooleanParameterCondition(this,
				PARAMETER_KEY_WRITE_TO_FILE, true, true));
		params.add(fileName);

		params.add(new ParameterTypeBoolean(PARAMETER_KEY_STORE_MODEL_SEQUENCE,
				PARAMETER_DESC_STORE_MODEL_SEQUENCE, false));

		ParameterType modelSequenceDir = new ParameterTypeDirectory(
				PARAMETER_KEY_STORE_MODEL_DIR, PARAMETER_DESC_STORE_MODEL_DIR,
				true);
		modelSequenceDir
				.registerDependencyCondition(new BooleanParameterCondition(this,
						PARAMETER_KEY_STORE_MODEL_SEQUENCE, true, true));
		params.add(modelSequenceDir);
		return params;
	}

	private ParameterType createFileNameParameterType() {
		return new ParameterTypeString(PARAMETER_KEY_FILE_NAME,
				PARAMETER_DESC_FILE_NAME);
	}

	private ParameterType createDirectoryChooserParameterType() {
		return new ParameterTypeDirectory(PARAMETER_KEY_FOLDER,
				PARAMETER_DESC_FOLDER, "");
	}

	private ParameterType createWriteToFileParameterType() {
		return new ParameterTypeBoolean(PARAMETER_KEY_WRITE_TO_FILE,
				PARAMETER_DESC_WRITE_TO_FILE, true);
	}

	private ParameterType createIntervalParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_INTERVAL,
				PARAMETER_DESC_INTERVAL, 0, Integer.MAX_VALUE, 50);
	}

	private ParameterType createEndPointParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_END_POINT,
				PARAMETER_DESC_END_POINT, 0, Integer.MAX_VALUE, 1000);
	}

	private ParameterType createAnalysisSchemeParameterType() {
		return new ParameterTypeCategory(PARAMETER_KEY_ANALYSIS_SCHEME,
				PARAMETER_DESC_ANALYSIS_SCHEME,
				PARAMETER_OPTIONS_ANALYSIS_SCHEME, 0);
	}

	private static AnalysisScheme[] getAnalysisSchemes() {
		return EnumSet.allOf(AnalysisScheme.class).toArray(
				new AnalysisScheme[EnumSet.allOf(AnalysisScheme.class).size()]);
	}

	private static String[] getAnalysisSchemesString() {
		AnalysisScheme[] schemes = getAnalysisSchemes();
		String[] res = new String[schemes.length];
		for (int i = 0; i < getAnalysisSchemes().length; i++) {
			res[i] = schemes[i].toString();
		}
		return res;
	}

}
