package me.admin.brewery;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PlayerPrefs {

    private final Settings settings;
    private final NamespacedKey showKey;
    private final NamespacedKey modeKey;
    private final NamespacedKey languageKey;
    private final NamespacedKey nearbyKey;

    public PlayerPrefs(BrewerySuitePlugin plugin, Settings settings) {
        this.settings = settings;
        this.showKey = new NamespacedKey(plugin, "show");
        this.modeKey = new NamespacedKey(plugin, "mode");
        this.languageKey = new NamespacedKey(plugin, "language");
        this.nearbyKey = new NamespacedKey(plugin, "nearby");
    }

    public PlayerPref get(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        boolean show = data.getOrDefault(showKey, PersistentDataType.BYTE, (byte) 1) != 0;
        String mode = Settings.normalizeMode(data.getOrDefault(modeKey, PersistentDataType.STRING, settings.defaultMode()));
        String language = Settings.normalizeLanguage(data.getOrDefault(languageKey, PersistentDataType.STRING, settings.defaultLanguage()));
        boolean nearby = data.getOrDefault(nearbyKey, PersistentDataType.BYTE, (byte) (settings.defaultNearby() ? 1 : 0)) != 0;
        return new PlayerPref(show, mode, language, nearby);
    }

    public void setShow(Player player, boolean show) {
        player.getPersistentDataContainer().set(showKey, PersistentDataType.BYTE, (byte) (show ? 1 : 0));
    }

    public void setMode(Player player, String mode) {
        player.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, Settings.normalizeMode(mode));
    }

    public void setLanguage(Player player, String language) {
        player.getPersistentDataContainer().set(languageKey, PersistentDataType.STRING, Settings.normalizeLanguage(language));
    }

    public void setNearby(Player player, boolean nearby) {
        player.getPersistentDataContainer().set(nearbyKey, PersistentDataType.BYTE, (byte) (nearby ? 1 : 0));
    }

    public static final class PlayerPref {
        private final boolean show;
        private final String mode;
        private final String language;
        private final boolean nearby;

        public PlayerPref(boolean show, String mode, String language, boolean nearby) {
            this.show = show;
            this.mode = mode;
            this.language = language;
            this.nearby = nearby;
        }

        public boolean show() {
            return show;
        }

        public String mode() {
            return mode;
        }

        public String language() {
            return language;
        }

        public boolean nearby() {
            return nearby;
        }

        public boolean detailed() {
            return "detailed".equals(mode);
        }
    }
}
