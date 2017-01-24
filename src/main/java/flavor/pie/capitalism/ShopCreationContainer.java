package flavor.pie.capitalism;

import org.spongepowered.api.item.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class ShopCreationContainer {
    Map<Currency, BigDecimal> sellPrice = new HashMap<>();
    Map<Currency, BigDecimal> buyPrice = new HashMap<>();
    ItemStack item = null;
    int amount = 1;
    boolean admin = false;
}
