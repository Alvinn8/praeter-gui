package ca.bkaw.praeter.gui.pack.send;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sends resource packs by replying to HTTP requests received
 * on the server's TCP connections.
 */
@ChannelHandler.Sharable
public class BuiltInTcpResourcePackSender extends ChannelInboundHandlerAdapter implements ResourcePackSender {
    private static final String HANDLER_KEY = "praeter_gui_resource_pack_sender";
    private static final String PATH = "/praeter/resource_pack.zip";

    private final PraeterGui praeterGui;

    public BuiltInTcpResourcePackSender(PraeterGui praeterGui) throws ReflectiveOperationException {
        this.praeterGui = praeterGui;
        this.praeterGui.getPlatform().injectChannelHandler(this, HANDLER_KEY);
    }

    @Override
    public void send(Audience player, boolean required, @Nullable Component prompt) {
        int port = this.praeterGui.getPlatform().getServerPort();
        PraeterGuiAssets assets = this.praeterGui.getAssets();
        String sha1Hash = assets.getSha1Hash();
        if (sha1Hash == null) {
            LOGGER.warn("Resource pack does not exist yet, but tried to send it.");
            return;
        }
        InetAddress playerAddress = this.praeterGui.getPlatform().getPlayerAddress(player);
        String url = "http://" + Utils.getHostnameFor(playerAddress) + ":" + port + PATH;
        player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
            .packs(
                ResourcePackInfo.resourcePackInfo(PraeterGuiAssets.PACK_UUID, URI.create(url), sha1Hash)
            )
            .required(required)
            .prompt(prompt)
        );
    }

    @Override
    public void remove() throws ReflectiveOperationException {
        this.praeterGui.getPlatform().uninjectChannelHandler(HANDLER_KEY);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byteBuf.markReaderIndex();
        if (!this.handle(ctx, byteBuf)) {
            // handle returned false, reset reader and call the super method to let vanilla
            // handle the connection.
            byteBuf.resetReaderIndex();
            super.channelRead(ctx, msg);
        }
    }

    private boolean handle(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        // There needs to be at least 16 bytes for "GET /praeter/<resource pack id>" to fit.
        if (byteBuf.capacity() < 16) return false;

        // Start by efficiently comparing byte by byte as this is some hot networking
        // code. This code runs for every received packet.

        if (byteBuf.readByte() != 'G') return false;
        if (byteBuf.readByte() != 'E') return false;
        if (byteBuf.readByte() != 'T') return false;
        if (byteBuf.readByte() != ' ') return false;

        // An HTTP GET request was received.
        // Let's ensure it is requesting a resource pack from us

        byte[] pathBytes = PATH.getBytes(StandardCharsets.UTF_8);
        for (byte pathByte : pathBytes) {
            if (byteBuf.readByte() != pathByte) return false;
        }

        // Read the pack id from the path

        StringBuilder resourcePackId = new StringBuilder();
        byte b;
        while (byteBuf.readableBytes() > 0 && (b = byteBuf.readByte()) != ' ') {
            resourcePackId.append((char) b);
        }

        // TODO we may want to create a request id and only reply to that to avoid people
        //  using resource packs as a way to slow the server / waste network bandwidth.
        //  Currently, we ignore the id.

        // Get the requested pack
        PraeterGuiAssets assets = this.praeterGui.getAssets();
        Path resourcePackPath = assets.getResourcePackPath();
        String sha1Hash = assets.getSha1Hash();

        if (sha1Hash == null) {
            // Sorry, we do not gracefully reply with an HTTP 404 response. We just close
            // the connection.
            ctx.close();

            // Return true, we have handled the packet.
            return true;
        }

        try {
            long contentLength = Files.size(resourcePackPath);

            String headerText =
                """
                    HTTP/1.1 200 OK
                    Server: PraeterGui
                    Content-Type: application/zip
                    Content-Length: %d
                    
                    """.formatted(contentLength);
            byte[] headerBytes = headerText.getBytes(StandardCharsets.UTF_8);

            ByteBuf response = Unpooled.buffer(headerBytes.length + (int) contentLength);

            response.writeBytes(headerBytes);

            ByteBufOutputStream stream = new ByteBufOutputStream(response);
            Files.copy(resourcePackPath, stream);
            stream.close();

            // Send the response
            ctx.pipeline().firstContext().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

            // Return true, we have handled the packet
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to reply with resource pack.", e);

            // Sorry, we do not gracefully reply with an HTTP 500 response. We just close
            // the connection.
            ctx.close();

            // Return true, we have handled the packet.
            return true;
        }
    }
}