package org.rapidprom.operators.streams.discovery;

import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.readers.acceptingpetrinet.XSEventStreamToAcceptingPetriNetReader;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.streamalphaminer.parameters.StreamAlphaMinerParameters;
import org.processmining.streamalphaminer.plugins.StreamAlphaMinerAcepptingPetriNetPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.streams.event.XSEventStreamToAcceptingPetriNetReaderIOObject;
import org.rapidprom.operators.streams.discovery.abstr.AbstractDFABasedMinerOperator;

import com.rapidminer.operator.OperatorDescription;

public class StreamAlphaMinerAcceptingPNOperator extends
		AbstractDFABasedMinerOperator<XSEventStreamToAcceptingPetriNetReader, XSEventStreamToAcceptingPetriNetReaderIOObject, StreamAlphaMinerParameters> {

	public StreamAlphaMinerAcceptingPNOperator(
			OperatorDescription description) {
		super(description);
	}

	@Override
	protected PluginContext getPluginContextForAlgorithm() {
		return ProMPluginContextManager.instance().getFutureResultAwareContext(
				StreamAlphaMinerAcepptingPetriNetPlugin.class);
	}

	@Override
	protected XSEventStreamToAcceptingPetriNetReader getAlgorithm(
			PluginContext context, XSEventStream stream,
			StreamAlphaMinerParameters parameters) {
		return StreamAlphaMinerAcepptingPetriNetPlugin.apply(context, stream,
				parameters);
	}

	@Override
	protected XSEventStreamToAcceptingPetriNetReaderIOObject getIOObject(
			XSEventStreamToAcceptingPetriNetReader algorithm,
			PluginContext context) {
		return new XSEventStreamToAcceptingPetriNetReaderIOObject(algorithm,
				context);
	}

	@Override
	protected StreamAlphaMinerParameters getAlgorithmParameterObject() {
		return new StreamAlphaMinerParameters();
	}

}
