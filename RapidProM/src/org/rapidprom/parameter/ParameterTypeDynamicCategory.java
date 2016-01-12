package org.rapidprom.parameter;

import java.util.Arrays;
import java.util.List;

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
public abstract class ParameterTypeDynamicCategory<T>
		extends ParameterTypeCategory {

	private static final long serialVersionUID = 5610913750316933718L;

	private MetaDataProvider metaDataProvider;

	public MetaDataProvider getMetaDataProvider() {
		return metaDataProvider;
	}

	public void setMetaDataProvider(MetaDataProvider metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}

	public String[] getCategories() {
		return categories;
	}

	public void setCategories(String[] categories) {
		this.categories = categories;
	}

	public T[] getCorrespondingValues() {
		return correspondingValues;
	}

	public void setCorrespondingValues(T[] correspondingValues) {
		this.correspondingValues = correspondingValues;
	}

	public void setDefaultValue(int defaultValue) {
		this.defaultValueIndex = defaultValue;
	}

	private int defaultValueIndex = -1;

	private T defaultValue = null;

	private String[] categories = new String[0];

	private T[] correspondingValues;

	public ParameterTypeDynamicCategory(String key, String description,
			String[] categories, T[] correspondingValues, int defaultValueIndex,
			boolean expert, final InputPort inputPort) {
		this(key, description, categories, correspondingValues,
				defaultValueIndex, expert, new MetaDataProvider() {

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
			String[] categories, T[] correspondingValues, int defaultValueIndex,
			boolean expert, MetaDataProvider metaDataProvider) {
		super(key, description, categories, defaultValueIndex, expert);
		this.defaultValueIndex = defaultValueIndex;
		this.categories = categories;
		this.correspondingValues = correspondingValues;
		if (defaultValueIndex > -1
				&& defaultValueIndex < correspondingValues.length) {
			this.defaultValue = correspondingValues[defaultValueIndex];
		}
		this.metaDataProvider = metaDataProvider;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
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
		String[] strs = newValues.getFirst();
		T[] corVals = newValues.getSecond();
		if (defaultValue != null) {
			List<T> list = Arrays.asList(corVals);
			if (!list.contains(defaultValue)) {
				defaultValueIndex = 0;
				corVals = (T[]) Arrays.copyOf(newValues.getSecond(),
						newValues.getSecond().length + 1);
				System.arraycopy(corVals, 0, corVals, 1, corVals.length - 1);
				corVals[defaultValueIndex] = defaultValue;
				strs = Arrays.copyOf(newValues.getFirst(),
						newValues.getFirst().length + 1);
				System.arraycopy(strs, 0, strs, 1, strs.length - 1);
				strs[defaultValueIndex] = defaultValue.toString();
			} else {
				defaultValueIndex = list.indexOf(defaultValue);
			}
		}
		categories = strs;
		correspondingValues = corVals;
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
		return values.toString() + "; default: "
				+ categories[defaultValueIndex];
	}

	public int getNumberOfCategories() {
		return categories.length;
	}

}
