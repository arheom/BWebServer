package org.bwebserver.http;

import org.bwebserver.http.protocol.HttpMethod;
import org.bwebserver.http.protocol.HttpVersion;

/**
 * Context of the current http request
 */
public class HttpContext {
    private boolean keepAlive = true;
    private boolean closeConnection = false;
    private HttpVersion httpVersion = null;
    private HttpMethod httpMethod = null;
    private String path = null;

    private HttpRequest httpRequest = null;
    private HttpResponse httpResponse = null;

    HttpContext(){

    }

    /**
     * Factory method to create the context from request and response
     * @param req - the current request of the context
     * @param res - the current response of the context
     * @return httpcontext for the current request
     */
    public static HttpContext create(HttpRequest req, HttpResponse res){
        HttpContext context = new HttpContext();
        context.httpMethod = req.getMethod();
        context.httpVersion = req.getVersion();
        context.path = req.getPath();
        // extract context info from the headers
        if (req.getHeaders().get("connection") != null){
            context.keepAlive = req.getHeaders().get("connection").toString().equalsIgnoreCase("keep-alive");
            context.closeConnection = req.getHeaders().get("connection").toString().equalsIgnoreCase("close");
        }
        context.httpRequest = req;
        context.httpResponse = res;
        return context;
    }

    boolean getCloseConnection() {
        return closeConnection;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    HttpMethod getHttpMethod() {
        return httpMethod;
    }

    HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public String getPath() {
        return path;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }
}
