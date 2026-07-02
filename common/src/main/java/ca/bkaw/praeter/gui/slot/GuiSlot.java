package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.gui.Ref;

import java.util.function.Predicate;

/**
 * The definition of a slot in a custom gui type.
 * <p>
 * One instance exists per slot and gui type. The contents of the slot for a
 * specific gui instance is stored as gui state, accessed with the {@link Ref}.
 *
 * @see Slot#slot
 */
public class GuiSlot {
    private final int rawSlot;
    private final Ref<Slot> ref;
    private final Predicate<GuiItem> canHold;

    /**
     * Create a new gui slot definition.
     *
     * @param rawSlot The raw slot index of the slot.
     * @param ref The reference to the per-instance slot state.
     * @param canHold A predicate deciding which items the slot can hold.
     */
    public GuiSlot(int rawSlot, Ref<Slot> ref, Predicate<GuiItem> canHold) {
        this.rawSlot = rawSlot;
        this.ref = ref;
        this.canHold = canHold;
    }

    /**
     * Get the raw slot index of this slot.
     *
     * @return The raw slot index.
     */
    public int getRawSlot() {
        return this.rawSlot;
    }

    /**
     * Whether this slot can hold the given item.
     *
     * @param item The item.
     * @return Whether the slot can hold the item.
     */
    public boolean canHold(GuiItem item) {
        return this.canHold.test(item);
    }

    /**
     * Get the item in this slot for the given gui instance.
     *
     * @param gui The gui instance.
     * @return The item, or the empty item.
     */
    public GuiItem getItem(CustomGui gui) {
        return this.ref.get(gui).getItem();
    }

    /**
     * Set the item in this slot for the given gui instance.
     *
     * @param gui The gui instance.
     * @param item The item to set.
     */
    public void setItem(CustomGui gui, GuiItem item) {
        this.ref.get(gui).setItem(item);
    }
}
