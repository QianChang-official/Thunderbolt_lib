package com.qianchang.ae2lt_api.api.event;

import com.qianchang.ae2lt_api.api.lightning.LightningEnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired on the NeoForge EVENT_BUS when the AE2LT Lightning Collector (or any
 * compatible collector registered through the framework) captures a lightning strike.
 *
 * <p>This event is cancellable. Cancelling it prevents the energy from being
 * added to the collector's storage. Use this to apply custom rules about
 * which strikes should be collected, or to redirect energy elsewhere.</p>
 *
 * <p>Subscribe with:</p>
 * <pre>{@code
 * @SubscribeEvent
 * public void onLightningCollected(LightningCollectedEvent event) {
 *     event.setHvAmount(event.getHvAmount() * 2); // double the yield
 * }
 * }</pre>
 *
 * <h2>Relationship to AE2LT 1.0.3's first-party event</h2>
 * <p>AE2LT 1.0.3 introduced its own {@code com.moakiee.ae2lt.api.event.LightningCollectedEvent}.
 * The two events are <em>not</em> the same Java type and fire from different
 * code paths: the library event continues to fire from the Thunderbolt_lib
 * collector bridge, while AE2LT's first-party event fires from AE2LT's own
 * {@code captureLightning}. Subscribing to both is safe and idempotent. The
 * {@link #isNaturalWeather()} flag (added in 1.0.3) mirrors the same flag on
 * AE2LT's first-party event for parity.</p>
 */
public class LightningCollectedEvent extends Event implements ICancellableEvent {

    private final ServerLevel level;
    private final BlockPos collectorPos;
    private final boolean naturalWeather;
    private long hvAmount;
    private long ehvAmount;

    /**
     * Backward-compatible constructor. {@link #isNaturalWeather()} will report
     * {@code false} when this constructor is used.
     */
    public LightningCollectedEvent(
            ServerLevel level,
            BlockPos collectorPos,
            long hvAmount,
            long ehvAmount) {
        this(level, collectorPos, hvAmount, ehvAmount, false);
    }

    /**
     * Full constructor.
     *
     * @param level          server level the strike occurred in
     * @param collectorPos   block position of the collector that captured the strike
     * @param hvAmount       initial HV amount that will be inserted (clamped to 0)
     * @param ehvAmount      initial EHV amount that will be inserted (clamped to 0)
     * @param naturalWeather whether the underlying lightning came from a natural
     *                       thunderstorm (vs a tagged trigger such as the
     *                       Lightning Rod or an Overload TNT explosion)
     * @since 1.0.3
     */
    public LightningCollectedEvent(
            ServerLevel level,
            BlockPos collectorPos,
            long hvAmount,
            long ehvAmount,
            boolean naturalWeather) {
        this.level = level;
        this.collectorPos = collectorPos;
        this.hvAmount = Math.max(0, hvAmount);
        this.ehvAmount = Math.max(0, ehvAmount);
        this.naturalWeather = naturalWeather;
    }

    /** The server level in which the lightning struck. */
    public ServerLevel getLevel() {
        return level;
    }

    /** The block position of the collector that captured the strike. */
    public BlockPos getCollectorPos() {
        return collectorPos;
    }

    /**
     * Whether the underlying lightning came from a natural thunderstorm.
     *
     * <p>{@code true} for organic, weather-driven strikes. {@code false} for
     * lightning produced by an item-tagged trigger (Lightning Rod, Overload TNT,
     * or any third-party trigger that posts the strike with the
     * {@code ae2lt.tagged_lightning} marker).</p>
     *
     * <p>Defaults to {@code false} when the event was constructed with the legacy
     * 4-argument constructor, so older callers still produce a well-defined
     * value.</p>
     *
     * @since 1.0.3
     */
    public boolean isNaturalWeather() {
        return naturalWeather;
    }

    /** High Voltage energy that will be added to the collector. */
    public long getHvAmount() {
        return hvAmount;
    }

    /** Override the amount of High Voltage energy to be stored. */
    public void setHvAmount(long hvAmount) {
        this.hvAmount = Math.max(0, hvAmount);
    }

    /** Extreme High Voltage energy that will be added to the collector. */
    public long getEhvAmount() {
        return ehvAmount;
    }

    /** Override the amount of Extreme High Voltage energy to be stored. */
    public void setEhvAmount(long ehvAmount) {
        this.ehvAmount = Math.max(0, ehvAmount);
    }

    /**
     * Returns the energy amount for the given tier.
     *
     * @param tier HV or EHV.
     * @return The current amount for that tier.
     */
    public long getAmount(LightningEnergyTier tier) {
        return switch (tier) {
            case HIGH_VOLTAGE -> hvAmount;
            case EXTREME_HIGH_VOLTAGE -> ehvAmount;
        };
    }

    /**
     * Sets the energy amount for the given tier.
     *
     * @param tier   HV or EHV.
     * @param amount New amount (clamped to non-negative).
     */
    public void setAmount(LightningEnergyTier tier, long amount) {
        switch (tier) {
            case HIGH_VOLTAGE -> setHvAmount(amount);
            case EXTREME_HIGH_VOLTAGE -> setEhvAmount(amount);
        }
    }
}
