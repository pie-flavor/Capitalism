package flavor.pie.capitalism;

import com.google.inject.Inject;
import flavor.pie.util.arguments.MoreArguments;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "capitalism", name = "Capitalism", version = "1.0.0-SNAPSHOT", authors = "pie_flavor", description = "A shops plugin.")
public class Capitalism {
    @Inject
    Game game;
    @Listener
    public void preInit(GamePreInitializationEvent e) {

    }
}
