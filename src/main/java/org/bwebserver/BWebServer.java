package org.bwebserver;

import org.bwebserver.connection.ConnectionManager;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import java.io.IOException;

public class BWebServer {
    private static BWebServer server = null;

    private boolean isRunning = false;
    private LoggerService logger = LoggerProvider.getInstance().serviceImpl();
    private ConnectionManager connectionManager = null;

    public BWebServer(){

    }

    static void startServer() throws Exception {
        if (server != null && server.isRunning){
            // proper error management needs to be done
            throw new Exception("BWebServer is already running!");
        }

        server = new BWebServer();
        server.connectionManager = ConnectionManager.create();
        server.start();
    }

    static void stopServer() throws Exception {
        if (server != null && server.isRunning){
            server.connectionManager.closeConnections();
        }
    }

    private void start() throws IOException {
        isRunning = true;
        logger.LogInfo("BWebServer starting...");
        connectionManager.acceptConnections();
    }
}
