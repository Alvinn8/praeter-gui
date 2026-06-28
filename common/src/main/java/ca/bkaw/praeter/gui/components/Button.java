package ca.bkaw.praeter.gui.components;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.font.Font;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.text.TextRenderer;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Button {

    /**
     * The texture key to the button texture.
     */
    public static final String BUTTON_TEXTURE = "minecraft:gui/sprites/widget/button.png";

    /**
     * The number of pixels of padding on the bottom of the button sprite.
     */
    public static final int BOTTOM_PADDING = 3;

    /**
     * The number of pixels of padding on the bottom of the button sprite.
     */
    public static final int TOP_PADDING = 3;

    /**
     * The number of pixels of padding on the left and right side of the button sprite.
     */
    public static final int HORIZONTAL_PADDING = 3;

    /**
     * A button component.
     *
     * @param r The render context.
     * @param pos The position to draw the button.
     * @param width The width, in pixels, of the button.
     * @param height The height, in pixels, of the button.
     * @return A reference to the button component.
     */
    public static Ref<Button> button(RenderContext r, String text, DrawPos pos, int width, int height) {
        Ref<Button> ref = r.useState(Button::new);

        BufferedImage image = createButtonImage(width, height);

        Font font = TextRenderer.defaultFont();
        int textWidth = TextRenderer.getTextWidth(text, font);
        int textHeight = TextRenderer.getTextHeight(text, font);
        int textX = (width - textWidth) / 2;
        int textY = (height - textHeight) / 2;
        TextRenderer.renderText(image, text, textX, textY, Color.WHITE, font);

        r.drawImage(pos, image);

        return ref;
    }

    /**
     * Create a button texture.
     *
     * @param width The width of the desired button.
     * @param height The height of the desired button. (Currently, must be 18.)
     * @return The created texture.
     */
    public static BufferedImage createButtonImage(int width, int height) {
        ResourcePack vanillaAssets = PraeterGui.instance().getAssets().getVanillaAssets();
        if (vanillaAssets == null) {
            throw new IllegalStateException("Cannot create button image because vanilla assets are not loaded.");
        }
        Path texturePath = vanillaAssets.getTexturePath(BUTTON_TEXTURE);
        BufferedImage sprite;
        try (InputStream stream = Files.newInputStream(texturePath)) {
            sprite = ImageIO.read(stream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load button texture", e);
        }
        int spriteWidth = sprite.getWidth();
        int spriteHeight = sprite.getHeight();

        // Width and height of the tileable center region of the sprite
        int centerSrcW = spriteWidth - 2 * HORIZONTAL_PADDING;
        int centerSrcH = spriteHeight - TOP_PADDING - BOTTOM_PADDING;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        // Draw the center (tiles the center region of the sprite)
        for (int x = HORIZONTAL_PADDING; x < width - HORIZONTAL_PADDING; x += centerSrcW) {
            int drawWidth = Math.min(centerSrcW, width - HORIZONTAL_PADDING - x);
            for (int y = TOP_PADDING; y < height - BOTTOM_PADDING; y += centerSrcH) {
                int drawHeight = Math.min(centerSrcH, height - BOTTOM_PADDING - y);
                drawPart(graphics, sprite, x, y, drawWidth, drawHeight, HORIZONTAL_PADDING, TOP_PADDING);
            }
        }

        // Draw the top and bottom edges
        for (int x = HORIZONTAL_PADDING; x < width - HORIZONTAL_PADDING; x += centerSrcW) {
            int drawWidth = Math.min(centerSrcW, width - HORIZONTAL_PADDING - x);
            drawPart(graphics, sprite, x, 0, drawWidth, TOP_PADDING, HORIZONTAL_PADDING, 0);
            drawPart(graphics, sprite, x, height - BOTTOM_PADDING, drawWidth, BOTTOM_PADDING, HORIZONTAL_PADDING, spriteHeight - BOTTOM_PADDING);
        }

        // Draw the left and right edges
        for (int y = TOP_PADDING; y < height - BOTTOM_PADDING; y += centerSrcH) {
            int drawHeight = Math.min(centerSrcH, height - BOTTOM_PADDING - y);
            drawPart(graphics, sprite, 0, y, HORIZONTAL_PADDING, drawHeight, 0, TOP_PADDING);
            drawPart(graphics, sprite, width - HORIZONTAL_PADDING, y, HORIZONTAL_PADDING, drawHeight, spriteWidth - HORIZONTAL_PADDING, TOP_PADDING);
        }

        // Draw the four corners
        drawPart(graphics, sprite, 0, 0, HORIZONTAL_PADDING, TOP_PADDING, 0, 0);
        drawPart(graphics, sprite, width - HORIZONTAL_PADDING, 0, HORIZONTAL_PADDING, TOP_PADDING, spriteWidth - HORIZONTAL_PADDING, 0);
        drawPart(graphics, sprite, 0, height - BOTTOM_PADDING, HORIZONTAL_PADDING, BOTTOM_PADDING, 0, spriteHeight - BOTTOM_PADDING);
        drawPart(graphics, sprite, width - HORIZONTAL_PADDING, height - BOTTOM_PADDING, HORIZONTAL_PADDING, BOTTOM_PADDING, spriteWidth - HORIZONTAL_PADDING, spriteHeight - BOTTOM_PADDING);

        graphics.dispose();
        return image;
    }

    private static void drawPart(Graphics2D graphics, BufferedImage sprite,
            int destX, int destY, int destWidth, int destHeight,
            int srcX, int srcY) {
        graphics.drawImage(sprite,
            destX, destY, destX + destWidth, destY + destHeight,
            srcX, srcY, srcX + destWidth, srcY + destHeight, null);
    }

}
