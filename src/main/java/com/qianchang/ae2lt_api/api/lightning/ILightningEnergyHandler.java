package com.qianchang.ae2lt_api.api.lightning;

/**
 * Capability interface for blocks and items that can store or transfer
 * lightning energy (the custom resource introduced by AE2 Lightning Tech).
 *
 * <p>Implement this interface and register it via the
 * {@link com.qianchang.ae2lt_api.api.capability.AE2LTCapabilities#LIGHTNING_ENERGY_BLOCK LIGHTNING_ENERGY_BLOCK}
 * capability to expose lightning energy I/O to the AE2LT network and to other
 * addon mods.</p>
 *
 * <p>Amounts are expressed as whole units of lightning energy (long integers).
 * Both {@link LightningEnergyTier#HIGH_VOLTAGE HV} and
 * {@link LightningEnergyTier#EXTREME_HIGH_VOLTAGE EHV} tiers share this interface;
 * implementations may support one or both tiers.</p>
 */
public interface ILightningEnergyHandler {

    /**
     * Returns the amount of lightning energy currently stored for the given tier.
     *
     * @param tier The energy tier to query.
     * @return Current stored amount, non-negative.
     */
    long getLightningStored(LightningEnergyTier tier);

    /**
     * AE2LT first-party naming alias for {@link #getLightningStored(LightningEnergyTier)}.
     *
     * @since 1.0.4
     */
    default long getStored(LightningEnergyTier tier) {
        return getLightningStored(tier);
    }

    /**
     * Returns the maximum amount of lightning energy that can be stored for the given tier.
     *
     * @param tier The energy tier to query.
     * @return Maximum capacity, non-negative. May be {@link Long#MAX_VALUE} for
     *         grid-backed or otherwise unbounded handlers.
     */
    long getLightningCapacity(LightningEnergyTier tier);

    /**
     * AE2LT first-party naming alias for {@link #getLightningCapacity(LightningEnergyTier)}.
     *
     * @since 1.0.4
     */
    default long getCapacity(LightningEnergyTier tier) {
        return getLightningCapacity(tier);
    }

    /**
     * Attempts to insert lightning energy into this handler.
     *
     * @param tier     The energy tier to insert.
     * @param amount   Amount to insert, must be positive.
     * @param simulate If {@code true} the operation is simulated without any side effects.
     * @return The amount that was (or would have been) accepted. Zero if the tier is not supported.
     */
    long insertLightning(LightningEnergyTier tier, long amount, boolean simulate);

    /**
     * AE2LT first-party naming alias for
     * {@link #insertLightning(LightningEnergyTier, long, boolean)}.
     *
     * @since 1.0.4
     */
    default long insert(LightningEnergyTier tier, long amount, boolean simulate) {
        return insertLightning(tier, amount, simulate);
    }

    /**
     * Attempts to extract lightning energy from this handler.
     *
     * @param tier     The energy tier to extract.
     * @param amount   Maximum amount to extract, must be positive.
     * @param simulate If {@code true} the operation is simulated without any side effects.
     * @return The amount that was (or would have been) extracted. Zero if the tier is not supported.
     */
    long extractLightning(LightningEnergyTier tier, long amount, boolean simulate);

    /**
     * AE2LT first-party naming alias for
     * {@link #extractLightning(LightningEnergyTier, long, boolean)}.
     *
     * @since 1.0.4
     */
    default long extract(LightningEnergyTier tier, long amount, boolean simulate) {
        return extractLightning(tier, amount, simulate);
    }

    /**
     * Whether this handler can accept energy of the given tier.
     *
     * @param tier The energy tier to check.
     * @return {@code true} if insertion is supported.
     */
    boolean canInsert(LightningEnergyTier tier);

    /**
     * Whether this handler can provide energy of the given tier.
     *
     * @param tier The energy tier to check.
     * @return {@code true} if extraction is supported.
     */
    boolean canExtract(LightningEnergyTier tier);

    /** Convenience: returns {@code true} if the stored amount is zero for the given tier. */
    default boolean isEmpty(LightningEnergyTier tier) {
        return getLightningStored(tier) == 0;
    }

    /**
     * Convenience: returns {@code true} if stored amount equals capacity for the
     * given tier. Returns {@code false} for unbounded {@link Long#MAX_VALUE}
     * capacity, matching AE2LT's first-party handler semantics.
     */
    default boolean isFull(LightningEnergyTier tier) {
        long capacity = getLightningCapacity(tier);
        return capacity > 0 && capacity != Long.MAX_VALUE && getLightningStored(tier) >= capacity;
    }
}
