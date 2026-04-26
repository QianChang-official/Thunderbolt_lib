package com.qianchang.ae2lt_api.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for <strong>Lightning Transform</strong> recipes
 * ({@code ae2lt:lightning_transform}).
 *
 * <p>A lightning transform recipe converts item entities on the ground when a
 * lightning bolt strikes nearby. Items placed in the correct pattern will be
 * consumed and replaced by the result.</p>
 *
 * <h2>JSON output example</h2>
 * <pre>{@code
 * {
 *   "type": "ae2lt:lightning_transform",
 *   "priority": 10,
 *   "inputs": [
 *     { "ingredient": { "item": "minecraft:gold_ingot" }, "count": 2 }
 *   ],
 *   "result": { "id": "ae2lt:overload_crystal", "count": 1 }
 * }
 * }</pre>
 *
 * <h2>Usage in a data provider</h2>
 * <pre>{@code
 * LightningTransformRecipeBuilder.create()
 *     .priority(10)
 *     .input("minecraft:gold_ingot", 2)
 *     .result("ae2lt:overload_crystal", 1)
 *     .toJson();
 * }</pre>
 */
public final class LightningTransformRecipeBuilder {

    private int priority = 0;
    private final List<InputSpec> inputs = new ArrayList<>();
    private String resultItem;
    private int resultCount = 1;

    private LightningTransformRecipeBuilder() {}

    /** Creates a new builder instance. */
    public static LightningTransformRecipeBuilder create() {
        return new LightningTransformRecipeBuilder();
    }

    /**
     * Sets the recipe priority. Higher-priority recipes are matched first when
     * multiple recipes could apply.
     *
     * @param priority Any integer; default is {@code 0}.
     */
    public LightningTransformRecipeBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Adds an item ingredient with the given count.
     *
     * @param itemId The namespaced item ID (e.g. {@code "minecraft:gold_ingot"}).
     * @param count  Number of items required.
     */
    public LightningTransformRecipeBuilder input(String itemId, int count) {
        Objects.requireNonNull(itemId, "itemId");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        inputs.add(new InputSpec(itemId, null, count));
        return this;
    }

    /**
     * Adds a tag ingredient with the given count.
     *
     * @param tagId The namespaced tag ID (e.g. {@code "forge:ingots/gold"}).
     * @param count Number of items required.
     */
    public LightningTransformRecipeBuilder inputTag(String tagId, int count) {
        Objects.requireNonNull(tagId, "tagId");
        if (count <= 0) throw new IllegalArgumentException("count must be positive");
        inputs.add(new InputSpec(null, tagId, count));
        return this;
    }

    /**
     * Sets the result item.
     *
     * @param itemId The namespaced item ID.
     * @param count  Number of items produced.
     */
    public LightningTransformRecipeBuilder result(String itemId, int count) {
        this.resultItem = Objects.requireNonNull(itemId, "itemId");
        this.resultCount = count;
        return this;
    }

    /**
     * Builds the recipe as a {@link JsonObject} in the format expected by AE2LT.
     *
     * @return A recipe JSON object.
     * @throws IllegalStateException if no inputs or result have been set.
     */
    public JsonObject toJson() {
        if (inputs.isEmpty()) throw new IllegalStateException("At least one input is required");
        if (resultItem == null) throw new IllegalStateException("A result item must be set");

        JsonObject json = new JsonObject();
        json.addProperty("type", "ae2lt:lightning_transform");
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
