package com.rapidminer.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class WriteProMIni {
	
	private static String newLineChar = "\n";
	private static String tabChar = "\t";
	
	private static WriteProMIni instance = null;
	
	private WriteProMIni () {
		
	}
	
	public static WriteProMIni getInstance () {
		if (instance == null) {
			instance = new WriteProMIni();
		}
		return instance;
	}
	
	public void generate(File file, String promPath) {
		try {
			FileOutputStream os = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			Writer w = new BufferedWriter(osw);
			writeIniFile(w, promPath);
			w.close();
			osw.close();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeIniFile(Writer w, String promPath) throws IOException {
//		# This file contains information about this ProM release
//		# it points ProM to the right packages and keeps version
//		# information
		w.write(tabChar + newLineChar);
		w.write("# This file contains information about ProM" + newLineChar);
		w.write("# it points ProM to the right packages and keeps version information" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# Folders should be separated using \"/\" (forward slash)." + newLineChar);
		w.write("# This will be replaced with File.Separator()." + newLineChar);
		w.write("#" + newLineChar);
		w.write("# Specifies the ProM release version" + newLineChar);
		w.write("PROM_VERSION = 6.4.1" + newLineChar);
		w.write("#" + newLineChar);
		w.write("#  Specifies which package should be installed" + newLineChar);
		w.write("RELEASE_PACKAGE = AllPackages" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# Specifies the URL to the default package repository" + newLineChar);
		w.write("# (default is \"http://prom.win.tue.nl/ProM/packages/packages.xml\")" + newLineChar);
		w.write("PACKAGE_URL = http://www.promtools.org/prom6/packages/packages.xml" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# Specifies whether ProM is Verbose" + newLineChar);
		w.write("#  (possible: \"ALL\" / \"ERROR\" / \"NONE\", defaults to \"ALL\")" + newLineChar);
		w.write("VERBOSE = ALL" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# The library folder is relative to the prom installation folder (default is \"lib\")" + newLineChar);
		w.write("# LIB_FOLDER = " + promPath +"/Lib" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# The images folder is relative to the prom library folder (default is \"=images\")" + newLineChar);
		w.write("IMAGES_FOLDER = images" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# The macro folder is relative to the prom library folder (default is \"macros\")" + newLineChar);
		w.write("MACRO_FOLDER = macros" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# The prom user folder is NOT relative to the prom installation folder. The (default is empty, in which case the OS handles the location)" + newLineChar);
		w.write("PROM_USER_FOLDER = " + promPath + newLineChar);
		w.write("#" + newLineChar);
		w.write("# The package folder is relative to the prom user folder. The (default is \"packages\")" + newLineChar);
		w.write("PACKAGE_FOLDER = packages" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# The workspace folder is relative to the prom user folder. The (default is \"workspace\")" + newLineChar);
		w.write("WORKSPACE_FOLDER = workspace" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# Indicate whether or not ProM should serialize the workspace. If switched off, the last serialized workspace will be loaded on each start" + newLineChar);
		w.write("# DO_SERIALIZATION = false" + newLineChar);
		w.write("#" + newLineChar);
		w.write("# Specifies the size of the backing store as used by OpenXES. The default is 4." + newLineChar);
		w.write("OPENXES_SHADOW_SIZE = 16" + newLineChar);
	}
	
	static void main(String [] args) {
		
	}

}
