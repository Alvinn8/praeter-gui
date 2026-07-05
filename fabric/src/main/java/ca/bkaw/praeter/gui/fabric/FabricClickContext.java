package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.click.AbstractClickContext;
import ca.bkaw.praeter.gui.gui.CustomGui;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;

/**
 * A {@link ca.bkaw.praeter.gui.click.ClickContext} that additionally exposes the
 * raw menu click info that caused the click.
 * <p>
 * Fabric's {@code clicked} method does not have a single native event object like
 * Paper's {@code InventoryClickEvent}, so the raw arguments are exposed
 * individually instead.
 */
public final class FabricClickContext extends AbstractClickContext {
    private final Player player;
    private final int button;
    private final ContainerInput input;

    public FabricClickContext(CustomGui gui, Player player, int rawSlot, int button, ContainerInput input) {
        super(gui, FabricGuiPlayer.of(player), rawSlot);
        this.player = player;
        this.button = button;
        this.input = input;
    }

    /**
     * Get the button argument passed to {@code clicked}.
     *
     * @return The button.
     */
    public int getButton() {
        return this.button;
    }

    /**
     * Get the input type that caused this click.
     *
     * @return The input type.
     */
    public ContainerInput getInput() {
        return this.input;
    }

    @Override
    public void playClickSound() {
        if (this.player instanceof ServerPlayer serverPlayer) {
            // Send the sound to only the clicking player, like on other platforms.
            serverPlayer.connection.send(new ClientboundSoundPacket(
                SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                1f, 1f, serverPlayer.level().getRandom().nextLong()));
        }
    }
}
