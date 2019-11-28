package org.bwebserver.http.protocol;

import org.bwebserver.BWebServer;
import org.bwebserver.content.ContentInfo;
import org.bwebserver.content.ContentProvider;
import org.bwebserver.content.ContentService;
import org.bwebserver.control.ControlPlaneProvider;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.http.HttpContext;
import org.bwebserver.http.HttpResponse;

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
            //TODO: implement the correct content type functionality
            context.getHttpResponse().addHeader("Content-Type", "text/html");
            context.getHttpResponse().writeBody(file.getContentBytes(), 200);
        } catch(FileNotFoundException ex){
            HttpResponse.sendError(context.getCurrentConnection(), 404);
        }
    }
}
