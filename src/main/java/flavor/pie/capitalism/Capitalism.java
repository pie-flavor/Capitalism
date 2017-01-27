package flavor.pie.capitalism;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import flavor.pie.util.arguments.MoreArguments;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Plugin(id = "capitalism", name = "Capitalism", version = "1.0.0-SNAPSHOT", authors = "pie_flavor", description = "A shops plugin.")
public class Capitalism {
    @Inject
    Game game;
    @Inject
    PluginContainer container;
    @Inject
    Logger logger;
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
                .executor(this::addSellPrice)
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

        game.getCommandManager().register(this, shop, "shop");
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
        EconomyService svc = game.getServiceManager().provideUnchecked(EconomyService.class);
        Currency currency = args.<Currency>getOne("currency").orElse(svc.getDefaultCurrency());
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
        EconomyService svc = game.getServiceManager().provideUnchecked(EconomyService.class);
        Currency currency = args.<Currency>getOne("currency").orElse(svc.getDefaultCurrency());
        BigDecimal amount = args.<BigDecimal>getOne("amount").get();
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CommandException(Text.of("Amount cannot be negative!"));
        } else if (amount.compareTo(BigDecimal.ZERO) == 0) {
            map.get(p.getUniqueId()).sellPrice.remove(currency);
            src.sendMessage(Text.of("Removed currency ", currency.getPluralDisplayName(), " from the selling price."));
            return CommandResult.success();
        } else {
            map.get(p.getUniqueId()).sellPrice.put(currency, amount);
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

    Map<Location<World>, ItemStackSnapshot> cachedLocs = new HashMap<>();

    @Listener
    public void interact(InteractBlockEvent.Secondary.MainHand e, @First Player p) {
        if (!(e.getTargetBlock() == BlockSnapshot.NONE)) {
            Location<World> block = e.getTargetBlock().getLocation().get();
            EconomyService svc = game.getServiceManager().provideUnchecked(EconomyService.class);
            if (testShop(p, block)) {
                ShopData data = block.get(ShopData.class).get();
                if (data.getSellPrice().isEmpty()) {
                    return;
                }
                if (data.getOwner().equals(p.getUniqueId()) && !data.isAdmin()) {
                    //TODO test functionality
                    return;
                }
                if (testHeldItem(p, data)) {
                    if (data.isAdmin()) {
                        e.setUseItemResult(Tristate.FALSE);
                        UniqueAccount acct = svc.getOrCreateAccount(p.getUniqueId()).get();
                        List<Currency> currencies = new ArrayList<>();
                        for (Currency currency : data.getSellPrice().keySet()) {
                            TransactionResult res = acct.deposit(currency, data.getSellPrice().get(currency), Cause.source(container).build());
                            if (res.getResult() != ResultType.SUCCESS) {
                                for (Currency currency2 : currencies) {
                                    acct.withdraw(currency2, data.getSellPrice().get(currency2), Cause.source(container).build());
                                }
                                p.sendMessage(Text.of("Unable to give you ", currency.format(data.getSellPrice().get(currency)), "!"));
                                e.setCancelled(true);
                                return;
                            } else {
                                currencies.add(currency);
                            }
                        }
                        ItemStack sold = p.getItemInHand(HandTypes.MAIN_HAND).get();
                        if (sold.getQuantity() == data.getAmount()) {
                            p.setItemInHand(HandTypes.MAIN_HAND, null);
                        } else {
                            sold.setQuantity(sold.getQuantity() - data.getAmount());
                            p.setItemInHand(HandTypes.MAIN_HAND, sold);
                        }
                    } else {
                        //                        Location<World> chest = block.getRelative(block.get(Keys.DIRECTION).get().getOpposite());
                        //                        if (testShopChest(p, block, chest)) {
                        //                            Inventory inv = ((Carrier) block.getRelative(block.get(Keys.DIRECTION).get().getOpposite()).getTileEntity().get()).getInventory();
                        //                            e.setUseItemResult(Tristate.FALSE);
                        //                            ItemStack sold = p.getItemInHand(HandTypes.MAIN_HAND).get();
                        //                            ItemStack test = sold.copy();
                        //                            test.setQuantity(data.getAmount());
                        //                            if (!attemptInsertion(p, sold, inv)) {
                        //                                e.setCancelled(true);
                        //                                return;
                        //                            }
                        //                            UniqueAccount acct = svc.getOrCreateAccount(p.getUniqueId()).get();
                        //                            UniqueAccount acct2 = svc.getOrCreateAccount(data.getOwner()).get();
                        //                            List<Currency> currencies = new ArrayList<>();
                        //                            for (Currency currency : data.getSellPrice().keySet()) {
                        //                                TransactionResult res = acct2.transfer(acct, currency, data.getSellPrice().get(currency), Cause.source(container).build());
                        //                                if (res.getResult() != ResultType.SUCCESS) {
                        //                                    for (Currency currency2 : currencies) {
                        //                                        acct.transfer(acct2, currency, data.getSellPrice().get(currency), Cause.source(container).build());
                        //                                    }
                        //                                    p.sendMessage(Text.of("Unable to give you ", currency.format(data.getSellPrice().get(currency)), "!"));
                        //                                    e.setCancelled(true);
                        //                                    return;
                        //                                } else {
                        //                                    currencies.add(currency);
                        //                                }
                        //                            }
                        //                            if (sold.getQuantity() == data.getAmount()) {
                        //                                p.setItemInHand(HandTypes.MAIN_HAND, null);
                        //                            } else {
                        //                                sold.setQuantity(sold.getQuantity() - data.getAmount());
                        //                                p.setItemInHand(HandTypes.MAIN_HAND, sold);
                        //                            }
                        //                            p.sendMessage(Text.of("Sold ", data.getAmount(), "x", data.getItem(), " for ", Text.of(currencies.stream().map(c -> c.format(data.getSellPrice().get(c))).toArray())));
                        //                        }
                    }
                }
            }
            if (p.getItemInHand(e.getHandType()).map(f -> f.get(ShopData.class)).isPresent() && p.getItemInHand(e.getHandType()).get().getItem().equals(ItemTypes.SIGN)) {
                Location<World> loc = block.getBlockRelative(e.getTargetSide());
                cachedLocs.put(loc, p.getItemInHand(e.getHandType()).get().createSnapshot());
            }

        }
    }
    @Listener
    public void interact(InteractBlockEvent.Primary e, @First Player p) {
        if (!(e.getTargetBlock() == BlockSnapshot.NONE)) {
            Location<World> block = e.getTargetBlock().getLocation().get();
            if (testShop(p, block)) {
                ShopData data = block.get(ShopData.class).get();
                if (data.getBuyPrice().isEmpty()) {
                    return;
                }
                if (p.get(Keys.IS_SNEAKING).get()) {
                    EconomyService svc = game.getServiceManager().provideUnchecked(EconomyService.class);
                    if (data.getOwner().equals(p.getUniqueId()) && !data.isAdmin()) {
                        //TODO test functionality
                        return;
                    }
                    if (data.isAdmin()) {
                        ItemStack bought = data.getItem().copy();
                        bought.setQuantity(data.getAmount());
                        UniqueAccount acct = svc.getOrCreateAccount(p.getUniqueId()).get();
                        List<Currency> currencies = new ArrayList<>();
                        for (Currency currency : data.getBuyPrice().keySet()) {
                            TransactionResult res = acct.withdraw(currency, data.getBuyPrice().get(currency), Cause.source(container).build());
                            if (res.getResult() != ResultType.SUCCESS) {
                                for (Currency currency2 : currencies) {
                                    acct.deposit(currency2, data.getSellPrice().get(currency2), Cause.source(container).build());
                                }
                                p.sendMessage(Text.of("Unable to pay ", currency.format(data.getSellPrice().get(currency)), "!"));
                                e.setCancelled(true);
                                return;
                            } else {
                                currencies.add(currency);
                            }
                        }
                        InventoryTransactionResult res = p.getInventory().offer(bought);
                        if (!res.getRejectedItems().isEmpty()) {
                            for (ItemStackSnapshot snap : res.getRejectedItems()) {
                                Item item = (Item) p.getWorld().createEntity(EntityTypes.ITEM, p.getLocation().getPosition());
                                item.offer(Keys.REPRESENTED_ITEM, snap);
                                p.getWorld().spawnEntity(item, Cause.source(EntitySpawnCause.builder().entity(item).type(SpawnTypes.PLUGIN).build()).build());
                                p.sendMessage(Text.of("Not enough space in your inventory to fit the items!"));
                            }
                        }
                        p.sendMessage(Text.of("Bought ", data.getAmount(), "x", data.getItem(), " for ", Text.of(currencies.stream().map(c -> c.format(data.getBuyPrice().get(c))).toArray())));
                    } else {
                    }
                }
            }
        }
    }

    private boolean testShop(Player p, Location<World> sign) {
        return sign.get(ShopData.class).isPresent() && sign.getBlockType().equals(BlockTypes.WALL_SIGN);
    }

    private boolean testShopChest(Player p, Location<World> sign, Location<World> chest) {
        Optional<TileEntity> tile_ = chest.getTileEntity();
        if (!tile_.isPresent()) {
            return false;
        }
        TileEntity tile = tile_.get();
        return tile instanceof Carrier;
    }

    private boolean testHeldItem(Player p, ShopData data) {
        Optional<ItemStack> stack_ = p.getItemInHand(HandTypes.MAIN_HAND);
        if (!stack_.isPresent()) {
            return false;
        }
        ItemStack stack = stack_.get();
        if (stack.getQuantity() < data.getAmount()) {
            p.sendMessage(Text.of("You don't have enough items to sell here!"));
            return false;
        }
        ItemStack test = data.getItem().copy();
        test.setQuantity(stack.getQuantity());
        if (!test.equalTo(stack)) {
            p.sendMessage(Text.of("The item is of an invalid type!"));
            return false;
        }
        return true;
    }

    private boolean attemptInsertion(Player p, ItemStack stack, Inventory inv) {
        InventoryTransactionResult result = inv.offer(stack);
        if (!result.getRejectedItems().isEmpty()) {
            p.sendMessage(Text.of("The storage does not have enough space!"));
            return false;
        }
        return true;
    }

    @Listener
    public void change(ChangeSignEvent e) {
        Sign sign = e.getTargetTile();
        if (cachedLocs.containsKey(sign.getLocation())) {
            ItemStackSnapshot itemSnap = cachedLocs.remove(sign.getLocation());
            if (!sign.getLocation().getBlockType().equals(BlockTypes.WALL_SIGN)) {
                return;
            }
            ShopData.Immutable data = itemSnap.get(ShopData.Immutable.class).get();
            Text user = data.isAdmin() ? Text.of(TextColors.BLUE, "Admin Shop") : Text.of(TextColors.BLUE, game.getServiceManager().provideUnchecked(UserStorageService.class).get(data.getOwner()).get().getName());
            Text line3;
            Text line4;
            int buySize = data.getBuyPrice().size();
            int sellSize = data.getSellPrice().size();
            Map.Entry<Currency, BigDecimal> buy = buySize == 1 ? data.getBuyPrice().entrySet().iterator().next() : null;
            Map.Entry<Currency, BigDecimal> sell = sellSize == 1 ? data.getSellPrice().entrySet().iterator().next() : null;
            if (buySize > 1) {
                if (sellSize > 1) {
                    line3 = Text.of("Right-click sign");
                    line4 = Text.of("for price details.");
                } else if (sellSize == 0) {
                    line3 = Text.of(TextColors.GREEN, "Right-click sign");
                    line4 = Text.of(TextColors.GREEN, "for buy details.");
                } else {
                    line3 = Text.of(TextColors.GREEN, "R-Click for info");
                    line4 = Text.of(TextColors.RED, sell.getKey().format(sell.getValue()));
                }
            } else if (buySize == 0) {
                if (sellSize > 1) {
                    line3 = Text.of(TextColors.RED, "Right-click sign");
                    line4 = Text.of(TextColors.RED, "for sell details.");
                } else if (sellSize == 0) {
                    line3 = Text.of("This sign seems");
                    line4 = Text.of("to have borked.");
                } else {
                    line3 = Text.of(TextColors.RED, "Sell price:");
                    line4 = Text.of(TextColors.RED, sell.getKey().format(sell.getValue()));
                }
            } else {
                if (sellSize > 1) {
                    line3 = Text.of(TextColors.GREEN, buy.getKey().format(buy.getValue()));
                    line4 = Text.of(TextColors.RED, "R-Click for info");
                } else if (sellSize == 0) {
                    line3 = Text.of(TextColors.GREEN, "Buy price:");
                    line4 = Text.of(TextColors.GREEN, buy.getKey().format(buy.getValue()));
                } else {
                    line3 = Text.of(TextColors.GREEN, buy.getKey().format(buy.getValue()));
                    line4 = Text.of(TextColors.RED, sell.getKey().format(sell.getValue()));
                }
            }
            Task.builder()
                    .delayTicks(1)
                    .execute(() -> {
                        SignData signData = sign.getOrCreate(SignData.class).get();
                        signData.setElements(ImmutableList.of(user,  Text.of(data.getAmount(), "x", data.getItem()), line3, line4));
                        sign.offer(signData);
                        sign.offer(data.asMutable());
                    }).submit(this);
        }
    }

}
