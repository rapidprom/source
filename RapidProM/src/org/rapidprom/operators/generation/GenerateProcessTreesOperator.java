package org.rapidprom.operators.generation;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csv.CSVFileReferenceUnivocityImpl;
import org.processmining.ptandloggenerator.models.NewickTree;
import org.processmining.ptandloggenerator.models.NewickTreeCollection;
import org.processmining.ptandloggenerator.plugins.GenerateTree;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.NewickTreeIOObject;

import com.rapidminer.operator.IOObjectCollection;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;

public class GenerateProcessTreesOperator extends Operator {

	protected final InputPort inputfile = getInputPorts().createPort("file",
			FileObject.class);
	protected final OutputPort outputPort = getOutputPorts()
			.createPort("newick tree collection");

	public GenerateProcessTreesOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputPort, IOObjectCollection.class));

	}

	public void doWork() throws OperatorException {
		IOObjectCollection<NewickTreeIOObject> result = new IOObjectCollection<NewickTreeIOObject>();
		PluginContext context = ProMPluginContextManager.instance()
				.getContext();

		FileObject file = inputfile.getData(FileObject.class);
		CSVFile csv = new CSVFileReferenceUnivocityImpl(
				file.getFile().toPath());

		GenerateTree generator = new GenerateTree();
		NewickTreeCollection collection = generator.run(context, csv);

		for (NewickTree tree : collection.getNewickTreeList())
			result.add(new NewickTreeIOObject(tree, context));

		outputPort.deliver(result);
	}

}
