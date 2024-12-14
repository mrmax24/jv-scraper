package scraper.app.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

public class LogConfig {

    public static void configureLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%-5level %logger{36} - %msg%n");
        encoder.setContext(context);
        encoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setFile("logs/scraper.log");
        fileAppender.setEncoder(encoder);
        fileAppender.setContext(context);

        ThresholdFilter fileFilter = new ThresholdFilter();
        fileFilter.setLevel("INFO");
        fileFilter.setContext(context);
        fileFilter.start();
        fileAppender.addFilter(fileFilter);

        fileAppender.start();

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.ALL);
        rootLogger.addAppender(fileAppender);
    }
}
