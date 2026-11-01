package me.admin.brewery.book;

import me.admin.brewery.BreweryHook;
import me.admin.brewery.Messages;
import me.admin.brewery.PlayerPrefs;
import me.admin.brewery.Settings;
import me.admin.brewery.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RecipeBookGui {

    private static final int LIST_SIZE = 54;
    private static final int LIST_COLUMNS = 45;
    private static final int DETAIL_SIZE = 27;
    private static final int DETAIL_INGREDIENT_START = 9;

    private final Settings settings;
    private final Messages messages;
    private final PlayerPrefs preferences;
    private final BreweryHook hook;
    private final RecipeProgress progress;

    public RecipeBookGui(Settings settings, Messages messages, PlayerPrefs preferences, BreweryHook hook,
                         RecipeProgress progress) {
        this.settings = settings;
        this.messages = messages;
        this.preferences = preferences;
        this.hook = hook;
        this.progress = progress;
    }

    public void openList(Player player, int requestedPage) {
        String language = languageOf(player);
        List<RecipeInfo> recipes = hook.recipes();
        if (recipes.isEmpty()) {
            player.sendMessage(messages.raw(language, "prefix") + messages.raw(language, "book.unavailable"));
            return;
        }
        int pages = (recipes.size() + LIST_COLUMNS - 1) / LIST_COLUMNS;
        int page = Math.max(0, Math.min(requestedPage, pages - 1));

        Holder holder = new Holder(false, page, null);
        Inventory inventory = Bukkit.createInventory(holder, LIST_SIZE, messages.raw(language, "book.title.list"));
        holder.inventory = inventory;

        ItemStack filler = filler(language);
        for (int slot = LIST_COLUMNS; slot < LIST_SIZE; slot++) {
            inventory.setItem(slot, filler);
        }

        UUID id = player.getUniqueId();
        int start = page * LIST_COLUMNS;
        int end = Math.min(recipes.size(), start + LIST_COLUMNS);
        for (int i = start; i < end; i++) {
            RecipeInfo recipe = recipes.get(i);
            inventory.setItem(i - start, listItem(language, recipe, progress.hasLearned(id, recipe.getKey())));
        }

        if (page > 0) {
            inventory.setItem(45, button(language, Material.ARROW, "book.prev"));
        }
        inventory.setItem(49, button(language, Material.BOOK, "book.page",
                "page", Integer.toString(page + 1), "pages", Integer.toString(pages)));
        inventory.setItem(50, button(language, Material.BARRIER, "book.close"));
        if (page + 1 < pages) {
            inventory.setItem(53, button(language, Material.ARROW, "book.next"));
        }
        player.openInventory(inventory);
    }

    public void openDetail(Player player, RecipeInfo recipe, int page) {
        String language = languageOf(player);
        Holder holder = new Holder(true, page, recipe);
        Inventory inventory = Bukkit.createInventory(holder, DETAIL_SIZE, messages.raw(language, "book.title.detail"));
        holder.inventory = inventory;

        ItemStack filler = filler(language);
        for (int slot = 0; slot < DETAIL_SIZE; slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(4, detailItem(language, recipe, progress.hasLearned(player.getUniqueId(), recipe.getKey())));

        List<IngredientLine> ingredients = recipe.getIngredients();
        int limit = Math.min(9, ingredients.size());
        for (int i = 0; i < limit; i++) {
            inventory.setItem(DETAIL_INGREDIENT_START + i, ingredientItem(language, ingredients.get(i)));
        }
        if (ingredients.isEmpty()) {
            inventory.setItem(DETAIL_INGREDIENT_START, button(language, Material.PAPER, "book.no_ingredients"));
        }

        inventory.setItem(18, button(language, Material.ARROW, "book.back"));
        inventory.setItem(26, button(language, Material.BARRIER, "book.close"));
        player.openInventory(inventory);
    }

    public boolean matches(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof Holder;
    }

    public void handleClick(Player player, Inventory inventory, int slot) {
        if (!(inventory.getHolder() instanceof Holder holder)) {
            return;
        }
        String language = languageOf(player);
        if (!holder.detail) {
            if (slot >= LIST_COLUMNS) {
                switch (slot) {
                    case 45 -> openList(player, holder.page - 1);
                    case 50 -> player.closeInventory();
                    case 53 -> openList(player, holder.page + 1);
                    default -> {
                    }
                }
                return;
            }
            List<RecipeInfo> recipes = hook.recipes();
            int index = holder.page * LIST_COLUMNS + slot;
            if (index >= 0 && index < recipes.size()) {
                openDetail(player, recipes.get(index), holder.page);
            }
            return;
        }

        if (slot == 18) {
            openList(player, holder.page);
        } else if (slot == 26) {
            player.closeInventory();
        }
    }

    public String languageOf(Player player) {
        return preferences.get(player).language();
    }

    private ItemStack listItem(String language, RecipeInfo recipe, boolean learned) {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colorPrefix(recipe) + recipe.getName());
        List<String> lore = new ArrayList<>();
        lore.add(messages.format(language, "book.list.line1",
                "age", Integer.toString(recipe.getAgeYears()),
                "alc", Integer.toString(recipe.getAlcohol())));
        lore.add(messages.format(language, "book.list.line2",
                "cooking", Integer.toString(recipe.getCookingMinutes()),
                "distill", Integer.toString(recipe.getDistillRuns()),
                "difficulty", Integer.toString(recipe.getDifficulty())));
        lore.add(learned ? messages.raw(language, "book.learned") : messages.raw(language, "book.not_learned"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack detailItem(String language, RecipeInfo recipe, boolean learned) {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colorPrefix(recipe) + recipe.getName());
        List<String> lore = new ArrayList<>();
        lore.add(messages.format(language, "book.detail.color",
                "color", recipe.getColor() == null ? "-" : recipe.getColor()));
        lore.add(messages.format(language, "book.detail.cooking",
                "minutes", Integer.toString(recipe.getCookingMinutes())));
        lore.add(messages.format(language, "book.detail.distill",
                "runs", Integer.toString(recipe.getDistillRuns()),
                "seconds", Integer.toString(recipe.getDistillSeconds())));
        lore.add(messages.format(language, "book.detail.age",
                "years", Integer.toString(recipe.getAgeYears())));
        lore.add(messages.format(language, "book.detail.alcohol",
                "alcohol", Integer.toString(recipe.getAlcohol())));
        lore.add(messages.format(language, "book.detail.difficulty",
                "difficulty", Integer.toString(recipe.getDifficulty())));
        if (recipe.getWoodKey() != null && !recipe.getWoodKey().equalsIgnoreCase("ANY")
                && !recipe.getWoodKey().equalsIgnoreCase("NONE")) {
            lore.add(messages.format(language, "book.detail.wood",
                    "wood", messages.wood(language, recipe.getWoodKey(), recipe.getWoodLabel())));
        }
        lore.add(messages.raw(language, "book.detail.ingredients"));
        lore.add(learned ? messages.raw(language, "book.learned") : messages.raw(language, "book.not_learned"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack ingredientItem(String language, IngredientLine line) {
        Material material = line.getKey() == null ? null : Material.matchMaterial(line.getKey());
        if (material == null || material.isAir()) {
            material = Material.PAPER;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(messages.material(language, line.getKey(), line.getLabel()) + " x" + line.getAmount());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack button(String language, Material material, String key, String... pairs) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(messages.format(language, key, pairs));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack filler(String language) {
        Material material = Material.matchMaterial(settings.bookFiller());
        if (material == null || material.isAir()) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private static String colorPrefix(RecipeInfo recipe) {
        return Text.legacyColor(recipe.getColor());
    }

    public static final class Holder implements InventoryHolder {

        private final boolean detail;
        private final int page;
        private final RecipeInfo recipe;
        private Inventory inventory;

        private Holder(boolean detail, int page, RecipeInfo recipe) {
            this.detail = detail;
            this.page = page;
            this.recipe = recipe;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public boolean isDetail() {
            return detail;
        }

        public RecipeInfo getRecipe() {
            return recipe;
        }
    }
}
