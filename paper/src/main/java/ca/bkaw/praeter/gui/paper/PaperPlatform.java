package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.Platform;
import ca.bkaw.praeter.gui.PraeterGui;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Paper-backed {@link Platform}.
 */
public final class PaperPlatform implements Platform {
    private @Nullable Plugin plugin;

    public void setPlugin(Plugin plugin) {
        if (this.plugin != null && this.plugin == plugin) {
            return;
        }
        this.plugin = plugin;
        PaperPlatformEvents events = new PaperPlatformEvents(PraeterGui.instance().getPlatformEvents());
        this.plugin.getServer().getPluginManager().registerEvents(events, this.plugin);
    }

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
        method.invoke(null, Key.key("praetergui", handlerKey), listener);
    }

    @Override
    public void uninjectChannelHandler(String handlerKey) throws ReflectiveOperationException {
        // Remove the listener
        Class<?> holderClass = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
        Method method = holderClass.getMethod("removeListener", Key.class);
        method.invoke(null, Key.key("praetergui", handlerKey));
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
}
