package org.rapidprom.operators.io;

import java.io.File;
import java.util.List;

import org.processmining.eventstream.authors.staticeventstream.plugins.XSStaticXSEventStreamImportPlugin;
import org.processmining.eventstream.core.interfaces.XSStaticXSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.streams.event.XSStaticXSEventStreamIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

public class ImportXSStaticXSEventStreamOperator
		extends AbstractRapidProMImportOperator<XSStaticXSEventStreamIOObject> {

	private final static String[] SUPPORTED_FILE_FORMATS = new String[] {
			"evst" };

	public ImportXSStaticXSEventStreamOperator(
			OperatorDescription description) {
		super(description, XSStaticXSEventStreamIOObject.class,
				SUPPORTED_FILE_FORMATS);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_KEY_FILE, PARAMETER_DESC_FILE,
				false, SUPPORTED_FILE_FORMATS));
		return types;
	}

	@Override
	protected XSStaticXSEventStreamIOObject read(File file) throws Exception {
		XSStaticXSEventStreamImportPlugin importer = new XSStaticXSEventStreamImportPlugin();
		PluginContext context = ProMPluginContextManager.instance()
				.getFutureResultAwareContext(
						XSStaticXSEventStreamImportPlugin.class);
		XSStaticXSEventStream staticStream = (XSStaticXSEventStream) importer
				.importFile(context, file);
		return new XSStaticXSEventStreamIOObject(staticStream, context);
	}

}
