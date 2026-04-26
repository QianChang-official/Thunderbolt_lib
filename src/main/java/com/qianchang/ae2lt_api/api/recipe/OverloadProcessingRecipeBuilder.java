package com.qianchang.ae2lt_api.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for <strong>Overload Processing</strong> recipes
 * ({@code ae2lt:overload_processing}).
 *
 * <p>Overload Processing recipes are handled by the <em>Overload Processing Factory</em>.
 * The current AE2LT schema supports item and fluid inputs, plus item and fluid results,
 * with FE and lightning requirements.</p>
 *
 * <h2>JSON output example</h2>
 * <pre>{@code
 * {
 *   "type": "ae2lt:overload_processing",
 *   "inputs": [
 *     { "ingredient": { "item": "ae2lt:overload_alloy_blank" }, "count": 3 }
 *   ],
 *   "inputFluid": {
 *     "id": "minecraft:water",
 *     "amount": 1000
 *   },
 *   "results": [
 *     { "id": "ae2lt:overload_alloy", "count": 1 }
 *   ],
 *   "totalEnergy": 800,
 *   "lightningCost": 1,
 *   "lightningTier": "high_voltage"
 * }
 * }</pre>
 */
public final class OverloadProcessingRecipeBuilder {

    private final List<InputSpec> inputs = new ArrayList<>();
    private final List<OutputSpec> results = new ArrayList<>();
    private FluidSpec inputFluid;
    private FluidSpec resultFluid;
    private long totalEnergy = 100;
    private int lightningCost = 4;
    private LightningEnergyTier lightningTier = LightningEnergyTier.HIGH_VOLTAGE;
    private Integer unsupportedProcessingTime;
    private int priority = 0;

    private OverloadProcessingRecipeBuilder() {}

    public static OverloadProcessingRecipeBuilder create() {
        return new OverloadProcessingRecipeBuilder();
    }

    public OverloadProcessingRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    /** Adds an item ingredient. */
    public OverloadProcessingRecipeBuilder input(String itemId, int count) {
        Objects.requireNonNull(itemId, "itemId");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        inputs.add(new InputSpec(itemId, null, count));
        return this;
    }

    /** Adds a tag ingredient. */
    public OverloadProcessingRecipeBuilder inputTag(String tagId, int count) {
        Objects.requireNonNull(tagId, "tagId");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        inputs.add(new InputSpec(null, tagId, count));
        return this;
    }

    /** Adds the item result. AE2LT currently supports at most one item result. */
    public OverloadProcessingRecipeBuilder output(String itemId, int count) {
        return result(itemId, count);
    }

    /** Adds the item result. AE2LT currently supports at most one item result. */
    public OverloadProcessingRecipeBuilder result(String itemId, int count) {
        Objects.requireNonNull(itemId, "itemId");
        if (!results.isEmpty()) throw new IllegalStateException("AE2LT currently supports at most one item result");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        results.add(new OutputSpec(itemId, count));
        return this;
    }

    public OverloadProcessingRecipeBuilder inputFluid(String fluidId, int amount) {
        this.inputFluid = new FluidSpec(fluidId, amount);
        return this;
    }

    public OverloadProcessingRecipeBuilder resultFluid(String fluidId, int amount) {
        this.resultFluid = new FluidSpec(fluidId, amount);
        return this;
    }

    /** Sets the total FE energy consumed per operation. */
    public OverloadProcessingRecipeBuilder totalEnergy(long totalEnergy) {
        if (totalEnergy < 5) throw new IllegalArgumentException("totalEnergy must be at least 5");
        this.totalEnergy = totalEnergy;
        return this;
    }

    /** @deprecated Use {@link #totalEnergy(long)} to match AE2LT's current recipe schema. */
    @Deprecated
    public OverloadProcessingRecipeBuilder energy(long energy) {
        return totalEnergy(energy);
    }

    public OverloadProcessingRecipeBuilder lightningCost(int lightningCost) {
        if (lightningCost <= 0) throw new IllegalArgumentException("lightningCost must be positive");
        this.lightningCost = lightningCost;
        return this;
    }

    public OverloadProcessingRecipeBuilder lightningTier(LightningEnergyTier tier) {
        this.lightningTier = Objects.requireNonNull(tier, "tier");
        return this;
    }

    /** @deprecated AE2LT's current overload_processing schema does not support processingTime. */
    @Deprecated
    public OverloadProcessingRecipeBuilder processingTime(int ticks) {
        if (ticks <= 0) throw new IllegalArgumentException("processingTime must be positive");
        this.unsupportedProcessingTime = ticks;
        return this;
    }

    public JsonObject toJson() {
        if (inputs.isEmpty() && inputFluid == null) {
            throw new IllegalStateException("At least one item or fluid input is required");
        }
        if (results.isEmpty() && resultFluid == null) {
            throw new IllegalStateException("At least one item or fluid result is required");
        }
        if (unsupportedProcessingTime != null) {
            throw new IllegalStateException("AE2LT overload_processing recipes do not currently support processingTime");
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "ae2lt:overload_processing");
        if (priority != 0) json.addProperty("priority", priority);

        if (!inputs.isEmpty()) {
            JsonArray inputsArray = new JsonArray();
            for (InputSpec spec : inputs) inputsArray.add(spec.toJson());
            json.add("inputs", inputsArray);
        }

        if (inputFluid != null) {
            json.add("inputFluid", inputFluid.toJson());
        }
        if (!results.isEmpty()) {
            JsonArray resultsArray = new JsonArray();
            for (OutputSpec spec : results) resultsArray.add(spec.toJson());
            json.add("results", resultsArray);
        }
        if (resultFluid != null) {
            json.add("resultFluid", resultFluid.toJson());
        }

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

    private record OutputSpec(String itemId, int count) {
        JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", itemId);
            obj.addProperty("count", count);
            return obj;
        }
    }

    private record FluidSpec(String fluidId, int amount) {
        FluidSpec {
            Objects.requireNonNull(fluidId, "fluidId");
            if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        }

        JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", fluidId);
            obj.addProperty("amount", amount);
            return obj;
        }
    }
}
