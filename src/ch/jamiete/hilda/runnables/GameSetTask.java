package ch.jamiete.hilda.runnables;

import ch.jamiete.hilda.Hilda;
import net.dv8tion.jda.core.entities.Game;

public class GameSetTask implements Runnable {
    private final Hilda hilda;
    private final String name;

    public GameSetTask(final Hilda hilda, final String name) {
        this.hilda = hilda;
        this.name = name;
    }

    @Override
    public void run() {
        Hilda.getLogger().fine("Setting game to " + this.name);
        this.hilda.getBot().getPresence().setGame(this.name == null ? null : Game.of(this.name));
    }

}
