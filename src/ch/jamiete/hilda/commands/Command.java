package ch.jamiete.hilda.commands;

import java.util.List;
import net.dv8tion.jda.core.Permission;

public interface Command {

    /**
     * Gets a list of the aliases this command should reply to.
     * @return The list of aliases.
     */
    public List<String> getAliases();

    /**
     * Gets the description of this command.
     * @return The command description.
     */
    public String getDescription();

    /**
     * Gets the minimum permission required to use this command.
     * @return The minimum permission.
     */
    public Permission getMinimumPermission();

    /**
     * Gets the main label that this command responds to.
     * @return The main command label.
     */
    public String getName();

    /**
     * Gets whether or not the specified alias is registered with this command.
     * @param alias The alias to be tested.
     * @return Whether the alias is registered with this command.
     */
    public boolean hasAlias(String alias);

    /**
     * Sets the aliases this command should respond to.
     * @param aliases A list of the aliases the command should respond to.
     */
    public void setAliases(List<String> aliases);

    /**
     * Sets the description of the command.
     * @param description The description of the command.
     */
    public void setDescription(String description);

    /**
     * Sets the minimum permission required to use this command.
     * @param minimum_permission The minimum permission required to use the command.
     */
    public void setMinimumPermission(Permission minimum_permission);

    /**
     * Sets the main label the command should respond to.
     * @param name The main label the command should respond to.
     */
    public void setName(String name);

}