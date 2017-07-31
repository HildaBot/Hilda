package ch.jamiete.hilda.runnables;

import java.util.logging.Level;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.plugins.HildaPlugin;

public class HeartbeatTask implements Runnable {
    private final Hilda hilda;

    public HeartbeatTask(final Hilda hilda) {
        this.hilda = hilda;
    }

    @Override
    public void run() {
        for (final HildaPlugin plugin : this.hilda.getPluginManager().getPlugins()) {
            try {
                plugin.save();
            } catch (Exception e) {
                Hilda.getLogger().log(Level.WARNING, "Encountered an exception during heartbeat of " + plugin.getPluginData().getName(), e);
            }
        }

        this.hilda.getConfigurationManager().save();
    }

}
