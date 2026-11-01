package me.admin.brewery.market;

import me.admin.brewery.BreweryHook;
import me.admin.brewery.BrewerySuitePlugin;
import me.admin.brewery.Settings;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MarketService {

    private final BrewerySuitePlugin plugin;
    private final Settings settings;
    private final BreweryHook hook;
    private final MarketPrices prices;
    private final MarketData data;

    private Economy economy;

    public MarketService(BrewerySuitePlugin plugin, Settings settings, BreweryHook hook, MarketPrices prices,
                         MarketData data) {
        this.plugin = plugin;
        this.settings = settings;
        this.hook = hook;
        this.prices = prices;
        this.data = data;
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> provider =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        economy = provider == null ? null : provider.getProvider();
        return economy != null;
    }

    public boolean ready() {
        return economy != null;
    }

    public double remainingLimit(Player player) {
        double limit = settings.marketDailyLimit();
        if (limit <= 0) {
            return -1;
        }
        return Math.max(0, limit - data.soldToday(player.getUniqueId()));
    }

    public SellResult sell(Player player, Inventory inventory, boolean all) {
        if (economy == null) {
            return SellResult.notReady();
        }
        Sale sale = new Sale();
        List<Runnable> removals = new ArrayList<>();

        if (inventory != null) {
            for (int slot = 0; slot < MarketGui.ITEM_SLOTS; slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item == null || item.getType().isAir() || !collect(item, sale)) {
                    continue;
                }
                int captured = slot;
                if (isShulker(item)) {
                    removals.add(() -> consumeShulker(item, inventory, captured));
                } else {
                    removals.add(() -> inventory.setItem(captured, null));
                }
            }
        }

        if (all) {
            ItemStack[] contents = player.getInventory().getContents();
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack item = contents[slot];
                if (item == null || item.getType().isAir() || !collect(item, sale)) {
                    continue;
                }
                int captured = slot;
                if (isShulker(item)) {
                    removals.add(() -> consumeShulker(item, player.getInventory(), captured));
                } else {
                    removals.add(() -> player.getInventory().setItem(captured, null));
                }
            }
        }

        if (sale.count <= 0 || sale.total <= 0) {
            return SellResult.empty();
        }
        double remaining = remainingLimit(player);
        if (remaining >= 0 && sale.total > remaining) {
            plugin.getLogger().info(player.getName() + " 酒市出售被拒绝：需 " + money(sale.total)
                    + "，剩余额度 " + money(remaining) + "，" + summary(sale.items));
            return SellResult.limit(sale.total, sale.count, remaining);
        }

        EconomyResponse response = economy.depositPlayer(player, sale.total);
        if (!response.transactionSuccess()) {
            plugin.getLogger().warning("Vault 支付失败: " + response.errorMessage);
            return SellResult.failed();
        }
        for (Runnable removal : removals) {
            removal.run();
        }
        data.recordSale(player.getUniqueId(), sale.total, sale.count, summary(sale.items));
        plugin.getLogger().info(player.getName() + " 酒市出售 " + sale.count + " 瓶，收入 " + money(sale.total)
                + "，今日累计 " + money(data.soldToday(player.getUniqueId())) + "，" + summary(sale.items));
        return SellResult.success(sale.total, sale.count);
    }

    public double valueOf(ItemStack item) {
        Sale sale = new Sale();
        collect(item, sale);
        return sale.total;
    }

    private boolean collect(ItemStack item, Sale sale) {
        BrewSale brew = hook.readBrewSale(item);
        if (brew != null) {
            int amount = item.getAmount();
            sale.total += prices.price(brew) * amount;
            sale.count += amount;
            sale.items.merge(brew.getName(), amount, Integer::sum);
            return true;
        }
        if (!settings.marketShulkerSelling() || !isShulker(item)) {
            return false;
        }
        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) {
            return false;
        }
        if (!(meta.getBlockState() instanceof ShulkerBox box)) {
            return false;
        }
        boolean found = false;
        for (ItemStack content : box.getInventory().getContents()) {
            BrewSale inner = hook.readBrewSale(content);
            if (inner == null) {
                continue;
            }
            int amount = content.getAmount();
            sale.total += prices.price(inner) * amount;
            sale.count += amount;
            sale.items.merge(inner.getName(), amount, Integer::sum);
            found = true;
        }
        return found;
    }

    private void consumeShulker(ItemStack item, Inventory inventory, int slot) {
        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) {
            return;
        }
        if (!(meta.getBlockState() instanceof ShulkerBox box)) {
            return;
        }
        ItemStack[] contents = box.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && hook.readBrewSale(contents[i]) != null) {
                box.getInventory().setItem(i, null);
            }
        }
        meta.setBlockState(box);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private static boolean isShulker(ItemStack item) {
        return item.getType().name().endsWith("SHULKER_BOX");
    }

    private static String money(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
    }

    private static String summary(Map<String, Integer> items) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            parts.add(entry.getKey() + " x" + entry.getValue());
        }
        return String.join(", ", parts);
    }

    private static final class Sale {
        private double total;
        private int count;
        private final Map<String, Integer> items = new LinkedHashMap<>();
    }

    public static final class SellResult {

        private final boolean ready;
        private final boolean failed;
        private final boolean limitReached;
        private final double total;
        private final int count;
        private final double remaining;

        private SellResult(boolean ready, boolean failed, boolean limitReached, double total, int count,
                           double remaining) {
            this.ready = ready;
            this.failed = failed;
            this.limitReached = limitReached;
            this.total = total;
            this.count = count;
            this.remaining = remaining;
        }

        public static SellResult notReady() {
            return new SellResult(false, false, false, 0, 0, 0);
        }

        public static SellResult failed() {
            return new SellResult(true, true, false, 0, 0, 0);
        }

        public static SellResult empty() {
            return new SellResult(true, false, false, 0, 0, 0);
        }

        public static SellResult limit(double total, int count, double remaining) {
            return new SellResult(true, false, true, total, count, remaining);
        }

        public static SellResult success(double total, int count) {
            return new SellResult(true, false, false, total, count, 0);
        }

        public boolean isReady() {
            return ready;
        }

        public boolean isFailed() {
            return failed;
        }

        public boolean isLimitReached() {
            return limitReached;
        }

        public double getTotal() {
            return total;
        }

        public int getCount() {
            return count;
        }

        public double getRemaining() {
            return remaining;
        }
    }
}
