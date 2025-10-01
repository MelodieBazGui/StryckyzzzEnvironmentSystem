package handlers;

import java.util.HashMap;
import java.util.Map;

public class InputHandler {
    private final Map<Integer, Long> keyPressTimes = new HashMap<>();

    public void keyPressed(int keyCode) {
        keyPressTimes.put(keyCode, System.currentTimeMillis());
    }

    public void keyReleased(int keyCode) {
        keyPressTimes.remove(keyCode);
    }

    public boolean isKeyDown(int keyCode) {
        return keyPressTimes.containsKey(keyCode);
    }

    public long getKeyHeldDuration(int keyCode) {
        if (keyPressTimes.containsKey(keyCode)) {
            return System.currentTimeMillis() - keyPressTimes.get(keyCode);
        }
        return 0;
    }
}
