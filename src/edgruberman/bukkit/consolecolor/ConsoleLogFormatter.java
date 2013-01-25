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

    private final Map<Level, String> levelPatterns = new HashMap<Level, String>();
    private final MessageFormat pattern;
    private final SimpleDateFormat stamp;
    private final boolean showCodes;

    public ConsoleLogFormatter(final String pattern, final SimpleDateFormat stamp, final boolean showCodes) {
        this.pattern = new MessageFormat(AnsiColor.translate(pattern));
        this.stamp = stamp;
        this.showCodes = showCodes;
        this.levelPatterns.put(null, ConsoleLogFormatter.DEFAULT_LEVEL_PATTERN);
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
        final String message = this.pattern.format(arguments);

        StringWriter trace = null;
        if (record.getThrown() != null) {
            trace = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(trace));
        }

        return message + "\n" + ( trace == null ? "" : trace );
    }

    @Override
    public synchronized String formatMessage(final LogRecord record) {
        return AnsiColor.translate(super.formatMessage(record), ( this.showCodes ? "{0}$1" : "{0}" ));
    }

    /** @throws IllegalArgumentException if name is not valid (same as {@link java.util.logging.Level#parse(String)}) */
    public void putLevelPattern(final String name, final String pattern) {
        final Level level = ( name.equals(ConsoleLogFormatter.LEVEL_DEFAULT) ? null : Level.parse(name) );
        this.levelPatterns.put(( level == null ? null : level ), pattern);
    }

    private String formatLevel(final Level level) {
        final String levelPattern = (this.levelPatterns.get(( this.levelPatterns.containsKey(level) ? level : null )));
        return AnsiColor.translate(MessageFormat.format(levelPattern, level.getLocalizedName().toUpperCase()));
    }

}
