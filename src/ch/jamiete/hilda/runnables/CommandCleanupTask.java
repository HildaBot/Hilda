package ch.jamiete.hilda.runnables;

import ch.jamiete.hilda.commands.CommandManager;

public class CommandCleanupTask implements Runnable {
    private final CommandManager manager;

    public CommandCleanupTask(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        this.manager.getChannelCommands().forEach(c -> c.clearTimeouts());
    }

}
