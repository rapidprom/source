package com.rapidminer.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;

import com.rapidminer.callprom.ClassLoaderUtils;
import com.rapidminer.configuration.GlobalProMParameters;

public class Utilities {

	public static void loadRequiredClasses() {
		GlobalProMParameters instance = GlobalProMParameters.getInstance();
		String promLocationStr = instance.getProMLocation();
		// throw away the .ini
		promLocationStr = promLocationStr.replaceAll("ProM.ini", "");
		promLocationStr = promLocationStr + "packages";
		System.out.println("Load required classes:" + promLocationStr);
		ClassLoaderUtils.loadJarsFromDir(new File(promLocationStr));
	}

	public static ProMJGraph getSizedGraph(ProMJGraphPanel promPanel,
			int desiredWidth, int desiredHeight) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(promPanel);
		frame.setSize(desiredWidth, desiredHeight);
		frame.setPreferredSize(new Dimension(desiredWidth, desiredHeight));
		frame.setMinimumSize(new Dimension(desiredWidth, desiredHeight));
		frame.setMaximumSize(new Dimension(desiredWidth, desiredHeight));

		// testing it
		promPanel.scaleToFit();

		frame.pack();
		frame.revalidate();
		frame.repaint();
		frame.setVisible(true);
		// sleep for a while
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close the frame
		// frame.dispatchEvent(new WindowEvent(frame,
		// WindowEvent.WINDOW_CLOSING));
		return promPanel.getGraph();
	}

	public static Component getSizedPanel(Component fullComponent,
			Component panel, int desiredWidth, int desiredHeight) {
		// panel.setSize(new Dimension(desiredWidth,desiredHeight));
		// panel.setMaximumSize(new Dimension(desiredWidth,desiredHeight));
		// RapidMiner.init();
		JFrame frame = new JFrame();
		frame.getContentPane().add(panel);
		frame.setSize(desiredWidth, desiredHeight);
		frame.setPreferredSize(new Dimension(desiredWidth, desiredHeight));
		frame.setMinimumSize(new Dimension(desiredWidth, desiredHeight));
		frame.setMaximumSize(new Dimension(desiredWidth, desiredHeight));
		frame.pack();
		frame.revalidate();
		frame.repaint();
		frame.setVisible(true);
		// put a JDialog for indicating when done.
		// JOptionPane.showMessageDialog(null,
		// "Click on OK button when done with laying out the Dotted Chart.");
		// sleep for a while
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close the frame
		// frame.dispatchEvent(new WindowEvent(frame,
		// WindowEvent.WINDOW_CLOSING));
		return panel;
	}

	public static JPanel getSizedPanelBlock(JComponent fullComponent,
			JPanel panel, int desiredWidth, int desiredHeight) {
		// panel.setSize(new Dimension(desiredWidth,desiredHeight));
		// //panel.setMinimumSize(new Dimension(desiredWidth,desiredHeight));
		// panel.setMaximumSize(new Dimension(desiredWidth,desiredHeight));

		final JFrame frame = new JFrame();
		frame.getContentPane().add(fullComponent);
		frame.setSize(desiredWidth, desiredHeight);
		frame.setPreferredSize(new Dimension(desiredWidth, desiredHeight));
		frame.setMinimumSize(new Dimension(desiredWidth, desiredHeight));
		frame.setMaximumSize(new Dimension(desiredWidth, desiredHeight));
		frame.setResizable(false);
		frame.pack();
		frame.revalidate();
		frame.repaint();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setVisible(true);

		final Object lock = new Object();

		Thread t = new Thread() {
			public void run() {
				synchronized (lock) {
					while (frame.isVisible())
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					System.out.println("Working now");
				}
			}
		};
		t.start();

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				synchronized (lock) {
					frame.setVisible(false);
					lock.notify();
				}
			}

		});

		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return panel;
	}

}
