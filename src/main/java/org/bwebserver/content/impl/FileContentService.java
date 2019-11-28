package org.bwebserver.content.impl;

import static java.util.concurrent.CompletableFuture.completedFuture;
import org.apache.commons.lang3.time.StopWatch;
import org.bwebserver.BWebServer;
import org.bwebserver.config.ConfigProvider;
import org.bwebserver.config.ConfigService;
import org.bwebserver.content.ContentInfo;
import org.bwebserver.content.ContentService;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * File content implementation of the Content Service
 */
public class FileContentService implements ContentService {

    private ConfigService config = BWebServer.getConfigService();
    private LoggerService logger = BWebServer.getLoggerService();

    private String rootFolderPath;
    private String pageTemplate = TemplateService.getInstrance().getTemplate("page-display-template");
    private String folderTemplate = TemplateService.getInstrance().getTemplate("folder-display-template");
    private String fileTemplate = TemplateService.getInstrance().getTemplate("file-display-template");

    // counts the total disk time - statistics only
    private static volatile long totalDiskTime = 0;
    // maintains a readwrite lock for each content item, so multiple writers cannot use same resource
    private static Hashtable<String, ReentrantReadWriteLock> fileLock;
    private static final Object fileLocker = new Object();

    public FileContentService() throws IOException {
        rootFolderPath = config.getContentRootPath();
        fileLock = new Hashtable<>();
    }

    @Override
    public CompletableFuture<ContentInfo> getContent(String path) throws IOException {
        // timer to count the time of accessing the content
        StopWatch timerContent = new StopWatch();
        timerContent.start();
        ContentInfo doc = ContentInfo.createEmpty();
        try {
            acquireReadLock(path);
            File file = getFileReference(path);
            if (file == null || !file.exists()) {
                // return empty object without value if file does not exist
                throw new FileNotFoundException(String.format("The current request %s does not contain any content associated with.", path));
            }
            if (file.isDirectory()){
                // list the current files and directories
                doc = new ContentInfo(file.getName(), getDocumentListingHTML(file));
                doc.setHasValue(true);
            } else {
                // reading the file
                BufferedInputStream br = new BufferedInputStream(new FileInputStream(file));
                byte[] buf = new byte[(int) file.length()];
                br.read(buf);
                br.close();
                doc = new ContentInfo(file.getName(), buf);
                doc.setHasValue(true);
            }

        }
        catch(FileNotFoundException ex){
            logger.LogInfo(ex.getMessage());
            throw ex;
        }
        catch(IOException ex){
            logger.LogError(ex.getMessage());
            throw ex;
        } finally {
            releaseReadLock(path);
        }
        timerContent.stop();
        logger.LogInfo(String.format("Content for request %s was completed in %d, total disk time %d!", path, timerContent.getTime(), totalDiskTime));
        totalDiskTime += timerContent.getTime();
        return CompletableFuture.completedFuture(doc);
    }

    @Override
    public CompletableFuture<Boolean> putContent(String path, byte[] body) throws IOException {
        // timer to count the time of accessing the content
        StopWatch timerContent = new StopWatch();
        timerContent.start();
        try {
            aquireWriteLock(path);
            File file = getFileReference(path);
            if (file == null || !file.exists() || file.isDirectory()) {
                throw new FileNotFoundException(String.format("The current request %s does not contain any content associated with.", path));
            }
            BufferedOutputStream br = new BufferedOutputStream(new FileOutputStream(file));
            br.write(body);
            br.close();
        } catch(FileNotFoundException ex){
            logger.LogInfo(ex.getMessage());
            throw ex;
        }
        catch (IOException ex){
            logger.LogError(ex.getMessage());
            throw ex;
        } finally {
            releaseWriteLock(path);
        }
        timerContent.stop();
        logger.LogInfo(String.format("Update for request %s was completed in %d!", path, timerContent.getTime()));
        return completedFuture(true);
    }


    @Override
    public CompletableFuture<Boolean> createContent(String path, byte[] body) throws IOException {
        // timer to count the time of accessing the content
        StopWatch timerContent = new StopWatch();
        timerContent.start();
        try {
            aquireWriteLock(path);
            File file = getFileReference(path);
            if (file == null || file.exists() || file.isDirectory()) {
                throw new FileAlreadyExistsException(String.format("The current request %s already contains content.", path));
            }

            BufferedOutputStream br = new BufferedOutputStream(new FileOutputStream(file));
            br.write(body);
            br.close();
        } catch(FileAlreadyExistsException ex){
            logger.LogInfo(ex.getMessage());
            throw ex;
        }
        catch (IOException ex){
            logger.LogError(ex.getMessage());
            throw ex;
        } finally {
            releaseWriteLock(path);
        }
        timerContent.stop();
        logger.LogInfo(String.format("Create for request %s was completed in %d!", path, timerContent.getTime()));
        return completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> deleteContent(String path) throws IOException {
        // timer to count the time of accessing the content
        StopWatch timerContent = new StopWatch();
        timerContent.start();
        boolean deleted;
        try {
            aquireWriteLock(path);
            File file = getFileReference(path);
            if (file == null || !file.exists() || file.isDirectory()) {
                throw new FileNotFoundException(String.format("The current request %s does not contain any content associated with.", path));
            }
            if (!file.delete()){
                throw new IOException(String.format("The current request %s cannot be deleted.", path));
            }
        } catch(FileNotFoundException ex){
            logger.LogInfo(ex.getMessage());
            throw ex;
        }
        catch (IOException ex){
            logger.LogError(ex.getMessage());
            throw ex;
        } finally {
            releaseWriteLock(path);
        }
        timerContent.stop();
        logger.LogInfo(String.format("Delete for request %s was completed in %d!", path, timerContent.getTime()));
        return completedFuture(true);
    }

    private File getFileReference(String path) {
        if (!SecurityValidator.isUserInputSafe(path)){
            // for any unsafe request -> 404
            logger.LogWarning(String.format("Security Validation failed for path %s", path));
            return null;
        }
        // for both windows and linux
        String universalPath = path.replace("\\", "/");
        return getFile(universalPath);
    }

    private void acquireReadLock(String path){
        if (!fileLock.containsKey(path)){
            // create new lock
            synchronized (fileLocker) {
                if (!fileLock.containsKey(path)) {
                    fileLock.put(path, new ReentrantReadWriteLock());
                }
            }
        }
        fileLock.get(path).readLock().lock();
        //logger.LogError(String.format("ReadLock: locked: %s", path));
    }

    private void releaseReadLock(String path){
        if (fileLock.containsKey(path)) {
            fileLock.get(path).readLock().unlock();
            //logger.LogError(String.format("ReadLock: unlocked: %s", path));
        }
    }

    private void aquireWriteLock(String path){
        if (!fileLock.containsKey(path)){
            // initialize the lock
            synchronized (fileLocker) {
                if (!fileLock.containsKey(path)) {
                    fileLock.put(path, new ReentrantReadWriteLock());
                }
            }
        }
        fileLock.get(path).writeLock().lock();
        //logger.LogError(String.format("WriteLock: locked: %s", path));
    }

    private void releaseWriteLock(String path){
        if (fileLock.containsKey(path)) {
            fileLock.get(path).writeLock().unlock();
            //logger.LogError(String.format("WriteLock: unlocked: %s", path));
        }
    }

    private File getFile(String path){
        return new File(String.format("%s%s%s", rootFolderPath, File.separator, path));
    }

    /**
     * gets the current folder as html
     * @param folder current folder
     * @return bytes of the html to be rendered
     */
    private byte[] getDocumentListingHTML(File folder) {
        StringBuilder files = new StringBuilder();
        for(File file : folder.listFiles()){
            files.append(renderTemplateFiles(file));
        }
        return pageTemplate.replace("{MainFolder}", folder.getName()).replace("{renderFiles}", files.toString()).getBytes();
    }

    /**
     * Renders current file or folder. Works recursive and renders HTML of a tree of nodes
     * @param file - current file or folder
     * @return curren html of the current node
     */
    private String renderTemplateFiles(File file) {
        if (file.isDirectory()){
            StringBuilder render = new StringBuilder();
            for(File f : file.listFiles()){
                render.append(renderTemplateFiles(f));
            }
            return folderTemplate.replace("{folderName}", file.getName()).replace("{files}", render.toString());
        } else {
            return fileTemplate.replace("{fileName}", file.getName()).replace("{fileUrl}", getUrlFromPath(file.getPath()));
        }
    }

    private String getUrlFromPath(String path){
        return path.replace(rootFolderPath, "").replace("\\", "/");
    }
}
