package ch.jamiete.hilda;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class HildaScheduledExecutor extends ScheduledThreadPoolExecutor {

    class HildaRunnable implements Runnable {
        private final Runnable sub;

        HildaRunnable(final Runnable sub) {
            this.sub = sub;
        }

        @Override
        public void run() {
            try {
                this.sub.run();
            } catch (final Throwable t) {
                Hilda.getLogger().log(Level.WARNING, "Caught unhandled exception in a scheduled task", t);
            }
        }
    }

    public HildaScheduledExecutor(final int corePoolSize, final ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    @Override
    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        return super.schedule(new HildaRunnable(command), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        return super.scheduleAtFixedRate(new HildaRunnable(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        return super.scheduleWithFixedDelay(new HildaRunnable(command), initialDelay, delay, unit);
    }

}
