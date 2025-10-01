package eventManager.events;
import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;
import utils.Logger;
public class AppEvents {

	public class AppTickEvent extends StryckEvent {
	    private static final Logger logger = new Logger(AppTickEvent.class);

	    public AppTickEvent() {
	        logger.info("Created");
	    }

	    @Override
	    public String getName() { return "AppTickEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.AppTick; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
	}

	public class AppUpdateEvent extends StryckEvent {
	    private static final Logger logger = new Logger(AppUpdateEvent.class);

	    public AppUpdateEvent() {
	        logger.info("Created");
	    }

	    @Override
	    public String getName() { return "AppUpdateEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.AppUpdate; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
	}

	public class AppRenderEvent extends StryckEvent {
	    private static final Logger logger = new Logger(AppRenderEvent.class);

	    public AppRenderEvent() {
	        logger.info("Created");
	    }

	    @Override
	    public String getName() { return "AppRenderEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.AppRender; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
	}


	public class AppPhysicsEvent extends StryckEvent {
	    private static final Logger logger = new Logger(AppPhysicsEvent.class);

	    public AppPhysicsEvent() {
	        logger.info("Created");
	    }

	    @Override
	    public String getName() { return "AppPhysicsEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.AppPhysics; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
	}

}
