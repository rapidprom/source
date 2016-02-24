package org.rapidprom.operators.streams.extract;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.core.interfaces.XSStaticXSEventStream;
import org.processmining.eventstream.readers.staticeventstream.parameters.XSEventStreamToXSStaticEventStreamParameters;
import org.processmining.eventstream.readers.staticeventstream.plugins.XSEventStreamToXSStaticEventStreamPlugin;
import org.processmining.stream.core.interfaces.XSAuthor;
import org.rapidprom.ioobjects.streams.XSAuthorIOObject;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;
import org.rapidprom.ioobjects.streams.event.XSStaticXSEventStreamIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;

public class EventStreamToStaticEventStream extends Operator {

	private static final String PARAMETER_KEY_NUM_PACKETS = "num_packets";
	private static final String PARAMETER_DESC_NUM_PACKETS = "Number of packets to capture into the static event stream.";
	private static final int PARAMETER_MIN_NUM_PACKETS = 0;
	private static final int PARAMETER_MAX_NUM_PACKETS = Integer.MAX_VALUE;
	private static final int PARAMETER_DEFAULT_NUM_PACKETS = 10000;

	private InputPort inputEventStream = getInputPorts().createPort("stream",
			XSEventStreamIOObject.class);

	//optional port -> no .class argument
	private InputPort inputAuthor = getInputPorts().createPort("author");

	private OutputPort outputStaticEventStream = getOutputPorts()
			.createPort("static stream");

	public EventStreamToStaticEventStream(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(outputStaticEventStream,
				XSStaticXSEventStreamIOObject.class));	
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "start do work Static Stream extractor");
		XSEventStreamToXSStaticEventStreamParameters parameters = new XSEventStreamToXSStaticEventStreamParameters();
		parameters.setTotalNumberOfEvents(
				getParameterAsInt(PARAMETER_KEY_NUM_PACKETS));
		XSStaticXSEventStream result;
		if (inputAuthor.getDataOrNull(XSAuthorIOObject.class) != null) {
			result = XSEventStreamToXSStaticEventStreamPlugin.runContextFree(
					(XSAuthor<XSEvent>) inputAuthor
							.getData(XSAuthorIOObject.class).getArtifact(),
					inputEventStream.getData(XSEventStreamIOObject.class)
							.getArtifact(),
					parameters);
		} else {
			result = XSEventStreamToXSStaticEventStreamPlugin.runContextFree(
					inputEventStream.getData(XSEventStreamIOObject.class)
							.getArtifact(),
					parameters);
		}

		outputStaticEventStream
				.deliver(new XSStaticXSEventStreamIOObject(result, null));
		logger.log(Level.INFO, "end do work Static Stream extractor");
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();
		ParameterType numPackets = new ParameterTypeInt(
				PARAMETER_KEY_NUM_PACKETS, PARAMETER_DESC_NUM_PACKETS,
				PARAMETER_MIN_NUM_PACKETS, PARAMETER_MAX_NUM_PACKETS,
				PARAMETER_DEFAULT_NUM_PACKETS, false);
		numPackets.setOptional(false);
		params.add(numPackets);
		return params;

	}

}
