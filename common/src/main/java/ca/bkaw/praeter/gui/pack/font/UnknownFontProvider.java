package ca.bkaw.praeter.gui.pack.font;

import com.google.gson.JsonObject;

/**
 * A {@link FontProvider} for unknown/unsupported font providers.
 * <p>
 * This is used to preserve the json.
 */
public class UnknownFontProvider implements FontProvider {
    private JsonObject json;

    public UnknownFontProvider(JsonObject json) {
        this.json = json;
    }

    @Override
    public boolean has(char c) {
        return false;
    }

    @Override
    public JsonObject asJsonObject() {
        return this.json;
    }
}
