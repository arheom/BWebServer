package org.bwebserver;

import org.bwebserver.config.ConfigProvider;
import org.bwebserver.config.ConfigService;
import org.bwebserver.connection.ConnectionManager;
import org.bwebserver.content.ContentProvider;
import org.bwebserver.content.ContentService;
import org.bwebserver.control.ControlPlaneProvider;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.heartbeat.HeartBeatProvider;
import org.bwebserver.heartbeat.HeartBeatService;
import org.bwebserver.http.client.Capability;
import org.bwebserver.http.client.CapabilityProvider;
import org.bwebserver.http.client.Policy;
import org.bwebserver.http.client.PolicyProvider;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import java.io.IOException;
import java.util.ArrayList;

public class BWebServer {
    private static BWebServer server = null;

    private boolean isRunning = false;
    private static LoggerService logger = LoggerProvider.getInstance().serviceImpl();
    private static ArrayList<Capability> serverRegisteredCapabilities = CapabilityProvider.getInstance().getAllRegisteredCapabilities();
    private static ArrayList<Policy> serverRegisteredPolicies = PolicyProvider.getInstance().getAllRegisteredPolicies();
    private static ConfigService config = ConfigProvider.getInstance().serviceImpl();
    private static HeartBeatService health = HeartBeatProvider.getInstance().serviceImpl();
    private static ControlPlaneService control = ControlPlaneProvider.getInstance().serviceImpl();
    private static ContentService contentService = ContentProvider.getInstance().serviceImpl();
    private static ConnectionManager connectionManager = null;

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

    public static ArrayList<Capability> getServerRegisteredCapabilities(){
        return serverRegisteredCapabilities;
    }

    public static ContentService getContentService() {
        return contentService;
    }

    public static ArrayList<Policy> getServerRegisteredPolicies() {
        return serverRegisteredPolicies;
    }

    private void start() throws IOException {
        isRunning = true;
        logger.LogInfo("BWebServer starting...");
        connectionManager.acceptConnections();
    }

    public static ControlPlaneService getControlPlaneService() {
        return control;
    }

    public static HeartBeatService getHealthService() {
        return health;
    }

    public static ConfigService getConfigService() {
        return config;
    }

    public static LoggerService getLoggerService() {
        return logger;
    }
}
