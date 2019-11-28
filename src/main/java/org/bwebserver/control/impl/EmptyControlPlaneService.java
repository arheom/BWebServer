package org.bwebserver.control.impl;

import org.bwebserver.BWebServer;
import org.bwebserver.content.ContentInfo;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.http.HttpContext;
import org.bwebserver.logging.LogLevel;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import java.net.Socket;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;

/**
 * This is an empty control plane implementation, so when it is necessary in production to change
 * any of the objects decorated, this can be done inside this service as a hotfix, without
 * having to change the whole project
 */
public class EmptyControlPlaneService implements ControlPlaneService {

    private LoggerService logger = BWebServer.getLoggerService();

    @Override
    public ExecutorService decorateExecutorService(ExecutorService exec) {
        logger.LogDebug("EmptyControlPlaneService: decorating ExecutorService if needed");
        return exec;
    }

    @Override
    public Socket decorateSocketConnection(Socket connection) {
        logger.LogDebug("EmptyControlPlaneService: decorating Socket if needed");
        return connection;
    }

    @Override
    public HttpContext decorateHttpContext(HttpContext context) {
        logger.LogDebug("EmptyControlPlaneService: decorating HttpContext if needed");
        return context;
    }

    @Override
    public ContentInfo decorateContentObject(ContentInfo content) {
        logger.LogDebug("EmptyControlPlaneService: decorating ContentInfo if needed");
        return content;
    }
}