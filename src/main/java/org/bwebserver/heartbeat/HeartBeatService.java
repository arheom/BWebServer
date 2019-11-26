package org.bwebserver.heartbeat;

/**
 * Very basic service to check the health of the current system
 */
public interface HeartBeatService {
    boolean IsHealthy();
}
