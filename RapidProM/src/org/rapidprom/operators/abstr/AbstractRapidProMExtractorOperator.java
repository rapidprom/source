package org.rapidprom.operators.abstr;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessSetupError.Severity;
import com.rapidminer.operator.io.AbstractReader;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.MDTransformationRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.MetaDataError;
import com.rapidminer.operator.ports.metadata.SimpleMetaDataError;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Observable;
import com.rapidminer.tools.Observer;

/**
 * The Abstract Extractor uses a lot of code from the {@link AbstractReader}
 * class in order to be able to produce meta data. Also it predefines an input
 * and output port
 * 
 * @author svzelst
 *
 * @param <T>
 */
public abstract class AbstractRapidProMExtractorOperator<T extends IOObject>
		extends Operator {

	protected final InputPort inputfile = getInputPorts().createPort("file",
			FileObject.class);
	protected final OutputPort outputPort = getOutputPorts()
			.createPort("output");
	protected final Class<? extends IOObject> generatedClass;

	protected boolean cacheDirty = true;
	protected MetaData cachedMetaData;
	protected MetaDataError cachedError;

	public AbstractRapidProMExtractorOperator(OperatorDescription description,
			Class<? extends IOObject> generatedClass) {
		super(description);
		this.generatedClass = generatedClass;
		getTransformer().addRule(new MDTransformationRule() {
			@Override
			public void transformMD() {
				if (cacheDirty || !isMetaDataCacheable()) {
					try {
						cachedMetaData = AbstractRapidProMExtractorOperator.this
								.getGeneratedMetaData();
						cachedError = null;
					} catch (OperatorException e) {
						cachedMetaData = new MetaData(
								AbstractRapidProMExtractorOperator.this.generatedClass);
						String msg = e.getMessage();
						if ((msg == null) || (msg.length() == 0)) {
							msg = e.toString();
						}
						// will be added below
						cachedError = new SimpleMetaDataError(Severity.WARNING,
								outputPort, "cannot_create_exampleset_metadata",
								new Object[] { msg });
					}
					if (cachedMetaData != null) {
						cachedMetaData.addToHistory(outputPort);
					}
					cacheDirty = false;
				}
				outputPort.deliverMD(cachedMetaData);
				if (cachedError != null) {
					outputPort.addError(cachedError);
				}
			}
		});
		observeParameters();
	}

	protected void observeParameters() {
		// we add this as the first observer. otherwise, this change is not seen
		// by the resulting meta data transformation
		getParameters().addObserverAsFirst(new Observer<String>() {
			@Override
			public void update(Observable<String> observable, String arg) {
				cacheDirty = true;
			}
		}, false);
	}

	public MetaData getGeneratedMetaData() throws OperatorException {
		return new MetaData(generatedClass);
	}

	protected boolean isMetaDataCacheable() {
		return false;
	}

	/**
	 * Creates (or reads) the ExampleSet that will be returned by
	 * {@link #apply()}.
	 */
	public abstract T read() throws OperatorException;

	@Override
	public void doWork() throws OperatorException {
		final T result = read();
		addAnnotations(result);
		outputPort.deliver(result);
	}

	/** Describes an operator that can read certain file types. */
	public static class ExtractorDescription {
		private final String fileExtension;
		private final Class<? extends AbstractRapidProMExtractorOperator<?>> extractorClass;
		/** This parameter must be set to the file name. */
		private final String fileParameterKey;

		public ExtractorDescription(String fileExtension,
				Class<? extends AbstractRapidProMExtractorOperator<?>> extractorClass,
				String fileParameterKey) {
			super();
			this.fileExtension = fileExtension;
			this.extractorClass = extractorClass;
			this.fileParameterKey = fileParameterKey;
		}
	}

	private static final Map<String, ExtractorDescription> EXTRACTOR_DESCRIPTIONS = new HashMap<String, ExtractorDescription>();

	protected void addAnnotations(T result) {
		for (ExtractorDescription ed : EXTRACTOR_DESCRIPTIONS.values()) {
			if (ed.extractorClass.equals(this.getClass())) {
				if (result.getAnnotations()
						.getAnnotation(Annotations.KEY_SOURCE) == null) {
					try {
						String source = getParameter(ed.fileParameterKey);
						if (source != null) {
							result.getAnnotations().setAnnotation(
									Annotations.KEY_SOURCE, source);
						}
					} catch (UndefinedParameterError e) {
					}
				}
				return;
			}
		}
	}

	/** Registers an operator that can read files with a given extension. */
	protected static void registerExtractorDescription(
			ExtractorDescription rd) {
		EXTRACTOR_DESCRIPTIONS.put(rd.fileExtension.toLowerCase(), rd);
	}

}
