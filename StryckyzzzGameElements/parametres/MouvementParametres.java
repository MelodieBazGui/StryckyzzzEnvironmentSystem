package parametres;

import utils.Logger;

public class MouvementParametres extends Parametres {
    private static final Logger logger = new Logger(MouvementParametres.class);

    // Primary movement keys
    private int keyForward = 'W';
    private int keyBackward = 'S';
    private int keyLeft = 'A';
    private int keyRight = 'D';

    // Modifier keys
    private int keyJump = 32;   // Space
    private int keySprint = 16; // Shift
    private int keyCrouch = 17; // Control

    // Layout setting
    private String layout = "WASD";

    @Override
    public String getName() {
        return "mouvement";
    }

    /* Getters
     */
    public int getKeyForward() { return keyForward; }
    public int getKeyBackward() { return keyBackward; }
    public int getKeyLeft() { return keyLeft; }
    public int getKeyRight() { return keyRight; }
    public int getKeyJump() { return keyJump; }
    public int getKeySprint() { return keySprint; }
    public int getKeyCrouch() { return keyCrouch; }
    public String getLayout() { return layout; }

    /**
     * Utility
     * @param newLayout
     */
    public void applyLayout(String newLayout) {
        newLayout = newLayout.toUpperCase();
        if (newLayout.equals("ZQSD")) {
            keyForward = 'Z';
            keyBackward = 'S';
            keyLeft = 'Q';
            keyRight = 'D';
            layout = "ZQSD";
        } else {
            keyForward = 'W';
            keyBackward = 'S';
            keyLeft = 'A';
            keyRight = 'D';
            layout = "WASD";
        }
        logger.info("Applied keyboard layout: " + layout);
    }

    @Override
    protected void copyFrom(Parametres other) {
        if (other instanceof MouvementParametres o) {
            this.keyForward = o.keyForward;
            this.keyBackward = o.keyBackward;
            this.keyLeft = o.keyLeft;
            this.keyRight = o.keyRight;
            this.keyJump = o.keyJump;
            this.keySprint = o.keySprint;
            this.keyCrouch = o.keyCrouch;
            this.layout = o.layout;
        }
    }
}
