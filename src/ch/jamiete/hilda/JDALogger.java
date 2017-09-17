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

import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.utils.SimpleLog.Level;
import net.dv8tion.jda.core.utils.SimpleLog.LogListener;

public class JDALogger implements LogListener {

    @Override
    public void onLog(SimpleLog log, Level level, Object object) {
        if (level.getPriority() < 3 && !Start.DEBUG) {
            return;
        }

        String message = "";

        message += "[" + log.name + "-JDA]";
        message += " " + String.valueOf(object);

        java.util.logging.Level newlevel;

        switch (level) {
            case DEBUG:
                newlevel = java.util.logging.Level.FINE;
                break;

            case FATAL:
                newlevel = java.util.logging.Level.SEVERE;
                break;

            case INFO:
                newlevel = java.util.logging.Level.INFO;
                break;

            case TRACE:
                newlevel = java.util.logging.Level.FINE;
                break;

            case WARNING:
                newlevel = java.util.logging.Level.WARNING;
                break;

            default:
                newlevel = java.util.logging.Level.FINE;
                break;
        }

        Hilda.getLogger().logp(newlevel, null, null, message);
    }

    @Override
    public void onError(SimpleLog log, Throwable thrown) {
        Hilda.getLogger().log(java.util.logging.Level.WARNING, "JDA encountered an exception.", thrown);
    }

}
