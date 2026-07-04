package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.Platform;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.item.GuiItem;
import io.netty.channel.ChannelHandler;
import net.kyori.adventure.audience.Audience;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Fabric-backed {@link Platform}.
 * */
public final class FabricPlatform implements Platform {
    private @Nullable GuiItem fillerItem;

    @Override
    public String name() {
        return "Fabric";
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public void injectChannelHandler(ChannelHandler channelHandler, String handlerKey) throws ReflectiveOperationException {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public void uninjectChannelHandler(String handlerKey) throws ReflectiveOperationException {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public @Nullable InetAddress getPlayerAddress(Audience player) {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public void guessOwner(Class<?> clazz) {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public @Nullable Path getStoragePath() {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public void includeAssetsFromOwners() {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public CustomGui createGui(CustomGuiType type) {
        return new FabricCustomGui(type);
    }

    @Override
    public void sendResourcePackToOnlinePlayers() {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public GuiItem createFillerItem() {
        if (this.fillerItem == null) {
            ItemStack itemStack = new ItemStack(Items.PAPER);
            itemStack.set(DataComponents.ITEM_MODEL, Identifier.parse(PraeterGuiAssets.EMPTY_ITEM_MODEL));
            itemStack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, new LinkedHashSet<>()));
            this.fillerItem = FabricGuiItem.of(itemStack);
        }
        return this.fillerItem;
    }

    @Override
    public GuiItem createHoverTextItem(List<String> lines) {
        return FabricHooks.createHoverTextItem(lines.stream()
            .<Component>map(Component::literal)
            .toList());
    }
}
