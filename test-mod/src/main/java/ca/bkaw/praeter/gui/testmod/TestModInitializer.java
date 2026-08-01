package ca.bkaw.praeter.gui.testmod;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.fabric.FabricCustomGui;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiRegistry;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TestModInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        CustomGuiRegistry.register("praeter_gui_testmod:example", ExampleGui.TYPE);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("example")
                .executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                        CustomGui gui = ExampleGui.TYPE.create();
                        ((FabricCustomGui) gui).show(player);
                    }
                    return Command.SINGLE_SUCCESS;
                }));

            dispatcher.register(Commands.literal("praeter-reload")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("Reloading praeter-gui..."), false);
                    PraeterGui.instance().reload();
                    ctx.getSource().sendSuccess(() -> Component.literal("Reload complete."), false);
                    return Command.SINGLE_SUCCESS;
                }));
        });
    }
}
