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
import java.nio.file.FileAlreadyExistsException;

import static com.ea.async.Async.await;


public class HttpPost {
    private ContentService contentService = BWebServer.getContentService();

    private HttpContext context;

    private HttpPost(){

    }
    public static HttpPost create(HttpContext context){
        HttpPost post = new HttpPost();
        post.context = context;
        return post;
    }
    public void execute() throws IOException {
        try {
            await(contentService.createContent(context.getPath(), context.getHttpRequest().getBody()));
            context.getHttpResponse().setResponseCode(200);
            Policy.applyBeforeResponsePolicies(context);
            context.getHttpResponse().writeBody("");
        } catch(FileAlreadyExistsException ex){
            HttpResponse.sendError(context.getCurrentConnection(), 405);
        }
    }
}
