package me.admin.brewery.model;

import org.bukkit.Location;

import java.util.List;

public final class BarrelSnapshot {
    private final Location location;
    private final float barrelAge;
    private final boolean small;
    private final int totalSlots;
    private final int usedSlots;
    private final List<BottleInfo> bottles;

    public BarrelSnapshot(Location location, float barrelAge, boolean small, int totalSlots, int usedSlots,
                          List<BottleInfo> bottles) {
        this.location = location;
        this.barrelAge = barrelAge;
        this.small = small;
        this.totalSlots = totalSlots;
        this.usedSlots = usedSlots;
        this.bottles = bottles;
    }

    public Location getLocation() {
        return location;
    }

    public float getBarrelAge() {
        return barrelAge;
    }

    public boolean isSmall() {
        return small;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getUsedSlots() {
        return usedSlots;
    }

    public List<BottleInfo> getBottles() {
        return bottles;
    }
}
