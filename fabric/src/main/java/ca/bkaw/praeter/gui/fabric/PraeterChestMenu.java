package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.fabric.platform.FabricGuiItem;
import ca.bkaw.praeter.gui.fabric.platform.FabricGuiPlayer;
import ca.bkaw.praeter.gui.gui.ClickContext;
import ca.bkaw.praeter.gui.gui.TopRegionType;
import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.slot.GuiSlotsState;
import ca.bkaw.praeter.gui.slot.SlotInteraction;
import ca.bkaw.praeter.gui.slot.SlotInteractionHandler;
import ca.bkaw.praeter.gui.slot.SlotInteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The menu used for open custom guis.
 * <p>
 * All vanilla click behavior is replaced. Interactions are translated to
 * platform-agnostic {@link SlotInteraction} objects that are handled by the
 * {@link SlotInteractionHandler} in the common module, and the resulting changes
 * are applied to the menu.
 */
public class PraeterChestMenu extends ChestMenu {
    private final FabricCustomGui gui;

    // The state of an in-progress drag. Drags arrive as separate clicks for each
    // phase, which are accumulated here until the final phase.
    private @Nullable SlotInteraction.DragType dragType;
    private final List<Integer> dragSlots = new ArrayList<>();

    public PraeterChestMenu(int containerId, Inventory playerInventory, Container container, TopRegionType topRegionType, FabricCustomGui gui) {
        super(menuType(topRegionType), containerId, playerInventory, container, rows(topRegionType));
        this.gui = gui;
    }

    private static MenuType<ChestMenu> menuType(TopRegionType topRegionType) {
        return switch (topRegionType) {
            case TopRegionType.GENERIC_9X1 -> MenuType.GENERIC_9x1;
            case TopRegionType.GENERIC_9X2 -> MenuType.GENERIC_9x2;
            case TopRegionType.GENERIC_9X3 -> MenuType.GENERIC_9x3;
            case TopRegionType.GENERIC_9X4 -> MenuType.GENERIC_9x4;
            case TopRegionType.GENERIC_9X5 -> MenuType.GENERIC_9x5;
            case TopRegionType.GENERIC_9X6 -> MenuType.GENERIC_9x6;
        };
    }

    private static int rows(TopRegionType topRegionType) {
        return switch (topRegionType) {
            case TopRegionType.GENERIC_9X1 -> 1;
            case TopRegionType.GENERIC_9X2 -> 2;
            case TopRegionType.GENERIC_9X3 -> 3;
            case TopRegionType.GENERIC_9X4 -> 4;
            case TopRegionType.GENERIC_9X5 -> 5;
            case TopRegionType.GENERIC_9X6 -> 6;
        };
    }

    /**
     * Get the custom gui this menu displays.
     *
     * @return The custom gui.
     */
    public FabricCustomGui getGui() {
        return this.gui;
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
        SlotInteraction interaction = this.translate(slotIndex, button, input, player);
        if (interaction != null) {
            FabricClickContext ctx = new FabricClickContext(this.gui, interaction, player);
            for (Consumer<ClickContext> clickListener : this.gui.getClickListeners()) {
                clickListener.accept(ctx);
            }
            if (!ctx.isCancelled()) {
                GuiSlotsState state = this.buildState(player);
                SlotInteractionResult result = SlotInteractionHandler.handle(this.gui, state, interaction, new FabricGuiPlayer(player));
                this.apply(result, player);
            }
        }
        // The client predicts vanilla behavior, so always resynchronize the menu.
        this.sendAllDataToRemote();
    }

    /**
     * Translate a click to a platform-agnostic slot interaction.
     *
     * @return The interaction, or null if the click requires no action.
     */
    private @Nullable SlotInteraction translate(int slotNum, int button, ContainerInput input, Player player) {
        if (input != ContainerInput.QUICK_CRAFT && this.dragType != null) {
            // An interrupted drag is abandoned.
            this.dragType = null;
            this.dragSlots.clear();
        }
        return switch (input) {
            case PICKUP -> slotNum == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE
                ? (button == 0
                ? new SlotInteraction.LeftClickOutside()
                : new SlotInteraction.RightClickOutside())
                : (button == 0
                ? new SlotInteraction.LeftClick(slotNum)
                : new SlotInteraction.RightClick(slotNum));
            case QUICK_MOVE -> new SlotInteraction.ShiftClick(slotNum);
            case SWAP -> button == Inventory.SLOT_OFFHAND
                ? new SlotInteraction.OffhandSwap(slotNum)
                : new SlotInteraction.HotbarSwap(slotNum, button);
            case THROW -> new SlotInteraction.DropSlot(slotNum, button == 1);
            case CLONE -> player.hasInfiniteMaterials()
                ? new SlotInteraction.Clone(slotNum)
                : null;
            case PICKUP_ALL -> new SlotInteraction.DoubleClick(slotNum);
            case QUICK_CRAFT -> this.quickCraft(slotNum, button, player);
        };
    }

    /**
     * Handle a phase of a drag. The button encodes the phase in the two lowest bits
     * (0 = start, 1 = add slot, 2 = end) and the drag type in the next two bits.
     *
     * @return The completed drag interaction, or null while the drag is in progress.
     */
    private @Nullable SlotInteraction quickCraft(int slotNum, int button, Player player) {
        int phase = button & 3;
        switch (phase) {
            case 0 -> {
                this.dragSlots.clear();
                this.dragType = switch ((button >> 2) & 3) {
                    case 0 -> SlotInteraction.DragType.LEFT;
                    case 1 -> SlotInteraction.DragType.RIGHT;
                    case 2 -> player.hasInfiniteMaterials() ? SlotInteraction.DragType.MIDDLE : null;
                    default -> null;
                };
            }
            case 1 -> {
                if (this.dragType != null && slotNum >= 0) {
                    this.dragSlots.add(slotNum);
                }
            }
            case 2 -> {
                if (this.dragType != null) {
                    SlotInteraction interaction = new SlotInteraction.Drag(this.dragType, List.copyOf(this.dragSlots));
                    this.dragType = null;
                    this.dragSlots.clear();
                    return interaction;
                }
            }
        }
        return null;
    }

    /**
     * Build the screen state from the custom gui state and the menu's slots.
     */
    private GuiSlotsState buildState(Player player) {
        GuiItem[] playerInventorySlots = new GuiItem[Inventory.INVENTORY_SIZE];
        int startIndex = this.gui.getType().getTopRegionType().getSlotCount();
        for (int i = 0; i < playerInventorySlots.length; i++) {
            int rawSlot = startIndex + i;
            playerInventorySlots[i] = FabricGuiItem.of(this.slots.get(rawSlot).getItem());
        }
        return new GuiSlotsState(
            this.gui,
            playerInventorySlots,
            FabricGuiItem.of(this.getCarried()),
            FabricGuiItem.of(player.getItemBySlot(EquipmentSlot.OFFHAND))
        );
    }

    /**
     * Apply the result of a slot interaction to the menu and player.
     */
    private void apply(SlotInteractionResult result, Player player) {
        for (Map.Entry<Integer, GuiItem> entry : result.playerInventoryChanges().entrySet()) {
            // The menu slots write through to the player's inventory.
            this.slots.get(entry.getKey()).set(FabricGuiItem.toItemStack(entry.getValue()));
        }
        if (result.cursorChanged()) {
            this.setCarried(FabricGuiItem.toItemStack(result.cursor()));
        }
        if (result.offhand() != null) {
            player.setItemSlot(EquipmentSlot.OFFHAND, FabricGuiItem.toItemStack(result.offhand()));
        }
        for (GuiItem drop : result.droppedItems()) {
            ItemStack itemStack = FabricGuiItem.toItemStack(drop);
            if (!itemStack.isEmpty()) {
                player.drop(itemStack, false);
            }
        }
        if (result.customSlotsChanged()) {
            this.gui.update();
        }
    }
}

