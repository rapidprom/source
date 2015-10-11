package org.rapidprom.parameter;

import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.MetaDataChangeListener;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.MetaDataProvider;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.container.Pair;

/**
 * The ParameterTypeDynamicCategory, allows us to change the contents of a
 * {@link ParameterTypeCategory}, depending on some inputPort's meta data.
 * Additionally this class introduces the option to query, given the selected
 * index, what the corresponding object is.
 * 
 * @author svzelst
 *
 * @param <T>
 *            indicates what underlying java object the user wants to choose.
 */
public abstract class ParameterTypeDynamicCategory<T> extends
		ParameterTypeCategory {

	private static final long serialVersionUID = 5610913750316933718L;

	protected MetaDataProvider metaDataProvider;

	protected int defaultValue;

	protected String[] categories = new String[0];

	protected T[] correspondingValues;

	public ParameterTypeDynamicCategory(String key, String description,
			String[] categories, T[] correspondingValues, int defaultValue,
			boolean expert, final InputPort inputPort) {
		this(key, description, categories, correspondingValues, defaultValue,
				expert, new MetaDataProvider() {

					@Override
					public void removeMetaDataChangeListener(
							MetaDataChangeListener l) {
						inputPort.removeMetaDataChangeListener(l);

					}

					@Override
					public MetaData getMetaData() {
						if (inputPort != null) {
							return inputPort.getMetaData();
						} else {
							return null;
						}
					}

					@Override
					public void addMetaDataChangeListener(
							MetaDataChangeListener l) {
						inputPort.registerMetaDataChangeListener(l);

					}
				});
	}

	public ParameterTypeDynamicCategory(String key, String description,
			String[] categories, T[] correspondingValues, int defaultValue,
			boolean expert, MetaDataProvider metaDataProvider) {
		super(key, description, categories, defaultValue, expert);
		this.defaultValue = defaultValue;
		this.categories = categories;
		this.correspondingValues = correspondingValues;
		this.metaDataProvider = metaDataProvider;
	}

	@Override
	public Object getDefaultValue() {
		if (defaultValue == -1) {
			return null;
		} else {
			return categories[defaultValue];
		}
	}

	public String getCategory(int index) {
		return categories[index];
	}

	public int getIndex(String string) {
		for (int i = 0; i < categories.length; i++) {
			if (categories[i].equals(string)) {
				return Integer.valueOf(i);
			}
		}
		// try to interpret string as number
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	public String toString(Object value) {
		try {
			if (value == null)
				return null;
			int index = Integer.parseInt(value.toString());
			if (index >= categories.length)
				return "";
			return super.toString(categories[index]);
		} catch (NumberFormatException e) {
			return super.toString(value);
		}
	}

	protected abstract Pair<String[], T[]> updateValues();

	public T getCorrespondingObjectForIndex(int i) {
		return correspondingValues[i];
	}

	@Override
	public String[] getValues() {
		Pair<String[], T[]> newValues = updateValues();
		categories = newValues.getFirst();
		correspondingValues = newValues.getSecond();
		return categories;
	}

	@Override
	public String getRange() {
		StringBuffer values = new StringBuffer();
		for (int i = 0; i < categories.length; i++) {
			if (i > 0)
				values.append(", ");
			values.append(categories[i]);
		}
		return values.toString() + "; default: " + categories[defaultValue];
	}

	public int getNumberOfCategories() {
		return categories.length;
	}

}
