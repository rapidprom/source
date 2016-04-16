package org.rapidprom.operators.streams.discovery.abstr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.processmining.eventstream.core.interfaces.XSEventSignature;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.readers.abstractions.CAxAADataStoreBasedDFAReaderImpl;
import org.processmining.eventstream.readers.abstractions.XSEventStreamToDFAReaderParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSDataPacket;
import org.processmining.stream.core.interfaces.XSReader;
import org.processmining.stream.model.datastructure.DSParameter;
import org.processmining.stream.model.datastructure.DSParameterDefinition;
import org.processmining.stream.model.datastructure.DSParameterFactory;
import org.processmining.stream.model.datastructure.DataStructure.Type;
import org.rapidprom.ioobjects.streams.XSReaderIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;
import org.rapidprom.util.ObjectUtils;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualStringCondition;

public abstract class AbstractDFABasedMinerOperator<D extends XSDataPacket<?, ?>, R, P extends XSEventStreamToDFAReaderParameters>
		extends Operator {

	private InputPort streamInputPort = getInputPorts()
			.createPort("event stream", XSEventStreamIOObject.class);

	private OutputPort readerOutputPort = getOutputPorts().createPort("reader");

	protected static final String PARAMETER_KEY_CASE_IDENTIFIER = "case_identifier";
	protected static final String PARAMETER_DESC_CASE_IDENTIFIER = "Defines what key to use within the data packet to identify a case.";
	protected static final String PARAMETER_DEFAULT_CASE_IDENTIFIER = XSEventSignature.TRACE
			.toString();

	protected static final String PARAMETER_KEY_EVENT_IDENTIFIER = "activity_identifier";
	protected static final String PARAMETER_DESC_EVENT_IDENTIFIER = "Defines what key to use within the data packet to identify the activity of the event.";
	protected static final String PARAMETER_DEFAULT_EVENT_IDENTIFIER = XConceptExtension.KEY_NAME;

	protected static final String PARAMETER_KEY_REFRESH_RATE = "refresh_rate";
	protected static final String PARAMETER_DESC_REFRESH_RATE = "Defines at what intervals (in terms of messages received) a new model should be queried.";
	protected static final int PARAMETER_DEFAULT_REFRESH_RATE = -1;

	protected static final String PARAMETER_KEY_CASE_ACTIVITY_STORE = "case_activity_store";
	protected static final String PARAMETER_DESC_CASE_ACTIVITY_STORE = "Defines what stream-based data store to use for capturing CASE X ACTIVITY information.";
	protected static final Type[] PARAMETER_OPTIONS_CASE_ACTIVITY_STORE = CAxAADataStoreBasedDFAReaderImpl.DEFAULT_ALLOWED_CASE_ACTIVITY_DATA_STRUCTURES
			.toArray(
					new Type[CAxAADataStoreBasedDFAReaderImpl.DEFAULT_ALLOWED_CASE_ACTIVITY_DATA_STRUCTURES
							.size()]);

	protected static final String PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE = "activity_activity_store";
	protected static final String PARAMETER_DESC_ACTIVITY_ACTIVITY_STORE = "Defines what stream-based data store to use for capturing ACITIVTY X ACTIVITY information.";
	protected static final Type[] PARAMETER_OPTIONS_ACTIVITY_ACTIVITY_STORE = CAxAADataStoreBasedDFAReaderImpl.DEFAULT_ALLOWED_ACTIVITY_ACTIVITY_DATA_STRUCTURES
			.toArray(
					new Type[CAxAADataStoreBasedDFAReaderImpl.DEFAULT_ALLOWED_ACTIVITY_ACTIVITY_DATA_STRUCTURES
							.size()]);

	public AbstractDFABasedMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(readerOutputPort,
				XSReaderIOObject.class));
	}

	protected abstract PluginContext getPluginContextForAlgorithm();

	protected abstract XSReader<D, R> getAlgorithm(PluginContext context,
			XSEventStream stream, P parameters);

	protected abstract XSReaderIOObject<D, R> getIOObject(
			XSReader<D, R> algorithm, PluginContext context);

	@Override
	public void doWork() throws UserError {
		XSEventStream eventStream = streamInputPort
				.getData(XSEventStreamIOObject.class).getArtifact();
		PluginContext context = getPluginContextForAlgorithm();
		P params = getAlgorithmParameterObject();
		params.setCaseIdentifier(
				getParameterAsString(PARAMETER_KEY_CASE_IDENTIFIER));
		params.setActivityIdentifier(
				getParameterAsString(PARAMETER_KEY_EVENT_IDENTIFIER));
		params.setRefreshRate(getParameterAsInt(PARAMETER_KEY_REFRESH_RATE));
		params = setCaseActivityDataStructure(params);
		params = setActivityActivityDataStructure(params);
		XSReader<D, R> reader = getAlgorithm(context, eventStream, params);
		reader.startXSRunnable();
		readerOutputPort.deliver(getIOObject(reader, context));
	}

	protected abstract P getAlgorithmParameterObject();

	protected P setActivityActivityDataStructure(P params)
			throws UndefinedParameterError {
		Type activityActivityStoreType = PARAMETER_OPTIONS_ACTIVITY_ACTIVITY_STORE[getParameterAsInt(
				PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE)];
		params.setActivityActivityDataStructureType(activityActivityStoreType);
		params.setActivityActivityDataStructureParameters(
				getDataStructureParameters(
						PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE,
						activityActivityStoreType));
		return params;
	}

	protected P setCaseActivityDataStructure(P params)
			throws UndefinedParameterError {
		Type caseActivityStoreType = PARAMETER_OPTIONS_CASE_ACTIVITY_STORE[getParameterAsInt(
				PARAMETER_KEY_CASE_ACTIVITY_STORE)];
		params.setCaseActivityDataStructureType(caseActivityStoreType);
		params.setCaseActivityDataStructureParameters(
				getDataStructureParameters(PARAMETER_KEY_CASE_ACTIVITY_STORE,
						caseActivityStoreType));
		return params;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();

		params.add(new ParameterTypeString(PARAMETER_KEY_CASE_IDENTIFIER,
				PARAMETER_DESC_CASE_IDENTIFIER,
				PARAMETER_DEFAULT_CASE_IDENTIFIER, false));

		params.add(new ParameterTypeString(PARAMETER_KEY_EVENT_IDENTIFIER,
				PARAMETER_DESC_EVENT_IDENTIFIER,
				PARAMETER_DEFAULT_EVENT_IDENTIFIER, false));

		params.add(new ParameterTypeInt(PARAMETER_KEY_REFRESH_RATE,
				PARAMETER_DESC_REFRESH_RATE, -1, Integer.MAX_VALUE,
				PARAMETER_DEFAULT_REFRESH_RATE, true));

		params.add(new ParameterTypeCategory(PARAMETER_KEY_CASE_ACTIVITY_STORE,
				PARAMETER_DESC_CASE_ACTIVITY_STORE,
				ObjectUtils.toString(PARAMETER_OPTIONS_CASE_ACTIVITY_STORE), 0,
				false));

		params = addDataStructureDependencyConditions(
				PARAMETER_KEY_CASE_ACTIVITY_STORE,
				PARAMETER_OPTIONS_CASE_ACTIVITY_STORE, params);

		params.add(
				new ParameterTypeCategory(PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE,
						PARAMETER_DESC_ACTIVITY_ACTIVITY_STORE,
						ObjectUtils.toString(
								PARAMETER_OPTIONS_ACTIVITY_ACTIVITY_STORE),
						0, false));

		params = addDataStructureDependencyConditions(
				PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE,
				PARAMETER_OPTIONS_ACTIVITY_ACTIVITY_STORE, params);

		return params;
	}

	protected List<ParameterType> addDataStructureDependencyConditions(
			String parameterTypeKey, Type[] options,
			List<ParameterType> params) {
		for (Type dataStructureType : options) {
			for (DSParameterDefinition paramDef : dataStructureType
					.getParameterDefinition()) {
				String key = getDataStructureParameterSubKey(parameterTypeKey,
						dataStructureType, paramDef.getName());
				ParameterType param = new ParameterTypeString(key, "",
						paramDef.getDefaultValue().toString(), false);
				param.setOptional(true);
				param.registerDependencyCondition(new EqualStringCondition(this,
						parameterTypeKey, true, dataStructureType.toString()));
				params.add(param);
			}
		}
		return params;
	}

	protected String getDataStructureParameterSubKey(
			String parentParameterTypeKey, Type sbdst, String param) {
		return sbdst.toString().toLowerCase() + "_" + parentParameterTypeKey
				+ "_" + param;
	}

	protected Map<DSParameterDefinition, DSParameter<?>> getDataStructureParameters(
			String parameterTypeKey, Type dataStructureType)
			throws UndefinedParameterError {
		Map<DSParameterDefinition, DSParameter<?>> result = new HashMap<DSParameterDefinition, DSParameter<?>>();
		for (DSParameterDefinition paramDef : dataStructureType
				.getParameterDefinition()) {
			String subKey = getDataStructureParameterSubKey(parameterTypeKey,
					dataStructureType, paramDef.getName());
			String userValue = getParameterAsString(subKey);
			DSParameter<?> paramInstance = DSParameterFactory
					.createParameter(paramDef.getParameterType()
							.cast(paramDef.parse(paramDef, userValue)));
			result.put(paramDef, paramInstance);
		}
		return result;
	}
}
