package me.admin.brewery.listener;

import me.admin.brewery.BrewerySuitePlugin;
import me.admin.brewery.PlayerPrefs.PlayerPref;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

public final class BrewDrinkListener implements Listener {

    private final BrewerySuitePlugin plugin;

    public BrewDrinkListener(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Event event) {
        try {
            Object playerValue = method(event.getClass(), "getPlayer").invoke(event);
            if (!(playerValue instanceof Player player)) {
                return;
            }
            Object brew = method(event.getClass(), "getBrew").invoke(event);
            Object recipe = method(brew.getClass(), "getCurrentRecipe").invoke(brew);
            if (recipe == null) {
                return;
            }
            String key = plugin.hook().recipeKeyOf(recipe);
            if (key == null || key.isEmpty() || !plugin.recipeProgress().learn(player.getUniqueId(), key)) {
                return;
            }
            PlayerPref pref = plugin.preferences().get(player);
            String name = plugin.hook().recipeNameOf(recipe);
            player.sendMessage(plugin.messages().raw(pref.language(), "prefix")
                    + plugin.messages().format(pref.language(), "book.learned_notice",
                    "name", name == null || name.isEmpty() ? key : name));
        } catch (Throwable ignored) {
        }
    }

    private static Method method(Class<?> owner, String name) throws NoSuchMethodException {
        return owner.getMethod(name);
    }
}
