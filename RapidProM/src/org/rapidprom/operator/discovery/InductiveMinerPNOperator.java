package org.rapidprom.operator.discovery;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.parameter.*;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersEKS;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMi;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMin;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.operator.abstr.AbstractRapidProMDiscoveryOperator;

public class InductiveMinerPNOperator extends AbstractRapidProMDiscoveryOperator {
	
	private static final String PARAMETER_1 = "Noise Threshold",
			PARAMETER_2 = "IM Variation";

	private static final String IM = "Inductive Miner",
			IMi = "Inductive Miner - Infrequent",
			IMin = "Inductive Miner - Incompleteness",
			IMeks = "Inductive Miner - exhaustive K-successor";

	private OutputPort outputPetrinet = getOutputPorts().createPort("model (ProM Petri Net)");

	public InductiveMinerPNOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPetrinet, PetriNetIOObject.class));
	}

	public void doWork() throws OperatorException {
		
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: inductive miner");
		long time = System.currentTimeMillis();
		
		PluginContext pluginContext = ProMPluginContextManager.instance().getContext();		
		MiningParameters param = getConfiguration();
		
		Object[] result = IMPetriNet.minePetriNet(pluginContext, getXLog(), param);
		
		PetriNetIOObject petrinet = new PetriNetIOObject((Petrinet) result[0]);
		petrinet.setPluginContext(pluginContext);
		
		outputPetrinet.deliver(petrinet);		
		logger.log(Level.INFO, "End: inductive miner ("
				+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

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

	private MiningParameters getConfiguration() {
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
