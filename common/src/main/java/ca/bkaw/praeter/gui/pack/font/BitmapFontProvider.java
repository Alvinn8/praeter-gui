package ca.bkaw.praeter.gui.pack.font;

import ca.bkaw.praeter.gui.pack.ResourcePack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A bitmap provider for a font.
 *
 * @see Font
 */
public final class BitmapFontProvider implements FontProvider {
    /**
     * The default height of a bitmap font provider if not specified in the json.
     */
    public static final int DEFAULT_HEIGHT = 8;

    private final ResourcePack pack;
    private final String textureKey;
    private final int height;
    private final int ascent;
    private final List<String> chars;
    private @Nullable BufferedImage texture;

    public BitmapFontProvider(ResourcePack pack, String textureKey, int height, int ascent, List<String> chars) {
        if (ascent > height) {
            throw new IllegalArgumentException("Ascent can not be higher than height.");
        }
        this.pack = pack;
        this.textureKey = textureKey;
        this.height = height;
        this.ascent = ascent;
        this.chars = chars;
    }

    public BitmapFontProvider(ResourcePack pack, JsonObject json) {
        this(
            pack,
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
     * Get the height of the characters in this bitmap font provider.
     *
     * @return The height of the characters.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the ascent of the characters in this bitmap font provider.
     *
     * @return The ascent.
     */
    public int getAscent() {
        return this.ascent;
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

    /**
     * Get the sprite for the specified character in this bitmap font provider, or null
     * if no such character exists.
     *
     * @param c The character to get the sprite for.
     * @return The sprite for the character, or null if no such character exists.
     * @throws IOException If an I/O error occurs while reading the texture.
     */
    public @Nullable BufferedImage getSprite(char c) throws IOException {
        if (!this.has(c)) {
            return null;
        }
        BufferedImage texture = this.texture;
        if (this.texture == null) {
            Path texturePath = this.pack.getTexturePath(this.textureKey);
            try (InputStream stream = Files.newInputStream(texturePath)) {
                texture = ImageIO.read(stream);
                this.texture = texture;
            }
        }
        int row;
        int col = -1;
        for (row = 0; row < this.chars.size(); row++) {
            String str = this.chars.get(row);
            col = str.indexOf(c);
            if (col >= 0) {
                break;
            }
        }
        if (row >= this.chars.size()) {
            return null;
        }
        int textureWidth = texture.getWidth();
        int textureHeight = texture.getHeight();
        int charWidth = textureWidth / this.chars.get(row).length();
        int charHeight = textureHeight / this.chars.size();
        BufferedImage subImage = texture.getSubimage(col * charWidth, row * charHeight, charWidth, charHeight);
        if (subImage.getHeight() == this.height) {
            return subImage;
        }
        BufferedImage resized = new BufferedImage(subImage.getWidth(), this.height, BufferedImage.TYPE_INT_ARGB);
        resized.getGraphics().drawImage(subImage, 0, 0, subImage.getWidth(), this.height, null);
        return resized;
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
