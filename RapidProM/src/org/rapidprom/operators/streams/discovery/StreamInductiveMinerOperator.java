package org.rapidprom.operators.streams.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.processmining.eventstream.core.interfaces.XSEventSignature;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.readers.acceptingpetrinet.XSEventStreamToAcceptingPetriNetReader;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.stream.models.streamdatastore.factories.StreamBasedDataStoreFactory;
import org.processmining.stream.models.streamdatastore.interfaces.StreamBasedDataStore;
import org.processmining.stream.models.streamdatastore.types.StreamBasedDataStoreType;
import org.processmining.streaminductiveminer.parameters.StreamInductiveMinerParameters;
import org.processmining.streaminductiveminer.plugins.StreamInductiveMinerPlugin;
import org.processmining.streaminductiveminer.utils.StreamInductiveMinerUtils;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.streams.XSEventStreamIOObject;
import org.rapidprom.ioobjects.streams.XSEventStreamToAcceptingPetriNetReaderIOObject;
import org.rapidprom.util.ObjectUtils;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualStringCondition;

public class StreamInductiveMinerOperator extends Operator {

	private InputPort streamInputPort = getInputPorts()
			.createPort("event stream", XSEventStreamIOObject.class);

	private OutputPort readerOutputPort = getOutputPorts().createPort("reader");

	protected static final String PARAMETER_KEY_CASE_IDENTIFIER = "case_identifier";
	protected static final String PARAMETER_DESC_CASE_IDENTIFIER = "Defines what key to use within the data packet to identify a case.";
	protected static final String PARAMETER_DEFAULT_CASE_IDENTIFIER = XSEventSignature.TRACE
			.toString();

	protected static final String PARAMETER_KEY_EVENT_IDENTIFIER = "event_identifier";
	protected static final String PARAMETER_DESC_EVENT_IDENTIFIER = "Defines what key to use within the data packet to identify an event.";
	protected static final String PARAMETER_DEFAULT_EVENT_IDENTIFIER = XConceptExtension.KEY_NAME;

	protected static final String PARAMETER_KEY_REFRESH_RATE = "refresh_rate";
	protected static final String PARAMETER_DESC_REFRESH_RATE = "Defines at what intervals (in terms of messages received) a new model should be queried.";
	protected static final int PARAMETER_DEFAULT_REFRESH_RATE = 100000;

	protected static final String PARAMETER_KEY_CASE_ACTIVITY_STORE = "case_activity_store";
	protected static final String PARAMETER_DESC_CASE_ACTIVITY_STORE = "Defines what stream-based data store to use for capturing CASE X ACTIVITY information.";
	protected static final StreamBasedDataStoreType[] PARAMETER_OPTIONS_CASE_ACTIVITY_STORE = StreamInductiveMinerUtils.streamBasedDataStoresAllowedForCaseActivityPairs
			.toArray(
					new StreamBasedDataStoreType[StreamInductiveMinerUtils.streamBasedDataStoresAllowedForCaseActivityPairs
							.size()]);

	protected static final String PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE = "activity_activity_store";
	protected static final String PARAMETER_DESC_ACTIVITY_ACTIVITY_STORE = "Defines what stream-based data store to use for capturing ACITIVTY X ACTIVITY information.";
	protected static final StreamBasedDataStoreType[] PARAMETER_OPTIONS_ACTIVITY_ACTIVITY_STORE = StreamInductiveMinerUtils.streamBasedDataStoresAllowedForActivityActivityPairs
			.toArray(
					new StreamBasedDataStoreType[StreamInductiveMinerUtils.streamBasedDataStoresAllowedForActivityActivityPairs
							.size()]);

	public StreamInductiveMinerOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(readerOutputPort,
				XSEventStreamToAcceptingPetriNetReaderIOObject.class));
	}

	@Override
	public void doWork() throws UserError {
		XSEventStream eventStream = streamInputPort
				.getData(XSEventStreamIOObject.class).getArtifact();
		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(StreamInductiveMinerPlugin.class);
		StreamInductiveMinerParameters params = new StreamInductiveMinerParameters();
		params.setCaseIdentifier(
				getParameterAsString(PARAMETER_KEY_CASE_IDENTIFIER));
		params.setEventIdentifier(
				getParameterAsString(PARAMETER_KEY_EVENT_IDENTIFIER));
		params.setRefreshRate(getParameterAsInt(PARAMETER_KEY_REFRESH_RATE));
		params = setCaseActivityStore(params);
		params = setActivityActivityStore(params);
		XSEventStreamToAcceptingPetriNetReader reader = StreamInductiveMinerPlugin
				.applyStreamInductiveMiner(context, eventStream, params);
		reader.start();
		readerOutputPort.deliver(
				new XSEventStreamToAcceptingPetriNetReaderIOObject(reader,
						context));

	}

	protected StreamInductiveMinerParameters setActivityActivityStore(
			StreamInductiveMinerParameters params)
					throws UndefinedParameterError {
		StreamBasedDataStoreType caseActivityStoreType = PARAMETER_OPTIONS_ACTIVITY_ACTIVITY_STORE[getParameterAsInt(
				PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE)];
		StreamBasedDataStore<Pair<XEventClass, XEventClass>> caseActivityStore = StreamBasedDataStoreFactory
				.createDataStoreBasedOnTypeAndParameters(caseActivityStoreType,
						getStoreParameters(
								PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE,
								caseActivityStoreType));
		params.setActivtyActivityPairStore(caseActivityStore);
		return params;
	}

	protected StreamInductiveMinerParameters setCaseActivityStore(
			StreamInductiveMinerParameters params)
					throws UndefinedParameterError {
		StreamBasedDataStoreType caseActivityStoreType = PARAMETER_OPTIONS_CASE_ACTIVITY_STORE[getParameterAsInt(
				PARAMETER_KEY_CASE_ACTIVITY_STORE)];
		StreamBasedDataStore<Pair<String, XEventClass>> caseActivityStore = StreamBasedDataStoreFactory
				.createDataStoreBasedOnTypeAndParameters(caseActivityStoreType,
						getStoreParameters(PARAMETER_KEY_CASE_ACTIVITY_STORE,
								caseActivityStoreType));
		params.setCaseActivityPairStore(caseActivityStore);
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

		params = addStreamBasedDataStoreDependencyConditions(
				PARAMETER_KEY_CASE_ACTIVITY_STORE,
				PARAMETER_OPTIONS_CASE_ACTIVITY_STORE, params);

		params.add(
				new ParameterTypeCategory(PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE,
						PARAMETER_DESC_ACTIVITY_ACTIVITY_STORE,
						ObjectUtils.toString(
								PARAMETER_OPTIONS_ACTIVITY_ACTIVITY_STORE),
						0, false));

		params = addStreamBasedDataStoreDependencyConditions(
				PARAMETER_KEY_ACTIVITY_ACTIVITY_STORE,
				PARAMETER_OPTIONS_ACTIVITY_ACTIVITY_STORE, params);

		return params;
	}

	protected List<ParameterType> addStreamBasedDataStoreDependencyConditions(
			String parameterTypeKey, StreamBasedDataStoreType[] options,
			List<ParameterType> params) {
		for (StreamBasedDataStoreType sbdst : options) {
			for (Map.Entry<String, Double> dsParam : sbdst
					.getDefaultParameters().entrySet()) {
				// TODO: We should add descriptions in the
				// StreamBasedDataStoreType enums
				ParameterType param = new ParameterTypeDouble(
						getSubKey(parameterTypeKey, sbdst, dsParam.getKey()),
						"TODO", Double.MIN_VALUE, Double.MAX_VALUE,
						dsParam.getValue(), false);
				param.setOptional(true);
				param.registerDependencyCondition(new EqualStringCondition(this,
						parameterTypeKey, true, sbdst.toString()));
				params.add(param);
			}
		}
		return params;
	}

	protected String getSubKey(String parentParameterTypeKey,
			StreamBasedDataStoreType sbdst, String param) {
		return sbdst.toString().toLowerCase() + "_" + parentParameterTypeKey
				+ "_" + param;
	}

	protected Map<String, Double> getStoreParameters(String parameterTypeKey,
			StreamBasedDataStoreType dataStore) throws UndefinedParameterError {
		Map<String, Double> result = new HashMap<String, Double>();
		// TODO: update stream package, let the datastore return the keyset of
		// the default params.
		for (Map.Entry<String, Double> paramOption : dataStore
				.getDefaultParameters().entrySet()) {
			result.put(paramOption.getKey(),
					getParameterAsDouble(getSubKey(parameterTypeKey, dataStore,
							paramOption.getKey())));
		}
		return result;
	}
}
