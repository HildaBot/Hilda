package ch.jamiete.hilda;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import ch.jamiete.hilda.listeners.UncaughtExceptionListener;

public class HildaThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private static final UncaughtExceptionListener listener = new UncaughtExceptionListener();
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String prefix;

    public HildaThreadFactory() {
        SecurityManager manager = System.getSecurityManager();
        group = (manager != null) ? manager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        prefix = "HildaPool(" + poolNumber.getAndIncrement() + ")-thread-";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(group, runnable, prefix + threadNumber.getAndIncrement(), 0);

        if (thread.isDaemon())
            thread.setDaemon(false);

        if (thread.getPriority() != Thread.NORM_PRIORITY)
            thread.setPriority(Thread.NORM_PRIORITY);

        thread.setUncaughtExceptionHandler(HildaThreadFactory.listener);

        return thread;
    }
}
