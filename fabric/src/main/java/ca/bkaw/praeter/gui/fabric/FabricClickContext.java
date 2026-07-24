package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.gui.ClickContext;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.slot.SlotInteraction;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * A {@link ClickContext} for a click on Fabric.
 */
public class FabricClickContext extends ClickContext {
    private final Player player;
    private boolean cancelled = false;

    public FabricClickContext(CustomGui gui, SlotInteraction slotInteraction, Player player) {
        super(gui, slotInteraction);
        this.player = player;
    }

    /**
     * Get the player that clicked.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public void playClickSound() {
        if (!(this.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        serverPlayer.connection.send(new ClientboundSoundPacket(
            SoundEvents.UI_BUTTON_CLICK, SoundSource.UI,
            serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
            1f, 1f, serverPlayer.level().getRandom().nextLong())
        );
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}
