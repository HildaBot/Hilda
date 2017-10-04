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
import java.util.List;
import ch.jamiete.hilda.Hilda;
import net.dv8tion.jda.core.Permission;

public abstract class GenericCommand implements Command {
    protected final Hilda hilda;

    private String name;

    List<String> aliases;
    private String description;
    private Permission minimumPermission;

    boolean aliasesFinal = false;
    private boolean hide = false;

    GenericCommand(final Hilda hilda) {
        this.hilda = hilda;
    }

    @Override
    public final List<String> getAliases() {
        return aliases == null ? Collections.emptyList() : Collections.unmodifiableList(this.aliases);
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
        List<String> temp = new ArrayList<>(aliases.size());
        aliases.forEach(a -> temp.add(a.toLowerCase()));

        this.aliases = temp;
    }

    @Override
    public final void setDescription(final String description) {
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

}
