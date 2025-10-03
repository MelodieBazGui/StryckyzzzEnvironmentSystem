package eventManager.events;

import eventManager.enumerations.StryckEventCategory;
import eventManager.enumerations.StryckEventType;
import utils.Logger;

public class MouseEvent {

	public class MouseButtonPressedEvent extends StryckEvent {
	    private final int button;
	    private static final Logger logger = new Logger(MouseButtonPressedEvent.class);

	    public MouseButtonPressedEvent(int button) {
	        this.button = button;
	        logger.info("Created with button=" + button);
	    }

	    public int getButton() { return button; }

	    @Override
	    public String getName() { return "MouseButtonPressedEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.MouseButtonPressed; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Input.getBit() | StryckEventCategory.MouseButton.getBit(); }
	}

	public class MouseButtonReleasedEvent extends StryckEvent {
	    private final int button;
	    private static final Logger logger = new Logger(MouseButtonReleasedEvent.class);

	    public MouseButtonReleasedEvent(int button) {
	        this.button = button;
	        logger.info("Created with button=" + button);
	    }

	    public int getButton() { return button; }

	    @Override
	    public String getName() { return "MouseButtonReleasedEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.MouseButtonReleased; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Input.getBit() | StryckEventCategory.MouseButton.getBit(); }
	}

	public class MouseMouvedEvent extends StryckEvent {
	    private final float x, y;
	    private static final Logger logger = new Logger(MouseMouvedEvent.class);

	    public MouseMouvedEvent(float x, float y) {
	        this.x = x;
	        this.y = y;
	        logger.info("Created at (" + x + "," + y + ")");
	    }

	    public float getX() { return x; }
	    public float getY() { return y; }

	    @Override
	    public String getName() { return "MouseMouvedEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.MouseMouved; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Input.getBit() | StryckEventCategory.Mouse.getBit(); }
	}

	public class MouseScrollEvent extends StryckEvent {
	    private final float offsetX, offsetY;
	    private static final Logger logger = new Logger(MouseScrollEvent.class);

	    public MouseScrollEvent(float offsetX, float offsetY) {
	        this.offsetX = offsetX;
	        this.offsetY = offsetY;
	        logger.info("Created with offsets: x=" + offsetX + ", y=" + offsetY);
	    }

	    public float getOffsetX() { return offsetX; }
	    public float getOffsetY() { return offsetY; }

	    @Override
	    public String getName() { return "MouseScrollEvent"; }
	    @Override
	    public StryckEventType getEventType() { return StryckEventType.MouseScroll; }
	    @Override
	    public int getCategoryFlags() { return StryckEventCategory.Input.getBit() | StryckEventCategory.Mouse.getBit(); }
	}

}
