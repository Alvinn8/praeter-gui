package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.gui.ClickContext;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.slot.SlotInteraction;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * A {@link ClickContext} for a click on Paper.
 */
public class PaperClickContext extends ClickContext {
    private final InventoryClickEvent event;

    public PaperClickContext(CustomGui gui, SlotInteraction slotinteraction, InventoryClickEvent event) {
        super(gui, slotinteraction);
        this.event = event;
    }

    /**
     * Get the {@link InventoryClickEvent} for the click.
     *
     * @return The event.
     */
    public InventoryClickEvent getEvent() {
        return event;
    }

    /**
     * Get the player that clicked, equivalent to {@code ctx.getEvent().getWhoClicked()}.
     *
     * @return The player.
     */
    public HumanEntity getPlayer() {
        return this.event.getWhoClicked();
    }

    @Override
    public void playClickSound() {
        this.getPlayer().playSound(
            Sound.sound()
                .type(org.bukkit.Sound.UI_BUTTON_CLICK)
                .source(Sound.Source.UI)
                .build()
        );
    }

    /**
     * Cancel the click event. Equivalent to {@code ctx.getEvent().setCancelled(true)}.
     */
    @Override
    public void cancel() {
        this.event.setCancelled(true);
    }

    /**
     * Check if the click event has been canceled. Equivalent to {@code ctx.getEvent().isCancelled()}.
     *
     * @return True if the click event has been canceled, false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return this.event.isCancelled();
    }
}
