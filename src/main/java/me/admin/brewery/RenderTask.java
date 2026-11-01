package me.admin.brewery;

import me.admin.brewery.model.BarrelSnapshot;
import me.admin.brewery.model.CauldronSnapshot;
import me.admin.brewery.render.BarrelRenderer;
import me.admin.brewery.render.CauldronRenderer;
import me.admin.brewery.util.Keys;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RenderTask {

    private final Settings settings;
    private final BreweryHook hook;
    private final Holograms holograms;
    private final BarrelRenderer barrelRenderer;
    private final CauldronRenderer cauldronRenderer;

    public RenderTask(Settings settings, Messages messages, BreweryHook hook, Holograms holograms) {
        this.settings = settings;
        this.hook = hook;
        this.holograms = holograms;
        this.barrelRenderer = new BarrelRenderer(settings, messages);
        this.cauldronRenderer = new CauldronRenderer(settings, messages);
    }

    public void tick() {
        Set<String> present = new HashSet<>();

        if (settings.barrelsEnabled() && hook.barrelsReady()) {
            for (Object barrel : hook.getBarrels()) {
                BarrelSnapshot snapshot = hook.readBarrel(barrel);
                if (snapshot == null) {
                    continue;
                }
                if (snapshot.getBottles().isEmpty() && !settings.showEmptyBarrels()) {
                    continue;
                }
                String key = Keys.barrel(snapshot.getLocation());
                present.add(key);
                holograms.upsert(key, holoLocation(snapshot.getLocation(), settings.barrelOffsetX(),
                        settings.barrelOffsetY(), settings.barrelOffsetZ()), barrelPages(snapshot));
            }
        }

        if (settings.cauldronsEnabled() && hook.cauldronsReady()) {
            for (Object cauldron : hook.getCauldrons()) {
                CauldronSnapshot snapshot = hook.readCauldron(cauldron);
                if (snapshot == null) {
                    continue;
                }
                String key = Keys.cauldron(snapshot.getLocation());
                present.add(key);
                holograms.upsert(key, holoLocation(snapshot.getLocation(), settings.cauldronOffsetX(),
                        settings.cauldronOffsetY(), settings.cauldronOffsetZ()), cauldronPages(snapshot));
            }
        }

        holograms.retainOnly(present);
        holograms.updateNearby();
    }

    private List<List<String>> barrelPages(BarrelSnapshot snapshot) {
        List<List<String>> pages = new ArrayList<>(Settings.LANGUAGES.size() * 2);
        for (String language : Settings.LANGUAGES) {
            pages.add(barrelRenderer.render(snapshot, language, false));
            pages.add(barrelRenderer.render(snapshot, language, true));
        }
        return pages;
    }

    private List<List<String>> cauldronPages(CauldronSnapshot snapshot) {
        List<List<String>> pages = new ArrayList<>(Settings.LANGUAGES.size() * 2);
        for (String language : Settings.LANGUAGES) {
            pages.add(cauldronRenderer.render(snapshot, language, false));
            pages.add(cauldronRenderer.render(snapshot, language, true));
        }
        return pages;
    }

    private static Location holoLocation(Location base, double x, double y, double z) {
        return base.clone().add(x, y, z);
    }
}
