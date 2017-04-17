package ch.jamiete.hilda;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import ch.jamiete.hilda.commands.CommandManager;
import ch.jamiete.hilda.listeners.ConsoleListener;
import ch.jamiete.hilda.plugins.PluginManager;
import ch.jamiete.hilda.runnables.LogRotateTask;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class Hilda {
    private static final Logger LOGGER = Logger.getLogger("Hilda");

    public static Logger getLogger() {
        return Hilda.LOGGER;
    }

    protected final JDA bot;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);

    private CommandManager commander;
    private PluginManager plugins;

    public Hilda(final String apikey) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
        this.bot = new JDABuilder(AccountType.BOT).setAutoReconnect(false).setToken(apikey).buildBlocking();
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
     * @return The {@link ScheduledThreadPoolExecutor} instance
     */
    public ScheduledThreadPoolExecutor getExecutor() {
        return this.executor;
    }

    /**
     * Helper method <br>
     * Gets the milliseconds of the nearest hour in timezone.
     * @param timezone The timezone to check.
     * @return The next hour in timezone.
     */
    public long getNextHour(final String timezone) {
        final Calendar time = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        time.add(Calendar.HOUR, 1);
        return time.getTimeInMillis();
    }

    /**
     * Helper method <br>
     * Gets the milliseconds of the next midnight in timezone.
     * @param timezone The timezone to check.
     * @return The next midnight in timezone.
     */
    public long getNextMidnightInMillis(final String timezone) {
        final Calendar time = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        time.set(Calendar.HOUR_OF_DAY, 0);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        time.add(Calendar.DAY_OF_MONTH, 1);
        return time.getTimeInMillis();
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

    public void start() {
        this.bot.setAutoReconnect(true);
        Hilda.getLogger().info("Connected to server!");

        this.bot.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);

        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.executor.setRemoveOnCancelPolicy(true);
        this.executor.setMaximumPoolSize(10);

        final long rotate = this.getNextMidnightInMillis("GMT+10") - System.currentTimeMillis();
        this.executor.scheduleAtFixedRate(new LogRotateTask(), rotate, 86400000, TimeUnit.MILLISECONDS); // At midnight then every 24 hours
        Hilda.getLogger().info("Rotating log files in " + Util.getFriendlyTime(rotate));

        Hilda.getLogger().info("Registering managers...");
        this.commander = new CommandManager(this);
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
