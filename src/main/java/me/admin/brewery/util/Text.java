package me.admin.brewery.util;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.Locale;

public final class Text {

    private Text() {
    }

    public static String color(String input) {
        return input == null ? "" : ChatColor.translateAlternateColorCodes('&', input);
    }

    public static String strip(String input) {
        if (input == null) {
            return "";
        }
        return ChatColor.stripColor(color(input));
    }

    public static String prettify(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        String s = key.replace('_', ' ').toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(s.length());
        boolean upper = true;
        for (char c : s.toCharArray()) {
            if (c == ' ') {
                upper = true;
                out.append(c);
            } else if (upper) {
                out.append(Character.toUpperCase(c));
                upper = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String age(float years) {
        String s = String.format(Locale.ROOT, "%.2f", years);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
            s = s.substring(0, s.length() - 1);
        }
        return s.isEmpty() || s.equals("-") ? "0" : s;
    }

    public static String number(double value) {
        if (value >= 10) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        return age((float) value);
    }

    public static String join(List<String> parts, String separator) {
        if (parts.isEmpty()) {
            return "";
        }
        return String.join(separator, parts);
    }

    public static String minutes(int minutes) {
        return Integer.toString(minutes);
    }

    private static final ChatColor[] PALETTE = {
            ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA,
            ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
            ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA,
            ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE
    };

    public static String legacyColor(String hex) {
        if (hex == null || !hex.matches("#[0-9a-fA-F]{6}")) {
            return ChatColor.WHITE.toString();
        }
        int value = Integer.parseInt(hex.substring(1), 16);
        int red = (value >> 16) & 0xFF;
        int green = (value >> 8) & 0xFF;
        int blue = value & 0xFF;
        ChatColor best = ChatColor.WHITE;
        int bestDistance = Integer.MAX_VALUE;
        for (ChatColor candidate : PALETTE) {
            int[] rgb = rgbOf(candidate);
            int distance = (red - rgb[0]) * (red - rgb[0])
                    + (green - rgb[1]) * (green - rgb[1])
                    + (blue - rgb[2]) * (blue - rgb[2]);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best.toString();
    }

    private static int[] rgbOf(ChatColor color) {
        return switch (color) {
            case BLACK -> new int[]{0, 0, 0};
            case DARK_BLUE -> new int[]{0, 0, 170};
            case DARK_GREEN -> new int[]{0, 170, 0};
            case DARK_AQUA -> new int[]{0, 170, 170};
            case DARK_RED -> new int[]{170, 0, 0};
            case DARK_PURPLE -> new int[]{170, 0, 170};
            case GOLD -> new int[]{255, 170, 0};
            case GRAY -> new int[]{170, 170, 170};
            case DARK_GRAY -> new int[]{85, 85, 85};
            case BLUE -> new int[]{85, 85, 255};
            case GREEN -> new int[]{85, 255, 85};
            case AQUA -> new int[]{85, 255, 255};
            case RED -> new int[]{255, 85, 85};
            case LIGHT_PURPLE -> new int[]{255, 85, 255};
            case YELLOW -> new int[]{255, 255, 85};
            default -> new int[]{255, 255, 255};
        };
    }
}
