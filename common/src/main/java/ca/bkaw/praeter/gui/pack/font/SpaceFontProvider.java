package ca.bkaw.praeter.gui.pack.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A {@link FontProvider} for character that only advance the cursor.
 * <p>
 * Note that mutating the provider will mutate the json.
 */
public class SpaceFontProvider implements FontProvider {
    private final JsonObject advances;

    public SpaceFontProvider() {
        this.advances = new JsonObject();
    }

    public SpaceFontProvider(JsonObject advances) {
        this.advances = advances;
    }

    /**
     * Map the specified character to advance by the set amount.
     *
     * @param c The character.
     * @param advance The amount to advance.
     */
    public void add(char c, int advance) {
        this.advances.addProperty(String.valueOf(c), advance);
    }

    /**
     * Get the character that has the specified advance in the space provider, or null
     * if no such character exists.
     *
     * @param advance The amount to advance by.
     * @return The existing character, or null.
     */
    public @Nullable Character getChar(int advance) {
        for (Map.Entry<String, JsonElement> entry : this.advances.entrySet()) {
            if (entry.getValue().getAsInt() == advance) {
                return entry.getKey().charAt(0);
            }
        }
        return null;
    }

    /**
     * Get the advance for the specified character, or null if no advance is specified
     * for that character.
     *
     * @param c The character.
     * @return The advance, or null.
     */
    public @Nullable Integer getAdvance(char c) {
        JsonElement advance = this.advances.get(String.valueOf(c));
        if (advance == null) {
            return null;
        }
        return advance.getAsInt();
    }

    @Override
    public boolean has(char c) {
        return this.advances.has(String.valueOf(c));
    }

    @Override
    public JsonObject asJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "space");
        json.add("advances", this.advances);
        return json;
    }
}
