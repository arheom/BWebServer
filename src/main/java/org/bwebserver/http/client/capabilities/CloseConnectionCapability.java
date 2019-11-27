package org.bwebserver.http.client.capabilities;

import org.apache.commons.lang3.time.StopWatch;
import org.bwebserver.http.HttpContext;
import org.bwebserver.http.client.Capability;
import org.bwebserver.http.protocol.HttpVersion;

import java.io.IOException;
import java.net.Socket;
import java.util.EnumSet;

/**
 * Close connection capabilities. Will detect when this is needed and apply it in the current context
 */
public class CloseConnectionCapability extends Capability {
    CloseConnectionCapability(String value) {
        super(value);
        init();
    }

    public CloseConnectionCapability() {
        super();
        init();
    }

    private void init(){
        supportedVersions = EnumSet.of(HttpVersion.HTTP1_0, HttpVersion.HTTP1_1);
        headerName = "connection";
    }

    @Override
    protected boolean isDetected(String headerName, String headerValue, HttpVersion version){
        boolean generalCondition = super.isDetected(headerName, headerValue, version);
        boolean specificCondition = headerValue.equalsIgnoreCase("close") ||
                (version == HttpVersion.HTTP1_0 && !headerValue.equalsIgnoreCase("keep-alive"));
        return generalCondition && specificCondition;
    }

    @Override
    public void beforeResponse(HttpContext context) {

    }

    @Override
    public void afterResponse(HttpContext context){
        try {
            Socket socket = context.getCurrentConnection();
            if (!socket.isClosed()) {
                socket.shutdownOutput();
                StopWatch timer = new StopWatch();
                timer.start();
                while ((socket.getInputStream().read() != -1) && (timer.getTime() < 100)) {
                    // waiting for the socket to finish
                }
                timer.stop();
                socket.close();
            }
        } catch (IOException e) {
            logger.LogError(String.format("CloseConnectionCapability cannot close the connections: %s", e.toString()));
        }
    }
}
