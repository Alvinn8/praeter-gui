package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.item.GuiItem;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * A snapshot of all slots of an open gui screen, together with the cursor and
 * offhand items.
 * <p>
 * The slots are indexed by raw slot index, covering the top gui slots followed by
 * the 36 bottom region slots (27 player inventory slots, then 9 hotbar slots).
 * <p>
 * Platforms create the state with {@link #create(CustomGui)}, which fills in the
 * contents of the custom slots, and then fill in the bottom region, cursor and
 * offhand from the game before handing the state to {@link SlotInteractionHandler}.
 */
public class GuiScreenState {
    /**
     * The number of slots in the bottom region of the screen.
     */
    public static final int BOTTOM_SLOT_COUNT = 36;

    /**
     * The number of slots in the main player inventory, excluding the hotbar.
     */
    public static final int PLAYER_MAIN_SLOT_COUNT = 27;

    private final int topSlotCount;
    private final GuiItem[] slots;
    private GuiItem cursor = GuiItem.empty();
    private GuiItem offhand = GuiItem.empty();

    /**
     * Create a new screen state where all slots are empty.
     *
     * @param topSlotCount The number of slots in the top gui.
     */
    public GuiScreenState(int topSlotCount) {
        this.topSlotCount = topSlotCount;
        this.slots = new GuiItem[topSlotCount + BOTTOM_SLOT_COUNT];
        Arrays.fill(this.slots, GuiItem.empty());
    }

    /**
     * Create a new screen state for the given gui with the contents of the custom
     * slots filled in from the gui state. All other slots are empty.
     *
     * @param gui The gui instance.
     * @return The screen state.
     */
    public static GuiScreenState create(CustomGui gui) {
        CustomGuiType type = gui.getType();
        GuiScreenState state = new GuiScreenState(type.getTopSlotCount());
        for (GuiSlot guiSlot : type.getGuiSlots()) {
            state.setSlot(guiSlot.getRawSlot(), guiSlot.getItem(gui));
        }
        return state;
    }

    /**
     * Get the number of slots in the top gui.
     *
     * @return The top slot count.
     */
    public int getTopSlotCount() {
        return this.topSlotCount;
    }

    /**
     * Get the total number of slots on the screen, including the bottom region.
     *
     * @return The slot count.
     */
    public int getSlotCount() {
        return this.slots.length;
    }

    /**
     * Get the item in the given slot.
     *
     * @param rawSlot The raw slot index.
     * @return The item, or the empty item.
     */
    public GuiItem getSlot(int rawSlot) {
        return this.slots[rawSlot];
    }

    /**
     * Set the item in the given slot.
     *
     * @param rawSlot The raw slot index.
     * @param item The item, where null is treated as the empty item.
     */
    public void setSlot(int rawSlot, @Nullable GuiItem item) {
        this.slots[rawSlot] = item == null ? GuiItem.empty() : item;
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
     * @param cursor The item, where null is treated as the empty item.
     */
    public void setCursor(@Nullable GuiItem cursor) {
        this.cursor = cursor == null ? GuiItem.empty() : cursor;
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
     * @param offhand The item, where null is treated as the empty item.
     */
    public void setOffhand(@Nullable GuiItem offhand) {
        this.offhand = offhand == null ? GuiItem.empty() : offhand;
    }

    /**
     * Get the raw slot index of a hotbar slot.
     *
     * @param hotbarSlot The hotbar slot. [0-8]
     * @return The raw slot index.
     */
    public int getHotbarRawSlot(int hotbarSlot) {
        return this.topSlotCount + PLAYER_MAIN_SLOT_COUNT + hotbarSlot;
    }
}
