package org.rapidprom.operators.streams.analysis;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.processmining.streamanalysis.parameters.XSEventStreamAnalyzerParameters;
import org.processmining.streamanalysis.parameters.XSEventStreamAnalyzerParameters.AnalysisScheme;
import org.processmining.streamanalysis.parameters.XSEventStreamAnalyzerParameters.FragmentationScheme;
import org.rapidprom.ioobjects.streams.XSStreamAnalyzerIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;
import org.rapidprom.util.IOUtils;
import org.rapidprom.util.ObjectUtils;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
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

public abstract class AbstractEventStreamBasedDiscoveryAlgorithmAnalyzer<P extends XSEventStreamAnalyzerParameters>
		extends Operator {

	private final static String PARAMETER_KEY_ANALYSIS_SCHEME = "analysis_scheme";
	private final static String PARAMETER_DESC_ANALYSIS_SCHEME = "Determines the analysis scheme, i.e. Continuous (analyze every packet), or Fragmented (do not analyze every packet).";
	private final static AnalysisScheme[] PARAMETER_OPTIONS_ANALYSIS_SCHEME = EnumSet
			.allOf(AnalysisScheme.class).toArray(new AnalysisScheme[EnumSet
					.allOf(AnalysisScheme.class).size()]);

	private final static String PARAMETER_KEY_FRAGMENTATION_SCHEME = "fragmentation_scheme";
	private final static String PARAMETER_DESC_FRAGMENTATION_SCHEME = "Determines what fragmentation scheme to use if the analysis scheme selected is Fragmented. Currently only a linear scheme is implemented. The linear scheme allows for selecting a step size (s) and a window size (w). The window size should be even. Each time t with k * s - 0.5 * w <= t <= k * s + 0.5 * w for arbitrary k >= 0 will be analyzed resulting in window sizes w + 1 ";
	private final static FragmentationScheme[] PARAMETER_OPTIONS_FRAGMENTATION_SCHEME = EnumSet
			.allOf(FragmentationScheme.class)
			.toArray(new FragmentationScheme[EnumSet
					.allOf(FragmentationScheme.class).size()]);

	private final static String PARAMETER_KEY_STEP_SIZE = "step_size";
	private final static String PARAMETER_DESC_STEP_SIZE = "Determines the step size for the linear fragmentation scheme.";
	private final static int PARAMETER_DEFAULT_VALUE_STEP_SIZE = XSEventStreamAnalyzerParameters.DEFAULT_STEP_SIZE;

	private final static String PARAMETER_KEY_FRAGMENTATION_WINDOW_SIZE = "fragmentation_window_size";
	private final static String PARAMETER_DESC_FRAGMENTATION_WINDOW_SIZE = "Determines what window size should be used for the (linear) fragmentation scheme.";
	private final static int PARAMETER_DEFAULT_VALUE_FRAGMENTATION_WINDOW_SIZE = XSEventStreamAnalyzerParameters.DEFAULT_FRAGMENTATION_WINDOW_SIZE;

	private final static String EXPORT_FILE_FORMAT = "csv";
	private static final String PARAMETER_DESC_END_POINT = "Determines at what point the analysis should stop.";

	private final static String PARAMETER_DESC_FILE_NAME = "The file name of the exported event log.";
	private final static String PARAMETER_DESC_FOLDER = "The folder where the exported event log should be stored.";

	// currently disabled storing model sequence.
	@SuppressWarnings("unused")
	private final static String PARAMETER_DESC_STORE_MODEL_DIR = "Directory where to store the model sequence";
	@SuppressWarnings("unused")
	private final static String PARAMETER_DESC_STORE_MODEL_SEQUENCE = "Store a sequence of models (showing model evolution), potentially memory expensive";

	private static final String PARAMETER_DESC_WRITE_TO_FILE = "Indicates whether the analyzer should write the results to a file";
	private static final String PARAMETER_KEY_END_POINT = "end_point";

	private final static String PARAMETER_KEY_FILE_NAME = "file_name";

	private final static String PARAMETER_KEY_FOLDER = "folder";
	@SuppressWarnings("unused")
	private final static String PARAMETER_KEY_STORE_MODEL_DIR = "store_model_dir";

	@SuppressWarnings("unused")
	private final static String PARAMETER_KEY_STORE_MODEL_SEQUENCE = "store_model_sequence";
	private static final String PARAMETER_KEY_WRITE_TO_FILE = "write_to_file";

	private final InputPortExtender algorithmsPort = new InputPortExtender(
			"algorithms", getInputPorts(), null, 1);

	private P analyzerParameters;

	public void setAnalyzerParameters(P analyzerParameters) {
		this.analyzerParameters = analyzerParameters;
	}

	private final OutputPort analyzerPort = getOutputPorts()
			.createPort("analyzer");

	private final InputPort streamPort = getInputPorts()
			.createPort("event stream", XSEventStreamIOObject.class);

	public AbstractEventStreamBasedDiscoveryAlgorithmAnalyzer(
			OperatorDescription description, P parameters) {
		super(description);
		getAlgorithmsPort().start();
		getTransformer().addRule(new GenerateNewMDRule(getAnalyzerPort(),
				XSStreamAnalyzerIOObject.class));
		this.analyzerParameters = parameters;
	}

	private ParameterType createDirectoryChooserParameterType() {
		return new ParameterTypeDirectory(PARAMETER_KEY_FOLDER,
				PARAMETER_DESC_FOLDER, "");
	}

	private ParameterType createEndPointParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_END_POINT,
				PARAMETER_DESC_END_POINT, 0, Integer.MAX_VALUE, 1000);
	}

	private ParameterType createFileNameParameterType() {
		return new ParameterTypeString(PARAMETER_KEY_FILE_NAME,
				PARAMETER_DESC_FILE_NAME);
	}

	private ParameterType createWriteToFileParameterType() {
		return new ParameterTypeBoolean(PARAMETER_KEY_WRITE_TO_FILE,
				PARAMETER_DESC_WRITE_TO_FILE, true);
	}

	/**
	 * @return the algorithmsPort
	 */
	public InputPortExtender getAlgorithmsPort() {
		return algorithmsPort;
	}

	public P getAnalyzerParameters() {
		return analyzerParameters;
	}

	/**
	 * @return the analyzerPort
	 */
	public OutputPort getAnalyzerPort() {
		return analyzerPort;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();
		ParameterTypeCategory analysisScheme = createAnalysisSchemeParameterType();
		params.add(analysisScheme);
		ParameterTypeCategory fragmentationScheme = createFragmentationSchemeParameterType();
		fragmentationScheme.registerDependencyCondition(
				new EqualStringCondition(this, PARAMETER_KEY_ANALYSIS_SCHEME,
						true, AnalysisScheme.FRAGMENTED.toString()));
		params.add(fragmentationScheme);

		ParameterTypeInt stepSizeParam = createStepSizeParameterType();
		stepSizeParam.registerDependencyCondition(
				new EqualStringCondition(this, PARAMETER_KEY_ANALYSIS_SCHEME,
						true, AnalysisScheme.FRAGMENTED.toString()));
		params.add(stepSizeParam);

		ParameterTypeInt windowSizeParam = createWindowSizeParameterType();
		windowSizeParam.registerDependencyCondition(
				new EqualStringCondition(this, PARAMETER_KEY_ANALYSIS_SCHEME,
						true, AnalysisScheme.FRAGMENTED.toString()));
		params.add(windowSizeParam);

		params.add(createEndPointParameterType());

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

		// params.add(new
		// ParameterTypeBoolean(PARAMETER_KEY_STORE_MODEL_SEQUENCE,
		// PARAMETER_DESC_STORE_MODEL_SEQUENCE, false));
		//
		// ParameterType modelSequenceDir = new ParameterTypeDirectory(
		// PARAMETER_KEY_STORE_MODEL_DIR, PARAMETER_DESC_STORE_MODEL_DIR,
		// true);
		// modelSequenceDir
		// .registerDependencyCondition(new BooleanParameterCondition(this,
		// PARAMETER_KEY_STORE_MODEL_SEQUENCE, true, true));
		// params.add(modelSequenceDir);
		return params;
	}

	private ParameterTypeInt createWindowSizeParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_FRAGMENTATION_WINDOW_SIZE,
				PARAMETER_DESC_FRAGMENTATION_WINDOW_SIZE, 0, Integer.MAX_VALUE,
				PARAMETER_DEFAULT_VALUE_FRAGMENTATION_WINDOW_SIZE, false);
	}

	private ParameterTypeInt createStepSizeParameterType() {
		return new ParameterTypeInt(PARAMETER_KEY_STEP_SIZE,
				PARAMETER_DESC_STEP_SIZE, 1, Integer.MAX_VALUE,
				PARAMETER_DEFAULT_VALUE_STEP_SIZE, false);
	}

	private ParameterTypeCategory createAnalysisSchemeParameterType() {
		return new ParameterTypeCategory(PARAMETER_KEY_ANALYSIS_SCHEME,
				PARAMETER_DESC_ANALYSIS_SCHEME,
				ObjectUtils.toString(PARAMETER_OPTIONS_ANALYSIS_SCHEME), 0,
				false);
	}

	private ParameterTypeCategory createFragmentationSchemeParameterType() {
		return new ParameterTypeCategory(PARAMETER_KEY_FRAGMENTATION_SCHEME,
				PARAMETER_DESC_FRAGMENTATION_SCHEME,
				ObjectUtils.toString(PARAMETER_OPTIONS_FRAGMENTATION_SCHEME), 0,
				false);
	}

	/**
	 * @return the streamPort
	 */
	public InputPort getStreamPort() {
		return streamPort;
	}

	protected P parseParameters() throws UserError, IOException {
		setAnalyzerParameters(renewParameters());
		getAnalyzerParameters()
				.setEndPoint(getParameterAsInt(PARAMETER_KEY_END_POINT));
		getAnalyzerParameters().setAnalysisScheme(
				PARAMETER_OPTIONS_ANALYSIS_SCHEME[getParameterAsInt(
						PARAMETER_KEY_ANALYSIS_SCHEME)]);
		if (getAnalyzerParameters().getAnalysisScheme()
				.equals(AnalysisScheme.FRAGMENTED)) {
			getAnalyzerParameters().setFragmentationScheme(
					PARAMETER_OPTIONS_FRAGMENTATION_SCHEME[getParameterAsInt(
							PARAMETER_KEY_FRAGMENTATION_SCHEME)]);
			getAnalyzerParameters()
					.setStepSize(getParameterAsInt(PARAMETER_KEY_STEP_SIZE));
			getAnalyzerParameters().setFragmentationWindowSize(
					getParameterAsInt(PARAMETER_KEY_FRAGMENTATION_WINDOW_SIZE));
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
			getAnalyzerParameters().setMetricsFile(target);
		}
		// if (getParameterAsBoolean(PARAMETER_KEY_STORE_MODEL_SEQUENCE)) {
		// File dir = getParameterAsFile(PARAMETER_KEY_STORE_MODEL_DIR);
		// assert (dir.exists() && dir.isDirectory());
		// getAnalyzerParameters().setStoreModelSequence(true);
		// getAnalyzerParameters().setModelSequenceDirectory(dir);
		// }
		getAnalyzerParameters().setVerbose(true);
		return getAnalyzerParameters();
	}

	/**
	 * The parameters have to be renewed in a repeated experimental setting such
	 * that writing to file succeeds.
	 * 
	 * @return fresh instance of P
	 */
	protected abstract P renewParameters();

}
