package org.rapidprom.operators.streams.discovery;

import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.readers.processtree.XSEventStreamToProcessTreeReader;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.streaminductiveminer.parameters.StreamInductiveMinerParameters;
import org.processmining.streaminductiveminer.plugins.StreamInductiveMinerProcessTreePlugin;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.streams.event.XSEventStreamToProcessTreePetriNetReaderIOObject;
import org.rapidprom.operators.streams.discovery.abstr.AbstractStreamInductiveMinerOperator;

import com.rapidminer.operator.OperatorDescription;

public class StreamInductiveMinerProcessTreeOperator extends
		AbstractStreamInductiveMinerOperator<XSEventStreamToProcessTreeReader, XSEventStreamToProcessTreePetriNetReaderIOObject> {

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
	protected PluginContext getPluginContextForISM() {
		return ProMPluginContextManager.instance().getFutureResultAwareContext(
				StreamInductiveMinerProcessTreePlugin.class);
	}

	@Override
	protected XSEventStreamToProcessTreePetriNetReaderIOObject getIOObject(
			XSEventStreamToProcessTreeReader algorithm, PluginContext context) {
		return new XSEventStreamToProcessTreePetriNetReaderIOObject(algorithm,
				context);
	}

}
