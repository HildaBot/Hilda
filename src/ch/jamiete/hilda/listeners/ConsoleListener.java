package ch.jamiete.hilda.listeners;

import java.util.Scanner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.commands.ChannelCommand;
import ch.jamiete.hilda.plugins.HildaPlugin;
import net.dv8tion.jda.core.OnlineStatus;

public class ConsoleListener extends Thread {
    private final Hilda hilda;

    public ConsoleListener(final Hilda hilda) {
        this.hilda = hilda;
        this.setName("Console Listener");
    }

    @Override
    public void run() {
        final Scanner scanner = new Scanner(System.in);

        while (true) {
            switch (scanner.nextLine().toLowerCase()) {
                case "stop":
                case "shutdown":
                case "end":
                case "quit":
                    scanner.close();
                    Hilda.getLogger().info("Shutting down...");

                    this.hilda.getBot().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                    this.hilda.getCommandManager().shutdown();

                    Hilda.getLogger().info("Shutting down commands...");
                    for (final ChannelCommand command : this.hilda.getCommandManager().getChannelCommands()) {
                        command.onShutdown();
                    }
                    Hilda.getLogger().info("Commands shut down!");

                    Hilda.getLogger().info("Shutting down plugins...");
                    for (final HildaPlugin plugin : this.hilda.getPluginManager().getPlugins()) {
                        plugin.onDisable();
                    }
                    Hilda.getLogger().info("Plugins shut down!");

                    Hilda.getLogger().info("Shutting down executor...");
                    this.hilda.getExecutor().shutdown();
                    try {
                        this.hilda.getExecutor().awaitTermination(30, TimeUnit.SECONDS);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                    Hilda.getLogger().info("Executor completed " + this.hilda.getExecutor().getCompletedTaskCount() + " with largest pool of " + this.hilda.getExecutor().getLargestPoolSize());
                    Hilda.getLogger().info("Executor shut down!");

                    Hilda.getLogger().info("Disconnecting from Discord...");
                    this.hilda.getBot().shutdown();
                    Hilda.getLogger().info("Disconnected!");

                    Hilda.getLogger().info("Goodbye!");
                    System.exit(0);
                    break;

                case "schedule":
                    Hilda.getLogger().info("Scheduler information:");
                    Hilda.getLogger().info("> Executing: " + this.hilda.getExecutor().getActiveCount());
                    Hilda.getLogger().info("> Completed: " + this.hilda.getExecutor().getCompletedTaskCount());
                    Hilda.getLogger().info("> Pool: " + this.hilda.getExecutor().getPoolSize());
                    Hilda.getLogger().info("> Largest pool: " + this.hilda.getExecutor().getLargestPoolSize());
                    Hilda.getLogger().info("> Queued: " + this.hilda.getExecutor().getQueue().size());

                    for (final Runnable runnable : this.hilda.getExecutor().getQueue()) {
                        final StringBuilder sb = new StringBuilder();
                        final ScheduledFuture<?> future = (ScheduledFuture<?>) runnable;

                        sb.append("    ").append(runnable.getClass().getName());
                        sb.append(" executing in ").append(Util.getFriendlyTime(future.getDelay(TimeUnit.MILLISECONDS)));

                        Hilda.getLogger().info(sb.toString());
                    }
                    break;

                default:
                    Hilda.getLogger().info("Unknown command.");
                    break;
            }
        }
    }

}
