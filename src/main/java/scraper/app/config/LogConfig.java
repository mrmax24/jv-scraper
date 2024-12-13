package scraper.app.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import java.io.File;
import org.slf4j.LoggerFactory;

public class LogConfig {

    public static void configureLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%-5level %logger{36} - %msg%n");
        encoder.setContext(context);
        encoder.start();
        consoleAppender.setEncoder(encoder);
        consoleAppender.setContext(context);
        consoleAppender.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setFile("logs/scraper.log");
        fileAppender.setEncoder(encoder);
        fileAppender.setContext(context);
        fileAppender.start();

        Logger consoleLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        consoleLogger.setLevel(Level.INFO);
        consoleLogger.addAppender(fileAppender);
    }
}
