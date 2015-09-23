package org.rapidprom.external.connectors.prom;

import java.io.File;
import java.util.List;

import org.processmining.framework.packages.PackageDescriptor;

public class RapidProMPackageDescriptor extends PackageDescriptor {

	private final File local;

	public RapidProMPackageDescriptor(String name, String version, OS os,
			String description, String organisation, String author,
			String maintainer, String license, String url, String logoUrl,
			boolean autoInstalled, boolean hasPlugins,
			List<String> dependencies, List<String> conflicts,
			File localPackageDirectory) {
		super(name, version, os, description, organisation, author, maintainer,
				license, url, logoUrl, autoInstalled, hasPlugins, dependencies,
				conflicts);
		local = localPackageDirectory;
	}

	@Override
	public File getLocalPackageDirectory() {
		return local;
	}

}
