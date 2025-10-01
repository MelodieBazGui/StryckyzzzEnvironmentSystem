package eventManager.events;
import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;
import utils.Logger;
public class WindowFocus {
	
	public class WindowFocusEvent extends StryckEvent {
	    private static final Logger logger = new Logger(WindowFocusEvent.class);

	    public WindowFocusEvent() {
	        logger.info("Created");
	    }

	    @Override
	    public String getName() { return "WindowFocusEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.WindowFocus; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
	}

	public class WindowLostFocusEvent extends StryckEvent {
	    private static final Logger logger = new Logger(WindowLostFocusEvent.class);

	    public WindowLostFocusEvent() {
	        logger.info("Created");
	    }

	    @Override
	    public String getName() { return "WindowLostFocusEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.WindowLostFocus; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Application.getBit(); }
	}
	
}
