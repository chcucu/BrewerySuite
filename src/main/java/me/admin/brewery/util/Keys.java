package me.admin.brewery.util;

import org.bukkit.Location;

public final class Keys {

    public static final String PREFIX = "bs_";

    private Keys() {
    }

    public static String barrel(Location location) {
        return PREFIX + "b_" + world(location) + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    public static String cauldron(Location location) {
        return PREFIX + "c_" + world(location) + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    private static String world(Location location) {
        String name = location.getWorld() == null ? "world" : location.getWorld().getName();
        StringBuilder out = new StringBuilder(name.length());
        for (char c : name.toCharArray()) {
            out.append(Character.isLetterOrDigit(c) || c == '_' ? c : '_');
        }
        return out.toString();
    }
}
