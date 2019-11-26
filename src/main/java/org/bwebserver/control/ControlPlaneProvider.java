package org.bwebserver.control;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public class ControlPlaneProvider {

    private static ControlPlaneProvider provider;
    private ServiceLoader<ControlPlaneService> loader;
    private ControlPlaneProvider() {
        loader = ServiceLoader.load(ControlPlaneService.class);
    }
    public static ControlPlaneProvider getInstance() {
        if(provider == null) {
            provider = new ControlPlaneProvider();
        }
        return provider;
    }
    public ControlPlaneService serviceImpl() {
        //TODO: extend for more than one service, connected to a control plane
        ControlPlaneService service = loader.iterator().next();
        if(service != null) {
            return service;
        } else {
            throw new NoSuchElementException(
                    "No implementation for ControlPlaneProvider");
        }
    }
}