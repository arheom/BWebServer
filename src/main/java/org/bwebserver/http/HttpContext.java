package org.bwebserver.http;

import org.bwebserver.http.client.Capability;
import org.bwebserver.http.protocol.HttpMethod;
import org.bwebserver.http.protocol.HttpVersion;

import java.net.Socket;
import java.util.List;

/**
 * Context of the current http request
 */
public class HttpContext {
    private Socket currentConnection;
    private boolean persistentConnection = false;
    private HttpVersion httpVersion = null;
    private HttpMethod httpMethod = null;
    private String path = null;

    private HttpRequest httpRequest = null;
    private HttpResponse httpResponse = null;
    private List<Capability> supportedCapabilities;

    HttpContext(){

    }

    /**
     * Factory method to create the context from request and response
     * @param req - the current request of the context
     * @param res - the current response of the context
     * @return httpcontext for the current request
     */
    public static HttpContext create(HttpRequest req, HttpResponse res, Socket socket){
        HttpContext context = new HttpContext();
        context.httpMethod = req.getMethod();
        context.httpVersion = req.getVersion();
        context.path = req.getPath();
        context.httpRequest = req;
        context.httpResponse = res;
        context.supportedCapabilities = Capability.detectCapabilities(req);
        context.currentConnection = socket;
        return context;
    }

    public Socket getCurrentConnection() {
        return currentConnection;
    }

    public boolean getPersistentConnection() {
        return persistentConnection;
    }

    public void setPersistentConnection(boolean value) {
        persistentConnection = value;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public String getPath() {
        return path;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public List<Capability> getSupportedCapabilities() {
        return supportedCapabilities;
    }
}
