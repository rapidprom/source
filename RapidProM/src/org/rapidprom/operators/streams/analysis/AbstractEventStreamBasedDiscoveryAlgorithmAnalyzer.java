package org.rapidprom.operators.streams.analysis;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.processmining.streamanalysis.parameters.XSEventStreamAnalyzerParameters;
import org.rapidprom.ioobjects.streams.XSStreamAnalyzerIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;
import org.rapidprom.util.IOUtils;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDirectory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;

public abstract class AbstractEventStreamBasedDiscoveryAlgorithmAnalyzer<P extends XSEventStreamAnalyzerParameters>
		extends Operator {

	private final static String EXPORT_FILE_FORMAT = "csv";
	private static final String PARAMETER_DESC_END_POINT = "Determines at what point should the analysis stop.";

	private final static String PARAMETER_DESC_FILE_NAME = "The file name of the exported event log.";
	private final static String PARAMETER_DESC_FOLDER = "The folder where the exported event log should be stored.";

	private final static String PARAMETER_DESC_STORE_MODEL_DIR = "Directory where to store the model sequence";
	private final static String PARAMETER_DESC_STORE_MODEL_SEQUENCE = "Store a sequence of models (showing model evolution), potentially memory expensive";

	private static final String PARAMETER_DESC_WRITE_TO_FILE = "Indicates whether the analyzer should write the results to a file";
	private static final String PARAMETER_KEY_END_POINT = "end_point";

	private final static String PARAMETER_KEY_FILE_NAME = "file_name";

	private final static String PARAMETER_KEY_FOLDER = "folder";
	private final static String PARAMETER_KEY_STORE_MODEL_DIR = "store_model_dir";

	private final static String PARAMETER_KEY_STORE_MODEL_SEQUENCE = "store_model_sequence";
	private static final String PARAMETER_KEY_WRITE_TO_FILE = "write_to_file";

	private final InputPortExtender algorithmsPort = new InputPortExtender(
			"algorithms", getInputPorts(), null, 1);

	private final OutputPort analyzerPort = getOutputPorts()
			.createPort("analyzer");

	private final P parameters;

	private final InputPort streamPort = getInputPorts()
			.createPort("event stream", XSEventStreamIOObject.class);

	public AbstractEventStreamBasedDiscoveryAlgorithmAnalyzer(
			OperatorDescription description, P parameters) {
		super(description);
		getAlgorithmsPort().start();
		getTransformer().addRule(new GenerateNewMDRule(getAnalyzerPort(),
				XSStreamAnalyzerIOObject.class));
		this.parameters = parameters;
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

	public P getAlgorithmParameters() {
		return parameters;
	}

	/**
	 * @return the algorithmsPort
	 */
	public InputPortExtender getAlgorithmsPort() {
		return algorithmsPort;
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

	/**
	 * @return the streamPort
	 */
	public InputPort getStreamPort() {
		return streamPort;
	}

	protected P parseParameters() throws UserError, IOException {
		getAlgorithmParameters()
				.setEndPoint(getParameterAsInt(PARAMETER_KEY_END_POINT));
		if (getParameterAsBoolean(PARAMETER_KEY_WRITE_TO_FILE)) {
			File target = IOUtils.prepareTargetFile(
					getParameterAsFile(PARAMETER_KEY_FOLDER).getCanonicalPath(),
					getParameterAsString(PARAMETER_KEY_FILE_NAME),
					EXPORT_FILE_FORMAT);
			if (target.exists()) {
				target.delete();
			}
			target.createNewFile();
			getAlgorithmParameters().setMetricsFile(target);
		}
		if (getParameterAsBoolean(PARAMETER_KEY_STORE_MODEL_SEQUENCE)) {
			File dir = getParameterAsFile(PARAMETER_KEY_STORE_MODEL_DIR);
			assert (dir.exists() && dir.isDirectory());
			getAlgorithmParameters().setStoreModelSequence(true);
			getAlgorithmParameters().setModelSequenceDirectory(dir);
		}
		getAlgorithmParameters().setVerbose(true);
		return getAlgorithmParameters();
	}

}
