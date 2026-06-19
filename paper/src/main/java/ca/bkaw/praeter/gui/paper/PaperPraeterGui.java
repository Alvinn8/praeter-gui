package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.PraeterGui;
import org.bukkit.plugin.Plugin;

public class PaperPraeterGui {
    public static void init(Plugin plugin) {
        PraeterGui praeterGui = PraeterGui.instance();
        PaperPlatform platform = (PaperPlatform) praeterGui.getPlatform();
        platform.setPlugin(plugin);
    }
}
