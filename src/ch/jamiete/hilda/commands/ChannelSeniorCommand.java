package ch.jamiete.hilda.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import ch.jamiete.hilda.Hilda;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.MessageBuilder.Formatting;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public abstract class ChannelSeniorCommand extends ChannelCommand {
    private final List<ChannelCommand> subcommands = new ArrayList<ChannelCommand>();

    protected ChannelSeniorCommand(final Hilda hilda) {
        super(hilda);
    }

    /**
     * Determines which subcommand to run, or sends a help message
     */
    @Override
    public void execute(final Message message, final String[] args, final String label) {
        final Member member = message.getGuild().getMember(message.getAuthor());

        if (args.length == 0) {
            this.help(message.getTextChannel(), member);
            return;
        }

        final ChannelCommand command = this.subcommands.stream().filter(c -> c.getName().equalsIgnoreCase(args[0]) || c.hasAlias(args[0])).findFirst().orElse(null);

        if (command == null) {
            this.help(message.getTextChannel(), member);
            return;
        }

        if (command.getMinimumPermission() != null && !member.hasPermission(message.getTextChannel(), command.getMinimumPermission())) {
            this.reply(message, "You don't have permission to use that command");
            return;
        }

        Hilda.getLogger().fine("Executing subcommand " + command.getName());
        command.execute(message, Arrays.copyOfRange(args, 1, args.length), args[0]);
    }

    /**
     * Gets the currently-registered subcommands.
     * @return The currently-registered subcommands in an unmodifiable list
     */
    public List<ChannelCommand> getSubcommands() {
        return Collections.unmodifiableList(this.subcommands);
    }

    /**
     * Sends a message detailing the subcommands of this command.
     * @param channel
     */
    public void help(final TextChannel channel, final Member member) {
        final MessageBuilder mb = new MessageBuilder();

        mb.append(StringUtils.capitalize(this.getName()) + " Help", Formatting.UNDERLINE);
        mb.append("\n");
        mb.append(this.getDescription(), Formatting.ITALICS);
        mb.append("\n");
        mb.append("Use ").append(CommandManager.PREFIX + this.getName() + " <command>", Formatting.BOLD);
        mb.append(" to use this command:");

        for (final ChannelCommand subcommand : this.subcommands) {
            if (subcommand.getHide() || subcommand.getMinimumPermission() != null && !member.hasPermission(channel, subcommand.getMinimumPermission())) {
                continue;
            }

            mb.append("\n  ");
            mb.append(subcommand.getName(), Formatting.BOLD);
            mb.append(" — ");
            mb.append(subcommand.getDescription());
        }

        channel.sendMessage(mb.build()).queue();
    }

    /**
     * Registers a subcommand to this senior command manager.
     * @param subcommand The subcommand to register
     * @return Whether the command was registered
     */
    public boolean registerSubcommand(final ChannelCommand subcommand) {
        if (this.subcommands.contains(subcommand)) {
            return false;
        }

        return this.subcommands.add(subcommand);
    }

}
