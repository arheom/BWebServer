package org.bwebserver.http.client.policies;

import org.bwebserver.http.HttpContext;
import org.bwebserver.http.HttpResponse;
import org.bwebserver.http.client.Policy;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class DatePolicy extends Policy {
    @Override
    protected boolean isDetected(HttpContext context) {
        return true;
    }

    @Override
    public void beforeResponse(HttpContext context) {
        String dateUtc = ZonedDateTime.now( ZoneOffset.UTC ).withNano( 0 ).toString();
        context.getHttpResponse().addHeader("Date", dateUtc);
    }

    @Override
    public void beforeErrorSent(HttpResponse response) {
        String dateUtc = ZonedDateTime.now( ZoneOffset.UTC ).withNano( 0 ).toString();
        response.addHeader("Date", dateUtc);
    }
}
