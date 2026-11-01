package me.admin.brewery.model;

public final class BottleInfo {
    private final String name;
    private final String recipeKey;
    private final int quality;
    private final float age;
    private final int alcohol;
    private final int distillRuns;
    private final int requiredDistillRuns;
    private final float recipeAge;
    private final boolean needsAge;
    private final boolean needsDistill;
    private final String woodKey;
    private final String woodLabel;
    private final BottleStatus status;

    public BottleInfo(String name, String recipeKey, int quality, float age, int alcohol, int distillRuns,
                      int requiredDistillRuns, float recipeAge, boolean needsAge, boolean needsDistill,
                      String woodKey, String woodLabel, BottleStatus status) {
        this.name = name;
        this.recipeKey = recipeKey;
        this.quality = quality;
        this.age = age;
        this.alcohol = alcohol;
        this.distillRuns = distillRuns;
        this.requiredDistillRuns = requiredDistillRuns;
        this.recipeAge = recipeAge;
        this.needsAge = needsAge;
        this.needsDistill = needsDistill;
        this.woodKey = woodKey;
        this.woodLabel = woodLabel;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getRecipeKey() {
        return recipeKey;
    }

    public int getQuality() {
        return quality;
    }

    public float getAge() {
        return age;
    }

    public int getAlcohol() {
        return alcohol;
    }

    public int getDistillRuns() {
        return distillRuns;
    }

    public int getRequiredDistillRuns() {
        return requiredDistillRuns;
    }

    public float getRecipeAge() {
        return recipeAge;
    }

    public boolean needsAge() {
        return needsAge;
    }

    public boolean needsDistill() {
        return needsDistill;
    }

    public String getWoodKey() {
        return woodKey;
    }

    public String getWoodLabel() {
        return woodLabel;
    }

    public BottleStatus getStatus() {
        return status;
    }
}
