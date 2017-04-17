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

import java.util.Collections;
import java.util.List;
import ch.jamiete.hilda.Hilda;
import net.dv8tion.jda.core.Permission;

public abstract class GenericCommand implements Command {
    protected final Hilda hilda;

    private String name;

    private List<String> aliases;
    private String description;
    private Permission minimum_permission;

    protected boolean aliases_final = false;
    private boolean hide = false;

    protected GenericCommand(final Hilda hilda) {
        this.hilda = hilda;
    }

    @Override
    public List<String> getAliases() {
        return this.aliases;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets whether the command should be hidden from the help command.
     * @return Whether the command should be hidden from the help command.
     */
    public boolean getHide() {
        return this.hide;
    }

    @Override
    public Permission getMinimumPermission() {
        return this.minimum_permission;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     * Case insensitive.
     */
    @Override
    public boolean hasAlias(final String test) {
        if (this.aliases == null) {
            return false;
        }

        for (final String alias : this.aliases) {
            if (alias.equalsIgnoreCase(test)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * The aliases will be saved in an unmodifiable list. If there are no aliases to be recognised by this channel do not invoke this method.
     * <b>Aliases cannot be set again after they haves been set. As such, aliases should be set by the constructor.</b>
     * @throws RuntimeException If the aliases have already been set.
     */
    @Override
    public void setAliases(final List<String> aliases) {
        if (this.aliases_final) {
            throw new RuntimeException("Command already has aliases specified.");
        }

        this.aliases = Collections.unmodifiableList(aliases);
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Sets whether the command should be hidden from the help command.
     * @param hide Whether the command should be hidden from the help command.
     */
    public void setHide(final boolean hide) {
        this.hide = hide;
    }

    @Override
    public void setMinimumPermission(final Permission minimum_permission) {
        this.minimum_permission = minimum_permission;
    }

    /**
     * {@inheritDoc}
     * <b>The name cannot be set again after it has been set. As such, the name should be set by the constructor.</b>
     * @throws RuntimeException If the name has already been set.
     */
    @Override
    public void setName(final String name) {
        if (this.name != null) {
            throw new RuntimeException("Command already named.");
        }

        this.name = name;
    }

}
