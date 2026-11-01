package me.admin.brewery.command;

import me.admin.brewery.BrewerySuitePlugin;
import me.admin.brewery.PlayerPrefs.PlayerPref;
import me.admin.brewery.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BreweryCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_USE = "brewsuite.use";
    private static final String PERMISSION_ADMIN = "brewsuite.admin";
    private static final int HELP_LINES = 9;

    private final BrewerySuitePlugin plugin;

    public BreweryCommand(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                sendStatus(player);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            if (!sender.hasPermission(PERMISSION_ADMIN)) {
                send(sender, languageOf(sender), "command.no-permission");
                return true;
            }
            plugin.reloadPlugin();
            plugin.getLogger().info(sender.getName() + " 重载了 BrewerySuite 配置。");
            send(sender, languageOf(sender), "command.reloaded");
            return true;
        }

        if (!(sender instanceof Player player)) {
            send(sender, plugin.settings().defaultLanguage(), "command.player-only");
            return true;
        }
        if (!player.hasPermission(PERMISSION_USE)) {
            send(player, languageOf(player), "command.no-permission");
            return true;
        }

        switch (sub) {
            case "help" -> sendHelp(player);
            case "status" -> sendStatus(player);
            case "toggle" -> {
                PlayerPref pref = plugin.preferences().get(player);
                boolean show = !pref.show();
                plugin.preferences().setShow(player, show);
                if (plugin.holograms() != null) {
                    plugin.holograms().applyPreferences(player);
                }
                send(player, pref.language(), show ? "command.toggle-on" : "command.toggle-off");
            }
            case "mode" -> {
                PlayerPref pref = plugin.preferences().get(player);
                String mode = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : (pref.detailed() ? "brief" : "detailed");
                if (!Settings.MODES.contains(mode)) {
                    send(player, pref.language(), "command.mode-invalid", "value", args.length > 1 ? args[1] : mode);
                    return true;
                }
                plugin.preferences().setMode(player, mode);
                if (plugin.holograms() != null) {
                    plugin.holograms().applyPreferences(player);
                }
                send(player, pref.language(), "command.mode-set",
                        "mode", plugin.messages().raw(pref.language(), "mode." + mode));
            }
            case "lang" -> {
                PlayerPref pref = plugin.preferences().get(player);
                if (args.length <= 1) {
                    send(player, pref.language(), "command.lang-current",
                            "lang", plugin.messages().raw(pref.language(), "lang." + pref.language()));
                    send(player, pref.language(), "command.lang-list",
                            "list", String.join(" / ", Settings.LANGUAGES));
                    return true;
                }
                String language = Settings.normalizeLanguage(args[1]);
                boolean valid = false;
                for (String candidate : Settings.LANGUAGES) {
                    if (candidate.equalsIgnoreCase(args[1])) {
                        valid = true;
                        break;
                    }
                }
                if (!valid) {
                    send(player, pref.language(), "command.lang-invalid", "value", args[1]);
                    return true;
                }
                plugin.preferences().setLanguage(player, language);
                if (plugin.holograms() != null) {
                    plugin.holograms().applyPreferences(player);
                }
                send(player, language, "command.lang-set",
                        "lang", plugin.messages().raw(language, "lang." + language));
            }
            case "nearby" -> {
                PlayerPref pref = plugin.preferences().get(player);
                Boolean requested = args.length > 1 ? parseBoolean(args[1]) : null;
                boolean nearby = requested != null ? requested : !pref.nearby();
                plugin.preferences().setNearby(player, nearby);
                if (plugin.holograms() != null) {
                    plugin.holograms().applyPreferences(player);
                }
                send(player, pref.language(), "command.nearby-set",
                        "state", plugin.messages().raw(pref.language(), nearby ? "common.nearby-on" : "common.nearby-off"));
            }
            case "book" -> {
                PlayerPref pref = plugin.preferences().get(player);
                if (!plugin.settings().bookEnabled()) {
                    send(player, pref.language(), "command.book-disabled");
                    return true;
                }
                plugin.recipeBook().openList(player, 0);
            }
            case "market" -> {
                PlayerPref pref = plugin.preferences().get(player);
                if (!plugin.settings().marketEnabled()) {
                    send(player, pref.language(), "command.market-disabled");
                    return true;
                }
                if (!plugin.marketService().ready()) {
                    send(player, pref.language(), "market.not_ready");
                    return true;
                }
                plugin.marketGui().open(player);
            }
            case "history" -> {
                PlayerPref pref = plugin.preferences().get(player);
                if (!plugin.settings().marketEnabled()) {
                    send(player, pref.language(), "command.market-disabled");
                    return true;
                }
                plugin.marketGui().openHistory(player);
            }
            default -> send(player, languageOf(player), "command.unknown", "sub", args[0]);
        }
        return true;
    }

    private void sendStatus(Player player) {
        PlayerPref pref = plugin.preferences().get(player);
        String lang = pref.language();
        send(player, lang, "command.status-header");
        send(player, lang, "command.status-line",
                "show", plugin.messages().raw(lang, pref.show() ? "common.enabled" : "common.disabled"),
                "mode", plugin.messages().raw(lang, "mode." + pref.mode()),
                "lang", plugin.messages().raw(lang, "lang." + pref.language()),
                "nearby", plugin.messages().raw(lang, pref.nearby() ? "common.nearby-on" : "common.nearby-off"));
        send(player, lang, "command.status-recipes",
                "count", Integer.toString(plugin.hook().recipes().size()));
    }

    private void sendHelp(CommandSender sender) {
        String lang = languageOf(sender);
        send(sender, lang, "command.help-header");
        for (int i = 1; i <= HELP_LINES; i++) {
            send(sender, lang, "command.help" + i);
        }
    }

    private String languageOf(CommandSender sender) {
        if (sender instanceof Player player) {
            return plugin.preferences().get(player).language();
        }
        return plugin.settings().defaultLanguage();
    }

    private void send(CommandSender sender, String language, String key, String... pairs) {
        sender.sendMessage(plugin.messages().raw(language, "prefix") + plugin.messages().format(language, key, pairs));
    }

    private static Boolean parseBoolean(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "on", "true", "yes", "1", "开", "开启" -> true;
            case "off", "false", "no", "0", "关", "关闭" -> false;
            default -> null;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("help", "status", "toggle", "mode", "lang", "nearby",
                    "book", "market", "history"));
            if (sender.hasPermission(PERMISSION_ADMIN)) {
                options.add("reload");
            }
            return filter(options, args[0]);
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "mode" -> filter(Settings.MODES, args[1]);
                case "lang" -> filter(Settings.LANGUAGES, args[1]);
                case "nearby" -> filter(List.of("on", "off"), args[1]);
                default -> List.of();
            };
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}
