package org.rapidprom.operators.abstr;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rapidprom.operators.io.ImportXLogOperator;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.AbstractReader;
import com.rapidminer.tools.LogService;

/**
 * 
 * @author svzelst
 *
 * @param <T>
 */
public abstract class AbstractRapidProMImportOperator<T extends IOObject>
		extends AbstractReader<T> {

	protected final static String PARAMETER_KEY_FILE = "file";
	protected final static String PARAMETER_DESC_FILE = "Select the file you would like to use to import.";
	protected Class<? extends IOObject> generatedClass;

	public AbstractRapidProMImportOperator(OperatorDescription description,
			Class<? extends IOObject> clazz, String[] supportedExtentions) {
		super(description, clazz);
		generatedClass = clazz;
		registerExtentions(supportedExtentions);
	}

	protected boolean checkFileParameterMetaData(String key) throws UserError {
		boolean result;
		File file = getParameterAsFile(key);
		if (!file.exists()) {
			throw new UserError(this, "301", file);
		} else if (!file.canRead()) {
			throw new UserError(this, "302", file, "");
		} else {
			result = true;
		}
		return result;
	}

	@Override
	public T read() throws OperatorException {
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: importing " + generatedClass.getName());
		long time = System.currentTimeMillis();
		if (checkFileParameterMetaData(PARAMETER_KEY_FILE)) {
			try {
				T result = read(getFile());
				logger.log(Level.INFO,
						"End: importing " + generatedClass.getName() + "("
								+ (System.currentTimeMillis() - time) / 1000
								+ " sec)");
				return result;
			} catch (Exception e) {
				throw new OperatorException("Import Failed: " + e.getMessage());
			}
		} else {
			throw new OperatorException("Import Failed");
		}
	}

	protected abstract T read(File file) throws Exception;

	protected File getFile() throws UserError {
		return getParameterAsFile(PARAMETER_KEY_FILE);
	}

	public void registerExtentions(String[] exts) {
		for (String ext : exts) {
			AbstractReader.registerReaderDescription(new ReaderDescription(ext,
					ImportXLogOperator.class, PARAMETER_KEY_FILE));
		}
	}
}
