package org.rapidprom.operators.streams.discovery;

import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.readers.processtree.XSEventStreamToProcessTreeReader;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.processtree.ProcessTree;
import org.processmining.stream.core.interfaces.XSReader;
import org.processmining.streaminductiveminer.parameters.StreamInductiveMinerParameters;
import org.processmining.streaminductiveminer.plugins.StreamInductiveMinerProcessTreePlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.streams.XSReaderIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamToProcessTreePetriNetReaderIOObject;
import org.rapidprom.operators.streams.discovery.abstr.AbstractDFABasedMinerOperator;

import com.rapidminer.operator.OperatorDescription;

public class StreamInductiveMinerProcessTreeOperator extends
		AbstractDFABasedMinerOperator<XSEvent, ProcessTree, StreamInductiveMinerParameters> {

	public StreamInductiveMinerProcessTreeOperator(
			OperatorDescription description) {
		super(description);
	}

	@Override
	protected XSEventStreamToProcessTreeReader getAlgorithm(
			PluginContext context, XSEventStream stream,
			StreamInductiveMinerParameters parameters) {
		StreamInductiveMinerProcessTreePlugin plugin = new StreamInductiveMinerProcessTreePlugin();
		return plugin.apply(context, stream, parameters);
	}

	@Override
	protected PluginContext getPluginContextForAlgorithm() {
		return ProMPluginContextManager.instance().getFutureResultAwareContext(
				StreamInductiveMinerProcessTreePlugin.class);
	}

	@Override
	protected StreamInductiveMinerParameters getAlgorithmParameterObject() {
		return new StreamInductiveMinerParameters();
	}

	@Override
	protected XSReaderIOObject<XSEvent, ProcessTree> getIOObject(
			XSReader<XSEvent, ProcessTree> algorithm, PluginContext context) {
		return new XSEventStreamToProcessTreePetriNetReaderIOObject(algorithm,
				context);
	}

}
