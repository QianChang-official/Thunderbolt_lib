package com.qianchang.ae2lt_api.api.recipe;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Builder for <strong>Crystal Catalyzer</strong> recipes
 * ({@code ae2lt:crystal_catalyzer}).
 *
 * <p>Crystal Catalyzer recipes are processed by the <em>Crystal Catalyzer</em> machine.
 * An optional catalyst item stays in the catalyst slot (it is <em>not</em> consumed)
 * while the machine runs, producing output per cycle. The optional Lightning Collapse
 * Matrix in the catalyst slot multiplies output.</p>
 *
 * <p>If no catalyst is specified, the catalyst slot must be empty for this recipe
 * to match.</p>
 *
 * <h2>JSON output example (with catalyst)</h2>
 * <pre>{@code
 * {
 *   "type": "ae2lt:crystal_catalyzer",
 *   "catalyst": { "item": "ae2lt:overload_crystal" },
 *   "catalystCount": 1,
 *   "output": { "id": "ae2lt:overload_crystal_dust", "count": 4 },
 *   "energyPerCycle": 100
 * }
 * }</pre>
 *
 * <h2>JSON output example (no catalyst)</h2>
 * <pre>{@code
 * {
 *   "type": "ae2lt:crystal_catalyzer",
 *   "output": { "id": "minecraft:quartz", "count": 1 },
 *   "energyPerCycle": 50
 * }
 * }</pre>
 */
public final class CrystalCatalyzerRecipeBuilder {

    @Nullable
    private String catalystItem;
    @Nullable
    private String catalystTag;
    private int catalystCount = 1;
    private String outputItem;
    private int outputCount = 1;
    private int energyPerCycle = 100;

    private CrystalCatalyzerRecipeBuilder() {}

    public static CrystalCatalyzerRecipeBuilder create() {
        return new CrystalCatalyzerRecipeBuilder();
    }

    /**
     * Sets the required catalyst item (not consumed during processing).
     *
     * @param itemId The namespaced item ID.
     * @param count  Minimum stack size required in the catalyst slot.
     */
    public CrystalCatalyzerRecipeBuilder catalyst(String itemId, int count) {
        this.catalystItem = Objects.requireNonNull(itemId, "itemId");
        this.catalystTag = null;
        this.catalystCount = count;
        return this;
    }

    /**
     * Sets the required catalyst using an item tag (not consumed during processing).
     *
     * @param tagId The namespaced tag ID.
     * @param count Minimum stack size required in the catalyst slot.
     */
    public CrystalCatalyzerRecipeBuilder catalystTag(String tagId, int count) {
        this.catalystTag = Objects.requireNonNull(tagId, "tagId");
        this.catalystItem = null;
        this.catalystCount = count;
        return this;
    }

    /** Sets the output item produced per cycle. */
    public CrystalCatalyzerRecipeBuilder output(String itemId, int count) {
        this.outputItem = Objects.requireNonNull(itemId, "itemId");
        this.outputCount = count;
        return this;
    }

    /**
     * Sets the AE energy consumed per cycle (must be at least 1, default: 100).
     */
    public CrystalCatalyzerRecipeBuilder energyPerCycle(int energy) {
        if (energy < 1) throw new IllegalArgumentException("energyPerCycle must be at least 1");
        this.energyPerCycle = energy;
        return this;
    }

    public JsonObject toJson() {
        if (outputItem == null) throw new IllegalStateException("An output item must be set");

        JsonObject json = new JsonObject();
        json.addProperty("type", "ae2lt:crystal_catalyzer");

        if (catalystItem != null || catalystTag != null) {
            JsonObject catalyst = new JsonObject();
            if (catalystItem != null) {
                catalyst.addProperty("item", catalystItem);
            } else {
                catalyst.addProperty("tag", catalystTag);
            }
            json.add("catalyst", catalyst);
            json.addProperty("catalystCount", catalystCount);
        }

        JsonObject output = new JsonObject();
        output.addProperty("id", outputItem);
        output.addProperty("count", outputCount);
        json.add("output", output);

        json.addProperty("energyPerCycle", energyPerCycle);

        return json;
    }
}
