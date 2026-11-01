package me.admin.brewery.listener;

import me.admin.brewery.BrewerySuitePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public final class GuiListener implements Listener {

    private final BrewerySuitePlugin plugin;

    public GuiListener(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory top = event.getView().getTopInventory();
        if (plugin.recipeBook().matches(top)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < top.getSize()) {
                plugin.recipeBook().handleClick(player, top, slot);
            }
            return;
        }
        if (plugin.marketGui().matches(top)) {
            if (plugin.marketGui().handleClick(player, top, event.getRawSlot())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (plugin.recipeBook().matches(event.getView().getTopInventory())) {
            event.setCancelled(true);
            return;
        }
        plugin.marketGui().handleDrag(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            plugin.marketGui().handleClose(player, event.getInventory());
        }
    }
}
