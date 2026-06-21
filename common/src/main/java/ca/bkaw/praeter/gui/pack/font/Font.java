package ca.bkaw.praeter.gui.pack.font;

import ca.bkaw.praeter.gui.pack.JsonResource;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A font in a {@link ResourcePack}.
 */
public class Font {
    /**
     * The default height of a bitmap font provider if not specified in the json.
     */
    public static final int DEFAULT_BITMAP_HEIGHT = 8;

    private final ResourcePack pack;
    private final String identifier;
    private final JsonResource fontJson;
    private @Nullable JsonObject spaceProviderAdvances;

    /**
     * Load a font by namespaced key.
     * <p>
     * Will create the font if it does not already exist.
     *
     * @param pack The pack of the font.
     * @param identifier The key of the font.
     * @throws IOException If an I/O error occurs.
     */
    public Font(ResourcePack pack, String identifier) throws IOException {
        this.pack = pack;
        this.identifier = identifier;
        Key key = Key.key(identifier);
        Path path = this.pack.getPath("assets")
            .resolve(key.namespace())
            .resolve("font")
            .resolve(key.value() + ".json");
        if (Files.exists(path)) {
            this.fontJson = new JsonResource(this.pack, path);
        } else {
            Files.createDirectories(path.getParent());

            JsonObject json = new JsonObject();
            json.add("providers", new JsonArray());

            this.fontJson = new JsonResource(this.pack, path, json);
            this.fontJson.save();
        }
    }

    /**
     * Get the identifier of this font.
     *
     * @return The identifier.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Add the specified font provider to this font.
     *
     * @param provider The provider.
     * @throws IOException If an I/O error occurs.
     */
    public void addProvider(JsonObject provider) throws IOException {
        JsonObject json = this.fontJson.getJson();
        JsonArray providers = json.getAsJsonArray("providers");
        providers.add(provider);
        this.fontJson.save();
    }

    /**
     * Get the character mapped to the specified texture identifier, height, and ascent
     * in a bitmap font provider, or null if no such character exists.
     *
     * @param textureIdentifier The identifier of the texture.
     * @param height The height of the character.
     * @param ascent The vertical shift of the character.
     * @return The existing character, or null.
     */
    @Nullable
    public Character getBitmapChar(String textureIdentifier, int height, int ascent) {
        JsonArray providers = this.fontJson.getJson().getAsJsonArray("providers");
        for (JsonElement element : providers) {
            JsonObject json = element.getAsJsonObject();
            if (!"bitmap".equals(json.get("type").getAsString())) {
                continue;
            }
            String jsonTextureIdentifier = json.get("file").getAsString();
            int jsonAscent = json.get("ascent").getAsInt();
            int jsonHeight = json.has("height") ? json.get("height").getAsInt() : Font.DEFAULT_BITMAP_HEIGHT;
            if (textureIdentifier.equals(jsonTextureIdentifier) && height == jsonHeight && ascent == jsonAscent) {
                JsonArray chars = json.get("chars").getAsJsonArray();
                if (!chars.isEmpty()) {
                    return chars.get(0).getAsString().charAt(0);
                }
            }
        }
        return null;
    }

    /**
     * Add the bitmap font provider by using the next free character.
     *
     * @param textureIdentifier The identifier of the texture.
     * @param height The height of the character.
     * @param ascent The vertical shift of the character.
     * @return The character mapped to the bitmap font provider.
     * @throws IOException If an I/O error occurs.
     */
    public char addBitmap(String textureIdentifier, int height, int ascent) throws IOException {
        if (ascent > height) {
            throw new IllegalArgumentException("Ascent can not be greater than height.");
        }
        Character existingChar = this.getBitmapChar(textureIdentifier, height, ascent);
        if (existingChar != null) {
            return existingChar;
        }
        JsonObject provider = new JsonObject();
        provider.addProperty("type", "bitmap");
        provider.addProperty("file", textureIdentifier);
        provider.addProperty("height", height);
        provider.addProperty("ascent", ascent);
        JsonArray chars = new JsonArray();
        char c = this.getNextChar();
        chars.add(c);
        provider.add("chars", chars);
        this.addProvider(provider);
        return c;
    }

    /**
     * Get the character that has the specified advance in the space provider, or null
     * if no such character exists.
     *
     * @param advance The number of pixels to advance.
     * @return The existing character, or null.
     */
    @Nullable
    public Character getSpaceChar(int advance) {
        if (this.spaceProviderAdvances == null) {
            return null;
        }
        for (var entry : this.spaceProviderAdvances.entrySet()) {
            if (entry.getValue().getAsInt() == advance) {
                return entry.getKey().charAt(0);
            }
        }
        return null;
    }

    /**
     * Add a new space font character by using the next free character.
     * <p>
     * If there already exists a character with the specified advance, then no new
     * character will be added and the existing character will be returned.
     *
     * @param advance The number to pixels to advance.
     * @return The character mapped to the advance in the space provider.
     * @throws IOException If an I/O error occurs.
     */
    public char addSpace(int advance) throws IOException {
        // Use the shared space provider for this font
        if (this.spaceProviderAdvances == null) {
            JsonObject spaceProvider = new JsonObject();
            this.spaceProviderAdvances = new JsonObject();
            spaceProvider.addProperty("type", "space");
            spaceProvider.add("advances", this.spaceProviderAdvances);
            this.addProvider(spaceProvider);
        }
        // Only add if it does not already exist
        Character existing = this.getSpaceChar(advance);
        if (existing != null) {
            return existing;
        }
        char c = this.getNextChar();
        this.spaceProviderAdvances.addProperty(String.valueOf(c), advance);
        this.fontJson.save();
        return c;
    }

    /**
     * Get the next free character to use.
     *
     * @return The character.
     */
    public char getNextChar() {
        int i = 0xe001;
        JsonArray providers = this.fontJson.getJson().getAsJsonArray("providers");
        freeValueLoop:
        while (true) {
            char c = (char) i;
            for (JsonElement element : providers) {
                JsonObject provider = element.getAsJsonObject();
                switch (provider.get("type").getAsString()) {
                    case "bitmap" -> {
                        JsonArray chars = provider.getAsJsonArray("chars");
                        for (JsonElement element2 : chars) {
                            if (element2.getAsString().indexOf(c) >= 0) {
                                // This character is occupied, lets increment and try again
                                i++;
                                continue freeValueLoop;
                            }
                        }
                    }
                    case "space" -> {
                        JsonObject advances = provider.getAsJsonObject("advances");
                        if (advances.has(String.valueOf(c))) {
                            // This character is occupied, lets increment and try again
                            i++;
                            continue freeValueLoop;
                        }
                    }
                }
            }
            // This point was reached without hitting a continue statement.
            // We have an unused character.
            return c;
        }
    }

}