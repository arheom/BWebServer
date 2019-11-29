package org.bwebserver.http.protocol;

import org.bwebserver.BWebServer;
import org.bwebserver.content.ContentInfo;
import org.bwebserver.content.ContentProvider;
import org.bwebserver.content.ContentService;
import org.bwebserver.control.ControlPlaneProvider;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.http.HttpContext;
import org.bwebserver.http.HttpResponse;
import org.bwebserver.http.client.Policy;

import java.io.FileNotFoundException;
import java.io.IOException;

import static com.ea.async.Async.await;


public class HttpGet {
    private ContentService contentService = BWebServer.getContentService();
    private ControlPlaneService control = BWebServer.getControlPlaneService();

    private HttpContext context;

    private HttpGet(){

    }
    public static HttpGet create(HttpContext context){
        HttpGet get = new HttpGet();
        get.context = context;
        return get;
    }
    public void execute() throws IOException {
        try {
            ContentInfo file = await(contentService.getContent(context.getPath()));
            file = control.decorateContentObject(file);
            context.setContentInfo(file);
            context.getHttpResponse().setResponseCode(200);
            Policy.applyBeforeResponsePolicies(context);
            context.getHttpResponse().writeBody(file.getContentBytes());
        } catch(FileNotFoundException ex){
            HttpResponse.sendError(context.getCurrentConnection(), 404);
        }
    }
}
