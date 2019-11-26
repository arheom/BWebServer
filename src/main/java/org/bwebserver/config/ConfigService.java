package org.bwebserver.config;

import org.bwebserver.logging.LogLevel;

import java.util.EnumSet;

public interface ConfigService {
    int getPort();
    int getNThreads();
    int getConnectionTimeout();
    String getContentRootPath();
    int getMaxCPUUsage();
    EnumSet<LogLevel> getLogLevels();
}
