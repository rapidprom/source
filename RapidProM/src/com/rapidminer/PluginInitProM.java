/**
 * 
 */
package com.rapidminer;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.rapidminer.callprom.ClassLoaderUtils;
import com.rapidminer.configuration.GlobalProMParameters;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.parameter.ParameterTypeDirectory;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.util.InstallProM;

/**
 * This class provides hooks for initialization
 * 
 * @author Sebastian Land
 */
public class PluginInitProM {

	/**
	 * This method will be called directly after the extension is initialized.
	 * This is the first hook during start up. No initialization of the
	 * operators or renderers has taken place when this is called.
	 */
	public static void initPlugin() {
		// ProMConfigurator config = new ProMConfigurator();
		// ConfigurationManager.getInstance().register(config);
		// // register prom location
		ParameterService.registerParameter(new ParameterTypeDirectory(
				"prom_folder",
				"Points to where RapidProM packages are installed.", false),
				"ProM framework");
		// loadRequiredClasses();
	}

	/**
	 * This method is called during start up as the second hook. It is called
	 * before the gui of the mainframe is created. The Mainframe is given to
	 * adapt the gui. The operators and renderers have been registered in the
	 * meanwhile.
	 */
	public static void initGui(MainFrame mainframe) {
		String parameterValue = ParameterService
				.getParameterValue("prom_folder");
		System.out.println("location of prom_folder:" + parameterValue);
		if (!parameterValue.equals("")) {
			// be happy, ProM is alive
			GlobalProMParameters instance = GlobalProMParameters.getInstance();
			instance.setProMLocation(parameterValue);
		} else {
			System.out.println("ProM6 not installed");
			// show dialog
			int showConfirmDialog = JOptionPane
					.showConfirmDialog(
							mainframe,
							"We did not detect RapidProM packages, have you installed them before?",
							"Warning", JOptionPane.YES_NO_OPTION);
			if (showConfirmDialog == JOptionPane.YES_OPTION) {
				final JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(false);
				int showDialog = fc.showDialog(mainframe,
						"Please select the location of the ProM.ini file");
				if (showDialog == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fc.getSelectedFile();
					String path = selectedFile.getPath();
					ParameterService.setParameterValue("prom_folder", path);
					ParameterService.saveParameters();
					GlobalProMParameters instance = GlobalProMParameters
							.getInstance();
					instance.setProMLocation(path);
				}
			} else {
				// it is no. install ProM
				try {
					// open a dialog for selecting a folder
					JFileChooser folderChooser = new JFileChooser();
					folderChooser.setCurrentDirectory(new java.io.File("."));
					folderChooser
							.setDialogTitle("Select a folder to install the RapidProM packages");
					folderChooser
							.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					//
					// disable the "All files" option.
					//
					folderChooser.setAcceptAllFileFilterUsed(false);
					//
					if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						InstallProM ip = new InstallProM(folderChooser,
								mainframe);
						ip.start();
					} else {
						System.out.println("No Selection");
						// custom title, error icon
						JOptionPane
								.showMessageDialog(
										null,
										"RapidProM couldn't be installed. Please refer to the user guide for troubleshooting.",
										"Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * The last hook before the splash screen is closed. Third in the row.
	 */
	public static void initFinalChecks() {
	}

	/**
	 * Will be called as fourth method, directly before the UpdateManager is
	 * used for checking updates. Location for exchanging the UpdateManager. The
	 * name of this method unfortunately is a result of a historical typo, so
	 * it's a little bit misleading.
	 */
	public static void initPluginManager() {
	}
}
