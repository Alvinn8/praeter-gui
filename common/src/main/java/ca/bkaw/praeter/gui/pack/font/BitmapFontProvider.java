package ca.bkaw.praeter.gui.pack.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A bitmap provider for a font.
 *
 * @param textureKey The identifier of the texture, relative to the namespace's
 *                   textures folder. The file extension must be present.
 * @param height The height of the character.
 * @param ascent The vertical shift of the character.
 * @param chars The list of characters.
 *
 * @see Font
 */
public record BitmapFontProvider(
    String textureKey,
    int height,
    int ascent,
    List<String> chars
) implements FontProvider {
    /**
     * The default height of a bitmap font provider if not specified in the json.
     */
    public static final int DEFAULT_HEIGHT = 8;

    public BitmapFontProvider {
        if (this.ascent() > this.height()) {
            throw new IllegalArgumentException("Ascent can not be higher than height.");
        }
    }

    public BitmapFontProvider(JsonObject json) {
        this(
            json.get("file").getAsString(),
            json.has("height") ? json.get("height").getAsInt() : DEFAULT_HEIGHT,
            json.get("ascent").getAsInt(),
            json.get("chars").getAsJsonArray()
                .asList()
                .stream()
                .map(JsonElement::getAsString)
                .toList()
        );
    }

    /**
     * Get the character mapped to the specified texture identifier, height, and ascent
     * in this bitmap font provider, or null if no such character exists.
     * <p>
     * This method only checks for a single character in the provider. If the provider
     * has multiple characters, it will return null.
     *
     * @param textureIdentifier The identifier of the texture.
     * @param height The height of the character.
     * @param ascent The vertical shift of the character.
     * @return The existing character, or null.
     */
    public @Nullable Character getChar(String textureIdentifier, int height, int ascent) {
        if (!this.textureKey.equals(textureIdentifier) || this.height != height || this.ascent != ascent) {
            return null;
        }
        if (this.chars.size() != 1) {
            return null;
        }
        return this.chars.getFirst().charAt(0);
    }

    @Override
    public boolean has(char c) {
        for (String str : this.chars) {
            if (str.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a json object representing this bitmap provider.
     *
     * @return The json object.
     */
    @Override
    public JsonObject asJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "bitmap");
        json.addProperty("file", this.textureKey.toString());
        json.addProperty("ascent", this.ascent);
        json.addProperty("height", this.height);
        JsonArray chars = new JsonArray();
        this.chars.forEach(chars::add);
        json.add("chars", chars);
        return json;
    }
}
