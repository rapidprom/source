package org.rapidprom.operators.experimental;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.videolectureanalysis.models.FitnessWrapper;
import org.processmining.videolectureanalysis.plugins.FitnessCalculator;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
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
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

public class FitnessCalculatorOperator extends Operator {

	private InputPort inputLog = getInputPorts()
			.createPort("event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort output = getOutputPorts()
			.createPort("example set (Data Table)");
	private ExampleSetMetaData metaData = null;

	public static final String COL_1 = "Student", COL_2 = "Fitness";

	public FitnessCalculatorOperator(OperatorDescription description) {
		super(description);

		this.metaData = new ExampleSetMetaData();
		AttributeMetaData amd1 = new AttributeMetaData(COL_1, Ontology.STRING);
		amd1.setRole(AttributeColumn.REGULAR);
		amd1.setNumberOfMissingValues(new MDInteger(0));
		metaData.addAttribute(amd1);

		AttributeMetaData amd2 = new AttributeMetaData(COL_2,
				Ontology.NUMERICAL);
		amd2.setRole(AttributeColumn.REGULAR);
		amd2.setNumberOfMissingValues(new MDInteger(0));
		metaData.addAttribute(amd2);

		getTransformer().addRule(new GenerateNewMDRule(output, this.metaData));
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: student fitness calculation");
		long time = System.currentTimeMillis();

		XLog xLog = cloneXLog(inputLog.getData(XLogIOObject.class).getArtifact());

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		FitnessCalculator calculator = new FitnessCalculator();

		ExampleSet converted_result = convertToTable(
				calculator.run(pluginContext, xLog));
		output.deliver(converted_result);

		logger.log(Level.INFO, "End: student fitness calculation ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	private ExampleSet convertToTable(FitnessWrapper input) {

		DataRowFactory factory = new DataRowFactory(
				DataRowFactory.TYPE_DOUBLE_ARRAY, '.');

		MemoryExampleTable table = null;
		List<Attribute> attributes = new LinkedList<Attribute>();
		attributes
				.add(AttributeFactory.createAttribute(COL_1, Ontology.STRING));
		attributes.add(
				AttributeFactory.createAttribute(COL_2, Ontology.NUMERICAL));
		table = new MemoryExampleTable(attributes);

		Attribute[] attribArray = new Attribute[attributes.size()];
		for (int i = 0; i < attributes.size(); i++) {
			attribArray[i] = attributes.get(i);
		}

		for (String s : input.getMap().keySet()) {

			Object[] vals = new Object[2];
			vals[0] = s;
			vals[1] = input.getMap().get(s);

			table.addDataRow(factory.create(vals, attribArray));
		}

		return table.createExampleSet();
	}

	public static XLog cloneXLog(XLog input) {
		XFactory factory = new XFactoryNaiveImpl();
		XLog newLog = factory
				.createLog(input.getAttributes());
		for (XTrace t : input) {
			XTrace newTrace = factory
					.createTrace(t.getAttributes());
			newTrace.addAll(t);
			newLog.add(newTrace);
		}
		newLog.getClassifiers().addAll(input.getClassifiers());
		return newLog;

	}

}
