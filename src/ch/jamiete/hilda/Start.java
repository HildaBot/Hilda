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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import ch.jamiete.hilda.listeners.UncaughtExceptionListener;
import net.dv8tion.jda.api.requests.RestAction;

public class Start {
    /**
     * Whether the bot should be more verbose in logging
     */
    public static boolean DEBUG = false;

    public static void main(final String[] args) {
        if (args.length < 1) {
            System.err.println("HILDA DID NOT START.");
            System.err.println();
            System.err.println("You must provide a single argument consisting of the API key to use when connecting.");
            System.err.println("Optionally, pass a second argument of 'true' to enable debug.");
            System.err.println();
            System.err.println("Terminating...");
            System.exit(1);
        }

        if (args.length == 2 && "true".equalsIgnoreCase(args[1])) {
            Start.DEBUG = true;
        }

        final Start start = new Start();

        // Hilda startup
        Start.setupLogging();
        start.start(args[0]);
    }

    public static void setupFileLogging() {
        final Logger global = Logger.getLogger("");

        for (final Handler handler : global.getHandlers()) {
            if (handler instanceof FileHandler) {
                global.removeHandler(handler);
            }
        }

        try {
            final File file = new File("log");

            if (!file.isDirectory()) {
                file.mkdir();
            }

            final FileHandler lfh = new FileHandler("log/hilda_" + new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime()) + ".log", true);
            lfh.setFormatter(new LogFormat());
            lfh.setLevel(Level.INFO);

            global.addHandler(lfh);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void setupLogging() {
        Hilda.getLogger().setUseParentHandlers(true);

        final Logger global = Logger.getLogger("");

        // Console logging

        for (final Handler handler : global.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                global.removeHandler(handler);
            }
        }

        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogFormat());
        global.addHandler(handler);

        // Uncaught logging

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionListener());

        // File logging

        Start.setupFileLogging();

        // Level of logging

        if (Start.DEBUG) {
            handler.setLevel(Level.FINE);
            global.setLevel(Level.FINE);
        } else {
            handler.setLevel(Level.INFO);
            global.setLevel(Level.INFO);
        }
    }

    private Hilda hilda;

    private int tries = 0;

    private void start(final String apikey) {
        if (Start.DEBUG) {
            Hilda.getLogger().fine("======================");
            Hilda.getLogger().fine("Started in DEBUG mode.");
            Hilda.getLogger().fine("======================");

            RestAction.setPassContext(true);
            RestAction.setDefaultFailure(Throwable::printStackTrace);
        }

        try {
            this.hilda = new Hilda(apikey);
            this.hilda.start();
        } catch (final IllegalArgumentException e) {
            Hilda.getLogger().log(Level.WARNING, "Encountered an exception while starting Hilda", e);
            System.exit(1);
        } catch (LoginException | InterruptedException e) {
            Hilda.getLogger().log(Level.WARNING, "Encountered an exception while logging in to Discord", e);

            this.tries++;

            if (this.tries < 5) {
                Hilda.getLogger().warning("Failed to login; retrying...");
                this.hilda.bot.shutdown();
                this.hilda = null;
                this.start(apikey);
            } else {
                Hilda.getLogger().severe("Failed to login 5 times; giving up.");
                System.exit(1);
            }
        }
    }

}
