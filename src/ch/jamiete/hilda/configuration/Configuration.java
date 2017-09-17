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
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ch.jamiete.hilda.Hilda;

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

}
