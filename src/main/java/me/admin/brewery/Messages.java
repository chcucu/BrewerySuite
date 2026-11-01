package me.admin.brewery;

import me.admin.brewery.util.Text;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Messages {

    private final BrewerySuitePlugin plugin;
    private final Map<String, YamlConfiguration> files = new LinkedHashMap<>();

    public Messages(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        files.clear();
        File folder = new File(plugin.getDataFolder(), "lang");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("无法创建语言文件夹: " + folder.getAbsolutePath());
        }
        for (String language : Settings.LANGUAGES) {
            String resource = "lang/" + language + ".yml";
            File file = new File(folder, language + ".yml");
            if (!file.exists()) {
                try {
                    plugin.saveResource(resource, false);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("缺少内置语言文件: " + resource);
                }
            }
            if (file.exists()) {
                files.put(language, YamlConfiguration.loadConfiguration(file));
            }
        }
    }

    public String raw(String language, String key) {
        String value = rawOrNull(language, key);
        return value == null ? key : Text.color(value);
    }

    public String rawOrNull(String language, String key) {
        YamlConfiguration config = files.get(language);
        if (config != null) {
            String value = config.getString(key);
            if (value != null) {
                return value;
            }
        }
        if (key.startsWith("materials.") || key.startsWith("wood.")) {
            return null;
        }
        YamlConfiguration fallback = files.get("zh_CN");
        return fallback == null ? null : fallback.getString(key);
    }

    public String format(String language, String key, String... pairs) {
        String template = raw(language, key);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            template = template.replace("%" + pairs[i] + "%", pairs[i + 1]);
        }
        return template;
    }

    public String wood(String language, String woodKey, String fallbackLabel) {
        if (woodKey != null) {
            String value = rawOrNull(language, "wood." + woodKey);
            if (value != null) {
                return Text.color(value);
            }
        }
        return fallbackLabel == null ? "" : fallbackLabel;
    }

    public String material(String language, String materialKey, String fallbackName) {
        if (materialKey != null) {
            String value = rawOrNull(language, "materials." + materialKey);
            if (value != null) {
                return Text.color(value);
            }
        }
        return fallbackName == null ? "" : fallbackName;
    }

    public boolean has(String language, String key) {
        return rawOrNull(language, key) != null;
    }
}
