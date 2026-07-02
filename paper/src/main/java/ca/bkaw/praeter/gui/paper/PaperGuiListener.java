package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.slot.DragType;
import ca.bkaw.praeter.gui.slot.GuiScreenState;
import ca.bkaw.praeter.gui.slot.SlotInteraction;
import ca.bkaw.praeter.gui.slot.SlotInteractionHandler;
import ca.bkaw.praeter.gui.slot.SlotInteractionResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The event listener that handles slot interactions on custom guis.
 * <p>
 * All interactions are cancelled and translated to platform-agnostic
 * {@link SlotInteraction} objects that are handled by the
 * {@link SlotInteractionHandler} in the common module. The resulting changes are
 * then applied to the game.
 */
public class PaperGuiListener implements Listener {
    private final Plugin plugin;

    public PaperGuiListener(Plugin plugin) {
        this.plugin = plugin;
    }

    private @Nullable PaperCustomGui getCustomGui(InventoryView view) {
        Inventory topInventory = view.getTopInventory();
        InventoryHolder holder = topInventory.getHolder();
        if (holder instanceof CustomGuiHolder customGuiHolder) {
            return customGuiHolder.gui();
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        PaperCustomGui gui = this.getCustomGui(event.getView());
        if (gui == null) {
            return;
        }
        // All vanilla behavior is cancelled. Interactions are instead simulated by
        // the common module and the resulting changes are applied.
        event.setCancelled(true);

        SlotInteraction interaction = this.translate(event);
        if (interaction == null) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        GuiScreenState state = this.buildState(gui, event.getView(), player, event.getCursor());
        SlotInteractionResult result = SlotInteractionHandler.handle(gui, state, interaction);
        this.apply(result, gui, event.getView(), player, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        PaperCustomGui gui = this.getCustomGui(event.getView());
        if (gui == null) {
            return;
        }
        event.setCancelled(true);

        DragType dragType = switch (event.getType()) {
            case SINGLE -> DragType.SINGLE;
            case EVEN -> DragType.EVEN;
        };
        // Use ascending slot order for a deterministic distribution.
        List<Integer> rawSlots = new ArrayList<>(event.getRawSlots());
        rawSlots.sort(null);
        SlotInteraction interaction = new SlotInteraction.Drag(dragType, rawSlots);

        Player player = (Player) event.getWhoClicked();
        GuiScreenState state = this.buildState(gui, event.getView(), player, event.getOldCursor());
        SlotInteractionResult result = SlotInteractionHandler.handle(gui, state, interaction);
        this.apply(result, gui, event.getView(), player, true);
    }

    /**
     * Translate a Bukkit click event to a platform-agnostic slot interaction.
     *
     * @param event The click event.
     * @return The interaction, or null if the click requires no action.
     */
    private @Nullable SlotInteraction translate(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        return switch (event.getAction()) {
            case PICKUP_ALL, PICKUP_SOME, PLACE_ALL, PLACE_SOME
                -> new SlotInteraction.PickupLeft(rawSlot);
            case PICKUP_HALF, PICKUP_ONE, PLACE_ONE
                -> new SlotInteraction.PickupRight(rawSlot);
            case SWAP_WITH_CURSOR -> event.getClick() == ClickType.RIGHT
                ? new SlotInteraction.PickupRight(rawSlot)
                : new SlotInteraction.PickupLeft(rawSlot);
            case MOVE_TO_OTHER_INVENTORY -> new SlotInteraction.ShiftClick(rawSlot);
            case HOTBAR_SWAP, HOTBAR_MOVE_AND_READD -> event.getClick() == ClickType.SWAP_OFFHAND
                ? new SlotInteraction.OffhandSwap(rawSlot)
                : new SlotInteraction.HotbarSwap(rawSlot, event.getHotbarButton());
            case DROP_ALL_SLOT -> new SlotInteraction.DropSlot(rawSlot, true);
            case DROP_ONE_SLOT -> new SlotInteraction.DropSlot(rawSlot, false);
            case DROP_ALL_CURSOR -> new SlotInteraction.DropCursor(true);
            case DROP_ONE_CURSOR -> new SlotInteraction.DropCursor(false);
            case COLLECT_TO_CURSOR -> new SlotInteraction.DoubleClick(rawSlot, false);
            case CLONE_STACK -> new SlotInteraction.Clone(rawSlot);
            default -> null;
        };
    }

    /**
     * Build the screen state from the custom gui state and the player's inventory.
     */
    private GuiScreenState buildState(PaperCustomGui gui, InventoryView view, Player player, @Nullable ItemStack cursor) {
        GuiScreenState state = GuiScreenState.create(gui);
        for (int rawSlot = state.getTopSlotCount(); rawSlot < state.getSlotCount(); rawSlot++) {
            state.setSlot(rawSlot, PaperGuiItem.of(view.getItem(rawSlot)));
        }
        state.setCursor(PaperGuiItem.of(cursor));
        state.setOffhand(PaperGuiItem.of(player.getInventory().getItemInOffHand()));
        return state;
    }

    /**
     * Apply the result of a slot interaction to the game.
     *
     * @param result The result to apply.
     * @param gui The gui that was interacted with.
     * @param view The open inventory view.
     * @param player The player that performed the interaction.
     * @param delayCursor Whether setting the cursor must be delayed a tick. This is
     *                    necessary for drag events since the game resets the cursor
     *                    of cancelled drags after the event.
     */
    private void apply(SlotInteractionResult result, PaperCustomGui gui, InventoryView view, Player player, boolean delayCursor) {
        for (Map.Entry<Integer, GuiItem> entry : result.playerInventoryChanges().entrySet()) {
            view.setItem(entry.getKey(), PaperGuiItem.toItemStack(entry.getValue()));
        }
        if (result.cursorChanged()) {
            ItemStack cursor = PaperGuiItem.toItemStack(result.cursor());
            if (delayCursor) {
                player.getServer().getScheduler().runTask(this.plugin, () -> player.setItemOnCursor(cursor));
            } else {
                player.setItemOnCursor(cursor);
            }
        }
        if (result.offhand() != null) {
            player.getInventory().setItemInOffHand(PaperGuiItem.toItemStack(result.offhand()));
        }
        for (GuiItem drop : result.droppedItems()) {
            ItemStack itemStack = PaperGuiItem.toItemStack(drop);
            if (itemStack != null) {
                player.dropItem(itemStack);
            }
        }
        if (result.customSlotsChanged()) {
            gui.update();
        }
    }
}
