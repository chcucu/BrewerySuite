package me.admin.brewery.market;

public final class BrewSale {

    private final String recipeKey;
    private final String name;
    private final int quality;
    private final int alcohol;

    public BrewSale(String recipeKey, String name, int quality, int alcohol) {
        this.recipeKey = recipeKey;
        this.name = name;
        this.quality = quality;
        this.alcohol = alcohol;
    }

    public String getRecipeKey() {
        return recipeKey;
    }

    public String getName() {
        return name;
    }

    public int getQuality() {
        return quality;
    }

    public int getAlcohol() {
        return alcohol;
    }

    public double score() {
        double quality = Math.max(1, Math.min(10, this.quality));
        return quality / 10.0;
    }
}
