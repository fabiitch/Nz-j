package com.nz.nzj.thread.factory;

import lombok.AllArgsConstructor;

import java.util.concurrent.ThreadFactory;

@AllArgsConstructor
public class NamedThreadFactory implements ThreadFactory {

    private final String name;
    private final boolean daemon;

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(name);
        t.setDaemon(daemon);
        return t;
    }
}
