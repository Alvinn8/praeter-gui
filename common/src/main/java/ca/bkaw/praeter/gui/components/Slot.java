package ca.bkaw.praeter.gui.components;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * A slot where the user can take and place items.
 * <p>
 * An instance of this class holds the contents of the slot for one gui instance.
 * Create and register slots with {@link #slot}.
 */
public class Slot {
    private GuiItem item = GuiItem.empty();

    private Slot() {}

    /**
     * Get the item in this slot.
     *
     * @return The item, or the empty item.
     */
    public GuiItem getItem() {
        return this.item;
    }

    /**
     * Set the item in this slot.
     * <p>
     * Remember to update the gui afterwards for viewers to see the change.
     *
     * @param item The item, where null is treated as the empty item.
     */
    public void setItem(@Nullable GuiItem item) {
        this.item = item == null ? GuiItem.empty() : item;
    }

    /**
     * A {@link Slot} in a gui that can hold any item.
     *
     * @param r The render context.
     * @param pos The position of the slot.
     * @return A reference to the slot state.
     */
    public static Ref<Slot> slot(RenderContext r, SlotPos pos) {
        return slot(r, pos, item -> true);
    }

    /**
     * A {@link Slot} in a gui that can only hold items that pass the given
     * predicate.
     *
     * @param r The render context.
     * @param pos The position of the slot.
     * @param canHold A predicate deciding which items the slot can hold.
     * @return A reference to the slot state.
     */
    public static Ref<Slot> slot(RenderContext r, SlotPos pos, Predicate<GuiItem> canHold) {
        Ref<Slot> ref = r.useState(Slot::new);
        r.addSlot(new GuiSlot(pos.slotIndex(), ref, canHold));

        // Render the slot using a panel
        Panel.panel(r, DrawPos.slotCorner(pos), DrawPos.SLOT_SIZE, DrawPos.SLOT_SIZE);

        return ref;
    }
}
