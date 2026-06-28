package ca.bkaw.praeter.gui.pack.font;

import ca.bkaw.praeter.gui.pack.ResourcePack;
import com.google.gson.JsonObject;

import java.io.IOException;

/**
 * A {@link FontProvider} that references another font by id.
 */
public class ReferenceFontProvider implements FontProvider {
    private final String id;
    private final Font referencedFont;

    public ReferenceFontProvider(ResourcePack pack, String id) throws IOException {
        this.id = id;
        this.referencedFont = new Font(pack, id);
    }

    /**
     * Get the font that this provider references.
     *
     * @return The referenced font.
     */
    public Font getReferencedFont() {
        return this.referencedFont;
    }

    @Override
    public boolean has(char c) {
        for (FontProvider provider : this.referencedFont.getProviders()) {
            if (provider.has(c)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JsonObject asJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "reference");
        json.addProperty("id", this.id);
        return json;
    }
}
