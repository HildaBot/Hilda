package ch.jamiete.hilda.listeners;

import java.util.logging.Level;
import ch.jamiete.hilda.Hilda;

public class UncaughtExceptionListener implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        Hilda.getLogger().log(Level.SEVERE, "Uncaught exception thrown in " + t.getName() + " (" + t.getClass().getName() + ")", e);
    }

}
