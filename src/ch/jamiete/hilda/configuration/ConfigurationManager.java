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
package ch.jamiete.hilda.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Start;
import ch.jamiete.hilda.plugins.HildaPlugin;

public class ConfigurationManager {
    private static final int TIME_LIMIT = 5 * 60 * 1000;
    private final List<ConfigurationWrapper> configs = Collections.synchronizedList(new ArrayList<>());

    public ConfigurationManager(final Hilda hilda) {
        hilda.getExecutor().scheduleWithFixedDelay(() -> {
            this.unload();
        }, 10, 10, TimeUnit.MINUTES);
    }

    public Configuration getConfiguration(final HildaPlugin plugin) {
        return this.getConfiguration(plugin, "config.json");
    }

    public Configuration getConfiguration(final HildaPlugin plugin, final String name) {
        return this.getConfiguration(plugin.getPluginData().getName() + "-" + name.toLowerCase());
    }

    public Configuration getConfiguration(final String name) {
        String id = name.toLowerCase();

        if (!id.endsWith(".json")) {
            id = id + ".json";
        }

        if (id.contains("/")) {
            id = id.replace('/', '-');
        }

        ConfigurationWrapper config;
        final String tmpId = id;

        synchronized (this.configs) {
            config = this.configs.stream().filter(wrapper -> wrapper.name.equals(tmpId)).findFirst().orElse(null);
        }

        if (config != null) {
            config.access = System.currentTimeMillis();
            return config.getConfiguration();
        }

        config = new ConfigurationWrapper(tmpId, new Configuration(new File("configs/" + id)));

        synchronized (this.configs) {
            this.configs.add(config);
        }

        config.getConfiguration().load();
        return config.getConfiguration();
    }

    public void save() {
        synchronized (this.configs) {
            this.configs.forEach(wrapper -> wrapper.getConfiguration().save());
        }

        Hilda.getLogger().fine("Saved all " + this.configs.size() + " configuration files.");
    }

    public void unload() {
        final long now = System.currentTimeMillis();
        final int size = this.configs.size();
        this.configs.removeIf(wrapper -> now - wrapper.access >= ConfigurationManager.TIME_LIMIT);

        if (Start.DEBUG && size > this.configs.size()) {
            Hilda.getLogger().fine("Pruned " + (size - this.configs.size()) + " loaded configuration files.");
        }
    }
}
