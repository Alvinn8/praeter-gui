package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.GuiBackgroundPainter;
import ca.bkaw.praeter.gui.draw.GuiFontSequenceBuilder;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.font.Font;
import ca.bkaw.praeter.gui.pack.font.FontSequence;
import ca.bkaw.praeter.gui.text.TextRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that can simulate the rendering that the game performs for a gui, and
 * return the result as a {@link BufferedImage}.
 */
public class StandaloneRender {
    /**
     * The x offset from the corner of a gui to where the game render the title.
     */
    public static final int TITLE_OFFSET_X = -GuiFontSequenceBuilder.GUI_ORIGIN_OFFSET_X;

    /**
     * The y offset from the corner of a gui to where the game render the title.
     */
    public static final int TITLE_OFFSET_Y = -GuiFontSequenceBuilder.GUI_ORIGIN_OFFSET_Y;

    /**
     * Render the current state of a gui to an image.
     *
     * @param gui The gui to render.
     * @return A {@link BufferedImage} of the rendered gui.
     */
    public static BufferedImage render(CustomGui gui) {
        CustomGuiType type = gui.getType();
        List<RenderStep> renderSteps = type.getRenderSteps();
        if (renderSteps == null) {
            throw new IllegalStateException("Gui has not been registered");
        }

        int width = GuiBackgroundPainter.WIDTH;
        int height = GuiBackgroundPainter.ZERO_ROWS_HEIGHT
            + type.getHeight() * DrawPos.SLOT_SIZE;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        RenderDispatcher rd = new RenderDispatcher();
        for (RenderStep renderStep : renderSteps) {
            renderStep.render(rd, gui);
        }

        Map<String, Font> fontCache = new HashMap<>();

        int x = TITLE_OFFSET_X;
        for (FontSequence fontSequence : rd.getRenderTitle()) {
            Font font = fontCache.computeIfAbsent(fontSequence.fontIdentifier(), StandaloneRender::getFont);
            TextRenderer.renderText(image, fontSequence.text(), x, TITLE_OFFSET_Y, Color.WHITE, font);
            x += TextRenderer.getTextWidth(fontSequence.text(), font);
        }

        return image;
    }

    private static Font getFont(String identifier) {
        PraeterGuiAssets assets = PraeterGui.instance().getAssets();
        ResourcePack vanillaAssets = assets.getVanillaAssets();
        ResourcePack pack = assets.getResourcePack();
        if (vanillaAssets == null || pack == null) {
            throw new IllegalStateException("Assets have already been saved.");
        }
        // Check the generated pack first (e.g. praeter_gui:font), then vanilla assets.
        // We must check existence before constructing Font because Font creates an empty
        // font file when the font doesn't exist, rather than throwing.
        try {
            Font fromPack = Font.loadIfExists(pack, identifier);
            if (fromPack != null) return fromPack;
            Font fromVanilla = Font.loadIfExists(vanillaAssets, identifier);
            if (fromVanilla != null) return fromVanilla;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font: " + identifier, e);
        }
        throw new RuntimeException("Font not found in any pack: " + identifier);
    }
}
