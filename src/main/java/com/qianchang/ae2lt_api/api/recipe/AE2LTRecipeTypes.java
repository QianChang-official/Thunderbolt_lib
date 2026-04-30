package com.qianchang.ae2lt_api.api.recipe;

import net.minecraft.resources.ResourceLocation;

/**
 * Canonical AE2 Lightning Tech recipe ids exposed by the addon framework.
 */
public final class AE2LTRecipeTypes {

    public static final String MOD_ID = "ae2lt";

    public static final ResourceLocation LIGHTNING_TRANSFORM_ID = id("lightning_transform");
    public static final ResourceLocation LIGHTNING_SIMULATION_ID = id("lightning_simulation");
    public static final ResourceLocation LIGHTNING_ASSEMBLY_ID = id("lightning_assembly");
    public static final ResourceLocation OVERLOAD_PROCESSING_ID = id("overload_processing");
    public static final ResourceLocation CRYSTAL_CATALYZER_ID = id("crystal_catalyzer");
    public static final ResourceLocation LIGHTNING_STRIKE_ID = id("lightning_strike");

    public static final String LIGHTNING_TRANSFORM = LIGHTNING_TRANSFORM_ID.toString();
    public static final String LIGHTNING_SIMULATION = LIGHTNING_SIMULATION_ID.toString();
    public static final String LIGHTNING_ASSEMBLY = LIGHTNING_ASSEMBLY_ID.toString();
    public static final String OVERLOAD_PROCESSING = OVERLOAD_PROCESSING_ID.toString();
    public static final String CRYSTAL_CATALYZER = CRYSTAL_CATALYZER_ID.toString();
    public static final String LIGHTNING_STRIKE = LIGHTNING_STRIKE_ID.toString();

    private AE2LTRecipeTypes() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
