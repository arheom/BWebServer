package org.bwebserver.http;

import static com.ea.async.Async.await;
import static org.bwebserver.http.protocol.HttpMethod.*;

import org.apache.commons.lang3.time.StopWatch;
import org.bwebserver.config.ConfigProvider;
import org.bwebserver.config.ConfigService;
import org.bwebserver.content.ContentInfo;
import org.bwebserver.content.ContentProvider;
import org.bwebserver.content.ContentService;
import org.bwebserver.control.ControlPlaneProvider;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.heartbeat.HeartBeatProvider;
import org.bwebserver.heartbeat.HeartBeatService;
import org.bwebserver.http.protocol.*;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import java.io.*;
import java.net.Socket;
import java.util.EnumSet;

/**
 * Handler of a request. Gets a socket and handles it as an http request
 */
public class HttpHandler implements Runnable {
    private Socket socket;
    private volatile int timeout;

    private LoggerService logger = LoggerProvider.getInstance().serviceImpl();
    private ContentService contentService = ContentProvider.getInstance().serviceImpl();
    private ConfigService config = ConfigProvider.getInstance().serviceImpl();
    private HeartBeatService health = HeartBeatProvider.getInstance().serviceImpl();
    private ControlPlaneService control = ControlPlaneProvider.getInstance().serviceImpl();

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

            boolean keepAlive = true;
            while(keepAlive) {
                if (isServerTooBusy(in, out))
                    break;
                // timer to measure the current request - statistical info only
                StopWatch timerRequest = new StopWatch();
                timerRequest.start();

                // create request and response objects
                HttpRequest httpRequest = HttpRequest.create(in);
                HttpResponse httpResponse = HttpResponse.create(out);

                // create context object for the current request
                context = HttpContext.create(httpRequest, httpResponse);
                context = control.decorateHttpContext(context);

                // handle the current request
                handleRequest(context);

                if (context.getCloseConnection()) {
                    // if current request is marked to close the connection
                    in.close();
                    out.close();
                    socket.close();
                    keepAlive = false;
                }
                timerRequest.stop();
                logger.LogInfo(String.format("Request for %s was handled in %d.", context.getPath(), timerRequest.getTime()));
            }
        } catch (Exception ex) {
            logger.LogError(String.format("Path: %s Error handling the request: %s", context.getPath(), ex.toString()));
            try {
                socket.close();
            } catch (IOException e) {
                logger.LogError(String.format("Path: %s Error closing the socket: %s", context.getPath(), ex.toString()));
            }
        }
    }

    private boolean isServerTooBusy(InputStream in, OutputStream out) throws IOException {
        if (!health.IsHealthy()){
            logger.LogInfo("Server too busy");
            HttpResponse.sendBusy(out);
            in.close();
            out.close();
            socket.close();
            return true;
        }
        return false;
    }

    private void handleRequest(HttpContext context) throws IOException {
        if (!isCurrentRequestSupported(context)) {
            context.getHttpResponse().writeBody("", 501);
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
