package com.rapidminer.operator.exportplugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjectrenderers.PetriNetIOObjectRenderer;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.LogService;

public class ExportToPNML_PetriNet extends Operator {
	
	public static final String PARAMETER_FILENAME = "filename";
	
	/** defining the ports */
	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputLog = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	
	/**
	 * The default constructor needed in exactly this signature
	 */
	public ExportToPNML_PetriNet(OperatorDescription description) {
		super(description);
	}
	
	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do work pnml export", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		// get the pn
		PetriNetIOObject pn = inputLog.getData(PetriNetIOObject.class);
		Petrinet pnProM = pn.getPn();
		// do the visualisation
		pn.setPluginContext(pluginContext);
		//PetriNetIOObjectRenderer.runVisualization(pn.getPn(), pluginContext);
		CallProm tp = new CallProm();
		List<Object> pars2 = new ArrayList<Object>();
		pars2.add(pnProM);
		Object[] runPlugin2 = tp.runPlugin(pluginContext, "14", "Visualize Petri net", pars2);
		if (pluginContext == null) {
			System.out.println("pluginContext is null");
		}
		else {
			System.out.println("pluginContext is not null");
		}
		// create the file
		File file = getParameterAsFile(PARAMETER_FILENAME);		
		List<Object> pars = new ArrayList<Object>();
		pars.add(pnProM);
		pars.add(file);
		Object[] runPlugin = tp.runPlugin(pluginContext, "4", "EPNML export (Petri net)", pars);
		logService.log("end do work pnml export", LogService.NOTE);
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterTypeFile parameterTypeFile = new ParameterTypeFile(
				PARAMETER_FILENAME, "File to open", "pnml", true, false);
		parameterTypes.add(parameterTypeFile);
		return parameterTypes;
	}

}
