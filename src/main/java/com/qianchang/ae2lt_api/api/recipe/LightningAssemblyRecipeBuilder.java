package com.qianchang.ae2lt_api.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for <strong>Lightning Assembly</strong> recipes
 * ({@code ae2lt:lightning_assembly}).
 *
 * <p>Lightning Assembly recipes are processed by the <em>Lightning Assembly Chamber</em>.
 * The machine accepts up to 9 item stacks and consumes FE plus lightning energy to
 * produce the result.</p>
 *
 * <h2>JSON output example</h2>
 * <pre>{@code
 * {
 *   "type": "ae2lt:lightning_assembly",
 *   "priority": 0,
 *   "inputs": [
 *     { "ingredient": { "item": "ae2lt:overload_crystal" }, "count": 1 },
 *     { "ingredient": { "item": "ae2lt:overload_alloy" }, "count": 2 }
 *   ],
 *   "result": { "id": "ae2lt:overload_processor", "count": 1 },
 *   "totalEnergy": 500,
 *   "lightningCost": 4,
 *   "lightningTier": "hv"
 * }
 * }</pre>
 */
public final class LightningAssemblyRecipeBuilder {

    private int priority = 0;
    private final List<InputSpec> inputs = new ArrayList<>();
    private String resultItem;
    private int resultCount = 1;
    private long totalEnergy = 100;
    private int lightningCost = 4;
    private LightningEnergyTier lightningTier = LightningEnergyTier.HIGH_VOLTAGE;

    private LightningAssemblyRecipeBuilder() {}

    public static LightningAssemblyRecipeBuilder create() {
        return new LightningAssemblyRecipeBuilder();
    }

    public LightningAssemblyRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    /** Adds an item ingredient with the given count (max 9 total inputs). */
    public LightningAssemblyRecipeBuilder input(String itemId, int count) {
        Objects.requireNonNull(itemId, "itemId");
        if (inputs.size() >= 9) throw new IllegalStateException("Maximum 9 inputs allowed");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        inputs.add(new InputSpec(itemId, null, count));
        return this;
    }

    /** Adds a tag ingredient with the given count (max 9 total inputs). */
    public LightningAssemblyRecipeBuilder inputTag(String tagId, int count) {
        Objects.requireNonNull(tagId, "tagId");
        if (inputs.size() >= 9) throw new IllegalStateException("Maximum 9 inputs allowed");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        inputs.add(new InputSpec(null, tagId, count));
        return this;
    }

    public LightningAssemblyRecipeBuilder result(String itemId, int count) {
        this.resultItem = Objects.requireNonNull(itemId, "itemId");
        this.resultCount = count;
        return this;
    }

    /**
     * Sets the total FE energy required to complete this recipe.
     * Must be at least 5.
     */
    public LightningAssemblyRecipeBuilder totalEnergy(long totalEnergy) {
        if (totalEnergy < 5) throw new IllegalArgumentException("totalEnergy must be at least 5");
        this.totalEnergy = totalEnergy;
        return this;
    }

    /**
     * Sets the number of lightning units consumed per recipe (default: 4).
     */
    public LightningAssemblyRecipeBuilder lightningCost(int lightningCost) {
        if (lightningCost <= 0) throw new IllegalArgumentException("lightningCost must be positive");
        this.lightningCost = lightningCost;
        return this;
    }

    /**
     * Sets the lightning energy tier required (default: {@link LightningEnergyTier#HIGH_VOLTAGE HV}).
     */
    public LightningAssemblyRecipeBuilder lightningTier(LightningEnergyTier tier) {
        this.lightningTier = Objects.requireNonNull(tier, "tier");
        return this;
    }

    public JsonObject toJson() {
        if (inputs.isEmpty()) throw new IllegalStateException("At least one input is required");
        if (resultItem == null) throw new IllegalStateException("A result item must be set");

        JsonObject json = new JsonObject();
        json.addProperty("type", "ae2lt:lightning_assembly");
        if (priority != 0) json.addProperty("priority", priority);

        JsonArray inputsArray = new JsonArray();
        for (InputSpec spec : inputs) {
            inputsArray.add(spec.toJson());
        }
        json.add("inputs", inputsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", resultItem);
        result.addProperty("count", resultCount);
        json.add("result", result);

        json.addProperty("totalEnergy", totalEnergy);
        json.addProperty("lightningCost", lightningCost);
        json.addProperty("lightningTier", lightningTier.getSerializedName());

        return json;
    }

    private record InputSpec(String itemId, String tagId, int count) {
        JsonObject toJson() {
            JsonObject obj = new JsonObject();
            JsonObject ingredient = new JsonObject();
            if (itemId != null) {
                ingredient.addProperty("item", itemId);
            } else {
                ingredient.addProperty("tag", tagId);
            }
            obj.add("ingredient", ingredient);
            obj.addProperty("count", count);
            return obj;
        }
    }
}
