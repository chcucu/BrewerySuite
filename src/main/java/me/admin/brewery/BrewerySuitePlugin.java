package me.admin.brewery;

import me.admin.brewery.book.RecipeBookGui;
import me.admin.brewery.book.RecipeProgress;
import me.admin.brewery.command.BreweryCommand;
import me.admin.brewery.listener.BrewDrinkListener;
import me.admin.brewery.listener.GuiListener;
import me.admin.brewery.listener.PlayerListener;
import me.admin.brewery.market.MarketData;
import me.admin.brewery.market.MarketGui;
import me.admin.brewery.market.MarketPrices;
import me.admin.brewery.market.MarketService;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

public final class BrewerySuitePlugin extends JavaPlugin {

    private Settings settings;
    private Messages messages;
    private PlayerPrefs preferences;
    private BreweryFiles files;
    private BreweryHook hook;
    private Holograms holograms;
    private RenderTask renderTask;
    private BukkitTask task;

    private RecipeProgress recipeProgress;
    private RecipeBookGui recipeBook;

    private MarketPrices marketPrices;
    private MarketData marketData;
    private MarketService marketService;
    private MarketGui marketGui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        settings = new Settings(this);
        settings.load();
        messages = new Messages(this);
        messages.load();
        preferences = new PlayerPrefs(this, settings);
        files = new BreweryFiles(this);
        if (settings.agingMinutesPerYear() <= 0) {
            settings.setAgingMinutesPerYear(files.agingMinutesPerYear());
        }

        hook = new BreweryHook(this);
        hook.init();

        if (getServer().getPluginManager().isPluginEnabled("DecentHolograms")) {
            holograms = new Holograms(this, settings, preferences);
            holograms.removeStale();
            renderTask = new RenderTask(settings, messages, hook, holograms);
        } else {
            getLogger().warning("未找到 DecentHolograms，全息功能已停用。");
        }

        recipeProgress = new RecipeProgress(this);
        recipeProgress.load();
        recipeBook = new RecipeBookGui(settings, messages, preferences, hook, recipeProgress);

        marketPrices = new MarketPrices(this);
        marketPrices.load();
        marketData = new MarketData(this, settings);
        marketData.load();
        marketService = new MarketService(this, settings, hook, marketPrices, marketData);
        if (!marketService.setupEconomy()) {
            getLogger().severe("未找到可用的 Vault 经济提供者，BrewerySuite 已禁用。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        marketGui = new MarketGui(settings, messages, preferences, marketService, marketData);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        registerBrewDrink();

        PluginCommand command = getCommand("brewsuite");
        if (command != null) {
            BreweryCommand executor = new BreweryCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        startTask();
        getLogger().info("BrewerySuite 已启动，配方 " + hook.recipes().size() + " 条，默认语言 "
                + settings.defaultLanguage() + "，刷新间隔 " + settings.refreshTicks() + " tick");
        getLogger().info("功能状态：全息 " + (holograms != null ? "开启" : "关闭")
                + "，配方书 " + (settings.bookEnabled() ? "开启" : "关闭")
                + "，酒市 " + (settings.marketEnabled() ? "开启" : "关闭")
                + "，每日额度 " + (settings.marketDailyLimit() <= 0 ? "无限制" : settings.marketDailyLimit()));
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (holograms != null) {
            holograms.removeAll();
        }
    }

    public void reloadPlugin() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        settings.load();
        if (settings.agingMinutesPerYear() <= 0) {
            settings.setAgingMinutesPerYear(files.agingMinutesPerYear());
        }
        messages.load();
        marketPrices.load();
        marketData.load();
        recipeProgress.load();
        hook.reloadRecipes();
        if (holograms != null) {
            holograms.removeAll();
        }
        startTask();
    }

    private void startTask() {
        if (renderTask == null) {
            return;
        }
        task = getServer().getScheduler().runTaskTimer(this, () -> {
            try {
                renderTask.tick();
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "刷新全息时出错", t);
            }
        }, 20L, Math.max(1L, settings.refreshTicks()));
    }

    private void registerBrewDrink() {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(
                    "com.dre.brewery.api.events.brew.BrewDrinkEvent");
            BrewDrinkListener listener = new BrewDrinkListener(this);
            getServer().getPluginManager().registerEvent(eventClass, listener, EventPriority.MONITOR,
                    (registered, event) -> listener.handle(event), this, true);
        } catch (Throwable t) {
            getLogger().warning("未能监听 BrewDrinkEvent: " + t.getMessage());
        }
    }

    public Settings settings() {
        return settings;
    }

    public Messages messages() {
        return messages;
    }

    public PlayerPrefs preferences() {
        return preferences;
    }

    public BreweryHook hook() {
        return hook;
    }

    public Holograms holograms() {
        return holograms;
    }

    public RecipeProgress recipeProgress() {
        return recipeProgress;
    }

    public RecipeBookGui recipeBook() {
        return recipeBook;
    }

    public MarketService marketService() {
        return marketService;
    }

    public MarketGui marketGui() {
        return marketGui;
    }
}
