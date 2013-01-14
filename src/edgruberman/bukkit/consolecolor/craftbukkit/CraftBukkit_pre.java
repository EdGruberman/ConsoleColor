package edgruberman.bukkit.consolecolor.craftbukkit;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import net.minecraft.server.ConsoleLogManager;
import net.minecraft.server.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;

public class CraftBukkit_pre extends CraftBukkit {

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
