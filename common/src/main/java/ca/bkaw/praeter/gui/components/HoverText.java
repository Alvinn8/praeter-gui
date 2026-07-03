package ca.bkaw.praeter.gui.components;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.render.RenderContext;

import java.util.List;

/**
 * Displays text when the user hovers a slot position in a gui.
 * <p>
 * The text is displayed using an invisible item whose tooltip contains the
 * text. The position cannot be interacted with.
 */
public class HoverText {
    private HoverText() {}

    /**
     * Display text when the user hovers the given slot position.
     *
     * @param r The render context.
     * @param pos The position to display the text at.
     * @param text The lines of text to display.
     */
    public static void hoverText(RenderContext r, SlotPos pos, String... text) {
        if (text.length == 0) {
            throw new IllegalArgumentException("At least one line of text must be provided.");
        }
        GuiItem item = PraeterGui.instance().getPlatform().createHoverTextItem(List.of(text));
        r.renderItem(pos, gui -> item);
    }
}
