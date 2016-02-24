package org.rapidprom.operators.abstr;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;
import org.rapidprom.util.IOUtils;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractWriter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDirectory;
import com.rapidminer.parameter.ParameterTypeString;

/**
 *
 * @param <T>
 *            IOObject
 * @param <T2>
 *            Internal object of IOObject (i.e. XLogIOObject => T2 == XLog)
 * @param <F>
 *            FileFormat, assumes: toString(); gives the file format.
 */
public abstract class AbstractRapidProMExporterOperator<T extends AbstractRapidProMIOObject<T2>, T2, F>
		extends AbstractWriter<T> {

	protected final static String PARAMETER_KEY_FOLDER = "folder";
	protected final static String PARAMETER_DESC_FOLDER = "The folder where the file should be stored.";

	protected final static String PARAMETER_KEY_FILE_NAME = "file_name";
	protected final static String PARAMETER_DESC_FILE_NAME = "The file name of the exported object.";

	protected final static String PARAMETER_KEY_FILE_FORMAT = "file_format";
	protected final static String PARAMETER_DESC_FILE_FORMAT = "The file format of the exported object.";

	protected final F[] PARAMETER_VALUES_FILE_FORMAT;
	protected final F defaultFileFormat;

	public AbstractRapidProMExporterOperator(OperatorDescription description,
			Class<T> savedClass, F[] fileFormats, F defaultFileFormat) {
		super(description, savedClass);
		assert (Arrays.asList(fileFormats).contains(defaultFileFormat));
		PARAMETER_VALUES_FILE_FORMAT = fileFormats;
		this.defaultFileFormat = defaultFileFormat;
	}

	@Override
	public T write(T ioobject) throws OperatorException {
		try {
			F format = PARAMETER_VALUES_FILE_FORMAT[getParameterAsInt(
					PARAMETER_KEY_FILE_FORMAT)];
			File target = IOUtils.prepareTargetFile(
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

	protected abstract void writeToFile(File file, T2 object, F format)
			throws IOException;

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType dir = new ParameterTypeDirectory(PARAMETER_KEY_FOLDER,
				PARAMETER_DESC_FOLDER, "");
		dir.setOptional(false);
		types.add(dir);

		ParameterTypeString fileNameParam = new ParameterTypeString(
				PARAMETER_KEY_FILE_NAME, PARAMETER_DESC_FILE_NAME);
		fileNameParam.setExpert(false);
		fileNameParam.setOptional(false);
		types.add(fileNameParam);

		String[] fileFormatStr = new String[PARAMETER_VALUES_FILE_FORMAT.length];
		for (int i = 0; i < fileFormatStr.length; i++) {
			fileFormatStr[i] = PARAMETER_VALUES_FILE_FORMAT[i].toString();
		}

		ParameterTypeCategory fileFormat = new ParameterTypeCategory(
				PARAMETER_KEY_FILE_FORMAT, PARAMETER_DESC_FILE_FORMAT,
				fileFormatStr, Arrays.asList(PARAMETER_VALUES_FILE_FORMAT)
						.indexOf(defaultFileFormat));
		fileFormat.setExpert(false);
		fileFormat.setOptional(false);

		types.add(fileFormat);
		return types;
	}
}
