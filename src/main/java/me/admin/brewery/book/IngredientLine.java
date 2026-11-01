package me.admin.brewery.book;

public final class IngredientLine {

    private final String key;
    private final String label;
    private final int amount;

    public IngredientLine(String key, String label, int amount) {
        this.key = key;
        this.label = label;
        this.amount = amount;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public int getAmount() {
        return amount;
    }
}
