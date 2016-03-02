package org.rapidprom.operators.generation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.algorithms.CSVIterator;
import org.processmining.ptandloggenerator.models.NewickTree;
import org.processmining.ptandloggenerator.models.NewickTreeCollection;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.experimental.NewickTreeIOObject;

import com.rapidminer.operator.IOObjectCollection;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;

public class GenerateNewickTreeOperator extends Operator {

	private InputPort input = getInputPorts().createPort("configuration file",
			FileObject.class);

	private OutputPort output = getOutputPorts()
			.createPort("newick tree collection");

	public GenerateNewickTreeOperator(OperatorDescription description) {
		super(description);

		getTransformer().addRule(new GenerateNewMDRule(output,
				IOObjectCollection.class));
	}

	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: generating newick trees");
		long time = System.currentTimeMillis();
		
		PluginContext pluginContext = ProMPluginContextManager.instance().getContext();
		
		CSVIterator iterator = new CSVIterator(input.getData(FileObject.class).getFile().getPath());
		
		IOObjectCollection<NewickTreeIOObject> result = new IOObjectCollection<NewickTreeIOObject>();
		NewickTreeCollection collection = iterator.getNewickTrees();
		for(NewickTree tree : collection.getNewickTreeList())
			result.add(new NewickTreeIOObject(tree,pluginContext));
		
		output.deliver(result);

		logger.log(Level.INFO, "End: generating newick trees ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}
}
