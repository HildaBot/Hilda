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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.plugins.HildaPlugin;

public class ConfigurationManager {
    private Map<String, Configuration> configs = Collections.synchronizedMap(new HashMap<String, Configuration>());

    public void save() {
        synchronized (this.configs) {
            configs.forEach((s, c) -> c.save());
        }

        Hilda.getLogger().info("Saved all " + configs.size() + " configuration files.");
    }

    public Configuration getConfiguration(HildaPlugin plugin) {
        return this.getConfiguration(plugin, "config.json");
    }

    public Configuration getConfiguration(String name) {
        String id = name.toLowerCase();

        if (!id.endsWith(".json")) {
            id = id + ".json";
        }

        if (id.contains("/")) {
            id = id.replace('/', '-');
        }

        Configuration config;

        synchronized (this.configs) {
            config = configs.get(id);
        }

        if (config != null) {
            return config;
        }

        config = new Configuration(new File("configs/" + id));

        synchronized (this.configs) {
            this.configs.put(id, config);
        }

        config.load();
        return config;
    }

    public Configuration getConfiguration(HildaPlugin plugin, String name) {
        return this.getConfiguration(plugin.getPluginData().getName() + "-" + name.toLowerCase());
    }
}
