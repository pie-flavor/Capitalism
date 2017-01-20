package flavor.pie.capitalism;

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
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.service.economy.Currency;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ShopData extends AbstractData<ShopData, ShopData.Immutable> {
    Map<Currency, BigDecimal> sellPrice = new HashMap<>();
    int amount = 0;
    Map<Currency, BigDecimal> buyPrice = new HashMap<>();
    boolean admin = true;
    UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000000");

    ShopData() {
        registerGettersAndSetters();
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
        this.sellPrice = sellPrice;
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
        this.buyPrice = buyPrice;
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
        return null;
    }

    @Override
    public Optional<ShopData> from(DataContainer container) {
        return null;
    }

    @Override
    public ShopData copy() {
        return null;
    }

    @Override
    public Immutable asImmutable() {
        return null;
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    public static class Immutable extends AbstractImmutableData<Immutable, ShopData> {

        @Override
        protected void registerGetters() {

        }

        @Override
        public ShopData asMutable() {
            return null;
        }

        @Override
        public int getContentVersion() {
            return 0;
        }
    }

    public static class Builder extends AbstractDataBuilder<ShopData> implements DataManipulatorBuilder<ShopData, Immutable> {

        protected Builder() {
            super(ShopData.class, 1);
        }

        @Override
        public ShopData create() {
            return null;
        }

        @Override
        public Optional<ShopData> createFrom(DataHolder dataHolder) {
            return null;
        }

        @Override
        protected Optional<ShopData> buildContent(DataView container) throws InvalidDataException {
            return null;
        }
    }

}
