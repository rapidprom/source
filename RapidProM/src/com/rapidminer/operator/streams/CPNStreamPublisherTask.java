package com.rapidminer.operator.streams;

import org.processmining.eventstream.authors.cpn.parameters.CPN2XSStreamParameters;
import org.processmining.eventstream.authors.cpn.plugins.CPNModelToXSEventStreamAuthorPlugin;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSPublisher;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;

import com.rapidminer.ioobjects.CPNModelIOObject;
import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.ioobjects.XSEventStreamIOObject;
import com.rapidminer.ioobjects.XSPublisherIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class CPNStreamPublisherTask extends Operator {

    // private InputPort inputContext =
    // getInputPorts().createPort("context (ProM Context)",
    // ProMContextIOObject.class);
    private InputPort inputCPNModel = getInputPorts().createPort(
	    "model (ProM CPN model)", CPNModelIOObject.class);

    private OutputPort outputPublisher = getOutputPorts().createPort(
	    "publisher (ProM)");
    private OutputPort outputStream = getOutputPorts().createPort(
	    "Event Stream (ProM)");

    public CPNStreamPublisherTask(OperatorDescription description) {
	super(description);

	getTransformer().addRule(
		new GenerateNewMDRule(outputPublisher,
			XSPublisherIOObject.class));
	getTransformer().addRule(
		new GenerateNewMDRule(outputStream, MarkingIOObject.class));
	// TODO Auto-generated constructor stub
    }

    public void doWork() throws OperatorException {

	LogService logService = LogService.getGlobal();
	logService.log("start do work Stream Generator", LogService.NOTE);

	// ProMContextIOObject context =
	// inputContext.getData(ProMContextIOObject.class);
	PluginContext pluginContext = ProMPluginContextManager.instance().getContext();

	CPN2XSStreamParameters parameters = new CPN2XSStreamParameters();

	Object[] result = CPNModelToXSEventStreamAuthorPlugin
		.cpnToXSEventStreamPlugin(
			pluginContext,
			inputCPNModel.getData(CPNModelIOObject.class).getData(),
			parameters);

	outputPublisher
		.deliver(new XSPublisherIOObject((XSPublisher) result[0]));
	outputStream.deliver(new XSEventStreamIOObject(
		(XSEventStream) result[1]));

	logService.log("end do work Stream Generator", LogService.NOTE);
    }

}
