package ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import appWindow.AppPanel;
import stryckyzzzComponents.StryckyzzzClasses.StryckyzzzTextArea;

import javax.swing.JSplitPane;

import java.awt.BorderLayout;

/** Tabs ui class, handles app's tabs
 * <p>
 *  @author MelodieBazGui
 */
public class Tabs extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2524951949485348774L;
	
	private JTabbedPane tabbedPane;
	private JPanel exploringTab;
	private JPanel overviewTab;
	private JLabel infoPaneOverview;
	private JSplitPane splitPane;
	private JSplitPane splitpane;
	private JLabel infoPaneExplorator;
	
	/**
	 * Instantiate the tabs using JTabbedPane, handles panels, you should reload the panels first and not the JTabbedPane
	 * @param appPanel
	 */
	public Tabs(AppPanel appPanel) {
		tabbedPane = new JTabbedPane();
	    
		overviewTab = new JPanel();
	    overviewTab.setLayout(new BorderLayout(0, 0));
	    infoPaneOverview = new JLabel(new StryckyzzzTextArea("tab.overview").getText());
	    overviewTab.add(infoPaneOverview);
	    
	    exploringTab = new JPanel();
	    exploringTab.setLayout(new BorderLayout(0, 0));
	    infoPaneExplorator = new JLabel(new StryckyzzzTextArea("tab.explorer").getText());
	    exploringTab.add(infoPaneExplorator);

	    tabbedPane.addTab(new StryckyzzzTextArea("tab.overview").getText(), overviewTab);
	    tabbedPane.addTab(new StryckyzzzTextArea("tab.explorer").getText(), exploringTab);
	    
	    splitPane = new JSplitPane();
	    overviewTab.add(splitPane);
	    
	    splitpane = new JSplitPane();
	    exploringTab.add(splitpane);
	    
	    appPanel.add(tabbedPane, BorderLayout.WEST);
	}
	
	public void reloadText() {
		
	    tabbedPane.setTitleAt(0, new StryckyzzzTextArea("tab.overview").getText());
	    tabbedPane.setTitleAt(1, new StryckyzzzTextArea("tab.explorer").getText());

	    infoPaneOverview.setText(new StryckyzzzTextArea("tab.overview").getText());
	    infoPaneExplorator.setText(new StryckyzzzTextArea("tab.explorer").getText());
	}
}
