package ca.bkaw.praeter.gui.paper.platform;

import ca.bkaw.praeter.gui.slot.SlotPos;
import ca.bkaw.praeter.gui.paper.PaperCustomGui;
import ca.bkaw.praeter.gui.paper.PaperGuiListener;
import ca.bkaw.praeter.gui.paper.PaperHooks;
import ca.bkaw.praeter.gui.paper.PaperPlatformEvents;
import ca.bkaw.praeter.gui.platform.GuiPlayer;
import ca.bkaw.praeter.gui.platform.Platform;
import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.collision.ResourceCollisionException;
import ca.bkaw.praeter.gui.render.RenderContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Paper-backed {@link Platform}.
 */
public final class PaperPlatform implements Platform {
    /**
     * The package praeter-gui-paper's classes ship under before shading/relocation.
     * <p>
     * Avoid a constant string literal by using join since shade/shadow relocation will
     * relocate string constants that match the relocation pattern.
     */
    private static final String UNRELOCATED_PACKAGE = String.join(".", "ca", "bkaw", "praeter");
    /**
     * The placeholder package used in the getting-started docs page.
     */
    private static final String PLACEHOLDER_PACKAGE = "your.plugin.package";

    private final Set<Plugin> handledPlugins = new HashSet<>();
    private @Nullable Plugin mainPlugin;

    @Override
    public int getServerPort() {
        return Bukkit.getPort();
    }

    @Override
    public void injectChannelHandler(ChannelHandler channelHandler, String handlerKey) throws ReflectiveOperationException {
        // Implement the ChannelInitializeListener interface using a proxy
        Class<?> listenerClass = Class.forName("io.papermc.paper.network.ChannelInitializeListener");
        Object listener = Proxy.newProxyInstance(
            PaperPlatform.class.getClassLoader(),
            new Class[]{ listenerClass },
            (proxy, method, args) -> {
                if ("afterInitChannel".equals(method.getName())) {
                    Channel channel = (Channel) args[0];
                    channel.pipeline().addFirst(handlerKey, channelHandler);
                    return null;
                }
                return method.invoke(proxy, args);
            });

        // Add the listener
        Class<?> holderClass = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
        Method method = holderClass.getMethod("addListener", Key.class, listenerClass);
        method.invoke(null, Key.key("praeter_gui", handlerKey), listener);
    }

    @Override
    public void uninjectChannelHandler(String handlerKey) throws ReflectiveOperationException {
        // Remove the listener
        Class<?> holderClass = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
        Method method = holderClass.getMethod("removeListener", Key.class);
        method.invoke(null, Key.key("praeter_gui", handlerKey));
    }

    private void assignMainPlugin(Plugin plugin) {
        this.mainPlugin = plugin;
        PraeterGui praeterGui = PraeterGui.instance();
        PaperPlatformEvents events = new PaperPlatformEvents(praeterGui.getPlatformEvents());
        this.mainPlugin.getServer().getPluginManager().registerEvents(events, this.mainPlugin);
        PaperGuiListener guiListener = new PaperGuiListener(this.mainPlugin);
        this.mainPlugin.getServer().getPluginManager().registerEvents(guiListener, this.mainPlugin);

        this.nagIfNotRelocated(plugin);
    }

    /**
     * Nag the plugin author(s) if praeter-gui-paper was not shaded and relocated
     * into the plugin's own package, since that causes classpath conflicts when
     * multiple plugins on the same server shade different praeter-gui versions.
     */
    private void nagIfNotRelocated(Plugin plugin) {
        if (!plugin.isNaggable()) {
            return;
        }
        String ownPackage = this.getClass().getPackageName();
        String problem;
        if (ownPackage.startsWith(UNRELOCATED_PACKAGE)) {
            problem = "praeter-gui-paper must be relocated to your own package to avoid classpath conflicts with other plugins bundling praeter-gui.";
        } else if (ownPackage.startsWith(PLACEHOLDER_PACKAGE)) {
            problem = "praeter-gui-paper was relocated but left under the placeholder package ('your.plugin.package') from the getting-started docs instead of being relocated into your own package.";
        } else {
            return;
        }
        plugin.setNaggable(false);
        plugin.getLogger().severe(String.format(
            "Nag author(s): '%s' of '%s' about the following: %s",
            plugin.getPluginMeta().getAuthors(),
            plugin.getPluginMeta().getDisplayName(),
            problem
        ));
    }

    private void includeAssets(PraeterGui gui, Plugin plugin) {
        try {
            ResourcePack jarResources = PraeterGuiAssets.getJarResources(plugin.getClass());
            gui.getAssets().includeAssets(jarResources);
            jarResources.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to include assets from plugin " + plugin.getName(), e);
        } catch (ResourceCollisionException e) {
            throw new RuntimeException("Resource collision when including assets from plugin " + plugin.getName(), e);
        }
    }

    @Override
    public void guessOwner(Class<?> clazz) {
        JavaPlugin plugin;
        try {
            plugin = JavaPlugin.getProvidingPlugin(clazz);
        } catch (Exception ignored) {
            return;
        }
        if (this.handledPlugins.contains(plugin)) {
            return;
        }
        this.handledPlugins.add(plugin);
        if (this.mainPlugin == null) {
            this.assignMainPlugin(plugin);
        }
        // Include plugins assets in the resource pack
        PraeterGui praeterGui = PraeterGui.instance();
        if (praeterGui.hasAssets()) {
            this.includeAssets(praeterGui, plugin);
        }
    }

    @Override
    public @Nullable Path getStoragePath() {
        if (this.mainPlugin == null) {
            return null;
        }
        return this.mainPlugin.getDataFolder().toPath().resolve(".praeter_gui");
    }

    @Override
    public void includeAssetsFromOwners() {
        PraeterGui praeterGui = PraeterGui.instance();
        for (Plugin plugin : this.handledPlugins) {
            this.includeAssets(praeterGui, plugin);
        }
    }

    @Override
    public CustomGui createGui(CustomGuiType type) {
        return new PaperCustomGui(type);
    }

    @Override
    public List<GuiPlayer> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
            .map(player -> (GuiPlayer) new PaperGuiPlayer(player))
            .toList();
    }

    @Override
    public void plainTextHoverText(RenderContext r, SlotPos pos, String[] text) {
        PaperHooks.hoverText(r, pos, Arrays.stream(text)
            .map(Component::text)
            .toArray(Component[]::new));
    }
}
