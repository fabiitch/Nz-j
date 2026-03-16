package com.nz.nzj.thread.factory;

import lombok.AllArgsConstructor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
public class NamedIncThreadFactory implements ThreadFactory {

    private final String name;
    private final int priority;
    private final boolean daemon;

    private final AtomicInteger threadNumber = new AtomicInteger(1);


    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(name + "-" + threadNumber.getAndIncrement());
        t.setDaemon(daemon);
        t.setPriority(priority);
        return t;
    }
}
