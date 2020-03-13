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
package ch.jamiete.hilda.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ch.jamiete.hilda.Hilda;
import net.dv8tion.jda.api.Permission;

public abstract class GenericCommand implements Command {
    protected final Hilda hilda;

    private String name;

    List<String> aliases;
    private String description;
    private Permission minimumPermission;

    boolean aliasesFinal = false;
    private boolean hide = false;
    private boolean async = false;

    private int timeout = 0;
    private Map<String, Long> timeouts;

    GenericCommand(final Hilda hilda) {
        this.hilda = hilda;
    }

    @Override
    public final List<String> getAliases() {
        return this.aliases == null ? Collections.emptyList() : Collections.unmodifiableList(this.aliases);
    }

    @Override
    public final String getDescription() {
        return this.description;
    }

    /**
     * Gets whether the command should be hidden from the help command.
     * @return Whether the command should be hidden from the help command.
     */
    public final boolean getHide() {
        return this.hide;
    }

    @Override
    public final Permission getMinimumPermission() {
        return this.minimumPermission;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc} <p>
     * Case insensitive.
     */
    @Override
    public final boolean hasAlias(final String test) {
        if (this.aliases == null) {
            return false;
        }

        return this.aliases.stream().anyMatch(a -> a.equalsIgnoreCase(test));
    }

    @Override
    public boolean isAsync() {
        return this.async;
    }

    /**
     * {@inheritDoc}
     * The aliases will be saved as a copy of the provided list. If there are no aliases to be recognised by this channel do not invoke this method.
     * <b>Aliases cannot be set again after they haves been set. As such, aliases should be set by the constructor.</b>
     * @throws RuntimeException If the aliases have already been set.
     */
    @Override
    public final void setAliases(final List<String> aliases) {
        if (this.aliasesFinal) {
            throw new RuntimeException("Command already has aliases specified.");
        }

        // Create a new list to ensure it is modifiable
        final List<String> temp = new ArrayList<>(aliases.size());
        aliases.forEach(a -> temp.add(a.toLowerCase()));

        this.aliases = temp;
    }

    @Override
    public void setAsync(final boolean async) {
        this.async = async;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Sets whether the command should be hidden from the help command.
     * @param hide Whether the command should be hidden from the help command.
     */
    public final void setHide(final boolean hide) {
        this.hide = hide;
    }

    @Override
    public final void setMinimumPermission(final Permission minimum_permission) {
        this.minimumPermission = minimum_permission;
    }

    /**
     * {@inheritDoc}
     * <b>The name cannot be set again after it has been set. As such, the name should be set by the constructor.</b>
     * @throws RuntimeException If the name has already been set.
     */
    @Override
    public final void setName(final String name) {
        if (this.name != null) {
            throw new RuntimeException("Command already named.");
        }

        this.name = name;
    }

    /**
     * Gets the timeout in seconds between command invocations per user.
     * @return timeout in seconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout in seconds between command invocations per user. Set to 0 for no timeout.
     * @param timeout timeout in seconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;

        if (timeout == 0) {
            this.timeouts = null;
        } else {
            this.timeouts = Collections.synchronizedMap(new HashMap<>());
        }
    }

    /**
     * Gets whether a particular user can execute the command with regards to the timeout.
     * @param id The user to check
     * @return Whether that user can execute the command
     */
    public boolean canExecute(String id) {
        if (this.timeout == 0) {
            return true;
        }

        long last = this.timeouts.getOrDefault(id, 0L);

        if (last == 0) {
            return true;
        }

        return System.currentTimeMillis() - last >= this.timeout * 1000;
    }

    /**
     * Marks the command as executed by the user at the current time for the timeout timer.
     * @param id
     */
    public void markExecuted(String id) {
        if (this.timeouts == null) {
            return;
        }

        this.timeouts.put(id, System.currentTimeMillis());
    }

    /**
     * Empties the old timeouts from the cache.
     */
    public void clearTimeouts() {
        if (this.timeouts == null) {
            return;
        }

        synchronized (this.timeouts) {
            this.timeouts.entrySet().removeIf(entry -> {
                if (System.currentTimeMillis() - entry.getValue() >= this.timeout * 1000) {
                    return true;
                }

                return false;
            });
        }
    }

}
