package com.rapidminer.operator.importplugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjectrenderers.XLogIOObjectRenderer;
import com.rapidminer.ioobjectrenderers.XLogIOObjectVisualizationType;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameters.Parameter;
import com.rapidminer.parameters.ParameterCategory;
import com.rapidminer.util.ProMIOObjectList;

public class ExtractLogFromFileTask extends Operator {

	public static final String PARAMETER_FILE_TYPE = "type of the log file";
	private static final String[] FILE_TYPES = { "Naive", "Buffered", "Normal",
			"Lightweight", "Buffered_by_MAPDB" };
	private static final int Naive = 0;
	private static final int Buffered = 1;
	private static final int Normal = 2;
	private static final int Lightweight = 3;
	private static final int Buffered_by_MAPDB = 4;
	private static final String TABLE_VIS = "Visualize Log as ExampleSet";
	private static final String PROM_VIS = "Visualize Log using ProM Log Visualizer";
	private static final String XDOT_VIS = "Visualize Log using XDottedChart";

	private ParameterCategory visType = null;

	/** defining the ports */
	// I need to have a context, perhaps make this more generic
	private InputPort proMcontext = getInputPorts().createPort(
			"context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputfile = getInputPorts().createPort("file",
			FileObject.class);

	private OutputPort output = getOutputPorts().createPort(
			"event log (ProM Event Log)");

	/**
	 * The default constructor needed in exactly this signature
	 */
	public ExtractLogFromFileTask(OperatorDescription description) {
		super(description);

		/**
		 * Adding a rule for meta data transformation: XLog will be passed
		 * through
		 */
		getTransformer().addRule(
				new GenerateNewMDRule(output, XLogIOObject.class));
		// add additional rule that the file should be ok, see LoadFileOperator
		// class
	}

	/**
	 * @throws UserError
	 * 
	 */
	protected void checkMetaData() throws UserError {
		try {
			File file = inputfile.getData(FileObject.class).getFile();

			// check if file exists and is readable
			if (!file.exists()) {
				throw new UserError(this, "301", file);
			} else if (!file.canRead()) {
				throw new UserError(this, "302", file, "");
			}
		} catch (OperatorException e) {
			// handled by parameter checks in super class
		}
	}

	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		// LogService logService = LogService.getGlobal();
		// logService.log("start do work read log task", LogService.NOTE);
		ProMContextIOObject context = proMcontext
				.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();

		// run the plugin for loading the log
		File file = inputfile.getData(FileObject.class).getFile();
		int fileType = getParameterAsInt(PARAMETER_FILE_TYPE);
		// try to get the visualization par
		Parameter parameter1 = visType;
		int par1int = getParameterAsInt(parameter1.getNameParameter());
		String valPar1 = (String) parameter1.getValueParameter(par1int);
		// check if file exists and is readable
		if (file == null || !file.exists()) {
			throw new UserError(this, "301", file);
		} else if (!file.canRead()) {
			throw new UserError(this, "302", file, "");
		}

		List<Object> parameters = new ArrayList<Object>();
		parameters.add(file);
		XLog promLog = null;
		CallProm tp = new CallProm();
		try {
			if (fileType == Naive) {
				promLog = (XLog) (tp.runPlugin(pluginContext, "000",
						"Open XES Log File (Naive)", parameters))[0];
				// Object[] results = tp.runPlugin(pluginContext, "000",
				// "Open XES Log File (Naive)", parameters);
			} else if (fileType == Buffered) {
				promLog = (XLog) (tp.runPlugin(pluginContext, "000",
						"Open XES Log File (Buffered)", parameters))[0];
			} else if (fileType == Normal) {
				promLog = (XLog) (tp.runPlugin(pluginContext, "000",
						"Open XES Log File", parameters))[0];
			} else if (fileType == Lightweight) {
				promLog = (XLog) (tp.runPlugin(pluginContext, "000",
						"Open XES Log File (Lightweight & Sequential IDs)",
						parameters))[0];
			} else if (fileType == Buffered_by_MAPDB) {
				promLog = (XLog) (tp.runPlugin(pluginContext, "000",
						"Open XES Log File (Disk-buffered by MapDB)",
						parameters))[0];
			}
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane
					.showMessageDialog(
							null,
							"The Log could not be read. Please have a look at the error trace. Perhaps something is wrong with the file?",
							"Read Log File Operator Error",
							JOptionPane.ERROR_MESSAGE);
		}
		// end plugin
		XLogIOObject xLogIOObject = new XLogIOObject(promLog);
		xLogIOObject.setPluginContext(pluginContext);
		if (valPar1.equals(PROM_VIS)) {
			// visualize using prom
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.DEFAULT);
		} else if (valPar1.equals(TABLE_VIS)) {
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.EXAMPLE_SET);
		} else {
			xLogIOObject
					.setVisualizationType(XLogIOObjectVisualizationType.X_DOTTED_CHART);
		}
		output.deliver(xLogIOObject);
		// add to list so that afterwards it can be cleared if needed
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.addToList(xLogIOObject);
		// logService.log("end do work first prom task", LogService.NOTE);
	}

	@Override
	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();
		ParameterTypeCategory filetypes = new ParameterTypeCategory(
				PARAMETER_FILE_TYPE,
				"select the format of the log file to be saved", FILE_TYPES,
				Naive);
		Object[] par1categories = new Object[] { TABLE_VIS, PROM_VIS, XDOT_VIS };
		ParameterCategory parameter1 = new ParameterCategory(par1categories,
				TABLE_VIS, String.class, "Visualize Log", "Visualize Log");
		ParameterTypeCategory parameterType1 = new ParameterTypeCategory(
				parameter1.getNameParameter(),
				parameter1.getDescriptionParameter(),
				parameter1.getOptionsParameter(),
				parameter1.getIndexValue(parameter1.getDefaultValueParameter()));
		parameterTypes.add(parameterType1);
		parameterTypes.add(filetypes);
		visType = parameter1;
		return parameterTypes;
	}

}
