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
 */
public class LightningCollectedEvent extends Event implements ICancellableEvent {

    private final ServerLevel level;
    private final BlockPos collectorPos;
    private long hvAmount;
    private long ehvAmount;

    public LightningCollectedEvent(
            ServerLevel level,
            BlockPos collectorPos,
            long hvAmount,
            long ehvAmount) {
        this.level = level;
        this.collectorPos = collectorPos;
        this.hvAmount = hvAmount;
        this.ehvAmount = ehvAmount;
    }

    /** The server level in which the lightning struck. */
    public ServerLevel getLevel() {
        return level;
    }

    /** The block position of the collector that captured the strike. */
    public BlockPos getCollectorPos() {
        return collectorPos;
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
