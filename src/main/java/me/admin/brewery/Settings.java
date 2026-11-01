package me.admin.brewery;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;

public final class Settings {

    public static final List<String> LANGUAGES = List.of("zh_CN", "en_US");
    public static final List<String> MODES = List.of("brief", "detailed");

    private final BrewerySuitePlugin plugin;

    private String defaultLanguage = "zh_CN";
    private String defaultMode = "detailed";
    private boolean defaultNearby = false;

    private int refreshTicks = 20;
    private boolean barrelsEnabled = true;
    private boolean cauldronsEnabled = true;
    private boolean showEmptyBarrels = true;

    private double barrelOffsetX = 0.5;
    private double barrelOffsetY = 1.2;
    private double barrelOffsetZ = 0.5;

    private double cauldronOffsetX = 0.5;
    private double cauldronOffsetY = 0.8;
    private double cauldronOffsetZ = 0.5;

    private int maxBottleLines = 6;
    private int maxIngredientLines = 4;
    private double agingMinutesPerYear = 20.0;
    private double nearbyRadius = 16.0;

    private boolean bookEnabled = true;
    private String bookFiller = "GRAY_STAINED_GLASS_PANE";

    private boolean marketEnabled = true;
    private double marketDailyLimit = 10000.0;
    private String marketMoneyFormat = "#,##0.##";
    private boolean marketShulkerSelling = true;
    private int marketHistoryMax = 50;

    public Settings(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        defaultLanguage = normalizeLanguage(c.getString("language", "zh_CN"));
        defaultMode = normalizeMode(c.getString("default-mode", "detailed"));
        defaultNearby = c.getBoolean("nearby.default", false);
        nearbyRadius = Math.max(0.0, c.getDouble("nearby.radius", 16.0));

        refreshTicks = Math.max(1, c.getInt("refresh-ticks", 20));
        barrelsEnabled = c.getBoolean("barrel.enabled", true);
        cauldronsEnabled = c.getBoolean("cauldron.enabled", true);
        showEmptyBarrels = c.getBoolean("barrel.show-empty", true);

        barrelOffsetX = c.getDouble("barrel.offset.x", 0.5);
        barrelOffsetY = c.getDouble("barrel.offset.y", 1.2);
        barrelOffsetZ = c.getDouble("barrel.offset.z", 0.5);

        cauldronOffsetX = c.getDouble("cauldron.offset.x", 0.5);
        cauldronOffsetY = c.getDouble("cauldron.offset.y", 0.8);
        cauldronOffsetZ = c.getDouble("cauldron.offset.z", 0.5);

        maxBottleLines = clamp(c.getInt("barrel.max-bottle-lines", 6), 1, 20);
        maxIngredientLines = clamp(c.getInt("cauldron.max-ingredient-lines", 4), 1, 20);
        agingMinutesPerYear = Math.max(0.0, c.getDouble("aging.minutes-per-year", 0.0));

        bookEnabled = c.getBoolean("book.enabled", true);
        bookFiller = c.getString("book.filler", "GRAY_STAINED_GLASS_PANE");

        marketEnabled = c.getBoolean("market.enabled", true);
        marketDailyLimit = Math.max(0.0, c.getDouble("market.daily-limit", 10000.0));
        marketMoneyFormat = c.getString("market.money-format", "#,##0.##");
        marketShulkerSelling = c.getBoolean("market.shulker-selling", true);
        marketHistoryMax = clamp(c.getInt("market.history.max-entries", 50), 1, 500);
    }

    public static String normalizeLanguage(String value) {
        if (value == null) {
            return "zh_CN";
        }
        String lower = value.toLowerCase(Locale.ROOT).replace('-', '_');
        if (lower.startsWith("zh") || lower.equals("cn") || lower.equals("chinese")) {
            return "zh_CN";
        }
        if (lower.startsWith("en")) {
            return "en_US";
        }
        return "zh_CN";
    }

    public static String normalizeMode(String value) {
        if (value != null && value.toLowerCase(Locale.ROOT).equals("brief")) {
            return "brief";
        }
        return "detailed";
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public String defaultLanguage() {
        return defaultLanguage;
    }

    public String defaultMode() {
        return defaultMode;
    }

    public boolean defaultNearby() {
        return defaultNearby;
    }

    public int refreshTicks() {
        return refreshTicks;
    }

    public boolean barrelsEnabled() {
        return barrelsEnabled;
    }

    public boolean cauldronsEnabled() {
        return cauldronsEnabled;
    }

    public boolean showEmptyBarrels() {
        return showEmptyBarrels;
    }

    public double barrelOffsetX() {
        return barrelOffsetX;
    }

    public double barrelOffsetY() {
        return barrelOffsetY;
    }

    public double barrelOffsetZ() {
        return barrelOffsetZ;
    }

    public double cauldronOffsetX() {
        return cauldronOffsetX;
    }

    public double cauldronOffsetY() {
        return cauldronOffsetY;
    }

    public double cauldronOffsetZ() {
        return cauldronOffsetZ;
    }

    public int maxBottleLines() {
        return maxBottleLines;
    }

    public int maxIngredientLines() {
        return maxIngredientLines;
    }

    public double agingMinutesPerYear() {
        return agingMinutesPerYear;
    }

    public void setAgingMinutesPerYear(double minutes) {
        agingMinutesPerYear = Math.max(0.1, minutes);
    }

    public double nearbyRadius() {
        return nearbyRadius;
    }

    public boolean bookEnabled() {
        return bookEnabled;
    }

    public String bookFiller() {
        return bookFiller;
    }

    public boolean marketEnabled() {
        return marketEnabled;
    }

    public double marketDailyLimit() {
        return marketDailyLimit;
    }

    public String marketMoneyFormat() {
        return marketMoneyFormat;
    }

    public boolean marketShulkerSelling() {
        return marketShulkerSelling;
    }

    public int marketHistoryMax() {
        return marketHistoryMax;
    }
}
