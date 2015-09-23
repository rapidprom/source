package com.rapidminer.operator.importplugins;

import java.util.*;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;
import com.rapidminer.parameter.*;
import com.rapidminer.parameters.*;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.ioobjects.ProMContextIOObject;

import java.io.File;
import java.lang.String;

import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.PetriNetWithDataIOObject;
import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.ioobjectrenderers.PetriNetWithDataIOObjectRenderer;
import com.rapidminer.ioobjectrenderers.MarkingIOObjectRenderer;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.prom.CallProm;

public class ImportDataPetriNetfromPNMLfileTask extends Operator {

	private List<Parameter> parametersImportDataPetriNetfromPNMLfile = null;
	
	public static final String PARAMETER_FILENAME = "filename";

	private InputPort inputContext = getInputPorts().createPort("prom context", ProMContextIOObject.class);
	private OutputPort outputPetriNetWithData = getOutputPorts().createPort("prom PetriNetWithData");
	private OutputPort outputMarking = getOutputPorts().createPort("prom Marking");

	public ImportDataPetriNetfromPNMLfileTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPetriNetWithData, PetriNetWithDataIOObject.class));
		getTransformer().addRule( new GenerateNewMDRule(outputMarking, MarkingIOObject.class));
	}
	
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

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Import Data Petri Net from PNML file", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		
		// run the plugin for loading the log
		File file = getParameterAsFile(PARAMETER_FILENAME);
		
		// check if file exists and is readable
		if (!file.exists()) {
			throw new UserError(this, "301", file);
		} else if (!file.canRead()) {
			throw new UserError(this, "302", file, "");
		}
		
		List<Object> pars = new ArrayList<Object>();
		pars.add(file);
		
		CallProm cp = new CallProm();
		Object[] runPlugin = cp.runPlugin(pluginContext, "XX", "Import Data Petri Net from PNML file", pars);
		
		PetriNetWithDataIOObject petriNetWithDataIOObject = new PetriNetWithDataIOObject((PetriNetWithData) runPlugin[0]);
		petriNetWithDataIOObject.setPluginContext(pluginContext);
		outputPetriNetWithData.deliver(petriNetWithDataIOObject);
		
		MarkingIOObject markingIOObject = new MarkingIOObject((Marking) runPlugin[1]);
		markingIOObject.setPluginContext(pluginContext);
		outputMarking.deliver(markingIOObject);
		logService.log("end do work Import Data Petri Net from PNML file", LogService.NOTE);
	}

	public List<ParameterType> getParameterTypes() {
		
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeFile parameterTypeFile = new ParameterTypeFile(
				PARAMETER_FILENAME, "File to open", null, true, false);
		parameterTypes.add(parameterTypeFile);
		return parameterTypes;
	}

}
