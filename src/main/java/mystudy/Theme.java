package mystudy;

public enum Theme {
    PURPLE("Purple", "/themes/purple.css"),
    DARK  ("Dark",   "/themes/dark.css"),
    LIGHT ("Light",  "/themes/light.css");

    private final String display;
    private final String sheet;
    Theme(String display, String sheet) {
        this.display = display;
        this.sheet   = sheet;
    }
    @Override public String toString() { return display; }
    public String stylesheet()        { return sheet; }
}
