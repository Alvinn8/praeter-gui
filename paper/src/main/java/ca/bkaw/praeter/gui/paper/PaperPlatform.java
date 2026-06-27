package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.Platform;
import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.collision.ResourceCollisionException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Paper-backed {@link Platform}.
 */
public final class PaperPlatform implements Platform {
    private final Set<Plugin> handledPlugins = new HashSet<>();
    private @Nullable Plugin mainPlugin;

    @Override
    public String name() {
        return "Paper";
    }

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

    @Override
    public @Nullable InetAddress getPlayerAddress(Audience audience) {
        if (audience instanceof Player player) {
            InetSocketAddress socketAddress = player.getAddress();
            if (socketAddress != null) {
                return socketAddress.getAddress();
            }
        }
        if (audience instanceof PlayerConfigurationConnection connection) {
            InetSocketAddress socketAddress = connection.getClientAddress();
            return socketAddress.getAddress();
        }
        return null;
    }

    private void assignMainPlugin(Plugin plugin) {
        this.mainPlugin = plugin;
        PraeterGui praeterGui = PraeterGui.instance();
        PaperPlatformEvents events = new PaperPlatformEvents(praeterGui.getPlatformEvents());
        this.mainPlugin.getServer().getPluginManager().registerEvents(events, this.mainPlugin);
    }

    private void includeAssets(PraeterGui gui, Plugin plugin) {
        try {
            ResourcePack jarResources = PraeterGuiAssets.getJarResources(plugin.getClass());
            gui.getAssets().includeAssets(jarResources);
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
}
