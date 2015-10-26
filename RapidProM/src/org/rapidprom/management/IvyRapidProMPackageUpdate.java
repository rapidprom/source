package org.rapidprom.management;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * This management script allows you to add a new (ProM)-based package to the
 * RapidProM lib folder. It takes three arguments:
 * 
 * 1. the location of the packages git-based folder 2. the name of the package
 * that is updated 3. the *new* version of the package (note you should have
 * created the folder, jar and ivy files for this new release yourself). 4.
 * current version of RapidProM
 * 
 * @author svzelst
 *
 */
public class IvyRapidProMPackageUpdate {

	private final File libFolder;
	private final String packageName;
	private final String newPackageVersion;
	private final String rapidProMRelease;
	private final String ivyRegex;

	private static final String GIT_FOLDER_NAME = ".git";
	private static final String THIRDPARTY_FOLDER_NAME = "thirdparty";
	private static final String DEPENDENCY_PATTERN = "<dependency";
	private static final String XML_CLOSE = "/>";
	private static final String DEPENDENCY_CLOSE = "</dependency>";
	private static final String ORGANISATION_TAG = "org=\"org.rapidprom\"";
	private static final String NAME_TAG = "name=";
	private static final String RAPIDPROM_RELEASE_TAG = "rapidprom:release=";
	private static final String REVISION_TAG_START = "rev=\"";

	public IvyRapidProMPackageUpdate(final File libFolder,
			final String packageName, final String newPackageVersion,
			final String rapidProMRelease) {
		this.libFolder = libFolder;
		this.packageName = packageName;
		this.newPackageVersion = newPackageVersion;
		this.rapidProMRelease = rapidProMRelease;
		ivyRegex = "ivy-.*" + "-" + rapidProMRelease + ".xml";

	}

	public void apply() {
		assert (libFolder.isDirectory());
		recurse(libFolder);
	}

	private void recurse(File f) {
		for (File child : f.listFiles()) {
			if (child.isDirectory() && !(child.getName().equals(GIT_FOLDER_NAME)
					|| child.getName().equals(THIRDPARTY_FOLDER_NAME))) {
				recurse(child);
			} else {
				if (child.getName().matches(ivyRegex)) {
					try {
						String content = FileUtils.readFileToString(child);
						int start = content.indexOf(DEPENDENCY_PATTERN);
						while (start >= 0) {
							int close = getIndexOfClosestEndTag(content, start);
							assert (close < Integer.MAX_VALUE && close > start);
							String dep = content.substring(start, close);
							if (dependencyPointsToUpdatedPackage(dep)) {
								content = updateDependency(content, dep);
							}
							start = content.indexOf(DEPENDENCY_PATTERN,
									start + 1);
						}
						IOUtils.write(content, new FileOutputStream(child));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private String updateDependency(String ivyFileContent, String dep) {
		int revStartIndex = dep.indexOf(REVISION_TAG_START);
		int revEndIndex = revStartIndex + REVISION_TAG_START.length() + 1;
		while (!dep.substring(revEndIndex, revEndIndex + 1).equals("\"")) {
			revEndIndex++;
		}
		String oldRev = dep.substring(revStartIndex, revEndIndex + 1);
		String newRev = REVISION_TAG_START + newPackageVersion + "\"";
		String newDep = dep.replace(oldRev, newRev);
		return ivyFileContent.replaceFirst(dep, newDep);
	}

	private int getIndexOfClosestEndTag(String content, int start) {
		int close = content.indexOf(XML_CLOSE, start) >= 0
				? content.indexOf(XML_CLOSE, start) : Integer.MAX_VALUE;

		close = content.indexOf(DEPENDENCY_CLOSE, start) >= 0
				? Math.min(close, content.indexOf(DEPENDENCY_CLOSE, start))
				: close;
		return close;
	}

	private boolean dependencyPointsToUpdatedPackage(String dep) {
		boolean result = true;
		result &= dep.contains(ORGANISATION_TAG);
		result &= dep.contains(NAME_TAG + "\"" + packageName + "\"");
		result &= dep.contains(
				RAPIDPROM_RELEASE_TAG + "\"" + rapidProMRelease + "\"");
		return result;
	}

	public static void main(String[] args) {
		File libFolder = new File(args[0]);
		IvyRapidProMPackageUpdate update = new IvyRapidProMPackageUpdate(
				libFolder, args[1], args[2], args[3]);
		update.apply();
	}

}
