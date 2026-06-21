package ca.bkaw.praeter.gui.pack.font;

/**
 * An immutable sequence of characters to be rendered with a specific font.
 *
 * @param fontIdentifier The identifier of the font to use for rendering this sequence.
 * @param text The sequence of characters.
 */
public record FontSequence(String fontIdentifier, String text) {
    /**
     * Get the number of characters in this font sequence.
     *
     * @return The length.
     */
    public int length() {
        return this.text.length();
    }
}
