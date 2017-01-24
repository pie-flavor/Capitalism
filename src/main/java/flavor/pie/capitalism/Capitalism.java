package flavor.pie.capitalism;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import flavor.pie.util.arguments.MoreArguments;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
        CommandSpec newShop = CommandSpec.builder().executor(this::newShop).build();
        CommandSpec setItem = CommandSpec.builder()
                .executor(this::setItem)
                .arguments(
                        GenericArguments.optional(GenericArguments.integer(Text.of("amount")), 1)
                ).build();
        CommandSpec addBuyPrice = CommandSpec.builder()
                .executor(this::addBuyPrice)
                .arguments(
                        MoreArguments.bigDecimal(Text.of("amount")),
                        GenericArguments.optional(GenericArguments.catalogedElement(Text.of("currency"), Currency.class))
                ).build();
        CommandSpec addSellPrice = CommandSpec.builder()
                .executor(this::addBuyPrice)
                .arguments(
                        MoreArguments.bigDecimal(Text.of("amount")),
                        GenericArguments.optional(GenericArguments.catalogedElement(Text.of("currency"), Currency.class))
                ).build();
        CommandSpec admin = CommandSpec.builder()
                .executor(this::admin)
                .permission("capitalism.admin")
                .build();
        CommandSpec apply = CommandSpec.builder().executor(this::apply).build();

        CommandSpec shop = CommandSpec.builder()
                .child(newShop, "new", "+")
                .child(addBuyPrice, "buy", ">")
                .child(addSellPrice, "sell", "<")
                .child(setItem, "item", "?")
                .child(admin, "admin", "@")
                .child(apply, "apply", "!")
                .build();
    }

    private Player validatePlayer(CommandSource src) throws CommandException {
        if (src instanceof Player) {
            return (Player) src;
        } else {
            throw new CommandException(Text.of("You must be a player!"));
        }
    }

    private Player validateState(Player p) throws CommandException {
        if (!map.containsKey(p.getUniqueId())) {
            throw new CommandException(Text.of("You must run ", TextColors.GRAY, "/shop new", TextColors.WHITE, " before configuring the shop!"));
        } else {
            return p;
        }
    }

    public CommandResult newShop(CommandSource src, CommandContext args) throws CommandException {
        Player p = validatePlayer(src);
        map.put(p.getUniqueId(), new ShopCreationContainer());
        p.sendMessage(Text.of("Reset shop settings."));
        return CommandResult.success();
    }

    public CommandResult setItem(CommandSource src, CommandContext args) throws CommandException {
        Player p = validateState(validatePlayer(src));
        int amount = args.<Integer>getOne("amount").get();
        if (amount < 1) {
            throw new CommandException(Text.of("Amount must be positive!"));
        }
        ItemStack stack = p.getItemInHand(HandTypes.MAIN_HAND)
                .orElse(p.getItemInHand(HandTypes.OFF_HAND)
                        .orElseThrow(() -> new CommandException(Text.of("You must be holding an item!"))));
        int max = stack.getItem().getMaxStackQuantity();
        if (amount > max) {
            throw new CommandException(Text.of("You cannot buy or sell more than ", max, "x", stack, " at once!"));
        }
        ItemStack copy = stack.copy();
        ShopCreationContainer cont = map.get(p.getUniqueId());
        cont.item = copy;
        cont.amount = amount;
        p.sendMessage(Text.of("Set the sold item to ", amount, "x", copy, "."));
        return CommandResult.success();
    }

    public CommandResult addBuyPrice(CommandSource src, CommandContext args) throws CommandException {
        Player p = validateState(validatePlayer(src));
        Currency currency = args.<Currency>getOne("currency").get();
        BigDecimal amount = args.<BigDecimal>getOne("amount").get();
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            map.get(p.getUniqueId()).buyPrice.remove(currency);
            src.sendMessage(Text.of("Removed currency ", currency.getPluralDisplayName(), " from the buying price."));
            return CommandResult.success();
        } else {
            map.get(p.getUniqueId()).buyPrice.put(currency, amount);
            src.sendMessage(Text.of("Set the buying price for ", currency.getPluralDisplayName(), " to ", currency.format(amount), "."));
            return CommandResult.success();
        }
    }

    public CommandResult addSellPrice(CommandSource src, CommandContext args) throws CommandException {
        Player p = validateState(validatePlayer(src));
        Currency currency = args.<Currency>getOne("currency").get();
        BigDecimal amount = args.<BigDecimal>getOne("amount").get();
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CommandException(Text.of("Amount cannot be negative!"));
        } else if (amount.compareTo(BigDecimal.ZERO) == 0) {
            map.get(p.getUniqueId()).buyPrice.remove(currency);
            src.sendMessage(Text.of("Removed currency ", currency.getPluralDisplayName(), " from the selling price."));
            return CommandResult.success();
        } else {
            map.get(p.getUniqueId()).buyPrice.put(currency, amount);
            src.sendMessage(Text.of("Set the selling price for ", currency.getPluralDisplayName(), " to ", currency.format(amount), "."));
            return CommandResult.success();
        }
    }

    public CommandResult admin(CommandSource src, CommandContext args) throws CommandException {
        Player p = validateState(validatePlayer(src));
        ShopCreationContainer cont = map.get(p.getUniqueId());
        cont.admin = !cont.admin;
        p.sendMessage(Text.of("Toggled admin to ", cont.admin, "."));
        return CommandResult.success();
    }

    public CommandResult apply(CommandSource src, CommandContext args) throws CommandException {
        Player p = validateState(validatePlayer(src));
        ShopCreationContainer cont = map.get(p.getUniqueId());
        if (cont.buyPrice.isEmpty() && cont.sellPrice.isEmpty()) {
            throw new CommandException(Text.of("You have no prices set!"));
        }
        if (cont.item == null) {
            throw new CommandException(Text.of("You have no item set!"));
        }
        ItemStack stack;
        HandType hand;
        Optional<ItemStack> stack_ = p.getItemInHand(hand = HandTypes.MAIN_HAND);
        if (!stack_.isPresent() || !(stack = stack_.get()).getItem().equals(ItemTypes.SIGN)) {
            stack_ = p.getItemInHand(hand = HandTypes.OFF_HAND);
            if (!stack_.isPresent() || !(stack = stack_.get()).getItem().equals(ItemTypes.SIGN)) {
                throw new CommandException(Text.of("You need to be holding one or more ", ItemTypes.SIGN, "s!"));
            }
        }
        ShopData data = stack.getOrCreate(ShopData.class).get();
        data.setItem(cont.item);
        data.setAmount(cont.amount);
        data.setBuyPrice(cont.buyPrice);
        data.setSellPrice(cont.sellPrice);
        data.setOwner(p.getUniqueId());
        data.setAdmin(cont.admin);
        stack.offer(data);
        stack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Shop Sign"));
        stack.offer(Keys.ITEM_LORE, ImmutableList.of(Text.of(TextColors.GRAY, cont.item.getItem().getId())));
        p.setItemInHand(hand, stack);
        p.sendMessage(Text.of("Converted your held signs to shop signs."));
        return CommandResult.success();
    }
}
