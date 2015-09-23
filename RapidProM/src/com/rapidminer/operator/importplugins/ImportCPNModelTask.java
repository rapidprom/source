package com.rapidminer.operator.importplugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.cpnet.ColouredPetriNet;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.CPNModelIOObject;
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
import com.rapidminer.tools.LogService;

public class ImportCPNModelTask extends Operator {
		
		public static final String PARAMETER_FILENAME = "filename";
			
			/** defining the ports */
			// I need to have a context, perhaps make this more generic
		
			private InputPort input = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
			private OutputPort output = getOutputPorts().createPort("model (ProM Colored Petri Net)");
			
			/**
			 * The default constructor needed in exactly this signature
			 */
			public ImportCPNModelTask(OperatorDescription description) {
				super(description);
				/** Adding a rule for meta data transformation: XLog will be passed through */
				getTransformer().addRule( new GenerateNewMDRule(output, CPNModelIOObject.class));
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
				ColouredPetriNet petrinet = null;
				CallProm tp = new CallProm ();
				try {
					Object[] objects = tp.runPlugin(pluginContext, "000", "CPN Tools Model", parameters);
					petrinet = (ColouredPetriNet) (objects)[0];
					
				} catch (Throwable e) {
					e.printStackTrace();
				}

				CPNModelIOObject petriNetIOObject = new CPNModelIOObject(petrinet);
				petriNetIOObject.setPluginContext(pluginContext);
				
				
				output.deliver(petriNetIOObject);
				
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
