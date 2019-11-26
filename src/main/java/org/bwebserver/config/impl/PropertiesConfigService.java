package org.bwebserver.config.impl;

import org.bwebserver.config.ConfigService;
import org.bwebserver.logging.LogLevel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PropertiesConfigService implements ConfigService {
    private int port = 8080;
    private int nThreads = 50;
    private int connectionTimeout = 10000;
    private String contentRootPath = "C:\\Projects\\ServerContent";
    private int maxCPUUsage = 80;
    private EnumSet<LogLevel> logLevels = EnumSet.allOf(LogLevel.class);

    private String propsFileName = "configservice.properties";

    public PropertiesConfigService() {
        Properties props = new Properties();
        try {
            props.load(new FileReader(propsFileName));
            port = Integer.parseInt((String) props.get("port"));
            nThreads = Integer.parseInt((String) props.get("nThreads"));
            connectionTimeout = Integer.parseInt((String) props.get("connectionTimeout"));
            contentRootPath = (String) props.get("contentRootPath");
            maxCPUUsage = Integer.parseInt((String) props.get("maxCPUUsage"));

            String logLevelValue = (String) props.get("logLevelMin");
            logLevels = EnumSet.range(LogLevel.Error, LogLevel.valueOf(logLevelValue));
        } catch (FileNotFoundException e) {
            System.err.println(String.format("Configuration cannot be found. Using default values. %s", e.toString()));
        } catch (IOException e) {
            System.err.println(String.format("Configuration cannot be read. Using default values. %s", e.toString()));
        } catch (Exception e){
            System.err.println(String.format("Configuration cannot be loaded. Using default values. %s", e.toString()));
        }
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getNThreads() {
        return nThreads;
    }

    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public String getContentRootPath() {
        return contentRootPath;
    }

    @Override
    public int getMaxCPUUsage() {
        return maxCPUUsage;
    }

    @Override
    public EnumSet<LogLevel> getLogLevels() {
        return logLevels;
    }
}