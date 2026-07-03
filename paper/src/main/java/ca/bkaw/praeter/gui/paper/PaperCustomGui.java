package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.pack.font.FontSequence;
import ca.bkaw.praeter.gui.render.RenderDispatcher;
import ca.bkaw.praeter.gui.item.ItemRenderer;
import ca.bkaw.praeter.gui.render.RenderStep;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PaperCustomGui extends CustomGui {
    private @Nullable Inventory inventory;
    private @Nullable Component currentRenderTitle;
    private boolean isReopening;

    public PaperCustomGui(CustomGuiType type) {
        super(type);
    }

    public void show(Player player) {
        if (this.inventory == null) {
            this.update();
        }
        player.openInventory(this.inventory);
    }

    public void update() {
        // Run render steps to get the render title
        RenderDispatcher rd = new RenderDispatcher();
        for (RenderStep renderStep : this.getType().getRenderSteps()) {
            renderStep.render(rd, this);
        }
        Component renderTitle = this.toComponent(rd.getRenderTitle());

        // In case the title has changed, recreate the inventory and open it again for
        // all viewers
        List<HumanEntity> viewers = null;
        if (!Objects.equals(this.currentRenderTitle, renderTitle) && this.inventory != null) {
            viewers = new ArrayList<>(this.inventory.getViewers());
            this.inventory = null;
        }

        if (this.inventory == null) {
            // Create the inventory
            int slotCount = this.getType().getHeight() * 9;
            CustomGuiHolder holder = new CustomGuiHolder(this);
            this.currentRenderTitle = renderTitle;
            this.inventory = Bukkit.createInventory(holder, slotCount, renderTitle);
        }

        // Clear items
        this.inventory.clear();

        // Render items from the custom slots and item renderers
        int topSlotCount = this.getType().getTopSlotCount();
        for (GuiSlot guiSlot : this.getType().getGuiSlots()) {
            if (guiSlot.getRawSlot() < topSlotCount) {
                this.inventory.setItem(guiSlot.getRawSlot(), PaperGuiItem.toItemStack(guiSlot.getItem(this)));
            }
        }
        for (ItemRenderer itemRenderer : this.getType().getItemRenderers()) {
            if (itemRenderer.rawSlot() < topSlotCount) {
                this.inventory.setItem(itemRenderer.rawSlot(), PaperGuiItem.toItemStack(itemRenderer.getItem(this)));
            }
        }

        // Fill positions that are not slots with invisible filler items so that
        // client-side prediction of item movement sees them as occupied and does
        // not move items into them.
        ItemStack fillerItem = PaperGuiItem.toItemStack(
            PraeterGui.instance().getPlatform().createFillerItem()
        );
        for (int rawSlot = 0; rawSlot < topSlotCount; rawSlot++) {
            if (this.getType().getGuiSlotAt(rawSlot) == null && this.inventory.getItem(rawSlot) == null) {
                this.inventory.setItem(rawSlot, fillerItem);
            }
        }

        // If the inventory was recreated with a new title,
        // open the new inventory for the viewers
        if (viewers != null) {
            this.isReopening = true;
            viewers.forEach(viewer -> viewer.openInventory(this.inventory));
            this.isReopening = false;
        }
    }

    private Component toComponent(List<FontSequence> fontSequences) {
        TextComponent.Builder builder = Component.text();
        builder.style(s -> s.color(NamedTextColor.WHITE));
        String currentFontIdentifier = null;
        StringBuilder currentText = new StringBuilder();
        for (FontSequence fontSequence : fontSequences) {
            if (fontSequence.fontIdentifier().equals(currentFontIdentifier)) {
                currentText.append(fontSequence.text());
            } else {
                if (currentFontIdentifier != null) {
                    builder.append(this.toComponent(currentFontIdentifier, currentText.toString()));
                }
                currentFontIdentifier = fontSequence.fontIdentifier();
                currentText = new StringBuilder(fontSequence.text());
            }
        }
        if (currentFontIdentifier != null) {
            builder.append(this.toComponent(currentFontIdentifier, currentText.toString()));
        }
        return Component.textOfChildren(
            builder.build(),
            Component.text("Title")
        );
    }

    private Component toComponent(String fontIdentifier, String text) {
        return Component.text()
            .content(text)
            .style(Style.style().font(Key.key(fontIdentifier)).build())
            .build();
    }

    public @Nullable Inventory getInventory() {
        return this.inventory;
    }
}
