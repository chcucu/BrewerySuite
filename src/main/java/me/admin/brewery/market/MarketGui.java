package me.admin.brewery.market;

import me.admin.brewery.Messages;
import me.admin.brewery.PlayerPrefs;
import me.admin.brewery.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public final class MarketGui {

    public static final int ITEM_SLOTS = 45;

    private static final int SIZE = 54;
    private static final int SLOT_SELL = 45;
    private static final int SLOT_SELL_ALL = 46;
    private static final int SLOT_HISTORY = 48;
    private static final int SLOT_CLOSE = 49;
    private static final int SLOT_INFO = 50;

    private final Settings settings;
    private final Messages messages;
    private final PlayerPrefs preferences;
    private final MarketService service;
    private final MarketData data;

    public MarketGui(Settings settings, Messages messages, PlayerPrefs preferences, MarketService service,
                     MarketData data) {
        this.settings = settings;
        this.messages = messages;
        this.preferences = preferences;
        this.service = service;
        this.data = data;
    }

    public void open(Player player) {
        String language = languageOf(player);
        if (!service.ready()) {
            send(player, language, "market.not_ready");
            return;
        }
        Holder holder = new Holder(false);
        Inventory inventory = Bukkit.createInventory(holder, SIZE, messages.raw(language, "market.title"));
        holder.inventory = inventory;

        ItemStack filler = filler();
        for (int slot = ITEM_SLOTS; slot < SIZE; slot++) {
            inventory.setItem(slot, filler);
        }
        inventory.setItem(SLOT_SELL, button(language, Material.EMERALD, "market.sell"));
        inventory.setItem(SLOT_SELL_ALL, button(language, Material.EMERALD_BLOCK, "market.sell_all"));
        inventory.setItem(SLOT_HISTORY, button(language, Material.BOOK, "market.history_button"));
        inventory.setItem(SLOT_CLOSE, button(language, Material.BARRIER, "market.close"));
        inventory.setItem(SLOT_INFO, infoItem(player, language));
        player.openInventory(inventory);
    }

    public void openHistory(Player player) {
        String language = languageOf(player);
        Holder holder = new Holder(true);
        Inventory inventory = Bukkit.createInventory(holder, SIZE, messages.raw(language, "market.history.title"));
        holder.inventory = inventory;

        ItemStack filler = filler();
        for (int slot = 0; slot < SIZE; slot++) {
            inventory.setItem(slot, filler);
        }

        List<String> entries = data.history(player.getUniqueId());
        if (entries.isEmpty()) {
            inventory.setItem(22, button(language, Material.PAPER, "market.history.empty"));
        } else {
            int limit = Math.min(ITEM_SLOTS, entries.size());
            for (int i = 0; i < limit; i++) {
                inventory.setItem(i, historyItem(language, entries.get(i)));
            }
        }
        inventory.setItem(45, button(language, Material.ARROW, "market.history.back"));
        inventory.setItem(49, button(language, Material.BARRIER, "market.close"));
        player.openInventory(inventory);
    }

    public boolean matches(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof Holder;
    }

    public boolean handleClick(Player player, Inventory inventory, int rawSlot) {
        if (!(inventory.getHolder() instanceof Holder holder)) {
            return false;
        }
        if (holder.history) {
            switch (rawSlot) {
                case 45 -> open(player);
                case 49 -> player.closeInventory();
                default -> {
                }
            }
            return true;
        }
        if (rawSlot >= inventory.getSize() || rawSlot < ITEM_SLOTS) {
            return false;
        }
        switch (rawSlot) {
            case SLOT_SELL -> sell(player, holder, false);
            case SLOT_SELL_ALL -> sell(player, holder, true);
            case SLOT_HISTORY -> openHistory(player);
            case SLOT_CLOSE -> player.closeInventory();
            default -> {
            }
        }
        return true;
    }

    public void handleDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder holder)) {
            return;
        }
        if (holder.history) {
            event.setCancelled(true);
            return;
        }
        for (int slot : event.getRawSlots()) {
            if (slot >= ITEM_SLOTS && slot < SIZE) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public void handleClose(Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof Holder holder) || holder.history) {
            return;
        }
        for (int slot = 0; slot < ITEM_SLOTS; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }
            inventory.setItem(slot, null);
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    private void sell(Player player, Holder holder, boolean all) {
        String language = languageOf(player);
        MarketService.SellResult result = service.sell(player, holder.inventory, all);
        if (!result.isReady()) {
            send(player, language, "market.not_ready");
            return;
        }
        if (result.isFailed()) {
            send(player, language, "market.failed");
            return;
        }
        if (result.isLimitReached()) {
            send(player, language, "market.limit_reached",
                    "amount", formatMoney(result.getRemaining()));
            return;
        }
        if (result.getCount() <= 0) {
            send(player, language, "market.sold_none");
            return;
        }
        send(player, language, "market.sold",
                "total", formatMoney(result.getTotal()),
                "count", Integer.toString(result.getCount()));
        holder.inventory.setItem(SLOT_INFO, infoItem(player, language));
    }

    private ItemStack infoItem(Player player, String language) {
        double remaining = service.remainingLimit(player);
        String text = remaining < 0
                ? messages.raw(language, "market.limit_unlimited")
                : messages.format(language, "market.limit_remaining", "amount", formatMoney(remaining));
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(messages.raw(language, "market.info"));
        meta.setLore(List.of(text));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack historyItem(String language, String entry) {
        String[] parts = entry.split("\\|", 4);
        String time = parts.length > 0 ? parts[0] : "";
        double total = parts.length > 1 ? parseDouble(parts[1]) : 0.0;
        String count = parts.length > 2 ? parts[2] : "0";
        String items = parts.length > 3 ? parts[3] : "";
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(messages.format(language, "market.history.entry",
                "time", time,
                "total", formatMoney(total),
                "count", count));
        if (!items.isEmpty()) {
            meta.setLore(List.of(messages.format(language, "market.history.items", "items", items)));
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack button(String language, Material material, String key, String... pairs) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(messages.format(language, key, pairs));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack filler() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private String formatMoney(double amount) {
        DecimalFormat format;
        try {
            format = new DecimalFormat(settings.marketMoneyFormat());
        } catch (IllegalArgumentException e) {
            format = new DecimalFormat("#,##0.##");
        }
        return format.format(amount);
    }

    private void send(Player player, String language, String key, String... pairs) {
        player.sendMessage(messages.raw(language, "prefix") + messages.format(language, key, pairs));
    }

    private String languageOf(Player player) {
        return preferences.get(player).language();
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static final class Holder implements InventoryHolder {

        private final boolean history;
        private Inventory inventory;

        private Holder(boolean history) {
            this.history = history;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
