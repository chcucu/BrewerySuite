package me.admin.brewery.market;

import me.admin.brewery.BrewerySuitePlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class MarketPrices {

    private final BrewerySuitePlugin plugin;
    private final Map<String, Double> prices = new LinkedHashMap<>();
    private double defaultPrice = 10.0;

    public MarketPrices(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        prices.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("market.prices");
        defaultPrice = section == null ? 10.0 : Math.max(0.0, section.getDouble("default", 10.0));
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase("default")) {
                continue;
            }
            prices.put(key.toLowerCase(Locale.ROOT), Math.max(0.0, section.getDouble(key, defaultPrice)));
        }
    }

    public double basePrice(String recipeKey, String name) {
        Double value = null;
        if (recipeKey != null) {
            value = prices.get(recipeKey.toLowerCase(Locale.ROOT));
        }
        if (value == null && name != null) {
            value = prices.get(name.toLowerCase(Locale.ROOT));
        }
        return value == null ? defaultPrice : value;
    }

    public double price(BrewSale sale) {
        return basePrice(sale.getRecipeKey(), sale.getName()) * sale.score();
    }

    public double defaultPrice() {
        return defaultPrice;
    }
}
