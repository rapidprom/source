package org.rapidprom.ioobjectrenderers;

import java.io.IOException;

import javax.swing.JComponent;
import javax.xml.parsers.ParserConfigurationException;

import org.cpntools.accesscpn.model.importer.NetCheckException;
import org.processmining.plugins.cpnet.DrawCPNGraph;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.CPNModelIOObject;
import org.xml.sax.SAXException;

public class CPNModelIOObjectRenderer extends AbstractRapidProMIOObjectRenderer<CPNModelIOObject> {

	@Override
	public String getName() {
		return "CPNModel renderer";
	}

	@Override
	protected JComponent runVisualization(CPNModelIOObject artifact) {
		DrawCPNGraph visualizer = new DrawCPNGraph();
		JComponent result = null;
		try {
			result = visualizer.visualize(artifact.getPluginContext(), artifact.getArtifact());
		} catch (NetCheckException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	
}
