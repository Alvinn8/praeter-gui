package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.player.GuiPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * A {@link GuiPlayer} that wraps a Minecraft {@link Player}.
 */
public final class FabricGuiPlayer implements GuiPlayer {
    private final Player player;

    private FabricGuiPlayer(Player player) {
        this.player = player;
    }

    /**
     * Create a {@link GuiPlayer} from a player.
     *
     * @param player The player.
     * @return The gui player.
     */
    public static FabricGuiPlayer of(Player player) {
        return new FabricGuiPlayer(player);
    }

    /**
     * Get the player this gui player wraps.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public String getName() {
        return this.player.getName().getString();
    }
}
