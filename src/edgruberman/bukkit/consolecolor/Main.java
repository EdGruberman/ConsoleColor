package edgruberman.bukkit.consolecolor;

import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import edgruberman.bukkit.consolecolor.commands.Reload;
import edgruberman.bukkit.consolecolor.craftbukkit.CraftBukkit;
import edgruberman.bukkit.consolecolor.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.consolecolor.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;

    private ConsoleHandler handler = null;
    private Formatter original = null;
    private boolean first = true;

    @Override
    public void onLoad() {
        this.putConfigMinimum("1.2.0");
        this.enable();
    }

    @Override
    public void onEnable() {
        if (!this.first) this.enable();
        this.first = false;
        this.getCommand("consolecolor:reload").setExecutor(new Reload(this));
    }

    private void enable() {
        // establish version support for CraftBukkit obc/nms access
        CraftBukkit cb;
        try {
            cb = CraftBukkit.create();
        } catch (final Exception e) {
            this.getLogger().severe("Unsupported CraftBukkit version " + Bukkit.getVersion() + "; " + e);
            this.getLogger().severe("Disabling plugin; Check " + this.getDescription().getWebsite() + " for updates");
            this.setEnabled(false);
            return;
        }

        // standard plugin enable
        this.reloadConfig();
        Main.courier = ConfigurationCourier.Factory.create(this).setFormatCode("format-code").build();

        this.handler = cb.consoleHandler();
        this.original = this.handler.getFormatter();
        final String patternLog = Main.courier.translate("pattern");
        final SimpleDateFormat stamp = ConsoleLogFormatter.stamp(cb.options());
        final boolean showCodes = this.getConfig().getBoolean("show-codes");
        final ConsoleLogFormatter custom = new ConsoleLogFormatter(patternLog, stamp, showCodes, this.original, this);

        // load level patterns from config
        final ConfigurationSection levels = this.getConfig().getConfigurationSection("levels");
        if (levels != null)
            for (final String name : levels.getKeys(false))
                try {
                    final String patternLevel = Main.courier.translate("levels." + name);
                    custom.putLevel(name, patternLevel);
                } catch (final Exception e) {
                    this.getLogger().log(Level.WARNING, "Discarded pattern for unrecognized logger in levels: {0}; {1}", new Object[] { name, e });
                }

        // substitute custom Formatter
        this.handler.setFormatter(custom);
    }

    @Override
    public void onDisable() {
        if (this.handler != null) this.handler.setFormatter(this.original);
        Main.courier = null;
    }

}
