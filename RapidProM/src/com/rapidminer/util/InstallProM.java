package com.rapidminer.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.processmining.framework.packages.impl.CancelledException;
import org.processmining.framework.util.OsUtil;

import com.rapidminer.configuration.GlobalProMParameters;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.tools.ParameterService;

public class InstallProM extends ProgressThread {
	
	private static String urlProMRM32 = "http://www.promtools.org/rapidprom/downloads/ProMRM_win32_202.zip";
	private static String urlProMRM64 = "http://www.promtools.org/rapidprom/downloads/ProMRM_win64_202.zip";
	private static String urlProMLIN32 = "http://www.promtools.org/rapidprom/downloads/ProMRM_linux32_202.zip";
	private static String urlProMLIN64 = "http://www.promtools.org/rapidprom/downloads/ProMRM_linux64_202.zip";
	private static String urlProMOSX32 = "http://www.promtools.org/rapidprom/downloads/ProMRM_osx_32_202.zip";
	private static String urlProMOSX64 = "http://www.promtools.org/rapidprom/downloads/ProMRM_osx_64_202.zip";
	
	
	private static final int UNIX_OWNER_EXECUTABLE_BIT = 64;
	
	private static JFileChooser folderChooser = null;
	private MainFrame mainframe = null;
	private static File unzipTo = null;
	
	private static String WIN32 = "Windows 32 bit";
	private static String WIN64 = "Windows 64 bit";
	private static String LINUX32 = "Linux 32 bit";
	private static String LINUX64 = "Linux 64 bit";
	private static String OSX32 = "OSX 32 bit";
	private static String OSX64 = "OSX 64 bit";
	
	public InstallProM(JFileChooser folderChooser, MainFrame mainframe) {
		this("INSTALL_PROM", true);
		InstallProM.folderChooser = folderChooser;
		this.mainframe = mainframe;
	}

	private InstallProM(String i18nKey, boolean runInForeground) {
		super(i18nKey, runInForeground);
	}

	@Override
	public void run() {
		getProgressListener().setMessage("START INSTALLING PROM");
		getProgressListener().setTotal(3);
		getProgressListener().setCompleted(0);
		// install prom
		System.out.println("getCurrentDirectory(): " 
		       +  folderChooser.getCurrentDirectory());
		System.out.println("getSelectedFile() : " 
		         +  folderChooser.getSelectedFile());
		Object[] possibilities = {WIN32, WIN64, LINUX32, LINUX64, OSX32, OSX64};
		String s = (String)JOptionPane.showInputDialog(mainframe,"Select the version of your machine.","Operating System",JOptionPane.PLAIN_MESSAGE,null,possibilities,WIN64);
		if (s != null && s.length()>0) {
			
		}
		else {
			s=WIN64;
		}
		getProgressListener().setCompleted(1);
		unzipTo = folderChooser.getSelectedFile();
		try {
			installProM(s,unzipTo.getAbsolutePath());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		getProgressListener().setCompleted(2);
		ParameterService.setParameterValue("prom_folder", unzipTo + File.separator + "ProMRM" + File.separator + "ProM.ini");
		ParameterService.saveParameters();
		GlobalProMParameters instance = GlobalProMParameters.getInstance();
		instance.setProMLocation(unzipTo + File.separator + "ProMRM" + File.separator + "ProM.ini");
		// write the prom.ini
		writeProMIni(unzipTo.getAbsolutePath());
		getProgressListener().setCompleted(3);
		getProgressListener().complete();
	}
	
	private static void installProM(String ostype, String unzipToPath) throws MalformedURLException {
		URL url = new URL(urlProMRM64);
		if (ostype.equals(WIN32)) { // win 32 bit
			url = new URL(urlProMRM32);
		}
		else if (ostype.equals(WIN64)) {
			
			url = new URL(urlProMRM64);
		}
		else if (ostype.equals(LINUX32)) {
			
			url = new URL(urlProMLIN32);
		}
		else if (ostype.equals(LINUX64)) {
			
			url = new URL(urlProMLIN64);
		}
		else if (ostype.equals(OSX32)) {
			
			url = new URL(urlProMOSX32);
		}
		else if (ostype.equals(OSX64)) {
			
			url = new URL(urlProMOSX64);
		}
		File sourceZipFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "ProMTemp.zip");
		// download zip file
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(sourceZipFile));
			copyInputStream(url.openStream(), out);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "I could not download the required packages. This could be caused by your firewall or internet connection. Please refer to the user guide for manual installation");
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
					// delete the temp file
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		
		boolean installed = false;
		while (!installed) {
			try {
				installed = extractZipFile(unzipToPath, sourceZipFile);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("Apparantly, I failed to copy to the selected folder");
				JOptionPane.showMessageDialog(null, "It is not possible to copy to the folder:" + unzipTo + ". Please select another folder!");
				folderChooser = new JFileChooser(); 
				folderChooser.setCurrentDirectory(new java.io.File("."));
				folderChooser.setDialogTitle("Select a folder to install the RapidProM packages");
				folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				folderChooser.setAcceptAllFileFilterUsed(false);
			    //    
			    if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			    	File unzipToFile = folderChooser.getSelectedFile();
			    	unzipTo = unzipToFile;
			    	unzipToPath = unzipToFile.getAbsolutePath();
			    	
			    }
			    else {
			    	installed = true;
			    }
			}
		}
		sourceZipFile.delete();
	}

	private static boolean extractZipFile(String unzipTo, File sourceZipFile)
			throws IOException, FileNotFoundException, CancelledException,
			ZipException {
		ZipFile zipFile = new ZipFile(sourceZipFile);
		Enumeration<?> zipFileEntries = zipFile.getEntries();

		while (zipFileEntries.hasMoreElements()) {
			ZipArchiveEntry entry = (ZipArchiveEntry) zipFileEntries.nextElement();
			File destFile = new File(unzipTo, entry.getName());

			if (entry.isDirectory()) {
				destFile.mkdirs();
			} else {
				destFile.getParentFile().mkdirs();

				OutputStream o = new FileOutputStream(destFile);
				try {
					copyInputStream(zipFile.getInputStream(entry), o);
				} finally {
					o.close();
				}

				//Only for non-windows operating systems: Check if the executable bit was set in the zip-archive,
				//if so, set it on the file system too. (Only checks and sets the owner executable bit.)
				if (!OsUtil.isRunningWindows()
						&& (entry.getUnixMode() & UNIX_OWNER_EXECUTABLE_BIT) == UNIX_OWNER_EXECUTABLE_BIT) {
					destFile.setExecutable(true);
				}
			}

		}
		zipFile.close();
		return true;
	}
	
	private static void writeProMIni(String locationProM) {
		String addendumIniFile = File.separator + "ProMRM" + File.separator + "ProM.ini";
		WriteProMIni instance = WriteProMIni.getInstance();
		String promPath = locationProM + File.separator + "ProMRM"; // + addendumIniFile;
		promPath = promPath.replace(File.separator, "/");
		promPath = promPath.replace("\\", "/");
		instance.generate(new File(locationProM + addendumIniFile), promPath);
	}
	
	private static void copyInputStream(InputStream in, OutputStream out) throws IOException, CancelledException {
		try {
			byte[] buffer = new byte[1024];
			int len;

			while ((len = in.read(buffer)) >= 0) {
				out.write(buffer, 0, len);
			}
		} finally {
			try {
				in.close();
			} finally {
				out.close();
			}
		}
	}

}
