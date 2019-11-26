package org.bwebserver.heartbeat;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public class HeartBeatProvider {

    private static HeartBeatProvider provider;
    private ServiceLoader<HeartBeatService> loader;
    private HeartBeatProvider() {
        loader = ServiceLoader.load(HeartBeatService.class);
    }
    public static HeartBeatProvider getInstance() {
        if(provider == null) {
            provider = new HeartBeatProvider();
        }
        return provider;
    }
    public HeartBeatService serviceImpl() {
        //TODO: extend for more than one service, connected to a control plane
        HeartBeatService service = loader.iterator().next();
        if(service != null) {
            return service;
        } else {
            throw new NoSuchElementException(
                    "No implementation for LoggerProvider");
        }
    }
}