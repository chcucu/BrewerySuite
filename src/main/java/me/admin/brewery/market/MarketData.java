package me.admin.brewery.market;

import me.admin.brewery.BrewerySuitePlugin;
import me.admin.brewery.Settings;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MarketData {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final BrewerySuitePlugin plugin;
    private final Settings settings;
    private final File file;
    private YamlConfiguration config;

    public MarketData(BrewerySuitePlugin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.file = new File(plugin.getDataFolder(), "market-data.yml");
    }

    public void load() {
        config = file.isFile() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
    }

    public double soldToday(UUID player) {
        String date = LocalDate.now().toString();
        if (config == null || !date.equals(config.getString("players." + player + ".date"))) {
            return 0.0;
        }
        return config.getDouble("players." + player + ".amount");
    }

    public void recordSale(UUID player, double total, int count, String items) {
        String date = LocalDate.now().toString();
        double today = soldToday(player);
        config.set("players." + player + ".date", date);
        config.set("players." + player + ".amount", today + total);

        List<String> history = new ArrayList<>(config.getStringList("players." + player + ".history"));
        history.add(0, date + " " + LocalTime.now().format(TIME) + "|" + total + "|" + count + "|" + items);
        while (history.size() > settings.marketHistoryMax()) {
            history.remove(history.size() - 1);
        }
        config.set("players." + player + ".history", history);
        save();
    }

    public List<String> history(UUID player) {
        return config == null ? List.of() : config.getStringList("players." + player + ".history");
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("保存 market-data.yml 失败: " + e.getMessage());
        }
    }
}
