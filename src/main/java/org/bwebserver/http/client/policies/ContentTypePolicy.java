package org.bwebserver.http.client.policies;

import org.bwebserver.http.HttpContext;
import org.bwebserver.http.HttpResponse;
import org.bwebserver.http.client.Policy;
import org.bwebserver.http.protocol.HttpMethod;

public class ContentTypePolicy extends Policy {
    @Override
    protected boolean isDetected(HttpContext context) {
        return true;
    }

    @Override
    public void beforeResponse(HttpContext context) {
        if (context.getHttpMethod() == HttpMethod.GET &&
                context.getHttpResponse() != null && context.getHttpResponse().getResponseCode() == 200 &&
            context.getContentInfo() != null){
            // only text/html is supported at the time being
            context.getHttpResponse().addHeader("Content-Type", "text/html");
        }
    }

    @Override
    public void beforeErrorSent(HttpResponse response) {

    }
}
