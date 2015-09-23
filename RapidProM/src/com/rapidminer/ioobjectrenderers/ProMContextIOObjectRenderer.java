package com.rapidminer.ioobjectrenderers;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.rapidminer.gui.renderer.AbstractTableModelTableRenderer;
import com.rapidminer.operator.IOContainer;

public class ProMContextIOObjectRenderer extends AbstractTableModelTableRenderer {
	@Override
	public String getName() {
		return "Extracted Values";
	}

	@Override
	public TableModel getTableModel(Object renderable, IOContainer ioContainer, boolean isReporting) {
		
		return new DefaultTableModel();
	}
	
	@Override
	public boolean isSortable() {
		return false;
	}
	
	@Override
	public boolean isAutoresize() {
		return false;
	}
	
	@Override
	public boolean isColumnMovable() {
		return true;
	}
}
