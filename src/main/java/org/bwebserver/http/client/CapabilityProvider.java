package org.bwebserver.http.client;

import java.util.*;

/**
 * Provider for the registered client capabilities
 */
public class CapabilityProvider {

    private static CapabilityProvider provider;
    private ServiceLoader<Capability> loader;
    private CapabilityProvider() {
        loader = ServiceLoader.load(Capability.class);
    }
    public static CapabilityProvider getInstance() {
        if(provider == null) {
            provider = new CapabilityProvider();
        }
        return provider;
    }
    public ArrayList<Capability> getAllRegisteredCapabilities() {
        ArrayList<Capability> capabilities = new ArrayList<>();
        for (Capability capability : loader){
            capabilities.add(capability);
        }
        return capabilities;
    }
}