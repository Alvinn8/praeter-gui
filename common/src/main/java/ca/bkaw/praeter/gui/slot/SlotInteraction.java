package ca.bkaw.praeter.gui.slot;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;

/**
 * An interaction a player performs on the slots of an open gui.
 * <p>
 * Slots are identified by their raw slot index. The raw slot index space covers
 * the entire open screen: indexes {@code [0, topSlotCount)} are the slots in the
 * top gui, followed by the 27 player inventory slots (top row first) and lastly
 * the 9 hotbar slots. This matches the slot numbering used by the game protocol.
 * <p>
 * This type represents the interaction itself, not the result of the interaction.
 * {@link SlotInteractionHandler} handles the interaction.
 */
public sealed interface SlotInteraction {
    record LeftClick(int slotIndex) implements SlotInteraction {}
    record RightClick(int slotIndex) implements SlotInteraction {}
    record ShiftClick(int slotIndex) implements SlotInteraction {}
    record DoubleClick(int slotIndex) implements SlotInteraction {}
    record HotbarSwap(int slotIndex, @Range(from = 0, to = 8) int hotbarSlot) implements SlotInteraction {}
    record OffhandSwap(int slotIndex) implements SlotInteraction {}
    record LeftClickOutside() implements SlotInteraction {}
    record RightClickOutside() implements SlotInteraction {}
    record DropSlot(int slotIndex, boolean all) implements SlotInteraction {}
    record Clone(int slotIndex) implements SlotInteraction {}
    record Drag(DragType type, List<Integer> slotIndices) implements SlotInteraction {}

    enum DragType {
        LEFT,
        RIGHT,
        MIDDLE
    }

    /**
     * Get the slot position of this interaction if it has one.
     *
     * @return The slot position, or null.
     */
    default @Nullable SlotPos getSlotPos() {
        return switch (this) {
            case LeftClick leftClick -> SlotPos.of(leftClick.slotIndex());
            case RightClick rightClick -> SlotPos.of(rightClick.slotIndex());
            case ShiftClick shiftClick -> SlotPos.of(shiftClick.slotIndex());
            case DoubleClick doubleClick -> SlotPos.of(doubleClick.slotIndex());
            case HotbarSwap hotbarSwap -> SlotPos.of(hotbarSwap.slotIndex());
            case OffhandSwap offhandSwap -> SlotPos.of(offhandSwap.slotIndex());
            case DropSlot dropSlot -> SlotPos.of(dropSlot.slotIndex());
            case Clone clone -> SlotPos.of(clone.slotIndex());
            default -> null;
        };
    }
}