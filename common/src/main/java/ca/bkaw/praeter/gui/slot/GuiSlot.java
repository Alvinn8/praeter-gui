package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.Ref;

/**
 * The definition of a slot in a custom gui type.
 * <p>
 * One instance exists per slot and gui type. The contents of the slot for a
 * specific gui instance is stored as gui state, accessed with the {@link Ref}.
 *
 * @see Slot#slot
 */
public class GuiSlot {
    private final int slotIndex;
    private final Ref<Slot> ref;
    private final SlotBehavior behavior;

    /**
     * Create a new gui slot definition.
     *
     * @param slotIndex The raw slot index in the inventory view.
     * @param ref A reference to the slot state.
     * @param behavior The behavior of the slot.
     */
    public GuiSlot(int slotIndex, Ref<Slot> ref, SlotBehavior behavior) {
        this.slotIndex = slotIndex;
        this.ref = ref;
        this.behavior = behavior;
    }

    /**
     * Get the slot index in the inventory view. Also known as the raw slot index.
     * <p>
     * For example, in a GUI with 1 row, the first slot of the player inventory is 9,
     * the second is 10, and so on.
     *
     * @return The slot index.
     */
    public int getSlotIndex() {
        return this.slotIndex;
    }

    /**
     * Get a reference to the slot state.
     *
     * @return A reference to the slot state.
     */
    public Ref<Slot> getRef() {
        return this.ref;
    }

    /**
     * Get the slot behavior that determines which items the slot can hold and which
     * players can modify the slot.
     *
     * @return The slot behavior.
     */
    public SlotBehavior getBehavior() {
        return this.behavior;
    }

}
