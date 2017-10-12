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
package ch.jamiete.hilda.runnables;

import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.plugins.HildaPlugin;
import java.util.logging.Level;

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
            } catch (final Exception e) {
                Hilda.getLogger().log(Level.WARNING, "Encountered an exception during heartbeat of " + plugin.getPluginData().getName(), e);
            }
        }

        this.hilda.getConfigurationManager().save();
    }

}
