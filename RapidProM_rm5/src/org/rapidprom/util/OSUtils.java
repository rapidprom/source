package org.rapidprom.util;

import java.io.File;
import java.io.IOException;

import sun.management.ManagementFactoryHelper;

import com.sun.management.OperatingSystemMXBean;

public class OSUtils {

	// hooray for eclipse auto formatting!!! triangular style, i love it.
	public enum OperatingSystem {
		WINDOWS_32("windows", true, "win32"), WINDOWS_64("windows", false,
				"win64"), LINUX_32("linux", true, "lin32"), LINUX_64("linux",
						false,
						"lin64"), OS_X_32("mac os x", true, "mac32"), OS_X_64(
								"mac os x", false,
								"mac64"), BSD_32("bsd", true, "lin32"), BSD_64(
										"bsd", false,
										"lin64"), RISCOS_32("risc os", true,
												"lin32"), RISCOS_64("risc os",
														false,
														"lin64"), BEOS_32(
																"beos", true,
																"lin32"), BEOS_64(
																		"beos",
																		false,
																		"lin64"), UNKNOWN(
																				"unknown",
																				false,
																				"unknown");

		private final String name;
		private final boolean is32Bit;
		private final String ivyConfig;

		private OperatingSystem(final String name, final boolean _32Bit,
				final String ivyConfig) {
			this.name = name;
			is32Bit = _32Bit;
			this.ivyConfig = ivyConfig;
		}

		public String getName() {
			return name;
		}

		public boolean is32Bit() {
			return is32Bit;
		}

		public String getIvyConfiguration() {
			return ivyConfig;
		}
	}

	private static OperatingSystem currentOs = null;

	/**
	 * Note: this function specifies the number of bits based on the JVM, not on
	 * the underlying architecture
	 * 
	 * @return
	 */
	public static OperatingSystem getOperatingSystem() {
		if (currentOs == null) {
			String osString = System.getProperty("os.name").trim()
					.toLowerCase();
			if (osString.startsWith("windows")) {
				if (is32Bit()) {
					currentOs = OperatingSystem.WINDOWS_32;
				} else {
					currentOs = OperatingSystem.WINDOWS_64;
				}
			} else if (osString.startsWith("mac os x")) {
				if (is32Bit()) {
					currentOs = OperatingSystem.OS_X_32;
				} else {
					currentOs = OperatingSystem.OS_X_64;
				}
			} else if (osString.startsWith("risc os")) {
				if (is32Bit()) {
					currentOs = OperatingSystem.RISCOS_32;
				} else {
					currentOs = OperatingSystem.RISCOS_64;
				}
			} else if ((osString.indexOf("linux") >= 0)
					|| (osString.indexOf("debian") >= 0)
					|| (osString.indexOf("redhat") >= 0)
					|| (osString.indexOf("lindows") >= 0)) {
				if (is32Bit()) {
					currentOs = OperatingSystem.LINUX_32;
				} else {
					currentOs = OperatingSystem.LINUX_64;
				}
			} else if ((osString.indexOf("freebsd") >= 0)
					|| (osString.indexOf("openbsd") >= 0)
					|| (osString.indexOf("netbsd") >= 0)
					|| (osString.indexOf("irix") >= 0)
					|| (osString.indexOf("solaris") >= 0)
					|| (osString.indexOf("sunos") >= 0)
					|| (osString.indexOf("hp/ux") >= 0)
					|| (osString.indexOf("risc ix") >= 0)
					|| (osString.indexOf("dg/ux") >= 0)) {
				if (is32Bit()) {
					currentOs = OperatingSystem.BSD_32;
				} else {
					currentOs = OperatingSystem.BSD_64;
				}
			} else if (osString.indexOf("beos") >= 0) {
				if (is32Bit()) {
					currentOs = OperatingSystem.BEOS_32;
				} else {
					currentOs = OperatingSystem.BEOS_64;
				}
			} else {
				currentOs = OperatingSystem.UNKNOWN;
			}
		}
		return currentOs;
	}

	private static boolean is32Bit() {
		return System.getProperty("sun.arch.data.model").equals("32");
	}

	public static void setWorkingDirectoryAtStartup() {
		OperatingSystem o = getOperatingSystem();
		if (o.equals(OperatingSystem.OS_X_32)
				|| o.equals(OperatingSystem.OS_X_64)) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			File here = new File(".");
			try {
				if (new File(here.getAbsolutePath() + "/ProM.app").exists()) {
					System.out.println(
							"--> Mac OS X: running from application bundle (1).");
					File nextHere = new File(here.getCanonicalPath()
							+ "/ProM.app/Contents/Resources/ProMhome");
					System.setProperty("user.dir", nextHere.getCanonicalPath());
				} else if (here.getAbsolutePath()
						.matches("^(.*)ProM\\.app(/*)$")) {
					System.out.println(
							"--> Mac OS X: running from application bundle (2).");
					File nextHere = new File(here.getCanonicalPath()
							+ "/Contents/Resources/ProMhome");
					System.setProperty("user.dir", nextHere.getCanonicalPath());
				}
				System.out.println("Mac OS X: Working directory set to "
						+ System.getProperty("user.dir") + " (from "
						+ here.getAbsolutePath() + ")");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Mac OS X: Working directory set to "
					+ System.getProperty("user.dir") + " (from "
					+ here.getAbsolutePath() + ")");
		}
	}

	public static long getPhysicalMemory() {
		OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactoryHelper
				.getOperatingSystemMXBean();
		return operatingSystemMXBean.getTotalPhysicalMemorySize();
	}
}
