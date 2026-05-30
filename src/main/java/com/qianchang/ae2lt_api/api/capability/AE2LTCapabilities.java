package com.qianchang.ae2lt_api.api.capability;

import com.qianchang.ae2lt_api.api.lightning.ILightningEnergyHandler;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

/**
 * Central registry of NeoForge capabilities exposed by the Thunderbolt_lib.
 *
 * <h2>Lightning Energy Capability</h2>
 * <p>Register {@link #LIGHTNING_ENERGY_BLOCK} on your block entity to participate in the
 * AE2 Lightning Tech energy network. The five lightning-grid machines published by
 * AE2LT 1.0.2+ (Lightning Collector, Lightning Simulation Room, Lightning Assembly
 * Chamber, Overload Processing Factory, Tesla Coil) automatically query this
 * capability when looking for lightning energy sources or sinks adjacent to them.</p>
 *
 * <h2>Relationship to the AE2LT 26.1.2 port's first-party API</h2>
 * <p>AE2LT 1.0.2+ and the verified 26.1.2 NeoForge port expose their own first-party capability under the
 * {@code ae2lt} namespace ({@code com.moakiee.ae2lt.api.AE2LTCapabilities}). The two
 * capability IDs are deliberately distinct ({@code ae2lt_api:lightning_energy}
 * vs {@code ae2lt:lightning_energy}); see
 * {@link com.qianchang.ae2lt_api.api.bridge.AE2LTNativeBridge} for guidance on
 * which to query.</p>
 *
 * <h2>Example registration</h2>
 * <pre>{@code
 * // In your mod's common setup / RegisterCapabilitiesEvent listener:
 * event.registerBlockEntity(
 *     AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK,
 *     MyBlockEntities.MY_MACHINE.get(),
 *     (be, side) -> be.getLightningEnergyHandler(side)
 * );
 * }</pre>
 */
public final class AE2LTCapabilities {

    private static final String MOD_ID = "ae2lt_api";

    /** Current API version for the 26.1.2 NeoForge migration branch. */
        public static final String API_VERSION = "1.0.10-alpha-hotfix.1.26.1.2neoforge";

    /**
     * Sided block capability for lightning energy I/O.
     *
     * <p>The context parameter is the queried {@link Direction} (may be {@code null}
     * for an unsided query). Return {@code null} from the provider lambda to indicate
     * that the block does not support lightning energy on that side.</p>
     */
    public static final BlockCapability<ILightningEnergyHandler, @Nullable Direction> LIGHTNING_ENERGY_BLOCK =
            BlockCapability.createSided(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "lightning_energy"),
                    ILightningEnergyHandler.class);

    /**
     * Item capability for lightning-energy-storing items (e.g., portable lightning cells).
     *
     * <p>The context is {@code Void} (no context required).</p>
     */
    public static final ItemCapability<ILightningEnergyHandler, Void> LIGHTNING_ENERGY_ITEM =
            ItemCapability.createVoid(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "lightning_energy_item"),
                    ILightningEnergyHandler.class);

    private AE2LTCapabilities() {}
}


