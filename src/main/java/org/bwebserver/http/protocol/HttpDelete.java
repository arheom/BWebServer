package org.bwebserver.http.protocol;

import org.bwebserver.content.ContentProvider;
import org.bwebserver.content.ContentService;
import org.bwebserver.control.ControlPlaneProvider;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.http.HttpContext;

import java.io.IOException;

import static com.ea.async.Async.await;


public class HttpDelete {
    private ContentService contentService = ContentProvider.getInstance().serviceImpl();
    private ControlPlaneService control = ControlPlaneProvider.getInstance().serviceImpl();

    private HttpContext context;

    private HttpDelete(){

    }
    public static HttpDelete create(HttpContext context){
        HttpDelete delete = new HttpDelete();
        delete.context = context;
        return delete;
    }
    public void execute() throws IOException {
        boolean success = await(contentService.deleteContent(context.getPath()));
        if (!success) {
            context.getHttpResponse().writeBody("", 400);
        } else {
            context.getHttpResponse().addHeader("Content-Type", "text/html");
            context.getHttpResponse().writeBody("", 200);
        }
    }
}
