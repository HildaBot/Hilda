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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Sanity;
import ch.jamiete.hilda.Util;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandManager extends ListenerAdapter {
    /**
     * The command prefix that the bot will respond to.
     */
    public static final String PREFIX = "!";
    private final List<ChannelCommand> channelcommands;
    private final List<String> ignoredchannels;
    private boolean stopping = false;
    private final Hilda hilda;

    public CommandManager(final Hilda hilda) {
        this.hilda = hilda;

        this.channelcommands = new ArrayList<ChannelCommand>();
        this.ignoredchannels = new ArrayList<String>();
    }

    public void addIgnoredChannel(final String id) {
        if (!this.ignoredchannels.contains(id)) {
            this.ignoredchannels.add(id);
        }
    }

    /**
     * Removes all aliases from a {@link List} that are already registered.
     * @param aliases The {@link List} to clean.
     * @return The resultant {@link List}.
     * @throws IllegalArgumentException If the supplied object is null.
     */
    public List<String> cleanChannelAliases(final List<String> aliases) {
        Sanity.nullCheck(aliases, "Cannot supply null object.");

        final List<String> clean = new ArrayList<String>();

        for (final String alias : aliases) {
            if (!this.isChannelCommand(alias)) {
                clean.add(alias);
            }
        }

        return clean;
    }

    /**
     * Searches for the channel command that responds to the label. Case insensitive.
     * @param label The label to test for.
     * @return The {@link ChannelCommand} that responds to the label or {@code null} if no command responds to that label.
     */
    public ChannelCommand getChannelCommand(final String label) {
        for (final ChannelCommand command : this.channelcommands) {
            if (command.getName().equalsIgnoreCase(label) || command.hasAlias(label)) {
                return command;
            }
        }

        return null;
    }

    /**
     * Lists the commands registered.
     * @return A {@link ChannelCommand} array containing all registered channel commands.
     */
    public List<ChannelCommand> getChannelCommands() {
        return Collections.unmodifiableList(this.channelcommands);
    }

    public List<String> getIgnoredChannels() {
        return Collections.unmodifiableList(this.ignoredchannels);
    }

    /**
     * Checks whether a command responds to the label. Case insensitive.
     * @param label The label to test for.
     * @return Whether a command responds to the label.
     */
    public boolean isChannelCommand(final String label) {
        return this.getChannelCommand(label) != null;
    }

    public boolean isChannelIgnored(final String id) {
        return this.ignoredchannels.contains(id);
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        if (event.getAuthor() == this.hilda.getBot().getSelfUser() || this.stopping) {
            return;
        }

        final long start = System.currentTimeMillis();
        Hilda.getLogger().fine("Determining message \"" + event.getMessage().getRawContent() + "\" by " + event.getAuthor().getName() + "...");

        String[] args = event.getMessage().getRawContent().split(" ");

        if (args[0].length() > 0 && args[0].startsWith(CommandManager.PREFIX)) {
            String label;

            label = args[0].substring(1);
            args = Arrays.copyOfRange(args, 1, args.length);

            try {
                if (this.isChannelCommand(label)) {
                    Hilda.getLogger().info("Executing " + label + " for " + event.getMember().getEffectiveName() + " in " + event.getGuild().getName());
                    Hilda.getLogger().fine("    > Executing command " + label + "...");

                    final ChannelCommand command = this.getChannelCommand(label);

                    if (this.ignoredchannels.contains(event.getChannel().getId()) && !command.shouldTranscend(event.getMessage()) || !event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.ADMINISTRATOR)) {
                        Hilda.getLogger().fine("Ignoring message due to ignore override");
                        return;
                    }

                    // Check permissions
                    if (command.getMinimumPermission() != null && !event.getMember().hasPermission(event.getChannel(), command.getMinimumPermission())) {
                        event.getChannel().sendMessage("You don't have permission to use that command.");
                        Hilda.getLogger().fine("    > No permission.");
                    } else {
                        command.execute(event.getMessage(), args, label);
                    }

                    Hilda.getLogger().fine("    > Finished execution.");
                }
            } catch (final Exception e) {
                Hilda.getLogger().log(Level.WARNING, "Encountered an exception while executing " + label + " for " + event.getMember().getEffectiveName() + " in " + event.getGuild().getName(), e);
                event.getChannel().sendMessage("Something went wrong while executing that command.");
            }
        }

        Hilda.getLogger().fine("Finished handling message in " + (System.currentTimeMillis() - start) + "ms.");
    }

    /**
     * Registers a channel command. Registered commands will be invoked whenever their name or aliases are used in chat. The command's aliases will be cleaned ({@link #cleanChannelAliases(List)}) before it is registered. This means that aliases are first come, first served.
     * @param command The command to register.
     * @throws IllegalArgumentException If the command supplied is null, the command does not have a name, the command is already registered or the command name is already registered.
     */
    public void registerChannelCommand(final ChannelCommand command) {
        Sanity.nullCheck(command, "You must specify a command.");
        Sanity.nullCheck(command.getName(), "Command must be named.");
        Sanity.truthiness(!this.channelcommands.contains(command), "Cannot register duplicate command " + command.getName() + ".");
        Sanity.truthiness(!this.isChannelCommand(command.getName()), "Command name " + command.getName() + " is already registered.");

        if (command.getAliases() != null) {
            command.setAliases(this.cleanChannelAliases(command.getAliases()));
            command.aliases_final = true;
        }

        this.channelcommands.add(command);
        Hilda.getLogger().info("Registered channel command " + command.getName() + (command.getAliases() != null ? " (" + Util.combineSplit(0, command.getAliases().toArray(new String[command.getAliases().size()]), ", ").trim() + ")" : ""));
    }

    public void removeIgnoredChannel(final String id) {
        this.ignoredchannels.remove(id);
    }

    public void shutdown() {
        this.stopping = true;
    }

}