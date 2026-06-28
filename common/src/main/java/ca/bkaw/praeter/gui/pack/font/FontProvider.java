package ca.bkaw.praeter.gui.pack.font;

import com.google.gson.JsonObject;

/**
 * A provider for characters in a font.
 *
 * @see Font
 * @see BitmapFontProvider
 * @see SpaceFontProvider
 */
public interface FontProvider {
    /**
     * Get the json object for this provider.
     *
     * @return The json.
     */
    JsonObject asJsonObject();

    /**
     * Check if this provider uses the specified character.
     *
     * @param c The character to check.
     * @return True if the character is used by this provider, false otherwise.
     */
    boolean has(char c);
}
