package com.rapidminer.operator.importplugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;

public class ReadPetrinetTask extends Operator {
	
public static final String PARAMETER_FILENAME = "filename";
	
	/** defining the ports */
	// I need to have a context, perhaps make this more generic
	private InputPort input = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private OutputPort output = getOutputPorts().createPort("model (ProM Petri Net)");
	private OutputPort outputMarking = getOutputPorts().createPort("marking (ProM marking)");
	/**
	 * The default constructor needed in exactly this signature
	 */
	public ReadPetrinetTask(OperatorDescription description) {
		super(description);
		
		/** Adding a rule for meta data transformation: XLog will be passed through */
		getTransformer().addRule( new GenerateNewMDRule(output, PetriNetIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputMarking, MarkingIOObject.class));
	}
	
	/**
	 * @throws UserError 
	 * 
	 */
	protected void checkMetaData() throws UserError {
		try {
			File file = getParameterAsFile(PARAMETER_FILENAME);
				
			// check if file exists and is readable
			if (!file.exists()) {
				throw new UserError(this, "301", file);
			} else if (!file.canRead()) {
				throw new UserError(this, "302", file, "");
			}
		} catch (UndefinedParameterError e) {
			// handled by parameter checks in super class
		}
	}
	
	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do work read petrinet task", LogService.NOTE);
		ProMContextIOObject context = input.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		
		// run the plugin for loading the log
		File file = getParameterAsFile(PARAMETER_FILENAME);
		
		// check if file exists and is readable
		if (!file.exists()) {
			throw new UserError(this, "301", file);
		} else if (!file.canRead()) {
			throw new UserError(this, "302", file, "");
		}
		
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(file);
		Petrinet petrinet = null;
		Marking marking = new Marking();
		Marking marking2 = new Marking();
		CallProm tp = new CallProm ();
		try {
			Object[] objects = tp.runPlugin(pluginContext, "000", "Import Petri net from PNML file", parameters);
			petrinet = (Petrinet) (objects)[0];
			marking = (Marking) (objects)[1];
			
			/*for(Place in :  petrinet.getPlaces())
			{
				if (in.getLabel().matches("source"))
					{
						marking1.add(in);
						System.out.println("initial Marking added: " + in.getLabel());
					}
				else if(in.getLabel().matches("sink"))
				{
					marking2.add(in);
					System.out.println("final Marking added: " + in.getLabel());
				}
			}
			pluginContext.addConnection(new InitialMarkingConnection(petrinet, marking1));
			pluginContext.addConnection(new FinalMarkingConnection(petrinet, marking2));*/
			//pluginContext.addConnection(new InitialMarkingConnection(petrinet, marking));
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		
		/*
		// These are the final markings from somewhere
		Marking finalMarking = new Marking();
		for(Place in :  petrinet.getPlaces())
		{
			if (in.getLabel().matches("source") || in.getLabel().matches("sink"))
				finalMarking.add(in);
		}

		//FinalMarkingConnection connection = FinalMarkingConnection.markedNetConnectionFactory(pluginContext, petrinet, finalMarking);
		pluginContext.addConnection(new InitialMarkingConnection(petrinet,finalMarking));
		//ProvidedObjectHelper.publish(context, "Final Marking for XXX", finalMarking, Marking.class, false);

		MarkingIOObject marking = new MarkingIOObject(finalMarking);
		marking.setPluginContext(pluginContext);
		*/
		// end plugin
		
		
		MarkingIOObject  markingIOObject = new MarkingIOObject(marking);
		markingIOObject.setPluginContext(pluginContext);
		PetriNetIOObject petriNetIOObject = new PetriNetIOObject(petrinet);
		petriNetIOObject.setPluginContext(pluginContext);
		
		//pluginContext.addConnection(new InitialMarkingConnection(petrinet,marking.getData()));
		
		
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(petriNetIOObject);
		
		output.deliver(petriNetIOObject);
		outputMarking.deliver(markingIOObject);
		
		// add to list so that afterwards it can be cleared if needed
		
		logService.log("end do work read petrinet task", LogService.NOTE);
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeFile parameterTypeFile = new ParameterTypeFile(
				PARAMETER_FILENAME, "File to open", null, true, false);
		parameterTypes.add(parameterTypeFile);
		return parameterTypes;
	}
}
