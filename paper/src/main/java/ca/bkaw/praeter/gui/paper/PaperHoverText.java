package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.CommonHooks;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.components.HoverText;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.render.RenderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Displays text when the user hovers a slot position in a gui, specified using
 * Adventure components instead of strings.
 *
 * @see HoverText
 */
public class PaperHoverText {
    private PaperHoverText() {}

    /**
     * Display text when the user hovers the given slot position.
     *
     * @param r The render context.
     * @param pos The position to display the text at.
     * @param text The lines of text to display.
     */
    public static void hoverText(RenderContext r, SlotPos pos, Component... text) {
        if (text.length == 0) {
            throw new IllegalArgumentException("At least one line of text must be provided.");
        }
        GuiItem item = createItem(List.of(text));
        CommonHooks.renderItem(r, pos, gui -> item);
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
    public static GuiItem createItem(List<Component> lines) {
        ItemStack itemStack = new ItemStack(Material.PAPER);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setItemModel(NamespacedKey.fromString(PraeterGuiAssets.EMPTY_ITEM_MODEL));
        meta.displayName(resetStyle(lines.getFirst()));
        if (lines.size() > 1) {
            meta.lore(lines.subList(1, lines.size()).stream()
                .map(PaperHoverText::resetStyle)
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
