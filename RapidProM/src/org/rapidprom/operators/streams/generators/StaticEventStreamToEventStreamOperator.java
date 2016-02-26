package org.rapidprom.operators.streams.generators;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.eventstream.authors.staticeventstream.plugins.XSStaticXSEventStreamToXSEventStreamPlugin;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSAuthor;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.streams.XSAuthorIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;
import org.rapidprom.ioobjects.streams.event.XSStaticXSEventStreamIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class StaticEventStreamToEventStreamOperator extends Operator {

	private InputPort inputStaticStream = getInputPorts()
			.createPort("static stream", XSStaticXSEventStreamIOObject.class);
	private OutputPort outputAuthor = getOutputPorts().createPort("generator");
	private OutputPort outputStream = getOutputPorts().createPort("stream");

	public StaticEventStreamToEventStreamOperator(
			OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputAuthor, XSAuthorIOObject.class));
		getTransformer().addRule(new GenerateNewMDRule(outputStream,
				XSEventStreamIOObject.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "start do work Stream Generator");
		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(
						XSStaticXSEventStreamToXSEventStreamPlugin.class);
		Object[] authStream = XSStaticXSEventStreamToXSEventStreamPlugin
				.apply(context,
						inputStaticStream
								.getData(XSStaticXSEventStreamIOObject.class)
								.getArtifact());

		outputAuthor.deliver(new XSAuthorIOObject<XSEvent>(
				(XSAuthor<XSEvent>) authStream[0], context));
		outputStream.deliver(new XSEventStreamIOObject(
				(XSEventStream) authStream[1], context));
		logger.log(Level.INFO, "end do work Stream Generator");
	}

}
