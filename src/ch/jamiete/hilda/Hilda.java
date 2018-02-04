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
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Hilda {
    private static final Logger LOGGER = Logger.getLogger("Hilda");

    public static Logger getLogger() {
        return Hilda.LOGGER;
    }

    final JDA bot;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3, new HildaThreadFactory());

    private CommandManager commander;
    private ConfigurationManager configs;
    private PluginManager plugins;
    private HildaDB db;

    public Hilda(final String apikey) throws LoginException, InterruptedException, RateLimitedException {
        this.bot = new JDABuilder(AccountType.BOT).setAutoReconnect(false).setToken(apikey).setEventManager(new AnnotatedEventManager()).setStatus(OnlineStatus.DO_NOT_DISTURB).buildBlocking();

        if (Start.DEBUG) {
            SimpleLog.getLog("JDA").setLevel(net.dv8tion.jda.core.utils.SimpleLog.Level.ALL);
            SimpleLog.LEVEL = SimpleLog.Level.ALL;
        }
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
     * @return The {@link HildaDB} instance
     */
    public HildaDB getHildaDb() {
        return this.db;
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

    void start() {
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
        this.configs = new ConfigurationManager();
        this.plugins = new PluginManager(this);
        this.db = new HildaDB();
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

        Hilda.getLogger().info("Connecting to database...");
        this.db.connect();

        Hilda.getLogger().info("Done!");

        this.bot.getPresence().setStatus(OnlineStatus.ONLINE);

        Hilda.getLogger().info("Startup complete!");
    }
}
