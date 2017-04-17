package ch.jamiete.hilda.runnables;

import net.dv8tion.jda.core.entities.Message;

public class MessageDeletionTask implements Runnable {
    private final Message message;

    public MessageDeletionTask(final Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        this.message.delete().queue();
    }

}
