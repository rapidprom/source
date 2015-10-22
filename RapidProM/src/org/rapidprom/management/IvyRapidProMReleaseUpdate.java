package org.rapidprom.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * The IvyRapidProMReleaseUpdate script allows us to create new ivy files for
 * all RapidProM library elements. The script needs three arguments: 1. location
 * of libraries 2. previous version (it will skip all libraries that where not a
 * member of the previous version) 3. new version
 * 
 */
public class IvyRapidProMReleaseUpdate {

	private final File library;
	private final String previousRelease;
	private final String newRelease;
	private final String ivyRegex;

	private static final String GIT_FOLDER_NAME = ".git";
	private static final String THIRDPARTY_FOLDER_NAME = "thirdparty";
	private static final String RAPID_PROM_RELEASE = "rapidprom:release";

	public IvyRapidProMReleaseUpdate(final File library,
			final String previousVersion, final String newVerison) {
		this.library = library;
		this.previousRelease = previousVersion;
		this.newRelease = newVerison;
		ivyRegex = "ivy-.*" + "-" + previousVersion + ".xml";
	}

	public void apply() {
		assert (library.isDirectory());
		recurse(library);

	}

	private void recurse(File f) {
		for (File child : f.listFiles()) {
			if (child.isDirectory() && !(child.getName().equals(GIT_FOLDER_NAME)
					|| child.getName().equals(THIRDPARTY_FOLDER_NAME))) {
				recurse(child);
			} else {
				if (child.getName().matches(ivyRegex)) {
					try {
						updateRapidProMRelease(copyOldIvyFile(child));
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void updateRapidProMRelease(File newIvyFile) throws IOException {
		String content = FileUtils.readFileToString(newIvyFile);
		content = content.replaceAll(
				RAPID_PROM_RELEASE + "=\"" + previousRelease + "\"",
				RAPID_PROM_RELEASE + "=\"" + newRelease + "\"");
		OutputStream targetOS = new FileOutputStream(newIvyFile);
		IOUtils.write(content, targetOS);
	}

	private File copyOldIvyFile(File oldIvyFile) throws IOException {
		String newName = oldIvyFile.getName();
		String[] newNameArr = newName.split("-" + previousRelease + ".xml");
		newName = newNameArr[0] + "-" + newRelease + ".xml";
		String newPath = oldIvyFile.getParentFile().getCanonicalPath()
				+ File.separator + newName;
		File newFile = new File(newPath);
		if (newFile.exists())
			newFile.delete();
		newFile.createNewFile();
		FileInputStream sourceIS = new FileInputStream(oldIvyFile);
		FileOutputStream targetOS = new FileOutputStream(newFile);
		FileChannel fcSource = sourceIS.getChannel();
		FileChannel fcTarget = targetOS.getChannel();
		fcTarget.transferFrom(fcSource, 0, fcSource.size());
		sourceIS.close();
		targetOS.close();
		return newFile;
	}

	public static void main(String[] args) {
		String libraryPath = args[0];
		File libraryFile = new File(libraryPath);
		String prevVersion = args[1];
		String newVersion = args[2];
		IvyRapidProMReleaseUpdate update = new IvyRapidProMReleaseUpdate(
				libraryFile, prevVersion, newVersion);
		update.apply();
	}

}
