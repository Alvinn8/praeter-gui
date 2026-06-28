package ca.bkaw.praeter.gui.pack.font;

/**
 * An immutable sequence of characters to be rendered with a specific font.
 */
public final class FontSequence {
    private final String fontIdentifier;
    private final String text;

    public FontSequence(String fontIdentifier, String text) {
        this.fontIdentifier = fontIdentifier;
        this.text = text;
    }

    public String fontIdentifier() {
        return this.fontIdentifier;
    }

    public String text() {
        return this.text;
    }

    /**
     * Get the number of characters in this font sequence.
     *
     * @return The length.
     */
    public int length() {
        return this.text.length();
    }
}
