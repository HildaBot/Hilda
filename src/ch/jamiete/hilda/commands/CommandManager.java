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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.Sanity;
import ch.jamiete.hilda.Util;
import ch.jamiete.hilda.events.EventHandler;
import ch.jamiete.hilda.runnables.CommandCleanupTask;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandManager {
    /**
     * The command prefix that the bot will respond to.
     */
    public static final String PREFIX = "!";
    private final List<ChannelCommand> channelCommands;
    private final List<String> ignoredChannels, ignoredUsers;
    private int executions = 0;
    private boolean stopping = false;
    private final Hilda hilda;

    public CommandManager(final Hilda hilda) {
        this.hilda = hilda;

        this.channelCommands = new ArrayList<>();
        this.ignoredChannels = new ArrayList<>();
        this.ignoredUsers = new ArrayList<>();

        this.hilda.getExecutor().scheduleWithFixedDelay(new CommandCleanupTask(this), 10, 10, TimeUnit.MINUTES);
    }

    public void addIgnoredChannel(final String id) {
        if (!this.ignoredChannels.contains(id)) {
            this.ignoredChannels.add(id);
        }
    }

    public void addIgnoredUser(final String id) {
        if (!this.ignoredUsers.contains(id)) {
            this.ignoredUsers.add(id);
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

        final List<String> temp = new ArrayList<>();
        aliases.stream().filter(a -> !this.isChannelCommand(a)).forEach(temp::add);

        return temp;
    }

    /**
     * Searches for the channel command that responds to the label. Case insensitive.
     * @param label The label to test for.
     * @return The {@link ChannelCommand} that responds to the label or {@code null} if no command responds to that label.
     */
    public ChannelCommand getChannelCommand(final String label) {
        return this.channelCommands.stream().filter(c -> c.getName().equalsIgnoreCase(label) || c.hasAlias(label)).findFirst().orElse(null);
    }

    /**
     * Lists the commands registered.
     * @return A {@link ChannelCommand} array containing all registered channel commands.
     */
    public List<ChannelCommand> getChannelCommands() {
        return Collections.unmodifiableList(this.channelCommands);
    }

    public int getExecutions() {
        return this.executions;
    }

    public List<String> getIgnoredChannels() {
        return Collections.unmodifiableList(this.ignoredChannels);
    }

    public List<String> getIgnoredUsers() {
        return Collections.unmodifiableList(this.ignoredUsers);
    }

    /**
     * Checks whether a command responds to the label. Case insensitive.
     * @param label The label to test for.
     * @return Whether a command responds to the label.
     */
    public boolean isChannelCommand(final String label) {
        return this.getChannelCommand(label) != null;
    }

    /**
     * Checks whether a command has the label registered as an alias. Case insensitive.
     * @param label The label to test for.
     * @return Whether a command has the label registered as an alias.
     */
    public boolean isChannelCommandAlias(final String label) {
        return this.channelCommands.stream().anyMatch(c -> c.hasAlias(label));
    }

    public boolean isChannelIgnored(final String id) {
        return this.ignoredChannels.contains(id);
    }

    public boolean isUserIgnored(final String id) {
        return this.ignoredChannels.contains(id);
    }

    @EventHandler
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        if (event.getAuthor() == this.hilda.getBot().getSelfUser() || this.stopping) {
            return;
        }

        if (this.ignoredUsers.contains(event.getAuthor().getId())) {
            return;
        }

        final String content = event.getMessage().getContentRaw();

        final long start = System.currentTimeMillis();
        Hilda.getLogger().fine("Determining message \"" + content + "\" by " + event.getAuthor().getName() + "...");

        final String[] temp_args = content.split(" ");

        if (temp_args[0].length() > 0 && temp_args[0].startsWith(CommandManager.PREFIX)) {
            final String label = temp_args[0].substring(1);

            if (!this.isChannelCommand(label)) {
                return;
            }

            final String[] args = Arrays.copyOfRange(temp_args, 1, temp_args.length);
            final ChannelCommand command = this.getChannelCommand(label);

            if (!event.getChannel().canTalk()) {
                event.getAuthor().openPrivateChannel().queue(channel -> {
                    MessageBuilder mb = new MessageBuilder();
                    mb.append("I can't run your command in ");
                    mb.append("#" + event.getChannel().getName(), MessageBuilder.Formatting.BOLD);
                    mb.append(" on ").append(event.getGuild().getName(), MessageBuilder.Formatting.BOLD);
                    mb.append(" because I don't have permission to speak in that channel. Please ask an administrator or the owner (");
                    mb.append(event.getGuild().getOwner().getAsMention()).append(") to grant me the appropriate permissions.");
                    channel.sendMessage(mb.build());
                }, failure -> {
                });

                return;
            }

            this.executions++;

            final Runnable execute = () -> {
                try {
                    Hilda.getLogger().info("Executing " + label + " for " + Util.getName(event.getAuthor()) + " (" + event.getAuthor().getId() + ") in " + event.getGuild().getName() + " (" + event.getGuild().getId() + ")");
                    Hilda.getLogger().fine("    > Executing command " + label + "...");

                    if (this.ignoredChannels.contains(event.getChannel().getId()) && !command.shouldTranscend(event.getMessage())) {
                        Hilda.getLogger().fine("Ignoring message due to ignore override");
                        return;
                    }

                    if (command.isServerLocked() && !command.matchesLock(event.getMessage())) {
                        Hilda.getLogger().fine("Ignoring message due to server lock");
                        return;
                    }

                    // Check permissions
                    if (command.getMinimumPermission() != null && !event.getMember().hasPermission(event.getChannel(), command.getMinimumPermission())) {
                        event.getChannel().sendMessage("You don't have permission to use that command.").queue();
                        Hilda.getLogger().fine("    > No permission.");
                    } else {
                        if (command.canExecute(event.getAuthor().getId())) {
                            command.markExecuted(event.getAuthor().getId());
                            command.execute(event.getMessage(), args, label);
                        } else {
                            event.getChannel().sendMessage("Slow down! You must wait at least " + command.getTimeout() + "s between command invocations.").queue(Util.deleteAfter(5));
                        }
                    }

                    Hilda.getLogger().fine("    > Finished execution.");
                } catch (final Exception e) {
                    Hilda.getLogger().log(Level.WARNING, "Encountered an exception while executing " + label + " for " + event.getMember().getEffectiveName() + " in " + event.getGuild().getName(), e);
                    event.getChannel().sendMessage("Something went wrong while executing that command.").queue();
                }
            };

            if (command.isAsync()) {
                new Thread(execute).start();
                Hilda.getLogger().fine("Passed message off to an async thread.");
            } else {
                execute.run();
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
        Sanity.falsiness(this.channelCommands.contains(command), "Cannot register duplicate command " + command.getName() + ".");
        Sanity.falsiness(this.isChannelCommand(command.getName()) && !this.isChannelCommandAlias(command.getName()), "Command name " + command.getName() + " is already registered.");

        if (this.isChannelCommand(command.getName()) && this.isChannelCommandAlias(command.getName())) {
            final ChannelCommand temp = this.getChannelCommand(command.getName());
            temp.aliases.remove(command.getName().toLowerCase());
            Hilda.getLogger().info("The alias " + command.getName() + " was removed from the command " + command.getName() + " to make way for another command.");
        }

        if (command.getAliases() != null && !command.getAliases().isEmpty()) {
            command.setAliases(this.cleanChannelAliases(command.getAliases()));
            command.aliasesFinal = true;
        }

        this.channelCommands.add(command);
        Hilda.getLogger().info("Registered channel command " + command.getName() + (!command.getAliases().isEmpty() ? " (" + Util.combineSplit(0, command.getAliases().toArray(new String[command.getAliases().size()]), ", ").trim() + ")" : ""));
    }

    public void removeIgnoredChannel(final String id) {
        this.ignoredChannels.remove(id);
    }

    public void removeIgnoredUser(final String id) {
        this.ignoredUsers.remove(id);
    }

    public void shutdown() {
        this.stopping = true;
    }

}