package flavor.pie.capitalism;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class ShopCreationContainer {
    Map<Currency, BigDecimal> sellPrice = new HashMap<>();
    Map<Currency, BigDecimal> buyPrice = new HashMap<>();
    ItemStackSnapshot snapshot = ItemStackSnapshot.NONE;
    int amount = 1;
    boolean admin = false;
}
