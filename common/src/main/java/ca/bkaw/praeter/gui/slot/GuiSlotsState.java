package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.platform.GuiItem;

/**
 * A snapshot of slots in a gui view, including custom gui slots in the gui, the
 * player's inventory and the cursor and offhand items.
 * <p>
 * The slots are indexed by raw slot index, covering the top gui slots followed by
 * the 36 bottom region slots (27 player inventory slots, then 9 hotbar slots).
 */
public class GuiSlotsState {
    /**
     * The number of slots in the main player inventory, excluding the hotbar.
     */
    public static final int PLAYER_MAIN_SLOT_COUNT = 3 * 9;

    private final CustomGui gui;
    private final GuiItem[] playerInventorySlots;
    private GuiItem cursor;
    private GuiItem offhand;

    public GuiSlotsState(CustomGui gui, GuiItem[] playerInventorySlots, GuiItem cursor, GuiItem offhand) {
        if (gui.getType().getBottomRegionType().getSlotCount() != playerInventorySlots.length) {
            throw new IllegalArgumentException("Player inventory slot count does not match bottom region slot count");
        }
        this.gui = gui;
        this.playerInventorySlots = playerInventorySlots;
        this.cursor = cursor;
        this.offhand = offhand;
    }

    /**
     * Get the number of slots in the top gui.
     *
     * @return The top slot count.
     */
    public int getTopSlotCount() {
        return this.gui.getType().getTopRegionType().getSlotCount();
    }

    /**
     * Get the total number of slots on the screen, including the bottom region.
     *
     * @return The slot count.
     */
    public int getSlotCount() {
        return this.getTopSlotCount() + this.playerInventorySlots.length;
    }

    /**
     * Get the item in the given slot.
     *
     * @param slotIndex The raw slot index.
     * @return The item. The empty item is returned if there is an empty slot or if
     * there is no slot at the index.
     */
    public GuiItem getItem(int slotIndex) {
        int topSlotCount = this.getTopSlotCount();
        if (slotIndex < topSlotCount) {
            GuiSlot guiSlot = this.gui.getType().getGuiSlotAt(slotIndex);
            if (guiSlot == null) {
                return GuiItem.empty();
            }
            Slot slotState = guiSlot.getRef().get(this.gui);
            return slotState.getGuiItem();
        }
        if (slotIndex >= topSlotCount + this.playerInventorySlots.length) {
            return GuiItem.empty();
        }
        return this.playerInventorySlots[slotIndex - topSlotCount];
    }

    /**
     * Set the item in the given slot.
     *
     * @param slotIndex The raw slot index.
     * @param item The item.
     * @throws IllegalArgumentException If there is no slot at the index.
     */
    public void setItem(int slotIndex, GuiItem item) {
        int topSlotCount = this.getTopSlotCount();
        if (slotIndex < topSlotCount) {
            GuiSlot guiSlot = this.gui.getType().getGuiSlotAt(slotIndex);
            if (guiSlot == null) {
                throw new IllegalArgumentException("No slot at index " + slotIndex);
            }
            Slot slotState = guiSlot.getRef().get(this.gui);
            slotState.setGuiItem(item);
        } else if (slotIndex < topSlotCount + this.playerInventorySlots.length) {
            this.playerInventorySlots[slotIndex - topSlotCount] = item;
        } else {
            throw new IllegalArgumentException("No slot at index " + slotIndex);
        }
    }

    /**
     * Get the item on the cursor.
     *
     * @return The item, or the empty item.
     */
    public GuiItem getCursor() {
        return this.cursor;
    }

    /**
     * Set the item on the cursor.
     *
     * @param cursor The item.
     */
    public void setCursor(GuiItem cursor) {
        this.cursor = cursor;
    }

    /**
     * Get the item in the player's offhand.
     *
     * @return The item, or the empty item.
     */
    public GuiItem getOffhand() {
        return this.offhand;
    }

    /**
     * Set the item in the player's offhand.
     *
     * @param offhand The item.
     */
    public void setOffhand(GuiItem offhand) {
        this.offhand = offhand;
    }

    /**
     * Get the raw slot index of a hotbar slot.
     *
     * @param hotbarSlot The hotbar slot. [0-8]
     * @return The raw slot index.
     */
    public int getHotbarSlotIndex(int hotbarSlot) {
        return this.getTopSlotCount() + PLAYER_MAIN_SLOT_COUNT + hotbarSlot;
    }
}