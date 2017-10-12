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
package ch.jamiete.hilda.plugins;

import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Sanity;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginManager {
    private final List<HildaPlugin> plugins = Collections.synchronizedList(new ArrayList<HildaPlugin>());
    private final Hilda hilda;

    public PluginManager(final Hilda hilda) {
        this.hilda = hilda;
    }

    public void disablePlugins() {
        synchronized (this.plugins) {
            final Iterator<HildaPlugin> iterator = this.plugins.iterator();

            while (iterator.hasNext()) {
                final HildaPlugin entry = iterator.next();

                try {
                    entry.onDisable();
                } catch (final Exception e) {
                    Hilda.getLogger().log(Level.WARNING, "Encountered an exception while disabling plugin " + entry.getPluginData().getName(), e);
                }
            }
        }
    }

    public void enablePlugins() {
        synchronized (this.plugins) {
            final Iterator<HildaPlugin> iterator = this.plugins.iterator();

            while (iterator.hasNext()) {
                final HildaPlugin entry = iterator.next();

                try {
                    entry.onEnable();
                    Hilda.getLogger().info("Enabled " + entry.getPluginData().getName() + " v" + entry.getPluginData().getVersion() + " by " + entry.getPluginData().getAuthor());
                } catch (final Exception e) {
                    Hilda.getLogger().log(Level.WARNING, "Encountered an exception while enabling plugin " + entry.getPluginData().getName() + " v" + entry.getPluginData().getVersion(), e);
                    this.plugins.remove(entry);
                }
            }
        }
    }

    /**
     * Gets the plugin with that name or null if there is none.
     * @param name Name to test
     * @return Plugin with that name
     */
    public HildaPlugin getPlugin(final String name) {
        return this.plugins.stream().filter(p -> p.getPluginData().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Gets a list of the plugins tracked by the manager.
     *
     * @return An unmodifiable list
     */
    public List<HildaPlugin> getPlugins() {
        return Collections.unmodifiableList(this.plugins);
    }

    private boolean isLoaded(final PluginData data) {
        return this.plugins.stream().filter(p -> p.getPluginData().equals(data)).findAny().isPresent();
    }

    private boolean isLoaded(final String name) {
        return this.plugins.stream().filter(p -> p.getPluginData().name.equals(name)).findAny().isPresent();
    }

    private boolean loadPlugin(final PluginData data) {
        if (this.isLoaded(data)) {
            return true;
        }

        try {
            final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            final Class<URLClassLoader> sysclass = URLClassLoader.class;
            final Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { data.pluginFile.toURI().toURL() });

            final Class<?> mainClass = Class.forName(data.mainClass);

            if (mainClass != null) {
                if (!HildaPlugin.class.isAssignableFrom(mainClass)) {
                    Hilda.getLogger().severe("Could not load plugin " + data.getName() + " because its main class did not implement HildaPlugin!");
                    return false;
                }

                final HildaPlugin newPlugin = (HildaPlugin) mainClass.getConstructor(Hilda.class).newInstance(this.hilda);

                final Field pluginDataField = HildaPlugin.class.getDeclaredField("pluginData");
                pluginDataField.setAccessible(true);
                pluginDataField.set(newPlugin, data);

                this.plugins.add(newPlugin);

                try {
                    newPlugin.onLoad();
                } catch (final Exception e) {
                    Hilda.getLogger().log(Level.WARNING, "Encountered exception when calling load method of plugin " + data.name + ". It may not have properly loaded and may cause errors.", e);
                }

                Hilda.getLogger().info("Loaded plugin " + data.name);
                return true;
            }
        } catch (final Exception ex) {
            Hilda.getLogger().log(Level.WARNING, "Encountered exception when loading plugin " + data.name, ex);
        }

        return false;
    }

    /**
     * Attempts to load plugin data from the {@code plugin.json} file.
     * @param file
     * @return the plugin data or {@code null} if no data could be loaded
     * @throws IllegalArgumentException if any of the conditions of a plugin data file are not met
     */
    private PluginData loadPluginData(final File file) {
        PluginData data = null;

        try {
            final ZipFile zipFile = new ZipFile(file);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final InputStream stream = zipFile.getInputStream(entry);

                if (entry.getName().equals("plugin.json")) {
                    data = new Gson().fromJson(IOUtils.toString(stream, Charset.defaultCharset()), PluginData.class);

                    Sanity.nullCheck(data.name, "A plugin must define its name.");
                    Sanity.nullCheck(data.mainClass, "A plugin must define its main class.");
                    Sanity.nullCheck(data.version, "A plugin must define its version.");
                    Sanity.nullCheck(data.author, "A plugin must define its author.");

                    data.pluginFile = file;

                    if (data.dependencies == null) {
                        data.dependencies = new String[0];
                    }
                }
            }

            zipFile.close();
        } catch (final Exception ex) {
            Hilda.getLogger().log(Level.SEVERE, "Encountered exception when trying to load plugin JSON for " + file.getName(), ex);
        }

        return data;
    }

    public void loadPlugins() {
        final File pluginsDir = new File("plugins");

        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            Hilda.getLogger().info("Starting without plugins!");
            return;
        }

        final Map<String, PluginData> jars = new HashMap<String, PluginData>();
        final Map<String, List<String>> dependencies = new HashMap<String, List<String>>();
        final Map<String, List<String>> load_after = new HashMap<String, List<String>>();

        // Load all data about valid plugins
        for (final File file : pluginsDir.listFiles()) {
            PluginData data = null;

            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    data = this.loadPluginData(file);

                    if (data == null) {
                        Hilda.getLogger().warning("Failed to load plugin data for " + file.getName());
                        continue;
                    } else {
                        jars.put(data.name, data);
                    }
                } catch (final Exception e) {
                    Hilda.getLogger().log(Level.WARNING, "Failed to load plugin data for " + file.getName(), e);
                    continue;
                }
            } else {
                continue;
            }

            if (data.dependencies != null && data.dependencies.length > 0) {
                dependencies.put(data.name, new ArrayList<String>(Arrays.asList(data.dependencies)));
            }

            if (data.load_after != null && data.load_after.length > 0) {
                load_after.put(data.name, new ArrayList<String>(Arrays.asList(data.load_after)));
            }
        }

        // Remove any load after value that isn't known to the plugin loader
        for (final List<String> list : load_after.values()) {
            final Iterator<String> values = list.iterator();

            while (values.hasNext()) {
                final String name = values.next();

                if (!jars.containsKey(name)) {
                    values.remove();
                }
            }
        }

        // Attempt to load all jars
        while (!jars.isEmpty()) {
            boolean missing = true;
            Iterator<String> iterator = jars.keySet().iterator();

            while (iterator.hasNext()) {
                final String plugin_name = iterator.next();

                // Remove any dependencies we've already loaded, or fail
                if (dependencies.containsKey(plugin_name)) {
                    final Iterator<String> dependency_iterator = dependencies.get(plugin_name).iterator();

                    while (dependency_iterator.hasNext()) {
                        final String dependency = dependency_iterator.next();

                        if (this.isLoaded(dependency)) {
                            dependency_iterator.remove();
                        } else if (!jars.containsKey(dependency)) {
                            iterator.remove();
                            dependency_iterator.remove();
                            load_after.remove(plugin_name);

                            Hilda.getLogger().warning("Failed to load plugin " + plugin_name + " because dependency " + dependency + " could not be found!");

                            break;
                        }
                    }

                    if (dependencies.containsKey(plugin_name) && dependencies.get(plugin_name).isEmpty()) {
                        dependencies.remove(plugin_name);
                    }
                }

                // Remove any load after value we've already loaded
                if (load_after.containsKey(plugin_name)) {
                    final Iterator<String> load_after_iterator = load_after.get(plugin_name).iterator();

                    while (load_after_iterator.hasNext()) {
                        final String loadafter = load_after_iterator.next();

                        if (this.isLoaded(loadafter)) {
                            load_after_iterator.remove();
                        }
                    }

                    if (load_after.containsKey(plugin_name) && load_after.get(plugin_name).isEmpty()) {
                        load_after.remove(plugin_name);
                    }
                }

                // No dependencies remain unloaded
                if (!dependencies.containsKey(plugin_name) || load_after.containsKey(plugin_name) && jars.containsKey(plugin_name)) {
                    final boolean successful = this.loadPlugin(jars.get(plugin_name));

                    if (successful) {
                        iterator.remove();
                        missing = false;
                        continue;
                    }
                }
            } // End of jar iteration

            // Try to load anything
            if (missing) {
                iterator = jars.keySet().iterator();

                while (iterator.hasNext()) {
                    final String plugin_name = iterator.next();

                    if (!dependencies.containsKey(plugin_name)) {
                        iterator.remove();
                        load_after.remove(plugin_name);

                        if (this.loadPlugin(jars.get(plugin_name))) {
                            break;
                        }
                    }
                }
            }

            // No plugins without a dependency remain
            if (missing) {
                iterator = jars.keySet().iterator();

                while (iterator.hasNext()) {
                    iterator.remove();
                    Hilda.getLogger().warning("Absolutely could not load " + iterator.next() + " and have given up trying.");
                }
            }
        }
    }
}
