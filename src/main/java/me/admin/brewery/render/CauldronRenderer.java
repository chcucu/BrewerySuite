package me.admin.brewery.render;

import me.admin.brewery.Messages;
import me.admin.brewery.Settings;
import me.admin.brewery.model.CauldronSnapshot;
import me.admin.brewery.model.IngredientInfo;

import java.util.ArrayList;
import java.util.List;

public final class CauldronRenderer {

    private final Settings settings;
    private final Messages messages;

    public CauldronRenderer(Settings settings, Messages messages) {
        this.settings = settings;
        this.messages = messages;
    }

    public List<String> render(CauldronSnapshot snapshot, String language, boolean detailed) {
        return detailed ? renderDetailed(snapshot, language) : renderBrief(snapshot, language);
    }

    private List<String> renderDetailed(CauldronSnapshot snapshot, String language) {
        List<String> lines = new ArrayList<>();
        lines.add(messages.format(language, "cauldron.header",
                "minutes", Integer.toString(snapshot.getMinutes()),
                "count", Integer.toString(snapshot.getIngredients().size())));

        List<IngredientInfo> ingredients = snapshot.getIngredients();
        if (ingredients.isEmpty()) {
            lines.add(messages.raw(language, "cauldron.empty"));
            return lines;
        }

        int limit = Math.min(settings.maxIngredientLines(), ingredients.size());
        for (int i = 0; i < limit; i++) {
            IngredientInfo ingredient = ingredients.get(i);
            lines.add(messages.format(language, "cauldron.ingredient",
                    "name", messages.material(language, ingredient.getKey(), ingredient.getFallbackName()),
                    "amount", Integer.toString(ingredient.getAmount())));
        }
        if (ingredients.size() > limit) {
            lines.add(messages.format(language, "cauldron.more",
                    "count", Integer.toString(ingredients.size() - limit)));
        }
        return lines;
    }

    private List<String> renderBrief(CauldronSnapshot snapshot, String language) {
        List<String> lines = new ArrayList<>();
        lines.add(messages.format(language, "cauldron.brief",
                "minutes", Integer.toString(snapshot.getMinutes()),
                "count", Integer.toString(snapshot.getIngredients().size())));
        return lines;
    }
}
