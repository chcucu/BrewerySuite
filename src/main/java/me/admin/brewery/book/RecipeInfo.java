package me.admin.brewery.book;

import java.util.List;

public final class RecipeInfo {

    private final String key;
    private final String name;
    private final String color;
    private final List<IngredientLine> ingredients;
    private final int cookingMinutes;
    private final int distillRuns;
    private final int distillSeconds;
    private final int ageYears;
    private final int alcohol;
    private final int difficulty;
    private final String woodKey;
    private final String woodLabel;
    private final boolean cookingOnly;

    public RecipeInfo(String key, String name, String color, List<IngredientLine> ingredients, int cookingMinutes,
                      int distillRuns, int distillSeconds, int ageYears, int alcohol, int difficulty,
                      String woodKey, String woodLabel, boolean cookingOnly) {
        this.key = key;
        this.name = name;
        this.color = color;
        this.ingredients = ingredients;
        this.cookingMinutes = cookingMinutes;
        this.distillRuns = distillRuns;
        this.distillSeconds = distillSeconds;
        this.ageYears = ageYears;
        this.alcohol = alcohol;
        this.difficulty = difficulty;
        this.woodKey = woodKey;
        this.woodLabel = woodLabel;
        this.cookingOnly = cookingOnly;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public List<IngredientLine> getIngredients() {
        return ingredients;
    }

    public int getCookingMinutes() {
        return cookingMinutes;
    }

    public int getDistillRuns() {
        return distillRuns;
    }

    public int getDistillSeconds() {
        return distillSeconds;
    }

    public int getAgeYears() {
        return ageYears;
    }

    public int getAlcohol() {
        return alcohol;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getWoodKey() {
        return woodKey;
    }

    public String getWoodLabel() {
        return woodLabel;
    }

    public boolean isCookingOnly() {
        return cookingOnly;
    }
}
