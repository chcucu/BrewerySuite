package me.admin.brewery.book;

import me.admin.brewery.BrewerySuitePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RecipeProgress {

    private final BrewerySuitePlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public RecipeProgress(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player-recipes.yml");
    }

    public void load() {
        if (!file.isFile()) {
            config = new YamlConfiguration();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean hasLearned(UUID player, String recipeKey) {
        if (config == null || recipeKey == null || recipeKey.isEmpty()) {
            return false;
        }
        return config.getStringList("players." + player).contains(recipeKey);
    }

    public boolean learn(UUID player, String recipeKey) {
        if (config == null || recipeKey == null || recipeKey.isEmpty() || hasLearned(player, recipeKey)) {
            return false;
        }
        List<String> keys = new ArrayList<>(config.getStringList("players." + player));
        keys.add(recipeKey);
        config.set("players." + player, keys);
        save();
        return true;
    }

    public int learnedCount(UUID player) {
        return config == null ? 0 : config.getStringList("players." + player).size();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("保存 player-recipes.yml 失败: " + e.getMessage());
        }
    }
}
