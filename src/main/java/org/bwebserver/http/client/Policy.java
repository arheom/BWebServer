package org.bwebserver.http.client;

import org.bwebserver.BWebServer;
import org.bwebserver.http.HttpContext;
import org.bwebserver.http.HttpRequest;
import org.bwebserver.http.HttpResponse;
import org.bwebserver.http.protocol.HttpVersion;
import org.bwebserver.logging.LoggerService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Abstract class to ensure one server policy in client
 */
public abstract class Policy {
    protected String value;
    protected String headerName;
    protected EnumSet<HttpVersion> supportedVersions;
    protected LoggerService logger = BWebServer.getLoggerService();

    public Policy() {
    }
    public Policy(String value) {
        this.value = value;
    }

    /**
     * Returns true if the current connection ensures this policy
     * @param context current connection context
     * @return true if the current connection needs to ensure this policy
     */
    protected abstract boolean isDetected(HttpContext context);

    /**
     * Policy will apply before the response is sent
     * @param context current context
     */
    public abstract void beforeResponse(HttpContext context);

    /**
     * Policy will apply before the error response is sent
     * @param response current response
     */
    public abstract void beforeErrorSent(HttpResponse response);

    /**
     * Factory method to get all policies for the current client request
     * @param context current http request
     * @return list of policies
     */
    public static List<Policy> detectPolicies(HttpContext context){
        ArrayList<Policy> policies = new ArrayList<>();
        ArrayList<Policy> registeredPolicies = BWebServer.getServerRegisteredPolicies();
        for (Policy regPolicy : registeredPolicies ) {
            if (regPolicy.isDetected(context)){
                policies.add(regPolicy);
            }
        }
        return policies;
    }

    /**
     * Injects policies before the response
     * @param context - decorating the context with the enabled policies.
     */
    public static void applyBeforeResponsePolicies(HttpContext context){
        for(Policy policy : context.getSEnforcedPolicies()){
            policy.beforeResponse(context);
        }
    }

    /**
     * Injects policies before the error response
     * @param response - decorating the context with the enabled policies.
     */
    public static void applyBeforeErrorPolicies(HttpResponse response){
        for(Policy policy : BWebServer.getServerRegisteredPolicies()){
            policy.beforeErrorSent(response);
        }
    }

}
