package org.rapidprom;

import org.rapidprom.external.connectors.prom.ProMLibraryManager;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.util.RapidProMUtils;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.tools.plugin.PluginException;

/**
 * This class provides hooks for initialization
 * 
 * @author Sebastian Land
 * @author svzelst
 */
public class RapidProMInitializer {

	/**
	 * The last hook before the splash screen is closed. Third in the row.
	 */
	public static void initFinalChecks() {
	}

	/**
	 * This method is called during start up as the second hook. It is called
	 * before the gui of the mainframe is created. The Mainframe is given to
	 * adapt the gui. The operators and renderers have been registered in the
	 * meanwhile.
	 */
	public static void initGui(MainFrame mainframe) {
	}

	/**
	 * This method will be called directly after the extension is initialized.
	 * This is the first hook during start up. No initialization of the
	 * operators or renderers has taken place when this is called.
	 * 
	 * We re-register the renderers because at startup they get loaded without the needed classes
	 * The re-registering of operators is commented as it causes them to get loaded twice
	 */
	public static void initPlugin() {
		// !DO NOT MOVE THE CALLS FROM THIS HOOK!
		(new ProMLibraryManager()).startAndWait();
		ProMPluginContextManager.instance().startAndWait();

		try {
			RapidProMUtils.getRapidProMPlugin().registerDescriptions();
//			RapidProMUtils.getRapidProMPlugin().registerOperators();
		} catch (PluginException e) {
			e.printStackTrace();
		}
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
