package eventManager.enumerations;

public enum StryckEventCategory {
    None(0),
    Application(1 << 0),
    Input(1 << 1),
    Keyboard(1 << 2),
    Mouse(1 << 3),
    MouseButton(1 << 4);

    private final int bit;

    StryckEventCategory(int bit) {
        this.bit = bit;
    }

    public int getBit() {
        return bit;
    }
}