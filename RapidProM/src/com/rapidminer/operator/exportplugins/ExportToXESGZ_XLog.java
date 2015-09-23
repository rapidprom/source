package com.rapidminer.operator.exportplugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.LogService;

public class ExportToXESGZ_XLog extends Operator {
	
	public static final String PARAMETER_FILENAME = "filename";
	
	public static final String PARAMETER_FILE_TYPE = "type of the log file";

	private static final int XES = 0;
	private static final int XES_GZ = 1;
	private static final int MXML = 2;
	private static final int MXML_GZ = 3;

	private static final String[] FILE_TYPES = { "XES", "XES.GZ", "MXML", "MXML.GZ" };
	
	/** defining the ports */
	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort inputLog = getInputPorts().createPort("event log (ProM Event Log)", XLogIOObject.class);
	
	/**
	 * The default constructor needed in exactly this signature
	 */
	public ExportToXESGZ_XLog(OperatorDescription description) {
		super(description);
	}
	
	@Override
	public void doWork() throws OperatorException {
		// get ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do work log export", LogService.NOTE);
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		// get the log
		XLogIOObject log = inputLog.getData(XLogIOObject.class);
		XLog promLog = log.getPromLog();
		System.out.println("DUMPTHIRD");
		dumpSizeTraces(promLog);
		CallProm tp = new CallProm();
		if (pluginContext == null) {
			System.out.println("pluginContext is null");
		}
		else {
			System.out.println("pluginContext is not null");
		}
		// create the file
		File file = getParameterAsFile(PARAMETER_FILENAME);	
		int fileType = getParameterAsInt(PARAMETER_FILE_TYPE);
		File newFile = manipulateFileNameIfNeeded(file, fileType);
		List<Object> pars = new ArrayList<Object>();
		pars.add(promLog);
		pars.add(newFile);
		if (fileType == XES) {
			Object[] runPlugin = tp.runPlugin(pluginContext, "5", "Export Log to XES File", pars);
		}
		else if (fileType == XES_GZ) {
			Object[] runPlugin = tp.runPlugin(pluginContext, "5", "Export Log to compressed XES File", pars);
		}
		else if (fileType == MXML) {
			Object[] runPlugin = tp.runPlugin(pluginContext, "5", "Export Log to MXML File", pars);
		}
		else if (fileType == MXML_GZ) {
			Object[] runPlugin = tp.runPlugin(pluginContext, "5", "Export Log to compressed MXML File", pars);
		}
		else {
//			Object[] runPlugin = tp.runPlugin(pluginContext, "5", "Export Log to compressed XES File", parClasses, pars);			
			Object[] runPlugin = tp.runPlugin(pluginContext, "5", "Export Log to compressed XES File", pars);
		}
		logService.log("end do work log export", LogService.NOTE);
	}
	
	private void dumpSizeTraces(XLog xLog) {
		for (XTrace t : xLog) {
			System.out.println(XConceptExtension.instance().extractName(t) + ":" + t.size());
		}
		
	}
	
	private File manipulateFileNameIfNeeded(File file, int fileType) {
		String absolutePath = file.getAbsolutePath();	
		if (fileType == XES) {
			if (!(absolutePath.endsWith(".xes") || absolutePath.endsWith(".Xes") || absolutePath.endsWith(".XES"))) {
				absolutePath = absolutePath + ".xes";
				File newFile = new File(absolutePath);
				return newFile;
			}
		}
		else if (fileType == XES_GZ) {
			if (!(absolutePath.endsWith(".xes.gz") || absolutePath.endsWith(".Xes.gz") || absolutePath.endsWith(".XES.GZ"))) {
				absolutePath = absolutePath + ".xes.gz";
				File newFile = new File(absolutePath);
				return newFile;
			}
		}
		else if (fileType == MXML) {
			if (!(absolutePath.endsWith(".mxml") || absolutePath.endsWith(".Mxml") || absolutePath.endsWith(".MXML"))) {
				absolutePath = absolutePath + ".mxml";
				File newFile = new File(absolutePath);
				return newFile;
			}
		}
		else if (fileType == MXML_GZ) {
			if (!(absolutePath.endsWith(".mxml.gz") || absolutePath.endsWith(".Mxml.gz") || absolutePath.endsWith(".MXML.GZ"))) {
				absolutePath = absolutePath + ".mxml.gz";
				File newFile = new File(absolutePath);
				return newFile;
			}
		}
		return file;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();
		ParameterTypeCategory filetypes = new ParameterTypeCategory(PARAMETER_FILE_TYPE, "select the format of the log file to be saved", FILE_TYPES, XES_GZ);
		ParameterTypeFile parameterTypeFile = new ParameterTypeFile(PARAMETER_FILENAME, "File to open", "log", true, false);
		parameterTypes.add(parameterTypeFile);
		parameterTypes.add(filetypes);
		return parameterTypes;
	}

}
