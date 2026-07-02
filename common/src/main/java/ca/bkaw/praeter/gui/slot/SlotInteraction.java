package ca.bkaw.praeter.gui.slot;

import java.util.List;

/**
 * A platform-agnostic representation of an interaction a player performs on the
 * slots of an open gui.
 * <p>
 * Slots are identified by their raw slot index. The raw slot index space covers
 * the entire open screen: indexes {@code [0, topSlotCount)} are the slots in the
 * top gui, followed by the 27 player inventory slots (top row first) and lastly
 * the 9 hotbar slots. This matches the slot numbering used by the game protocol.
 * <p>
 * Platforms translate their native events or packets into instances of this type
 * and pass them to {@link SlotInteractionHandler}.
 */
public sealed interface SlotInteraction {

    /**
     * A left click on a slot. Picks up the slot contents, places the cursor contents,
     * merges stacks, or swaps the cursor and slot contents.
     *
     * @param rawSlot The raw slot index that was clicked.
     */
    record PickupLeft(int rawSlot) implements SlotInteraction {}

    /**
     * A right click on a slot. Picks up half the slot contents, or places a single
     * item from the cursor.
     *
     * @param rawSlot The raw slot index that was clicked.
     */
    record PickupRight(int rawSlot) implements SlotInteraction {}

    /**
     * A shift click on a slot, quickly moving the contents to the other region of
     * the screen.
     *
     * @param rawSlot The raw slot index that was clicked.
     */
    record ShiftClick(int rawSlot) implements SlotInteraction {}

    /**
     * A number key press while hovering a slot, swapping the slot contents with the
     * hotbar slot.
     *
     * @param rawSlot The raw slot index that was hovered.
     * @param hotbarSlot The hotbar slot to swap with. [0-8]
     */
    record HotbarSwap(int rawSlot, int hotbarSlot) implements SlotInteraction {}

    /**
     * An offhand swap key press while hovering a slot, swapping the slot contents
     * with the offhand item.
     *
     * @param rawSlot The raw slot index that was hovered.
     */
    record OffhandSwap(int rawSlot) implements SlotInteraction {}

    /**
     * A click outside the screen while holding items on the cursor, dropping them.
     *
     * @param all Whether the entire cursor stack is dropped (left click), or a
     *            single item (right click).
     */
    record DropCursor(boolean all) implements SlotInteraction {}

    /**
     * A drop key press while hovering a slot, dropping items from the slot.
     *
     * @param rawSlot The raw slot index that was hovered.
     * @param all Whether the entire stack is dropped (Ctrl+Q), or a single
     *            item (Q).
     */
    record DropSlot(int rawSlot, boolean all) implements SlotInteraction {}

    /**
     * A completed drag ("quick craft") that distributes the cursor stack over
     * multiple slots.
     *
     * @param type The type of the drag.
     * @param rawSlots The raw slot indexes that were dragged over, in the order
     *                 they were added to the drag.
     */
    record Drag(DragType type, List<Integer> rawSlots) implements SlotInteraction {}

    /**
     * A double click on a slot, collecting all items that can stack with the cursor
     * into the cursor.
     *
     * @param rawSlot The raw slot index that was clicked.
     * @param reverse Whether slots are scanned in reverse order.
     */
    record DoubleClick(int rawSlot, boolean reverse) implements SlotInteraction {}

    /**
     * A middle click on a slot in creative mode, cloning the slot contents to a
     * full stack on the cursor.
     * <p>
     * Platforms must only create this interaction for players that are allowed to
     * clone items (creative mode players).
     *
     * @param rawSlot The raw slot index that was clicked.
     */
    record Clone(int rawSlot) implements SlotInteraction {}
}
