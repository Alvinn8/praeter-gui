package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.click.AbstractClickContext;
import ca.bkaw.praeter.gui.gui.CustomGui;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * A {@link ca.bkaw.praeter.gui.click.ClickContext} that additionally exposes the
 * Bukkit {@link InventoryClickEvent} that caused the click.
 */
public final class PaperClickContext extends AbstractClickContext {
    private final Player player;
    private final InventoryClickEvent event;

    public PaperClickContext(CustomGui gui, Player player, int rawSlot, InventoryClickEvent event) {
        super(gui, PaperGuiPlayer.of(player), rawSlot);
        this.player = player;
        this.event = event;
    }

    /**
     * Get the Bukkit event that caused this click.
     *
     * @return The event.
     */
    public InventoryClickEvent getEvent() {
        return this.event;
    }

    @Override
    public void playClickSound() {
        this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }
}
