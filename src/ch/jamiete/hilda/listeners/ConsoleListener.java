/*******************************************************************************
 * Copyright 2017 jamietech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ch.jamiete.hilda.listeners;

import java.util.Scanner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Util;
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

                    Hilda.getLogger().info("Shutting down plugins...");
                    this.hilda.getPluginManager().disablePlugins();
                    Hilda.getLogger().info("Plugins shut down!");

                    Hilda.getLogger().info("Saving configurations...");
                    this.hilda.getConfigurationManager().save();
                    Hilda.getLogger().info("Configurations saved!");

                    Hilda.getLogger().info("Shutting down executor...");
                    this.hilda.getExecutor().shutdown();
                    try {
                        this.hilda.getExecutor().awaitTermination(30, TimeUnit.SECONDS);
                    } catch (final InterruptedException e) {
                        Hilda.getLogger().log(Level.WARNING, "Encountered an exception whilst terminating executor", e);
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
