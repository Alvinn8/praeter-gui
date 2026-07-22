package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.fabric.platform.FabricGuiItem;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.TopRegionType;
import ca.bkaw.praeter.gui.pack.font.FontSequence;
import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.render.RenderDispatcher;
import ca.bkaw.praeter.gui.render.RenderStep;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import ca.bkaw.praeter.gui.slot.ItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.LinkedHashSet;
import java.util.List;

public class FabricCustomGui extends CustomGui {
    private final SimpleContainer container;

    public FabricCustomGui(CustomGuiType type) {
        super(type);
        this.container = new SimpleContainer(type.getTopRegionType().getSlotCount());
    }

    /**
     * Open this gui for the given player.
     *
     * @param player The player.
     */
    public void show(ServerPlayer player) {
        this.update();
        Component title = this.renderTitle();
        TopRegionType topRegionType = this.getType().getTopRegionType();
        player.openMenu(new SimpleMenuProvider(
            (containerId, playerInventory, _) ->
                new PraeterChestMenu(containerId, playerInventory, this.container, topRegionType, this),
            title
        ));
    }

    /**
     * Re-render the gui, updating the displayed items.
     */
    public void update() {
        // Render items from the custom slots
        int topSlotCount = this.getType().getTopRegionType().getSlotCount();
        for (GuiSlot guiSlot : this.getType().getGuiSlots()) {
            if (guiSlot.getSlotIndex() < topSlotCount) {
                GuiItem guiItem = guiSlot.getRef().get(this).getGuiItem();
                this.container.setItem(guiSlot.getSlotIndex(), FabricGuiItem.toItemStack(guiItem));
            }
        }

        // Render items from item renderers
        for (ItemRenderer itemRenderer : this.getType().getItemRenderers()) {
            if (itemRenderer.slotIndex() < topSlotCount) {
                this.container.setItem(itemRenderer.slotIndex(), FabricGuiItem.toItemStack(itemRenderer.getItem(this)));
            }
        }

        // Fill positions that are not slots with invisible filler items so that
        // client-side prediction of item movement sees them as occupied and does
        // not move items into them.
        ItemStack fillerItem = new ItemStack(Items.PAPER);
        fillerItem.set(DataComponents.ITEM_MODEL, Identifier.parse(PraeterGuiAssets.EMPTY_ITEM_MODEL));
        fillerItem.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, new LinkedHashSet<>()));
        for (int rawSlot = 0; rawSlot < topSlotCount; rawSlot++) {
            if (this.getType().getGuiSlotAt(rawSlot) == null && this.container.getItem(rawSlot).isEmpty()) {
                this.container.setItem(rawSlot, fillerItem.copy());
            }
        }

        // TODO re-render the title and reopen for viewers when it changes, like on
        //  Paper.
    }

    /**
     * Run the render steps and build the inventory title that renders the gui.
     *
     * @return The title component.
     */
    private Component renderTitle() {
        RenderDispatcher rd = new RenderDispatcher();
        List<RenderStep> renderSteps = this.getType().getRenderSteps();
        if (renderSteps == null) {
            throw new IllegalStateException("Tried to render a custom gui that was not registered.");
        }
        for (RenderStep renderStep : renderSteps) {
            renderStep.render(rd, this);
        }
        MutableComponent component = Component.empty().withStyle(ChatFormatting.WHITE);
        String currentFontIdentifier = null;
        StringBuilder currentText = new StringBuilder();
        for (FontSequence fontSequence : rd.getRenderTitle()) {
            if (fontSequence.fontIdentifier().equals(currentFontIdentifier)) {
                currentText.append(fontSequence.text());
            } else {
                if (currentFontIdentifier != null) {
                    component.append(toComponent(currentFontIdentifier, currentText.toString()));
                }
                currentFontIdentifier = fontSequence.fontIdentifier();
                currentText = new StringBuilder(fontSequence.text());
            }
        }
        if (currentFontIdentifier != null) {
            component.append(toComponent(currentFontIdentifier, currentText.toString()));
        }
        return component;
    }

    private static Component toComponent(String fontIdentifier, String text) {
        FontDescription font = new FontDescription.Resource(Identifier.parse(fontIdentifier));
        return Component.literal(text).withStyle(style -> style.withFont(font));
    }
}
