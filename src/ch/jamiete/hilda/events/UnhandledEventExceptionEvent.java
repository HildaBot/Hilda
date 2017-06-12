package ch.jamiete.hilda.events;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.Event;

public class UnhandledEventExceptionEvent extends Event {
    private final Throwable throwable;
    private final Event event;

    public UnhandledEventExceptionEvent(JDA api, long responseNumber, Throwable throwable, Event event) {
        super(api, responseNumber);

        this.throwable = throwable;
        this.event = event;
    }

    /**
     * Get the throwable that was thrown.
     * @return
     */
    public Throwable getThrowable() {
        return this.throwable;
    }

    /**
     * Get the event that was handled with an exception.
     * @return
     */
    public Event getEvent() {
        return this.event;
    }
}
