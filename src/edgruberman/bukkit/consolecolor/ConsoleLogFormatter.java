package edgruberman.bukkit.consolecolor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.bukkit.craftbukkit.libs.joptsimple.OptionException;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import edgruberman.bukkit.consolecolor.util.CustomLevel;

public class ConsoleLogFormatter extends Formatter {

    private static final String DEFAULT_STAMP_VANILLA = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_STAMP_CRAFTBUKKIT = "HH:mm:ss";
    private static final String DEFAULT_LEVEL_PATTERN = "{0}";

    private static final String LEVEL_DEFAULT = "(default)";

    // based on logic in ShortConsoleLogFormatter constructor
    public static SimpleDateFormat stamp(final OptionSet options) {
        if (options.has("date-format"))
            try {
                final Object object = options.valueOf("date-format");
                if ((object != null) && (object instanceof SimpleDateFormat)) return (SimpleDateFormat) object;
            } catch (final OptionException e) {
                System.err.println("Given date format is not valid. Falling back to default.");
            }

        if (options.has("nojline")) return new SimpleDateFormat(ConsoleLogFormatter.DEFAULT_STAMP_VANILLA);

        return new SimpleDateFormat(ConsoleLogFormatter.DEFAULT_STAMP_CRAFTBUKKIT);
    }



    // ---- instance ----

    private final Map<Level, MessageFormat> levels = new HashMap<Level, MessageFormat>();
    private final MessageFormat pattern;
    private final SimpleDateFormat stamp;
    private final boolean showCodes;
    private final Formatter original;
    private final Plugin plugin;

    public ConsoleLogFormatter(final String pattern, final SimpleDateFormat stamp, final boolean showCodes, final Formatter original, final Plugin plugin) {
        this.pattern = new MessageFormat(AnsiColor.translate(pattern));
        this.stamp = stamp;
        this.showCodes = showCodes;
        this.original = original;
        this.plugin = plugin;
        this.putLevel(ConsoleLogFormatter.LEVEL_DEFAULT, ConsoleLogFormatter.DEFAULT_LEVEL_PATTERN);
    }

    /** @throws IllegalArgumentException if name is not valid (same as {@link java.util.logging.Level#parse(String)}) */
    public void putLevel(final String name, final String pattern) {
        final Level level = ( name.equals(ConsoleLogFormatter.LEVEL_DEFAULT) ? null : Level.parse(name) );
        this.levels.put(level, new MessageFormat(AnsiColor.translate(pattern)));
    }

    @Override
    public String format(final LogRecord record) {
        // 0 = Record Date, 1 = Level Name, 2 = Message, 3 = Level Value, 4 = Server Timestamp
        final Object[] arguments = new Object[] {
                new Date(record.getMillis())
              , this.formatLevel(record.getLevel())
              , this.formatMessage(record)
              , record.getLevel().intValue()
              , this.stamp.format(record.getMillis())
        };

        final StringWriter message = new StringWriter();
        try {
            message.write(this.pattern.format(arguments));
            message.write("\n");
        } catch (final Exception e) {
            message.write(this.describeException(e));
            this.debug(message, arguments, e);
            message.write(this.originalFormat(record));
            return message.toString();
        }

        if (record.getThrown() != null) {
            record.getThrown().printStackTrace(new PrintWriter(message));
            message.write("\n");
        }

        return message.toString();
    }

    @Override
    public synchronized String formatMessage(final LogRecord record) {
        return AnsiColor.translate(super.formatMessage(record), ( this.showCodes ? "{0}$1" : "{0}" ));
    }

    private String formatLevel(final Level level) {
        final MessageFormat levelPattern = this.levels.get(( this.levels.containsKey(level) ? level : null ));
        return levelPattern.format(new Object[] { level.getLocalizedName() });
    }

    private String describeException(final Exception e) {
        final LogRecord description = new LogRecord(Level.SEVERE, "[{0}] Error formatting log record (check pattern); {1}");
        return this.originalFormat(description, ConsoleLogFormatter.prefix(this.plugin), e.toString());
    }

    private void debug(final StringWriter message, final Object[] arguments, final Exception e) {
        if (!(e instanceof IllegalArgumentException) || !this.plugin.getLogger().isLoggable(CustomLevel.DEBUG)) return;

        final String prefix = ConsoleLogFormatter.prefix(this.plugin);

        final LogRecord details = new LogRecord(CustomLevel.DEBUG, "[{0}] pattern: {1}");
        message.write(this.originalFormat(details, prefix, this.pattern.toPattern()));

        if (!this.plugin.getLogger().isLoggable(CustomLevel.TRACE)) return;
        for (int i = 0; i < arguments.length; i++) {
            final LogRecord arg = new LogRecord(CustomLevel.TRACE, "[{0}] argument '{'{1}} {2}: {3}");
            message.write(this.originalFormat(arg, prefix, i, arguments[i].getClass().getName(), arguments[i].toString()));
        }
    }

    private String originalFormat(final LogRecord record, final Object... parameters) {
        record.setParameters(parameters);
        return this.original.format(record);
    }

    private static String prefix(final Plugin plugin) {
        final PluginDescriptionFile pdf = plugin.getDescription();
        return ( pdf.getPrefix() != null ? pdf.getPrefix() : pdf.getName() );
    }

}
