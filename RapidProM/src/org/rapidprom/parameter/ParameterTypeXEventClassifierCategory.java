package org.rapidprom.parameter;

import org.deckfour.xes.classification.XEventClassifier;
import org.rapidprom.operator.ports.metadata.XLogIOObjectMetaData;

import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.tools.container.Pair;

public class ParameterTypeXEventClassifierCategory extends
		ParameterTypeDynamicCategory<XEventClassifier> {

	private static final long serialVersionUID = 2977722407280721507L;

	public ParameterTypeXEventClassifierCategory(String key,
			String description, String[] categories,
			XEventClassifier[] correspondingValues, int defaultValue,
			boolean expert, InputPort inputPort) {
		super(key, description, categories, correspondingValues, defaultValue,
				expert, inputPort);
		this.correspondingValues = correspondingValues;

	}

	@Override
	protected Pair<String[], XEventClassifier[]> updateValues() {
		MetaData md = metaDataProvider.getMetaData();
		if (md != null) {
			assert (md instanceof XLogIOObjectMetaData);
			XLogIOObjectMetaData mdC = (XLogIOObjectMetaData) md;
			if (!(mdC.getXEventClassifiers().isEmpty())) {
				categories = new String[mdC.getXEventClassifiers().size()];
				correspondingValues = new XEventClassifier[mdC
						.getXEventClassifiers().size()];
				int i = 0;
				for (XEventClassifier e : mdC.getXEventClassifiers()) {
					correspondingValues[i] = e;
					categories[i] = e.toString();
					i++;
				}
				System.out
						.println("We should now be able to read the classifiers!");
				System.out.println(categories);
			}
			return new Pair<String[], XEventClassifier[]>(categories,
					correspondingValues);
		} else {
			return new Pair<String[], XEventClassifier[]>(categories,
					correspondingValues);
		}
	}

}
