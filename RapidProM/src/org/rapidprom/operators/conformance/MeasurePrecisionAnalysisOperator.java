package org.rapidprom.operators.conformance;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PNRepResultIOObject;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractDataReader.AttributeColumn;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

import javassist.tools.rmi.ObjectNotFoundException;

public class MeasurePrecisionAnalysisOperator extends Operator {

	private static final String PARAMETER_1 = "Consider traces with the same activity sequence as the same trace";
	private InputPort input = getInputPorts().createPort(
			"alignments (ProM PNRepResult)", PNRepResultIOObject.class);

	private OutputPort outputMetrics = getOutputPorts()
			.createPort("example set (Data Table)");

	//private ExampleSetMetaData metaData = null;

	private final String NAMECOL = "Name";
	private final String VALUECOL = "Value";

	public MeasurePrecisionAnalysisOperator(OperatorDescription description) {
		super(description);

//		this.metaData = new ExampleSetMetaData();
//		AttributeMetaData amd1 = new AttributeMetaData(NAMECOL,
//				Ontology.STRING);
//		amd1.setRole(AttributeColumn.REGULAR);
//		amd1.setNumberOfMissingValues(new MDInteger(0));
//		metaData.addAttribute(amd1);
//		AttributeMetaData amd2 = new AttributeMetaData(VALUECOL,
//				Ontology.NUMERICAL);
//		amd2.setRole(AttributeColumn.REGULAR);
//		amd2.setNumberOfMissingValues(new MDInteger(0));
//		metaData.addAttribute(amd2);
//		metaData.setNumberOfExamples(2);
//		getTransformer()
//				.addRule(new GenerateNewMDRule(outputMetrics, this.metaData));
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO,
				"Start: measure precision/generalization based on alignments");
		long time = System.currentTimeMillis();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		PNRepResultIOObject alignment = input
				.getData(PNRepResultIOObject.class);

		ExampleSet es = null;
		MemoryExampleTable table = null;
		List<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(AttributeFactory.createAttribute(this.NAMECOL,
				Ontology.STRING));
		attributes.add(AttributeFactory.createAttribute(this.VALUECOL,
				Ontology.NUMERICAL));
		table = new MemoryExampleTable(attributes);

		if (alignment.getArtifact() != null) {
			AlignmentPrecGen aligner = new AlignmentPrecGen();
			AlignmentPrecGenRes result = null;
			try {
				result = aligner.measureConformanceAssumingCorrectAlignment(
						pluginContext, alignment.getMapping(),
						alignment.getArtifact(),
						alignment.getPn().getArtifact(),
						alignment.getPn().getInitialMarking(),
						getParameterAsBoolean(PARAMETER_1));

				fillTableWithRow(table, "Precision", result.getPrecision(),
						attributes);
				fillTableWithRow(table, "Generalization",
						result.getGeneralization(), attributes);

			} catch (ObjectNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			fillTableWithRow(table, "Precision", Double.NaN, attributes);
			fillTableWithRow(table, "Generalization", Double.NaN, attributes);
		}
		es = table.createExampleSet();
		outputMetrics.deliver(es);

		logger.log(Level.INFO,
				"End: measure precision/generalization based on alignments ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeBoolean parameterType1 = new ParameterTypeBoolean(
				PARAMETER_1, PARAMETER_1, true);
		parameterTypes.add(parameterType1);

		return parameterTypes;
	}

	private void fillTableWithRow(MemoryExampleTable table, String name,
			Object value, List<Attribute> attributes) {
		// fill table
		DataRowFactory factory = new DataRowFactory(
				DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
		Object[] vals = new Object[2];
		vals[0] = name;
		vals[1] = value;
		// convert the list to array
		Attribute[] attribArray = new Attribute[attributes.size()];
		for (int i = 0; i < attributes.size(); i++) {
			attribArray[i] = attributes.get(i);
		}
		DataRow dataRow = factory.create(vals, attribArray);
		table.addDataRow(dataRow);
	}
}
