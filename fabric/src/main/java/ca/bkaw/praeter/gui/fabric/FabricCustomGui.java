package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.gui.BottomRegionType;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.pack.font.FontSequence;
import ca.bkaw.praeter.gui.render.RenderDispatcher;
import ca.bkaw.praeter.gui.render.RenderStep;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;

import java.util.List;

public class FabricCustomGui extends CustomGui {
    private final SimpleContainer container;

    public FabricCustomGui(CustomGuiType type) {
        super(type);
        if (type.getBottomRegionType() == BottomRegionType.CUSTOM) {
            throw new UnsupportedOperationException("Custom bottom regions are not yet supported on Fabric.");
        }
        this.container = new SimpleContainer(type.getTopSlotCount());
    }

    /**
     * Get the container that holds the items displayed in the top gui.
     *
     * @return The container.
     */
    public SimpleContainer getContainer() {
        return this.container;
    }

    /**
     * Open this gui for the given player.
     *
     * @param player The player.
     */
    public void show(ServerPlayer player) {
        this.update();
        Component title = this.renderTitle();
        int rows = this.getType().getHeight();
        player.openMenu(new SimpleMenuProvider(
            (containerId, playerInventory, p) ->
                new PraeterChestMenu(containerId, playerInventory, this.container, rows, this),
            title
        ));
    }

    /**
     * Re-render the gui, updating the displayed items.
     */
    public void update() {
        int topSlotCount = this.getType().getTopSlotCount();
        for (GuiSlot guiSlot : this.getType().getGuiSlots()) {
            if (guiSlot.getRawSlot() < topSlotCount) {
                this.container.setItem(guiSlot.getRawSlot(), FabricGuiItem.toItemStack(guiSlot.getItem(this)));
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
        for (RenderStep renderStep : this.getType().getRenderSteps()) {
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
