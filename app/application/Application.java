package application;

import eventManager.managing.StryckEventManager;
import utils.Logger;

public class Application{

	private Logger log;
	private StryckEventManager stryckEventManager;
	private EnvironmentWindow environmentWindow;

	public Application() {
		log = new Logger(this.getClass());
		stryckEventManager = new StryckEventManager();


		environmentWindow = new EnvironmentWindow(stryckEventManager);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
