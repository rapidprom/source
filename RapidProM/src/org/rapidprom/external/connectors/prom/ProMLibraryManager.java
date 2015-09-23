package org.rapidprom.external.connectors.prom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

import org.apache.ivy.util.cli.CommandLineParser;
import org.processmining.framework.boot.Boot;
import org.rapidprom.RapidProMInitializer;
import org.rapidprom.external.connectors.ivy.IvyResolveException;
import org.rapidprom.external.connectors.ivy.IvyStandAlone;
import org.rapidprom.properties.RapidProMProperties;

import com.rapidminer.gui.tools.ProgressThread;

public class ProMLibraryManager extends ProgressThread {

	private File packageDir;
	private File ivyFile;
	private File ivySettingsFile;

	private static void copyInputStreamToOutputStream(InputStream i,
			OutputStream o) throws IOException {
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = i.read(bytes)) != -1) {
			o.write(bytes, 0, read);
		}
	}

	public ProMLibraryManager() {
		super("INSTALL_VERIFY_PROM_PACKAGES", true);
	}

	/**
	 * true iff .RapidProM-X.Y.Z folder exists in user home *AND* Both ivy and
	 * ivysettings file are present.
	 * 
	 * @return
	 */
	private static boolean isReadyForIvy() {
		boolean result = false;
		if (Files.exists(RapidProMProperties.instance()
				.getRapidProMPackagesLocationPath())) {
			if (Files.exists(Paths.get(RapidProMProperties.instance()
					.getRapidProMPackagesLocationString()
					+ File.separator
					+ RapidProMProperties.instance().getProperties()
							.getProperty("ivy_file_name_ext")))
					&& Files.exists(Paths.get(RapidProMProperties.instance()
							.getRapidProMPackagesLocationString()
							+ File.separator
							+ RapidProMProperties.instance().getProperties()
									.getProperty("ivy_settings_file_name_ext")))) {
				result = true;
			}
		}
		return result;
	}

	public void run() {
		synchronized (RapidProMInitializer.LOCK) {
			if (!isReadyForIvy()) {
				getProgressListener().setTotal(3);
				getProgressListener().setMessage(
						"Setting up library directory...");
				packageDir = createPackageFolder();
				getProgressListener().setCompleted(1);
				getProgressListener().setMessage(
						"Copying library definitions...");
				ivyFile = unPackIvyFile(packageDir);
				ivySettingsFile = unPackIvySettingsFile(packageDir);
				getProgressListener().setCompleted(2);
				getProgressListener().setMessage(
						"Downloading libraries, please be patient...");
				runIvy();
				getProgressListener().setCompleted(3);
			} else {
				getProgressListener().setTotal(1);
				getProgressListener().setCompleted(0);
				getProgressListener().setMessage("Checking libraries...");
				packageDir = getPackageFolder();
				ivyFile = getIvyFile();
				ivySettingsFile = getIvySettingsFile();
				runIvy();
				getProgressListener().setCompleted(1);
			}
			getProgressListener().complete();
			RapidProMInitializer.PROM_LIBRARIES_LOADED = true;
			RapidProMInitializer.LOCK.notifyAll();
		}

	}

	private File createPackageFolder() {
		File rapidProMPackageDirectory = new File(RapidProMProperties
				.instance().getRapidProMPackagesLocationString());
		rapidProMPackageDirectory.mkdir();
		return rapidProMPackageDirectory;
	}

	private File getPackageFolder() {
		return new File(RapidProMProperties.instance()
				.getRapidProMPackagesLocationString());
	}

	private File getIvyFile() {
		return new File(RapidProMProperties.instance()
				.getRapidProMPackagesLocationString()
				+ File.separator
				+ RapidProMProperties.instance().getProperties()
						.getProperty("ivy_file_name_ext"));
	}

	private File getIvySettingsFile() {
		return new File(RapidProMProperties.instance()
				.getRapidProMPackagesLocationString()
				+ File.separator
				+ RapidProMProperties.instance().getProperties()
						.getProperty("ivy_settings_file_name_ext"));
	}

	protected void runIvy() {
		String[] args = new String[7];
		args[0] = "-settings";
		args[1] = ivySettingsFile.getAbsolutePath();
		args[2] = "-ivy";
		args[3] = ivyFile.getAbsolutePath();
		args[4] = "-cache";
		args[5] = RapidProMProperties.instance()
				.getRapidProMPackagesLocationString();
		args[6] = "-m2compatible";
		try {
			IvyStandAlone.invokeIvy(args, Boot.Level.ALL);
		} catch (IvyResolveException ire) {
			Object[] options = { "OK" };
			JOptionPane
					.showOptionDialog(
							null,
							"Loading/Verifying the RapidProM extension failed. Please check your internet connection.",
							"Warning", JOptionPane.PLAIN_MESSAGE,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected File unPackIvyFile(File toDir) {
		assert (Files.exists(toDir.toPath()));
		String ivyFileStr = RapidProMProperties.instance().getProperties()
				.getProperty("ivy_file_name_ext");
		InputStream ivyIS = RapidProMInitializer.class
				.getResourceAsStream(RapidProMProperties.instance()
						.getProperties()
						.getProperty("ivy_file_location_in_archive")
						+ ivyFileStr);
		File ivyFile = new File(toDir.getAbsolutePath() + File.separator
				+ ivyFileStr);
		try {
			OutputStream ivyOS = new FileOutputStream(ivyFile);
			copyInputStreamToOutputStream(ivyIS, ivyOS);
			ivyIS.close();
			ivyOS.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ivyFile;
	}

	protected File unPackIvySettingsFile(File toDir) {
		String ivySettingsStr = RapidProMProperties.instance().getProperties()
				.getProperty("ivy_settings_file_name_ext");
		InputStream ivySettingsIS = RapidProMInitializer.class
				.getResourceAsStream(RapidProMProperties.instance()
						.getProperties()
						.getProperty("ivy_settings_file_location_in_archive")
						+ ivySettingsStr);
		OutputStream ivySettingsOS;
		File ivySettingsFile = new File(toDir.getAbsolutePath()
				+ File.separator + ivySettingsStr);
		try {
			ivySettingsOS = new FileOutputStream(ivySettingsFile);
			copyInputStreamToOutputStream(ivySettingsIS, ivySettingsOS);
			ivySettingsIS.close();
			ivySettingsOS.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ivySettingsFile;
	}

}
