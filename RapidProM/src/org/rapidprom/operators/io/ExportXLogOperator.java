package org.rapidprom.operators.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.log.FileFormat;
import org.processmining.plugins.log.exporting.ExportLogMxml;
import org.processmining.plugins.log.exporting.ExportLogMxmlGz;
import org.processmining.plugins.log.exporting.ExportLogXes;
import org.processmining.plugins.log.exporting.ExportLogXesGz;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.AbstractWriter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDirectory;
import com.rapidminer.parameter.ParameterTypeString;

public class ExportXLogOperator extends AbstractWriter<XLogIOObject> {

	private final static String PARAMETER_KEY_FOLDER = "folder";
	private final static String PARAMETER_DESC_FOLDER = "The folder where the exported event log should be stored.";

	private final static String PARAMETER_KEY_FILE_NAME = "file_name";
	private final static String PARAMETER_DESC_FILE_NAME = "The file name of the exported event log.";

	private final static String PARAMETER_KEY_FILE_FORMAT = "file_format";
	private final static String PARAMETER_DESC_FILE_FORMAT = "The file format of the exported event log.";
	private final static FileFormat[] PARAMETER_VALUES_FILE_FORMAT = EnumSet
			.allOf(FileFormat.class)
			.toArray(new FileFormat[EnumSet.allOf(FileFormat.class).size()]);

	public ExportXLogOperator(OperatorDescription description) {
		super(description, XLogIOObject.class);
	}

	@Override
	public XLogIOObject write(XLogIOObject ioobject) throws OperatorException {
		try {

			FileFormat format = PARAMETER_VALUES_FILE_FORMAT[getParameterAsInt(
					PARAMETER_KEY_FILE_FORMAT)];
			File target = prepareTargetFile(
					getParameterAsFile(PARAMETER_KEY_FOLDER).getCanonicalPath(),
					getParameterAsString(PARAMETER_KEY_FILE_NAME), format);
			if (target.exists()) {
				target.delete();
			}
			target.createNewFile();
			writeToFile(target, ioobject.getArtifact(), format);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return ioobject;
	}

	private File prepareTargetFile(String dirPath, String name,
			FileFormat format) throws UserError, IOException {
		// only remove a ".null" if it is the last occurring element of the
		// path.
		String nullStr = ".null";
		if (dirPath.length() > nullStr.length()) {
			if (dirPath.substring(dirPath.length() - nullStr.length(),
					dirPath.length()).contains(".null")) {
				dirPath = dirPath.substring(0,
						dirPath.length() - nullStr.length());
			}
		}
		if (!dirPath.endsWith(File.separator)) {
			dirPath += File.separator;
		}
		dirPath += name + "." + format.toString();
		return new File(dirPath);
	}

	private void writeToFile(File file, XLog log, FileFormat format)
			throws IOException {
		switch (format) {
		case MXML:
			ExportLogMxml.export(log, file);
			break;
		case MXML_GZ:
			ExportLogMxmlGz.export(log, file);
			break;
		case XES_GZ:
			ExportLogXesGz.export(log, file);
			break;
		case XES:
		default:
			ExportLogXes.export(log, file);
			break;
		}
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType dir = new ParameterTypeDirectory(PARAMETER_KEY_FOLDER,
				PARAMETER_DESC_FOLDER, "");
		dir.setOptional(false);
		types.add(dir);

		types.add(new ParameterTypeString(PARAMETER_KEY_FILE_NAME,
				PARAMETER_DESC_FILE_NAME));

		String[] fileFormatStr = new String[PARAMETER_VALUES_FILE_FORMAT.length];
		for (int i = 0; i < fileFormatStr.length; i++) {
			fileFormatStr[i] = PARAMETER_VALUES_FILE_FORMAT[i].toString();
		}
		types.add(new ParameterTypeCategory(PARAMETER_KEY_FILE_FORMAT,
				PARAMETER_DESC_FILE_FORMAT, fileFormatStr,
				Arrays.asList(PARAMETER_VALUES_FILE_FORMAT)
						.indexOf(FileFormat.XES)));
		return types;
	}

}
