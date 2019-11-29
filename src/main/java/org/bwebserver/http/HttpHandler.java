package org.bwebserver.http;

import static org.bwebserver.http.protocol.HttpMethod.*;

import org.apache.commons.lang3.time.StopWatch;
import org.bwebserver.BWebServer;
import org.bwebserver.config.ConfigProvider;
import org.bwebserver.config.ConfigService;
import org.bwebserver.content.ContentProvider;
import org.bwebserver.content.ContentService;
import org.bwebserver.control.ControlPlaneProvider;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.heartbeat.HeartBeatProvider;
import org.bwebserver.heartbeat.HeartBeatService;
import org.bwebserver.http.client.Capability;
import org.bwebserver.http.protocol.*;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.EnumSet;

/**
 * Handler of a request. Gets a socket and handles it as an http request
 */
public class HttpHandler implements Runnable {
    private Socket socket;
    private volatile int timeout;

    private ConfigService config = BWebServer.getConfigService();
    private LoggerService logger = BWebServer.getLoggerService();
    private ControlPlaneService control = BWebServer.getControlPlaneService();
    private HeartBeatService health = BWebServer.getHealthService();

    public HttpHandler(Socket socket) {
        this.socket = socket;
        timeout = config.getConnectionTimeout();
    }

    @Override
    public void run() {
        HttpContext context = new HttpContext();
        try {
            logger.LogInfo("Starting new HTTP Connection");
            socket.setSoTimeout(timeout);
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket = control.decorateSocketConnection(socket);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            do {
                if (isServerTooBusy(socket))
                    break;
                // timer to measure the current request - statistical info only
                StopWatch timerRequest = new StopWatch();
                timerRequest.start();

                // create request and response objects
                HttpRequest httpRequest;
                try {
                    httpRequest = HttpRequest.create(in);
                } catch (IOException e) {
                    throw e;
                } catch (Exception ex){
                    logger.LogError(String.format("Path: %s Error processing the request information: %s", context.getPath(), ex.toString()));
                    HttpResponse.sendError(socket, 400);
                    throw ex;
                }
                HttpResponse httpResponse = HttpResponse.create(out);

                // create context object for the current request
                context = HttpContext.create(httpRequest, httpResponse, socket);
                context = control.decorateHttpContext(context);

                Capability.applyBeforeResponseCapabilities(context);
                // handle the current request
                handleRequest(context);
                Capability.applyAfterResponseCapabilities(context);

                timerRequest.stop();
                logger.LogInfo(String.format("Request for %s was handled in %d.", context.getPath(), timerRequest.getTime()));
            } while (context.getPersistentConnection() && !socket.isClosed());
        } catch (SocketTimeoutException e) {
            logger.LogInfo(String.format("Path: %s was closed due to timeout", context.getPath()));
            HttpContext.silentCloseConnection(socket);
        } catch (IOException e) {
            logger.LogInfo(String.format("Path: %s was closed due to IO error. %s", context.getPath(), e.getMessage()));
        } catch (Exception ex) {
            logger.LogError(String.format("Path: %s Error handling the request: %s", context.getPath(), ex.getMessage()));
            try {
                if (!socket.isClosed()) {
                    HttpResponse.sendError(socket, 500);
                    HttpContext.closeConnection(socket);
                }
            } catch (IOException e) {
                logger.LogError(String.format("Path: %s Error closing the socket: %s", context.getPath(), ex.getMessage()));
            }
        }
    }

    private boolean isServerTooBusy(Socket socket) throws IOException {
        if (!health.IsHealthy()){
            logger.LogInfo("Server too busy");
            HttpResponse.sendBusy(socket);
            return true;
        }
        return false;
    }

    private void handleRequest(HttpContext context) throws IOException {
        if (!isCurrentRequestSupported(context)) {
            context.getHttpResponse().setResponseCode(501);
            context.getHttpResponse().writeBody("");
        } else{
            switch(context.getHttpMethod()){
                case GET:
                    HttpGet.create(context).execute();
                    break;
                case PUT:
                    HttpPut.create(context).execute();
                    break;
                case POST:
                    HttpPost.create(context).execute();
                    break;
                case DELETE:
                    HttpDelete.create(context).execute();
                    break;
            }

        }
    }

    private boolean isCurrentRequestSupported(HttpContext context){
        boolean supportedMethods = context.getHttpMethod() != UNSUPPORTED;
        boolean supportedVersion = context.getHttpVersion() != HttpVersion.UNSUPPORTED;
        return supportedMethods && supportedVersion;
    }
}
