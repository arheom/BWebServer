package org.bwebserver.http.client;

import org.bwebserver.BWebServer;
import org.bwebserver.http.HttpContext;
import org.bwebserver.http.HttpRequest;
import org.bwebserver.http.protocol.HttpVersion;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import java.util.*;

/**
 * Abstract class to hold one capability in client
 * The class will also ensure the capability is applied
 */
public abstract class Capability{
    protected String value;
    protected String headerName;
    protected boolean isEnabled;
    protected EnumSet<HttpVersion> supportedVersions;
    protected LoggerService logger = BWebServer.getLoggerService();

    public Capability() {
    }
    public Capability(String value) {
        this.value = value;
    }

    /**
     * Returns true if the current connection has this capability
     * @param headerName header name
     * @param headerValue header value
     * @param version http version
     * @return true if the current client request has this capability
     */
    protected boolean isDetected(String headerName, String headerValue, HttpVersion version){
        return this.headerName.equalsIgnoreCase(headerName) &&
                supportedVersions.contains(version);
    }

    /**
     * Capability will apply before the response is sent
     * @param context current context
     */
    public abstract void beforeResponse(HttpContext context);

    /**
     * Capability will apply after the response was sent
     * @param context current context
     */
    public abstract void afterResponse(HttpContext context);

    /**
     * Factory method to get all capabilities for the current client request
     * @param request current http request
     * @return list of capabilities
     */
    public static List<Capability> detectCapabilities(HttpRequest request){
        ArrayList<Capability> capabilities = new ArrayList<>();
        ArrayList<Capability> registeredCapabilities = BWebServer.getServerRegisteredCapabilities();
        for (Capability regCapability : registeredCapabilities ) {
            if (regCapability.isDetected(regCapability.headerName, request.getHeaders().get(regCapability.headerName), request.getVersion())){
                capabilities.add(regCapability);
            }
        }
        return capabilities;
    }

    /**
     * Injects capabilities before the response
     * @param context - decorating the context with the enabled capabilities.
     */
    public static void applyBeforeResponseCapabilities(HttpContext context){
        for(Capability capability : context.getSupportedCapabilities()){
            capability.beforeResponse(context);
        }
    }

    /**
     * Injects capabilities after the response
     * @param context - decorating the context with the enabled capabilities.
     */
    public static void applyAfterResponseCapabilities(HttpContext context){
        for(Capability capability : context.getSupportedCapabilities()){
            capability.afterResponse(context);
        }
    }
}
