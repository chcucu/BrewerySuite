package me.admin.brewery.listener;

import me.admin.brewery.BrewerySuitePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    private final BrewerySuitePlugin plugin;

    public PlayerListener(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.holograms() != null) {
            plugin.holograms().applyPreferences(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (plugin.holograms() != null) {
            plugin.holograms().forget(event.getPlayer().getUniqueId());
        }
    }
}
