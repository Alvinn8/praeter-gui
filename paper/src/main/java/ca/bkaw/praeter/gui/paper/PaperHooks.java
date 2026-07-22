package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.CommonHooks;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.slot.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.paper.platform.PaperGuiItem;
import ca.bkaw.praeter.gui.render.RenderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * Paper-specific methods, "hooks", used to set up custom guis, complementing
 * {@link CommonHooks} with variants that use Bukkit types.
 * <p>
 * Statically importing the methods of this class is recommended.
 */
public class PaperHooks {

    /**
     * Render an item stack at the given slot position, without the position being
     * a slot that can be interacted with.
     * <p>
     * The item is computed from the gui instance each time the gui is updated, so
     * the displayed item can be fully dynamic. This can be used to display
     * tooltips or to show items for decorative purposes.
     * <p>
     * The position must not also have a slot registered.
     *
     * @param r The render context.
     * @param pos The slot position to render the item at.
     * @param itemFunction The function that computes the item stack to display.
     *                     May return null to display nothing.
     */
    public static void renderItem(RenderContext r, SlotPos pos, Function<CustomGui, @Nullable ItemStack> itemFunction) {
        CommonHooks.renderGuiItem(r, pos, gui -> PaperGuiItem.of(itemFunction.apply(gui)));
    }

    /**
     * Get the item in a slot as an {@link ItemStack}.
     *
     * @param slotRef The reference to the slot.
     * @param gui The gui instance.
     * @return The item stack, or null if the slot is empty.
     */
    public static @Nullable ItemStack getSlotItem(Ref<Slot> slotRef, CustomGui gui) {
        return PaperGuiItem.toItemStack(slotRef.get(gui).getGuiItem());
    }

    /**
     * Get the item in a slot as an {@link ItemStack}.
     *
     * @param slot The slot state.
     * @return The item stack, or null if the slot is empty.
     */
    public static @Nullable ItemStack getSlotItem(Slot slot) {
        return PaperGuiItem.toItemStack(slot.getGuiItem());
    }

    /**
     * Set the {@link ItemStack} in a slot.
     * <p>
     * Remember to update the gui for viewers to see the change.
     *
     * @param slotRef The reference to the slot.
     * @param gui The gui instance.
     * @param itemStack The item stack, where null empties the slot.
     */
    public static void setSlotItem(Ref<Slot> slotRef, CustomGui gui, @Nullable ItemStack itemStack) {
        slotRef.get(gui).setGuiItem(PaperGuiItem.of(itemStack));
    }

    /**
     * Set the {@link ItemStack} in a slot.
     * <p>
     * Remember to update the gui for viewers to see the change.
     *
     * @param slot The slot state.
     * @param itemStack The item stack.
     */
    public static void setSlotItem(Slot slot, @Nullable ItemStack itemStack) {
        slot.setGuiItem(PaperGuiItem.of(itemStack));
    }

    /**
     * Display text when the user hovers the given slot position.
     * <p>
     * There may not be any slots or item renderers at the position.
     *
     * @param r The render context.
     * @param pos The position to display the text at.
     * @param text The lines of text to display.
     */
    public static void hoverText(RenderContext r, SlotPos pos, Component... text) {
        if (text.length == 0) {
            throw new IllegalArgumentException("At least one line of text must be provided.");
        }
        ItemStack item = createHoverTextItem(List.of(text));
        renderItem(r, pos, _ -> item);
    }

    /**
     * Create an invisible item that displays the given lines of text as its
     * tooltip when hovered.
     * <p>
     * The first line is the item name, and the remaining lines are the item lore.
     * The vanilla text styling of names and lore is reset.
     *
     * @param lines The lines of text. Must not be empty.
     * @return The hover text item.
     */
    static ItemStack createHoverTextItem(List<Component> lines) {
        ItemStack itemStack = new ItemStack(Material.PAPER);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setItemModel(NamespacedKey.fromString(PraeterGuiAssets.EMPTY_ITEM_MODEL));
        meta.customName(resetStyle(lines.getFirst()));
        if (lines.size() > 1) {
            meta.lore(lines.subList(1, lines.size()).stream()
                .map(PaperHooks::resetStyle)
                .toList());
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Wrap a component so that the vanilla styling of item names and lore (italic,
     * and purple for lore) is reset. The component's own styling is kept.
     */
    private static Component resetStyle(Component component) {
        return Component.text()
            .decoration(TextDecoration.ITALIC, false)
            .color(NamedTextColor.WHITE)
            .append(component)
            .build();
    }
}
