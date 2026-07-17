package ca.bkaw.praeter.gui.fabric.mixin;

import ca.bkaw.praeter.gui.fabric.platform.FabricPlatform;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Registers {@link FabricPlatform}'s injected channel handlers on every newly
 * accepted connection.
 */
@Mixin(targets = "net/minecraft/server/network/ServerConnectionListener$1")
public class ServerConnectionListenerMixin {
    @Inject(method = "initChannel", at = @At("TAIL"))
    private void praeterGui$afterInitChannel(Channel channel, CallbackInfo ci) {
        FabricPlatform platform = FabricPlatform.getInstance();
        if (platform == null) {
            return;
        }
        for (Map.Entry<String, ChannelHandler> entry : platform.getChannelHandlers().entrySet()) {
            channel.pipeline().addFirst(entry.getKey(), entry.getValue());
        }
    }
}
