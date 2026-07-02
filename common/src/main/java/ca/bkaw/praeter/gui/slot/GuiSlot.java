package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.player.GuiPlayer;

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
    private final SlotBehavior behavior;

    /**
     * Create a new gui slot definition.
     *
     * @param rawSlot The raw slot index of the slot.
     * @param ref The reference to the per-instance slot state.
     * @param behavior The behavior of the slot.
     */
    public GuiSlot(int rawSlot, Ref<Slot> ref, SlotBehavior behavior) {
        this.rawSlot = rawSlot;
        this.ref = ref;
        this.behavior = behavior;
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
        return this.behavior.canHold(item);
    }

    /**
     * Whether the given player may change the contents of this slot.
     *
     * @param player The player.
     * @return Whether the player may change the slot.
     */
    public boolean mayChange(GuiPlayer player) {
        return this.behavior.mayChange(player);
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
