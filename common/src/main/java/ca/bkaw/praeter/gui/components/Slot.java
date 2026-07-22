package ca.bkaw.praeter.gui.components;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.slot.SlotPos;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import ca.bkaw.praeter.gui.slot.SlotBehavior;

import static ca.bkaw.praeter.gui.CommonHooks.useState;

/**
 * A slot where the user can take and place items.
 */
public class Slot {
    private GuiItem item = GuiItem.empty();

    private Slot() {}

    /**
     * A {@link Slot} in a gui that can hold any item.
     *
     * @param r The render context.
     * @param pos The position of the slot.
     * @return A reference to the slot state.
     */
    public static Ref<Slot> slot(RenderContext r, SlotPos pos) {
        return slot(r, pos, SlotBehavior.DEFAULT);
    }

    /**
     * A {@link Slot} in a gui with a specific behavior.
     * <p>
     * To make a slot that can only hold certain items, use {@code slotCanHold}
     * imported from the platform hooks.
     * <pre>
     *     SLOT = Slot.slot(r, SlotPos.of(0, 0), slotCanHold(item -> true or false));
     * </pre>
     * <p>
     * To make a slot that can never be modified by the player, or only modified by
     * certain players, use {@code slotCanModify} imported from the platform hooks.
     * <pre>
     *     STATIC_SLOT = Slot.slot(r, SlotPos.of(0, 0), slotCanModify(player -> false));
     * </pre>
     * <p>
     * To use both, use {@code slotBehavior} imported from the platform hooks.
     *
     * @param r The render context.
     * @param pos The position of the slot.
     * @param behavior The behavior of the slot.
     * @return A reference to the slot state.
     */
    public static Ref<Slot> slot(RenderContext r, SlotPos pos, SlotBehavior behavior) {
        Ref<Slot> ref = useState(r, Slot::new);
        r.addSlot(new GuiSlot(pos.slotIndex(), ref, behavior));

        // Render the slot using a panel
        Panel.panel(r, DrawPos.slotCorner(pos), DrawPos.SLOT_SIZE, DrawPos.SLOT_SIZE);

        return ref;
    }

    /**
     * Get the item in this slot as a {@link GuiItem}.
     * <p>
     * Usually, you want the platform-specific {@code ItemStack} type instead of this,
     * which can be obtained using {@code getSlotItem(slot, gui)} imported from the
     * platform hooks.
     *
     * @return The item in this slot.
     */
    public GuiItem getGuiItem() {
        return this.item;
    }

    /**
     * Set the item in this slot as a {@link GuiItem}.
     * <p>
     * Usually, you want to set using the platform-specific {@code ItemStack} type
     * instead of this, which can be done using {@code setSlotItem(slot, gui, item)}
     * imported from the platform hooks.
     *
     * @param item The item to set in this slot.
     */
    public void setGuiItem(GuiItem item) {
        this.item = item;
    }
}
