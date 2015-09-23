package org.rapidprom.external.connectors.prom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.packages.PackageDescriptor.OS;
import org.rapidprom.properties.RapidProMProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX2 handler for parsing ProM package information from an ivy.xml file. The
 * handler assumes that a ProM package has a dedicated ivy.xml file.
 * 
 * Tags that are supported by the handler: <ivy-module /> <info /> <dependencies
 * />
 * 
 * 
 * @author svzelst
 *
 */
public class IvyBasedProMPackageSAXHandler extends DefaultHandler {

	/* ATTRIBUTES */
	private static final String RPROM_NS = RapidProMProperties.instance()
			.getProperties().getProperty("rapidprom_namespace")
			+ ":";

	private static final String DEPENDENCIES_TAG = "dependencies";

	private static final String DEPENDENCY_TAG = "dependency";
	private static final String INFO_TAG = "info";
	private static final String DEPENDENCY_ORG_ATTRIBUTE = "org";
	/* TAGS */
	private static final String IVY_MODULE_TAG = "ivy-module";
	private static final String PACKAGE_AUTHOR_ATTRIBUTE = RPROM_NS + "author";

	private static final String PACKAGE_AUTO_INSTALLED_ATTRIBUTE = RPROM_NS
			+ "auto";
	private static final String PACKAGE_DESCRIPTION_ATTRIBUTE = RPROM_NS
			+ "desc";
	private static final String PACKAGE_HAS_PLUGINS_ATTRIBUTE = RPROM_NS
			+ "hasPlugins";
	private static final String PACKAGE_LICENSE_ATTRIBUTE = RPROM_NS
			+ "license";
	private static final String PACKAGE_LOGO_URL_ATTRIBUTE = RPROM_NS + "logo";
	private static final String PACKAGE_MAINTAINER_ATTR = "maintainer";
	private static final String PACKAGE_NAME_ATTRIBUTE = "module";
	private static final String PACKAGE_ORGANISATION_ATTRIBUTE = RPROM_NS
			+ "org";
	private static final String PACKAGE_OS_ATTRIBUTE = RPROM_NS + "os";
	private static final String PACKAGE_URL_ATTRIBUTE = RPROM_NS + "url";
	private static final String PACKAGE_VERSION_ATTRIBUTE = "revision";

	private final List<String> dependencies = new ArrayList<String>();

	private boolean isInDependencies = false;
	private String packageLogoURL = null;
	private String packageAuthor = null;
	private String packageAutoInstalled = null;
	private String packageDesc = null;
	private String packageHasPlugins = null;
	private String packageLicense = null;
	private String packageMaintainer = null;
	private String packageName = null;
	private String packageOrg = null;
	private String packageOS = null;
	private String packageURL = null;
	private String packageVersion = null;

	private RapidProMPackageDescriptor packageDescriptor = null;
	private final File packageDirectory;

	public IvyBasedProMPackageSAXHandler(final File packageDirectory) {
		this.packageDirectory = packageDirectory;
	}

	@Override
	public void startElement(String uri, String local, String qName,
			Attributes attributes) throws SAXException {
		qName = qName.toLowerCase();
		if (qName.equals(INFO_TAG)) {
			readInfoTag(uri, local, qName, attributes);
		}
		if (qName.equals(DEPENDENCIES_TAG)) {
			isInDependencies = true;
		}
		if (isInDependencies && qName.equals(DEPENDENCY_TAG)) {
			readDependency(uri, local, qName, attributes);
		}
	}

	@Override
	public void endElement(String uri, String local, String qName)
			throws SAXException {
		qName = qName.toLowerCase();
		if (qName.equals(IVY_MODULE_TAG)) {
			packageDescriptor = new RapidProMPackageDescriptor(packageName,
					packageVersion, OS.fromString(packageOS), packageDesc,
					packageOrg, packageAuthor, packageMaintainer,
					packageLicense, packageURL, packageLogoURL,
					"true".equals(packageAutoInstalled),
					"true".equals(packageHasPlugins), dependencies,
					new ArrayList<String>(), packageDirectory);
		}
	}

	private void readDependency(String uri, String local, String qName,
			Attributes attributes) {
		if (attributes.getValue(DEPENDENCY_ORG_ATTRIBUTE) != null
				&& attributes.getValue(DEPENDENCY_ORG_ATTRIBUTE).equals(
						RapidProMProperties.instance().getProperties()
								.getProperty("rapidprom_organisation"))) {
			if (attributes.getValue("name") != null) {
				dependencies.add(attributes.getValue("name"));
			}
		}
	}

	private void readInfoTag(String uri, String local, String qName,
			Attributes attributes) {
		packageName = attributes.getValue(PACKAGE_NAME_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_NAME_ATTRIBUTE);
		packageVersion = attributes.getValue(PACKAGE_VERSION_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_VERSION_ATTRIBUTE);
		packageURL = attributes.getValue(PACKAGE_URL_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_URL_ATTRIBUTE);
		packageLogoURL = attributes.getValue(PACKAGE_LOGO_URL_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_LOGO_URL_ATTRIBUTE);
		packageDesc = attributes.getValue(PACKAGE_DESCRIPTION_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_DESCRIPTION_ATTRIBUTE);
		packageOrg = attributes.getValue(PACKAGE_ORGANISATION_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_ORGANISATION_ATTRIBUTE);
		packageLicense = attributes.getValue(PACKAGE_LICENSE_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_LICENSE_ATTRIBUTE);
		packageAuthor = attributes.getValue(PACKAGE_AUTHOR_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_AUTHOR_ATTRIBUTE);
		packageAutoInstalled = attributes
				.getValue(PACKAGE_AUTO_INSTALLED_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_AUTO_INSTALLED_ATTRIBUTE);
		packageHasPlugins = attributes.getValue(PACKAGE_HAS_PLUGINS_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_HAS_PLUGINS_ATTRIBUTE);
		packageOS = attributes.getValue(PACKAGE_OS_ATTRIBUTE) == null ? ""
				: attributes.getValue(PACKAGE_OS_ATTRIBUTE);
		packageMaintainer = attributes.getValue(PACKAGE_MAINTAINER_ATTR) == null ? ""
				: attributes.getValue(PACKAGE_MAINTAINER_ATTR);
	}

	public RapidProMPackageDescriptor getPackageDescriptor() {
		return packageDescriptor;
	}
}
