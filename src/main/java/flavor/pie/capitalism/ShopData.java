package flavor.pie.capitalism;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.immutable.ImmutableMapValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.util.Identifiable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ShopData extends AbstractData<ShopData, ShopData.Immutable> {
    public final static UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    Map<Currency, BigDecimal> sellPrice;
    int amount;
    Map<Currency, BigDecimal> buyPrice;
    boolean admin;
    UUID owner;
    {
        registerGettersAndSetters();
    }

    ShopData() {
        sellPrice = new HashMap<>();
        buyPrice = new HashMap<>();
        amount = 0;
        admin = true;
        owner = ZERO_UUID;
    }

    ShopData(Map<Currency, BigDecimal> sellPrice, Map<Currency, BigDecimal> buyPrice, int amount, boolean admin, UUID owner) {
        this.sellPrice = Maps.newHashMap(sellPrice);
        this.amount = amount;
        this.buyPrice = Maps.newHashMap(buyPrice);
        this.admin = admin;
        this.owner = owner;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(ShopKeys.SELL_PRICE, this::getSellPrice);
        registerFieldSetter(ShopKeys.SELL_PRICE, this::setSellPrice);
        registerKeyValue(ShopKeys.SELL_PRICE, this::sellPrice);
        registerFieldGetter(ShopKeys.BUY_PRICE, this::getBuyPrice);
        registerFieldSetter(ShopKeys.BUY_PRICE, this::setBuyPrice);
        registerKeyValue(ShopKeys.BUY_PRICE, this::buyPrice);
        registerFieldGetter(ShopKeys.IS_ADMIN, this::isAdmin);
        registerFieldSetter(ShopKeys.IS_ADMIN, this::setAdmin);
        registerKeyValue(ShopKeys.IS_ADMIN, this::admin);
        registerFieldGetter(ShopKeys.AMOUNT, this::getAmount);
        registerFieldSetter(ShopKeys.AMOUNT, this::setAmount);
        registerKeyValue(ShopKeys.AMOUNT, this::amount);
        registerFieldGetter(ShopKeys.OWNER, this::getOwner);
        registerFieldSetter(ShopKeys.OWNER, this::setOwner);
        registerKeyValue(ShopKeys.OWNER, this::owner);
    }

    public Map<Currency, BigDecimal> getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Map<Currency, BigDecimal> sellPrice) {
        this.sellPrice = Maps.newHashMap(sellPrice);
    }

    public MapValue<Currency, BigDecimal> sellPrice() {
        return Sponge.getRegistry().getValueFactory().createMapValue(ShopKeys.SELL_PRICE, sellPrice);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Value<Integer> amount() {
        return Sponge.getRegistry().getValueFactory().createValue(ShopKeys.AMOUNT, amount);
    }

    public Map<Currency, BigDecimal> getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Map<Currency, BigDecimal> buyPrice) {
        this.buyPrice = Maps.newHashMap(buyPrice);
    }

    public MapValue<Currency, BigDecimal> buyPrice() {
        return Sponge.getRegistry().getValueFactory().createMapValue(ShopKeys.BUY_PRICE, buyPrice);
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Value<Boolean> admin() {
        return Sponge.getRegistry().getValueFactory().createValue(ShopKeys.IS_ADMIN, admin);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Value<UUID> owner() {
        return Sponge.getRegistry().getValueFactory().createValue(ShopKeys.OWNER, owner);
    }

    @Override
    public Optional<ShopData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<ShopData> that_ = dataHolder.get(ShopData.class);
        if (that_.isPresent()) {
            ShopData that = that_.get();
            ShopData data = overlap.merge(this, that);
            this.admin = data.admin;
            this.buyPrice = Maps.newHashMap(data.buyPrice);
            this.amount = data.amount;
            this.sellPrice = Maps.newHashMap(data.sellPrice);
            this.owner = data.owner;
        } else {
            if (dataHolder instanceof Identifiable) {
                owner = ((Identifiable) dataHolder).getUniqueId();
            }
        }
        return Optional.of(this);
    }

    @Override
    public Optional<ShopData> from(DataContainer container) {
        return from((DataView) container);
    }

    public Optional<ShopData> from(DataView container) {
        container.getBoolean(ShopKeys.IS_ADMIN.getQuery()).ifPresent(this::setAdmin);
        container.getInt(ShopKeys.AMOUNT.getQuery()).ifPresent(this::setAmount);
        container.getMap(ShopKeys.BUY_PRICE.getQuery()).ifPresent(m -> setBuyPrice(deserializeMap(m)));
        container.getMap(ShopKeys.SELL_PRICE.getQuery()).ifPresent(m -> setSellPrice(deserializeMap(m)));
        container.getObject(ShopKeys.OWNER.getQuery(), UUID.class).ifPresent(this::setOwner);
        return Optional.of(this);
    }

    @Override
    public ShopData copy() {
        return new ShopData(sellPrice, buyPrice, amount, admin, owner);
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(sellPrice, buyPrice, amount, admin, owner);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(ShopKeys.IS_ADMIN.getQuery(), admin)
                .set(ShopKeys.AMOUNT.getQuery(), amount)
                .set(ShopKeys.OWNER.getQuery(), owner)
                .set(ShopKeys.BUY_PRICE.getQuery(), serializeMap(buyPrice))
                .set(ShopKeys.SELL_PRICE.getQuery(), serializeMap(sellPrice));
    }

    public static Map<String, String> serializeMap(Map<Currency, BigDecimal> map) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        map.forEach((c, d) -> builder.put(c.getId(), d.toPlainString()));
        return builder.build();
    }

    public static Map<Currency, BigDecimal> deserializeMap(Map<?, ?> map) {
        ImmutableMap.Builder<Currency, BigDecimal> builder = ImmutableMap.builder();
        map.forEach((o1, o2) -> {
            Optional<Currency> currency = Sponge.getRegistry().getType(Currency.class, o1.toString());
            if (currency.isPresent()) {
                if (o2 instanceof Number) {
                    builder.put(currency.get(), BigDecimal.valueOf(((Number) o2).doubleValue()));
                } else if (o2 instanceof String) {
                    builder.put(currency.get(), new BigDecimal((String) o2));
                }
            }
        });
        return builder.build();
    }

    public static class Immutable extends AbstractImmutableData<Immutable, ShopData> {

        Map<Currency, BigDecimal> sellPrice;
        Map<Currency, BigDecimal> buyPrice;
        int amount;
        boolean admin;
        UUID owner;

        {
            registerGetters();
        }

        Immutable() {
            sellPrice = ImmutableMap.of();
            buyPrice = ImmutableMap.of();
            amount = 0;
            admin = true;
            owner = ShopData.ZERO_UUID;
        }

        Immutable(Map<Currency, BigDecimal> sellPrice, Map<Currency, BigDecimal> buyPrice, int amount, boolean admin, UUID owner) {
            this.sellPrice = ImmutableMap.copyOf(sellPrice);
            this.buyPrice = ImmutableMap.copyOf(buyPrice);
            this.amount = amount;
            this.admin = admin;
            this.owner = owner;
        }

        public Map<Currency, BigDecimal> getSellPrice() {
            return sellPrice;
        }

        public Map<Currency, BigDecimal> getBuyPrice() {
            return buyPrice;
        }

        public int getAmount() {
            return amount;
        }

        public boolean isAdmin() {
            return admin;
        }

        public UUID getOwner() {
            return owner;
        }

        public ImmutableMapValue<Currency, BigDecimal> sellPrice() {
            return Sponge.getRegistry().getValueFactory().createMapValue(ShopKeys.SELL_PRICE, sellPrice).asImmutable();
        }

        public ImmutableMapValue<Currency, BigDecimal> buyPrice() {
            return Sponge.getRegistry().getValueFactory().createMapValue(ShopKeys.BUY_PRICE, buyPrice).asImmutable();
        }

        public ImmutableValue<Integer> amount() {
            return Sponge.getRegistry().getValueFactory().createValue(ShopKeys.AMOUNT, amount).asImmutable();
        }

        public ImmutableValue<Boolean> admin() {
            return Sponge.getRegistry().getValueFactory().createValue(ShopKeys.IS_ADMIN, admin).asImmutable();
        }

        public ImmutableValue<UUID> owner() {
            return Sponge.getRegistry().getValueFactory().createValue(ShopKeys.OWNER, owner).asImmutable();
        }

        @Override
        protected void registerGetters() {
            registerFieldGetter(ShopKeys.IS_ADMIN, this::isAdmin);
            registerKeyValue(ShopKeys.IS_ADMIN, this::admin);
            registerFieldGetter(ShopKeys.BUY_PRICE, this::getBuyPrice);
            registerKeyValue(ShopKeys.BUY_PRICE, this::buyPrice);
            registerFieldGetter(ShopKeys.SELL_PRICE, this::getSellPrice);
            registerKeyValue(ShopKeys.SELL_PRICE, this::sellPrice);
            registerFieldGetter(ShopKeys.OWNER, this::getOwner);
            registerKeyValue(ShopKeys.OWNER, this::owner);
            registerFieldGetter(ShopKeys.AMOUNT, this::getAmount);
            registerKeyValue(ShopKeys.AMOUNT, this::amount);
        }

        @Override
        public ShopData asMutable() {
            return new ShopData(sellPrice, buyPrice, amount, admin, owner);
        }

        @Override
        public int getContentVersion() {
            return 1;
        }

        @Override
        public DataContainer toContainer() {
            return super.toContainer()
                    .set(ShopKeys.IS_ADMIN.getQuery(), admin)
                    .set(ShopKeys.AMOUNT.getQuery(), amount)
                    .set(ShopKeys.OWNER.getQuery(), owner)
                    .set(ShopKeys.BUY_PRICE.getQuery(), serializeMap(buyPrice))
                    .set(ShopKeys.SELL_PRICE.getQuery(), serializeMap(sellPrice));
        }
    }

    public static class Builder extends AbstractDataBuilder<ShopData> implements DataManipulatorBuilder<ShopData, Immutable> {

        protected Builder() {
            super(ShopData.class, 1);
        }

        @Override
        public ShopData create() {
            return new ShopData();
        }

        @Override
        public Optional<ShopData> createFrom(DataHolder dataHolder) {
            return create().fill(dataHolder);
        }

        @Override
        protected Optional<ShopData> buildContent(DataView container) throws InvalidDataException {
            return create().from(container);
        }
    }

}
