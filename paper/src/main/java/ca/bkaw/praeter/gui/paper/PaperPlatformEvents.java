package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.PlatformEvents;
import io.papermc.paper.event.connection.configuration.PlayerConnectionInitialConfigureEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PaperPlatformEvents implements Listener {
    private final PlatformEvents platformEvents;

    public PaperPlatformEvents(PlatformEvents platformEvents) {
        this.platformEvents = platformEvents;
    }

    @EventHandler
    public void onPlayerConfigure(PlayerConnectionInitialConfigureEvent event) {
        this.platformEvents.onPlayerConfigure(event.getConnection().getAudience());
    }
}
