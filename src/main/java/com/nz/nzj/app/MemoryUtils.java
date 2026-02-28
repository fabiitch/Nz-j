package com.nz.nzj.app;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class MemoryUtils {
    public static String getMemoryLog() {
        Runtime rt = Runtime.getRuntime();

        long heapTotal = rt.totalMemory();
        long heapFree  = rt.freeMemory();
        long heapUsed  = heapTotal - heapFree;
        long heapMax   = rt.maxMemory();

        double nativeUsed = -1;

        try {
            OperatingSystemMXBean os =
                    (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            nativeUsed = os.getProcessCpuLoad(); // dummy init guard
            nativeUsed = os.getCommittedVirtualMemorySize();
        } catch (Throwable ignored) {
            // JVM sans accès aux extensions com.sun.*
        }

        return String.format(
                "MEM | heap: used=%dMB total=%dMB max=%dMB | native: committed=%s",
                heapUsed / 1024 / 1024,
                heapTotal / 1024 / 1024,
                heapMax / 1024 / 1024,
                nativeUsed > 0 ? (nativeUsed / 1024 / 1024 + "MB") : "n/a"
        );
    }
}
