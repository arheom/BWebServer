package org.bwebserver.http.protocol;

import org.bwebserver.BWebServer;
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


public class HttpDelete {
    private ContentService contentService = BWebServer.getContentService();

    private HttpContext context;

    private HttpDelete(){

    }
    public static HttpDelete create(HttpContext context){
        HttpDelete delete = new HttpDelete();
        delete.context = context;
        return delete;
    }
    public void execute() throws IOException {
        try {
            await(contentService.deleteContent(context.getPath()));
            context.getHttpResponse().setResponseCode(200);
            Policy.applyBeforeResponsePolicies(context);
            context.getHttpResponse().writeBody("");
        } catch(FileNotFoundException ex){
            HttpResponse.sendError(context.getCurrentConnection(), 404);
        }
    }
}
