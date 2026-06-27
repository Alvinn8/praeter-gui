package ca.bkaw.praeter.gui.draw;

import ca.bkaw.praeter.gui.pack.ResourcePack;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuiBackgroundPainter {
    /**
     * The key to the generic_54 texture, relative to the textures folder and including
     * the file extension.
     */
    public static final String GENERIC_54_TEXTURE
        = "minecraft:gui/container/generic_54.png";

    /**
     * The width of the background image.
     */
    public static final int WIDTH
        = DrawPos.HORIZONTAL_PADDING + 9 * DrawPos.SLOT_SIZE + DrawPos.HORIZONTAL_PADDING;

    /**
     * The height of the background image when there are no rows of slots. This is the
     * height of the player inventory and hotbar, plus the top edge and padding.
     */
    public static final int ZERO_ROWS_HEIGHT
        = DrawPos.TOP_PADDING + DrawPos.INVENTORY_VIEW_GAP + 4 * DrawPos.SLOT_SIZE + DrawPos.HOTBAR_GAP + DrawPos.BOTTOM_PADDING;

    private final BufferedImage image;
    private final ResourcePack resourcePack;
    private final ResourcePack vanillaAssets;

    public GuiBackgroundPainter(int rows, ResourcePack resourcePack, ResourcePack vanillaAssets) throws IOException {
        int height = ZERO_ROWS_HEIGHT + rows * DrawPos.SLOT_SIZE;
        this.image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_ARGB);
        this.resourcePack = resourcePack;
        this.vanillaAssets = vanillaAssets;
        this.paintBackground(rows);
    }

    /**
     * Get the image to use as the background.
     *
     * @return The image.
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * Paint the vanilla background onto the image, before any custom drawing is done.
     * @throws IOException If an I/O error occurs.
     */
    private void paintBackground(int rows) throws IOException {
        Path generic54Path = this.vanillaAssets.getTexturePath(GENERIC_54_TEXTURE);

        BufferedImage generic54 = ImageIO.read(Files.newInputStream(generic54Path));

        Graphics2D graphics = this.image.createGraphics();

        // Draw the top padding
        BufferedImage topEdge = generic54.getSubimage(0, 0, WIDTH, DrawPos.TOP_PADDING);
        graphics.drawImage(topEdge, 0, 0, null);

        // Get the row right below the top edge. This is the row of pixels we will loop
        // for the content
        BufferedImage pixelRow = generic54.getSubimage(0, DrawPos.TOP_EDGE_HEIGHT + 1, WIDTH, 1);
        for (int i = 0; i < rows * DrawPos.SLOT_SIZE; i++) {
            int y = DrawPos.TOP_PADDING + i;
            graphics.drawImage(pixelRow, 0, y, null);
        }

        // Draw the player inventory
        int sourcePlayerInventoryY = DrawPos.TOP_PADDING + 6 * DrawPos.SLOT_SIZE + 1; // generic_54 has 6 rows of slots
        int destPlayerInventoryY = DrawPos.TOP_PADDING + rows * DrawPos.SLOT_SIZE;
        BufferedImage playerInventory = generic54.getSubimage(0, sourcePlayerInventoryY, WIDTH, generic54.getHeight() - sourcePlayerInventoryY);
        graphics.drawImage(playerInventory, 0, destPlayerInventoryY, null);
    }

    /**
     * Carve out the specified area by replacing the pixels with transparency.
     * <p>
     * When carving over a slot, this results in the slot from the vanilla gui showing
     * trough, including the hover.
     *
     * @param x The x pixel coordinate,
     * @param y The y pixel coordinate,
     * @param width The width.
     * @param height The height.
     */
    public void carve(int x, int y, int width, int height) {
        for (int offsetX = 0; offsetX < width; offsetX++) {
            for (int offsetY = 0; offsetY < height; offsetY++) {
                int pixelX = x + offsetX;
                int pixelY = y + offsetY;
                this.image.setRGB(pixelX, pixelY, 0);
            }
        }
    }

    public void drawImage(String textureKey, int x, int y) throws IOException {
        // Read the image
        Path texturePath = this.resourcePack.getTexturePath(textureKey);
        if (!Files.exists(texturePath)) {
            throw new FileNotFoundException("Texture not found: " + textureKey);
        }
        BufferedImage image = ImageIO.read(Files.newInputStream(texturePath));

        // Draw the image
        this.drawImage(image, x, y);
    }

    public void drawImage(BufferedImage image, int x, int y) {
        Graphics2D graphics = this.image.createGraphics();
        graphics.drawImage(image, x, y, null);
    }

}
