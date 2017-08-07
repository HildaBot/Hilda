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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ch.jamiete.hilda.Hilda;

public class Configuration {
    private File file;
    private JsonObject json;

    public Configuration(File file) {
        this.file = file;
    }

    public JsonObject get() {
        return this.json;
    }

    public void save() {
        Charset charset = null;

        try {
            charset = Charset.forName("UTF-8");
        } catch (Exception e) {
            charset = Charset.defaultCharset();
        }

        String output = new Gson().toJson(json);

        if (output.equals("{}")) {
            return;
        }

        try {
            FileUtils.write(file, output, charset);
        } catch (IOException e) {
            Hilda.getLogger().log(Level.WARNING, "Encountered an exception when saving config " + file.getName(), e);
        }
    }

    public void load() {
        if (!file.exists()) {
            this.json = new JsonObject();
            return;
        }

        Charset charset = null;

        try {
            charset = Charset.forName("UTF-8");
        } catch (Exception e) {
            charset = Charset.defaultCharset();
        }

        try {
            this.json = new JsonParser().parse(FileUtils.readFileToString(file, charset)).getAsJsonObject();
        } catch (IOException e) {
            Hilda.getLogger().log(Level.WARNING, "Encountered an exception while loading configuration " + file.getName(), e);
            this.json = new JsonObject();
        }

        if (new Gson().toJson(this.json).equals("{}")) {
            try {
                file.delete();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public boolean getBoolean(String name, boolean def) {
        JsonElement ele = this.json.get(name);

        if (ele == null) {
            return def;
        }

        try {
            return ele.getAsBoolean();
        } catch (Exception e) {
            return def;
        }
    }

    public String getString(String name, String def) {
        JsonElement ele = this.json.get(name);

        if (ele == null) {
            return def;
        }

        try {
            return ele.getAsString();
        } catch (Exception e) {
            return def;
        }
    }

    public int getInteger(String name, int def) {
        JsonElement ele = this.json.get(name);

        if (ele == null) {
            return def;
        }

        try {
            return ele.getAsInt();
        } catch (Exception e) {
            return def;
        }
    }

}
