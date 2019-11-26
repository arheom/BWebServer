package org.bwebserver.http.protocol;

import org.bwebserver.content.ContentInfo;
import org.bwebserver.content.ContentProvider;
import org.bwebserver.content.ContentService;
import org.bwebserver.control.ControlPlaneProvider;
import org.bwebserver.control.ControlPlaneService;
import org.bwebserver.http.HttpContext;

import java.io.IOException;

import static com.ea.async.Async.await;


public class HttpGet {
    private ContentService contentService = ContentProvider.getInstance().serviceImpl();
    private ControlPlaneService control = ControlPlaneProvider.getInstance().serviceImpl();

    private HttpContext context;

    private HttpGet(){

    }
    public static HttpGet create(HttpContext context){
        HttpGet get = new HttpGet();
        get.context = context;
        return get;
    }
    public void execute() throws IOException {
        ContentInfo file = await(contentService.getContent(context.getPath()));
        file = control.decorateContentObject(file);
        if (file == null) {
            context.getHttpResponse().writeBody("", 404);
        } else {
            context.getHttpResponse().addHeader("Content-Type", "text/html");
            context.getHttpResponse().writeBody(file.getContentBytes(), 200);
        }
    }
}
