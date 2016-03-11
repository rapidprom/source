package org.rapidprom.external.connectors.prom;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.processmining.framework.boot.Boot;
import org.rapidprom.RapidProMInitializer;
import org.rapidprom.external.connectors.ivy.IvyResolveException;
import org.rapidprom.external.connectors.ivy.IvyStandAlone;
import org.rapidprom.properties.RapidProMProperties;
import org.rapidprom.util.OSUtils;

import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.tools.ProgressListener;

/**
 * The ProM Library Manager Entity makes sure that all the required java
 * archives as well as all needed resources (e.g. LpSolve) are present on the
 * user's machine. For resources, which should be in the resources folder,
 * defined in the /resources/org/rapidprom/properties/config.properties file,
 * the script makes sure that all .zip files will be unzipped.
 * 
 * @author svzelst
 *
 */
public class ProMLibraryManager extends ProgressThread {

	private class IvyInstallationProgressionTracker implements Runnable {

		private final ProgressListener progress;
		private final File rapidProMIvyFolder;
		private final int total;
		private int current = 0;
		private static final long TIMEOUT_MS = 15000;

		public IvyInstallationProgressionTracker(ProgressListener progress,
				File rapidProMIvyFolder) {
			this.progress = progress;
			this.rapidProMIvyFolder = rapidProMIvyFolder;
			total = Integer
					.valueOf(RapidProMProperties.instance().getProperties()
							.getProperty("rapidprom_total_library_files"));
			progress.setTotal(total);
		}

		@Override
		public void run() {
			while (current < total) {
				current = FileUtils.listFiles(rapidProMIvyFolder,
						TrueFileFilter.TRUE, TrueFileFilter.TRUE).size();
				progress.setCompleted(Math.min(total, current));
				try {
					Thread.sleep(TIMEOUT_MS);
				} catch (InterruptedException e) {
				}
			}
			progress.setCompleted(total);
		}

	}

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
			if (Files
					.exists(Paths.get(RapidProMProperties.instance()
							.getRapidProMPackagesLocationString()
							+ File.separator
							+ RapidProMProperties.instance().getProperties()
									.getProperty("ivy_file_name_ext")))
					&& Files.exists(Paths.get(RapidProMProperties.instance()
							.getRapidProMPackagesLocationString()
							+ File.separator
							+ RapidProMProperties.instance().getProperties()
									.getProperty(
											"ivy_settings_file_name_ext")))) {
				result = true;
			}
		}
		return result;
	}

	public void run() {
		ExecutorService service = Executors.newSingleThreadExecutor();
		getProgressListener().setTotal(0);
		if (!isReadyForIvy()) {
			JOptionPane.showMessageDialog(null,
					"RapidProM is running for the first time, and needs to download several libraries. This may take a while. Please be patient.");
			getProgressListener()
					.setMessage("Downloading libraries, please be patient...");
			packageDir = createPackageFolder();
			Runnable progressTracker = new IvyInstallationProgressionTracker(
					getProgressListener(), packageDir);
			service.execute(progressTracker);
			// disable prepack (prom config may still indicate "live"
			// if (RapidProMProperties.instance().getDeployment()
			// .equals(Deployment.LIVE)) {
			// deployPrepack(packageDir);
			// }
			ivyFile = unPackIvyFile(packageDir);
			ivySettingsFile = unPackIvySettingsFile(packageDir);
		} else {
			getProgressListener().setMessage("Checking libraries...");
			packageDir = getPackageFolder();
			Runnable progressTracker = new IvyInstallationProgressionTracker(
					getProgressListener(), packageDir);
			service.execute(progressTracker);
			ivyFile = getIvyFile();
			ivySettingsFile = getIvySettingsFile();
		}
		runIvy();
		unzipResources(new File(RapidProMProperties.instance()
				.getRapidProMPackagesLocationString() + File.separator
				+ RapidProMProperties.instance().getProperties()
						.getProperty("rapidprom_resources_dir")));
		if (service != null) {
			service.shutdownNow();
		}
		getProgressListener().complete();
	}

	@SuppressWarnings("unused")
	private void deployPrepack(File rapidProMPackageDirectory) {
		try {
			File prepack = downloadPrepackedIvyFolders(
					rapidProMPackageDirectory);
			unpackPrepackedIvyFolders(prepack, rapidProMPackageDirectory);
			Files.delete(prepack.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File createPackageFolder() {
		File rapidProMPackageDirectory = new File(RapidProMProperties.instance()
				.getRapidProMPackagesLocationString());
		rapidProMPackageDirectory.mkdir();
		return rapidProMPackageDirectory;
	}

	private File downloadPrepackedIvyFolders(File rapidProMPackageDirectory)
			throws IOException {
		URL url = new URL(RapidProMProperties.instance().getProperties()
				.getProperty("rapidprom_prapack_location")
				+ RapidProMProperties.instance().getVersionsRevisionUpdate()
				+ RapidProMProperties.instance().getProperties()
						.getProperty("rapidprom_prepack_ext"));
		File target = new File(
				rapidProMPackageDirectory.getPath() + File.separator
						+ RapidProMProperties.instance()
								.getVersionsRevisionUpdate()
						+ RapidProMProperties.instance().getProperties()
								.getProperty("rapidprom_prepack_ext"));
		target.createNewFile();
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(target);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		rbc.close();
		return target;
	}

	/**
	 * used some of;
	 * http://java-tweets.blogspot.nl/2012/07/untar-targz-file-with-apache-
	 * commons.html
	 * 
	 * @param prepack
	 * @param rapidpromTargetLocation
	 * @throws IOException
	 */
	private void unpackPrepackedIvyFolders(File prepack,
			File rapidpromTargetLocation) throws IOException {
		int buffer = 2048;
		FileInputStream fin = new FileInputStream(prepack);
		BufferedInputStream in = new BufferedInputStream(fin);
		GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
		TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);
		TarArchiveEntry entry = null;
		while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
			// System.out.println("Extracting: " + entry.getName());
			String fileName = entry.getName();
			int strip = fileName.indexOf("/");
			fileName = fileName.substring(strip, fileName.length());
			File f = new File(
					rapidpromTargetLocation + File.separator + fileName);
			if (entry.isDirectory()) {
				f.mkdirs();
			} else {
				int count;
				byte data[] = new byte[buffer];
				FileOutputStream fos = new FileOutputStream(f);
				BufferedOutputStream dest = new BufferedOutputStream(fos,
						buffer);
				while ((count = tarIn.read(data, 0, buffer)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				fos.close();
			}
		}
		tarIn.close();
		gzIn.close();
		in.close();
		fin.close();
	}

	private File getPackageFolder() {
		return new File(RapidProMProperties.instance()
				.getRapidProMPackagesLocationString());
	}

	private File getIvyFile() {
		return new File(RapidProMProperties.instance()
				.getRapidProMPackagesLocationString() + File.separator
				+ RapidProMProperties.instance().getProperties()
						.getProperty("ivy_file_name_ext"));
	}

	private File getIvySettingsFile() {
		return new File(RapidProMProperties.instance()
				.getRapidProMPackagesLocationString() + File.separator
				+ RapidProMProperties.instance().getProperties()
						.getProperty("ivy_settings_file_name_ext"));
	}

	protected void runIvy() {
		String[] args = new String[8];
		args[0] = "-settings";
		args[1] = ivySettingsFile.getAbsolutePath();
		args[2] = "-ivy";
		args[3] = ivyFile.getAbsolutePath();
		args[4] = "-cache";
		args[5] = RapidProMProperties.instance()
				.getRapidProMPackagesLocationString();
		args[6] = "-m2compatible";
		args[7] = "-error";
		try {
			IvyStandAlone.invokeIvy(args, Boot.Level.ALL);
		} catch (IvyResolveException ire) {
			ire.printStackTrace();
			try {
				IvyStandAlone.invokeIvy(args, Boot.Level.ALL);
			} catch (Exception ire2) {
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(null,
						"Loading/Verifying the RapidProM extension failed. Please check your internet connection and restart RapidMiner",
						"Warning", JOptionPane.PLAIN_MESSAGE,
						JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]);
				System.exit(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected File unPackIvyFile(File toDir) {
		assert (Files.exists(toDir.toPath()));
		String ivyFileStr = RapidProMProperties.instance().getProperties()
				.getProperty("ivy_file_name_ext");
		InputStream ivyIS = RapidProMInitializer.class.getResourceAsStream(
				RapidProMProperties.instance().getProperties().getProperty(
						"ivy_file_location_in_archive") + ivyFileStr);
		File ivyFile = new File(
				toDir.getAbsolutePath() + File.separator + ivyFileStr);
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
		return fixOperatingSystemInIvyFile(ivyFile);
	}

	protected File fixOperatingSystemInIvyFile(File ivyFile) {
		Charset charset = StandardCharsets.UTF_8;
		String search = RapidProMProperties.IVY_OPERATING_SYSTEM_REGEX;
		String ivyConf = OSUtils.getOperatingSystem().getIvyConfiguration();
		String replacement = "defaultconf=\"" + ivyConf + "\"";
		try {
			String ivyContents = new String(
					Files.readAllBytes(ivyFile.toPath()), charset);
			ivyContents = ivyContents.replaceAll(search, replacement);
			Files.write(ivyFile.toPath(), ivyContents.getBytes(charset));
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
		File ivySettingsFile = new File(
				toDir.getAbsolutePath() + File.separator + ivySettingsStr);
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

	protected void unzipResources(File resourcesDir) {
		if (resourcesDir.exists() && resourcesDir.isDirectory()
				&& resourcesDir.canRead()) {
			for (File f : resourcesDir.listFiles()) {
				if (f.isDirectory()) {
					unzipResources(f);
				} else if (isZipFile(f)) {
					if (needToUnpackZipFileInDir(f, resourcesDir)) {
						unpackZipFileIntoDirectory(f, resourcesDir);
					}
				}
			}
		}
	}

	// as we are not guaranteed to have commons io, do a file name based check
	private boolean isZipFile(File f) {
		boolean result = false;
		int i = f.getName().lastIndexOf('.');
		if (i > 0) {
			if (f.getName().substring(i + 1).equals("zip")) {
				result = true;
			}
		}
		return result;
	}

	private boolean needToUnpackZipFileInDir(File zipFile, File dir) {
		boolean unpack = true;
		String zipName = zipFile.getName().substring(0,
				zipFile.getName().lastIndexOf('.'));
		for (File f : dir.listFiles()) {
			if (f.isDirectory() && f.getName().equals(zipName)) {
				unpack = false;
				break;
			}
		}
		return unpack;
	}

	/*
	 * Thanks to:
	 * http://www.codejava.net/java-se/file-io/programmatically-extract
	 * -a-zip-file-using-java
	 */
	private void unpackZipFileIntoDirectory(File zipFile, File dir) {
		try {
			File outputFolder = new File(
					dir.getCanonicalPath() + File.separator + zipFile.getName()
							.substring(0, zipFile.getName().lastIndexOf('.')));
			if (!outputFolder.exists()) {
				outputFolder.mkdir();
			}

			ZipInputStream zipIn = new ZipInputStream(
					new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry entry = zipIn.getNextEntry();
			while (entry != null) {
				String filePath = outputFolder + File.separator
						+ entry.getName();
				if (!entry.isDirectory()) {
					extractFile(zipIn, filePath);
				} else {
					File d = new File(filePath);
					d.mkdir();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			zipIn.closeEntry();
			zipIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Thanks to:
	 * http://www.codejava.net/java-se/file-io/programmatically-extract
	 * -a-zip-file-using-java
	 */
	private void extractFile(ZipInputStream zipIn, String filePath)
			throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(filePath));
		byte[] bytesIn = new byte[1024];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) > 0) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

}
