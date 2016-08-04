package org.rapidprom.operators.experimental;

import com.rapidminer.gui.wizards.AbstractConfigurationWizardCreator;
import com.rapidminer.gui.wizards.ConfigurationListener;
import com.rapidminer.parameter.ParameterType;

public class DAPOQLangDialogCreator extends AbstractConfigurationWizardCreator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8650517055293020063L;

	@Override
	public String getI18NKey() {
		return "configure";
	}

	@Override
	public void createConfigurationWizard(ParameterType type,
			ConfigurationListener listener) {
		(new DAPOQLangDialog(type, listener)).setVisible(true);
	}

}
