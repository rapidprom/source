package org.rapidprom.operators.generation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.plugins.NewickTreeToProcessTreeConverter;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.ProcessTreeIOObject;
import org.rapidprom.ioobjects.experimental.NewickTreeIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class ConvertNewickTreeToProcessTree extends Operator {

	private InputPort input = getInputPorts().createPort("newick tree",
			NewickTreeIOObject.class);

	private OutputPort output = getOutputPorts().createPort("process tree");

	public ConvertNewickTreeToProcessTree(OperatorDescription description) {
		super(description);

		getTransformer().addRule(
				new GenerateNewMDRule(output, ProcessTreeIOObject.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: converting newick tree to process tree");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		NewickTreeToProcessTreeConverter converter = new NewickTreeToProcessTreeConverter();

		output.deliver(
				new ProcessTreeIOObject(
						converter
								.run(pluginContext,
										input.getData(NewickTreeIOObject.class)
												.getArtifact()),
						pluginContext));

		logger.log(Level.INFO, "End: converting newick tree to process tree ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

}
