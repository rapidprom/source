package org.rapidprom.management;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.tar.TarOutputStream;

/**
 * Used some code from:
 * http://www.mkyong.com/java/how-to-copy-directory-in-java/
 * http://stackoverflow.com/questions/13461393/compress-directory-to-tar-gz-with
 * -commons-compress
 * 
 * @author svzelst
 *
 */
public class CreateIvyPrePack {

	private final File libUserFolder;
	private final File promLibGitFolder;

	public CreateIvyPrePack(File libUserFolder, File promLibGitFolder) {
		this.libUserFolder = libUserFolder;
		this.promLibGitFolder = promLibGitFolder;
	}

	public void run() {
		try {
			copyFolder(libUserFolder, promLibGitFolder);
			System.out.println("===== copy source file done =====");
			String dirPath = promLibGitFolder.getPath();
			String tarGzPath = promLibGitFolder.getPath() + ".tar.gz";
			FileOutputStream fOut = new FileOutputStream(new File(tarGzPath));
			BufferedOutputStream bOut = new BufferedOutputStream(fOut);
			GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(
					bOut);
			TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut);
			tOut.setLongFileMode(TarOutputStream.LONGFILE_GNU);
			addFileToTarGz(tOut, dirPath, "");
			tOut.finish();
			tOut.close();
			gzOut.close();
			bOut.close();
			fOut.close();
			System.out.println("===== compression done =====");
			System.out.println("===== deleting intermediary folder =====");
			FileUtils.deleteDirectory(promLibGitFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addFileToTarGz(TarArchiveOutputStream tOut, String path,
			String base) throws IOException {
		File f = new File(path);
		System.out.println(f.exists());
		String entryName = base + f.getName();
		TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
		tOut.putArchiveEntry(tarEntry);

		if (f.isFile()) {
			FileInputStream in = new FileInputStream(f);
			IOUtils.copy(in, tOut);
			in.close();
			tOut.closeArchiveEntry();
		} else {
			tOut.closeArchiveEntry();
			File[] children = f.listFiles();
			if (children != null) {
				for (File child : children) {
					System.out.println(child.getName());
					addFileToTarGz(tOut, child.getAbsolutePath(),
							entryName + "/");
				}
			}
		}
	}

	private void copyFolder(File src, File target) throws IOException {
		if (src.isDirectory()) {
			if (!target.exists()) {
				target.mkdir();
				System.out.println(
						"Directory copied from " + src + "  to " + target);
			}
			for (String file : src.list()) {
				File srcFile = new File(src, file);
				File destFile = new File(target, file);
				copyFolder(srcFile, destFile);
			}
			// don't copy the ivy / ivy-settings file, we do that when running
			// RapidProM for the first time
		} else if (!target.getParent().equals(promLibGitFolder.getPath())) {
			String ext = FilenameUtils.getExtension(src.getPath());
			if (!ext.equals("jar") && 	!ext.equals("zip") && !ext.equals("dll") && !ext.equals("exe")) {
				InputStream in = new FileInputStream(src);
				OutputStream out = new FileOutputStream(target);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
				in.close();
				out.close();
				System.out.println("File copied from " + src + " to " + target);
			}
		}

	}

	/**
	 * 
	 * @param args
	 *            [1] source, [2] target, [3] version
	 */
	public static void main(String[] args) {
		File libUserFolder = new File(args[0]);
		File promLibGitFolder = new File(args[1] + File.separator + args[2]);
		CreateIvyPrePack script = new CreateIvyPrePack(libUserFolder,
				promLibGitFolder);
		script.run();
	}

}
