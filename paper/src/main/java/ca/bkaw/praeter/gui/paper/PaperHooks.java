package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.CommonHooks;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.item.GuiItem;
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
 * Statically importing the methods of this class alongside the common hooks is
 * recommended:
 * <pre>
 * import static ca.bkaw.praeter.gui.CommonHooks.*;
 * import static ca.bkaw.praeter.gui.paper.PaperHooks.*;
 * </pre>
 */
public class PaperHooks {
    private PaperHooks() {}

    /**
     * Display text when the user hovers the given slot position.
     * <p>
     * The text is displayed using an invisible item whose tooltip contains the
     * text. The position cannot be interacted with.
     *
     * @param r The render context.
     * @param pos The position to display the text at.
     * @param text The lines of text to display.
     */
    public static void hoverText(RenderContext r, SlotPos pos, Component... text) {
        if (text.length == 0) {
            throw new IllegalArgumentException("At least one line of text must be provided.");
        }
        GuiItem item = createHoverTextItem(List.of(text));
        CommonHooks.renderItem(r, pos, gui -> item);
    }

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
    public static void renderItemStack(RenderContext r, SlotPos pos, Function<CustomGui, @Nullable ItemStack> itemFunction) {
        CommonHooks.renderItem(r, pos, PaperGuiItem.renderer(itemFunction));
    }

    /**
     * Get the item in a slot.
     *
     * @param slotRef The reference to the slot.
     * @param gui The gui instance.
     * @return The item stack, or null if the slot is empty.
     */
    public static @Nullable ItemStack getSlotItem(Ref<Slot> slotRef, CustomGui gui) {
        return PaperGuiItem.toItemStack(slotRef.get(gui).getItem());
    }

    /**
     * Set the item in a slot.
     * <p>
     * Remember to update the gui afterwards for viewers to see the change.
     *
     * @param slotRef The reference to the slot.
     * @param gui The gui instance.
     * @param itemStack The item stack, where null empties the slot.
     */
    public static void setSlotItem(Ref<Slot> slotRef, CustomGui gui, @Nullable ItemStack itemStack) {
        slotRef.get(gui).setItem(PaperGuiItem.of(itemStack));
    }

    /**
     * Create an invisible item that displays the given lines of text as its
     * tooltip when hovered.
     * <p>
     * The first line is the item name and the remaining lines are the item lore.
     * The vanilla text styling of names and lore is reset.
     *
     * @param lines The lines of text. Must not be empty.
     * @return The hover text item.
     */
    static GuiItem createHoverTextItem(List<Component> lines) {
        ItemStack itemStack = new ItemStack(Material.PAPER);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setItemModel(NamespacedKey.fromString(PraeterGuiAssets.EMPTY_ITEM_MODEL));
        meta.displayName(resetStyle(lines.getFirst()));
        if (lines.size() > 1) {
            meta.lore(lines.subList(1, lines.size()).stream()
                .map(PaperHooks::resetStyle)
                .toList());
        }
        itemStack.setItemMeta(meta);
        return PaperGuiItem.of(itemStack);
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
