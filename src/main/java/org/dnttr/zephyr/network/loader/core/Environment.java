package org.dnttr.zephyr.network.loader.core;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class Environment {

    private final Worker worker;

    public void execute() {
        try {
            this.worker.construct0();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            this.worker.destroy();
        }
    }

    public static final ThreadFactory DAEMON_THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "netty-daemon-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    };
}