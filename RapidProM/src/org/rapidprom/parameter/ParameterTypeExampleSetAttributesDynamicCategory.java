package org.rapidprom.parameter;

import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.tools.container.Pair;

public class ParameterTypeExampleSetAttributesDynamicCategory
		extends ParameterTypeDynamicCategory<String> {

	private static final long serialVersionUID = -6944684403198889196L;

	public ParameterTypeExampleSetAttributesDynamicCategory(String key,
			String description, String[] categories,
			String[] correspondingValues, int defaultValue, boolean expert,
			final InputPort inputPort) {
		super(key, description, categories, correspondingValues, defaultValue,
				expert, inputPort);
	}

	@Override
	protected Pair<String[], String[]> updateValues() {
		MetaData md = getMetaDataProvider().getMetaData();
		// sometimes, the metadata can be null although it is actually not.
		// suspending the thread for a while appears to be overcoming this
		// problem.
		// this is an ugly fix, yet it works. The problem appears in RM 5 and RM
		// 7.
		if (md == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			md = getMetaDataProvider().getMetaData();
		}
		if (md != null && md instanceof ExampleSetMetaData) {
			String[] categories = null;
			ExampleSetMetaData mdc = (ExampleSetMetaData) md;
			if (!mdc.getAllAttributes().isEmpty()) {
				categories = new String[mdc.getAllAttributes().size()];
				int i = 0;
				for (AttributeMetaData amd : mdc.getAllAttributes()) {
					categories[i] = amd.getName();
					i++;
				}
				return new Pair<String[], String[]>(categories, categories);
			}
		}
		// restore default
		return new Pair<String[], String[]>(getDefaultValuesToString(),
				getDefaultValues());
	}

}
