package org.bwebserver.http.protocol;

import org.bwebserver.content.ContentProvider;
import org.bwebserver.content.ContentService;
import org.bwebserver.control.ControlPlaneProvider;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.http.HttpContext;

import java.io.IOException;

import static com.ea.async.Async.await;


public class HttpPost {
    private ContentService contentService = ContentProvider.getInstance().serviceImpl();
    private ControlPlaneService control = ControlPlaneProvider.getInstance().serviceImpl();

    private HttpContext context;

    private HttpPost(){

    }
    public static HttpPost create(HttpContext context){
        HttpPost post = new HttpPost();
        post.context = context;
        return post;
    }
    public void execute() throws IOException {
        boolean success = await(contentService.createContent(context.getPath(), context.getHttpRequest().getBody()));
        if (!success) {
            context.getHttpResponse().writeBody("", 404);
        } else {
            context.getHttpResponse().addHeader("Content-Type", "text/html");
            context.getHttpResponse().writeBody("", 200);
        }
    }
}
