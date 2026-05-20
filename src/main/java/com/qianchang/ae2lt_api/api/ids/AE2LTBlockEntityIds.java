package com.qianchang.ae2lt_api.api.ids;

import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Frozen registry IDs for the public-facing block entities of AE2 Lightning Tech.
 *
 * <p>These constants mirror AE2LT's own first-party {@code com.moakiee.ae2lt.api.ids.AE2LTBlockEntityIds}
 * (introduced in AE2LT 1.0.2 / 1.0.3). They use the {@code ae2lt} namespace, which is
 * AE2LT's own mod id, NOT Thunderbolt_lib's {@code ae2lt_api} namespace. The two
 * namespaces are intentionally distinct; addons that want to query AE2LT's own
 * registries (block entity types, recipe types, datagen output) should use these
 * constants.</p>
 *
 * <p>The {@link #LIGHTNING_GRID_MEMBERS} list captures the five block entities on
 * which AE2LT 1.0.2+ publicly registers the {@code LIGHTNING_ENERGY_BLOCK}
 * capability. Crystal Catalyzer is intentionally excluded: it runs on FE only and
 * is not part of the lightning-energy network.</p>
 *
 * @since 1.0.3
 */
public final class AE2LTBlockEntityIds {

    /** Mod id of AE2 Lightning Tech itself. Frozen as part of the API contract. */
    public static final String MOD_ID = "ae2lt";

    public static final Identifier LIGHTNING_COLLECTOR =
            Identifier.fromNamespaceAndPath(MOD_ID, "lightning_collector");

    public static final Identifier LIGHTNING_SIMULATION_ROOM =
            Identifier.fromNamespaceAndPath(MOD_ID, "lightning_simulation_room");

    public static final Identifier LIGHTNING_ASSEMBLY_CHAMBER =
            Identifier.fromNamespaceAndPath(MOD_ID, "lightning_assembly_chamber");

    public static final Identifier OVERLOAD_PROCESSING_FACTORY =
            Identifier.fromNamespaceAndPath(MOD_ID, "overload_processing_factory");

    public static final Identifier TESLA_COIL =
            Identifier.fromNamespaceAndPath(MOD_ID, "tesla_coil");

    public static final Identifier CRYSTAL_CATALYZER =
            Identifier.fromNamespaceAndPath(MOD_ID, "crystal_catalyzer");

    /**
     * Immutable list of the five block entities that participate in the AE2LT
     * lightning-energy grid (i.e. on which {@code LIGHTNING_ENERGY_BLOCK} is
     * registered upstream). Iteration order is stable and matches the order the
     * machines were introduced in AE2LT.
     */
    public static final List<Identifier> LIGHTNING_GRID_MEMBERS = List.of(
            LIGHTNING_COLLECTOR,
            LIGHTNING_SIMULATION_ROOM,
            LIGHTNING_ASSEMBLY_CHAMBER,
            OVERLOAD_PROCESSING_FACTORY,
            TESLA_COIL);

    private AE2LTBlockEntityIds() {
    }
}
