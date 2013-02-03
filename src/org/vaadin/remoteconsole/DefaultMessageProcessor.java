package org.vaadin.remoteconsole;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DefaultMessageProcessor implements MessageProcessor {
    private final Logger logger = Logger
            .getLogger(DefaultMessageProcessor.class.getName());

    @Override
    public void processMessage(final long time, String message, boolean isError) {
        Level level = isError ? Level.SEVERE : Level.INFO;
        if (logger.isLoggable(level)) {
            LogRecord logRecord = new LogRecord(level, message);
            logRecord.setMillis(time);
            logger.log(logRecord);
        }
    }

}
