package org.bwebserver.control;

import org.bwebserver.content.ContentInfo;
import org.bwebserver.http.HttpContext;

import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * This is an empty control plane implementation, so when it is necessary in production to change
 * any of the objects decorated asap, this can be done inside this service as a hotfix, without
 * having to change the whole project. Useful to troubleshoot production issues as well.
 */
public interface ControlPlaneService {
    ExecutorService decorateExecutorService(ExecutorService exec);
    Socket decorateSocketConnection(Socket connection);
    HttpContext decorateHttpContext(HttpContext context);
    ContentInfo decorateContentObject(ContentInfo content);
}
