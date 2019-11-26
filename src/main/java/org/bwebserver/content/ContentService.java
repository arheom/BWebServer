package org.bwebserver.content;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface ContentService {
    CompletableFuture<ContentInfo> getContent(String path) throws IOException;
    CompletableFuture<Boolean> putContent(String path, byte[] body) throws IOException;
    CompletableFuture<Boolean> createContent(String path, byte[] body) throws IOException;
    CompletableFuture<Boolean> deleteContent(String path) throws IOException;
}
