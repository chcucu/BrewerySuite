package me.admin.brewery;

import me.admin.brewery.book.IngredientLine;
import me.admin.brewery.book.RecipeInfo;
import me.admin.brewery.market.BrewSale;
import me.admin.brewery.model.BarrelSnapshot;
import me.admin.brewery.model.BottleInfo;
import me.admin.brewery.model.BottleStatus;
import me.admin.brewery.model.CauldronSnapshot;
import me.admin.brewery.model.IngredientInfo;
import me.admin.brewery.util.Text;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BreweryHook {

    private static final String BARREL_CLASS = "com.dre.brewery.Barrel";
    private static final String CAULDRON_CLASS = "com.dre.brewery.BCauldron";
    private static final String BREW_CLASS = "com.dre.brewery.Brew";
    private static final String RECIPE_CLASS = "com.dre.brewery.recipe.BRecipe";
    private static final String INGREDIENTS_CLASS = "com.dre.brewery.BIngredients";
    private static final String INGREDIENT_CLASS = "com.dre.brewery.recipe.Ingredient";
    private static final String RECIPE_ITEM_CLASS = "com.dre.brewery.recipe.RecipeItem";

    private final BrewerySuitePlugin plugin;
    private final Map<String, Optional<Method>> methodCache = new HashMap<>();

    private Class<?> barrelClass;
    private Class<?> cauldronClass;
    private Class<?> brewClass;
    private Class<?> recipeClass;
    private Class<?> ingredientsClass;
    private Class<?> ingredientClass;
    private Class<?> recipeItemClass;

    private Field barrelsField;
    private Field bcauldronsField;
    private Field recipesField;

    private Method barrelGetSpigot;
    private Method barrelGetTime;
    private Method barrelGetInventory;
    private Method barrelIsSmall;

    private Method brewGet;
    private Method brewGetAgeTime;
    private Method brewGetQuality;
    private Method brewGetAlc;
    private Method brewGetOrCalcAlc;
    private Method brewGetDistillRuns;
    private Method brewGetWood;
    private Method brewGetRecipe;
    private Method brewIsStripped;

    private Method recipeGetId;
    private Method recipeGetName;
    private Method recipeGetColor;
    private Method recipeGetIngredients;
    private Method recipeGetCookingTime;
    private Method recipeGetDistillRuns;
    private Method recipeGetDistillTime;
    private Method recipeGetAge;
    private Method recipeGetAlcohol;
    private Method recipeGetDifficulty;
    private Method recipeGetWood;

    private Method recipeItemGetAmount;
    private Method recipeItemGetMaterials;
    private Method recipeItemGetConfigId;
    private Method recipeItemGetDebugId;

    private Method ingredientsGetList;
    private Method ingredientsGetField;
    private Method ingredientGetAmount;
    private Method ingredientGetDebugId;

    private Method cauldronGetBlock;
    private Method cauldronGetState;
    private Method cauldronGetIngredients;

    private boolean barrelsReady;
    private boolean cauldronsReady;
    private boolean recipesReady;

    private List<RecipeInfo> recipes = List.of();

    public BreweryHook(BrewerySuitePlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        barrelClass = loadClass(BARREL_CLASS);
        brewClass = loadClass(BREW_CLASS);
        recipeClass = loadClass(RECIPE_CLASS);
        ingredientsClass = loadClass(INGREDIENTS_CLASS);
        ingredientClass = loadClass(INGREDIENT_CLASS);
        recipeItemClass = loadClass(RECIPE_ITEM_CLASS);
        cauldronClass = loadClass(CAULDRON_CLASS);

        if (barrelClass != null && brewClass != null && recipeClass != null) {
            barrelsField = findField(barrelClass, "barrels");
            barrelGetSpigot = findMethod(barrelClass, "getSpigot");
            barrelGetTime = firstMethod(barrelClass, "getTime", "getAge");
            barrelGetInventory = findMethod(barrelClass, "getInventory");
            barrelIsSmall = firstMethod(barrelClass, "isSmall", "getSmall");

            brewGet = findMethod(brewClass, "get", ItemStack.class);
            brewGetAgeTime = firstMethod(brewClass, "getAgeTime", "getAge");
            brewGetQuality = findMethod(brewClass, "getQuality");
            brewGetAlc = findMethod(brewClass, "getAlc");
            brewGetOrCalcAlc = findMethod(brewClass, "getOrCalcAlc");
            brewGetDistillRuns = findMethod(brewClass, "getDistillRuns");
            brewGetWood = findMethod(brewClass, "getWood");
            brewGetRecipe = findMethod(brewClass, "getCurrentRecipe");
            brewIsStripped = findMethod(brewClass, "isStripped");

            barrelsReady = barrelsField != null && barrelGetSpigot != null && brewGet != null;
            if (!barrelsReady) {
                plugin.getLogger().warning("BreweryX 酒桶 API 不完整，酒桶全息已停用。");
            }
        } else {
            plugin.getLogger().warning("未能加载 BreweryX 酒桶类，酒桶全息已停用。");
        }

        if (recipeClass != null) {
            recipesField = findField(recipeClass, "recipes");
            recipeGetId = findMethod(recipeClass, "getId");
            recipeGetName = findMethod(recipeClass, "getRecipeName");
            recipeGetColor = findMethod(recipeClass, "getColor");
            recipeGetIngredients = findMethod(recipeClass, "getIngredients");
            recipeGetCookingTime = findMethod(recipeClass, "getCookingTime");
            recipeGetDistillRuns = findMethod(recipeClass, "getDistillruns");
            recipeGetDistillTime = findMethod(recipeClass, "getDistillTime");
            recipeGetAge = findMethod(recipeClass, "getAge");
            recipeGetAlcohol = findMethod(recipeClass, "getAlcohol");
            recipeGetDifficulty = findMethod(recipeClass, "getDifficulty");
            recipeGetWood = findMethod(recipeClass, "getWood");

            if (recipeItemClass != null) {
                recipeItemGetAmount = findMethod(recipeItemClass, "getAmount");
                recipeItemGetMaterials = findMethod(recipeItemClass, "getMaterials");
                recipeItemGetConfigId = findMethod(recipeItemClass, "getConfigId");
                recipeItemGetDebugId = findMethod(recipeItemClass, "getDebugID");
            }
            recipesReady = recipeGetName != null && (recipesField != null || findMethod(recipeClass, "getAllRecipes") != null);
        }

        if (cauldronClass != null && ingredientsClass != null) {
            bcauldronsField = findField(cauldronClass, "bcauldrons");
            cauldronGetBlock = findMethod(cauldronClass, "getBlock");
            cauldronGetState = findMethod(cauldronClass, "getState");
            cauldronGetIngredients = findMethod(cauldronClass, "getIngredients");
            ingredientsGetList = findMethod(ingredientsClass, "getIngredientList");
            ingredientsGetField = findMethod(ingredientsClass, "getIngredients");
            if (ingredientClass != null) {
                ingredientGetAmount = findMethod(ingredientClass, "getAmount");
                ingredientGetDebugId = findMethod(ingredientClass, "getDebugID");
            }
            cauldronsReady = bcauldronsField != null && cauldronGetBlock != null;
            if (!cauldronsReady) {
                plugin.getLogger().warning("BreweryX 炼药锅 API 不完整，炼药锅全息已停用。");
            }
        } else {
            plugin.getLogger().warning("未能加载 BreweryX 炼药锅类，炼药锅全息已停用。");
        }

        reloadRecipes();
    }

    public void reloadRecipes() {
        List<RecipeInfo> loaded = new ArrayList<>();
        if (recipesReady) {
            for (Object recipe : recipeObjects()) {
                RecipeInfo info = readRecipe(recipe);
                if (info != null) {
                    loaded.add(info);
                }
            }
        }
        recipes = loaded;
    }

    public boolean barrelsReady() {
        return barrelsReady;
    }

    public boolean cauldronsReady() {
        return cauldronsReady;
    }

    public boolean recipesReady() {
        return recipesReady;
    }

    public List<RecipeInfo> recipes() {
        return recipes;
    }

    public List<Object> getBarrels() {
        if (!barrelsReady) {
            return Collections.emptyList();
        }
        return readMapValues(barrelsField);
    }

    public List<Object> getCauldrons() {
        if (!cauldronsReady) {
            return Collections.emptyList();
        }
        return readMapValues(bcauldronsField);
    }

    public BarrelSnapshot readBarrel(Object barrel) {
        try {
            Block spigot = asBlock(call(barrelGetSpigot, barrel));
            if (spigot == null) {
                return null;
            }
            float barrelTime = toFloat(call(barrelGetTime, barrel));
            boolean small = toBoolean(call(barrelIsSmall, barrel));
            Inventory inventory = asInventory(call(barrelGetInventory, barrel));

            int totalSlots = inventory == null ? 0 : inventory.getSize();
            int usedSlots = 0;
            List<BottleInfo> bottles = new ArrayList<>();
            if (inventory != null) {
                for (ItemStack item : inventory.getContents()) {
                    if (item == null || item.getType().isAir()) {
                        continue;
                    }
                    usedSlots++;
                    Object brew = call(brewGet, null, item);
                    if (brew != null) {
                        bottles.add(readBottle(brew, item, barrelTime));
                    }
                }
            }
            return new BarrelSnapshot(spigot.getLocation(), barrelTime, small, totalSlots, usedSlots, bottles);
        } catch (Throwable t) {
            plugin.getLogger().warning("读取酒桶数据失败: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return null;
        }
    }

    public CauldronSnapshot readCauldron(Object cauldron) {
        try {
            Block block = asBlock(call(cauldronGetBlock, cauldron));
            if (block == null || !block.getType().name().contains("CAULDRON")) {
                return null;
            }
            int minutes = toInt(call(cauldronGetState, cauldron));
            List<IngredientInfo> ingredients = new ArrayList<>();
            Object bag = call(cauldronGetIngredients, cauldron);
            if (bag != null) {
                List<?> list = asList(call(ingredientsGetList, bag));
                if (list == null) {
                    list = asList(call(ingredientsGetField, bag));
                }
                if (list != null) {
                    for (Object ingredient : list) {
                        if (ingredient != null) {
                            ingredients.add(readIngredient(ingredient));
                        }
                    }
                }
            }
            if (minutes <= 0 && ingredients.isEmpty()) {
                return null;
            }
            return new CauldronSnapshot(block.getLocation(), minutes, ingredients);
        } catch (Throwable t) {
            plugin.getLogger().warning("读取炼药锅数据失败: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return null;
        }
    }

    public BrewSale readBrewSale(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        Object brew = call(brewGet, null, item);
        if (brew == null) {
            return null;
        }
        Object recipe = call(brewGetRecipe, brew);
        if (recipe == null) {
            return null;
        }
        String key = recipeKeyOf(recipe);
        if (key == null) {
            return null;
        }
        String name = recipeNameOf(recipe);
        if (name == null || name.isEmpty()) {
            name = key;
        }
        int quality = toInt(call(brewGetQuality, brew));
        int alcohol = toInt(call(brewGetAlc, brew));
        if (alcohol <= 0) {
            alcohol = toInt(call(brewGetOrCalcAlc, brew));
        }
        return new BrewSale(key, name, quality, alcohol);
    }

    public String recipeKeyOf(Object recipe) {
        String id = stringOf(call(recipeGetId, recipe));
        if (id != null && !id.isEmpty()) {
            return id;
        }
        return recipeNameOf(recipe);
    }

    public String recipeNameOf(Object recipe) {
        String name = stringOf(call(recipeGetName, recipe));
        return name == null ? null : Text.strip(name);
    }

    private BottleInfo readBottle(Object brew, ItemStack item, float barrelTime) {
        float ageTime = toFloat(call(brewGetAgeTime, brew));
        float effectiveAge = ageTime + barrelTime;
        int quality = toInt(call(brewGetQuality, brew));
        int alcohol = toInt(call(brewGetAlc, brew));
        if (alcohol <= 0) {
            int calculated = toInt(call(brewGetOrCalcAlc, brew));
            if (calculated > 0) {
                alcohol = calculated;
            }
        }
        int distillRuns = toInt(call(brewGetDistillRuns, brew));
        boolean stripped = toBoolean(call(brewIsStripped, brew));

        Object wood = call(brewGetWood, brew);
        String woodKey = null;
        String woodLabel = "";
        if (wood instanceof Enum<?> woodEnum) {
            woodKey = woodEnum.name();
            woodLabel = Text.prettify(woodKey);
        }

        Object recipe = call(brewGetRecipe, brew);
        String name = null;
        String recipeKey = null;
        int requiredDistillRuns = 0;
        float recipeAge = 0f;
        boolean needsAge = false;
        boolean needsDistill = false;
        boolean cookingOnly = false;

        if (recipe != null) {
            name = recipeNameOf(recipe);
            recipeKey = recipeKeyOf(recipe);
            requiredDistillRuns = toInt(call(recipeGetDistillRuns, recipe));
            recipeAge = toFloat(call(recipeGetAge, recipe));
            needsAge = recipeAge > 0;
            needsDistill = requiredDistillRuns > 0;
            cookingOnly = !needsAge && !needsDistill;
        }
        if (name == null || name.isEmpty()) {
            name = fallbackName(item);
        }

        BottleStatus status;
        if (stripped) {
            status = BottleStatus.STRIPPED;
        } else if (recipe == null) {
            status = BottleStatus.UNKNOWN;
        } else if (cookingOnly) {
            status = BottleStatus.COOKING_ONLY;
        } else if (needsDistill && distillRuns < requiredDistillRuns) {
            status = BottleStatus.NEEDS_DISTILL;
        } else if (needsAge && effectiveAge < recipeAge) {
            status = BottleStatus.AGING;
        } else {
            status = BottleStatus.READY;
        }

        return new BottleInfo(name, recipeKey, quality, effectiveAge, alcohol, distillRuns, requiredDistillRuns,
                recipeAge, needsAge, needsDistill, woodKey, woodLabel, status);
    }

    private IngredientInfo readIngredient(Object ingredient) {
        int amount = Math.max(1, toInt(call(ingredientGetAmount, ingredient), 1));
        Method materialMethod = cachedMethod(ingredient.getClass(), "getMaterial");
        if (materialMethod != null) {
            Object value = call(materialMethod, ingredient);
            if (value instanceof Material material) {
                return new IngredientInfo(material.name(), Text.prettify(material.name()), amount);
            }
        }
        Method itemMethod = cachedMethod(ingredient.getClass(), "getItem");
        if (itemMethod != null) {
            Object value = call(itemMethod, ingredient);
            if (value instanceof ItemStack item && item.getType() != Material.AIR) {
                return new IngredientInfo(item.getType().name(), Text.prettify(item.getType().name()), amount);
            }
        }
        Object debug = call(ingredientGetDebugId, ingredient);
        String fallback = debug != null ? Text.strip(debug.toString()) : Text.prettify(ingredient.getClass().getSimpleName());
        return new IngredientInfo(null, fallback, amount);
    }

    private RecipeInfo readRecipe(Object recipe) {
        String name = recipeNameOf(recipe);
        if (name == null || name.isEmpty()) {
            return null;
        }
        String key = recipeKeyOf(recipe);
        if (key == null || key.isEmpty()) {
            key = name;
        }
        String color = stringOf(call(recipeGetColor, recipe));
        List<IngredientLine> ingredients = new ArrayList<>();
        List<?> items = asList(call(recipeGetIngredients, recipe));
        if (items != null) {
            for (Object item : items) {
                if (item != null) {
                    ingredients.add(readRecipeIngredient(item));
                }
            }
        }
        int cooking = toInt(call(recipeGetCookingTime, recipe));
        int distillRuns = toInt(call(recipeGetDistillRuns, recipe));
        int distillTicks = toInt(call(recipeGetDistillTime, recipe));
        int age = toInt(call(recipeGetAge, recipe));
        int alcohol = toInt(call(recipeGetAlcohol, recipe));
        int difficulty = toInt(call(recipeGetDifficulty, recipe));

        Object wood = call(recipeGetWood, recipe);
        String woodKey = null;
        String woodLabel = "";
        if (wood instanceof Enum<?> woodEnum) {
            woodKey = woodEnum.name();
            woodLabel = Text.prettify(woodKey);
        }
        boolean cookingOnly = age == 0 && distillRuns == 0;
        return new RecipeInfo(key, name, color, ingredients, cooking, distillRuns, Math.max(0, distillTicks / 20),
                age, alcohol, difficulty, woodKey, woodLabel, cookingOnly);
    }

    private IngredientLine readRecipeIngredient(Object item) {
        int amount = Math.max(1, toInt(call(recipeItemGetAmount, item), 1));
        List<?> materials = asList(call(recipeItemGetMaterials, item));
        if (materials != null && !materials.isEmpty() && materials.get(0) instanceof Material material) {
            return new IngredientLine(material.name(), Text.prettify(material.name()), amount);
        }
        String configId = stringOf(call(recipeItemGetConfigId, item));
        if (configId != null && !configId.isEmpty()) {
            return new IngredientLine(configId, configId, amount);
        }
        String debugId = stringOf(call(recipeItemGetDebugId, item));
        if (debugId != null && !debugId.isEmpty()) {
            return new IngredientLine(debugId, Text.prettify(debugId), amount);
        }
        return new IngredientLine(null, Text.prettify(item.getClass().getSimpleName()), amount);
    }

    private List<Object> recipeObjects() {
        if (recipesField == null) {
            return Collections.emptyList();
        }
        Object value;
        try {
            value = recipesField.get(null);
        } catch (Throwable t) {
            return Collections.emptyList();
        }
        if (!(value instanceof List<?> list)) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    private String fallbackName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() != null) {
            String display = item.getItemMeta().getDisplayName();
            if (display != null && !display.isEmpty()) {
                return Text.strip(display);
            }
        }
        return Text.prettify(item.getType().name());
    }

    private Method cachedMethod(Class<?> owner, String name) {
        String key = owner.getName() + "#" + name;
        Optional<Method> cached = methodCache.get(key);
        if (cached != null) {
            return cached.orElse(null);
        }
        Method method = findMethod(owner, name);
        methodCache.put(key, Optional.ofNullable(method));
        return method;
    }

    private List<Object> readMapValues(Field field) {
        Object value;
        try {
            value = field.get(null);
        } catch (Throwable t) {
            return Collections.emptyList();
        }
        if (!(value instanceof Map<?, ?> map)) {
            return Collections.emptyList();
        }
        LinkedHashSet<Object> unique = new LinkedHashSet<>();
        for (Object entry : map.values()) {
            if (entry instanceof List<?> list) {
                for (Object item : list) {
                    if (item != null) {
                        unique.add(item);
                    }
                }
            } else if (entry != null) {
                unique.add(entry);
            }
        }
        return new ArrayList<>(unique);
    }

    private Object call(Method method, Object target, Object... args) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(target, args);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Method findMethod(Class<?> owner, String name, Class<?>... parameterTypes) {
        if (owner == null) {
            return null;
        }
        try {
            return owner.getMethod(name, parameterTypes);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Method firstMethod(Class<?> owner, String... names) {
        for (String name : names) {
            Method method = findMethod(owner, name);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    private static Field findField(Class<?> owner, String name) {
        Class<?> current = owner;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (Throwable t) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static Block asBlock(Object value) {
        return value instanceof Block block ? block : null;
    }

    private static Inventory asInventory(Object value) {
        return value instanceof Inventory inventory ? inventory : null;
    }

    private static List<?> asList(Object value) {
        return value instanceof List<?> list ? list : null;
    }

    private static String stringOf(Object value) {
        return value == null ? null : value.toString();
    }

    private static float toFloat(Object value) {
        return value instanceof Number number ? number.floatValue() : 0f;
    }

    private static int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static int toInt(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static boolean toBoolean(Object value) {
        return value instanceof Boolean bool && bool;
    }

    private static boolean toBoolean(Object value, boolean fallback) {
        return value instanceof Boolean bool ? bool : fallback;
    }
}
