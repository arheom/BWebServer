package org.bwebserver.config;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public class ConfigProvider {

    private static ConfigProvider provider;
    private ServiceLoader<ConfigService> loader;
    private ConfigProvider() {
        loader = ServiceLoader.load(ConfigService.class);
    }
    public static ConfigProvider getInstance() {
        if(provider == null) {
            provider = new ConfigProvider();
        }
        return provider;
    }
    public ConfigService serviceImpl() {
        //TODO: extend for more than one service, connected to a control plane
        ConfigService service = loader.iterator().next();
        if(service != null) {
            return service;
        } else {
            throw new NoSuchElementException(
                    "No implementation for ConfigProvider");
        }
    }
}