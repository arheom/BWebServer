package org.bwebserver.content;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public class ContentProvider {

    private static ContentProvider provider;
    private ServiceLoader<ContentService> loader;
    private ContentProvider() {
        loader = ServiceLoader.load(ContentService.class);
    }
    public static ContentProvider getInstance() {
        if(provider == null) {
            provider = new ContentProvider();
        }
        return provider;
    }
    public ContentService serviceImpl() {
        //TODO: extend for more than one service, connected to a control plane
        ContentService service = loader.iterator().next();
        if(service != null) {
            return service;
        } else {
            throw new NoSuchElementException(
                    "No implementation for ContentProvider");
        }
    }
}