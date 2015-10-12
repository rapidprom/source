package org.rapidprom.operator.abstr;

import java.util.List;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersEKS;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMin;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;

public abstract class AbstractInductiveMinerOperator extends AbstractRapidProMDiscoveryOperator {
	
	public AbstractInductiveMinerOperator(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	private static final String PARAMETER_1 = "Noise Threshold",
			PARAMETER_2 = "IM Variation";

	private static final String IM = "Inductive Miner",
			IMi = "Inductive Miner - Infrequent",
			IMin = "Inductive Miner - Incompleteness",
			IMeks = "Inductive Miner - exhaustive K-successor";

	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeCategory parameter1 = new ParameterTypeCategory(
				PARAMETER_1, PARAMETER_1,
				new String[] { IM, IMi, IMin, IMeks }, 1);
		parameterTypes.add(parameter1);

		ParameterTypeDouble parameter2 = new ParameterTypeDouble(PARAMETER_2,
				PARAMETER_2, 0, 1, 0.2);
		parameterTypes.add(parameter2);

		return parameterTypes;
	}

	protected MiningParameters getConfiguration() {
		MiningParameters miningParameters = null;
		try 
		{
			if(getParameterAsString(PARAMETER_1).matches(IM))
				miningParameters = new MiningParametersIM();
			else if(getParameterAsString(PARAMETER_1).matches(IMi))
				miningParameters = new MiningParametersIMi();
			else if(getParameterAsString(PARAMETER_1).matches(IMin))
				miningParameters = new MiningParametersIMin();
			else if(getParameterAsString(PARAMETER_1).matches(IMeks))
				miningParameters = new MiningParametersEKS();
			
			miningParameters.setNoiseThreshold((float) getParameterAsDouble(PARAMETER_2));
			miningParameters.setClassifier(getXEventClassifier());
		} 
		catch (UndefinedParameterError e) 
		{
			e.printStackTrace();
		}
		return miningParameters;
	}
}
