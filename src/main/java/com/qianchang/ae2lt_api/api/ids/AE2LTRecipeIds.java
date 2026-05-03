package com.qianchang.ae2lt_api.api.ids;

import net.minecraft.resources.ResourceLocation;

/**
 * Frozen registry IDs for the public-facing recipe types of AE2 Lightning Tech.
 *
 * <p>These constants mirror AE2LT's own first-party {@code com.moakiee.ae2lt.api.ids.AE2LTRecipeIds}
 * (introduced in AE2LT 1.0.2 / 1.0.3). They use the {@code ae2lt} namespace, which
 * is AE2LT's own mod id.</p>
 *
 * <p>Note: these are recipe <em>type</em> IDs (i.e. the namespaced IDs used to
 * register a {@code RecipeType<T>}), not individual recipe content paths. Use
 * these when filtering a recipe manager or wiring datagen.</p>
 *
 * <p>The recipe builders in
 * {@link com.qianchang.ae2lt_api.api.recipe} continue to write recipes under these
 * same type IDs, so addons that build recipes via the library and addons that read
 * AE2LT's first-party recipes share a single ID surface.</p>
 *
 * @since 1.0.3
 */
public final class AE2LTRecipeIds {

    private static final String MOD_ID = AE2LTBlockEntityIds.MOD_ID;

    /** Lightning Assembly Chamber recipes — pattern-based item assembly under HV/EHV input. */
    public static final ResourceLocation LIGHTNING_ASSEMBLY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "lightning_assembly");

    /** Lightning Transform recipes — natural-strike item-to-item transformation. */
    public static final ResourceLocation LIGHTNING_TRANSFORM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "lightning_transform");

    /** Lightning Simulation Chamber recipes — bulk lightning generation from energy. */
    public static final ResourceLocation LIGHTNING_SIMULATION =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "lightning_simulation");

    /** Overload Processing Factory recipes — high-tier item processing. */
    public static final ResourceLocation OVERLOAD_PROCESSING =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "overload_processing");

    /** Crystal Catalyzer recipes — FE-driven crystal cultivation (note: FE only). */
    public static final ResourceLocation CRYSTAL_CATALYZER =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "crystal_catalyzer");

    /** Lightning Strike recipes — overload-TNT-triggered item conversion. */
    public static final ResourceLocation LIGHTNING_STRIKE =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "lightning_strike");

    private AE2LTRecipeIds() {
    }
}
