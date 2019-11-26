package org.bwebserver.content.impl;

import static java.util.concurrent.CompletableFuture.completedFuture;
import org.apache.commons.lang3.time.StopWatch;
import org.bwebserver.config.ConfigProvider;
import org.bwebserver.config.ConfigService;
import org.bwebserver.content.ContentInfo;
import org.bwebserver.content.ContentService;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import java.io.*;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * File content implementation of the Content Service
 */
public class FileContentService implements ContentService {

    private ConfigService config = ConfigProvider.getInstance().serviceImpl();
    private LoggerService logger = LoggerProvider.getInstance().serviceImpl();

    private String rootFolderPath;

    // counts the total disk time - statistics only
    private static volatile long totalDiskTime = 0;
    // maintains a readwrite lock for each content item, so multiple writers cannot use same resource
    private static Hashtable<String, ReentrantReadWriteLock> fileLock;

    public FileContentService(){
        rootFolderPath = config.getContentRootPath();
        fileLock = new Hashtable<>();
    }

    @Override
    public CompletableFuture<ContentInfo> getContent(String path) {
        // timer to count the time of accessing the content
        StopWatch timerContent = new StopWatch();
        timerContent.start();
        ContentInfo doc = ContentInfo.createEmpty();
        try {
            acquireReadLock(path);
            File file = getFileReference(path);
            if (file == null || !file.exists()) {
                // return empty object without value if file does not exist
                return CompletableFuture.completedFuture(doc);
            }
            // reading the file
            BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[(int) file.length()];
            br.read(buf);
            br.close();
            doc = new ContentInfo(file.getName(), buf);
            doc.setHasValue(true);
            releaseReadLock(path);
        }catch(Exception ex){
            logger.LogError(ex.toString());
        }
        timerContent.stop();
        logger.LogInfo(String.format("Content for request %s was completed in %d, total disk time %d!", path, timerContent.getTime(), totalDiskTime));
        totalDiskTime += timerContent.getTime();
        return CompletableFuture.completedFuture(doc);
    }

    @Override
    public CompletableFuture<Boolean> putContent(String path, byte[] body){
        // timer to count the time of accessing the content
        StopWatch timerContent = new StopWatch();
        timerContent.start();
        try {
            aquireWriteLock(path);
            File file = getFileReference(path);
            if (file == null || !file.exists()) {
                return completedFuture(false);
            }
            BufferedOutputStream br = new BufferedOutputStream(new FileOutputStream(file));
            br.write(body);
            br.close();
            releaseWriteLock(path);
        } catch (Exception ex){
            logger.LogError(ex.toString());
            return completedFuture(false);
        }
        timerContent.stop();
        logger.LogInfo(String.format("Update for request %s was completed in %d!", path, timerContent.getTime()));
        return completedFuture(true);
    }


    @Override
    public CompletableFuture<Boolean> createContent(String path, byte[] body){
        // timer to count the time of accessing the content
        StopWatch timerContent = new StopWatch();
        timerContent.start();
        try {
            aquireWriteLock(path);
            File file = getFileReference(path);
            if (file == null || file.exists()) {
                return completedFuture(false);
            }

            BufferedOutputStream br = new BufferedOutputStream(new FileOutputStream(file));
            br.write(body);
            br.close();
            releaseWriteLock(path);
        } catch (Exception ex){
            logger.LogError(ex.toString());
            return completedFuture(false);
        }
        timerContent.stop();
        logger.LogInfo(String.format("Create for request %s was completed in %d!", path, timerContent.getTime()));
        return completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> deleteContent(String path){
        // timer to count the time of accessing the content
        StopWatch timerContent = new StopWatch();
        timerContent.start();
        boolean deleted;
        try {
            aquireWriteLock(path);
            File file = getFileReference(path);
            if (file == null || !file.exists()) {
                return completedFuture(false);
            }
            deleted = file.delete();

            releaseWriteLock(path);
        } catch (Exception ex){
            logger.LogError(ex.toString());
            return completedFuture(false);
        }
        timerContent.stop();
        logger.LogInfo(String.format("Delete for request %s was completed in %d!", path, timerContent.getTime()));
        return completedFuture(deleted);
    }

    private File getFileReference(String path) {
        if (!SecurityValidator.isUserInputSafe(path)){
            // for any unsafe request -> 404
            logger.LogWarning(String.format("Security Validation failed for path %s", path));
            return null;
        }

        return getFile(path);
    }

    private void acquireReadLock(String path){
        if (!fileLock.containsKey(path)){
            // create new lock
            synchronized (fileLock) {
                fileLock.put(path, new ReentrantReadWriteLock());
            }
        }
        fileLock.get(path).readLock().lock();
    }

    private void releaseReadLock(String path){
        fileLock.get(path).readLock().unlock();
    }

    private void aquireWriteLock(String path){
        if (!fileLock.containsKey(path)){
            // initialize the lock
            synchronized (fileLock) {
                fileLock.put(path, new ReentrantReadWriteLock());
            }
        }
        fileLock.get(path).writeLock().lock();
    }

    private void releaseWriteLock(String path){
        fileLock.get(path).writeLock().unlock();
    }

    private File getFile(String path){
        return new File(String.format("%s\\%s", rootFolderPath, path));
    }
}
