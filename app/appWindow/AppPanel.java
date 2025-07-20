package appWindow;

import java.awt.Component;

import javax.swing.JPanel;

import ui.Menu;
import ui.Tabs;
import utils.Logger;

public class AppPanel extends JPanel{

	private static JPanel appPanel;
	private static Logger logger;

	private static Menu menu;
	private static Tabs tabs;
	
	public AppPanel() {
		appPanel = new JPanel();
		logger = new Logger(this.getClass());
		init();
		logger.logInfo("Instantied Class");
	}
	
	private void init() {
        addMenu();
        addTabs();
        logger.logInfo("Initiated Content for AppPanel");
	}
	
	public void reloadUIs() {
        if (appPanel == null) {
            throw new IllegalStateException("Frame is not initialized!");
        }
        appPanel.removeAll();
        appPanel.revalidate();
        appPanel.repaint();

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
    	tabs = new Tabs(this);
        logger.logInfo("Created tabs");
    }
	
}
