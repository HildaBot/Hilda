package ch.jamiete.hilda.runnables;

import java.io.File;
import java.util.logging.Level;
import ch.jamiete.hilda.Hilda;

public class FileDeletionTask implements Runnable {
    private final File file;

    public FileDeletionTask(final File file) {
        this.file = file;
    }

    @Override
    public void run() {
        try {
            this.file.delete();
            Hilda.getLogger().info("Deleted " + this.file.getAbsolutePath());
        } catch (final Exception e) {
            Hilda.getLogger().log(Level.WARNING, "Failed to delete file " + this.file.getAbsolutePath(), e);
            this.file.deleteOnExit();
        }
    }

}
