package appWindow;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import ui.Menu;
import ui.Tabs;
import utils.Logger;

public class AppPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9069657847861666301L;
	
	private static Logger logger;

	private static Menu menu;
	private static Tabs tabs;

	private JPanel appPanel;
	
	public AppPanel() {
		appPanel = new JPanel(new BorderLayout());
		logger = new Logger(this.getClass());
		init();
		logger.logInfo("Instantied Class");
	}
	
	private void init() {
        addMenu();
        addTabs();
        setVisible(true);
        logger.logInfo("Initiated Content for AppPanel");
	}
	
	public void reloadUIs() {
        removeAll();
        revalidate();
        repaint();

        addMenu();
        addTabs();
        
        logger.logInfo("Reloaded UI components");
    }
    
    public static Menu getMenu() {
        return menu;
    }

    public static Tabs getTabs() {
        return tabs;
    }
    
    private void addMenu() {
    	menu = new Menu(this);
        logger.logInfo("Created menu");
    }
    
    private void addTabs() {
    	tabs = new Tabs(appPanel);
    	this.add(tabs);
        logger.logInfo("Created tabs");
    }
	
}
