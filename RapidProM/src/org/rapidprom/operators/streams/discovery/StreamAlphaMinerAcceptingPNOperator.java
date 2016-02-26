package org.rapidprom.operators.streams.discovery;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSReader;
import org.processmining.streamalphaminer.parameters.StreamAlphaMinerParameters;
import org.processmining.streamalphaminer.plugins.StreamAlphaMinerAcepptingPetriNetPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.streams.XSReaderIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamToAcceptingPetriNetReaderIOObject;
import org.rapidprom.operators.streams.discovery.abstr.AbstractDFABasedMinerOperator;

import com.rapidminer.operator.OperatorDescription;

public class StreamAlphaMinerAcceptingPNOperator extends
		AbstractDFABasedMinerOperator<XSEvent, AcceptingPetriNet, StreamAlphaMinerParameters> {

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
	protected StreamAlphaMinerParameters getAlgorithmParameterObject() {
		return new StreamAlphaMinerParameters();
	}

	@Override
	protected XSReaderIOObject<XSEvent, AcceptingPetriNet> getIOObject(
			XSReader<XSEvent, AcceptingPetriNet> algorithm,
			PluginContext context) {
		return new XSEventStreamToAcceptingPetriNetReaderIOObject(algorithm,
				context);
	}

	@Override
	protected XSReader<XSEvent, AcceptingPetriNet> getAlgorithm(
			PluginContext context, XSEventStream stream,
			StreamAlphaMinerParameters parameters) {
		return StreamAlphaMinerAcepptingPetriNetPlugin.apply(context, stream,
				parameters);
	}
}
