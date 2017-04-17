package ch.jamiete.hilda.runnables;

import ch.jamiete.hilda.Start;

public class LogRotateTask implements Runnable {

    @Override
    public void run() {
        Start.setupLogging();
    }

}
