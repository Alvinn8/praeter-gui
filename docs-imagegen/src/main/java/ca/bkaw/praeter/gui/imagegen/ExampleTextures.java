package ca.bkaw.praeter.gui.imagegen;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Generates placeholder textures used in the documentation.
 */
public class ExampleTextures {
    private ExampleTextures() {}

    public static BufferedImage checkerboard(int width, int height, Color border, Color a, Color b) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        int tile = 8;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean even = ((x / tile) + (y / tile)) % 2 == 0;
                g.setColor(even ? a : b);
                g.fillRect(x, y, 1, 1);
            }
        }

        g.setColor(border);
        g.fillRect(0, 0, width, 2);
        g.fillRect(0, height - 2, width, 2);
        g.fillRect(0, 0, 2, height);
        g.fillRect(width - 2, 0, 2, height);

        g.dispose();
        return image;
    }

    public static BufferedImage icon(int size, Color border, Color fill, String label) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(fill);
        g.fillRect(0, 0, size, size);

        g.setColor(border);
        int borderSize = 2;
        g.fillRect(0, 0, size, borderSize);
        g.fillRect(0, size - borderSize, size, borderSize);
        g.fillRect(0, 0, borderSize, size);
        g.fillRect(size - borderSize, 0, borderSize, size);

        Font font = new Font(Font.SANS_SERIF, Font.BOLD, size / 2);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int textX = (size - metrics.stringWidth(label)) / 2;
        int textY = (size - metrics.getHeight()) / 2 + metrics.getAscent();
        g.setColor(Color.WHITE);
        g.drawString(label, textX, textY);

        g.dispose();
        return image;
    }
}
