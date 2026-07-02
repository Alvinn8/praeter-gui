package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.player.GuiPlayer;
import org.bukkit.entity.HumanEntity;

/**
 * A {@link GuiPlayer} that wraps a Bukkit {@link HumanEntity}.
 */
public final class PaperGuiPlayer implements GuiPlayer {
    private final HumanEntity player;

    private PaperGuiPlayer(HumanEntity player) {
        this.player = player;
    }

    /**
     * Create a {@link GuiPlayer} from a Bukkit player.
     *
     * @param player The player.
     * @return The gui player.
     */
    public static PaperGuiPlayer of(HumanEntity player) {
        return new PaperGuiPlayer(player);
    }

    /**
     * Get the Bukkit player this gui player wraps.
     *
     * @return The player.
     */
    public HumanEntity getHumanEntity() {
        return this.player;
    }

    @Override
    public String getName() {
        return this.player.getName();
    }
}
