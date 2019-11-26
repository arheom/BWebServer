package org.bwebserver.logging.impl;

import org.bwebserver.config.ConfigProvider;
import org.bwebserver.config.ConfigService;
import org.bwebserver.logging.LogLevel;
import org.bwebserver.logging.LoggerService;

import java.time.LocalDateTime;
import java.util.EnumSet;

/**
 * Very basic console logger server (easier to develop).
 * To be replaced with a good logger service or component. But in here could be decorated with special
 * implementation needs
 */
public class ConsoleLoggerService implements LoggerService {
    //private String filterLoggingByThread = "pool-1-thread-1";
    private String filterLoggingByThread = "";
    private ConfigService config = ConfigProvider.getInstance().serviceImpl();
    private EnumSet<LogLevel> logLevel;

    public ConsoleLoggerService(){
        logLevel = config.getLogLevels();
    }

    @Override
    public void LogError(String message) {
        Log(message, LogLevel.Error);
    }

    @Override
    public void LogWarning(String message) {
        Log(message, LogLevel.Warning);
    }

    @Override
    public void LogInfo(String message) {
        Log(message, LogLevel.Info);
    }

    @Override
    public void LogDebug(String message) {
        Log(message, LogLevel.Debug);
    }

    private void Log(String message, LogLevel level) {
        if (shouldLog(level)) {
            String messageToLog = String.format("%s: %s -> thread %s: %s", level.toString(), LocalDateTime.now().toString(), Thread.currentThread().getName(), message);
            if (level == LogLevel.Error) {
                System.err.println(messageToLog);
            } else {
                System.out.println(messageToLog);
            }
        }
    }

    /**
     * Decide if it should log
     * @param level log level to store
     */
    private boolean shouldLog(LogLevel level){
        return logLevel.contains(level) &&
                (Thread.currentThread().getName().equalsIgnoreCase(filterLoggingByThread) ||
                        Thread.currentThread().getName().equalsIgnoreCase("main") ||
                        filterLoggingByThread.isEmpty());
    }
}


