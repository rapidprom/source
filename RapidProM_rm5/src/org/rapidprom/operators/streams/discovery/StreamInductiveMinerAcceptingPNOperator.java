package org.rapidprom.operators.streams.discovery;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.readers.acceptingpetrinet.XSEventStreamToAcceptingPetriNetReader;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSReader;
import org.processmining.streaminductiveminer.parameters.StreamInductiveMinerParameters;
import org.processmining.streaminductiveminer.plugins.StreamInductiveMinerAcceptingPetriNetPlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.streams.XSReaderIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamToAcceptingPetriNetReaderIOObject;
import org.rapidprom.operators.streams.discovery.abstr.AbstractDFABasedMinerOperator;

import com.rapidminer.operator.OperatorDescription;

public class StreamInductiveMinerAcceptingPNOperator extends
		AbstractDFABasedMinerOperator<XSEvent, AcceptingPetriNet, StreamInductiveMinerParameters> {

	public StreamInductiveMinerAcceptingPNOperator(
			OperatorDescription description) {
		super(description);
	}

	@Override
	protected PluginContext getPluginContextForAlgorithm() {
		return ProMPluginContextManager.instance().getFutureResultAwareContext(
				StreamInductiveMinerAcceptingPetriNetPlugin.class);
	}

	@Override
	protected XSEventStreamToAcceptingPetriNetReader getAlgorithm(
			PluginContext context, XSEventStream stream,
			StreamInductiveMinerParameters parameters) {
		StreamInductiveMinerAcceptingPetriNetPlugin plugin = new StreamInductiveMinerAcceptingPetriNetPlugin();
		return plugin.apply(context, stream, parameters);
	}

	@Override
	protected StreamInductiveMinerParameters getAlgorithmParameterObject() {
		return new StreamInductiveMinerParameters();
	}

	@Override
	protected XSReaderIOObject<XSEvent, AcceptingPetriNet> getIOObject(
			XSReader<XSEvent, AcceptingPetriNet> algorithm,
			PluginContext context) {
		return new XSEventStreamToAcceptingPetriNetReaderIOObject(algorithm,
				context);
	}

}
