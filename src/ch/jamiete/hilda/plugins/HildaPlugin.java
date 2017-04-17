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

public abstract class HildaPlugin {

    private final Hilda hilda;
    private PluginData pluginData;

    public HildaPlugin(final Hilda hilda) {
        this.hilda = hilda;
    }

    public final Hilda getHilda() {
        return this.hilda;
    }

    public final PluginData getPluginData() {
        return this.pluginData;
    }

    public void onDisable() {

    }

    public abstract void onEnable();

    public void onLoad() {

    }

}
