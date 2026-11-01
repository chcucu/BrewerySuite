package me.admin.brewery.model;

import org.bukkit.Location;

import java.util.List;

public final class CauldronSnapshot {
    private final Location location;
    private final int minutes;
    private final List<IngredientInfo> ingredients;

    public CauldronSnapshot(Location location, int minutes, List<IngredientInfo> ingredients) {
        this.location = location;
        this.minutes = minutes;
        this.ingredients = ingredients;
    }

    public Location getLocation() {
        return location;
    }

    public int getMinutes() {
        return minutes;
    }

    public List<IngredientInfo> getIngredients() {
        return ingredients;
    }
}
