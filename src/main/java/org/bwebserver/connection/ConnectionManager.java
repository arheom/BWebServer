package org.bwebserver.connection;

import org.bwebserver.config.ConfigProvider;
import org.bwebserver.config.ConfigService;
import org.bwebserver.control.ControlPlaneProvider;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.heartbeat.HeartBeatProvider;
import org.bwebserver.heartbeat.HeartBeatService;
import org.bwebserver.http.HttpHandler;
import org.bwebserver.http.client.Capability;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Manages the connections received by the server. Will dispatch the connections to
 * their handlers
 */
public class ConnectionManager {
    private ServerSocket serverSocket = null;
    private ServerSocketFactory serverSocketFactory = null;
    private ExecutorService exec = null;

    private static LoggerService logger = LoggerProvider.getInstance().serviceImpl();
    private static ConfigService config = ConfigProvider.getInstance().serviceImpl();
    private static HeartBeatService health = HeartBeatProvider.getInstance().serviceImpl();
    private static ControlPlaneService control = ControlPlaneProvider.getInstance().serviceImpl();

    private ConnectionManager() {

    }

    /**
     * Factory method to initialize the connection manager
     *
     * @return an initialized object for managing connections
     */
    public static ConnectionManager create() throws IOException {
        ConnectionManager manager = new ConnectionManager();
        if (manager.serverSocketFactory == null) {
            manager.serverSocketFactory = ServerSocketFactory.getDefault();
        }
        ServerSocket serverSocket = manager.serverSocketFactory.createServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(config.getPort()));

        manager.serverSocket = serverSocket;
        //manager.exec = Executors.newFixedThreadPool(config.getNThreads());
        manager.exec = Executors.newCachedThreadPool();
        manager.exec = control.decorateExecutorService(manager.exec);
        return manager;
    }

    /**
     * Starts accepting connections from clients
     *
     */
    public void acceptConnections() throws IOException {
        while (!exec.isShutdown() && !serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                // if the server is too busy, reject new connections as fast as possible
                if (health.IsHealthy()) {
                    // create one thread for each connection and pass the socket to it
                    exec.execute(new HttpHandler(socket));
                } else {
                    socket.close();
                    logger.LogWarning("Server too busy - closing new connections.");
                }
            } catch (RejectedExecutionException ex) {
                if (!exec.isShutdown()) {
                    logger.LogError("Executor rejected incoming requests!");
                }
            }
        }
    }

    public void closeConnections() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}
