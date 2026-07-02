package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.slot.DragType;
import ca.bkaw.praeter.gui.slot.GuiScreenState;
import ca.bkaw.praeter.gui.slot.SlotInteraction;
import ca.bkaw.praeter.gui.slot.SlotInteractionHandler;
import ca.bkaw.praeter.gui.slot.SlotInteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private @Nullable DragType dragType;
    private final List<Integer> dragSlots = new ArrayList<>();

    public PraeterChestMenu(int containerId, Inventory playerInventory, Container container, int rows, FabricCustomGui gui) {
        super(menuType(rows), containerId, playerInventory, container, rows);
        this.gui = gui;
    }

    private static MenuType<ChestMenu> menuType(int rows) {
        return switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            case 6 -> MenuType.GENERIC_9x6;
            default -> throw new IllegalArgumentException("Invalid gui height: " + rows);
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
    public void clicked(int slotNum, int button, ContainerInput input, Player player) {
        SlotInteraction interaction = this.translate(slotNum, button, input, player);
        if (interaction != null) {
            GuiScreenState state = this.buildState(player);
            SlotInteractionResult result = SlotInteractionHandler.handle(this.gui, state, interaction, FabricGuiPlayer.of(player));
            this.apply(result, player);
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
            case PICKUP -> slotNum == -999
                ? new SlotInteraction.DropCursor(button == 0)
                : (button == 0
                    ? new SlotInteraction.PickupLeft(slotNum)
                    : new SlotInteraction.PickupRight(slotNum));
            case QUICK_MOVE -> new SlotInteraction.ShiftClick(slotNum);
            case SWAP -> button == 40
                ? new SlotInteraction.OffhandSwap(slotNum)
                : new SlotInteraction.HotbarSwap(slotNum, button);
            case THROW -> new SlotInteraction.DropSlot(slotNum, button == 1);
            case CLONE -> player.hasInfiniteMaterials()
                ? new SlotInteraction.Clone(slotNum)
                : null;
            case PICKUP_ALL -> new SlotInteraction.DoubleClick(slotNum, button == 1);
            case QUICK_CRAFT -> this.quickCraft(slotNum, button, player);
            default -> null;
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
                    case 0 -> DragType.EVEN;
                    case 1 -> DragType.SINGLE;
                    case 2 -> player.hasInfiniteMaterials() ? DragType.CLONE : null;
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
    private GuiScreenState buildState(Player player) {
        GuiScreenState state = GuiScreenState.create(this.gui);
        for (int rawSlot = state.getTopSlotCount(); rawSlot < state.getSlotCount(); rawSlot++) {
            state.setSlot(rawSlot, FabricGuiItem.of(this.slots.get(rawSlot).getItem()));
        }
        state.setCursor(FabricGuiItem.of(this.getCarried()));
        state.setOffhand(FabricGuiItem.of(player.getItemBySlot(EquipmentSlot.OFFHAND)));
        return state;
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
