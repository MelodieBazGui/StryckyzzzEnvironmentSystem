package ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import stryckyzzzComponents.StryckyzzzClasses.StryckyzzzTextArea;
import utils.Logger;

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
	private JLabel infoPaneExplorator;

	private Logger logger;
	
	/**
	 * Instantiate the tabs using JTabbedPane, handles panels, you should reload the panels first and not the JTabbedPane
	 * @param appPanel 
	 * @param appPanel
	 */
	public Tabs(JPanel appPanel) {
		logger = new Logger(this.getClass());
		tabbedPane = new JTabbedPane();
		logger.logInfo("Created JTabbedPane");
	    
		overviewTab = new JPanel();
	    overviewTab.setLayout(new BorderLayout(0, 0));
	    infoPaneOverview = new JLabel(new StryckyzzzTextArea("tab.overview").getText());
	    overviewTab.add(infoPaneOverview);
	    logger.logInfo("Added Overview tab");
	    
	    exploringTab = new JPanel();
	    exploringTab.setLayout(new BorderLayout(0, 0));
	    infoPaneExplorator = new JLabel(new StryckyzzzTextArea("tab.explorer").getText());
	    exploringTab.add(infoPaneExplorator);
	    logger.logInfo("Added Explorer tab");

	    tabbedPane.addTab(new StryckyzzzTextArea("tab.overview").getText(), overviewTab);
	    tabbedPane.addTab(new StryckyzzzTextArea("tab.explorer").getText(), exploringTab);
	    logger.logInfo("Added tabs to the JTabbedPane");
	    
	    appPanel.add(tabbedPane, BorderLayout.WEST);
	    logger.logInfo("Added tabbedPane to appPanel");
	    
	    logger.logInfo("Finished Instantiating class");
	}
	
	public void reloadText() {
		logger.logInfo("Relaoding text");
	    tabbedPane.setTitleAt(0, new StryckyzzzTextArea("tab.overview").getText());
	    tabbedPane.setTitleAt(1, new StryckyzzzTextArea("tab.explorer").getText());

	    infoPaneOverview.setText(new StryckyzzzTextArea("tab.overview").getText());
	    infoPaneExplorator.setText(new StryckyzzzTextArea("tab.explorer").getText());
	    logger.logInfo("Reloaded text");
	}
}
