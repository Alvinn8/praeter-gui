package ca.bkaw.praeter.gui.draw;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.font.Font;
import ca.bkaw.praeter.gui.pack.font.FontSequence;
import org.jetbrains.annotations.Contract;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A builder for a {@link FontSequence} that may be used for rendering.
 */
public class GuiFontSequenceBuilder {
    /**
     * The x offset from the title of a gui (where custom fonts are placed to render a
     * gui) to the top-left pixel of the gui.
     */
    public static final int GUI_ORIGIN_OFFSET_X = -8;

    /**
     * The y offset from the title of a gui (where custom fonts are placed to render a
     * gui) to the top-left pixel of the gui.
     */
    public static final int GUI_ORIGIN_OFFSET_Y = -13;

    private final ResourcePack resourcePack;
    private final Font font;
    private final StringBuilder fontChars = new StringBuilder();
    private boolean splitImageCreated = false;

    public GuiFontSequenceBuilder(ResourcePack resourcePack, String fontIdentifier) throws IOException {
        this.resourcePack = resourcePack;
        this.font = new Font(resourcePack, fontIdentifier);
    }

    /**
     * Create the {@link FontSequence} from this builder.
     *
     * @return The built {@link FontSequence}.
     */
    public FontSequence build() {
        return new FontSequence(this.font.getIdentifier(), this.fontChars.toString());
    }

    /**
     * Shift the cursor to the left by the specified number of pixels.
     * <p>
     * This will alter the x-position of all subsequent renders. It is therefore
     * important to shift back to avoid unwanted shift.
     *
     * @param pixels The number of pixels to shift to the left.
     * @return The builder, for chaining.
     * @throws IOException If an I/O error occurs.
     */
    @Contract("_ -> this")
    public GuiFontSequenceBuilder shiftLeft(int pixels) throws IOException {
        this.shiftRight(-pixels);
        return this;
    }

    /**
     * Shift the cursor to the right by the specified number of pixels.
     * <p>
     * This will alter the x-position of all subsequent renders. It is therefore
     * important to shift back to avoid unwanted shift.
     *
     * @param pixels The number of pixels to shift to the right.
     * @return The builder, for chaining.
     * @throws IOException If an I/O error occurs.
     */
    @Contract("_ -> this")
    public GuiFontSequenceBuilder shiftRight(int pixels) throws IOException {
        char c = this.font.addSpace(pixels);
        this.fontChars.append(c);
        return this;
    }

    /**
     * Insert a character that ensures things rendered before this character are
     * rendered behind and the things that come after are rendered in front of the
     * things that come behind.
     * <p>
     * It almost works as declaring a "new layer".
     *
     * @throws IOException If an I/O error occurs.
     */
    protected void newLayer() throws IOException {
        // We create what is known as a "splitting" character. This character is large
        // enough that rendering is split, causing predictable z-index ordering.
        String key = "praeter_gui:split.png";

        if (!this.splitImageCreated) {
            // A transparent 256x256 image is large enough to split rendering.
            BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

            // One pixel is set to an almost-transparent color. This is required to be able
            // to create a zero-width character. If it is fully transparent, the game always
            // shifts by one pixel to the right, regardless of the height.
            image.setRGB(255, 0, 0x11000000);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            Path path = this.resourcePack.getTexturePath(key);
            Files.createDirectories(path.getParent());
            Files.write(path, stream.toByteArray());
            this.splitImageCreated = true;
        }
        // A height of -2 means the character will shift left enough to cancel out its
        // own shift to the right, effectively making it a zero-width character.
        char c = this.font.addBitmap(key, -2, -Short.MAX_VALUE);
        this.fontChars.append(c);
    }

    /**
     * Draw an image.
     *
     * @param textureIdentifier The key of the texture to render. The key is relative
     *                          to the textures folder and may or may not contain the
     *                          file extension.
     * @param x The x offset to render the image at, in pixels.
     * @param y The y offset to render the image at, in pixels.
     * @return The same instance, for chaining.
     * @throws IOException If an I/O error occurs.
     */
    @Contract("_, _, _ -> this")
    public GuiFontSequenceBuilder drawImage(String textureIdentifier, int x, int y) throws IOException {
        // Ensure the key ends with .png, which is required for fonts
        if (!textureIdentifier.endsWith(".png")) {
            textureIdentifier += ".png";
        }

        // To ensure the things drawn last are drawn on top, we need to insert a special
        // character. It almost works as declaring a "new layer". We do that now to
        // ensure the coming image will be displayed in front of everything before it in
        // case there is overlap.
        this.newLayer();

        // x offset: shift right with spaces (and shift back afterward)
        // y offset: use the character ascent
        this.shiftRight(x);
        int ascent = -y;

        // Read the texture
        Path texturePath = this.resourcePack.getTexturePath(textureIdentifier);
        BufferedImage image = ImageIO.read(Files.newInputStream(texturePath));
        int height = Math.max(image.getWidth(), image.getHeight());

        // Bitmap font providers don't allow the ascent to be larger than the height of
        // the character. If that is the case, we must create a new image that is big
        // enough. The rest of the area will just be transparent.
        if (ascent > height) {
            height = ascent;
        }

        // If the current image isn't already the expected height
        if (image.getHeight() != height) {
            BufferedImage createdImage = new BufferedImage(image.getWidth(), height, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = createdImage.getGraphics();

            // Draw the image in the top-left corner.
            graphics.drawImage(image, 0, 0, null);

            // Create the new key for the image
            int extIndex = textureIdentifier.lastIndexOf('.');
            String generatedIdentifier = PraeterGui.GENERATED_NAMESPACE + ':' +
                textureIdentifier.substring(0, extIndex).replace(':', '/') +
                "_" + height +
                textureIdentifier.substring(extIndex);

            // Save the image
            Path path = this.resourcePack.getTexturePath(generatedIdentifier);
            Files.createDirectories(path.getParent());
            try (OutputStream stream = Files.newOutputStream(path)) {
                ImageIO.write(createdImage, "png", stream);
            }
        }

        // Add the font character to the fonts
        char c = this.font.addBitmap(textureIdentifier, height, ascent);
        this.fontChars.append(c);

        // Shift back, and the image has an effective width we need to move back by,
        // and an additional pixel for the single-pixel-wide space after the character.
        this.shiftLeft(x + getEffectiveWidth(image) + 1);

        return this;
    }

    /**
     * Get the effective width of the image, the width the game will advance the text
     * "cursor" by.
     *
     * @param image The image.
     * @return The effective width.
     */
    public static int getEffectiveWidth(BufferedImage image) {
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
     * Draw an image.
     *
     * @param image The image to draw.
     * @param x The x offset to render the image at, in pixels.
     * @param y The y offset to render the image at, in pixels.
     * @return The same instance, for chaining.
     * @throws IOException If an I/O error occurs.
     */
    @Contract("_, _, _ -> this")
    public GuiFontSequenceBuilder drawImage(BufferedImage image, int x, int y) throws IOException {
        // Write the image as a texture in the resource pack
        long hash = this.imageHashCode(image);
        String hashString = Long.toHexString(hash);
        String textureIdentifier = PraeterGui.GENERATED_NAMESPACE + ':' + hashString + ".png";
        Path texturePath = resourcePack.getTexturePath(textureIdentifier);
        Files.createDirectories(texturePath.getParent());
        try (OutputStream stream = Files.newOutputStream(texturePath)) {
            ImageIO.write(image, "png", stream);
        }
        // Then draw the image from that texture
        return drawImage(textureIdentifier, x, y);
    }

    private long imageHashCode(BufferedImage image) {
        long hash = 1;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                hash = 31 * hash + image.getRGB(x, y);
            }
        }
        return hash;
    }

}
