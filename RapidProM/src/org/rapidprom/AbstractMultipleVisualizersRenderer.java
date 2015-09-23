package org.rapidprom;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EnumSet;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.operator.IOContainer;

public abstract class AbstractMultipleVisualizersRenderer<E extends Enum<E>>
		extends AbstractRenderer {

	protected final JPanel container = new JPanel();

	protected final String name;

	protected final JComboBox<E> visualizersJCombo = new JComboBox<E>();

	protected final EnumSet<E> visualizerTypes;

	Object renderable = null;
	IOContainer ioContainer = null;

	public AbstractMultipleVisualizersRenderer(EnumSet<E> visualizerTypes,
			String name) {
		this.visualizerTypes = visualizerTypes;
		this.name = name;
		setupVisualizerSelector();
	}

	private void setupVisualizerSelector() {
		for (E visualizerType : visualizerTypes) {
			visualizersJCombo.addItem(visualizerType);
		}
		visualizersJCombo.addItemListener(new ItemListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED
						&& renderable != null) {
					setupRenderer(visualizeRendererOption(
							(E) visualizersJCombo.getSelectedItem(),
							renderable, ioContainer));
				}
			}
		});
	}

	@Override
	public String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getVisualizationComponent(Object renderable,
			IOContainer ioContainer) {
		this.renderable = renderable;
		this.ioContainer = ioContainer;
		return setupRenderer(visualizeRendererOption(
				(E) visualizersJCombo.getSelectedItem(), renderable,
				ioContainer));

	}

	private Component setupRenderer(Component objectVisualizer) {
		container.removeAll();
		container.setLayout(new BorderLayout());
		container.add(visualizersJCombo, BorderLayout.NORTH);
		container.add(objectVisualizer, BorderLayout.CENTER);
		container.revalidate();
		container.repaint();
		return container;
	}

	protected abstract Component visualizeRendererOption(E visualizerType,
			Object renderable, IOContainer ioContainer);
}
