package com.qianchang.ae2lt_api.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for <strong>Lightning Simulation</strong> recipes
 * ({@code ae2lt:lightning_simulation}).
 *
 * <p>Lightning Simulation recipes are processed by the <em>Lightning Simulation Room</em>.
 * This machine replicates a lightning strike indoors using a Lightning Collapse Matrix,
 * consuming items and producing output without requiring an actual thunderstorm.</p>
 *
 * <h2>JSON output example</h2>
 * <pre>{@code
 * {
 *   "type": "ae2lt:lightning_simulation",
 *   "inputs": [
 *     { "ingredient": { "item": "minecraft:iron_ingot" }, "count": 4 }
 *   ],
 *   "result": { "id": "ae2lt:overload_alloy_blank", "count": 1 },
 *   "energy": 1000
 * }
 * }</pre>
 */
public final class LightningSimulationRecipeBuilder {

    private final List<InputSpec> inputs = new ArrayList<>();
    private String resultItem;
    private int resultCount = 1;
    private long energy = 1000;
    private int priority = 0;

    private LightningSimulationRecipeBuilder() {}

    public static LightningSimulationRecipeBuilder create() {
        return new LightningSimulationRecipeBuilder();
    }

    public LightningSimulationRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public LightningSimulationRecipeBuilder input(String itemId, int count) {
        Objects.requireNonNull(itemId, "itemId");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        inputs.add(new InputSpec(itemId, null, count));
        return this;
    }

    public LightningSimulationRecipeBuilder inputTag(String tagId, int count) {
        Objects.requireNonNull(tagId, "tagId");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        inputs.add(new InputSpec(null, tagId, count));
        return this;
    }

    public LightningSimulationRecipeBuilder result(String itemId, int count) {
        this.resultItem = Objects.requireNonNull(itemId, "itemId");
        this.resultCount = count;
        return this;
    }

    /** FE energy consumed per simulation run. */
    public LightningSimulationRecipeBuilder energy(long energy) {
        if (energy < 0) throw new IllegalArgumentException("energy must be non-negative");
        this.energy = energy;
        return this;
    }

    public JsonObject toJson() {
        if (inputs.isEmpty()) throw new IllegalStateException("At least one input is required");
        if (resultItem == null) throw new IllegalStateException("A result item must be set");

        JsonObject json = new JsonObject();
        json.addProperty("type", "ae2lt:lightning_simulation");
        if (priority != 0) json.addProperty("priority", priority);

        JsonArray inputsArray = new JsonArray();
        for (InputSpec spec : inputs) inputsArray.add(spec.toJson());
        json.add("inputs", inputsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", resultItem);
        result.addProperty("count", resultCount);
        json.add("result", result);

        json.addProperty("energy", energy);

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
