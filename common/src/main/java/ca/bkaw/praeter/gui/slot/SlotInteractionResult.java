package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.item.GuiItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The result of handling a {@link SlotInteraction}, describing the changes the
 * platform must apply to the game.
 * <p>
 * Changes to custom gui slots have already been applied to the gui state when
 * this result is returned. The platform re-renders the gui when
 * {@link #customSlotsChanged()} is true, which displays those changes.
 *
 * @param playerInventoryChanges The changed bottom region slots, mapping raw slot
 *                               index to the new item. The platform must write
 *                               these to the player's inventory.
 * @param cursor The item now on the cursor.
 * @param cursorChanged Whether the cursor changed and must be written back.
 * @param offhand The new offhand item, or null if the offhand did not change.
 * @param droppedItems Items that the player dropped, that the platform must spawn
 *                     as item entities dropped by the player.
 * @param customSlotsChanged Whether any custom gui slots changed, requiring the
 *                           gui to be re-rendered.
 */
public record SlotInteractionResult(
    Map<Integer, GuiItem> playerInventoryChanges,
    GuiItem cursor,
    boolean cursorChanged,
    @Nullable GuiItem offhand,
    List<GuiItem> droppedItems,
    boolean customSlotsChanged
) {}
