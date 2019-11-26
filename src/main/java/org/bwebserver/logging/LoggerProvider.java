package org.bwebserver.logging;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public class LoggerProvider {

    private static LoggerProvider provider;
    private ServiceLoader<LoggerService> loader;
    private LoggerProvider() {
        loader = ServiceLoader.load(LoggerService.class);
    }
    public static LoggerProvider getInstance() {
        if(provider == null) {
            provider = new LoggerProvider();
        }
        return provider;
    }
    public LoggerService serviceImpl() {
        //TODO: extend for more than one service, connected to a control plane
        LoggerService service = loader.iterator().next();
        if(service != null) {
            return service;
        } else {
            throw new NoSuchElementException("No implementation for LoggerProvider");
        }
    }
}