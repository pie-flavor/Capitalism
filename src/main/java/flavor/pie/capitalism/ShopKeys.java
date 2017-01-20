package flavor.pie.capitalism;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.service.economy.Currency;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class ShopKeys {
    private ShopKeys() {}
    public final static Key<MapValue<Currency, BigDecimal>> SELL_PRICE;
    public final static Key<MapValue<Currency, BigDecimal>> BUY_PRICE;
    public final static Key<Value<Integer>> AMOUNT;
    public final static Key<Value<Boolean>> IS_ADMIN;
    public final static Key<Value<UUID>> OWNER;
    static {
        TypeToken<Map<Currency, BigDecimal>> mapToken = new TypeToken<Map<Currency, BigDecimal>>(){};
        TypeToken<MapValue<Currency, BigDecimal>> mapValueToken = new TypeToken<MapValue<Currency, BigDecimal>>(){};
        SELL_PRICE = KeyFactory.makeMapKey(mapToken, mapValueToken, DataQuery.of("SellPrice"), "capitalism:sell_price", "Sell Price");
        BUY_PRICE = KeyFactory.makeMapKey(mapToken, mapValueToken, DataQuery.of("BuyPrice"), "capitalism:buy_price", "Buy Price");
        AMOUNT = KeyFactory.makeSingleKey(TypeToken.of(Integer.class), new TypeToken<Value<Integer>>(){}, DataQuery.of("Amount"), "capitalism:amount", "Amount");
        IS_ADMIN = KeyFactory.makeSingleKey(TypeToken.of(Boolean.class), new TypeToken<Value<Boolean>>(){}, DataQuery.of("Admin"), "capitalism:admin", "Admin");
        OWNER = KeyFactory.makeSingleKey(TypeToken.of(UUID.class), new TypeToken<Value<UUID>>(){}, DataQuery.of("Owner"), "capitalism:owner", "Owner");
    }
}
