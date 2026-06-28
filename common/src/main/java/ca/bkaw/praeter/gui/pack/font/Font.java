package ca.bkaw.praeter.gui.pack.font;

import ca.bkaw.praeter.gui.pack.JsonResource;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A font in a {@link ResourcePack}.
 */
public class Font {
    private final ResourcePack pack;
    private final String identifier;
    private final JsonResource fontJson;
    private @Nullable List<FontProvider> providers;
    private @Nullable SpaceFontProvider spaceProvider;

    /**
     * Load a font from a pack if it exists, or return null if the font is not in the pack.
     * <p>
     * Unlike the constructor, this does not create the font if it does not exist.
     *
     * @param pack The pack to look in.
     * @param identifier The key of the font.
     * @return The font, or null if the font is not in the pack.
     * @throws IOException If an I/O error occurs.
     */
    public static @Nullable Font loadIfExists(ResourcePack pack, String identifier) throws IOException {
        String[] parts = identifier.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "minecraft";
        String value     = parts.length == 2 ? parts[1] : identifier;
        Path path = pack.getPath("assets")
            .resolve(namespace)
            .resolve("font")
            .resolve(value + ".json");
        if (!Files.exists(path)) {
            return null;
        }
        return new Font(pack, identifier);
    }

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
        String[] parts = identifier.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "minecraft";
        String value     = parts.length == 2 ? parts[1] : identifier;
        Path path = this.pack.getPath("assets")
            .resolve(namespace)
            .resolve("font")
            .resolve(value + ".json");
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
    public void addProvider(FontProvider provider) throws IOException {
        JsonObject json = this.fontJson.getJson();
        JsonArray providers = json.getAsJsonArray("providers");
        providers.add(provider.asJsonObject());
        this.fontJson.save();
        if (this.providers != null) {
            this.providers.add(provider);
        }
    }

    /**
     * Get the list of font providers in this font.
     *
     * @return The list of font providers.
     */
    public List<FontProvider> getProviders() {
        if (this.providers != null) {
            return this.providers;
        }
        JsonArray providersJson = this.fontJson.getJson().getAsJsonArray("providers");
        this.providers = new ArrayList<>(providersJson.asList().stream()
            .map(JsonElement::getAsJsonObject)
            .map(provider -> {
                String type = provider.get("type").getAsString();
                switch (type) {
                    case "bitmap": return (FontProvider) new BitmapFontProvider(this.pack, provider);
                    case "space":  return (FontProvider) new SpaceFontProvider(provider.getAsJsonObject("advances"));
                    case "reference": {
                        try {
                            return (FontProvider) new ReferenceFontProvider(this.pack, provider.get("id").getAsString());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to resolve reference font provider.", e);
                        }
                    }
                    default: return (FontProvider) new UnknownFontProvider(provider);
                }
            })
            .collect(Collectors.toList()));
        return this.providers;
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
        for (FontProvider provider : this.getProviders()) {
            if (!(provider instanceof BitmapFontProvider)) {
                continue;
            }
            BitmapFontProvider bitmapProvider = (BitmapFontProvider) provider;
            Character c = bitmapProvider.getChar(textureIdentifier, height, ascent);
            if (c != null) {
                return c;
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
        Character existingChar = this.getBitmapChar(textureIdentifier, height, ascent);
        if (existingChar != null) {
            return existingChar;
        }
        char c = this.getNextChar();
        List<String> chars = Collections.singletonList(String.valueOf(c));
        this.addProvider(new BitmapFontProvider(this.pack, textureIdentifier, height, ascent, chars));
        return c;
    }

    private SpaceFontProvider getOrCreateSpaceProvider() throws IOException {
        if (this.spaceProvider != null) {
            return this.spaceProvider;
        }
        // Find existing provider
        for (FontProvider provider : this.getProviders()) {
            if (provider instanceof SpaceFontProvider) {
                this.spaceProvider = (SpaceFontProvider) provider;
                return this.spaceProvider;
            }
        }
        // Create a new space provider if it does not exist
        this.spaceProvider = new SpaceFontProvider();
        this.addProvider(this.spaceProvider);
        return this.spaceProvider;
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
        if (this.spaceProvider == null) {
            this.spaceProvider = this.getOrCreateSpaceProvider();
        }
        // Only add if it does not already exist
        Character existing = this.spaceProvider.getChar(advance);
        if (existing != null) {
            return existing;
        }
        char c = this.getNextChar();
        this.spaceProvider.add(c, advance);
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
        freeValueLoop:
        while (true) {
            char c = (char) i;
            for (FontProvider provider : this.getProviders()) {
                if (provider.has(c)) {
                    // This character is occupied, lets increment and try again
                    i++;
                    continue freeValueLoop;
                }
            }
            // This point was reached without hitting a continue statement.
            // We have an unused character.
            return c;
        }
    }
}