package me.admin.brewery;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.admin.brewery.PlayerPrefs.PlayerPref;
import me.admin.brewery.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Holograms {

    private final BrewerySuitePlugin plugin;
    private final Settings settings;
    private final PlayerPrefs preferences;
    private final Map<String, HoloEntry> entries = new LinkedHashMap<>();

    public Holograms(BrewerySuitePlugin plugin, Settings settings, PlayerPrefs preferences) {
        this.plugin = plugin;
        this.settings = settings;
        this.preferences = preferences;
    }

    public int pageIndex(PlayerPref pref) {
        int language = Settings.LANGUAGES.indexOf(pref.language());
        if (language < 0) {
            language = 0;
        }
        int mode = pref.detailed() ? 1 : 0;
        return language * 2 + mode;
    }

    public void upsert(String key, Location location, List<List<String>> pageLines) {
        if (location.getWorld() == null || pageLines.isEmpty()) {
            return;
        }
        HoloEntry entry = entries.get(key);
        if (entry == null) {
            Hologram hologram;
            try {
                hologram = DHAPI.getHologram(key);
                if (hologram == null) {
                    hologram = DHAPI.createHologram(key, location, false);
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("创建全息失败 " + key + ": " + t.getMessage());
                return;
            }
            while (hologram.size() < pageLines.size()) {
                hologram.addPage();
            }
            List<List<String>> stored = new ArrayList<>();
            for (int i = 0; i < pageLines.size(); i++) {
                List<String> lines = new ArrayList<>(pageLines.get(i));
                stored.add(lines);
                try {
                    DHAPI.setHologramLines(hologram, i, lines);
                } catch (Throwable t) {
                    plugin.getLogger().warning("写入全息失败 " + key + " 第 " + i + " 页: " + t.getMessage());
                }
            }
            entry = new HoloEntry(hologram, stored);
            entries.put(key, entry);
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyEntry(entry, player);
            }
            return;
        }

        for (int i = 0; i < pageLines.size(); i++) {
            List<String> lines = pageLines.get(i);
            if (i < entry.lines.size() && entry.lines.get(i).equals(lines)) {
                continue;
            }
            try {
                DHAPI.setHologramLines(entry.hologram, i, lines);
                if (i < entry.lines.size()) {
                    entry.lines.set(i, new ArrayList<>(lines));
                } else {
                    entry.lines.add(new ArrayList<>(lines));
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("更新全息失败 " + key + " 第 " + i + " 页: " + t.getMessage());
            }
        }
    }

    public void retainOnly(Set<String> keys) {
        Iterator<Map.Entry<String, HoloEntry>> iterator = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, HoloEntry> current = iterator.next();
            if (!keys.contains(current.getKey())) {
                removeHologram(current.getValue().hologram);
                iterator.remove();
            }
        }
    }

    public void applyPreferences(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        for (HoloEntry entry : entries.values()) {
            applyEntry(entry, player);
        }
    }

    public void updateNearby() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerPref pref = preferences.get(player);
            UUID id = player.getUniqueId();
            for (HoloEntry entry : entries.values()) {
                boolean hide = shouldHide(entry.hologram, player, pref);
                boolean hidden = entry.hidden.contains(id);
                if (hide && !hidden) {
                    entry.hidden.add(id);
                    entry.hologram.setHidePlayer(player);
                    entry.hologram.hide(player);
                } else if (!hide && hidden) {
                    entry.hidden.remove(id);
                    entry.hologram.removeHidePlayer(player);
                    entry.hologram.show(player, entry.hologram.getPlayerPage(player));
                }
            }
        }
    }

    public void forget(UUID id) {
        for (HoloEntry entry : entries.values()) {
            entry.hidden.remove(id);
            entry.hologram.getViewerPages().remove(id);
        }
    }

    public void removeAll() {
        for (HoloEntry entry : entries.values()) {
            removeHologram(entry.hologram);
        }
        entries.clear();
    }

    public void removeStale() {
        try {
            for (Hologram hologram : new ArrayList<>(Hologram.getCachedHolograms())) {
                if (hologram != null && hologram.getName().startsWith(Keys.PREFIX)) {
                    removeHologram(hologram);
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("清理旧全息失败: " + t.getMessage());
        }
    }

    private void applyEntry(HoloEntry entry, Player player) {
        PlayerPref pref = preferences.get(player);
        UUID id = player.getUniqueId();
        int page = pageIndex(pref);
        entry.hologram.getViewerPages().put(id, page);

        if (shouldHide(entry.hologram, player, pref)) {
            entry.hidden.add(id);
            entry.hologram.setHidePlayer(player);
            entry.hologram.hide(player);
        } else {
            entry.hidden.remove(id);
            entry.hologram.removeHidePlayer(player);
            entry.hologram.show(player, page);
        }
    }

    private boolean shouldHide(Hologram hologram, Player player, PlayerPref pref) {
        if (!pref.show()) {
            return true;
        }
        if (!pref.nearby()) {
            return false;
        }
        Location location = hologram.getLocation();
        Location playerLocation = player.getLocation();
        if (location == null || location.getWorld() == null || !location.getWorld().equals(playerLocation.getWorld())) {
            return true;
        }
        double radius = settings.nearbyRadius();
        return location.distanceSquared(playerLocation) > radius * radius;
    }

    private void removeHologram(Hologram hologram) {
        if (hologram == null) {
            return;
        }
        try {
            DHAPI.removeHologram(hologram.getName());
        } catch (Throwable ignored) {
        }
    }

    private static final class HoloEntry {
        private final Hologram hologram;
        private final List<List<String>> lines;
        private final Set<UUID> hidden = new HashSet<>();

        private HoloEntry(Hologram hologram, List<List<String>> lines) {
            this.hologram = hologram;
            this.lines = lines;
        }
    }
}
