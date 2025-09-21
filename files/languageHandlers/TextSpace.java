package languageHandlers;

public class TextSpace {
    private String text;

    public TextSpace(String txt) {
        this.text = txt;
    }

    public String getTextSpace() {
        return text;
    }

    public void clear() {
        text = null;
    }
}