package eventManager.enumerations;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public enum StryckEventType {
	None,
	WindowClose, WindowResize, WindowFocus, WindowLostFocus, WindowMoved,
	AppTick, AppUpdate, AppRender, AppPhysics,
	KeyboardPressed, KeyboardReleased,
	MouseButtonPressed, MouseButtonReleased, MouseMouved, MouseScroll;
	
	public static List<StryckEventType> getWindowEvents() {
        return EnumSet.of(WindowClose, WindowResize, WindowFocus, WindowLostFocus, WindowMoved)
                      .stream()
                      .collect(Collectors.toList());
    }

    public static List<StryckEventType> getAppEvents() {
        return EnumSet.of(AppTick, AppUpdate, AppRender, AppPhysics)
                      .stream()
                      .collect(Collectors.toList());
    }

    public static List<StryckEventType> getKeyboardEvents() {
        return EnumSet.of(KeyboardPressed, KeyboardReleased)
                      .stream()
                      .collect(Collectors.toList());
    }

    public static List<StryckEventType> getMouseEvents() {
        return EnumSet.of(MouseButtonPressed, MouseButtonReleased, MouseMouved, MouseScroll)
                      .stream()
                      .collect(Collectors.toList());
    }
	
}
