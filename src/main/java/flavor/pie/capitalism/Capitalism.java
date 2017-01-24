package flavor.pie.capitalism;

import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin(id = "capitalism", name = "Capitalism", version = "1.0.0-SNAPSHOT", authors = "pie_flavor", description = "A shops plugin.")
public class Capitalism {
    @Inject
    Game game;
    Map<UUID, ShopCreationContainer> map = new HashMap<>();

    @Listener
    public void preInit(GamePreInitializationEvent e) {
        game.getDataManager().register(ShopData.class, ShopData.Immutable.class, new ShopData.Builder());
    }

    @Listener
    public void init(GameInitializationEvent e) {

    }

    private Player validatePlayer(CommandSource src) throws CommandException {
        if (src instanceof Player) {
            return (Player) src;
        } else {
            throw new CommandException(Text.of("You must be a player!"));
        }
    }

    public CommandResult newShop(CommandSource src, CommandContext args) throws CommandException {
        Player p = validatePlayer(src);
        map.put(p.getUniqueId(), new ShopCreationContainer());
        p.sendMessage(Text.of("Reset shop settings."));
        return CommandResult.success();
    }
}
