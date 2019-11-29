package org.bwebserver.http.client.policies;

import org.bwebserver.http.HttpContext;
import org.bwebserver.http.HttpResponse;
import org.bwebserver.http.client.Policy;
import org.bwebserver.http.protocol.HttpMethod;

public class ContentLengthPolicy extends Policy {
    @Override
    protected boolean isDetected(HttpContext context) {
        return true;
    }

    @Override
    public void beforeResponse(HttpContext context) {
        if (context.getHttpResponse() != null && context.getHttpResponse().getResponseCode() == 200){
            if (context.getContentInfo() != null && context.getContentInfo().getContentBytes() != null){
                String contentLength = Integer.toString(context.getContentInfo().getContentBytes().length);
                context.getHttpResponse().addHeader("Content-Length", contentLength);
            }
        }
    }

    @Override
    public void beforeErrorSent(HttpResponse response) {

    }
}
