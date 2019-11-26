package org.bwebserver.heartbeat.impl;

import org.bwebserver.config.ConfigProvider;
import org.bwebserver.config.ConfigService;
import org.bwebserver.heartbeat.HeartBeatService;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Very basic implementation to just check the CPU usage in order to
 * determine if the system runs correctly
 */
public class BasicHeartBeatService implements HeartBeatService {

    private ConfigService config = ConfigProvider.getInstance().serviceImpl();
    private LoggerService logger = LoggerProvider.getInstance().serviceImpl();

    //very basic cache for CPU value. To be implemented as a debouncer.
    private static volatile double cpuCachedValue;
    private static volatile long cpuCachedTime;

    @Override
    public boolean IsHealthy() {
        // to be extended with other basic stats of the system
        boolean isCPUHealthy = isCPUHealthy();
        return isCPUHealthy;
    }

    private boolean isCPUHealthy() {
        long now = System.currentTimeMillis();
        double cpuValue;
        if (cpuCachedTime > now - 1000){
            cpuValue = cpuCachedValue;
        } else {
            cpuValue = getProcessCpuLoad();
            logger.LogWarning(String.format("CPU: %s", String.valueOf(cpuValue)));
            cpuCachedTime = now;
        }
        return cpuValue <= config.getMaxCPUUsage();
    }

    /**
     * from stackoverflow to get the CPU
     * @return CPU usage value
     */
    private double getProcessCpuLoad(){
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

            if (list.isEmpty()) return Double.NaN;

            Attribute att = (Attribute) list.get(0);
            Double value = (Double) att.getValue();

            // usually takes a couple of seconds before we get real values
            if (value == -1.0) return Double.NaN;
            // returns a percentage value with 1 decimal point precision
            return ((int)(value * 1000) / 10.0);
        }catch (Exception ex){
            return 0;
        }
    }
}
