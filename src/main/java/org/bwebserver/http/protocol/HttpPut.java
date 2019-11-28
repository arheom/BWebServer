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
import java.nio.file.FileAlreadyExistsException;

import static com.ea.async.Async.await;


public class HttpPut {
    private ContentService contentService = BWebServer.getContentService();;

    private HttpContext context;

    private HttpPut(){

    }
    public static HttpPut create(HttpContext context){
        HttpPut put = new HttpPut();
        put.context = context;
        return put;
    }
    public void execute() throws IOException {
        try {
            await(contentService.putContent(context.getPath(), context.getHttpRequest().getBody()));
            context.getHttpResponse().addHeader("Content-Type", "text/html");
            context.getHttpResponse().writeBody("", 200);
        } catch(FileNotFoundException ex){
            HttpResponse.sendError(context.getCurrentConnection(), 404);
        }
    }
}
