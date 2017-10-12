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
package ch.jamiete.hilda;

import ch.jamiete.hilda.commands.CommandManager;
import ch.jamiete.hilda.configuration.ConfigurationManager;
import ch.jamiete.hilda.events.AnnotatedEventManager;
import ch.jamiete.hilda.listeners.ConsoleListener;
import ch.jamiete.hilda.plugins.PluginManager;
import ch.jamiete.hilda.runnables.HeartbeatTask;
import ch.jamiete.hilda.runnables.LogRotateTask;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import javax.security.auth.login.LoginException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Hilda {
    private static final Logger LOGGER = Logger.getLogger("Hilda");

    public static Logger getLogger() {
        return Hilda.LOGGER;
    }

    protected final JDA bot;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3, new HildaThreadFactory());

    private CommandManager commander;
    private ConfigurationManager configs;
    private PluginManager plugins;

    public Hilda(final String apikey) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
        this.bot = new JDABuilder(AccountType.BOT).setAutoReconnect(false).setToken(apikey).setEventManager(new AnnotatedEventManager()).setStatus(OnlineStatus.DO_NOT_DISTURB).buildBlocking();
    }

    /**
     * @return The {@link JDA} instance used by the bot.
     */
    public JDA getBot() {
        return this.bot;
    }

    /**
     * @return The {@link CommandManager} instance
     */
    public CommandManager getCommandManager() {
        return this.commander;
    }

    /**
     * @return The {@link ConfigurationManager} instance
     */
    public ConfigurationManager getConfigurationManager() {
        return this.configs;
    }

    /**
     * @return The {@link ScheduledThreadPoolExecutor} instance
     */
    public ScheduledThreadPoolExecutor getExecutor() {
        return this.executor;
    }

    /**
     * @return The {@link PluginManager} instance
     */
    public PluginManager getPluginManager() {
        return this.plugins;
    }

    /**
     * Helper method
     * @return The current username of the bot
     */
    public String getUsername() {
        return this.bot.getSelfUser().getName();
    }

    /**
     * Calls all necessary methods for a graceful shutdown. <p>
     * <b>You will still need to exit the system with the correct system exit code.</b>
     */
    public void shutdown() {
        Hilda.getLogger().info("Shutting down...");

        this.bot.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        this.commander.shutdown();

        Hilda.getLogger().info("Shutting down plugins...");
        this.plugins.disablePlugins();
        Hilda.getLogger().info("Plugins shut down!");

        Hilda.getLogger().info("Saving configurations...");
        this.configs.save();
        Hilda.getLogger().info("Configurations saved!");

        Hilda.getLogger().info("Shutting down executor...");
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Hilda.getLogger().log(Level.WARNING, "Encountered an exception whilst terminating executor", e);
        }
        Hilda.getLogger().info("Executor completed " + this.executor.getCompletedTaskCount() + " with largest pool of " + this.executor.getLargestPoolSize());
        Hilda.getLogger().info("Executor shut down!");

        Hilda.getLogger().info("Disconnecting from Discord...");
        this.bot.shutdown();
        Hilda.getLogger().info("Disconnected!");

        Hilda.getLogger().info("Goodbye!");
    }

    public void start() {
        this.bot.setAutoReconnect(true);
        Hilda.getLogger().info("Connected to server!");

        Util.setHilda(this);

        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.executor.setRemoveOnCancelPolicy(true);
        this.executor.setMaximumPoolSize(10);

        final long rotate = Util.getNextMidnightInMillis("GMT+10") - System.currentTimeMillis();
        this.executor.scheduleAtFixedRate(new LogRotateTask(), rotate, 86400000, TimeUnit.MILLISECONDS); // At midnight then every 24 hours
        Hilda.getLogger().info("Rotating log files in " + Util.getFriendlyTime(rotate));

        this.executor.scheduleAtFixedRate(new HeartbeatTask(this), 5, 5, TimeUnit.MINUTES);

        Hilda.getLogger().info("Registering managers...");
        this.commander = new CommandManager(this);
        this.configs = new ConfigurationManager(this);
        this.plugins = new PluginManager(this);
        Hilda.getLogger().info("Managers registered!");

        Hilda.getLogger().info("Registering listeners...");
        this.bot.addEventListener(this.commander);
        new ConsoleListener(this).start();
        Hilda.getLogger().info("Listeners registered!");

        Hilda.getLogger().info("Loading plugins...");
        this.plugins.loadPlugins();
        Hilda.getLogger().info("Plugins loaded!");

        Hilda.getLogger().info("Enabling plugins...");
        this.plugins.enablePlugins();
        Hilda.getLogger().info("Plugins enabled!");

        Hilda.getLogger().info("Done!");

        this.bot.getPresence().setStatus(OnlineStatus.ONLINE);

        Hilda.getLogger().info("Startup complete!");
    }

}
