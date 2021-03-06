package edgruberman.bukkit.consolecolor.craftbukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.ConsoleHandler;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;

public abstract class CraftBukkit {

    public static CraftBukkit create() throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Class<?> provider = Class.forName(CraftBukkit.class.getPackage().getName() + "." + CraftBukkit.class.getSimpleName() + "_" + CraftBukkit.version());
        return (CraftBukkit) provider.getConstructor().newInstance();
    }

    private static String version() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        if (version.equals("craftbukkit")) version = "pre";
        return version;
    }

    public abstract OptionSet options();

    public abstract ConsoleHandler consoleHandler();

}
