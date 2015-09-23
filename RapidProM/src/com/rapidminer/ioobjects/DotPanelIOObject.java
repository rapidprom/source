package com.rapidminer.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import com.rapidminer.operator.ResultObjectAdapter;

public class DotPanelIOObject extends ResultObjectAdapter implements ProMIOObject {

		private static final long serialVersionUID = 1L;

		private PluginContext pc = null;
		private DotPanel panel = null;

		public DotPanelIOObject (DotPanel panel) {
			this.panel = panel;
		}

		public void setPluginContext (PluginContext pc) {
			this.pc = pc;
		}

		public PluginContext getPluginContext () {
			return this.pc;
		}

		public void setDotPanel(DotPanel panel) {
			this.panel = panel;
		}

		public DotPanel getDotPanel() {
			return panel;
		}

		public String toResultString() {
			String extractName = panel.toString();
			return "DotPanelIOObject:" + extractName;
		}

		public DotPanel getData() {
			return panel;
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}

	}