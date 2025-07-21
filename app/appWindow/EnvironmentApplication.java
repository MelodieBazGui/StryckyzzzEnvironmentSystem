package appWindow;

import javax.swing.JFrame;

import Handlers.IMGHandler;
import languageHandlers.LanguageLoader;
import stryckyzzzComponents.StryckyzzzClasses.StryckyzzzTextAreas;
import utils.ClassUtil;
import utils.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;

public class EnvironmentApplication {
	
	public static final String NAME = "StryckyzzzEnvironmentSystem";
	public static final String VERSION = "V0.1-indev";
	private static AppPanel appPanel;
	
	public static StryckyzzzTextAreas STAS ;
	public static LanguageLoader LL;
	public static Logger logger;
	private static IMGHandler IMGH;
	
    public static String defaultLang = "en_EN.txt";
	
	
	public static JFrame frame = new JFrame(NAME);
	
	private static void initEnv() {
		logger = new Logger(ClassUtil.getClassName());
		STAS = new StryckyzzzTextAreas();
		LL = new LanguageLoader();
		IMGH = new IMGHandler();
		appPanel = new AppPanel();
		logger.logInfo("Initialized app");
	}
	
	/**
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
    	initEnv();
    	logger.logInfo("Instantiated Main");
    	logger.logInfo("Starting Main");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(400, 300));
        frame.setIconImage(IMGH.loadImage("png", "icon"));
        frame.setLayout(new BorderLayout());
        frame.add(appPanel);
        frame.setVisible(true);
        logger.logDuration(logger.getFileName());
    }
    
    public static String getDefaultLang() {
    	return defaultLang;
    }
    
    public static void changeDefaultLang(String s) {
    	defaultLang = s;
    }
    
}
