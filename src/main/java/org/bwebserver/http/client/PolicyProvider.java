package org.bwebserver.http.client;

import java.util.ArrayList;
import java.util.ServiceLoader;

/**
 * Provider for the registered server policies
 */
public class PolicyProvider {

    private static PolicyProvider provider;
    private ServiceLoader<Policy> loader;
    private PolicyProvider() {
        loader = ServiceLoader.load(Policy.class);
    }
    public static PolicyProvider getInstance() {
        if(provider == null) {
            provider = new PolicyProvider();
        }
        return provider;
    }
    public ArrayList<Policy> getAllRegisteredPolicies() {
        ArrayList<Policy> policies = new ArrayList<>();
        for (Policy policy : loader){
            policies.add(policy);
        }
        return policies;
    }
}