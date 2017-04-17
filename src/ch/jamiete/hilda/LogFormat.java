package ch.jamiete.hilda;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class LogFormat extends Formatter {
    private final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd kk:mm:ss");

    @Override
    public String format(final LogRecord record) {
        String log = "";

        log += this.sdf.format(new Date(record.getMillis()));
        log += " [" + record.getLevel().getLocalizedName().toUpperCase() + "]";

        if (record.getSourceClassName() != null) {
            final String[] split = record.getSourceClassName().split("\\.");
            log += " [" + split[split.length == 1 ? 0 : split.length - 1] + "]";
        }

        log += " " + record.getMessage();

        if (record.getThrown() != null) {
            log += System.getProperty("line.separator") + ExceptionUtils.getStackTrace(record.getThrown());
        }

        log += System.getProperty("line.separator");

        return log;
    }
}