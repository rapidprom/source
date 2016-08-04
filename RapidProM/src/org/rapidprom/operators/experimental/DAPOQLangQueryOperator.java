package org.rapidprom.operators.experimental;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.processmining.database.metamodel.dapoql.DAPOQLRunner;
import org.processmining.database.metamodel.dapoql.DAPOQLVariable;
import org.processmining.database.metamodel.dapoql.QueryResult;
import org.processmining.openslex.metamodel.SLEXMMStorageMetaModelImpl;
import org.rapidprom.ioobjectrenderers.SLEXMMIOSubSetObjectVisualizationType;
import org.rapidprom.ioobjects.SLEXMMIOObject;
import org.rapidprom.ioobjects.SLEXMMSubSetIOObject;

import com.rapidminer.gui.properties.ConfigureParameterOptimizationDialogCreator;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeConfiguration;
import com.rapidminer.parameter.ParameterTypeInnerOperator;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeParameterValue;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeTupel;
import com.rapidminer.tools.LogService;

public class DAPOQLangQueryOperator extends Operator {

	public static final String PARAMETER_1 = "Query";

	private InputPort inputMM = getInputPorts().createPort(
			"OpenSLEX Meta Model", SLEXMMIOObject.class);

	private final InputPortExtender inputMMSet = new InputPortExtender(
			"OpenSLEX Meta Model SubSet", getInputPorts(), new MetaData(SLEXMMSubSetIOObject.class), 0);
			
	private OutputPort outputMMSet = getOutputPorts().createPort(
			"OpenSLEX Meta Model SubSet");
	
	private MetaData outputMD = new MetaData(SLEXMMSubSetIOObject.class);
	
	public DAPOQLangQueryOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(outputMMSet, SLEXMMSubSetIOObject.class));
		//outputMMSet.deliverMD(outputMD);
		inputMMSet.start();
	}
	
	@Override
	public void doWork() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: dapoq-lang query");
		long time = System.currentTimeMillis();
		MetaData md = inputMM.getMetaData();
		
		SLEXMMIOObject mmIO = inputMM.getData(SLEXMMIOObject.class);
		SLEXMMStorageMetaModelImpl slxmm = mmIO.getArtifact();
		
		Set<DAPOQLVariable> vars = new HashSet<>();

		List<SLEXMMSubSetIOObject> listInpt = inputMMSet.getData(SLEXMMSubSetIOObject.class, true);
		
		int i = 1;
		for (SLEXMMSubSetIOObject item : listInpt) {
			DAPOQLVariable v = new DAPOQLVariable("_v"+i, item.getType(), item.getMapResults());
			vars.add(v);
			i++;
		}
		
		String query = getParameterAsString(PARAMETER_1);

		boolean failed = true;
		QueryResult qr = null;
		DAPOQLRunner dapoqlr = new DAPOQLRunner();
		String msgFailure = "";
		try {
			qr = dapoqlr.executeQuery(slxmm, query, vars);
			failed = false;
		} catch (Exception e) {
			failed = true;
			e.printStackTrace();
			msgFailure = e.toString();
		}
		
		if (!failed) {
			SLEXMMSubSetIOObject slxmmSubSetIOObject = new SLEXMMSubSetIOObject(slxmm,qr.mapResult,qr.result,qr.type);
			slxmmSubSetIOObject
					.setVisualizationType(SLEXMMIOSubSetObjectVisualizationType.DEFAULT);
			outputMMSet.deliverMD(outputMD);
			outputMMSet.deliver(slxmmSubSetIOObject);
		} else {
			outputMMSet.deliverMD(outputMD);
			outputMMSet.deliver(null);
			throw new OperatorException(msgFailure);
		}
		logger.log(Level.INFO,
				"End: dapoq-lang query ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterType queryType = new ParameterTypeString(PARAMETER_1,PARAMETER_1,"",true);
		parameterTypes.add(queryType);
		ParameterType type =
				new ParameterTypeConfiguration(DAPOQLangDialogCreator.class, this);
        type.setExpert(false);
        parameterTypes.add(type);
		
		return parameterTypes;
	}
	
}
