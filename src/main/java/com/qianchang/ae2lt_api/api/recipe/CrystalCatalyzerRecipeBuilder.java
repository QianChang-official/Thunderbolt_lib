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
 * <h2>JSON output example (item output)</h2>
 * <pre>{@code
 * {
 *   "type": "ae2lt:crystal_catalyzer",
 *   "catalyst": { "item": "minecraft:amethyst_block" },
 *   "catalystCount": 1,
 *   "output": { "id": "minecraft:amethyst_shard", "count": 1 },
 *   "energyPerCycle": 100000
 * }
 * }</pre>
 *
 * <h2>JSON output example (dust mode + tag output)</h2>
 * <pre>{@code
 * {
 *   "type": "ae2lt:crystal_catalyzer",
 *   "mode": "dust",
 *   "catalyst": { "item": "minecraft:amethyst_block" },
 *   "catalystCount": 1,
 *   "output": { "tag": "c:dusts/amethyst", "count": 1 },
 *   "energyPerCycle": 100000
 * }
 * }</pre>
 */
public final class CrystalCatalyzerRecipeBuilder {

    /**
     * Standard catalyzer modes recognised by AE2LT 1.0.2.
     */
    public static final String MODE_DUST = "dust";

    @Nullable
    private String catalystItem;
    @Nullable
    private String catalystTag;
    private int catalystCount = 1;
    @Nullable
    private String outputItem;
    @Nullable
    private String outputTag;
    private int outputCount = 1;
    private int energyPerCycle = 100;
    @Nullable
    private String mode;

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
        this.outputTag = null;
        this.outputCount = count;
        return this;
    }

    /**
     * Sets the output as a tag-based stack. Used by AE2LT's "dust" mode where
     * the produced item is resolved against an item tag (e.g. {@code c:dusts/amethyst}).
     */
    public CrystalCatalyzerRecipeBuilder outputTag(String tagId, int count) {
        this.outputTag = Objects.requireNonNull(tagId, "tagId");
        this.outputItem = null;
        this.outputCount = count;
        return this;
    }

    /**
     * Sets a free-form mode string (e.g. {@link #MODE_DUST "dust"}). When unset
     * the recipe is a regular catalyzer recipe; passing {@code null} clears it.
     */
    public CrystalCatalyzerRecipeBuilder mode(@Nullable String mode) {
        this.mode = mode;
        return this;
    }

    /** Convenience: enables AE2LT's {@code dust} catalyzer mode. */
    public CrystalCatalyzerRecipeBuilder dustMode() {
        return mode(MODE_DUST);
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
        if (outputItem == null && outputTag == null) {
            throw new IllegalStateException("An output item or tag must be set");
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", AE2LTRecipeTypes.CRYSTAL_CATALYZER);
        if (mode != null) {
            json.addProperty("mode", mode);
        }

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
        if (outputItem != null) {
            output.addProperty("id", outputItem);
        } else {
            output.addProperty("tag", outputTag);
        }
        output.addProperty("count", outputCount);
        json.add("output", output);

        json.addProperty("energyPerCycle", energyPerCycle);

        return json;
    }
}
