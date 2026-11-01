package me.admin.brewery.render;

import me.admin.brewery.Messages;
import me.admin.brewery.Settings;
import me.admin.brewery.model.BarrelSnapshot;
import me.admin.brewery.model.BottleInfo;
import me.admin.brewery.model.BottleStatus;
import me.admin.brewery.util.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BarrelRenderer {

    private final Settings settings;
    private final Messages messages;

    public BarrelRenderer(Settings settings, Messages messages) {
        this.settings = settings;
        this.messages = messages;
    }

    public List<String> render(BarrelSnapshot snapshot, String language, boolean detailed) {
        return detailed ? renderDetailed(snapshot, language) : renderBrief(snapshot, language);
    }

    private List<String> renderDetailed(BarrelSnapshot snapshot, String language) {
        List<String> lines = new ArrayList<>();
        List<BottleInfo> bottles = snapshot.getBottles();

        if (bottles.isEmpty()) {
            lines.add(messages.format(language, "barrel.header_empty",
                    "barrel_age", Text.age(snapshot.getBarrelAge())));
            lines.add(messages.raw(language, "barrel.empty_line"));
            lines.add(infoLine(snapshot, language));
            return lines;
        }

        lines.add(messages.format(language, "barrel.header",
                "count", Integer.toString(bottles.size()),
                "barrel_age", Text.age(snapshot.getBarrelAge())));

        int limit = Math.min(settings.maxBottleLines(), bottles.size());
        for (int i = 0; i < limit; i++) {
            BottleInfo bottle = bottles.get(i);
            lines.add(messages.format(language, "barrel.bottle",
                    "i", Integer.toString(i + 1),
                    "name", bottle.getName(),
                    "quality", bottle.getQuality() + "/10",
                    "age", Text.age(bottle.getAge()),
                    "alc", Integer.toString(bottle.getAlcohol()),
                    "distill", distillText(bottle),
                    "wood", messages.wood(language, bottle.getWoodKey(), bottle.getWoodLabel()),
                    "status", statusText(language, bottle)));
        }

        if (bottles.size() > limit) {
            lines.add(messages.format(language, "barrel.summary_more",
                    "count", Integer.toString(bottles.size() - limit),
                    "list", summaryList(language, bottles.subList(limit, bottles.size()))));
        }

        lines.add(infoLine(snapshot, language));
        return lines;
    }

    private List<String> renderBrief(BarrelSnapshot snapshot, String language) {
        List<String> lines = new ArrayList<>();
        List<BottleInfo> bottles = snapshot.getBottles();
        if (bottles.isEmpty()) {
            lines.add(messages.raw(language, "barrel.brief.empty"));
            return lines;
        }
        double totalAge = 0.0;
        for (BottleInfo bottle : bottles) {
            totalAge += bottle.getAge();
        }
        double average = totalAge / bottles.size();
        int remainder = remainingMinutes(snapshot);
        String remaining = remainder < 0
                ? messages.raw(language, "barrel.no_remaining")
                : messages.format(language, "barrel.remaining", "minutes", Text.minutes(remainder));
        lines.add(messages.format(language, "barrel.brief.filled",
                "years", Text.number(average),
                "remaining", remaining,
                "count", Integer.toString(bottles.size())));
        return lines;
    }

    private String infoLine(BarrelSnapshot snapshot, String language) {
        int remainder = remainingMinutes(snapshot);
        String remaining = remainder < 0
                ? messages.raw(language, "barrel.no_remaining")
                : messages.format(language, "barrel.remaining", "minutes", Text.minutes(remainder));
        String size = messages.raw(language, snapshot.isSmall() ? "barrel.size_small" : "barrel.size_large");
        return messages.format(language, "barrel.info",
                "size", size,
                "used", Integer.toString(snapshot.getUsedSlots()),
                "total", Integer.toString(snapshot.getTotalSlots()),
                "barrel_age", Text.age(snapshot.getBarrelAge()),
                "remaining", remaining);
    }

    private int remainingMinutes(BarrelSnapshot snapshot) {
        int longest = -1;
        for (BottleInfo bottle : snapshot.getBottles()) {
            if (bottle.getStatus() != BottleStatus.AGING) {
                continue;
            }
            int minutes = (int) Math.ceil(Math.max(0f, bottle.getRecipeAge() - bottle.getAge()) * settings.agingMinutesPerYear());
            if (minutes > longest) {
                longest = minutes;
            }
        }
        return longest;
    }

    private String distillText(BottleInfo bottle) {
        if (bottle.needsDistill()) {
            return bottle.getDistillRuns() + "/" + bottle.getRequiredDistillRuns();
        }
        return Integer.toString(bottle.getDistillRuns());
    }

    private String statusText(String language, BottleInfo bottle) {
        return switch (bottle.getStatus()) {
            case READY -> messages.raw(language, "barrel.status.ready");
            case AGING -> {
                int minutes = (int) Math.ceil(Math.max(0f, bottle.getRecipeAge() - bottle.getAge()) * settings.agingMinutesPerYear());
                yield messages.format(language, "barrel.status.aging", "minutes", Text.minutes(minutes));
            }
            case NEEDS_DISTILL -> messages.raw(language, "barrel.status.needs_distill");
            case COOKING_ONLY -> messages.raw(language, "barrel.status.cooking_only");
            case UNKNOWN -> messages.raw(language, "barrel.status.unknown");
            case STRIPPED -> messages.raw(language, "barrel.status.stripped");
        };
    }

    private String summaryList(String language, List<BottleInfo> bottles) {
        Map<String, Integer> grouped = new LinkedHashMap<>();
        for (BottleInfo bottle : bottles) {
            grouped.merge(bottle.getName(), 1, Integer::sum);
        }
        List<String> parts = new ArrayList<>();
        String multiply = messages.raw(language, "barrel.multiply");
        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            parts.add(entry.getKey() + " " + multiply + entry.getValue());
        }
        return Text.join(parts, messages.raw(language, "barrel.list_separator"));
    }
}
