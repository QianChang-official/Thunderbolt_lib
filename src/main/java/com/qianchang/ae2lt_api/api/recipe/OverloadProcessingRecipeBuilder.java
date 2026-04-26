package com.qianchang.ae2lt_api.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for <strong>Overload Processing</strong> recipes
 * ({@code ae2lt:overload_processing}).
 *
 * <p>Overload Processing recipes are handled by the <em>Overload Processing Factory</em>,
 * a high-throughput parallel processor. Each recipe maps a set of ingredient inputs to
 * one or more item outputs, consuming FE in the process.</p>
 *
 * <h2>JSON output example</h2>
 * <pre>{@code
 * {
 *   "type": "ae2lt:overload_processing",
 *   "inputs": [
 *     { "ingredient": { "item": "ae2lt:overload_alloy_blank" }, "count": 3 }
 *   ],
 *   "outputs": [
 *     { "id": "ae2lt:overload_alloy", "count": 1 }
 *   ],
 *   "energy": 800,
 *   "processingTime": 40
 * }
 * }</pre>
 */
public final class OverloadProcessingRecipeBuilder {

    private final List<InputSpec> inputs = new ArrayList<>();
    private final List<OutputSpec> outputs = new ArrayList<>();
    private long energy = 100;
    private int processingTime = 20;
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

    /** Adds an output item. */
    public OverloadProcessingRecipeBuilder output(String itemId, int count) {
        Objects.requireNonNull(itemId, "itemId");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        outputs.add(new OutputSpec(itemId, count));
        return this;
    }

    /** Sets the FE energy consumed per operation. */
    public OverloadProcessingRecipeBuilder energy(long energy) {
        if (energy < 0) throw new IllegalArgumentException("energy must be non-negative");
        this.energy = energy;
        return this;
    }

    /** Sets the processing time in ticks (default: 20). */
    public OverloadProcessingRecipeBuilder processingTime(int ticks) {
        if (ticks <= 0) throw new IllegalArgumentException("processingTime must be positive");
        this.processingTime = ticks;
        return this;
    }

    public JsonObject toJson() {
        if (inputs.isEmpty()) throw new IllegalStateException("At least one input is required");
        if (outputs.isEmpty()) throw new IllegalStateException("At least one output is required");

        JsonObject json = new JsonObject();
        json.addProperty("type", "ae2lt:overload_processing");
        if (priority != 0) json.addProperty("priority", priority);

        JsonArray inputsArray = new JsonArray();
        for (InputSpec spec : inputs) inputsArray.add(spec.toJson());
        json.add("inputs", inputsArray);

        JsonArray outputsArray = new JsonArray();
        for (OutputSpec spec : outputs) outputsArray.add(spec.toJson());
        json.add("outputs", outputsArray);

        json.addProperty("energy", energy);
        json.addProperty("processingTime", processingTime);

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
}
