package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.platform.GuiItem;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Renders an item at a slot position in a gui, without the position being a slot
 * that can be interacted with.
 * <p>
 * The item is computed from the gui instance each time the gui is updated, so the
 * displayed item can be fully dynamic. This can be used to display tooltips or to
 * show items for decorative purposes.
 *
 * @param slotIndex The raw slot index to render the item at.
 * @param itemFunction The function that computes the item to display.
 */
// Nullable since we cannot trust that consumers always return a non-null item.
public record ItemRenderer(int slotIndex, Function<CustomGui, @Nullable GuiItem> itemFunction) {

    /**
     * Get the item to display for the given gui instance.
     *
     * @param gui The gui instance.
     * @return The item, or the empty item.
     */
    public GuiItem getItem(CustomGui gui) {
        GuiItem item = this.itemFunction.apply(gui);
        return item == null ? GuiItem.empty() : item;
    }
}