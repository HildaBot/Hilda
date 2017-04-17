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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Sanity;

public class PluginManager {
    private final List<HildaPlugin> plugins = Collections.synchronizedList(new ArrayList<>());
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
                } catch (final Exception e) {
                    Hilda.getLogger().log(Level.WARNING, "Encountered an exception while disabling plugin " + entry.getPluginData().getName(), e);
                    this.plugins.remove(entry);
                }
            }
        }
    }

    /**
     * Gets a list of the plugins tracked by the manager.
     * @return An unmodifiable list
     */
    public List<HildaPlugin> getPlugins() {
        return Collections.unmodifiableList(this.plugins);
    }

    private void loadPlugin(final File file) {
        try {
            final ZipFile zipFile = new ZipFile(file);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            PluginData data = null;

            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final InputStream stream = zipFile.getInputStream(entry);

                if (entry.getName().equals("plugin.json")) {
                    data = new Gson().fromJson(IOUtils.toString(stream, Charset.defaultCharset()), PluginData.class);

                    Sanity.nullCheck(data.name, "A plugin must define its name.");
                    Sanity.nullCheck(data.mainClass, "A plugin must define its main class.");
                    Sanity.nullCheck(data.version, "A plugin must define its version.");
                    Sanity.nullCheck(data.author, "A plugin must define its author.");
                }
            }

            zipFile.close();

            if (data == null) {
                Hilda.getLogger().severe("Could not load plugin " + file.getName() + " as it has no JSON file!");
                return;
            }

            final URLClassLoader classLoader = new URLClassLoader(new URL[] { file.toURI().toURL() });
            final Class<?> mainClass = Class.forName(data.mainClass, true, classLoader);

            if (mainClass != null) {
                if (!HildaPlugin.class.isAssignableFrom(mainClass)) {
                    Hilda.getLogger().severe("Could not load plugin " + file.getName() + " because its main class did not implement HildaPlugin!");
                    return;
                }

                final HildaPlugin newPlugin = (HildaPlugin) mainClass.getConstructor(Hilda.class).newInstance(this.hilda);

                final Field pluginDataField = HildaPlugin.class.getDeclaredField("pluginData");
                pluginDataField.setAccessible(true);
                pluginDataField.set(newPlugin, data);

                this.plugins.add(newPlugin);

                newPlugin.onLoad();
                Hilda.getLogger().info("Loaded plugin " + data.name);
            }
        } catch (final Exception ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadPlugins() {
        final File pluginsDir = new File("plugins");

        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            Hilda.getLogger().info("Starting without plugins!");
            return;
        }

        for (final File file : pluginsDir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                this.loadPlugin(file);
            }
        }
    }
}
