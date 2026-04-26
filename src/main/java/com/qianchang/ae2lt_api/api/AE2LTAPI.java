package com.qianchang.ae2lt_api.api;

import com.qianchang.ae2lt_api.AE2LTAddonFramework;
import com.qianchang.ae2lt_api.api.capability.AE2LTCapabilities;
import com.qianchang.ae2lt_api.api.lightning.ILightningEnergyHandler;
import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Main static entry point for the AE2 Lightning Tech Addon Framework API.
 *
 * <p>After the framework is initialized (during {@code FMLCommonSetupEvent}),
 * this class provides convenient helper methods for querying lightning energy
 * capabilities and checking mod availability.</p>
 *
 * <h2>Initialization</h2>
 * <p>The instance is created automatically by the framework mod class. You do
 * not need to call {@link #init()} yourself.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Query HV lightning stored in a block adjacent to (x, y, z)
 * Optional<ILightningEnergyHandler> handler =
 *     AE2LTAPI.getInstance().getLightningHandler(level, pos, Direction.DOWN);
 * handler.ifPresent(h -> {
 *     long stored = h.getLightningStored(LightningEnergyTier.HIGH_VOLTAGE);
 *     long extracted = h.extractLightning(LightningEnergyTier.HIGH_VOLTAGE, 64, false);
 * });
 * }</pre>
 */
public final class AE2LTAPI {

    private static AE2LTAPI instance;

    private AE2LTAPI() {}

    /**
     * Returns the singleton API instance.
     *
     * @throws IllegalStateException if the framework has not yet been initialized.
     */
    public static AE2LTAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "[AE2LT API] API accessed before initialization. " +
                    "Ensure your mod depends on '" + AE2LTAddonFramework.MODID + "'.");
        }
        return instance;
    }

    /** Called by the framework mod class during setup — do not call this yourself. */
    public static void init() {
        if (instance == null) {
            instance = new AE2LTAPI();
        }
    }

    // -----------------------------------------------------------------------
    // Mod detection
    // -----------------------------------------------------------------------

    /** Returns {@code true} if AE2 Lightning Tech (ae2lt) is loaded at runtime. */
    public boolean isAE2LTLoaded() {
        return ModList.get().isLoaded("ae2lt");
    }

    /** Returns {@code true} if the given mod id is loaded at runtime. */
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    // -----------------------------------------------------------------------
    // Capability helpers
    // -----------------------------------------------------------------------

    /**
     * Queries the {@link AE2LTCapabilities#LIGHTNING_ENERGY_BLOCK LIGHTNING_ENERGY_BLOCK}
     * capability from a block at the given position, on the given side.
     *
     * @param level The level to query in.
     * @param pos   The block position.
     * @param side  The direction to query from (may be {@code null} for unsided).
     * @return An {@link Optional} containing the handler, or empty if none is present.
     */
    public Optional<ILightningEnergyHandler> getLightningHandler(
            Level level, BlockPos pos, @Nullable Direction side) {
        ILightningEnergyHandler handler =
                level.getCapability(AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK, pos, side);
        return Optional.ofNullable(handler);
    }

    /**
     * Returns the amount of lightning energy stored for a given tier at a position.
     * Returns {@code 0} if no capability is present.
     *
     * @param level The level to query in.
     * @param pos   The block position.
     * @param side  The direction to query from.
     * @param tier  The energy tier.
     * @return Stored lightning energy, or {@code 0}.
     */
    public long getLightningStored(
            Level level, BlockPos pos, @Nullable Direction side, LightningEnergyTier tier) {
        return getLightningHandler(level, pos, side)
                .map(h -> h.getLightningStored(tier))
                .orElse(0L);
    }

    /**
     * Inserts lightning energy into a block capability. Returns 0 if no handler is present.
     *
     * @param level    The level.
     * @param pos      The block position.
     * @param side     The direction.
     * @param tier     The energy tier.
     * @param amount   Amount to insert.
     * @param simulate If {@code true}, the operation is simulated.
     * @return The amount accepted.
     */
    public long insertLightning(
            Level level, BlockPos pos, @Nullable Direction side,
            LightningEnergyTier tier, long amount, boolean simulate) {
        return getLightningHandler(level, pos, side)
                .map(h -> h.insertLightning(tier, amount, simulate))
                .orElse(0L);
    }

    /**
     * Extracts lightning energy from a block capability. Returns 0 if no handler is present.
     *
     * @param level    The level.
     * @param pos      The block position.
     * @param side     The direction.
     * @param tier     The energy tier.
     * @param amount   Maximum amount to extract.
     * @param simulate If {@code true}, the operation is simulated.
     * @return The amount extracted.
     */
    public long extractLightning(
            Level level, BlockPos pos, @Nullable Direction side,
            LightningEnergyTier tier, long amount, boolean simulate) {
        return getLightningHandler(level, pos, side)
                .map(h -> h.extractLightning(tier, amount, simulate))
                .orElse(0L);
    }
}
