package com.qianchang.ae2lt_api.api.capability;

import com.qianchang.ae2lt_api.api.lightning.ILightningEnergyHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

/**
 * Central registry of Forge capabilities exposed by the Thunderbolt_lib.
 *
 * <h2>Lightning Energy Capability</h2>
 * <p>Register {@link #LIGHTNING_ENERGY_BLOCK} on your block entity to participate in the
 * AE2 Lightning Tech energy network. The five lightning-grid machines published by
 * AE2LT 1.0.2+ (Lightning Collector, Lightning Simulation Room, Lightning Assembly
 * Chamber, Overload Processing Factory, Tesla Coil) automatically query this
 * capability when looking for lightning energy sources or sinks adjacent to them.</p>
 *
 * <h2>Relationship to AE2LT 1.0.10's first-party API</h2>
 * <p>AE2LT 1.0.2+ exposes its own first-party capability under the
 * {@code ae2lt} namespace ({@code com.moakiee.ae2lt.api.AE2LTCapabilities}). On
 * Forge 1.20.1 both the block and item views are keyed by interface token rather
 * than by a runtime {@code ResourceLocation}; the historical names
 * {@code ae2lt_api:lightning_energy} and {@code ae2lt_api:lightning_energy_item}
 * are kept only as documentation aliases. See
 * {@link com.qianchang.ae2lt_api.api.bridge.AE2LTNativeBridge} for guidance on
 * which to query.</p>
 *
 * <h2>Example registration</h2>
 * <pre>{@code
 * // In your mod's RegisterCapabilitiesEvent listener:
 * event.registerBlockEntity(
 *     AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK,
 *     MyBlockEntities.MY_MACHINE.get(),
 *     (be, side) -> be.getLightningEnergyHandler(side)
 * );
 * }</pre>
 */
public final class AE2LTCapabilities {

    /** Current API version — bump when breaking changes are introduced. */
    public static final String API_VERSION = "1.0.10-1.20.1forge";

    /**
     * Block-side capability for lightning energy I/O.
     */
    public static final Capability<ILightningEnergyHandler> LIGHTNING_ENERGY_BLOCK =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    /**
     * Item capability for lightning-energy-storing items (e.g., portable lightning cells).
     * Forge 1.20.1 keys this to the same underlying token as the block-side
     * capability, mirroring AE2LT's own first-party API on this loader.
     */
    public static final Capability<ILightningEnergyHandler> LIGHTNING_ENERGY_ITEM =
            LIGHTNING_ENERGY_BLOCK;

    /** Registers the library capability token on Forge 1.20.1. */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ILightningEnergyHandler.class);
    }

    private AE2LTCapabilities() {}
}


