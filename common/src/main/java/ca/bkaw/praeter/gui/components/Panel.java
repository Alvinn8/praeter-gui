package ca.bkaw.praeter.gui.components;

import ca.bkaw.praeter.gui.click.ClickContext;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.render.RenderContext;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * An indented area that looks like a slot, but with any size.
 */
public class Panel {
    /**
     * The main background color of a panel.
     */
    public static final Color BACKGROUND_COLOR = new Color(139, 139, 139);

    /**
     * The darker color used on the edges of a panel.
     */
    public static final Color DARK_COLOR = new Color(55, 55, 55);

    /**
     * The lighter color used on the edges of a panel.
     */
    public static final Color LIGHT_COLOR = Color.WHITE;

    private Panel() {}

    /**
     * Render a panel at the specified position with the specified width and height.
     *
     * @param r The render context.
     * @param pos The position to render the panel at.
     * @param width The width of the panel.
     * @param height The height of the panel.
     */
    public static void panel(RenderContext r, DrawPos pos, int width, int height) {
        BufferedImage image = createPanelImage(width, height);
        r.drawImage(pos, image);
    }

    /**
     * Render a panel that occupies a rectangular area of slots and can be clicked.
     *
     * @param r The render context.
     * @param pos The top-left slot position of the panel.
     * @param widthInSlots The width of the panel, in slots.
     * @param heightInSlots The height of the panel, in slots.
     * @param onClick The handler to run when the panel is clicked, or null for no handler.
     */
    public static void panel(RenderContext r, SlotPos pos, int widthInSlots, int heightInSlots,
                              @Nullable Consumer<ClickContext> onClick) {
        int width = widthInSlots * DrawPos.SLOT_SIZE;
        int height = heightInSlots * DrawPos.SLOT_SIZE;
        panel(r, pos.cornerPixel(), width, height);
        if (onClick != null) {
            r.onClick(pos.region(widthInSlots, heightInSlots), onClick);
        }
    }

    /**
     * Create a panel image with the specified width and height.
     *
     * @param width The width of the image.
     * @param height The height of the image.
     * @return The created image.
     */
    public static BufferedImage createPanelImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        // Fill everything with the background color
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, width, height);

        // Left and top edge
        graphics.setColor(DARK_COLOR);
        graphics.fillRect(0, 0, width - 1, 1);
        graphics.fillRect(0, 0, 1, height - 1);

        // Right and bottom edge
        graphics.setColor(LIGHT_COLOR);
        graphics.fillRect(width - 1, 1, 1, height - 1);
        graphics.fillRect(1, height - 1, width - 1, 1);

        return image;
    }
}
