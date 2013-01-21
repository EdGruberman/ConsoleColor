package edgruberman.bukkit.consolecolor.craftbukkit;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import net.minecraft.server.v1_4_R1.ConsoleLogManager;
import net.minecraft.server.v1_4_R1.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;
import org.bukkit.craftbukkit.v1_4_R1.CraftServer;

public class CraftBukkit_v1_4_R1 extends CraftBukkit {

    @Override
    public OptionSet options() {
        final MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        return server.options;
    }

    @Override
    public ConsoleHandler consoleHandler() {
        for (final Handler handler : ConsoleLogManager.global.getHandlers())
            if (handler instanceof ConsoleHandler)
                return (ConsoleHandler) handler;

        throw new IllegalStateException("ConsoleHandler unavailable");
    }

}
