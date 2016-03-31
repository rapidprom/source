package org.rapidprom.operators.logmanipulation;

import java.util.List;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;

public class SplitXLogOperator extends Operator {

	public static final String PARAMETER_1_KEY = "split ratio";

	private final InputPort inputXLog = getInputPorts().createPort("event log",
			XLogIOObject.class);

	private OutputPort outputLog1 = getOutputPorts()
			.createPort("event log 1");

	private OutputPort outputLog2 = getOutputPorts()
			.createPort("event log 2");

	public SplitXLogOperator(OperatorDescription description) {
		super(description);
		getTransformer()
				.addRule(new GenerateNewMDRule(outputLog1, XLogIOObject.class));
		getTransformer()
				.addRule(new GenerateNewMDRule(outputLog2, XLogIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		
		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();
		XFactory factory = new XFactoryNaiveImpl();
		
		XLog original = inputXLog.getData(XLogIOObject.class).getArtifact();
		
		XLog log1 = factory.createLog(original.getAttributes());
		log1.getClassifiers().addAll(original.getClassifiers());
		XLog log2 = factory.createLog(original.getAttributes());
		log2.getClassifiers().addAll(original.getClassifiers());

		int split = (int) (original.size() * getParameterAsDouble(PARAMETER_1_KEY));
		for (int i = 0; i < original.size(); i++) {
			XTrace newTrace = factory
					.createTrace(original.get(i).getAttributes());
			newTrace.addAll(original.get(i));

			if (i < split)
				log1.add(newTrace);
			else
				log2.add(newTrace);
		}
		
		outputLog1.deliver(new XLogIOObject(log1,pluginContext));
		outputLog2.deliver(new XLogIOObject(log2,pluginContext));
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_1_KEY,
				PARAMETER_1_KEY, 0, 1, 0.5, false);
		types.add(type);
		return types;
	}

}
