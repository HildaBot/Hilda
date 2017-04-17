package ch.jamiete.hilda.commands;

import ch.jamiete.hilda.Hilda;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

public abstract class ChannelCommand extends GenericCommand {

    protected ChannelCommand(final Hilda hilda) {
        super(hilda);
    }

    /**
     * Called whenever the channel command has been invoked.
     * @param message The message that the channel command was invoked in.
     * @param arguments A {@link String} array of the arguments passed to the command. Does not include the label.
     * @param label The label (could be an alias or the name) that was used to invoke the command.
     */
    public abstract void execute(Message message, String[] arguments, String label);

    /**
     * Called whenever the bot commences shutting down.
     */
    public void onShutdown() {

    }

    /**
     * Called whenever the bot has finished starting up.
     */
    public void onStartup() {

    }

    /**
     * Sends a reply to the channel in which the message was received.
     * @param received The message to be replied to.
     * @param outgoing The message to be sent.
     */
    protected void reply(final Message received, final Message outgoing) {
        received.getChannel().sendMessage(outgoing).queue();
    }

    /**
     * Sends a reply to the channel in which the message was received.
     * @param received The message to be replied to.
     * @param outgoing The {@link MessageEmbed} to be sent.
     */
    protected void reply(final Message received, final MessageEmbed outgoing) {
        received.getChannel().sendMessage(outgoing).queue();
    }

    /**
     * Sends a reply to the channel in which the message was received.
     * @param received The message to be replied to.
     * @param outgoing The message to be sent.
     */
    protected void reply(final Message received, final String outgoing) {
        received.getChannel().sendMessage(outgoing).queue();
    }

    /**
     * Sends a reply to the channel in which the message was received that the argument was not recognised. <br>
     * <b>If the command contains aliases a warning will be logged that {@link #usage(Message, String, String)} should instead be used.</b>
     * @param received The message to be replied to.
     * @param params The parameters that the command recognises and can respond to.
     */
    protected void usage(final Message received, final String params) {
        if (this.getAliases() != null) {
            Hilda.getLogger().warning(this.getName() + " has aliases but called #usage without providing the label called.");
        }

        this.usage(received, params, this.getName());
    }

    /**
     * Sends a reply to the channel in which the message was received that the argument was not recognised and shows the label that was used.
     * @param received The message to be replied to.
     * @param params The parameters that the command recognises and can respond to.
     * @param label The label that was used to invoke the command
     */
    protected void usage(final Message received, final String params, final String label) {
        this.reply(received, "Incorrect usage. " + CommandManager.PREFIX + label + " " + params);
    }

}
