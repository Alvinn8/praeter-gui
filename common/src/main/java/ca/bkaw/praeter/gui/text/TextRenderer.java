package ca.bkaw.praeter.gui.text;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.font.BitmapFontProvider;
import ca.bkaw.praeter.gui.pack.font.Font;
import ca.bkaw.praeter.gui.pack.font.FontProvider;
import ca.bkaw.praeter.gui.pack.font.ReferenceFontProvider;
import ca.bkaw.praeter.gui.pack.font.SpaceFontProvider;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

/**
 * A utility class for rendering text.
 * <p>
 * Only supports bitmap and space font providers.
 */
public class TextRenderer {

    /**
     * The number of pixels between the start of a line and the start of the line below.
     */
    public static final int LINE_HEIGHT = 9;

    /**
     * Get the default font from the vanilla assets.
     *
     * @return The default font.
     */
    public static Font defaultFont() {
        ResourcePack vanillaAssets = PraeterGui.instance().getAssets().getVanillaAssets();
        if (vanillaAssets == null) {
            throw new IllegalStateException("Vanilla assets are not loaded.");
        }
        try {
            return new Font(vanillaAssets, "minecraft:default");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the default font.", e);
        }
    }

    /**
     * Render text onto an image.
     *
     * @param image The image to render onto.
     * @param text The text to render.
     * @param x The x pixel coordinate to start at.
     * @param y The y pixel coordinate to start at.
     * @param color The color to render the text as.
     * @param font The font to use when rendering the text.
     */
    public static void renderText(BufferedImage image, String text, int x, int y, Color color, Font font) {
        int startX = x;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                x = startX;
                y += LINE_HEIGHT;
                continue;
            }

            FontProvider provider = getProviderForChar(font, c);
            switch (provider) {
                case null -> throw new IllegalArgumentException("Cannot draw the character: '" + c + "'");
                case SpaceFontProvider spaceProvider ->
                    x += Objects.requireNonNull(spaceProvider.getAdvance(c));
                case BitmapFontProvider bitmapProvider -> {
                    BufferedImage sprite;
                    try {
                        sprite = Objects.requireNonNull(bitmapProvider.getSprite(c));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load sprite for character: '" + c + "'", e);
                    }
                    for (int rows = 0; rows < sprite.getHeight(); rows++) {
                        for (int cols = 0; cols < sprite.getWidth(); cols++) {
                            int argb = sprite.getRGB(cols, rows);
                            int alpha = argb & 0xFF000000;
                            if (alpha != 0) {
                                image.setRGB(x + cols, y + rows, color.getRGB());
                            }
                        }
                    }
                    x += getAdvance(sprite) + 1;
                }
                default -> throw new IllegalStateException("Cannot draw the character: '"+ c +"' because the font provider is not a bitmap or space provider.");
            }
        }
    }

    private static @Nullable FontProvider getProviderForChar(Font font, char c) {
        for (FontProvider provider : font.getProviders()) {
            if (provider.has(c)) {
                if (provider instanceof ReferenceFontProvider reference) {
                    return getProviderForChar(reference.getReferencedFont(), c);
                } else {
                    return provider;
                }
            }
        }
        return null;
    }

    /**
     * Get the number of pixels to advance the cursor after drawing the specified image.
     * <p>
     * Excluding the one-pixel space added after each character.
     *
     * @param image The image of the character.
     * @return The number of pixels to advance the cursor.
     */
    public static int getAdvance(BufferedImage image) {
        // Don't count transparent columns to the right
        int x;
        for (x = image.getWidth() - 1; x >= 0; x--) {
            for (int y = 0; y < image.getHeight(); y++) {
                int argb = image.getRGB(x, y);
                int alpha = argb & 0xFF000000;
                if (alpha != 0) {
                    return x + 1;
                }
            }
        }
        return x + 1;
    }

    /**
     * Get the width of the text, in pixels.
     *
     * @param text The text to measure.
     * @param font The font to use when measuring the text.
     * @return The width of the text, in pixels.
     */
    public static int getTextWidth(String text, Font font) {
        int maxWidth = 0;
        int width = 0;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                // A new line, reset the count
                if (width > maxWidth) {
                    maxWidth = width;
                }
                width = 0;
                continue;
            }

            FontProvider provider = getProviderForChar(font, c);
            switch (provider) {
                case null -> throw new IllegalArgumentException("Cannot draw the character: '" + c + "'");
                case SpaceFontProvider spaceProvider ->
                    width += Objects.requireNonNull(spaceProvider.getAdvance(c));
                case BitmapFontProvider bitmapProvider -> {
                    BufferedImage sprite;
                    try {
                        sprite = Objects.requireNonNull(bitmapProvider.getSprite(c));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load sprite for character: '" + c + "'", e);
                    }
                    width += getAdvance(sprite) + 1;
                }
                default -> throw new IllegalStateException("Cannot draw the character: '"+ c +"' because the font provider is not a bitmap or space provider.");
            }
        }
        if (width > maxWidth) {
            maxWidth = width;
        }
        return maxWidth;
    }

    /**
     * Get the text height, in pixels.
     *
     * @param text The text to measure.
     * @param font The font to use when measuring the text.
     * @return The height of the text, in pixels.
     */
    public static int getTextHeight(String text, Font font) {
        int lines = 1;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                lines++;
            }
        }
        return lines * LINE_HEIGHT - 1;
    }
}
