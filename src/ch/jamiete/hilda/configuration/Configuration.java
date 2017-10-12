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

import ch.jamiete.hilda.Hilda;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;

/**
 * A representation of a configuration file loaded from disk. <p>
 * <b>Do not store references of a configuration file. Fetch it every time you need it.</b> <p>
 * If you do store references of a configuration file, these may become stale over time as new instances of that configuration file are saved to and loaded from disk. As such, data loss becomes possible.
 */
public class Configuration {
    private final File file;
    private JsonObject json;

    public Configuration(final File file) {
        this.file = file;
    }

    public JsonObject get() {
        return this.json;
    }

    public JsonArray getArray(final String name) {
        final JsonArray array = this.json.getAsJsonArray(name);
        return array == null ? new JsonArray() : array;
    }

    public boolean getBoolean(final String name, final boolean def) {
        final JsonElement ele = this.json.get(name);

        if (ele == null) {
            return def;
        }

        try {
            return ele.getAsBoolean();
        } catch (final Exception e) {
            return def;
        }
    }

    public int getInteger(final String name, final int def) {
        final JsonElement ele = this.json.get(name);

        if (ele == null) {
            return def;
        }

        try {
            return ele.getAsInt();
        } catch (final Exception e) {
            return def;
        }
    }

    public String getString(final String name, final String def) {
        final JsonElement ele = this.json.get(name);

        if (ele == null) {
            return def;
        }

        try {
            return ele.getAsString();
        } catch (final Exception e) {
            return def;
        }
    }

    public void load() {
        if (!this.file.exists()) {
            this.json = new JsonObject();
            return;
        }

        Charset charset = null;

        try {
            charset = Charset.forName("UTF-8");
        } catch (final Exception e) {
            charset = Charset.defaultCharset();
        }

        try {
            this.json = new JsonParser().parse(FileUtils.readFileToString(this.file, charset)).getAsJsonObject();
        } catch (final IOException e) {
            Hilda.getLogger().log(Level.WARNING, "Encountered an exception while loading configuration " + this.file.getName(), e);
            this.json = new JsonObject();
        }

        if (new Gson().toJson(this.json).equals("{}")) {
            try {
                this.file.delete();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    public void reset() {
        this.json = new JsonObject();
    }

    public void save() {
        Charset charset = null;

        try {
            charset = Charset.forName("UTF-8");
        } catch (final Exception e) {
            charset = Charset.defaultCharset();
        }

        final String output = new Gson().toJson(this.json);

        if (output.equals("{}")) {
            return;
        }

        try {
            FileUtils.write(this.file, output, charset);
        } catch (final IOException e) {
            Hilda.getLogger().log(Level.WARNING, "Encountered an exception when saving config " + this.file.getName(), e);
        }
    }

    public void setBoolean(final String name, final boolean value) {
        this.json.addProperty(name, value);
        this.save();
    }

    public void setString(final String name, final String value) {
        this.json.addProperty(name, value);
        this.save();
    }

    public void setInteger(final String name, final String value) {
        this.json.addProperty(name, value);
        this.save();
    }

    public boolean hasString(final String name) {
        String ret = null;

        try {
            ret = this.json.get(name).getAsString();
        } catch (Exception e) {

        }

        return ret != null;
    }

    public boolean hasBoolean(final String name) {
        Boolean ret = null;

        try {
            ret = this.json.get(name).getAsBoolean();
        } catch (Exception e) {

        }

        return ret != null;
    }

    public boolean hasInteger(final String name) {
        Integer ret = null;

        try {
            ret = this.json.get(name).getAsInt();
        } catch (Exception e) {

        }

        return ret != null;
    }

}
