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

public class PluginData {

    // can be in .json
    protected String name;
    protected String mainClass;
    protected String version;
    protected String author;
    protected String[] dependencies;
    protected String[] load_after;

    // internal fields
    protected File pluginFile;

    public String getAuthor() {
        return this.author;
    }

    public String[] getDependencies() {
        return this.dependencies;
    }

    public String[] getLoadAfter() {
        return this.load_after;
    }

    public String getName() {
        return this.name;
    }

    public File getPluginFile() {
        return this.pluginFile;
    }

    public String getVersion() {
        return this.version;
    }

}
