package org.rapidprom.util;

import java.io.File;
import java.io.IOException;

import com.rapidminer.operator.UserError;

public class IOUtils {

	/**
	 * Creates a File object in some directory, given some name, and given some
	 * extention (which is possibly an enum). It specifically checks whether the
	 * directory path contains ".null" at the end which is a side-effect of
	 * RapidMiner's directory chooser.
	 * 
	 * @param dirPath
	 * @param name
	 * @param format
	 * @return
	 * @throws UserError
	 * @throws IOException
	 */
	public static <F> File prepareTargetFile(String dirPath, String name,
			F format) throws UserError, IOException {
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

}
