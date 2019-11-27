package org.bwebserver.http.client.capabilities;

import org.bwebserver.http.HttpContext;
import org.bwebserver.http.client.Capability;
import org.bwebserver.http.protocol.HttpVersion;

import java.util.EnumSet;

/**
 * Keep Alive capability. Will detect when it is needed and apply it to the context.
 */
public class KeepAliveCapability extends Capability {
    KeepAliveCapability(String value) {
        super(value);
        init();
    }

    public KeepAliveCapability() {
        super();
        init();
    }

    private void init(){
        supportedVersions = EnumSet.of(HttpVersion.HTTP1_0, HttpVersion.HTTP1_1);
        headerName = "connection";
    }

    @Override
    protected boolean isDetected(String headerName, String headerValue, HttpVersion version){
        return super.isDetected(headerName, headerValue,version)
                && headerValue.equalsIgnoreCase("keep-alive");
    }

    @Override
    public void beforeResponse(HttpContext context) {
        context.setPersistentConnection(true);
    }

    @Override
    public void afterResponse(HttpContext context) {

    }
}
