package ca.bkaw.praeter.gui.fabric.platform;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.fabric.FabricCustomGui;
import ca.bkaw.praeter.gui.fabric.FabricHooks;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.collision.ResourceCollisionException;
import ca.bkaw.praeter.gui.platform.GuiPlayer;
import ca.bkaw.praeter.gui.platform.Platform;
import ca.bkaw.praeter.gui.render.RenderContext;
import io.netty.channel.ChannelHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fabric-backed {@link Platform}.
 * */
public final class FabricPlatform implements Platform {
    private static @Nullable FabricPlatform instance;

    private final Map<ModContainer, Class<?>> handledMods = new HashMap<>();
    private @Nullable ModContainer mainMod;
    private @Nullable MinecraftServer server;
    private final Map<String, ChannelHandler> channelHandlers = new HashMap<>(1);

    public FabricPlatform() {
        instance = this;

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            PraeterGui.instance().getPlatformEvents().onServerStarted();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> this.server = null);

        ServerConfigurationConnectionEvents.CONFIGURE.register((listener, server) ->
            PraeterGui.instance().getPlatformEvents().onPlayerConfigure(new FabricConfiguringGuiPlayer(listener))
        );
    }

    /**
     * Get the {@link FabricPlatform} instance.
     * <p>
     * Used by {@code ServerConnectionListenerMixin} to reach the registered channel
     * handlers, since mixins cannot be constructed with the platform instance.
     *
     * @return The instance, or null if the platform has not been bootstrapped yet.
     */
    public static @Nullable FabricPlatform getInstance() {
        return instance;
    }

    /**
     * Get the channel handlers that have been injected, keyed by their handler key.
     *
     * @return The channel handlers.
     */
    public Map<String, ChannelHandler> getChannelHandlers() {
        return this.channelHandlers;
    }

    @Override
    public int getServerPort() {
        if (this.server == null) {
            throw new IllegalStateException("The server has not started yet.");
        }
        return this.server.getPort();
    }

    @Override
    public void injectChannelHandler(ChannelHandler channelHandler, String handlerKey) {
        this.channelHandlers.put(handlerKey, channelHandler);
    }

    @Override
    public void uninjectChannelHandler(String handlerKey) {
        this.channelHandlers.remove(handlerKey);
    }

    /**
     * Find the mod that owns the given class by matching its code source
     * against the root paths of every loaded mod.
     *
     * @param clazz The class.
     * @return The owning mod, or null if it could not be determined.
     */
    private static @Nullable ModContainer findOwningMod(Class<?> clazz) {
        if (clazz.getProtectionDomain().getCodeSource() == null) {
            return null;
        }
        Path location;
        try {
            location = Path.of(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            return null;
        }
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            for (Path rootPath : mod.getRootPaths()) {
                if (location.equals(rootPath) || location.startsWith(rootPath)) {
                    return mod;
                }
            }
        }
        // TODO this does not work in development, test if it works with jars.
        return null;
    }

    private void includeAssets(PraeterGui praeterGui, ModContainer mod, Class<?> clazz) {
        try {
            ResourcePack jarResources = PraeterGuiAssets.getJarResources(clazz);
            praeterGui.getAssets().includeAssets(jarResources);
            jarResources.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to include assets from mod " + mod.getMetadata().getId(), e);
        } catch (ResourceCollisionException e) {
            throw new RuntimeException("Resource collision when including assets from mod " + mod.getMetadata().getId(), e);
        }
    }

    @Override
    public void guessOwner(Class<?> clazz) {
        ModContainer mod = findOwningMod(clazz);
        if (mod == null || this.handledMods.containsKey(mod)) {
            return;
        }
        this.handledMods.put(mod, clazz);
        if (this.mainMod == null) {
            this.mainMod = mod;
        }
        PraeterGui praeterGui = PraeterGui.instance();
        if (praeterGui.hasAssets()) {
            this.includeAssets(praeterGui, mod, clazz);
        }
    }

    @Override
    public @Nullable Path getStoragePath() {
        if (this.mainMod == null) {
            return null;
        }
        return FabricLoader.getInstance().getConfigDir()
            .resolve(".praeter_gui")
            .resolve(this.mainMod.getMetadata().getId());
    }

    @Override
    public void includeAssetsFromOwners() {
        PraeterGui praeterGui = PraeterGui.instance();
        for (Map.Entry<ModContainer, Class<?>> entry : this.handledMods.entrySet()) {
            this.includeAssets(praeterGui, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public CustomGui createGui(CustomGuiType type) {
        return new FabricCustomGui(type);
    }

    @Override
    public List<GuiPlayer> getOnlinePlayers() {
        if (this.server == null) {
            return List.of();
        }
        return this.server.getPlayerList().getPlayers().stream()
            .map(player -> (GuiPlayer) new FabricGuiPlayer(player))
            .toList();
    }

    @Override
    public void plainTextHoverText(RenderContext r, SlotPos pos, String[] text) {
        FabricHooks.hoverText(r, pos, Arrays.stream(text)
            .map(Component::literal)
            .toArray(Component[]::new));
    }
}
