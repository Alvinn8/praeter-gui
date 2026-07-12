package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.BottomRegionType;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.platform.GuiPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles {@link SlotInteraction slot interactions}, simulating vanilla item
 * movement behavior.
 * <p>
 * The handler is a pure simulation and has no knowledge of the platform. It
 * operates on a {@link GuiSlotsState} snapshot and produces a
 * {@link SlotInteractionResult} describing the changes to apply. Changes to
 * custom gui slots are applied to the gui state before the result is returned.
 */
public class SlotInteractionHandler {
    private final CustomGui gui;
    private final CustomGuiType type;
    private final GuiSlotsState state;
    private final GuiPlayer player;
    private final Set<Integer> dirtySlots = new LinkedHashSet<>();
    private final List<GuiItem> drops = new ArrayList<>();
    private boolean cursorChanged;
    private boolean offhandChanged;

    private SlotInteractionHandler(CustomGui gui, GuiSlotsState state, GuiPlayer player) {
        this.gui = gui;
        this.type = gui.getType();
        this.state = state;
        this.player = player;
    }

    /**
     * Handle a slot interaction on the given gui.
     * <p>
     * Changes to custom gui slots are applied to the gui state. All other changes
     * are described by the returned result and must be applied by the caller.
     *
     * @param gui The gui instance the interaction was performed on.
     * @param state The current state of the screen. Will be mutated.
     * @param interaction The interaction to handle.
     * @param player The player that performed the interaction.
     * @return The result describing the changes.
     */
    public static SlotInteractionResult handle(CustomGui gui, GuiSlotsState state, SlotInteraction interaction, GuiPlayer player) {
        SlotInteractionHandler handler = new SlotInteractionHandler(gui, state, player);
        handler.process(interaction);
        return handler.buildResult();
    }

    private void process(SlotInteraction interaction) {
        switch (interaction) {
            case SlotInteraction.LeftClick(int slotIndex) -> this.click(slotIndex, false);
            case SlotInteraction.RightClick(int slotIndex) -> this.click(slotIndex, true);
            case SlotInteraction.ShiftClick(int slotIndex) -> this.shiftClick(slotIndex);
            case SlotInteraction.HotbarSwap(int slotIndex, int hotbarSlot) -> this.hotbarSwap(slotIndex, hotbarSlot);
            case SlotInteraction.OffhandSwap(int slotIndex) -> this.offhandSwap(slotIndex);
            case SlotInteraction.LeftClickOutside() -> this.dropCursor(true);
            case SlotInteraction.RightClickOutside() -> this.dropCursor(false);
            case SlotInteraction.DropSlot(int slotIndex, boolean all) -> this.dropSlot(slotIndex, all);
            case SlotInteraction.Drag(SlotInteraction.DragType dragType, List<Integer> slotIndices) -> this.drag(dragType, slotIndices);
            case SlotInteraction.DoubleClick(int slotIndex) -> this.doubleClick(slotIndex);
            case SlotInteraction.Clone(int slotIndex) -> this.cloneStack(slotIndex);
        }
    }

    private SlotInteractionResult buildResult() {
        Map<Integer, GuiItem> playerInventoryChanges = new LinkedHashMap<>();
        boolean customSlotsChanged = false;
        for (int slotIndex : this.dirtySlots) {
            GuiItem item = this.state.getItem(slotIndex);
            GuiSlot guiSlot = this.type.getGuiSlotAt(slotIndex);
            if (guiSlot != null) {
                Slot slotState = guiSlot.getRef().get(this.gui);
                slotState.setGuiItem(item);
                customSlotsChanged = true;
            } else {
                playerInventoryChanges.put(slotIndex, item);
            }
        }
        return new SlotInteractionResult(
            playerInventoryChanges,
            this.state.getCursor(),
            this.cursorChanged,
            this.offhandChanged ? this.state.getOffhand() : null,
            this.drops,
            customSlotsChanged
        );
    }

    // Mutation helpers

    private void setSlot(int slotIndex, GuiItem item) {
        this.state.setItem(slotIndex, item);
        this.dirtySlots.add(slotIndex);
    }

    private void setCursor(GuiItem item) {
        this.state.setCursor(item);
        this.cursorChanged = true;
    }

    private void setOffhand(GuiItem item) {
        this.state.setOffhand(item);
        this.offhandChanged = true;
    }

    /**
     * Get a copy of the item with the given amount, or the empty item if the amount
     * is zero or less.
     */
    private static GuiItem sized(GuiItem item, int amount) {
        return amount <= 0 ? GuiItem.empty() : item.withAmount(amount);
    }

    // Slot semantics

    /**
     * Whether a slot exists at the given raw slot index. Positions in the gui
     * without a registered custom slot do not have slots and cannot be interacted
     * with, even though the game client believes there is a slot there.
     */
    private boolean slotExists(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.state.getSlotCount()) {
            return false;
        }
        if (this.type.getGuiSlotAt(slotIndex) != null) {
            return true;
        }
        // Bottom region slots are all plain slots when the bottom region is the
        // player's inventory. Otherwise, only registered custom slots exist.
        return slotIndex >= this.state.getTopSlotCount()
            && this.type.getBottomRegionType() == BottomRegionType.PLAYER_INVENTORY;
    }

    /**
     * Whether the slot at the given raw slot index cannot hold the given item.
     */
    private boolean cannotHold(int slotIndex, GuiItem item) {
        GuiSlot guiSlot = this.type.getGuiSlotAt(slotIndex);
        if (guiSlot != null) {
            return !guiSlot.getBehavior().canHold(item);
        }
        return false;
    }

    /**
     * Whether the player may change the contents of the slot at the given slot index.
     */
    private boolean mayNotChange(int slotIndex) {
        GuiSlot guiSlot = this.type.getGuiSlotAt(slotIndex);
        return guiSlot != null && !guiSlot.getBehavior().mayChange(this.player);
    }

    // Interactions

    private void click(int slotIndex, boolean rightClick) {
        if (!this.slotExists(slotIndex) || this.mayNotChange(slotIndex)) {
            return;
        }
        GuiItem cursor = this.state.getCursor();
        GuiItem slotItem = this.state.getItem(slotIndex);
        if (cursor.isEmpty()) {
            if (slotItem.isEmpty()) {
                return;
            }
            // Pick up the stack, or half of it (rounded up) when right-clicking.
            int take = rightClick ? (slotItem.getAmount() + 1) / 2 : slotItem.getAmount();
            this.setCursor(slotItem.withAmount(take));
            this.setSlot(slotIndex, sized(slotItem, slotItem.getAmount() - take));
        } else if (slotItem.isEmpty()) {
            if (this.cannotHold(slotIndex, cursor)) {
                return;
            }
            // Place the stack, or a single item when right-clicking.
            int place = rightClick ? 1 : cursor.getAmount();
            this.setSlot(slotIndex, cursor.withAmount(place));
            this.setCursor(sized(cursor, cursor.getAmount() - place));
        } else if (slotItem.canStackWith(cursor)) {
            if (this.cannotHold(slotIndex, cursor)) {
                return;
            }
            // Merge the cursor into the slot.
            int place = rightClick ? 1 : cursor.getAmount();
            place = Math.min(place, slotItem.getMaxStackSize() - slotItem.getAmount());
            if (place <= 0) {
                return;
            }
            this.setSlot(slotIndex, slotItem.withAmount(slotItem.getAmount() + place));
            this.setCursor(sized(cursor, cursor.getAmount() - place));
        } else {
            if (this.cannotHold(slotIndex, cursor)) {
                return;
            }
            // Different items, swap the cursor and slot contents.
            this.setSlot(slotIndex, cursor);
            this.setCursor(slotItem);
        }
    }

    private void shiftClick(int slotIndex) {
        if (!this.slotExists(slotIndex) || this.mayNotChange(slotIndex)) {
            return;
        }
        GuiItem item = this.state.getItem(slotIndex);
        if (item.isEmpty()) {
            return;
        }
        int topSlotCount = this.state.getTopSlotCount();
        if (slotIndex < topSlotCount) {
            // Move to the bottom region, scanning backwards like vanilla.
            this.moveToRange(slotIndex, topSlotCount, this.state.getSlotCount(), true);
        } else {
            // Move to the top gui, scanning forwards.
            this.moveToRange(slotIndex, 0, topSlotCount, false);
        }
    }

    /**
     * Move the contents of a slot into a range of slots, merging with existing
     * stacks first and then filling empty slots. Mirrors the logic of vanilla's
     * {@code AbstractContainerMenu#moveItemStackTo}.
     *
     * @param sourceSlotIndex The slot to move items from.
     * @param start The start of the target range (inclusive).
     * @param end The end of the target range (exclusive).
     * @param backwards Whether to scan the range backwards.
     */
    private void moveToRange(int sourceSlotIndex, int start, int end, boolean backwards) {
        GuiItem source = this.state.getItem(sourceSlotIndex);
        int remaining = source.getAmount();

        // First pass: merge into existing stacks of the same item.
        if (source.getMaxStackSize() > 1) {
            for (int i = 0; i < end - start && remaining > 0; i++) {
                int slotIndex = backwards ? end - 1 - i : start + i;
                if (!this.slotExists(slotIndex) || this.cannotHold(slotIndex, source) || this.mayNotChange(slotIndex)) {
                    continue;
                }
                GuiItem target = this.state.getItem(slotIndex);
                if (target.isEmpty() || !target.canStackWith(source)) {
                    continue;
                }
                int move = Math.min(remaining, target.getMaxStackSize() - target.getAmount());
                if (move <= 0) {
                    continue;
                }
                this.setSlot(slotIndex, target.withAmount(target.getAmount() + move));
                remaining -= move;
            }
        }

        // Second pass: fill empty slots.
        for (int i = 0; i < end - start && remaining > 0; i++) {
            int slotIndex = backwards ? end - 1 - i : start + i;
            if (!this.slotExists(slotIndex) || this.cannotHold(slotIndex, source) || this.mayNotChange(slotIndex)) {
                continue;
            }
            if (!this.state.getItem(slotIndex).isEmpty()) {
                continue;
            }
            int move = Math.min(remaining, source.getMaxStackSize());
            this.setSlot(slotIndex, source.withAmount(move));
            remaining -= move;
        }

        if (remaining != source.getAmount()) {
            this.setSlot(sourceSlotIndex, sized(source, remaining));
        }
    }

    private void hotbarSwap(int slotIndex, int hotbarSlot) {
        if (hotbarSlot < 0 || hotbarSlot > 8) {
            return;
        }
        if (this.type.getBottomRegionType() != BottomRegionType.PLAYER_INVENTORY) {
            // There is no real hotbar on the screen to swap with.
            return;
        }
        int hotbarSlotIndex = this.state.getHotbarSlotIndex(hotbarSlot);
        if (!this.slotExists(slotIndex) || slotIndex == hotbarSlotIndex || this.mayNotChange(slotIndex)) {
            return;
        }
        GuiItem slotItem = this.state.getItem(slotIndex);
        GuiItem hotbarItem = this.state.getItem(hotbarSlotIndex);
        if (slotItem.isEmpty() && hotbarItem.isEmpty()) {
            return;
        }
        if (!hotbarItem.isEmpty() && this.cannotHold(slotIndex, hotbarItem)) {
            return;
        }
        this.setSlot(slotIndex, hotbarItem);
        this.setSlot(hotbarSlotIndex, slotItem);
    }

    private void offhandSwap(int slotIndex) {
        if (this.type.getBottomRegionType() != BottomRegionType.PLAYER_INVENTORY) {
            // The player's real inventory is not on the screen, so keep the
            // offhand out of reach as well.
            return;
        }
        if (!this.slotExists(slotIndex) || this.mayNotChange(slotIndex)) {
            return;
        }
        GuiItem slotItem = this.state.getItem(slotIndex);
        GuiItem offhandItem = this.state.getOffhand();
        if (slotItem.isEmpty() && offhandItem.isEmpty()) {
            return;
        }
        if (!offhandItem.isEmpty() && this.cannotHold(slotIndex, offhandItem)) {
            return;
        }
        this.setSlot(slotIndex, offhandItem);
        this.setOffhand(slotItem);
    }

    private void dropCursor(boolean all) {
        GuiItem cursor = this.state.getCursor();
        if (cursor.isEmpty()) {
            return;
        }
        int drop = all ? cursor.getAmount() : 1;
        this.drops.add(cursor.withAmount(drop));
        this.setCursor(sized(cursor, cursor.getAmount() - drop));
    }

    private void dropSlot(int slotIndex, boolean all) {
        if (!this.state.getCursor().isEmpty()) {
            // Vanilla only allows dropping from slots when the cursor is empty.
            return;
        }
        if (!this.slotExists(slotIndex) || this.mayNotChange(slotIndex)) {
            return;
        }
        GuiItem slotItem = this.state.getItem(slotIndex);
        if (slotItem.isEmpty()) {
            return;
        }
        int drop = all ? slotItem.getAmount() : 1;
        this.drops.add(slotItem.withAmount(drop));
        this.setSlot(slotIndex, sized(slotItem, slotItem.getAmount() - drop));
    }

    private void doubleClick(int slotIndex) {
        GuiItem cursor = this.state.getCursor();
        if (cursor.isEmpty()) {
            return;
        }
        if (this.slotExists(slotIndex) && !this.state.getItem(slotIndex).isEmpty()) {
            // Vanilla only collects when the clicked slot is empty.
            return;
        }
        int slotCount = this.state.getSlotCount();
        // First pass only collects from partial stacks, second pass from full stacks.
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < slotCount; i++) {
                if (cursor.getAmount() >= cursor.getMaxStackSize()) {
                    break;
                }
                if (!this.slotExists(i) || this.mayNotChange(i)) {
                    continue;
                }
                GuiItem item = this.state.getItem(i);
                if (item.isEmpty() || !item.canStackWith(cursor)) {
                    continue;
                }
                if (pass == 0 && item.getAmount() >= item.getMaxStackSize()) {
                    continue;
                }
                int take = Math.min(item.getAmount(), cursor.getMaxStackSize() - cursor.getAmount());
                this.setSlot(i, sized(item, item.getAmount() - take));
                cursor = cursor.withAmount(cursor.getAmount() + take);
                this.setCursor(cursor);
            }
        }
    }

    private void drag(SlotInteraction.DragType dragType, List<Integer> slotIndices) {
        GuiItem cursor = this.state.getCursor();
        if (cursor.isEmpty()) {
            return;
        }

        // Filter to slots that can accept the dragged item.
        List<Integer> validSlots = new ArrayList<>(slotIndices.size());
        for (int slotIndex : slotIndices) {
            if (!this.slotExists(slotIndex) || this.cannotHold(slotIndex, cursor) || this.mayNotChange(slotIndex)) {
                continue;
            }
            GuiItem existing = this.state.getItem(slotIndex);
            if (!existing.isEmpty() && !existing.canStackWith(cursor)) {
                continue;
            }
            validSlots.add(slotIndex);
        }
        if (validSlots.isEmpty()) {
            return;
        }
        if (validSlots.size() == 1) {
            // Vanilla treats a single-slot drag as a regular click.
            int slotIndex = validSlots.getFirst();
            switch (dragType) {
                case LEFT -> this.click(slotIndex, false);
                case RIGHT -> this.click(slotIndex, true);
                case MIDDLE -> this.cloneStack(slotIndex);
            }
            return;
        }

        int placePerSlot = switch (dragType) {
            case LEFT -> cursor.getAmount() / validSlots.size();
            case RIGHT -> 1;
            case MIDDLE -> cursor.getMaxStackSize();
        };
        int remaining = cursor.getAmount();
        for (int slotIndex : validSlots) {
            GuiItem existing = this.state.getItem(slotIndex);
            int existingAmount = existing.isEmpty() ? 0 : existing.getAmount();
            int place = Math.min(placePerSlot, cursor.getMaxStackSize() - existingAmount);
            if (dragType != SlotInteraction.DragType.MIDDLE) {
                place = Math.min(place, remaining);
            }
            if (place <= 0) {
                continue;
            }
            this.setSlot(slotIndex, cursor.withAmount(existingAmount + place));
            if (dragType != SlotInteraction.DragType.MIDDLE) {
                remaining -= place;
            }
        }
        if (dragType != SlotInteraction.DragType.MIDDLE && remaining != cursor.getAmount()) {
            this.setCursor(sized(cursor, remaining));
        }
    }

    private void cloneStack(int slotIndex) {
        if (!this.state.getCursor().isEmpty()) {
            return;
        }
        if (!this.slotExists(slotIndex)) {
            return;
        }
        GuiItem slotItem = this.state.getItem(slotIndex);
        if (slotItem.isEmpty()) {
            return;
        }
        this.setCursor(slotItem.withAmount(slotItem.getMaxStackSize()));
    }
}