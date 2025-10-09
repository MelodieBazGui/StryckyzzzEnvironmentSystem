package application;

import javax.swing.JFrame;

import eventManager.managing.StryckEventManager;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import java.awt.Color;

public class EnvironmentWindow {
	
	private final StryckEventManager SEM;
	
	private JFrame frame;
	private JPanel fileExplorer;
	private JPanel iDE;
	private JPanel renderEnvironment;
	private JTabbedPane tabbedPane;

	public EnvironmentWindow(StryckEventManager stryckEventManager) {
		this.SEM = stryckEventManager;
		generalWindowSetup();
	}

	private void generalWindowSetup() {
		frame = new JFrame();
		frame.setBackground(new Color(255, 255, 255));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane);
		
		renderEnvironment = new JPanel();
		tabbedPane.addTab("Render", null, renderEnvironment, null);
		
		iDE = new JPanel();
		tabbedPane.addTab("IDE", null, iDE, null);
		
		fileExplorer = new JPanel();
		tabbedPane.addTab("File Explorer", null, fileExplorer, "A file explorer to see what's available in your data folder");
	}



}
