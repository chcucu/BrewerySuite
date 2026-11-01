package me.admin.brewery;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class BreweryFiles {

    private final BrewerySuitePlugin plugin;

    public BreweryFiles(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
    }

    public double agingMinutesPerYear() {
        File file = new File(plugin.getDataFolder().getParentFile(), "BreweryX/config.yml");
        if (!file.isFile()) {
            return 20.0;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        double minutes = config.getDouble("agingYearDuration", 20.0);
        return minutes > 0.0 ? minutes : 20.0;
    }

    public int recipeCount() {
        File file = new File(plugin.getDataFolder().getParentFile(), "BreweryX/recipes.yml");
        if (!file.isFile()) {
            return 0;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("recipes");
        return section == null ? 0 : section.getKeys(false).size();
    }
}
