package org.rapidprom.operators.logmanipulation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.log.ReSortLog;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.tools.LogService;

public class TimestampSortOperator extends Operator {

	private InputPort inputLog = getInputPorts().createPort(
			"event log (ProM Event Log)", XLogIOObject.class);
	private OutputPort outputLog = getOutputPorts().createPort(
			"event log (ProM Event Log)");

	public TimestampSortOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(
				new GenerateNewMDRule(outputLog, XLogIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: sort by timestamp");
		long time = System.currentTimeMillis();
		
		MetaData md = inputLog.getMetaData();

		XLogIOObject log = inputLog.getData(XLogIOObject.class);
		XLog resultLog = ReSortLog.removeEdgePoints(log.getPluginContext(),
				log.getArtifact());
		XLogIOObject result = new XLogIOObject(resultLog,
				log.getPluginContext());
		
		outputLog.deliverMD(md);
		outputLog.deliver(result);

		logger.log(Level.INFO,
				"End: sort by timestamp ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");

	}
}
