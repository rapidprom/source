package org.rapidprom.external.connectors.prom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FilenameUtils;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageSet;
import org.processmining.framework.packages.events.PackageManagerListener;
import org.rapidprom.properties.RapidProMProperties;

public class ProMIvyBasedPackageManager {

	private static ProMIvyBasedPackageManager instance = null;

	private final PackageManagerListener.ListenerList listeners = new PackageManagerListener.ListenerList();

	private final PackageSet packages = new PackageSet();

	private ProMIvyBasedPackageManager() {
	}

	public static ProMIvyBasedPackageManager getInstance() {
		if (instance == null) {
			instance = new ProMIvyBasedPackageManager();
		}
		return instance;
	}

	public void addListener(PackageManagerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PackageManagerListener listener) {
		listeners.remove(listener);
	}

	public File getPackagesDirectory() {
		return RapidProMProperties.instance().getProMPackagesLocation();
	}

	public void initialize(Boot.Level verbose) {
		if (verbose == Level.ALL) {
			System.out.println(">>> Loading packages from "
					+ getPackagesDirectory().getAbsolutePath());
		}
		try {
			packages.clear();
			for (File promPackageDir : getPackagesDirectory().listFiles()) {
				for (File promPackageFile : promPackageDir.listFiles()) {
					if (isIvyFile(promPackageFile)) {
						BufferedInputStream bis = new BufferedInputStream(
								new FileInputStream(promPackageFile));
						IvyBasedProMPackageSAXHandler handler = new IvyBasedProMPackageSAXHandler(
								promPackageDir);
						SAXParserFactory parserFactory = SAXParserFactory
								.newInstance();

						parserFactory.setNamespaceAware(false);
						parserFactory.setValidating(false);
						try {
							parserFactory.setSchema(null);
						} catch (UnsupportedOperationException ex) {
						}

						SAXParser parser = parserFactory.newSAXParser();
						parser.parse(bis, handler);
						bis.close();
						if (verbose.equals(Boot.Level.ALL)) {
							System.out.println("Registering "
									+ handler.getPackageDescriptor().getName()
									+ " as a RapidProM plugin.");
						}
						packages.add(handler.getPackageDescriptor());
					}
				}
			}
		} catch (Exception e) {
			listeners.fireException(e);
		}
	}

	private boolean isIvyFile(File f) {
		String fN = f.getName();
		String[] fNSplit = fN.split("\\.");
		return fN.contains("ivy") && fNSplit[fNSplit.length - 1].equals("xml");
	}

	public PackageSet getPackages() {
		return packages;
	}

}
