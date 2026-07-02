package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.gui.BottomRegionType;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.player.GuiPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles {@link SlotInteraction slot interactions} on a gui screen, simulating
 * vanilla item movement behavior.
 * <p>
 * The handler is a pure simulation and has no knowledge of the platform. It
 * operates on a {@link GuiScreenState} snapshot and produces a
 * {@link SlotInteractionResult} describing the changes to apply. Changes to
 * custom gui slots are applied to the gui state before the result is returned.
 */
public class SlotInteractionHandler {
    private final CustomGui gui;
    private final CustomGuiType type;
    private final GuiScreenState state;
    private final GuiPlayer player;
    private final Set<Integer> dirtySlots = new LinkedHashSet<>();
    private final List<GuiItem> drops = new ArrayList<>();
    private boolean cursorChanged;
    private boolean offhandChanged;

    private SlotInteractionHandler(CustomGui gui, GuiScreenState state, GuiPlayer player) {
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
    public static SlotInteractionResult handle(CustomGui gui, GuiScreenState state, SlotInteraction interaction, GuiPlayer player) {
        SlotInteractionHandler handler = new SlotInteractionHandler(gui, state, player);
        handler.process(interaction);
        return handler.buildResult();
    }

    private void process(SlotInteraction interaction) {
        switch (interaction) {
            case SlotInteraction.PickupLeft(int rawSlot) -> this.pickup(rawSlot, false);
            case SlotInteraction.PickupRight(int rawSlot) -> this.pickup(rawSlot, true);
            case SlotInteraction.ShiftClick(int rawSlot) -> this.shiftClick(rawSlot);
            case SlotInteraction.HotbarSwap(int rawSlot, int hotbarSlot) -> this.hotbarSwap(rawSlot, hotbarSlot);
            case SlotInteraction.OffhandSwap(int rawSlot) -> this.offhandSwap(rawSlot);
            case SlotInteraction.DropCursor(boolean all) -> this.dropCursor(all);
            case SlotInteraction.DropSlot(int rawSlot, boolean all) -> this.dropSlot(rawSlot, all);
            case SlotInteraction.Drag(DragType dragType, List<Integer> rawSlots) -> this.drag(dragType, rawSlots);
            case SlotInteraction.DoubleClick(int rawSlot, boolean reverse) -> this.doubleClick(rawSlot, reverse);
            case SlotInteraction.Clone(int rawSlot) -> this.cloneStack(rawSlot);
        }
    }

    private SlotInteractionResult buildResult() {
        Map<Integer, GuiItem> playerInventoryChanges = new LinkedHashMap<>();
        boolean customSlotsChanged = false;
        for (int rawSlot : this.dirtySlots) {
            GuiItem item = this.state.getSlot(rawSlot);
            GuiSlot guiSlot = this.type.getGuiSlotAt(rawSlot);
            if (guiSlot != null) {
                guiSlot.setItem(this.gui, item);
                customSlotsChanged = true;
            } else {
                playerInventoryChanges.put(rawSlot, item);
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

    private void setSlot(int rawSlot, GuiItem item) {
        this.state.setSlot(rawSlot, item);
        this.dirtySlots.add(rawSlot);
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
    private boolean slotExists(int rawSlot) {
        if (rawSlot < 0 || rawSlot >= this.state.getSlotCount()) {
            return false;
        }
        if (this.type.getGuiSlotAt(rawSlot) != null) {
            return true;
        }
        // Bottom region slots are all plain slots when the bottom region is the
        // player's inventory. Otherwise only registered custom slots exist.
        return rawSlot >= this.state.getTopSlotCount()
            && this.type.getBottomRegionType() == BottomRegionType.PLAYER_INVENTORY;
    }

    /**
     * Whether the slot at the given raw slot index can hold the given item.
     */
    private boolean canHold(int rawSlot, GuiItem item) {
        GuiSlot guiSlot = this.type.getGuiSlotAt(rawSlot);
        if (guiSlot != null) {
            return guiSlot.canHold(item);
        }
        return true;
    }

    /**
     * Whether the player may change the contents of the slot at the given raw slot
     * index.
     */
    private boolean mayChange(int rawSlot) {
        GuiSlot guiSlot = this.type.getGuiSlotAt(rawSlot);
        return guiSlot == null || guiSlot.mayChange(this.player);
    }

    // Interactions

    private void pickup(int rawSlot, boolean rightClick) {
        if (!this.slotExists(rawSlot) || !this.mayChange(rawSlot)) {
            return;
        }
        GuiItem cursor = this.state.getCursor();
        GuiItem slotItem = this.state.getSlot(rawSlot);
        if (cursor.isEmpty()) {
            if (slotItem.isEmpty()) {
                return;
            }
            // Pick up the stack, or half of it (rounded up) when right-clicking.
            int take = rightClick ? (slotItem.getAmount() + 1) / 2 : slotItem.getAmount();
            this.setCursor(slotItem.withAmount(take));
            this.setSlot(rawSlot, sized(slotItem, slotItem.getAmount() - take));
        } else if (slotItem.isEmpty()) {
            if (!this.canHold(rawSlot, cursor)) {
                return;
            }
            // Place the stack, or a single item when right-clicking.
            int place = rightClick ? 1 : cursor.getAmount();
            this.setSlot(rawSlot, cursor.withAmount(place));
            this.setCursor(sized(cursor, cursor.getAmount() - place));
        } else if (slotItem.canStackWith(cursor)) {
            if (!this.canHold(rawSlot, cursor)) {
                return;
            }
            // Merge the cursor into the slot.
            int place = rightClick ? 1 : cursor.getAmount();
            place = Math.min(place, slotItem.getMaxStackSize() - slotItem.getAmount());
            if (place <= 0) {
                return;
            }
            this.setSlot(rawSlot, slotItem.withAmount(slotItem.getAmount() + place));
            this.setCursor(sized(cursor, cursor.getAmount() - place));
        } else {
            if (!this.canHold(rawSlot, cursor)) {
                return;
            }
            // Different items, swap the cursor and slot contents.
            this.setSlot(rawSlot, cursor);
            this.setCursor(slotItem);
        }
    }

    private void shiftClick(int rawSlot) {
        if (!this.slotExists(rawSlot) || !this.mayChange(rawSlot)) {
            return;
        }
        GuiItem item = this.state.getSlot(rawSlot);
        if (item.isEmpty()) {
            return;
        }
        int topSlotCount = this.state.getTopSlotCount();
        if (rawSlot < topSlotCount) {
            // Move to the bottom region, scanning backwards like vanilla.
            this.moveToRange(rawSlot, topSlotCount, this.state.getSlotCount(), true);
        } else {
            // Move to the top gui, scanning forwards.
            this.moveToRange(rawSlot, 0, topSlotCount, false);
        }
    }

    /**
     * Move the contents of a slot into a range of slots, merging with existing
     * stacks first and then filling empty slots. Mirrors the logic of vanilla's
     * {@code AbstractContainerMenu#moveItemStackTo}.
     *
     * @param sourceRawSlot The slot to move items from.
     * @param start The start of the target range (inclusive).
     * @param end The end of the target range (exclusive).
     * @param backwards Whether to scan the range backwards.
     */
    private void moveToRange(int sourceRawSlot, int start, int end, boolean backwards) {
        GuiItem source = this.state.getSlot(sourceRawSlot);
        int remaining = source.getAmount();

        // First pass: merge into existing stacks of the same item.
        if (source.getMaxStackSize() > 1) {
            for (int i = 0; i < end - start && remaining > 0; i++) {
                int rawSlot = backwards ? end - 1 - i : start + i;
                if (!this.slotExists(rawSlot) || !this.canHold(rawSlot, source) || !this.mayChange(rawSlot)) {
                    continue;
                }
                GuiItem target = this.state.getSlot(rawSlot);
                if (target.isEmpty() || !target.canStackWith(source)) {
                    continue;
                }
                int move = Math.min(remaining, target.getMaxStackSize() - target.getAmount());
                if (move <= 0) {
                    continue;
                }
                this.setSlot(rawSlot, target.withAmount(target.getAmount() + move));
                remaining -= move;
            }
        }

        // Second pass: fill empty slots.
        for (int i = 0; i < end - start && remaining > 0; i++) {
            int rawSlot = backwards ? end - 1 - i : start + i;
            if (!this.slotExists(rawSlot) || !this.canHold(rawSlot, source) || !this.mayChange(rawSlot)) {
                continue;
            }
            if (!this.state.getSlot(rawSlot).isEmpty()) {
                continue;
            }
            int move = Math.min(remaining, source.getMaxStackSize());
            this.setSlot(rawSlot, source.withAmount(move));
            remaining -= move;
        }

        if (remaining != source.getAmount()) {
            this.setSlot(sourceRawSlot, sized(source, remaining));
        }
    }

    private void hotbarSwap(int rawSlot, int hotbarSlot) {
        if (hotbarSlot < 0 || hotbarSlot > 8) {
            return;
        }
        if (this.type.getBottomRegionType() != BottomRegionType.PLAYER_INVENTORY) {
            // There is no real hotbar on the screen to swap with.
            return;
        }
        int hotbarRawSlot = this.state.getHotbarRawSlot(hotbarSlot);
        if (!this.slotExists(rawSlot) || rawSlot == hotbarRawSlot || !this.mayChange(rawSlot)) {
            return;
        }
        GuiItem slotItem = this.state.getSlot(rawSlot);
        GuiItem hotbarItem = this.state.getSlot(hotbarRawSlot);
        if (slotItem.isEmpty() && hotbarItem.isEmpty()) {
            return;
        }
        if (!hotbarItem.isEmpty() && !this.canHold(rawSlot, hotbarItem)) {
            return;
        }
        this.setSlot(rawSlot, hotbarItem);
        this.setSlot(hotbarRawSlot, slotItem);
    }

    private void offhandSwap(int rawSlot) {
        if (this.type.getBottomRegionType() != BottomRegionType.PLAYER_INVENTORY) {
            // The player's real inventory is not on the screen, so keep the
            // offhand out of reach as well.
            return;
        }
        if (!this.slotExists(rawSlot) || !this.mayChange(rawSlot)) {
            return;
        }
        GuiItem slotItem = this.state.getSlot(rawSlot);
        GuiItem offhandItem = this.state.getOffhand();
        if (slotItem.isEmpty() && offhandItem.isEmpty()) {
            return;
        }
        if (!offhandItem.isEmpty() && !this.canHold(rawSlot, offhandItem)) {
            return;
        }
        this.setSlot(rawSlot, offhandItem);
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

    private void dropSlot(int rawSlot, boolean all) {
        if (!this.state.getCursor().isEmpty()) {
            // Vanilla only allows dropping from slots when the cursor is empty.
            return;
        }
        if (!this.slotExists(rawSlot) || !this.mayChange(rawSlot)) {
            return;
        }
        GuiItem slotItem = this.state.getSlot(rawSlot);
        if (slotItem.isEmpty()) {
            return;
        }
        int drop = all ? slotItem.getAmount() : 1;
        this.drops.add(slotItem.withAmount(drop));
        this.setSlot(rawSlot, sized(slotItem, slotItem.getAmount() - drop));
    }

    private void doubleClick(int rawSlot, boolean reverse) {
        GuiItem cursor = this.state.getCursor();
        if (cursor.isEmpty()) {
            return;
        }
        if (this.slotExists(rawSlot) && !this.state.getSlot(rawSlot).isEmpty()) {
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
                int scanSlot = reverse ? slotCount - 1 - i : i;
                if (!this.slotExists(scanSlot) || !this.mayChange(scanSlot)) {
                    continue;
                }
                GuiItem item = this.state.getSlot(scanSlot);
                if (item.isEmpty() || !item.canStackWith(cursor)) {
                    continue;
                }
                if (pass == 0 && item.getAmount() >= item.getMaxStackSize()) {
                    continue;
                }
                int take = Math.min(item.getAmount(), cursor.getMaxStackSize() - cursor.getAmount());
                this.setSlot(scanSlot, sized(item, item.getAmount() - take));
                cursor = cursor.withAmount(cursor.getAmount() + take);
                this.setCursor(cursor);
            }
        }
    }

    private void drag(DragType dragType, List<Integer> rawSlots) {
        GuiItem cursor = this.state.getCursor();
        if (cursor.isEmpty()) {
            return;
        }

        // Filter to slots that can accept the dragged item.
        List<Integer> validSlots = new ArrayList<>(rawSlots.size());
        for (int rawSlot : rawSlots) {
            if (!this.slotExists(rawSlot) || !this.canHold(rawSlot, cursor) || !this.mayChange(rawSlot)) {
                continue;
            }
            GuiItem existing = this.state.getSlot(rawSlot);
            if (!existing.isEmpty() && !existing.canStackWith(cursor)) {
                continue;
            }
            validSlots.add(rawSlot);
        }
        if (validSlots.isEmpty()) {
            return;
        }
        if (validSlots.size() == 1) {
            // Vanilla treats a single-slot drag as a regular click.
            int rawSlot = validSlots.getFirst();
            switch (dragType) {
                case EVEN -> this.pickup(rawSlot, false);
                case SINGLE -> this.pickup(rawSlot, true);
                case CLONE -> this.cloneStack(rawSlot);
            }
            return;
        }

        int placePerSlot = switch (dragType) {
            case EVEN -> cursor.getAmount() / validSlots.size();
            case SINGLE -> 1;
            case CLONE -> cursor.getMaxStackSize();
        };
        int remaining = cursor.getAmount();
        for (int rawSlot : validSlots) {
            GuiItem existing = this.state.getSlot(rawSlot);
            int existingAmount = existing.isEmpty() ? 0 : existing.getAmount();
            int place = Math.min(placePerSlot, cursor.getMaxStackSize() - existingAmount);
            if (dragType != DragType.CLONE) {
                place = Math.min(place, remaining);
            }
            if (place <= 0) {
                continue;
            }
            this.setSlot(rawSlot, cursor.withAmount(existingAmount + place));
            if (dragType != DragType.CLONE) {
                remaining -= place;
            }
        }
        if (dragType != DragType.CLONE && remaining != cursor.getAmount()) {
            this.setCursor(sized(cursor, remaining));
        }
    }

    private void cloneStack(int rawSlot) {
        if (!this.state.getCursor().isEmpty()) {
            return;
        }
        if (!this.slotExists(rawSlot)) {
            return;
        }
        GuiItem slotItem = this.state.getSlot(rawSlot);
        if (slotItem.isEmpty()) {
            return;
        }
        this.setCursor(slotItem.withAmount(slotItem.getMaxStackSize()));
    }
}
