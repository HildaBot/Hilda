/*
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
 */
package ch.jamiete.hilda.configuration;

class ConfigurationWrapper {
    String name;
    private final Configuration configuration;
    long access;

    ConfigurationWrapper(final String name, final Configuration configuration) {
        this(name, configuration, System.currentTimeMillis());
    }

    ConfigurationWrapper(final String name, final Configuration configuration, final long access) {
        this.name = name;
        this.configuration = configuration;
        this.access = access;
    }

    public Configuration getConfiguration() {
        this.access = System.currentTimeMillis();
        return this.configuration;
    }
}
