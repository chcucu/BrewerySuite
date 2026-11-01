package me.admin.brewery.model;

public final class IngredientInfo {
    private final String key;
    private final String fallbackName;
    private final int amount;

    public IngredientInfo(String key, String fallbackName, int amount) {
        this.key = key;
        this.fallbackName = fallbackName;
        this.amount = amount;
    }

    public String getKey() {
        return key;
    }

    public String getFallbackName() {
        return fallbackName;
    }

    public int getAmount() {
        return amount;
    }
}
