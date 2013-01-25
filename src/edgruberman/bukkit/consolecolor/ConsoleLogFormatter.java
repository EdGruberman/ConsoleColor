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

    public ConsoleLogFormatter(final String pattern, final SimpleDateFormat stamp, final boolean showCodes, final Formatter original) {
        this.pattern = new MessageFormat(AnsiColor.translate(pattern));
        this.stamp = stamp;
        this.showCodes = showCodes;
        this.original = original;
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
            message.write("    [ConsoleColor] Using original formatter due to error: ");
            message.write(e.toString());
            message.write("\n");
            message.write(this.original.format(record));
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

}
