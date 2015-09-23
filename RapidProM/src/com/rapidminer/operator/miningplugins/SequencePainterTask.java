//package com.rapidminer.operator.miningplugins;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.deckfour.xes.model.XLog;
//import org.processmining.framework.plugin.PluginContext;
//import org.processmining.models.graphbased.directed.petrinet.Petrinet;
//import org.processmining.models.semantics.petrinet.Marking;
//import org.processmining.plugins.graphviz.visualisation.DotPanel;
//import org.processmining.processcomparator.parameters.SequencePainterParameters;
//import org.rapidprom.prom.CallProm;
//
//import com.rapidminer.ioobjects.DotPanelIOObject;
//import com.rapidminer.ioobjects.ProMContextIOObject;
//import com.rapidminer.ioobjects.XLogIOObject;
//import com.rapidminer.operator.Operator;
//import com.rapidminer.operator.OperatorDescription;
//import com.rapidminer.operator.OperatorException;
//import com.rapidminer.operator.ports.InputPort;
//import com.rapidminer.operator.ports.OutputPort;
//import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
//import com.rapidminer.parameter.ParameterType;
//import com.rapidminer.parameter.ParameterTypeCategory;
//import com.rapidminer.parameter.ParameterTypeDouble;
//import com.rapidminer.parameter.ParameterTypeInt;
//import com.rapidminer.parameters.Parameter;
//import com.rapidminer.parameters.ParameterCategory;
//import com.rapidminer.parameters.ParameterDouble;
//import com.rapidminer.parameters.ParameterInteger;
//import com.rapidminer.tools.LogService;
//import com.rapidminer.util.ProMIOObjectList;
//import com.rapidminer.util.Utilities;
//
//public class SequencePainterTask extends Operator{
//	
//	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
//	private InputPort inputLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
//	private OutputPort output = getOutputPorts().createPort("model (DotPanel)");
//	
//	private List<Parameter> parameters;
//	public SequencePainterTask(OperatorDescription description) {
//		super(description);
//
//		getTransformer().addRule( new GenerateNewMDRule(output, DotPanelIOObject.class));
//	}
//	
//
//	@Override
//	public void doWork() throws OperatorException {
//		// get ProMContext
//		LogService logService = LogService.getGlobal();
//		logService.log("start do work Sequential Process Painter", LogService.NOTE);
//		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
//		PluginContext pluginContext = context.getPluginContext();
//		// get the log
//		XLogIOObject log = inputLog.getData(XLogIOObject.class);
//		XLog promLog = log.getData();
//		CallProm tp = new CallProm();
//		
//		
//		List<Object> pars = new ArrayList<Object>();
//		pars.add(promLog);
//		
//		SequencePainterParameters seqpar = (SequencePainterParameters) getConfiguration(parameters);
//		pars.add(seqpar);
//		
//		Object[] runPlugin = tp.runPlugin(pluginContext, "XX", "Sequential Process Painter", pars);
//		
//		DotPanelIOObject result = new DotPanelIOObject((DotPanel) runPlugin[0]);
//		result.setPluginContext(pluginContext);
//		// add to list so that afterwards it can be cleared if needed
//		
//		ProMIOObjectList instance = ProMIOObjectList.getInstance();
//		instance.addToList(result);
//		
//		
//		output.deliver(result);
//	
//		logService.log("end do work Sequential Process Painter", LogService.NOTE);
//
//	}
//	
//	public List<ParameterType> getParameterTypes() {
//		Utilities.loadRequiredClasses();
//		
//		this.parameters = new ArrayList<Parameter>();
//		List<ParameterType> parameterTypes = super.getParameterTypes();
//		
//		ParameterInteger parameter_charsRemoved = new ParameterInteger(8, 0, Integer.MAX_VALUE, 1, null, "Chars removed from activity name", "number of characters that are removed from the beggining of each activity name");
//		ParameterTypeInt parameterType_0 = new ParameterTypeInt(parameter_charsRemoved.getNameParameter(), parameter_charsRemoved.getDescriptionParameter(), parameter_charsRemoved.getMin(), parameter_charsRemoved.getMax(), parameter_charsRemoved.getDefaultValueParameter());
//		parameterTypes.add(parameterType_0);
//		parameters.add(parameter_charsRemoved);
//		
//		ParameterInteger parameter_activititesPerColumn = new ParameterInteger(10, 0, Integer.MAX_VALUE, 1, null, "Number of activities per column", "defines how many activities are fitted in a column before adding a new column");
//		ParameterTypeInt parameterType_1 = new ParameterTypeInt(parameter_activititesPerColumn.getNameParameter(), parameter_activititesPerColumn.getDescriptionParameter(), parameter_activititesPerColumn.getMin(), parameter_activititesPerColumn.getMax(), parameter_activititesPerColumn.getDefaultValueParameter());
//		parameterTypes.add(parameterType_1);
//		parameters.add(parameter_activititesPerColumn);
//		
//		ParameterInteger parameter_numberOfDeviations = new ParameterInteger(10, 0, Integer.MAX_VALUE, 1, null, "Number of deviations", "defines the number of deviations that are shown in the model using different colors (max 13)");
//		ParameterTypeInt parameterType_2 = new ParameterTypeInt(parameter_numberOfDeviations.getNameParameter(), parameter_numberOfDeviations.getDescriptionParameter(), parameter_numberOfDeviations.getMin(), parameter_numberOfDeviations.getMax(), parameter_numberOfDeviations.getDefaultValueParameter());
//		parameterTypes.add(parameterType_2);
//		parameters.add(parameter_numberOfDeviations);
//		
//		ParameterDouble parameter_Threshold = new ParameterDouble(0.05, 0, 1, 0.01, null, "Min Threshold for deviations", "Min Threshold for deviations");
//		ParameterTypeDouble parameterType_3 = new ParameterTypeDouble(parameter_Threshold.getNameParameter(), parameter_Threshold.getDescriptionParameter(), parameter_Threshold.getMin(), parameter_Threshold.getMax());
//		parameterTypes.add(parameterType_3);
//		parameters.add(parameter_Threshold);
//		
//		return parameterTypes;
//	}
//	
//	private Object getConfiguration (List<Parameter> pars)
//	{
//		try
//		{
//		
//		int charsRemoved = getParameterAsInt(pars.get(0).getNameParameter());
//		int activitiesPerColumn = getParameterAsInt(pars.get(1).getNameParameter());
//		int numberOfDeviations = getParameterAsInt(pars.get(2).getNameParameter());
//		
//		double threshold = getParameterAsDouble(pars.get(3).getNameParameter());
//		
//		return new SequencePainterParameters(charsRemoved, activitiesPerColumn, numberOfDeviations, threshold);
//		
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		return null;
//		
//	}
//}
