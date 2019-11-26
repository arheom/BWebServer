package org.bwebserver.logging;

/**
 * Very basic logger service
 */
public interface LoggerService {
    void LogError(String message);
    void LogWarning(String message);
    void LogInfo(String message);
    void LogDebug(String message);
}
