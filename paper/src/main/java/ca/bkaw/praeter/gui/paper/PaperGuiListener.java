package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.paper.platform.PaperGuiItem;
import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.paper.platform.PaperGuiPlayer;
import ca.bkaw.praeter.gui.slot.GuiSlotsState;
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
 * All interactions are canceled and translated to platform-agnostic
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
        if (holder instanceof CustomGuiHolder(PaperCustomGui gui)) {
            return gui;
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        PaperCustomGui gui = this.getCustomGui(event.getView());
        if (gui == null) {
            return;
        }
        // All vanilla behavior is canceled. Interactions are instead simulated by
        // the common module, and the resulting changes are applied.
        event.setCancelled(true);

        SlotInteraction interaction = this.translate(event);
        if (interaction == null) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        GuiSlotsState state = this.buildState(gui, event.getView(), player, event.getCursor());
        SlotInteractionResult result = SlotInteractionHandler.handle(gui, state, interaction, new PaperGuiPlayer(player));
        this.apply(result, gui, event.getView(), player, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        PaperCustomGui gui = this.getCustomGui(event.getView());
        if (gui == null) {
            return;
        }
        event.setCancelled(true);

        SlotInteraction.DragType dragType = switch (event.getType()) {
            case SINGLE -> SlotInteraction.DragType.RIGHT;
            case EVEN -> SlotInteraction.DragType.LEFT;
        };
        // Use ascending slot order for a deterministic distribution.
        List<Integer> rawSlots = new ArrayList<>(event.getRawSlots());
        rawSlots.sort(null);
        SlotInteraction interaction = new SlotInteraction.Drag(dragType, rawSlots);

        Player player = (Player) event.getWhoClicked();
        GuiSlotsState state = this.buildState(gui, event.getView(), player, event.getOldCursor());
        SlotInteractionResult result = SlotInteractionHandler.handle(gui, state, interaction, new PaperGuiPlayer(player));
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
                -> new SlotInteraction.LeftClick(rawSlot);
            case PICKUP_HALF, PICKUP_ONE, PLACE_ONE
                -> new SlotInteraction.RightClick(rawSlot);
            case SWAP_WITH_CURSOR -> event.getClick() == ClickType.RIGHT
                ? new SlotInteraction.RightClick(rawSlot)
                : new SlotInteraction.LeftClick(rawSlot);
            case MOVE_TO_OTHER_INVENTORY -> new SlotInteraction.ShiftClick(rawSlot);
            case HOTBAR_SWAP -> event.getClick() == ClickType.SWAP_OFFHAND
                ? new SlotInteraction.OffhandSwap(rawSlot)
                : new SlotInteraction.HotbarSwap(rawSlot, event.getHotbarButton());
            case DROP_ALL_SLOT -> new SlotInteraction.DropSlot(rawSlot, true);
            case DROP_ONE_SLOT -> new SlotInteraction.DropSlot(rawSlot, false);
            case DROP_ALL_CURSOR -> new SlotInteraction.LeftClickOutside();
            case DROP_ONE_CURSOR -> new SlotInteraction.RightClickOutside();
            case COLLECT_TO_CURSOR -> new SlotInteraction.DoubleClick(rawSlot);
            case CLONE_STACK -> new SlotInteraction.Clone(rawSlot);
            default -> null;
        };
    }

    /**
     * Build the screen state from the custom gui state and the player's inventory.
     */
    private GuiSlotsState buildState(PaperCustomGui gui, InventoryView view, Player player, @Nullable ItemStack cursor) {
        int playerInventorySize = gui.getType().getBottomRegionType().getSlotCount();
        GuiItem[] playerInventoryItems = new GuiItem[playerInventorySize];
        int startIndex = gui.getType().getTopRegionType().getSlotCount();
        for (int i = 0; i < playerInventoryItems.length; i++) {
            int rawSlot = startIndex + i;
            playerInventoryItems[i] = PaperGuiItem.of(view.getItem(rawSlot));
        }
        return new GuiSlotsState(
            gui,
            playerInventoryItems,
            PaperGuiItem.of(cursor),
            PaperGuiItem.of(player.getInventory().getItemInOffHand())
        );
    }

    /**
     * Apply the result of a slot interaction to the game.
     *
     * @param result The result to apply.
     * @param gui The gui that was interacted with.
     * @param view The open inventory view.
     * @param player The player that performed the interaction.
     * @param delayCursor Whether setting the cursor must be delayed a tick. This is
     *                    necessary for drag events since the cursor is reset after
     *                    the event when the event is canceled.
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
