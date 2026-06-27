package ca.bkaw.praeter.gui.testplugin;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiRegistry;
import ca.bkaw.praeter.gui.paper.PaperCustomGui;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        CustomGuiRegistry.register0("test_plugin:example1", ExampleGui1.TYPE);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                Commands.literal("example")
                    .executes(ctx -> {
                        ctx.getSource().getSender().sendPlainMessage("some message");
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(
                        Commands.literal("example1")
                            .executes(ctx -> {
                                CustomGui gui = ExampleGui1.TYPE.create();
                                if (ctx.getSource().getExecutor() instanceof Player player) {
                                    ((PaperCustomGui) gui).show(player);
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .build()
            );
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("Hello, " + event.getPlayer().getName() + "!"));
    }
}